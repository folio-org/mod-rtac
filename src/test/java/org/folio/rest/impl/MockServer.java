package org.folio.rest.impl;

import static java.lang.String.format;
import static org.folio.rest.RestVerticle.OKAPI_HEADER_TENANT;
import static org.folio.rest.impl.MockData.HOLDING_ID_WITH_PIECES;
import static org.folio.rest.impl.MockData.INSTANCE_ID;
import static org.folio.rest.impl.MockData.INSTANCE_ID_HOLDINGS_AND_PIECES_IN_CONSORTIA;

import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.junit5.VertxTestContext;
import org.apache.http.HttpStatus;

public class MockServer {

  public static final String TEST_CENTRAL_TENANT_ID = "test_central_tenant";
  public static final String TEST_TENANT_ID = "test_tenant";
  public static final String TEST_TENANT_0001_ID = "test_tenant_0001";
  private static final String LOANS_URI = "/loan-storage/loans";
  private static final String INVENTORY_VIEW_URI = "/inventory-hierarchy/items-and-holdings";
  private static final String CIRCULATION_REQUESTS_URI = "/circulation/requests";
  private static final String ORDERS_PIECES_URI = "/orders/pieces";
  private static final String USER_TENANTS_URI = "/user-tenants";
  private static final String HOLDINGS_FACET_URI = "/search/instances/facets";
  private static final String SETTINGS_URI = "/settings/entries";
  private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";

  private final int port;
  private final Vertx vertx;

  public MockServer(int port, Vertx vertx) {
    this.port = port;
    this.vertx = vertx;
  }

  void start(VertxTestContext context) {
    HttpServer server = vertx.createHttpServer();
    server
        .requestHandler(defineRoutes())
        .listen(
            port,
            context.succeeding(
                result -> {
                  context.completeNow();
                }));
  }

  private Router defineRoutes() {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.post(INVENTORY_VIEW_URI).handler(this::handleInventoryViewResponse);
    router.get(LOANS_URI).handler(this::handleLoansResponse);
    router.get(CIRCULATION_REQUESTS_URI).handler(this::handleHoldRequestsResponse);
    router.get(ORDERS_PIECES_URI).handler(this::handleOrderPiecesResponse);
    router.get(USER_TENANTS_URI).handler(this::handleUserTenantsResponse);
    router.get(HOLDINGS_FACET_URI).handler(this::handleHoldingsFacetResponse);
    router.get(SETTINGS_URI).handler(this::handleSettingsResponse);
    return router;
  }

