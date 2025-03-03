package org.folio.rest.impl;

import static org.folio.rest.impl.MockData.INSTANCE_ID;
import static org.folio.rest.impl.MockData.UUID_400;
import static org.folio.rest.impl.MockData.UUID_403;
import static org.folio.rest.impl.MockData.UUID_404;
import static org.folio.rest.impl.MockData.UUID_500;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.folio.HttpStatus;
import org.folio.rest.RestVerticle;
import org.folio.rest.jaxrs.model.Error;
import org.folio.rest.jaxrs.model.Holding;
import org.folio.rest.jaxrs.model.Item;
import org.folio.rest.jaxrs.model.RtacHolding;
import org.folio.rest.jaxrs.model.RtacHoldings;
import org.folio.rest.jaxrs.model.RtacHoldingsBatch;
import org.folio.rest.jaxrs.model.RtacRequest;
import org.folio.rest.tools.utils.NetworkUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(VertxExtension.class)
@TestInstance(PER_CLASS)
class RtacBatchResourceImplTest {

  public static final String TEST_CENTRAL_TENANT_ID = "test_central_tenant";
  public static final String TEST_TENANT_ID = "test_tenant";
  public static final String TEST_TENANT_0001_ID = "test_tenant_0001";
  private final int okapiPort = NetworkUtils.nextFreePort();
  private static int mockPort = NetworkUtils.nextFreePort();
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  private static final String SERVER_ERROR = "Internal Server Error";
  private static final String RTAC_PATH = "/rtac-batch";
  private static final String TEST_USER_ID = "30fde4be-2d1a-4546-8d6c-b468caca2720";
  private static final Header okapiTenantHeader = new Header("X-Okapi-Tenant", TEST_TENANT_ID);
  private static final Header okapiCentralTenantHeader = new Header("X-Okapi-Tenant",
      TEST_CENTRAL_TENANT_ID);
  private static final Header okapiUrlHeader =
      new Header("X-Okapi-Url", "http://localhost:" + mockPort);
  private static final Header okapiUserHeader = new Header("X-Okapi-User-Id", TEST_USER_ID);
  private static final Header contentTypeHeader = new Header("Content-Type", "application/json");

  @BeforeAll
  void setUpOnce(Vertx vertx, VertxTestContext testContext) throws Exception {
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

    RestAssured.baseURI = "http://localhost:" + okapiPort;
    RestAssured.port = okapiPort;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    JsonObject dpConfig = new JsonObject();
    dpConfig.put("http.port", okapiPort);
    DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(dpConfig);

    vertx.deployVerticle(
        RestVerticle.class.getName(),
        deploymentOptions,
        testContext.succeeding(
            v -> {
              new MockServer(mockPort, vertx).start(testContext);
              testContext.completeNow();
            }));
  }

  @AfterAll
  void afterAll(Vertx vertx, VertxTestContext testContext) {
    vertx.close(testContext.succeeding(v -> testContext.completeNow()));
  }

  @Test
  void shouldReturnRtacResponse_whenLegacyApiIsCalledWIthCalidId(VertxTestContext testContext) {
    testContext.verify(
        () -> {
          RequestSpecification request =
              RestAssured.given()
                  .header(okapiTenantHeader)
                  .header(okapiUrlHeader)
                  .header(okapiUserHeader)
                  .header(contentTypeHeader);
          request
              .when()
              .get("/rtac/" + INSTANCE_ID)
              .then()
              .statusCode(200)
              .contentType(ContentType.JSON);
          testContext.completeNow();
        });
  }

  @Test
  void shouldReturnRtacResponse_whenPostValidInstanceIds(VertxTestContext testContext) {
    testContext.verify(
        () -> {
          String validInstanceIdsJson = pojoToJson(MockData.VALID_INSTANCE_IDS_RTAC_REQUEST);
          RequestSpecification request = createBaseRequest(validInstanceIdsJson);
          request.when().post().then().statusCode(200).contentType(ContentType.JSON);
          testContext.completeNow();
        });
  }

