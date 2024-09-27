
package org.folio.clients;

import static jakarta.ws.rs.core.HttpHeaders.ACCEPT;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.folio.rest.RestVerticle.OKAPI_HEADER_TENANT;
import static org.folio.rest.RestVerticle.OKAPI_HEADER_TOKEN;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.JsonParser;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.HttpStatus;
import org.folio.rest.jaxrs.model.InventoryHoldingsAndItems;
import org.folio.rtac.rest.exceptions.HttpException;

class InventoryClient extends FolioClient {

  private static final Logger logger = LogManager.getLogger();

  private static final String VIEW_URI = "/inventory-hierarchy/items-and-holdings";

  InventoryClient(Map<String, String> okapiHeaders, WebClient webClient) {
    super(okapiHeaders, webClient);
  }

  Future<List<InventoryHoldingsAndItems>> getItemAndHoldingInfo(List<String> instanceIds,
      String tenantId) {
    logger.info("Getting item and holding information from inventory");
    Promise<List<InventoryHoldingsAndItems>> promise = Promise.promise();

    if (CollectionUtils.isEmpty(instanceIds)) {
      promise.fail(
          new HttpException(HttpStatus.HTTP_NOT_FOUND.toInt(),
              "Could not find inventory instances"));
      return promise.future();
    }

    String inventoryUrl = String.format("%s%s", okapiUrl, VIEW_URI);
    HttpRequest<Buffer> request = webClient.requestAbs(HttpMethod.POST, inventoryUrl);
    request.putHeader(OKAPI_HEADER_TOKEN, okapiToken)
        .putHeader(OKAPI_HEADER_TENANT, tenantId)
        .putHeader(ACCEPT, APPLICATION_JSON)
        .putHeader(CONTENT_TYPE, APPLICATION_JSON);

    final var instances = new ArrayList<InventoryHoldingsAndItems>();
    var parser = JsonParser.newParser();
    parser.objectValueMode();
    parser.exceptionHandler(err -> logger.error(err.getMessage(), err));
    parser.handler(e -> {
      var inventoryHoldingsAndItems = e.objectValue()
          .mapTo(InventoryHoldingsAndItems.class);
      instances.add(inventoryHoldingsAndItems);
    });
    parser.endHandler(e -> {
      logger.info("Instances received from inventory: {}", instances.size());
      promise.complete(instances);
    });

    request.sendBuffer(createPayload(instanceIds).toBuffer())
        .onFailure(promise::fail)
        .onSuccess(resp -> {
          if (resp.statusCode() != HttpStatus.HTTP_OK.toInt()) {
            promise.fail(new HttpException(resp.statusCode(), resp.statusMessage()));
          } else {
            final var buffer = resp.bodyAsBuffer();

            // If the response is empty then the buffer will be null
            // passing a null buffer to the JSON parser causes a null pointer exception
            if (buffer == null) {
              promise.complete(List.of());
            }

            parser.handle(buffer);
            parser.end();
          }
        });

    return promise.future();
  }

  private JsonObject createPayload(List<String> instanceIds) {
    final var payload = new JsonObject();
    payload.put("instanceIds", new JsonArray(instanceIds));
    payload.put("skipSuppressedFromDiscoveryRecords", true);
    logger.debug("Request body: \n {}", payload::encodePrettily);
    return payload;
  }
}
