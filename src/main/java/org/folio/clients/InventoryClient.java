package org.folio.clients;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.parsetools.JsonParser;
import io.vertx.core.parsetools.impl.JsonParserImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.folio.rest.jaxrs.model.InventoryHoldingsAndItems;
import org.folio.rtac.rest.exceptions.HttpException;

class InventoryClient extends FolioClient {

  private static final String VIEW_URI = "/inventory-hierarchy/items-and-holdings";
  private static final String OKAPI_TOKEN_KEY = "X-Okapi-Token";
  private static final String OKAPI_TENANT_KEY = "X-Okapi-Tenant";
  private static final HttpClient httpClient = Vertx.vertx().createHttpClient();

  private final Logger logger = LoggerFactory.getLogger(getClass());

  InventoryClient(Map<String, String> okapiHeaders) {
    super(okapiHeaders);
  }

  Future<List<InventoryHoldingsAndItems>> getItemAndHoldingInfo(List<String> instanceIds) {
    logger.info("Getting item and holding information from inventory");
    Promise<List<InventoryHoldingsAndItems>> promise = Promise.promise();

    if (CollectionUtils.isEmpty(instanceIds)) {
      promise.fail("InstanceIds is empty");
      return promise.future();
    }

    final var httpClientRequest = buildRequest();
    final var inventoryClientRequest = httpClientRequest.handler(
        resp -> {
          // resp.endHandler(eh -> httpClient.close());
          final var instances = new ArrayList<InventoryHoldingsAndItems>();
          final var i = resp.statusCode();
          if (i != 200) {
            promise.fail(new HttpException(resp.statusCode(), resp.statusMessage()));
          } else {
            JsonParser jp = new JsonParserImpl(resp);
            jp.objectValueMode();
            jp.handler(e -> {
              try {
                var inventoryHoldingsAndItems = e.objectValue()
                    .mapTo(InventoryHoldingsAndItems.class);
                instances.add(inventoryHoldingsAndItems);
              } catch (Exception exception) {
                logger.error(exception.getMessage(), exception);
              }
            });
            jp.endHandler(e -> {
              logger.info("Instances received from inventory: {}", instances.size());
              promise.complete(instances);
            });
          }
        }
    );

    final var payload = createPayload(instanceIds);
    inventoryClientRequest.exceptionHandler(promise::fail);
    inventoryClientRequest.end(payload.toBuffer());

    return promise.future();
  }

  private HttpClientRequest buildRequest() {
    var inventoryUrl = String.format("%s%s", okapiUrl, VIEW_URI);
    logger.info("Sending request to {}", inventoryUrl);

    final var httpClientRequest = httpClient.postAbs(inventoryUrl)

        .putHeader(OKAPI_TOKEN_KEY, okapiToken)
        .putHeader(OKAPI_TENANT_KEY, tenantId)
        .putHeader(ACCEPT, APPLICATION_JSON)
        .putHeader(CONTENT_TYPE, APPLICATION_JSON);

    httpClientRequest.setChunked(true);
    return httpClientRequest;
  }

  private JsonObject createPayload(List<String> instanceIds) {
    final var payload = new JsonObject();
    payload.put("instanceIds", new JsonArray(instanceIds));
    payload.put("skipSuppressedFromDiscoveryRecords", true);
    logger.debug("Request body: \n {}", payload.encodePrettily());
    return payload;
  }
}