  private void handleInventoryViewResponse(RoutingContext routingContext) {
    JsonObject jsonObject = routingContext.getBody().toJsonObject();
    JsonArray jsonArray = jsonObject.getJsonArray("instanceIds");
    var tenant = routingContext.request().getHeader(OKAPI_HEADER_TENANT);
    var first = jsonArray.getString(0);

    if (first.equals(MockData.UUID_400)) {
      failureResponse(routingContext, 400, "Internal Server error");
    } else if (first.equals(MockData.UUID_403)) {
      failureResponse(routingContext, 403, "Internal Server error");
    } else if (first.equals(MockData.UUID_404)) {
      failureResponse(routingContext, 404, "Internal Server error");
    } else if (first.equals(MockData.UUID_500)) {
      failureResponse(routingContext, 500, "Internal Server error");
    }  else if (jsonArray.contains(MockData.INSTANCE_ID)
        && jsonArray.contains(MockData.INSTANCE_ID_NO_ITEMS_AND_HOLDINGS)) {
      String multipleResponse = MockData.pojoToJson(MockData.INSTANCE_WITH_HOLDINGS_AND_ITEMS)
          + MockData.pojoToJson(MockData.INSTANCE_WITHOUT_HOLDINGS_AND_ITEMS);
      successResponse(routingContext, multipleResponse);
    } else if (jsonArray.contains(MockData.INSTANCE_ID_HOLDINGS_NO_ITEMS)
        && jsonArray.contains(MockData.INSTANCE_ID_WITH_NO_LOANS_ITEM)) {

      String multipleResponse =
          MockData.pojoToJson(MockData.INSTANCE_WITH_ITEM_WHICH_HAS_NOT_LOANS)
          + MockData.pojoToJson(MockData.INSTANCE_WITH_HOLDINGS_NO_ITEMS);
      successResponse(routingContext, multipleResponse);
    } else if (jsonArray.contains(MockData.INSTANCE_ID) && tenant.equals(TEST_TENANT_ID)) {
      successResponse(
          routingContext, MockData.pojoToJson(MockData.INSTANCE_WITH_HOLDINGS_AND_ITEMS));
    } else if (jsonArray.contains(MockData.INSTANCE_ID_HOLDINGS_AND_PIECES_IN_CONSORTIA)
        && tenant.equals(TEST_TENANT_ID)) {
      successResponse(
          routingContext,
          MockData.pojoToJson(MockData.INSTANCE_WITH_HOLDINGS_AND_PIECES_IN_CONSORTIA));
    } else if (jsonArray.contains(MockData.INSTANCE_ID) && tenant.equals(TEST_TENANT_0001_ID)) {
      successResponse(
          routingContext,
          MockData.pojoToJson(MockData.INSTANCE_WITH_HOLDINGS_AND_ITEMS_TEST_TENANT_0001));
    } else if (jsonArray.contains(MockData.INSTANCE_ID_WITH_NO_LOANS_ITEM)) {
      successResponse(
          routingContext, MockData.pojoToJson(MockData.INSTANCE_WITH_ITEM_WHICH_HAS_NOT_LOANS));
    } else if (jsonArray.contains(MockData.NONEXISTENT_INSTANCE_ID)) {
      successResponse(routingContext, MockData.pojoToJson(MockData.INSTANCE_LOAN_STORAGE_ERROR));
    } else if (jsonArray.contains(MockData.INSTANCE_ID_INVENTORY_VIEW_ERROR)) {
      failureResponse(routingContext, 500, INTERNAL_SERVER_ERROR);
    } else if (jsonArray.contains(MockData.INSTANCE_ID_NO_ITEMS_AND_HOLDINGS)) {
      successResponse(
          routingContext, MockData.pojoToJson(MockData.INSTANCE_WITHOUT_HOLDINGS_AND_ITEMS));
    } else if (jsonArray.contains(MockData.INSTANCE_ID_NO_FULL_PERIODICALS)) {
      successResponse(routingContext, MockData.pojoToJson(MockData.INSTANCE_NO_FULL_PERIODICALS));
    } else if (jsonArray.contains(MockData.INSTANCE_ID_HOLDINGS_NO_ITEMS)) {
      successResponse(
          routingContext, MockData.pojoToJson(MockData.INSTANCE_WITH_HOLDINGS_NO_ITEMS));
    } else if (jsonArray.contains(MockData.INSTANCE_ID_HOLDINGS_AND_PIECES)) {
      successResponse(
          routingContext, MockData.pojoToJson(MockData.INSTANCE_WITH_HOLDINGS_AND_PIECES));
    } else if (jsonArray.contains(MockData.INSTANCE_ID_HOLDINGS_AND_NO_PIECES)) {
      successResponse(
          routingContext, MockData.pojoToJson(MockData.INSTANCE_WITH_HOLDINGS_AND_NO_PIECES));
    } else if (jsonArray.contains(MockData.INSTANCE_ID_NOT_EXISTS)) {
      successResponse(routingContext, "");
    } else {
      failureResponse(
          routingContext,
          HttpStatus.SC_BAD_REQUEST,
          format("there is no mock handler for request:%s", routingContext.request().uri()));
    }
  }

  private void handleLoansResponse(RoutingContext routingContext) {
    HttpServerRequest request = routingContext.request();
    String query = request.getParam("query");
    if (query.contains(MockData.INSTANCE_ITEM_ID_1)) {
      successResponse(routingContext, MockData.LOAN_JSON);
    } else if (query.contains(MockData.ITEM_WITHOUT_LOAN_ID)) {
      successResponse(routingContext, MockData.EMPTY_LOANS_JSON);
    } else if (query.contains(MockData.ITEM_ID_LOAN_STORAGE_ERROR)) {
      failureResponse(routingContext, 500, INTERNAL_SERVER_ERROR);
    } else {
      failureResponse(
          routingContext, HttpStatus.SC_BAD_REQUEST, "There is no mock response for request");
    }
  }

