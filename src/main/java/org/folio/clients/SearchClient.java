package org.folio.clients;

import static jakarta.ws.rs.core.HttpHeaders.ACCEPT;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.folio.rest.RestVerticle.OKAPI_HEADER_TENANT;
import static org.folio.rest.RestVerticle.OKAPI_HEADER_TOKEN;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.jaxrs.model.HoldingsFacet;
import org.folio.rest.tools.utils.TenantTool;

public class SearchClient extends FolioClient {

  private static final Logger logger = LogManager.getLogger();
  private static final String URI = "/search/instances/facets";
  private static final String FACET = "holdings.tenantId";
  private final String tenantId;

  public SearchClient(Map<String, String> okapiHeaders, WebClient webClient) {
    super(okapiHeaders, webClient);
    this.tenantId = TenantTool.tenantId(okapiHeaders);
  }

  Future<HoldingsFacet> getHoldingsTenantsFacet(String instanceId) {
    logger.info("Fetching holdings tenants facet");
    Promise<HoldingsFacet> promise = Promise.promise();

    String searchUrl = String.format("%s%s", okapiUrl, URI);
    final var httpClientRequest = webClient.getAbs(searchUrl);
    httpClientRequest
        .putHeader(OKAPI_HEADER_TOKEN, okapiToken)
        .putHeader(OKAPI_HEADER_TENANT, tenantId)
        .putHeader(ACCEPT, APPLICATION_JSON)
        .putHeader(CONTENT_TYPE, APPLICATION_JSON)
        .addQueryParam("facet", FACET)
        .addQueryParam("query", "id=" + instanceId);

    httpClientRequest.send(asyncResult -> handleResponse(asyncResult, promise));
    return promise.future();
  }

  private void handleResponse(AsyncResult<HttpResponse<Buffer>> asyncResult,
      Promise<HoldingsFacet> promise) {
    final var httpResponse = asyncResult.result();
    if (validateHttpStatusOk(asyncResult, promise)) {
      promise.complete(httpResponse.bodyAsJson(HoldingsFacet.class));
    }
  }
}