  @Test
  void shouldProperlyFormatTheHoldingValueField(VertxTestContext testContext) {
    testContext.verify(
        () -> {
          String validInstanceIdsJson = pojoToJson(MockData.VALID_INSTANCE_IDS_RTAC_REQUEST);
          RequestSpecification request = createBaseRequest(validInstanceIdsJson);
          String body =
              request
                  .when()
                  .post()
                  .then()
                  .statusCode(200)
                  .contentType(ContentType.JSON)
                  .extract()
                  .body()
                  .asString();
          RtacHoldingsBatch response = MockData.stringToPojo(body, RtacHoldingsBatch.class);
          RtacHolding holding =
              response.getHoldings().iterator().next().getHoldings().iterator().next();
          Item item = MockData.INSTANCE_WITH_HOLDINGS_AND_ITEMS.getItems().iterator().next();
          String expectedVolume = "(" + item.getEnumeration() + " " + item.getChronology() + ")";
          assertEquals(expectedVolume, holding.getVolume());
          testContext.completeNow();
        });
  }

  @Test
  void shouldProperlyFormatTheHoldingValueFieldWithCopyNumbers(VertxTestContext testContext) {
    testContext.verify(
        () -> {
          String validInstanceIdsJson = pojoToJson(MockData.VALID_INSTANCE_IDS_RTAC_REQUEST);
          RequestSpecification request = createBaseRequest(validInstanceIdsJson);
          String body =
              request
                  .when()
                  .post()
                  .then()
                  .statusCode(200)
                  .contentType(ContentType.JSON)
                  .extract()
                  .body()
                  .asString();
          RtacHoldingsBatch response = MockData.stringToPojo(body, RtacHoldingsBatch.class);
          RtacHolding rtacHolding =
              response.getHoldings().iterator().next().getHoldings().iterator().next();
          Item item = MockData.INSTANCE_WITH_HOLDINGS_AND_ITEMS.getItems().iterator().next();
          Holding holding =
              MockData.INSTANCE_WITH_HOLDINGS_AND_ITEMS.getHoldings().iterator().next();
          assertEquals(item.getCopyNumber(), rtacHolding.getItemCopyNumber());
          assertEquals(holding.getCopyNumber(), rtacHolding.getHoldingsCopyNumber());
          testContext.completeNow();
        });
  }

  @Test
  void shouldPopulateItemDataWithDueDate_whenLoanForItemExists(VertxTestContext testContext) {
    testContext.verify(
        () -> {
          String validInstanceIdsJson = pojoToJson(MockData.VALID_INSTANCE_IDS_RTAC_REQUEST);
          RequestSpecification request = createBaseRequest(validInstanceIdsJson);
          String body =
              request
                  .when()
                  .post()
                  .then()
                  .statusCode(200)
                  .contentType(ContentType.JSON)
                  .extract()
                  .body()
                  .asString();
          RtacHoldingsBatch rtacResponse = MockData.stringToPojo(body, RtacHoldingsBatch.class);
          RtacHolding holding = getSingleHolding(rtacResponse);
          final var expected = dateFormat.parse(MockData.LOAN_DUE_DATE_FIELD_VALUE);
          assertEquals(expected, holding.getDueDate());
          assertEquals(MockData.INSTANCE_ITEM_ID_1, holding.getId());
          testContext.completeNow();
        });
  }

  @Test
  void shouldReturnRtacResponse_PiecesDataAvailable(VertxTestContext testContext) {
    testContext.verify(
        () -> {
          String validInstanceIdsJson = pojoToJson(MockData.RTAC_REQUEST_WITH_INSTANCE_AND_PIECES);
          RequestSpecification request = createBaseRequest(validInstanceIdsJson);
          String body =
              request
                  .when()
                  .post()
                  .then()
                  .statusCode(200)
                  .contentType(ContentType.JSON)
                  .extract()
                  .body()
                  .asString();

          RtacHoldingsBatch rtacResponse = MockData.stringToPojo(body, RtacHoldingsBatch.class);
          List<RtacHolding> holdings = rtacResponse.getHoldings().get(0).getHoldings();
          assertThat(holdings, hasItem(anyOf(
              hasProperty("status", equalTo("Expected")),
              hasProperty("status", equalTo("Received"))
          )));
          testContext.completeNow();
        });
  }

