package org.folio.clients;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.folio.rest.RestVerticle.OKAPI_HEADER_TENANT;
import static org.folio.rest.RestVerticle.OKAPI_HEADER_TOKEN;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.mappers.CirculationToRtacMapper;
import org.folio.rest.jaxrs.model.InventoryHoldingsAndItems;
import org.folio.rest.jaxrs.model.Item;
import org.folio.rtac.rest.exceptions.HttpException;

class CirculationClient extends FolioClient {

  private static final int CIRCULATION_BATCH_SIZE = 50;
  private static final CirculationToRtacMapper circulationToRtacMapper =
      new CirculationToRtacMapper();

  private static final String URI = "/loan-storage/loans";

  private static final WebClient webClient = WebClient.create(Vertx.currentContext().owner());
  private final Logger logger = LogManager.getLogger(getClass());

  CirculationClient(Map<String, String> okapiHeaders) {
    super(okapiHeaders);
  }

  Future<List<InventoryHoldingsAndItems>> getLoansForItems(
      List<InventoryHoldingsAndItems> inventoryInstances) {

    logger.info("Getting loan from circulation");
    Promise<List<InventoryHoldingsAndItems>> promise = Promise.promise();

    if (CollectionUtils.isEmpty(inventoryInstances)) {
      promise.complete(inventoryInstances);
      return promise.future();
    }

    final var httpClientRequest = buildRequest();
    final var instances = new CopyOnWriteArrayList<InventoryHoldingsAndItems>();
    var futures = new ArrayList<Future>();
    for (InventoryHoldingsAndItems inventoryInstance : inventoryInstances) {
      final List<Item> items = inventoryInstance.getItems();
      if (CollectionUtils.isEmpty(items)) {
        instances.add(inventoryInstance);
        continue;
      }

      for (List<Item> itemList : ListUtils.partition(items, CIRCULATION_BATCH_SIZE)) {
        String cql = buildCql(itemList);
        logger.debug("?query={}", cql);
        futures.add(queryForLoans(httpClientRequest.copy(), inventoryInstance, cql)
            .onSuccess(instances::addAll));
      }
    }
    CompositeFuture.all(futures).onSuccess(r -> {
      if (instances.isEmpty()) {
        promise.complete(inventoryInstances);
      } else {
        promise.complete(instances);
      }
    }).onFailure(promise::fail);

    return promise.future();
  }

  private Future<List<InventoryHoldingsAndItems>> queryForLoans(
      HttpRequest<Buffer> httpClientRequest, InventoryHoldingsAndItems inventoryInstance,
      String cql) {

    Promise<List<InventoryHoldingsAndItems>> promise = Promise.promise();
    var result = new ArrayList<InventoryHoldingsAndItems>();

    httpClientRequest.addQueryParam("query", cql).send(ar -> {
          final var httpResponse = ar.result();
          if (ar.failed()) {
            promise.fail(new HttpException(httpResponse.statusCode(),
                httpResponse.statusMessage()));
          } else {
            final var i = httpResponse.statusCode();
            if (i != 200) {
              promise.fail(new HttpException(httpResponse.statusCode(),
                  httpResponse.statusMessage()));
              return;
            } else {
              final JsonObject loans = httpResponse.bodyAsJsonObject();
              final InventoryHoldingsAndItems updatedItems =
                  inventoryInstance
                      .withItems(
                          circulationToRtacMapper.mapToRtac(loans, inventoryInstance.getItems()));
              result.add(updatedItems);
            }
          }
          promise.tryComplete(result);
        }
    );
    return promise.future();
  }

  private HttpRequest<Buffer> buildRequest() {
    var url = String.format("%s%s", okapiUrl, URI);
    logger.info("Sending request to {}", url);

    final var httpClientRequest = webClient.getAbs(url);

    httpClientRequest
        .putHeader(OKAPI_HEADER_TOKEN, okapiToken)
        .putHeader(OKAPI_HEADER_TENANT, tenantId)
        .putHeader(ACCEPT, APPLICATION_JSON)
        .putHeader(CONTENT_TYPE, APPLICATION_JSON).addQueryParam("limit", "10000");

    return httpClientRequest;
  }

  private String buildCql(List<Item> items) {
    final StringBuilder cql = new StringBuilder();
    final String query = items.stream()
        .map(i -> "itemId==" + i.getId())
        .collect(Collectors.joining(" or ", "(", ")"));
    cql.append(query);
    cql.append("and status.name==open");

    return cql.toString();
  }
}
