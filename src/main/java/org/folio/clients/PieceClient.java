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
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.models.InventoryHoldingsAndItemsAndPieces;
import org.folio.rest.jaxrs.model.Holding;
import org.folio.rest.jaxrs.model.InventoryHoldingsAndItems;
import org.folio.rest.jaxrs.model.PieceCollection;

public class PieceClient extends FolioClient {
  private static final Logger logger = LogManager.getLogger();
  private static final String URI = "/orders/pieces";

  PieceClient(Map<String, String> okapiHeaders, WebClient webClient) {
    super(okapiHeaders, webClient);
  }

  Future<List<InventoryHoldingsAndItemsAndPieces>> getPieces(
      List<InventoryHoldingsAndItems> inventoryInstances, String tenantId, String centralTenantId) {
    logger.info("Fetching pieces for inventory instances.");
    Promise<List<InventoryHoldingsAndItemsAndPieces>> promise = Promise.promise();

    if (CollectionUtils.isEmpty(inventoryInstances)) {
      promise.complete(Collections.emptyList());
      return promise.future();
    }

    final var httpMemberClientRequest = buildRequest(tenantId);
    final var httpCentralClientRequest = Optional.ofNullable(
        centralTenantId != null ? buildRequest(centralTenantId) : null);

    List<Future> futures = inventoryInstances.stream()
        .map(instance -> processInstance(instance, httpMemberClientRequest,
            httpCentralClientRequest))
        .collect(Collectors.toCollection(ArrayList::new));

    CompositeFuture.all(futures)
        .onSuccess(composite -> promise.complete(composite.result().list()))
        .onFailure(promise::fail);

    return promise.future();
  }

  private Future<InventoryHoldingsAndItemsAndPieces> processInstance(
      InventoryHoldingsAndItems instance, HttpRequest<Buffer> httpMemberClientRequest,
      Optional<HttpRequest<Buffer>> httpCentralClientRequest) {
    logger.info("Processing instance: {}", instance.getInstanceId());
    Promise<InventoryHoldingsAndItemsAndPieces> promise = Promise.promise();
    String cql = buildCql(instance.getHoldings());
    List<Future<PieceCollection>> piecesRequests = new ArrayList<>();
    piecesRequests.add(processHolding(httpMemberClientRequest, cql));
    httpCentralClientRequest.ifPresent(request -> piecesRequests.add(processHolding(request, cql)));
    Future.all(piecesRequests)
        .onSuccess(composite -> {
          List<PieceCollection> piecesColections = composite.list();
          var pieces = piecesColections.stream()
              .flatMap(pieceCollection -> pieceCollection.getPieces().stream()).toList();
          promise.complete(new InventoryHoldingsAndItemsAndPieces(instance, pieces));
        })
        .onFailure(
            t -> {
              logger.warn(t.getMessage(), t);
              promise.complete(new InventoryHoldingsAndItemsAndPieces(instance,
                  Collections.emptyList()));
            }
        );

    return promise.future();
  }

  private Future<PieceCollection> processHolding(HttpRequest<Buffer> httpClientRequest,
      String cql) {
    logger.info("Fetching pieces for holding with ?query={}", cql);
    Promise<PieceCollection> promise = Promise.promise();
    var jsonParser = getJsonParser(promise);
    httpClientRequest
        .addQueryParam("query", cql)
        .send(ar -> handleResponse(ar, promise, jsonParser));
    return promise.future();
  }

  private void handleResponse(AsyncResult<HttpResponse<Buffer>> ar,
      Promise<PieceCollection> promise, JsonParser parser) {
    final var httpResponse = ar.result();
    if (validateHttpStatusOk(ar, promise)) {
      final var buffer = httpResponse.bodyAsBuffer();
      if (buffer == null) {
        logger.error("Piece response buffer is null, returning fallback result");
        handleNullPointerException(promise);
      }
      parser.handle(buffer);
      parser.end();
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

  private HttpRequest<Buffer> buildRequest(String tenantId) {
    var url = String.format("%s%s", okapiUrl, URI);
    logger.info("Sending request to {}", url);

    final var httpClientRequest = webClient.getAbs(url);
    httpClientRequest
        .putHeader(OKAPI_HEADER_TOKEN, okapiToken)
        .putHeader(OKAPI_HEADER_TENANT, tenantId)
        .putHeader(ACCEPT, APPLICATION_JSON)
        .putHeader(CONTENT_TYPE, APPLICATION_JSON);

    return httpClientRequest;
  }

  private String buildCql(List<Holding> holdings) {
    final var cql = new StringBuilder();
    final String query =
        holdings.stream()
            .map(i -> "holdingId==" + i.getId())
            .collect(Collectors.joining(" or ", "(", ")"));
    cql.append(query);
    cql.append("and displayToPublic=true and displayOnHolding=true");

    return cql.toString();
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