  @Test
  void shouldReturnRtacResponse_whenPiecesNotExist(VertxTestContext testContext) {
    testContext.verify(
        () -> {
          String validInstanceIdsJson = pojoToJson(MockData.VALID_INSTANCE_IDS_RTAC_REQUEST);
          RequestSpecification request = createBaseRequest(validInstanceIdsJson);
          String body =
              request
                  .when()
                  .post()
                  .then()
                  .statusCode(200)
                  .contentType(ContentType.JSON)
                  .extract()
                  .body()
                  .asString();

          RtacHoldingsBatch rtacResponse = MockData.stringToPojo(body, RtacHoldingsBatch.class);
          List<RtacHolding> holdings = rtacResponse.getHoldings().get(0).getHoldings();
          assertFalse(holdings.isEmpty());
          assertThat(holdings, hasItem(anyOf(
              hasProperty("status", not("Expected")),
              hasProperty("status", not("Received"))
          )));
          testContext.completeNow();
        });
  }

  @Test
  void shouldReturnError_whenInstanceIdNotValidUuid(VertxTestContext testContext) {
    testContext.verify(
        () -> {
          String validInstanceIdsJson = pojoToJson(MockData.RTAC_REQUEST_WITH_NOT_VALID_IDS);
          RequestSpecification request = createBaseRequest(validInstanceIdsJson);
          String body =
              request
                  .when()
                  .post()
                  .then()
                  .statusCode(HttpStatus.HTTP_NOT_FOUND.toInt())
                  .contentType(ContentType.TEXT)
                  .extract()
                  .body()
                  .asString();
          assertEquals("Could not find instances", body);
          testContext.completeNow();
        });
  }

  @Test
  void shouldReturnError_whenInstanceNotExists(VertxTestContext testContext) {
    testContext.verify(
        () -> {
          String validInstanceIdsJson = pojoToJson(MockData.RTAC_REQUEST_WITH_EMPTY_RESPONSE);
          RequestSpecification request = createBaseRequest(validInstanceIdsJson);
          String body =
              request
                  .when()
                  .post()
                  .then()
                  .statusCode(HttpStatus.HTTP_OK.toInt())
                  .contentType(ContentType.JSON)
                  .extract()
                  .body()
                  .asString();
          RtacHoldingsBatch rtacResponse = MockData.stringToPojo(body, RtacHoldingsBatch.class);
          Error error = rtacResponse.getErrors().get(0);
          String msg = "Instance 16757796-da9a-4435-959b-88ce4f2ec272 can not be retrieved";
          assertEquals(error.getMessage(), msg);
          testContext.completeNow();
        });
  }

  @Test
  void shouldReturnItemDataWithoutDueDate_whenLoanForInstanceItemDoesNotExist(
      VertxTestContext testContext) {
    testContext.verify(
        () -> {
          String validInstanceIdsJson =
              pojoToJson(MockData.RTAC_REQUEST_WITH_INSTANCE_NO_LOANS_ITEM);
          RequestSpecification request = createBaseRequest(validInstanceIdsJson);
          String body =
              request
                  .when()
                  .post()
                  .then()
                  .statusCode(200)
                  .contentType(ContentType.JSON)
                  .extract()
                  .body()
                  .asString();
          RtacHoldingsBatch rtacResponse = MockData.stringToPojo(body, RtacHoldingsBatch.class);
          RtacHolding holding = getSingleHolding(rtacResponse);
          assertTrue(Objects.isNull(holding.getDueDate()));
          assertEquals(MockData.ITEM_WITHOUT_LOAN_ID, holding.getId());
          testContext.completeNow();
        });
  }

  @Test
  void shouldRespondWithInternalServerError_whenErrorOccurredWhileRetrievingInstances(
      VertxTestContext testContext) {
    testContext.verify(
        () -> {
          String validInstanceIdsJson =
              pojoToJson(MockData.RTAC_REQUEST_WITH_INSTANCE_ID_INVENTORY_VIEW_ERROR);
          RequestSpecification request = createBaseRequest(validInstanceIdsJson);
          request
              .when()
              .post()
              .then()
              .statusCode(500)
              .contentType(ContentType.TEXT)
              .body(is(SERVER_ERROR));
          testContext.completeNow();
        });
  }

