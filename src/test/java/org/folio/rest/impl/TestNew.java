package org.folio.rest.impl;

import org.folio.rest.RestVerticle;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.tools.PomReader;
import org.folio.rest.tools.utils.NetworkUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class TestNew {

  private static final Logger logger = LoggerFactory.getLogger(TestNew.class);

  private final int okapiPort = NetworkUtils.nextFreePort();
  private final int mockPort = NetworkUtils.nextFreePort();

  private final String RTAC_PATH = "/rtac/batch";
  private final String TEST_TENANT_ID = "test_tenant";
  private final String TEST_USER_ID = "30fde4be-2d1a-4546-8d6c-b468caca2720";
  private Header okapiTenantHeader = new Header("X-Okapi-Tenant", TEST_TENANT_ID);
  private Header okapiUrlHeader = new Header("X-Okapi-Url", "http://localhost:" + mockPort);
  private Header okapiUserHeader = new Header("X-Okapi-User-Id", TEST_USER_ID);
  private Header contentTypeHeader = new Header("Content-Type", "application/json");

  @BeforeAll
  void setUpOnce(Vertx vertx, VertxTestContext testContext) throws Exception {
    String moduleName = PomReader.INSTANCE.getModuleName()
      .replaceAll("_", "-");
    String moduleVersion = PomReader.INSTANCE.getVersion();
    String moduleId = moduleName + "-" + moduleVersion;
    logger.info("Test setup starting for " + moduleId);

    RestAssured.baseURI = "http://localhost:" + okapiPort;
    RestAssured.port = okapiPort;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    PostgresClient client = PostgresClient.getInstance(vertx);
    client.startEmbeddedPostgres();

    JsonObject dpConfig = new JsonObject();
    dpConfig.put("http.port", okapiPort);
    DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(dpConfig);

    vertx.deployVerticle(RestVerticle.class.getName(), deploymentOptions, testContext.succeeding(v -> {
        new MockServer(mockPort, vertx).start(testContext);
        testContext.completeNow(); //it also is called into start(testContext) method above
      }
    ));
  }

  @AfterAll
  void afterAll(Vertx vertx, VertxTestContext testContext) {
    vertx.close(testContext.succeeding(v -> testContext.completeNow()));
  }

  @Test
  void shouldReturnRtacResponse_whenPostValidInstanceId(VertxTestContext testContext) {
    testContext.verify(() -> {
      String validInstanceIdsJson = pojoToJson(MockData.VALID_INSTANCE_IDS_RTAC_REQUEST);
      RequestSpecification request = createBaseRequest(validInstanceIdsJson);
      request.when()
        .post()
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON);
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
    return RestAssured
      .given()
      .header(okapiTenantHeader)
      .header(okapiUrlHeader)
      .header(okapiUserHeader)
      .header(contentTypeHeader)
      .body(body)
      .basePath(RTAC_PATH);
  }

}