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
import org.folio.models.Settings;

/**
 * Client for accessing settings entries in the FOLIO platform. This client is used to fetch
 * settings based on a specific key and tenant.
 */

public class SettingsClient extends FolioClient {

  private static final String URI = "/settings/entries";
  private static final String SCOPE = "mod-rtac";
  private final Logger logger = LogManager.getLogger(getClass());


  public SettingsClient(Map<String, String> okapiHeaders,
      WebClient webClient) {
    super(okapiHeaders, webClient);
  }

  /**
   * Fetches settings for a given tenant and key.
   *
   * @param tenantId the tenant ID
   * @param key      the settings key
   * @return a Future containing the Settings object
   */

  public Future<Settings> getSettings(String tenantId, String key) {
    logger.info("Fetching settings for key: {}", key);
    Promise<Settings> promise = Promise.promise();
    String settingsUrl = String.format("%s%s", okapiUrl, URI);
    final var httpClientRequest = webClient.getAbs(settingsUrl);
    httpClientRequest
        .putHeader(OKAPI_HEADER_TOKEN, okapiToken)
        .putHeader(OKAPI_HEADER_TENANT, tenantId)
        .putHeader(ACCEPT, APPLICATION_JSON)
        .putHeader(CONTENT_TYPE, APPLICATION_JSON)
        .addQueryParam("query", String.format("(scope==%s and key==%s)", SCOPE, key));

    httpClientRequest.send(assyncResult -> handleResponse(assyncResult, promise));
    return promise.future();
  }

  private void handleResponse(AsyncResult<HttpResponse<Buffer>> asyncResult,
      Promise<Settings> promise) {
    final var httpResponse = asyncResult.result();
    if (validateHttpStatusOk(asyncResult, promise)) {
      promise.complete(httpResponse.bodyAsJson(Settings.class));
    }
  }
}