  @Test
  void shouldAttachError_whenInstanceHasNoHoldings(VertxTestContext testContext) {
    testContext.verify(
        () -> {
          String validInstanceIdsJson =
              pojoToJson(MockData.RTAC_REQUEST_WITH_INSTANCE_NO_ITEMS_AND_HOLDINGS);
          RequestSpecification request = createBaseRequest(validInstanceIdsJson);
          String body =
              request
                  .when()
                  .post()
                  .then()
                  .statusCode(200)
                  .contentType(ContentType.JSON)
                  .extract()
                  .body()
                  .asString();
          RtacHoldingsBatch rtacResponse = MockData.stringToPojo(body, RtacHoldingsBatch.class);
          assertTrue(rtacResponse.getHoldings().get(0).getHoldings().isEmpty());
          assertEquals("4ed2a3b3-2fb4-414c-aa6f-a265685ca5a6",
              rtacResponse.getHoldings().get(0).getInstanceId());
          Error error = rtacResponse.getErrors().get(0);
          String msg = "Holdings not found for instance 4ed2a3b3-2fb4-414c-aa6f-a265685ca5a6";
          assertEquals(error.getMessage(), msg);
          testContext.completeNow();
        });
  }

  @Test
  void shouldRetrunResponseAndAttachError_whenInstanceWithItemsAndInstanceHasNoHoldings(
      VertxTestContext testContext) {
    testContext.verify(
        () -> {
          String validInstanceIdsJson =
              pojoToJson(MockData.RTAC_REQUEST_MIXED_INSTANCES_WITH_ITEMS_AND_NO_HOLDINGS);
          RequestSpecification request = createBaseRequest(validInstanceIdsJson);
          String body =
              request
                  .when()
                  .post()
                  .then()
                  .statusCode(200)
                  .contentType(ContentType.JSON)
                  .extract()
                  .body()
                  .asString();
          RtacHoldingsBatch rtacResponse = MockData.stringToPojo(body, RtacHoldingsBatch.class);
          assertEquals(2, rtacResponse.getHoldings().size());
          RtacHoldings rtacHoldingWithoutHoldings = rtacResponse.getHoldings().stream()
              .filter(rtacHolding ->
                  rtacHolding.getInstanceId().equals("4ed2a3b3-2fb4-414c-aa6f-a265685ca5a6"))
              .findFirst().get();
          assertTrue(rtacHoldingWithoutHoldings.getHoldings().isEmpty());
          Error error = rtacResponse.getErrors().get(0);
          String msg = "Holdings not found for instance 4ed2a3b3-2fb4-414c-aa6f-a265685ca5a6";
          assertEquals(error.getMessage(), msg);
          RtacHoldings rtacHoldingWithHoldings = rtacResponse.getHoldings().stream()
              .filter(rtacHolding ->
                  rtacHolding.getInstanceId().equals("76d5a72a-af24-4ac6-8e73-4e39604f6f59"))
              .findFirst().get();
          assertFalse(rtacHoldingWithHoldings.getHoldings().isEmpty());
          testContext.completeNow();
        });
  }

  @Test
  void shouldAttachError_whenInstanceIsNotFound(VertxTestContext testContext) {
    testContext.verify(
        () -> {
          String invalidInstanceIdsJson =
              pojoToJson(MockData.RTAC_REQUEST_WITH_NON_EXISTED_INSTANCE_ID);
          RequestSpecification request = createBaseRequest(invalidInstanceIdsJson);
          String body =
              request
                .when()
                .post()
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body()
                .asString();
          RtacHoldingsBatch rtacResponse = MockData.stringToPojo(body, RtacHoldingsBatch.class);
          Error error = rtacResponse.getErrors().get(0);
          String msg = "Instance 207dda4d-06dd-4822-856e-63ca5b6c7f1a can not be retrieved";
          assertEquals(error.getMessage(), msg);
          testContext.completeNow();
        });
  }