  private void handleHoldRequestsResponse(RoutingContext routingContext) {
    HttpServerRequest request = routingContext.request();
    String query = request.getParam("query");
    if (query.contains(MockData.INSTANCE_ITEM_ID_1)) {
      successResponse(routingContext, MockData.REQUESTS_JSON);
    } else {
      failureResponse(
          routingContext, HttpStatus.SC_BAD_REQUEST, "There is no mock response for request");
    }
  }

  private void handleOrderPiecesResponse(RoutingContext routingContext) {
    HttpServerRequest request = routingContext.request();
    String query = request.getParam("query");
    String tenant = request.getHeader(OKAPI_HEADER_TENANT);
    String pieceCollectionResponse = MockData.pojoToJson(MockData.PIECE_COLLECTION);
    String centralPieceCollectionResponse = MockData.pojoToJson(
        MockData.PIECE_COLLECTION_FOR_CENTRAL_TENANT);

    if (query.contains(HOLDING_ID_WITH_PIECES)) {
      successResponse(routingContext, pieceCollectionResponse);
    } else if (query.contains(MockData.HOLDING_ID_WITHOUT_PIECES)) {
      successResponse(routingContext, MockData.EMPTY_PIECE_COLLECTION_JSON);
    } else if (query.contains(MockData.HOLDING_ID_WITH_PIECES_IN_CENTRAL)) {
      if (tenant.equals(TEST_CENTRAL_TENANT_ID)) {
        successResponse(routingContext, centralPieceCollectionResponse);
      } else {
        successResponse(routingContext, MockData.EMPTY_PIECE_COLLECTION_JSON);
      }
    } else {
      failureResponse(
          routingContext, HttpStatus.SC_BAD_REQUEST, "There is no mock response for pieces");
    }
  }

  private void handleUserTenantsResponse(RoutingContext routingContext) {
    HttpServerRequest request = routingContext.request();
    String tenant = request.getHeader(OKAPI_HEADER_TENANT);
    if (tenant.equals(TEST_CENTRAL_TENANT_ID)) {
      successResponse(routingContext, MockData.USERS_CENTRAL_TENANT_JSON);
    } else {
      successResponse(routingContext, MockData.USERS_NON_CONSORTIA_TENANT_JSON);
    }
  }

  private void handleHoldingsFacetResponse(RoutingContext routingContext) {
    HttpServerRequest request = routingContext.request();
    String tenant = request.getHeader(OKAPI_HEADER_TENANT);
    if (tenant.equals(TEST_CENTRAL_TENANT_ID) && request.query()
        .contains(INSTANCE_ID)) {
      successResponse(routingContext, MockData.HOLDINGS_FACET_JSON);
    } else if (tenant.equals(TEST_CENTRAL_TENANT_ID) && request.query()
        .contains(INSTANCE_ID_HOLDINGS_AND_PIECES_IN_CONSORTIA)) {
      successResponse(routingContext, MockData.HOLDINGS_FACET_WITH_1_TENANT_JSON);
    } else {
      failureResponse(
          routingContext, HttpStatus.SC_BAD_REQUEST, "There is no mock response for pieces");
    }
  }

  private void handleSettingsResponse(RoutingContext routingContext) {
    HttpServerRequest request = routingContext.request();
    String tenant = request.getHeader(OKAPI_HEADER_TENANT);
    if (tenant.equals((TEST_CENTRAL_TENANT_ID))) {
      successResponse(routingContext, MockData.SETTINGS_JSON);
    }
    successResponse(routingContext, MockData.EMPTY_SETTINGS_JSON);
  }

  private void successResponse(RoutingContext ctx, String body) {
    ctx.response()
        .setStatusCode(200)
        .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
        .end(body);
  }

  private void failureResponse(RoutingContext ctx, int code, String body) {
    ctx.response().setStatusCode(code).putHeader(HttpHeaders.CONTENT_TYPE, "text/plain").end(body);
  }
}
