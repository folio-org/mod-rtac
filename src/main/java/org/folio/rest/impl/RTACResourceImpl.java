package org.folio.rest.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import org.folio.rest.RestVerticle;
import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.Holding;
import org.folio.rest.jaxrs.model.Holdings;
import org.folio.rest.jaxrs.resource.RTACResource;
import org.folio.rest.tools.client.HttpClientFactory;
import org.folio.rest.tools.client.Response;
import org.folio.rest.tools.client.interfaces.HttpClientInterface;
import org.folio.rest.tools.utils.TenantTool;
import org.folio.rtac.rest.exceptions.HttpException;
import org.joda.time.DateTime;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public final class RTACResourceImpl implements RTACResource {
  private final Logger log = LoggerFactory.getLogger(RTACResourceImpl.class);

  @Override
  @Validate
  public void getRtac(Map<String, String> okapiHeaders,
      Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler, Context vertxContext)
      throws Exception {
    asyncResultHandler.handle(Future.succeededFuture(GetRtacResponse.withPlainNotImplemented("Not implemented")));
  }

  @Override
  @Validate
  public void getRtacById(String id, String lang,
      Map<String, String> okapiHeaders,
      Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler, Context vertxContext)
      throws Exception {
    final String okapiURL;
    if (okapiHeaders.containsKey("X-Okapi-Url")) {
      okapiURL = okapiHeaders.get("X-Okapi-Url");
    } else {
      okapiURL = System.getProperty("okapi.url");
    }
    final String tenantId = TenantTool.calculateTenantId(okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT));

    final HttpClientInterface httpClient = HttpClientFactory.getHttpClient(okapiURL, tenantId);
    try {
      httpClient.request("/inventory/instances/" + id, okapiHeaders)
      .thenAccept(this::verifyInstanceExists)
      .thenCompose(v -> getHoldings(id, httpClient, okapiHeaders))
      .thenApply(this::verifyAndExtractHoldings)
      .thenCompose(res -> getItems(res, httpClient, okapiHeaders))
      .thenCompose(holdings -> checkForLoans(holdings, httpClient, okapiHeaders))
      .thenAccept(holdings -> {
        asyncResultHandler.handle(Future.succeededFuture(RTACResource.GetRtacByIdResponse.withJsonOK(holdings)));
        httpClient.closeClient();
      }).exceptionally(throwable -> {
        asyncResultHandler.handle(handleError(throwable));
        httpClient.closeClient();
        return null;
      });
    } catch (Exception e) {
      asyncResultHandler.handle(Future.succeededFuture(RTACResource.GetRtacByIdResponse.withPlainInternalServerError(e.getMessage())));
      httpClient.closeClient();
    }
  }

  private CompletableFuture<Holdings> convertFolioItemListToRTAC(Map<CompletableFuture<Response>, String> cfs) {
    return CompletableFuture.allOf(cfs.keySet().toArray(new CompletableFuture[cfs.size()]))
    .thenApply(result -> {
      final Holdings holdings = new Holdings();
      final List<Holding> holdingList = new ArrayList<>();

      cfs.forEach((cf, callNumber) -> {
        final Response response = cf.join();

        if (!Response.isSuccess(response.getCode())) {
          throw new CompletionException(new HttpException(response.getCode(), response.getError().toString()));
        }

        final JsonObject responseBody = response.getBody();

        holdingList.addAll(responseBody.getJsonArray("items").stream()
            .map(item -> convertFolioItemToRTACHolding(callNumber, item))
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));
      });

      return holdings.withHoldings(holdingList);
    });
  }

  private Holding convertFolioItemToRTACHolding(final String callNumber, final Object o) {
    if (!(o instanceof JsonObject)) {
      return null;
    }

    final JsonObject item = (JsonObject) o;
    final Holding holding = new Holding();

    holding.setCallNumber(callNumber);
    holding.setId(item.getString("id"));
    holding.setStatus(item.getJsonObject("status", new JsonObject()).getString("name"));
    holding.setLocation(item.getJsonObject("effectiveLocation", new JsonObject()).getString("name"));

    return holding;
  }

  private void verifyInstanceExists(Response response) {
    if (!Response.isSuccess(response.getCode())) {
      throw new CompletionException(new HttpException(response.getCode(), response.getError().toString()));
    }
  }

  private CompletableFuture<Response> getHoldings(String id,
      HttpClientInterface httpClient, Map<String, String> okapiHeaders) {
    try {
      return httpClient.request("/holdings-storage/holdings?limit=100&query=instanceId%3D%3D" + id, okapiHeaders);
    } catch (Exception e) {
      throw new CompletionException(e);
    }
  }

  private JsonArray verifyAndExtractHoldings(Response response) {
    if (!Response.isSuccess(response.getCode())) {
      throw new CompletionException(new HttpException(response.getCode(), response.getError().toString()));
    }

    return response.getBody().getJsonArray("holdingsRecords");
  }

  private CompletableFuture<Holdings> getItems(JsonArray holdingsRecords,
      HttpClientInterface httpClient, Map<String, String> okapiHeaders) {
    Map<CompletableFuture<Response>, String> cfs = new HashMap<>();
    for (Object o : holdingsRecords) {
      if (o instanceof JsonObject) {
        final JsonObject jo = (JsonObject) o;
        try {
          cfs.put(httpClient.request("/inventory/items?limit=100&query=holdingsRecordId%3D%3D" + jo.getString("id"), okapiHeaders), jo.getString("callNumber"));
        } catch (Exception e) {
          throw new CompletionException(e);
        }
      }
    }

    return convertFolioItemListToRTAC(cfs);
  }

  private CompletableFuture<Holdings> checkForLoans(Holdings holdings,
      HttpClientInterface httpClient, Map<String, String> okapiHeaders) {
    final Map<Holding, CompletableFuture<Response>> cfs = new HashMap<>();

    for (Holding holding : holdings.getHoldings()) {
      try {
        cfs.put(holding,
            httpClient.request("/circulation/loans?limit=100&query=%28itemId%3D%3D"
                + holding.getId() + "%20and%20status.name%3D%3DOpen%29",
                okapiHeaders));
      } catch (Exception e) {
        throw new CompletionException(e);
      }
    }

    return assignDueDates(holdings, cfs);
  }

  private CompletableFuture<Holdings> assignDueDates(Holdings holdings, Map<Holding, CompletableFuture<Response>> cfs) {
    return CompletableFuture.allOf(cfs.values().toArray(new CompletableFuture[cfs.size()]))
    .thenApply(result -> {
      cfs.forEach((holding, cf) -> {
        final Response response = cf.join();
        if (!Response.isSuccess(response.getCode())) {
          throw new CompletionException(new HttpException(response.getCode(), response.getError().toString()));
        }

        assignDueDate(holding, response.getBody());
      });

      return holdings;
    });
  }

  private void assignDueDate(Holding holding, JsonObject responseBody) {
    final int totalLoans = responseBody.getInteger("totalRecords", Integer.valueOf(0)).intValue();
    if (totalLoans > 0) {
      if (totalLoans != 1) {
        log.warn("Item '" + holding.getId() + "' has more than one open loan: " + totalLoans);
      }
      Date dueDate = null;
      try {
        final String dueDateString = responseBody.getJsonArray("loans").getJsonObject(0).getString("dueDate");
        if (dueDateString != null) {
          dueDate = new DateTime(dueDateString).toDate();
        }
      } catch (Exception e) {
        log.error("Unable to extract the loan dueDate", e);
      }
      if (dueDate != null) {
        holding.setDueDate(dueDate);
      }
    }
  }

  private Future<javax.ws.rs.core.Response> handleError(Throwable throwable) {
    final Future<javax.ws.rs.core.Response> result;

    final Throwable t = throwable.getCause();
    if (t instanceof HttpException) {
      final int code = ((HttpException) t).getCode();
      final String message = ((HttpException) t).getMessage();
      switch (code) {
      case 400:
        // This means that we screwed up something in the request to another
        // module. This API only takes a UUID, so a client side 400 is not
        // possible here, only server side, which the client won't be able to
        // do anything about.
        result = Future.succeededFuture(RTACResource.GetRtacByIdResponse.withPlainInternalServerError(message));
        break;
      case 401:
        result = Future.succeededFuture(RTACResource.GetRtacByIdResponse.withPlainUnauthorized(message));
        break;
      case 403:
        result = Future.succeededFuture(RTACResource.GetRtacByIdResponse.withPlainForbidden(message));
        break;
      case 404:
        result = Future.succeededFuture(RTACResource.GetRtacByIdResponse.withPlainNotFound(message));
        break;
      default:
        result = Future.succeededFuture(RTACResource.GetRtacByIdResponse.withPlainInternalServerError(message));
      }
    } else {
      result = Future.succeededFuture(RTACResource.GetRtacByIdResponse.withPlainInternalServerError(throwable.getMessage()));
    }

    return result;
  }
}
