//package org.folio.rest.impl;
//
//import static io.restassured.RestAssured.given;
//import static org.folio.rtac.utils.Utils.encode;
//import static org.folio.rtac.utils.Utils.readMockFile;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.fail;
//
//import io.restassured.RestAssured;
//import io.restassured.http.ContentType;
//import io.restassured.http.Header;
//import io.restassured.http.Headers;
//import io.restassured.response.Response;
//import io.restassured.specification.RequestSpecification;
//import io.vertx.core.DeploymentOptions;
//import io.vertx.core.Vertx;
//import io.vertx.core.http.HttpServer;
//import io.vertx.core.json.JsonObject;
//import io.vertx.core.logging.Logger;
//import io.vertx.core.logging.LoggerFactory;
//import io.vertx.junit5.Checkpoint;
//import io.vertx.junit5.VertxExtension;
//import io.vertx.junit5.VertxTestContext;
//import java.util.stream.Stream;
//import org.folio.rest.RestVerticle;
//import org.folio.rest.tools.PomReader;
//import org.folio.rtac.utils.Utils;
//import org.hamcrest.Matchers;
//import org.junit.Ignore;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.Arguments;
//import org.junit.jupiter.params.provider.MethodSource;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@Ignore
//public class RtacResourceImplTest {
//
//
//  static {
//    System.setProperty("vertx.logger-delegate-factory-class-name",
//        "io.vertx.core.logging.Log4j2LogDelegateFactory");
//  }
//
//
//  private final Logger logger = LoggerFactory.getLogger(RtacResourceImplTest.class);
//  private static final String RTAC_URI = "/rtac/batch";
//  private final int okapiPort = Utils.getRandomPort();
//  private final int serverPort = Utils.getRandomPort();
//  private final Header tenantHeader = new Header("X-Okapi-Tenant", "rtacresourceimpltest");
//  private final Header urlHeader = new Header("X-Okapi-Url", "http://localhost:" + serverPort);
//  private final Header contentTypeHeader = new Header("Content-Type", "application/json");
//  private String moduleName;
//  private String moduleVersion;
//  private String moduleId;
//
//  static Stream<Arguments> rtacFailureCodes() {
//    return Stream.of(
//      // Even though we receive a 400, we need to return a 500 since there is nothing the client
//      // can do to correct the 400. We'd have to correct it in the code.
//      Arguments.of("400", 500),
//      Arguments.of("401", 401),
//      Arguments.of("403", 403),
//      Arguments.of("404", 404),
//      Arguments.of("500", 500),
//      Arguments.of("java.lang.NullPointerException", 500)
//    );
//  }
//
//  @BeforeEach
//  void setUp(Vertx vertx, VertxTestContext context) throws Exception {
//    vertx = Vertx.vertx();
//
//    moduleName = PomReader.INSTANCE.getModuleName().replaceAll("_", "-");
//    moduleVersion = PomReader.INSTANCE.getVersion();
//    moduleId = moduleName + "-" + moduleVersion;
//    logger.info("Test setup starting for " + moduleId);
//
//    final JsonObject conf = new JsonObject();
//    conf.put("http.port", okapiPort);
//
//    final Checkpoint verticleStarted = context.checkpoint(1);
//    final Checkpoint mockOkapiStarted = context.checkpoint(1);
//
//    final DeploymentOptions opt = new DeploymentOptions().setConfig(conf);
//    vertx.deployVerticle(RestVerticle.class.getName(), opt,
//        context.succeeding(id -> verticleStarted.flag()));
//    RestAssured.port = okapiPort;
//    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
//    logger.info("RTAC Resource Test Setup Done using port " + okapiPort);
//
//    final String host = "localhost";
//
//    final HttpServer server = vertx.createHttpServer();
//    server.requestHandler(req -> {
//      if (req.path().equals(String.format("/inventory/instances/%s",
//          "76d5a72a-af24-4ac6-8e73-4e39604f6f59"))) {
//        req.response()
//            .setStatusCode(200)
//            .putHeader("content-type", "application/json")
//            .end(readMockFile("RTACResourceImpl/success_instance.json"));
//      } else if (req.path().equals(String.format("/inventory/instances/%s",
//          "b285980f-a040-4e62-bc40-e097bec5b09e"))) {
//        req.response()
//            .setStatusCode(404)
//            .putHeader("content-type", "text/plain")
//            .end(readMockFile("RTACResourceImpl/not_found_instance.txt"));
//      } else if (req.path().equals(String.format("/inventory/instances/%s",
//          "1613697d-5e18-49b0-9613-08443b87cbc7"))) {
//        req.response()
//            .setStatusCode(200)
//            .putHeader("content-type", "application/json")
//            .end(readMockFile("RTACResourceImpl/success_instance_2.json"));
//      } else if (req.path().equals(String.format("/inventory/instances/%s",
//          "2ae0635e-5534-4b7d-b28f-f0816329baa3"))) {
//        req.response()
//            .setStatusCode(200)
//            .putHeader("content-type", "application/json")
//            .end(readMockFile("RTACResourceImpl/success_instance_3.json"));
//      } else if (req.path().equals(String.format("/inventory/instances/%s",
//          "0085f8ed-80ba-435b-8734-d3262aa4fc07"))) {
//        req.response()
//            .setStatusCode(200)
//            .putHeader("content-type", "application/json")
//            .end(readMockFile("RTACResourceImpl/success_instance_4.json"));
//      } else if (req.path().equals(String.format("/inventory/instances/%s",
//          "a50aa30b-33d0-4067-89cc-67a61df8bc84"))) {
//        req.response()
//            .setStatusCode(200)
//            .putHeader("content-type", "application/json")
//            .end(readMockFile("RTACResourceImpl/success_instance_5.json"));
//      } else if (req.path().equals("/holdings-storage/holdings")) {
//        if (req.query().equals(String.format("limit=%d&query=%s",
//            Integer.MAX_VALUE,
//            encode("instanceId==76d5a72a-af24-4ac6-8e73-4e39604f6f59")))) {
//          final String badDataValue = req.getHeader("x-okapi-bad-data");
//          if (badDataValue != null) {
//            if (badDataValue.equals("java.lang.NullPointerException")) {
//              req.response()
//                  .setStatusCode(200)
//                  .putHeader("content-type", "application/json")
//                  .end("{}");
//            } else {
//              req.response()
//                  .setStatusCode(Integer.parseInt(badDataValue))
//                  .putHeader("content-type", "text/plain")
//                  .end(badDataValue);
//            }
//          } else {
//            req.response()
//                .setStatusCode(200)
//                .putHeader("content-type", "application/json")
//                .end(readMockFile("RTACResourceImpl/success_holdings.json"));
//          }
//        } else if (req.query().equals(String.format("limit=%d&query=%s",
//            Integer.MAX_VALUE, encode("instanceId==1613697d-5e18-49b0-9613-08443b87cbc7")))) {
//          req.response()
//              .setStatusCode(200)
//              .putHeader("content-type", "application/json")
//              .end(readMockFile("RTACResourceImpl/success_no_holdings.json"));
//        } else if (req.query().equals(String.format("limit=%d&query=%s",
//            Integer.MAX_VALUE, encode("instanceId==2ae0635e-5534-4b7d-b28f-f0816329baa3")))) {
//          req.response()
//              .setStatusCode(200)
//              .putHeader("content-type", "application/json")
//              .end(readMockFile("RTACResourceImpl/success_holdings_2.json"));
//        } else if (req.query().equals(String.format("limit=%d&query=%s",
//            Integer.MAX_VALUE, encode("instanceId==0085f8ed-80ba-435b-8734-d3262aa4fc07")))) {
//          req.response()
//              .setStatusCode(200)
//              .putHeader("content-type", "application/json")
//              .end(readMockFile("RTACResourceImpl/success_holdings_3.json"));
//        } else if (req.query().equals(String.format("limit=%d&query=%s",
//            Integer.MAX_VALUE, encode("instanceId==a50aa30b-33d0-4067-89cc-67a61df8bc84")))) {
//          req.response()
//              .setStatusCode(200)
//              .putHeader("content-type", "application/json")
//              .end(readMockFile("RTACResourceImpl/success_holdings_4.json"));
//        } else {
//          req.response().setStatusCode(500).end("Unexpected call: " + req.path());
//        }
//      } else if (req.path().equals("/inventory/items")) {
//        if (req.query().equals(String.format("limit=%d&query=%s",
//            Integer.MAX_VALUE, encode("holdingsRecordId==13269e78-d7bd-4e7c-b06a-0a979238f3fd")))) {
//          req.response()
//              .setStatusCode(200)
//              .putHeader("content-type", "application/json")
//              .end(readMockFile("RTACResourceImpl/success_items_1.json"));
//        } else if (req.query().equals(String.format("limit=%d&query=%s",
//            Integer.MAX_VALUE, encode("holdingsRecordId==9fec4043-6963-4e2f-8e48-c12f914462bb")))) {
//          req.response()
//              .setStatusCode(200)
//              .putHeader("content-type", "application/json")
//              .end(readMockFile("RTACResourceImpl/success_items_2.json"));
//        } else if (req.query().equals(String.format("limit=%d&query=%s",
//            Integer.MAX_VALUE, encode("holdingsRecordId==2fad39ae-03f4-496f-9ae9-533ce08c7344")))) {
//          req.response()
//              .setStatusCode(200)
//              .putHeader("content-type", "application/json")
//              .end(readMockFile("RTACResourceImpl/success_items_3.json"));
//        } else if (req.query().equals(String.format("limit=%d&query=%s",
//            Integer.MAX_VALUE, encode("holdingsRecordId==aa3487a4-af65-4285-939c-05601b98827c")))) {
//          req.response()
//              .setStatusCode(200)
//              .putHeader("content-type", "application/json")
//              .end(readMockFile("RTACResourceImpl/success_items_4.json"));
//        } else if (req.query().equals(String.format("limit=%d&query=%s",
//            Integer.MAX_VALUE, encode("holdingsRecordId==2839b3fa-a47b-4283-bdc4-6ee54ac67b38")))) {
//          req.response()
//              .setStatusCode(200)
//              .putHeader("content-type", "application/json")
//              .end(readMockFile("RTACResourceImpl/success_items_5.json"));
//        } else {
//          req.response().setStatusCode(500).end("Unexpected call: " + req.path());
//        }
//      } else if (req.path().equals("/circulation/loans")) {
//        if (req.query().equals(String.format("limit=%d&query=%s",
//            Integer.MAX_VALUE,
//            encode("(itemId==1a2f476a-436b-4530-82fe-b21a22d55514 and status.name==Open)")))) {
//          req.response()
//              .setStatusCode(200)
//              .putHeader("content-type", "application/json")
//              .end(readMockFile("RTACResourceImpl/success_loans_1.json"));
//        } else if (req.query().equals(String.format("limit=%d&query=%s",
//            Integer.MAX_VALUE,
//            encode("(itemId==b116cfe0-b75f-4e12-bdb7-61b08505e164 and status.name==Open)")))) {
//          req.response()
//              .setStatusCode(200)
//              .putHeader("content-type", "application/json")
//              .end(readMockFile("RTACResourceImpl/success_loans_2.json"));
//        } else if (req.query().equals(String.format("limit=%d&query=%s",
//            Integer.MAX_VALUE,
//            encode("(itemId==a754a5d1-6bd4-4f41-8239-87b7afecccc6 and status.name==Open)")))) {
//          req.response()
//              .setStatusCode(200)
//              .putHeader("content-type", "application/json")
//              .end(readMockFile("RTACResourceImpl/success_loans_3.json"));
//        } else if (req.query().equals(String.format("limit=%d&query=%s",
//            Integer.MAX_VALUE,
//            encode("(itemId==650559de-a159-4620-a923-11f38cfcdb87 and status.name==Open)")))) {
//          req.response()
//              .setStatusCode(200)
//              .putHeader("content-type", "application/json")
//              .end(readMockFile("RTACResourceImpl/success_loans_4.json"));
//        } else if (req.query().equals(String.format("limit=%d&query=%s",
//            Integer.MAX_VALUE,
//            encode("(itemId==64a37e8b-1967-4a7e-a20f-d7d2da96e8f6 and status.name==Open)")))) {
//          req.response()
//              .setStatusCode(200)
//              .putHeader("content-type", "application/json")
//              .end(readMockFile("RTACResourceImpl/success_loans_5.json"));
//        } else if (req.query().equals(String.format("limit=%d&query=%s",
//            Integer.MAX_VALUE,
//            encode("(itemId==53462dc6-55aa-4b99-8717-6ae8bbef5331 and status.name==Open)")))) {
//          final String badDataValue = req.getHeader("x-okapi-bad-data");
//          if (badDataValue != null) {
//            req.response()
//                .setStatusCode(500)
//                .putHeader("content-type", "text/plain")
//                .end("Server Error");
//          } else {
//            req.response()
//                .setStatusCode(200)
//                .putHeader("content-type", "application/json")
//                .end(readMockFile("RTACResourceImpl/success_loans_6.json"));
//          }
//        } else {
//          // Instead of an error here, return no results. This way we don't have to supply each
//          // item ID above with an empty response. If we need to test errors, we can do something
//          // different.
//          req.response()
//                .setStatusCode(200)
//                .putHeader("content-type", "application/json")
//                .end(readMockFile("RTACResourceImpl/success_loans_3.json"));
//        }
//      } else {
//        req.response().setStatusCode(500).end("Unexpected call: " + req.path());
//      }
//    });
//
//    server.listen(serverPort, host, context.succeeding(id -> mockOkapiStarted.flag()));
//  }
//
//  @AfterEach
//  public void tearDown(Vertx vertx, VertxTestContext context) {
//    logger.info("RTAC Resource Testing Complete");
//    vertx.close(context.completing());
//  }
//
//  private String pojoToJson(Object pojo) {
//    try{
//      return new ObjectMapper().writeValueAsString(pojo);
//    } catch (JsonProcessingException ex) {
//      throw new IllegalArgumentException("Passed pojo object cannot be converted to json");
//    }
//  }
//
//  // DONE
// //start
////  @Test
////  public final void testGetRtacById() {
////    logger.info("Testing for successful RTAC by instance id");
////
////    RequestSpecification request = createBaseRequest(pojoToJson(MockData.requestWithValidInstanceId));
////    String response = request.when().post().then().statusCode(200).extract().response().asString();
////
////
////
////    // Test done
////    logger.info("Test done");
////  }
//
//  //DONE
//  @Test
//  public final void testGetRtacByIdNoDueDate() {
//    logger.info("Testing for successful RTAC by instance id");
//
//    final Response r = RestAssured
//          .given()
//          .header(tenantHeader)
//          .header(urlHeader)
//          .header(contentTypeHeader)
//          .when()
//          .post("/rtac/0085f8ed-80ba-435b-8734-d3262aa4fc07")
//          .then()
//          .contentType(ContentType.JSON)
//          .statusCode(200)
//          .extract()
//          .response();
//
//    final String body = r.getBody().asString();
//    final JsonObject json = new JsonObject(body);
//    final JsonObject expectedJson =
//        new JsonObject(readMockFile("RTACResourceImpl/success_rtac_response_no_dueDate.json"));
//
//    assertEquals(1, json.getJsonArray("holdings").size());
//    for (int i = 0; i < 1; i++) {
//      final JsonObject jo = json.getJsonArray("holdings").getJsonObject(i);
//      final String id = jo.getString("id");
//
//      boolean found = false;
//      for (int j = 0; j < 1; j++) {
//        final JsonObject expectedJO = expectedJson.getJsonArray("holdings").getJsonObject(j);
//        if (id.equals(expectedJO.getString("id"))) {
//          found = true;
//          assertEquals(expectedJO.getString("location"), jo.getString("location"));
//          assertEquals(expectedJO.getString("callNumber"), jo.getString("callNumber"));
//          assertEquals(expectedJO.getString("status"), jo.getString("status"));
//          assertEquals(expectedJO.getString("dueDate"), jo.getString("dueDate"));
//          assertEquals(expectedJO.getString("volume"), jo.getString("volume"));
//          break;
//        }
//      }
//
//      if (found == false) {
//        fail("Unexpected id: " + id);
//      }
//    }
//
//    // Test done
//    logger.info("Test done");
//  }
//
//  // DONE
//  @Test
//  public final void testGetRtacByIdVolumeFormatting() {
//    logger.info("Testing for proper volume string formatting");
//
//    final Response r = RestAssured
//          .given()
//          .header(tenantHeader)
//          .header(urlHeader)
//          .header(contentTypeHeader)
//          .when()
//          .get("/rtac/a50aa30b-33d0-4067-89cc-67a61df8bc84")
//          .then()
//          .contentType(ContentType.JSON)
//          .statusCode(200)
//          .extract()
//          .response();
//
//    final String body = r.getBody().asString();
//    final JsonObject json = new JsonObject(body);
//    final JsonObject expectedJson = new JsonObject(
//        readMockFile("RTACResourceImpl/success_rtac_response_volume_formatting.json"));
//
//    final int expectedSize = 8;
//    assertEquals(expectedSize, json.getJsonArray("holdings").size());
//    for (int i = 0; i < expectedSize; i++) {
//      final JsonObject jo = json.getJsonArray("holdings").getJsonObject(i);
//      final String id = jo.getString("id");
//
//      boolean found = false;
//      for (int j = 0; j < expectedSize; j++) {
//        final JsonObject expectedJO = expectedJson.getJsonArray("holdings").getJsonObject(j);
//        if (id.equals(expectedJO.getString("id"))) {
//          found = true;
//          assertEquals(expectedJO.getString("volume"), jo.getString("volume"));
//          break;
//        }
//      }
//
//      if (found == false) {
//        fail("Unexpected id: " + id);
//      }
//    }
//
//    // Test done
//    logger.info("Test done");
//  }
//
//  //DONE
//  @Test
//  public final void testGetRtacByIdErrorRetrievingLoan() {
//    logger.info("Testing for error when loan cannot be retrieved");
//
//    RestAssured
//          .given()
//          .headers(new Headers(tenantHeader, urlHeader, contentTypeHeader,
//            new Header("x-okapi-bad-data", "bad data")))
//          .when()
//          .get("/rtac/0085f8ed-80ba-435b-8734-d3262aa4fc07")
//          .then()
//          .contentType(ContentType.TEXT)
//          .statusCode(500)
//          .body(Matchers.is("Server Error"));
//
//    // Test done
//    logger.info("Test done");
//  }
//
//  //???
//  @Test
//  public final void testGetRtacById404() {
//    logger.info("Testing for 404 RTAC by unknown instance id");
//
//    RestAssured
//          .given()
//          .header(tenantHeader)
//          .header(urlHeader)
//          .header(contentTypeHeader)
//          .when()
//          .get("/rtac/b285980f-a040-4e62-bc40-e097bec5b09e")
//          .then()
//          .contentType(ContentType.TEXT)
//          .statusCode(404);
//
//    // Test done
//    logger.info("Test done");
//  }
//
////DONE
//  @Test
//  public final void testGetRtacByIdNoHoldings() {
//    logger.info("Testing for successful RTAC by instance id with no holdings");
//
//    final Response r = RestAssured
//          .given()
//          .header(tenantHeader)
//          .header(urlHeader)
//          .header(contentTypeHeader)
//          .when()
//          .get("/rtac/1613697d-5e18-49b0-9613-08443b87cbc7")
//          .then()
//          .contentType(ContentType.JSON)
//          .statusCode(200)
//          .extract()
//          .response();
//
//    final String body = r.getBody().asString();
//    final JsonObject json = new JsonObject(body);
//
//    assertEquals(0, json.getJsonArray("holdings").size());
//
//    // Test done
//    logger.info("Test done");
//  }
//
//  //IN NEW BEHAVIOUR IS THE SAME AS testGetRtacByIdNoHoldings and done into shouldReturnEmptyHoldings_whenInstanceHasNotItems
//  @Test
//  public final void testGetRtacByIdNoItems() {
//    logger.info("Testing for successful RTAC by instance id with no items");
//
//    final Response r = RestAssured
//        .given()
//        .header(tenantHeader)
//        .header(urlHeader)
//        .header(contentTypeHeader)
//        .when()
//        .get("/rtac/2ae0635e-5534-4b7d-b28f-f0816329baa3")
//        .then()
//        .contentType(ContentType.JSON)
//        .statusCode(200)
//        .extract()
//        .response();
//
//    final String body = r.getBody().asString();
//    final JsonObject json = new JsonObject(body);
//
//    assertEquals(0, json.getJsonArray("holdings").size());
//
//    // Test done
//    logger.info("Test done");
//  }
//
//  @ParameterizedTest
//  @MethodSource("rtacFailureCodes")
//  final void testGetRtacWithErrors(String codeString, int expectedCode) {
//    logger.info("Testing retrieving RTAC with a {} error", codeString);
//
//    given()
//      .headers(new Headers(tenantHeader, urlHeader, contentTypeHeader,
//        new Header("x-okapi-bad-data", codeString)))
//      .when()
//      .get("/rtac/76d5a72a-af24-4ac6-8e73-4e39604f6f59")
//      .then()
//      .log().all()
//      .and().assertThat().statusCode(expectedCode)
//      .and().assertThat().contentType(ContentType.TEXT)
//      .and().assertThat().body(Matchers.equalTo(codeString));
//
//    // Test done
//    logger.info("Complete - Testing retrieving RTAC with a {} error", codeString);
//  }
//
//  private RequestSpecification createBaseRequest(String body) {
//    return RestAssured
//      .given()
//      .header(tenantHeader)
//      .header(urlHeader)
//      .header(contentTypeHeader)
//      .body(body)
//      .basePath(RTAC_URI);
//  }
//}
