package org.folio.rest.impl;

import static org.folio.rtac.utils.Utils.readMockFile;

import org.folio.rest.RestVerticle;
import org.folio.rest.tools.PomReader;
import org.folio.rtac.utils.Utils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class RTACResourceImplTest {
  private final Logger logger = LoggerFactory.getLogger(RTACResourceImplTest.class);

  private Vertx vertx;
  private String moduleName;
  private String moduleVersion;
  private String moduleId;

  private final int okapiPort = Utils.getRandomPort();
  private final int serverPort = Utils.getRandomPort();

  private final Header tenantHeader = new Header("X-Okapi-Tenant", "rtacresourceimpltest");
  private final Header urlHeader = new Header("X-Okapi-Url", "http://localhost:" + serverPort);
  private final Header contentTypeHeader = new Header("Content-Type", "application/json");


  @Before
  public void setUp(TestContext context) throws Exception {
    vertx = Vertx.vertx();

    moduleName = PomReader.INSTANCE.getModuleName().replaceAll("_", "-");
    moduleVersion = PomReader.INSTANCE.getVersion();
    moduleId = moduleName + "-" + moduleVersion;
    logger.info("Test setup starting for " + moduleId);

    final JsonObject conf = new JsonObject();
    conf.put("http.port", okapiPort);

    final DeploymentOptions opt = new DeploymentOptions().setConfig(conf);
    vertx.deployVerticle(RestVerticle.class.getName(), opt, context.asyncAssertSuccess());
    RestAssured.port = okapiPort;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    logger.info("RTAC Resource Test Setup Done using port " + okapiPort);

    final String host = "localhost";

    final Async async = context.async();
    final HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals(String.format("/inventory/instances/%s", "76d5a72a-af24-4ac6-8e73-4e39604f6f59"))) {
        req.response()
          .setStatusCode(200)
          .putHeader("content-type", "application/json")
          .end(readMockFile("RTACResourceImpl/success_instance.json"));
      } else if (req.path().equals(String.format("/inventory/instances/%s", "b285980f-a040-4e62-bc40-e097bec5b09e"))) {
        req.response()
          .setStatusCode(404)
          .putHeader("content-type", "text/plain")
          .end(readMockFile("RTACResourceImpl/not_found_instance.txt"));
      } else if (req.path().equals(String.format("/inventory/instances/%s", "1613697d-5e18-49b0-9613-08443b87cbc7"))) {
        req.response()
          .setStatusCode(200)
          .putHeader("content-type", "application/json")
          .end(readMockFile("RTACResourceImpl/success_instance_2.json"));
      } else if (req.path().equals(String.format("/inventory/instances/%s", "2ae0635e-5534-4b7d-b28f-f0816329baa3"))) {
        req.response()
          .setStatusCode(200)
          .putHeader("content-type", "application/json")
          .end(readMockFile("RTACResourceImpl/success_instance_3.json"));
      } else if (req.path().equals(String.format("/inventory/instances/%s", "0085f8ed-80ba-435b-8734-d3262aa4fc07"))) {
        req.response()
          .setStatusCode(200)
          .putHeader("content-type", "application/json")
          .end(readMockFile("RTACResourceImpl/success_instance_4.json"));
      } else if (req.path().equals("/holdings-storage/holdings")) {
        if (req.query().equals(String.format("limit=100&query=instanceId%%3D%%3D%s", "76d5a72a-af24-4ac6-8e73-4e39604f6f59"))) {
          req.response()
            .setStatusCode(200)
            .putHeader("content-type", "application/json")
            .end(readMockFile("RTACResourceImpl/success_holdings.json"));
        } else if (req.query().equals(String.format("limit=100&query=instanceId%%3D%%3D%s", "1613697d-5e18-49b0-9613-08443b87cbc7"))) {
          req.response()
            .setStatusCode(200)
            .putHeader("content-type", "application/json")
            .end(readMockFile("RTACResourceImpl/success_no_holdings.json"));
        } else if (req.query().equals(String.format("limit=100&query=instanceId%%3D%%3D%s", "2ae0635e-5534-4b7d-b28f-f0816329baa3"))) {
          req.response()
            .setStatusCode(200)
            .putHeader("content-type", "application/json")
            .end(readMockFile("RTACResourceImpl/success_holdings_2.json"));
        } else if (req.query().equals(String.format("limit=100&query=instanceId%%3D%%3D%s", "0085f8ed-80ba-435b-8734-d3262aa4fc07"))) {
          req.response()
            .setStatusCode(200)
            .putHeader("content-type", "application/json")
            .end(readMockFile("RTACResourceImpl/success_holdings_3.json"));
        } else {
          req.response().setStatusCode(500).end("Unexpected call: " + req.path());
        }
      } else if (req.path().equals("/inventory/items")) {
        if (req.query().equals(String.format("limit=100&query=holdingsRecordId%%3D%%3D%s", "13269e78-d7bd-4e7c-b06a-0a979238f3fd"))) {
          req.response()
            .setStatusCode(200)
            .putHeader("content-type", "application/json")
            .end(readMockFile("RTACResourceImpl/success_items_1.json"));
        } else if (req.query().equals(String.format("limit=100&query=holdingsRecordId%%3D%%3D%s", "9fec4043-6963-4e2f-8e48-c12f914462bb"))) {
          req.response()
            .setStatusCode(200)
            .putHeader("content-type", "application/json")
            .end(readMockFile("RTACResourceImpl/success_items_2.json"));
        } else if (req.query().equals(String.format("limit=100&query=holdingsRecordId%%3D%%3D%s", "2fad39ae-03f4-496f-9ae9-533ce08c7344"))) {
          req.response()
            .setStatusCode(200)
            .putHeader("content-type", "application/json")
            .end(readMockFile("RTACResourceImpl/success_items_3.json"));
        } else if (req.query().equals(String.format("limit=100&query=holdingsRecordId%%3D%%3D%s", "aa3487a4-af65-4285-939c-05601b98827c"))) {
          req.response()
            .setStatusCode(200)
            .putHeader("content-type", "application/json")
            .end(readMockFile("RTACResourceImpl/success_items_4.json"));
        } else {
          req.response().setStatusCode(500).end("Unexpected call: " + req.path());
        }
      } else if (req.path().equals("/circulation/loans")) {
        if (req.query().equals(String.format("limit=100&query=%%28itemId%%3D%%3D%s%%20and%%20status.name%%3D%%3DOpen%%29", "1a2f476a-436b-4530-82fe-b21a22d55514"))) {
          req.response()
            .setStatusCode(200)
            .putHeader("content-type", "application/json")
            .end(readMockFile("RTACResourceImpl/success_loans_1.json"));
        } else if (req.query().equals(String.format("limit=100&query=%%28itemId%%3D%%3D%s%%20and%%20status.name%%3D%%3DOpen%%29", "b116cfe0-b75f-4e12-bdb7-61b08505e164"))) {
          req.response()
            .setStatusCode(200)
            .putHeader("content-type", "application/json")
            .end(readMockFile("RTACResourceImpl/success_loans_2.json"));
        } else if (req.query().equals(String.format("limit=100&query=%%28itemId%%3D%%3D%s%%20and%%20status.name%%3D%%3DOpen%%29", "a754a5d1-6bd4-4f41-8239-87b7afecccc6"))) {
          req.response()
            .setStatusCode(200)
            .putHeader("content-type", "application/json")
            .end(readMockFile("RTACResourceImpl/success_loans_3.json"));
        } else if (req.query().equals(String.format("limit=100&query=%%28itemId%%3D%%3D%s%%20and%%20status.name%%3D%%3DOpen%%29", "650559de-a159-4620-a923-11f38cfcdb87"))) {
          req.response()
            .setStatusCode(200)
            .putHeader("content-type", "application/json")
            .end(readMockFile("RTACResourceImpl/success_loans_4.json"));
        } else if (req.query().equals(String.format("limit=100&query=%%28itemId%%3D%%3D%s%%20and%%20status.name%%3D%%3DOpen%%29", "64a37e8b-1967-4a7e-a20f-d7d2da96e8f6"))) {
          req.response()
            .setStatusCode(200)
            .putHeader("content-type", "application/json")
            .end(readMockFile("RTACResourceImpl/success_loans_5.json"));
        } else if (req.query().equals(String.format("limit=100&query=%%28itemId%%3D%%3D%s%%20and%%20status.name%%3D%%3DOpen%%29", "53462dc6-55aa-4b99-8717-6ae8bbef5331"))) {
          req.response()
            .setStatusCode(200)
            .putHeader("content-type", "application/json")
            .end(readMockFile("RTACResourceImpl/success_loans_6.json"));
        } else {
          req.response().setStatusCode(500).end("Unexpected call: " + req.path());
        }
      } else {
        req.response().setStatusCode(500).end("Unexpected call: " + req.path());
      }
    });

    server.listen(serverPort, host, ar -> {
      context.assertTrue(ar.succeeded());
      async.complete();
    });
  }

  @After
  public void tearDown(TestContext context) {
    logger.info("RTAC Resource Testing Complete");
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public final void testGetRtacById(TestContext context) {
    logger.info("Testing for successful RTAC by instance id");
    final Async asyncLocal = context.async();

    final Response r = RestAssured
      .given()
        .header(tenantHeader)
        .header(urlHeader)
        .header(contentTypeHeader)
      .get("/rtac/76d5a72a-af24-4ac6-8e73-4e39604f6f59")
        .then()
          .contentType(ContentType.JSON)
          .statusCode(200).extract().response();

    final String body = r.getBody().asString();
    final JsonObject json = new JsonObject(body);
    final JsonObject expectedJson = new JsonObject(readMockFile("RTACResourceImpl/success_rtac_response.json"));

    context.assertEquals(5, json.getJsonArray("holdings").size());
    for (int i = 0; i < 5; i++) {
      final JsonObject jo = json.getJsonArray("holdings").getJsonObject(i);
      final String id = jo.getString("id");

      boolean found = false;
      for (int j = 0; j < 5; j++) {
        final JsonObject expectedJO = expectedJson.getJsonArray("holdings").getJsonObject(j);
        if (id.equals(expectedJO.getString("id"))) {
          found = true;
          context.assertEquals(expectedJO.getString("location"), jo.getString("location"));
          context.assertEquals(expectedJO.getString("callNumber"), jo.getString("callNumber"));
          context.assertEquals(expectedJO.getString("status"), jo.getString("status"));
          context.assertEquals(expectedJO.getString("dueDate"), jo.getString("dueDate"));
          context.assertEquals(expectedJO.getString("tempLocation"), jo.getString("tempLocation"));
          break;
        }
      }

      if (found == false) {
        context.fail("Unexpected id: " + id);
      }
    }

    asyncLocal.complete();

    // Test done
    logger.info("Test done");
  }

  @Test
  public final void testGetRtacByIdNoDueDate(TestContext context) {
    logger.info("Testing for successful RTAC by instance id");
    final Async asyncLocal = context.async();

    final Response r = RestAssured
      .given()
        .header(tenantHeader)
        .header(urlHeader)
        .header(contentTypeHeader)
      .get("/rtac/0085f8ed-80ba-435b-8734-d3262aa4fc07")
        .then()
          .contentType(ContentType.JSON)
          .statusCode(200).extract().response();

    final String body = r.getBody().asString();
    final JsonObject json = new JsonObject(body);
    final JsonObject expectedJson = new JsonObject(readMockFile("RTACResourceImpl/success_rtac_response_no_dueDate.json"));

    context.assertEquals(1, json.getJsonArray("holdings").size());
    for (int i = 0; i < 1; i++) {
      final JsonObject jo = json.getJsonArray("holdings").getJsonObject(i);
      final String id = jo.getString("id");

      boolean found = false;
      for (int j = 0; j < 1; j++) {
        final JsonObject expectedJO = expectedJson.getJsonArray("holdings").getJsonObject(j);
        if (id.equals(expectedJO.getString("id"))) {
          found = true;
          context.assertEquals(expectedJO.getString("location"), jo.getString("location"));
          context.assertEquals(expectedJO.getString("callNumber"), jo.getString("callNumber"));
          context.assertEquals(expectedJO.getString("status"), jo.getString("status"));
          context.assertEquals(expectedJO.getString("dueDate"), jo.getString("dueDate"));
          context.assertEquals(expectedJO.getString("tempLocation"), jo.getString("tempLocation"));
          break;
        }
      }

      if (found == false) {
        context.fail("Unexpected id: " + id);
      }
    }

    asyncLocal.complete();

    // Test done
    logger.info("Test done");
  }

  @Test
  public final void testGetRtacById404(TestContext context) {
    logger.info("Testing for 404 RTAC by unknown instance id");
    final Async asyncLocal = context.async();

    RestAssured
      .given()
        .header(tenantHeader)
        .header(urlHeader)
        .header(contentTypeHeader)
      .get("/rtac/b285980f-a040-4e62-bc40-e097bec5b09e")
        .then()
          .contentType(ContentType.TEXT)
          .statusCode(404);

    asyncLocal.complete();

    // Test done
    logger.info("Test done");
  }

  @Test
  public final void testGetRtacByIdNoHoldings(TestContext context) {
    logger.info("Testing for successful RTAC by instance id with no holdings");
    final Async asyncLocal = context.async();

    final Response r = RestAssured
      .given()
        .header(tenantHeader)
        .header(urlHeader)
        .header(contentTypeHeader)
      .get("/rtac/1613697d-5e18-49b0-9613-08443b87cbc7")
        .then()
          .contentType(ContentType.JSON)
          .statusCode(200).extract().response();

    final String body = r.getBody().asString();
    final JsonObject json = new JsonObject(body);

    context.assertEquals(0, json.getJsonArray("holdings").size());

    asyncLocal.complete();

    // Test done
    logger.info("Test done");
  }

  @Test
  public final void testGetRtacByIdNoItems(TestContext context) {
    logger.info("Testing for successful RTAC by instance id with no items");
    final Async asyncLocal = context.async();

    final Response r = RestAssured
      .given()
        .header(tenantHeader)
        .header(urlHeader)
        .header(contentTypeHeader)
      .get("/rtac/2ae0635e-5534-4b7d-b28f-f0816329baa3")
        .then()
          .contentType(ContentType.JSON)
          .statusCode(200).extract().response();

    final String body = r.getBody().asString();
    final JsonObject json = new JsonObject(body);

    context.assertEquals(0, json.getJsonArray("holdings").size());

    asyncLocal.complete();

    // Test done
    logger.info("Test done");
  }

  @Test
  public final void testGetTitles(TestContext context) {
    logger.info("Testing for successful RTAC collection");

    final Async asyncLocal = context.async();

    RestAssured
      .given()
        .header(tenantHeader)
        .header(urlHeader)
        .header(contentTypeHeader)
      .get("/rtac")
        .then()
          .contentType(ContentType.TEXT)
          .statusCode(501)
          .body(Matchers.containsString("Not implemented"));

    asyncLocal.complete();

    // Test done
    logger.info("Test done");
  }
}
