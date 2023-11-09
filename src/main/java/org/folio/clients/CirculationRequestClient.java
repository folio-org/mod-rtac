package org.folio.clients;


import static jakarta.ws.rs.core.HttpHeaders.ACCEPT;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.folio.rest.RestVerticle.OKAPI_HEADER_TENANT;
import static org.folio.rest.RestVerticle.OKAPI_HEADER_TOKEN;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.JsonParser;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.HttpStatus;
import org.folio.rest.jaxrs.model.InventoryHoldingsAndItems;
import org.folio.rest.jaxrs.model.Item;
import org.folio.rest.jaxrs.model.Request;
import org.folio.rest.jaxrs.model.Requests;
import org.folio.rtac.rest.exceptions.HttpException;

class CirculationRequestClient extends FolioClient {

  private static final String URI = "/circulation/requests";
  private final Logger logger = LogManager.getLogger(getClass());

  CirculationRequestClient(Map<String, String> okapiHeaders, WebClient client) {
    super(okapiHeaders, client);
  }

  public Future<List<InventoryHoldingsAndItems>> updateInstanceItemsWithRequestsCount(
      List<InventoryHoldingsAndItems> inventoryInstances) {

    logger.info("Getting hold requests for instance items from circulation");
    Promise<List<InventoryHoldingsAndItems>> promise = Promise.promise();

    if (CollectionUtils.isEmpty(inventoryInstances)) {
      promise.complete(inventoryInstances);
      return promise.future();
    }

    List<Future> futures =
        inventoryInstances.stream()
        .map(this::processInstance)
        .collect(Collectors.toCollection(ArrayList::new));

    CompositeFuture.all(futures)
      .onSuccess(updatedInstances -> promise.complete(updatedInstances.result().list()))
        .onFailure(promise::fail);

    return promise.future();
  }

  private Future<InventoryHoldingsAndItems> processInstance(
      InventoryHoldingsAndItems inventoryInstance) {
    var url = String.format("%s%s", okapiUrl, URI);
    var httpClientRequest = webClient.getAbs(url);
    httpClientRequest.putHeader(OKAPI_HEADER_TOKEN, okapiToken)
      .putHeader(OKAPI_HEADER_TENANT, tenantId)
      .putHeader(ACCEPT, APPLICATION_JSON)
        .putHeader(CONTENT_TYPE, APPLICATION_JSON);
    Promise<InventoryHoldingsAndItems> promise = Promise.promise();

    var items = inventoryInstance.getItems();
    String cql = items.stream()
        .map(Item::getId)
        .collect(Collectors.joining(" or ", "(", ")"));

    fetchRequests(httpClientRequest, cql)
        .onSuccess(
          requestMap -> {
            List<Item> collect = itemsWithRequestCount(requestMap, items);
            promise.complete(inventoryInstance.withItems(collect));
          })
        .onFailure(
          t -> {
            logger.warn(t.getMessage(), t);
            promise.complete(inventoryInstance);
          });

    return promise.future();
  }

  private static List<Item> itemsWithRequestCount(Map<String, Long> requestMap,
      List<Item> items) {
    items.forEach(item -> {
      String id = item.getId();
      item.setTotalHoldRequests(Math.toIntExact(requestMap.getOrDefault(id, 0L)));
    });
    return items;
  }

  private Future<Map<String, Long>> fetchRequests(HttpRequest<Buffer> httpClientRequest,
      String itemIds) {
    Promise<Map<String, Long>> promise = Promise.promise();

    var parser = getJsonParser(promise);

    httpClientRequest
      .addQueryParam("query", String.format("itemId==%s", itemIds))
      .addQueryParam("limit", "10000")
        .send(ar -> handleResponse(ar, promise, parser));
    return promise.future();
  }

  private void handleResponse(AsyncResult<HttpResponse<Buffer>> ar,
      Promise<Map<String, Long>> promise, JsonParser parser) {
    final var httpResponse = ar.result();
    if (ar.failed()) {
      promise.fail(
          new HttpException(httpResponse.statusCode(), httpResponse.statusMessage()));
    } else {
      if (httpResponse.statusCode() != HttpStatus.HTTP_OK.toInt()) {
        promise.fail(
            new HttpException(httpResponse.statusCode(), httpResponse.statusMessage()));
      } else {
        final var buffer = httpResponse.bodyAsBuffer();
        if (buffer == null) {
          handleNullPointerException(promise);
        }

        parser.handle(buffer);
        parser.end();
      }
    }
  }

  private JsonParser getJsonParser(Promise<Map<String, Long>> promise) {
    return JsonParser.newParser()
      .objectValueMode()
      .exceptionHandler(this::logError)
      .handler(event -> {
        var requests = event.objectValue().toString();
        var objectMapper  = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Requests requestsResp;
        try {
          requestsResp = objectMapper.readValue(requests, Requests.class);
        } catch (JsonProcessingException e) {
          promise.fail(e);
          return;
        }
        Map<String, Long> requestMap = requestsResp.getRequests()
            .stream()
            .collect(groupingBy(Request::getItemId, counting()));
        promise.complete(requestMap);
      });
  }

  private void logError(Throwable err) {
    logger.error(err.getMessage(), err);
  }

  /**
   * If the response is empty then the buffer will be null passing a null buffer to the JSON parser
   * causes a null pointer exception.
   */
  private void handleNullPointerException(Promise<Map<String, Long>> promise) {
    promise.complete(new HashMap<>());
  }
}
