package org.folio.clients;

import static jakarta.ws.rs.core.HttpHeaders.ACCEPT;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
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
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.HttpException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.HttpStatus;
import org.folio.models.InventoryHoldingsAndItemsAndPieces;
import org.folio.rest.jaxrs.model.Holding;
import org.folio.rest.jaxrs.model.InventoryHoldingsAndItems;
import org.folio.rest.jaxrs.model.Piece;
import org.folio.rest.jaxrs.model.PieceCollection;

public class PieceClient extends FolioClient {
  private static final Logger logger = LogManager.getLogger();
  private static final String URI = "/orders/pieces";

  PieceClient(Map<String, String> okapiHeaders, WebClient webClient) {
    super(okapiHeaders, webClient);
  }

  Future<List<InventoryHoldingsAndItemsAndPieces>> getPieces(
      List<InventoryHoldingsAndItems> inventoryInstances) {
    logger.info("Fetching pieces for inventory instances.");
    Promise<List<InventoryHoldingsAndItemsAndPieces>> promise = Promise.promise();

    if (CollectionUtils.isEmpty(inventoryInstances)) {
      promise.complete(Collections.emptyList());
      return promise.future();
    }

    List<Future> futures = inventoryInstances.stream()
        .map(this::processInstance)
        .collect(Collectors.toCollection(ArrayList::new));

    CompositeFuture.all(futures)
        .onSuccess(composite -> promise.complete(composite.result()
            .list()))
        .onFailure(promise::fail);

    return promise.future();
  }

  private Future<InventoryHoldingsAndItemsAndPieces> processInstance(
      InventoryHoldingsAndItems instance) {
    logger.info("Processing instance: {}", instance.getInstanceId());
    Promise<InventoryHoldingsAndItemsAndPieces> promise = Promise.promise();
    List<Future> futures = instance.getHoldings()
        .stream()
        .map(this::processHolding)
        .collect(Collectors.toCollection(ArrayList::new));

    CompositeFuture.all(futures)
        .onSuccess(composite -> {
          List<PieceCollection> pieceCollections = composite.result().list();
          List<Piece> pieces = pieceCollections.stream()
              .flatMap(pieceCollection -> pieceCollection.getPieces().stream())
              .toList();

          promise.complete(new InventoryHoldingsAndItemsAndPieces(instance, pieces));
        })
        .onFailure(
          t -> {
            logger.warn(t.getMessage(), t);
            promise.complete(new InventoryHoldingsAndItemsAndPieces(instance,
                Collections.emptyList()));
          });

    return promise.future();
  }

  private Future<PieceCollection> processHolding(Holding holding) {
    logger.info("Fetching pieces for holding: {}", holding.getId());
    var url = String.format("%s%s", okapiUrl, URI);
    var httpClientRequest = webClient.getAbs(url);
    httpClientRequest.putHeader(OKAPI_HEADER_TOKEN, okapiToken)
        .putHeader(OKAPI_HEADER_TENANT, tenantId)
        .putHeader(ACCEPT, APPLICATION_JSON)
        .putHeader(CONTENT_TYPE, APPLICATION_JSON);

    Promise<PieceCollection> promise = Promise.promise();
    var jsonParser = getJsonParser(promise);
    httpClientRequest
        .addQueryParam("query", String.format("holdingId==%s and displayToPublic=true and "
            + "displayOnHolding=true", holding.getId()))
        .send(ar -> handleResponse(ar, promise, jsonParser));
    return promise.future();
  }

  private void handleResponse(AsyncResult<HttpResponse<Buffer>> ar,
      Promise<PieceCollection> promise, JsonParser parser) {
    final var httpResponse = ar.result();
    if (ar.failed()) {
      promise.fail(new HttpException(httpResponse.statusCode(), httpResponse.statusMessage()));
    } else {
      if (httpResponse.statusCode() != HttpStatus.HTTP_OK.toInt()) {
        logger.error("Failed with HTTP status: {}", httpResponse.statusCode());
        promise.fail(new HttpException(httpResponse.statusCode(), httpResponse.statusMessage()));
      } else {
        final var buffer = httpResponse.bodyAsBuffer();
        if (buffer == null) {
          logger.error("Piece response buffer is null, returning fallback result");
          handleNullPointerException(promise);
        }
        parser.handle(buffer);
        parser.end();
      }
    }
  }

  private JsonParser getJsonParser(Promise<PieceCollection> promise) {
    return JsonParser.newParser()
        .objectValueMode()
        .exceptionHandler(this::logError)
        .handler(event -> {
          var pieceCollection = event.objectValue().toString();
          var objectMapper = new ObjectMapper()
              .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

          PieceCollection pieceCollectionResponse;
          try {
            pieceCollectionResponse = objectMapper
                .readValue(pieceCollection, PieceCollection.class);
          } catch (JsonProcessingException e) {
            logError(e);
            promise.fail(e);
            return;
          }
          promise.complete(pieceCollectionResponse);
        });
  }

  private void logError(Throwable err) {
    logger.error(err.getMessage(), err);
  }

  /**
   * If the response is empty then the buffer will be null passing
   * a null buffer to the JSON parser causes a null pointer exception.
   */
  private void handleNullPointerException(Promise<PieceCollection> promise) {
    promise.complete(new PieceCollection());
  }
}
