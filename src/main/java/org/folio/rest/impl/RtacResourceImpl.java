package org.folio.rest.impl;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.RestVerticle;
import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.Holding;
import org.folio.rest.jaxrs.model.Holdings;
import org.folio.rest.jaxrs.resource.Rtac;
import org.folio.rest.tools.client.HttpClientFactory;
import org.folio.rest.tools.client.Response;
import org.folio.rest.tools.client.interfaces.HttpClientInterface;
import org.folio.rest.tools.utils.TenantTool;
import org.folio.rtac.rest.exceptions.HttpException;
import org.joda.time.DateTime;

public final class RtacResourceImpl implements Rtac {
  private static final String ERROR_MESSAGE = "errorMessage";
  private static final String QUERY = "&query=";
  private final Logger log = LogManager.getLogger();

  @Override
  @Validate
  public void getRtacById(String id, String lang,
      Map<String, String> okapiHeaders,
      Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler, Context vertxContext) {
    final String okapiUrl = okapiHeaders.getOrDefault("X-Okapi-Url", "");
    final String tenantId =
        TenantTool.calculateTenantId(okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT));

    final HttpClientInterface httpClient = HttpClientFactory.getHttpClient(okapiUrl, tenantId);
    try {
      httpClient.request("/inventory/instances/" + id, okapiHeaders)
          .thenAccept(this::verifyInstanceExists)
          .thenCompose(v -> getHoldings(id, httpClient, okapiHeaders))
          .thenApply(this::verifyAndExtractHoldings)
          .thenCompose(res -> getItems(res, httpClient, okapiHeaders))
          .thenCompose(holdings -> checkForLoans(holdings, httpClient, okapiHeaders))
          .thenAccept(holdings -> {
            asyncResultHandler.handle(Future.succeededFuture(
                Rtac.GetRtacByIdResponse.respond200WithApplicationJson(holdings)));
            httpClient.closeClient();
          }).exceptionally(throwable -> {
            asyncResultHandler.handle(handleError(throwable));
            httpClient.closeClient();
            return null;
          });
    } catch (Exception e) {
      asyncResultHandler.handle(Future.succeededFuture(
          Rtac.GetRtacByIdResponse.respond500WithTextPlain(e.getMessage())));
      httpClient.closeClient();
    }
  }

  private CompletableFuture<Holdings> convertFolioItemListToRtac(
      Map<CompletableFuture<Response>,
      String> cfs) {
    return CompletableFuture.allOf(cfs.keySet().toArray(new CompletableFuture[cfs.size()]))
    .thenApply(result -> {
      final Holdings holdings = new Holdings();
      final List<Holding> holdingList = new ArrayList<>();

      cfs.forEach((cf, callNumber) -> {
        final Response response = cf.join();

        if (!Response.isSuccess(response.getCode())) {
          throw new CompletionException(new HttpException(response.getCode(),
              response.getError().toString()));
        }

        final JsonObject responseBody = response.getBody();

        holdingList.addAll(responseBody.getJsonArray("items").stream()
            .map(item -> convertFolioItemToRtacHolding(callNumber, item))
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));
      });

      return holdings.withHoldings(holdingList);
    });
  }

  private Holding convertFolioItemToRtacHolding(final String callNumber, final Object o) {
    if (!(o instanceof JsonObject)) {
      return null;
    }

    final JsonObject item = (JsonObject) o;
    final Holding holding = new Holding();

    holding.setCallNumber(callNumber);
    holding.setId(item.getString("id"));
    holding.setStatus(item.getJsonObject("status", new JsonObject()).getString("name"));
    holding.setLocation(item.getJsonObject("effectiveLocation",
        new JsonObject()).getString("name"));
    holding.setVolume(getVolume(item));

    return holding;
  }

  private void verifyInstanceExists(Response response) {
    if (!Response.isSuccess(response.getCode())) {
      throw new CompletionException(new HttpException(response.getCode(),
          response.getError().getString(ERROR_MESSAGE)));
    }
  }

  CompletableFuture<Response> getHoldings(String id,
      HttpClientInterface httpClient, Map<String, String> okapiHeaders) {
    try {
      return httpClient.request("/holdings-storage/holdings?limit=" + Integer.MAX_VALUE
          + QUERY + encode("instanceId==" + id), okapiHeaders);
    } catch (Exception e) {
      throw new CompletionException(e);
    }
  }

  private JsonArray verifyAndExtractHoldings(Response response) {
    if (!Response.isSuccess(response.getCode())) {
      throw new CompletionException(new HttpException(response.getCode(),
          response.getError().getString(ERROR_MESSAGE)));
    }

    return response.getBody().getJsonArray("holdingsRecords");
  }

  CompletableFuture<Holdings> getItems(JsonArray holdingsRecords,
      HttpClientInterface httpClient, Map<String, String> okapiHeaders) {
    Map<CompletableFuture<Response>, String> cfs = new HashMap<>();
    for (Object o : holdingsRecords) {
      if (o instanceof JsonObject) {
        final JsonObject jo = (JsonObject) o;
        try {
          cfs.put(httpClient.request("/inventory/items?limit=" + Integer.MAX_VALUE
              + QUERY + encode("holdingsRecordId==" + jo.getString("id")), okapiHeaders),
              assembleCallNumber(jo.getString("callNumber"),
                                 jo.getString("callNumberPrefix"),
                                 jo.getString("callNumberSuffix")));
        } catch (Exception e) {
          throw new CompletionException(e);
        }
      }
    }

    return convertFolioItemListToRtac(cfs);
  }

  private String assembleCallNumber(String callNumber, String prefix, String suffix) {
    if (isNotEmpty(prefix)) {
      callNumber = prefix + " " + callNumber;
    }
    if (isNotEmpty(suffix)) {
      callNumber = callNumber + " " + suffix;
    }
    return callNumber;
  }

  CompletableFuture<Holdings> checkForLoans(Holdings holdings,
      HttpClientInterface httpClient, Map<String, String> okapiHeaders) {
    final Map<Holding, CompletableFuture<Response>> cfs = new HashMap<>();

    for (Holding holding : holdings.getHoldings()) {
      try {
        cfs.put(holding,
            httpClient.request("/circulation/loans?limit="
                + Integer.MAX_VALUE + QUERY
                + encode("(itemId==" + holding.getId() + " and status.name==Open)"),
                okapiHeaders));
      } catch (Exception e) {
        throw new CompletionException(e);
      }
    }

    return assignDueDates(holdings, cfs);
  }

  private CompletableFuture<Holdings> assignDueDates(Holdings holdings,
      Map<Holding, CompletableFuture<Response>> cfs) {
    return CompletableFuture.allOf(cfs.values().toArray(new CompletableFuture[cfs.size()]))
    .thenApply(result -> {
      cfs.forEach((holding, cf) -> {
        final Response response = cf.join();
        if (!Response.isSuccess(response.getCode())) {
          throw new CompletionException(new HttpException(response.getCode(),
              response.getError().getString(ERROR_MESSAGE)));
        }

        assignDueDate(holding, response.getBody());
      });

      return holdings;
    });
  }

  void assignDueDate(Holding holding, JsonObject responseBody) {
    final int totalLoans = responseBody.getInteger("totalRecords", Integer.valueOf(0)).intValue();
    if (totalLoans > 0) {
      if (totalLoans != 1) {
        log.warn("Item '{}' has more than one open loan: {}", holding.getId(), totalLoans);
      }
      Date dueDate = null;
      try {
        final String dueDateString =
            responseBody.getJsonArray("loans").getJsonObject(0).getString("dueDate");
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

  private String getVolume(JsonObject item) {
    final String enumeration = item.getString("enumeration");
    final String chronology = item.getString("chronology");
    final String volume = item.getString("volume");

    // The rules for generating "volume" are as follows:
    // |data set                     |"volume"                    |
    // |-----------------------------|----------------------------|
    // |enumeration                  |(<enumeration>)             |
    // |enumeration chronology       |(<enumeration> <chronology>)|
    // |enumeration chronology volume|(<enumeration> <chronology>)|
    // |volume                       |(<volume>)                  |
    // |chronology volume            |(<volume>)                  |
    // |chronology                   |(<chronology>)              |

    final StringJoiner sj = new StringJoiner(" ", "(", ")").setEmptyValue("");

    if (isNotBlank(enumeration)) {
      sj.add(enumeration);
      if (isNotBlank(chronology)) {
        sj.add(chronology);
      }
    } else if (isNotBlank(volume)) {
      sj.add(volume);
    } else if (isNotBlank(chronology)) {
      sj.add(chronology);
    }

    
    return defaultIfEmpty(sj.toString(), null);
  }

  private String encode(String value) {
    try {
      return URLEncoder.encode(value, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      log.error("JVM unable to encode using UTF-8...", e);
      return value;
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
          result = Future.succeededFuture(
              Rtac.GetRtacByIdResponse.respond500WithTextPlain(message));
          break;
        case 401:
          result = Future.succeededFuture(
              Rtac.GetRtacByIdResponse.respond401WithTextPlain(message));
          break;
        case 403:
          result = Future.succeededFuture(
              Rtac.GetRtacByIdResponse.respond403WithTextPlain(message));
          break;
        case 404:
          result = Future.succeededFuture(
              Rtac.GetRtacByIdResponse.respond404WithTextPlain(message));
          break;
        default:
          result = Future.succeededFuture(
              Rtac.GetRtacByIdResponse.respond500WithTextPlain(message));
      }
    } else {
      result = Future.succeededFuture(
          Rtac.GetRtacByIdResponse.respond500WithTextPlain(throwable.getMessage()));
    }

    return result;
  }
}
