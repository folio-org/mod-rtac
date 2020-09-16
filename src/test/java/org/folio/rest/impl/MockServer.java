package org.folio.rest.impl;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.junit5.VertxTestContext;

public class MockServer {

  private static final Logger logger = LoggerFactory.getLogger(MockServer.class);

  private static final String LOANS_URI = "/loan-storage/loans";
  private static final String INVENTORY_VIEW_URI = "/inventory-hierarchy/items-and-holdings";
  private static final String INVENTORY_VIEW = "/inventory-view/";

  private final int port;
  private final Vertx vertx;

  public MockServer(int port, Vertx vertx) {
    this.port = port;
    this.vertx = vertx;
  }

  public void start(VertxTestContext context) {
    HttpServer server = vertx.createHttpServer();
    server.requestHandler(defineRoutes()).listen(port, context.succeeding(result -> {
      logger.info("The mock server has started. port:{}", port);
      context.completeNow();
    }));
  }

  private Router defineRoutes() {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.post(INVENTORY_VIEW_URI).handler(this::handleInventoryViewResponse);
    router.get(LOANS_URI).handler(this::handleLoansResponse);
    return router;
  }

  private void handleInventoryViewResponse(RoutingContext routingContext) {
    routingContext.request().bodyHandler(body -> {
      JsonObject jsonObject = new JsonObject(body);
      JsonArray jsonArray = jsonObject.getJsonArray("instanceIds");
      if (jsonArray.contains(MockData.TEST_INSTANCE_ID)) {
        successResponse(routingContext, MockData.pojoToJson(MockData.TEST_INSTANCE_WITH_HOLDINGS_AND_ITEMS));
//      } else if (jsonArray.contains("1613697d-5e18-49b0-9613-08443b87cbc7")) {
////        successResponse(routingContext, getJsonObjectFromFile());
////      } else if (jsonArray.contains("2ae0635e-5534-4b7d-b28f-f0816329baa3")) {
////        successResponse(routingContext, getJsonObjectFromFile());
////      } else if (jsonArray.contains("0085f8ed-80ba-435b-8734-d3262aa4fc07")) {
////        successResponse(routingContext, getJsonObjectFromFile());
////      } else if (jsonArray.contains("a50aa30b-33d0-4067-89cc-67a61df8bc84")) {
////        successResponse(routingContext, getJsonObjectFromFile());
      } else {
        failureResponse(routingContext, HttpStatus.SC_BAD_REQUEST, format("there is no mock handler for request:%s", routingContext.request().uri()));
      }
    });
  }

  private void handleLoansResponse(RoutingContext routingContext) {
    HttpServerRequest request = routingContext.request();
    String query = request.getParam("query");
    if (query.contains(MockData.TEST_INSTANCE_ID)) {
      successResponse(routingContext, MockData.TEST_LOAN_JSON);
    } else {
      failureResponse(routingContext, HttpStatus.SC_BAD_REQUEST, "There is no mock response for request");
    }
  }

  private void successResponse(RoutingContext ctx, String body) {
    ctx.response()
      .setStatusCode(200)
      .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
      .end(body);
  }

  private void failureResponse(RoutingContext ctx, int code, String body) {
    ctx.response()
      .setStatusCode(code)
      .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
      .end(body);
  }

  private String getJsonObjectFromFile(String path) {
    try {
      logger.debug("Loading file " + path);
      URL resource = MockServer.class.getResource(path);
      if (resource == null) {
        return null;
      }
      File file = new File(resource.getFile());
      byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
      return new String(encoded, StandardCharsets.UTF_8);
    } catch (IOException e) {
      logger.error("Unexpected error", e);
      fail(e.getMessage());
    }
    return null;
  }


}
