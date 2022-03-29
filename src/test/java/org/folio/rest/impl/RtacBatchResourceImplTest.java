package org.folio.rest.impl;

import static org.folio.rest.impl.MockData.INSTANCE_ID;
import static org.folio.rest.impl.MockData.UUID_400;
import static org.folio.rest.impl.MockData.UUID_403;
import static org.folio.rest.impl.MockData.UUID_404;
import static org.folio.rest.impl.MockData.UUID_500;
import static org.hamcrest.Matchers.is;
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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Stream;
import org.folio.rest.RestVerticle;
import org.folio.rest.jaxrs.model.Item;
import org.folio.rest.jaxrs.model.RtacHolding;
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
  private final int okapiPort = NetworkUtils.nextFreePort();
  private static int mockPort = NetworkUtils.nextFreePort();
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

  private static final String SERVER_ERROR = "Internal Server Error";
  private static final String RTAC_PATH = "/rtac-batch";
  private static final String TEST_TENANT_ID = "test_tenant";
  private static final String TEST_USER_ID = "30fde4be-2d1a-4546-8d6c-b468caca2720";
  private static final Header okapiTenantHeader = new Header("X-Okapi-Tenant", TEST_TENANT_ID);
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
          assertEquals(MockData.INSTANCE_ITEM_ID, holding.getId());
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
  void shouldSkipInstance_whenInstanceHasNoHoldings(VertxTestContext testContext) {
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
          assertTrue(rtacResponse.getHoldings().isEmpty());
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
              .stream()
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
