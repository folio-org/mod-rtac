package org.folio.rest.impl;

import static java.lang.String.format;

import org.apache.http.HttpStatus;

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

  private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";

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
    JsonObject jsonObject = routingContext.getBody().toJsonObject();
    JsonArray jsonArray = jsonObject.getJsonArray("instanceIds");
    if (jsonArray.contains(MockData.INSTANCE_ID)) {
      successResponse(routingContext, MockData.pojoToJson(MockData.INSTANCE_WITH_HOLDINGS_AND_ITEMS));
    } else if (jsonArray.contains(MockData.INSTANCE_ID_WITH_NO_LOANS_ITEM)) {
      successResponse(routingContext, MockData.pojoToJson(MockData.INSTANCE_WITH_ITEM_WHICH_HAS_NOT_LOANS));
    } else if (jsonArray.contains(MockData.NONEXISTENT_INSTANCE_ID)) {
      successResponse(routingContext, MockData.pojoToJson(MockData.INSTANCE_LOAN_STORAGE_ERROR));
    } else if (jsonArray.contains(MockData.INSTANCE_ID_INVENTORY_VIEW_ERROR)) {
      failureResponse(routingContext, 500, INTERNAL_SERVER_ERROR);
    } else if (jsonArray.contains(MockData.INSTANCE_ID_NO_ITEMS_AND_HOLDINGS)) {
      successResponse(routingContext, MockData.pojoToJson(MockData.INSTANCE_WITHOUT_HOLDINGS_AND_ITEMS));
    } else {
      failureResponse(routingContext, HttpStatus.SC_BAD_REQUEST, format("there is no mock handler for request:%s", routingContext.request().uri()));
    }
  }

  private void handleLoansResponse(RoutingContext routingContext) {
    HttpServerRequest request = routingContext.request();
    String query = request.getParam("query");
    if (query.contains(MockData.INSTANCE_ITEM_ID)) {
      successResponse(routingContext, MockData.LOAN_JSON);
    } else if (query.contains(MockData.ITEM_WITHOUT_LOAN_ID)) {
      successResponse(routingContext, MockData.EMPTY_LOANS_JSON);
    } else if (query.contains(MockData.ITEM_ID_LOAN_STORAGE_ERROR)) {
      failureResponse(routingContext, 500, INTERNAL_SERVER_ERROR);
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
}