  @Test
  void shouldProvideHoldingsData_whenInstancesWithAndWithoutItemsRequested(
      VertxTestContext testContext) {
    testContext.verify(
        () -> {
          String validInstanceIdsJson =
              pojoToJson(MockData.RTAC_REQUEST_MIXED_INSTANCES_WITH_ITEMS_AND_NO_ITEMS);
          RequestSpecification request = createBaseRequest(validInstanceIdsJson);
          String body =
              request
                .when()
                .post()
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body()
                .asString();

            RtacHoldingsBatch rtacResponse = MockData.stringToPojo(body, RtacHoldingsBatch.class);
            assertTrue(rtacResponse.getErrors().isEmpty());
            rtacResponse
              .getHoldings()
              .forEach(holding -> assertFalse(holding.getHoldings().isEmpty()));
            testContext.completeNow();
        });
  }

  @Test
  void shouldProvideHoldingDataForPeriodicalsWithNoItemsWhenFullPeriodicalsFalse(
      VertxTestContext testContext) {
    testContext.verify(
        () -> {
          String validInstanceIdsJson =
              pojoToJson(MockData.RTAC_REQUEST_WITH_INSTANCE_HOLDINGS_NO_ITEMS);
          RequestSpecification request = createBaseRequest(validInstanceIdsJson);
          String body =
              request
                  .when()
                  .post()
                  .then()
                  .statusCode(200)
                  .contentType(ContentType.JSON)
                  .extract()
                  .body()
                  .asString();
          RtacHoldingsBatch rtacResponse = MockData.stringToPojo(body, RtacHoldingsBatch.class);
          assertFalse(rtacResponse.getHoldings().isEmpty());
          testContext.completeNow();
      });
  }

  @Test
  void shouldProvideHoldingDataFromMemberTenantsWhenFolioIsInConsortia(
      VertxTestContext testContext) {
    testContext.verify(
        () -> {
          String validInstanceIdsJson =
              pojoToJson(MockData.RTAC_REQUEST_WITH_INSTANCE_IN_CONSORTIA);
          RequestSpecification request = createBaseRequest(validInstanceIdsJson);
          request.header(okapiCentralTenantHeader);
          String body =
              request
                  .when()
                  .post()
                  .then()
                  .statusCode(200)
                  .contentType(ContentType.JSON)
                  .extract()
                  .body()
                  .asString();
          RtacHoldingsBatch rtacResponse = MockData.stringToPojo(body, RtacHoldingsBatch.class);
          assertEquals(4, rtacResponse.getHoldings().get(0).getHoldings().size());
          testContext.completeNow();
        });
  }

  private String pojoToJson(Object pojo) {
    try {
      return new ObjectMapper().writeValueAsString(pojo);
    } catch (JsonProcessingException ex) {
      throw new IllegalArgumentException("Passed pojo object cannot be converted to json");
    }
  }

  private RequestSpecification createBaseRequest(String body) {
    return RestAssured.given()
        .header(okapiTenantHeader)
        .header(okapiUrlHeader)
        .header(okapiUserHeader)
        .header(contentTypeHeader)
        .body(body)
        .basePath(RTAC_PATH);
  }

  private RtacHolding getSingleHolding(RtacHoldingsBatch rtacResponse) {
    return rtacResponse.getHoldings().iterator().next().getHoldings().iterator().next();
  }

  @ParameterizedTest
  @MethodSource("rtacFailureCodes")
  final void testGetRtacWithErrors(String codeString, int expectedCode) {

    final var rtacRequest = new RtacRequest();
    rtacRequest.setInstanceIds(List.of(codeString));
    RequestSpecification request = createBaseRequest(pojoToJson(rtacRequest));

    request
        .headers(new Headers(okapiTenantHeader, okapiUrlHeader, contentTypeHeader))
        .when()
        .post()
        .then()
        .log()
        .all()
        .and()
        .assertThat()
        .statusCode(expectedCode)
        .and()
        .assertThat()
        .contentType(ContentType.TEXT);

  }

  static Stream<Arguments> rtacFailureCodes() {
    return Stream.of(
        // Even though we receive a 400, we need to return a 500 since there is nothing the client
        // can do to correct the 400. We'd have to correct it in the code.
        Arguments.of(UUID_400, 500),
        Arguments.of(UUID_403, 403),
        Arguments.of(UUID_404, 404),
        Arguments.of(UUID_500, 500));
  }
}
