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
import org.folio.rest.jaxrs.model.UserTenants;

public class UsersClient extends FolioClient {

  private static final Logger logger = LogManager.getLogger();
  private static final String URI = "/user-tenants";


  public UsersClient(Map<String, String> okapiHeaders, WebClient webClient) {
    super(okapiHeaders, webClient);
  }

  Future<UserTenants> getUserTenants(String tenantId) {
    logger.info("Fetching user tenants.");
    Promise<UserTenants> promise = Promise.promise();

    String usersUrl = String.format("%s%s", okapiUrl, URI);
    final var httpClientRequest = webClient.getAbs(usersUrl);
    httpClientRequest
        .putHeader(OKAPI_HEADER_TOKEN, okapiToken)
        .putHeader(OKAPI_HEADER_TENANT, tenantId)
        .putHeader(ACCEPT, APPLICATION_JSON)
        .putHeader(CONTENT_TYPE, APPLICATION_JSON);

    httpClientRequest.send(asyncResult -> handleResponse(asyncResult, promise));
    return promise.future();
  }

  private void handleResponse(AsyncResult<HttpResponse<Buffer>> asyncResult,
      Promise<UserTenants> promise) {
    final var httpResponse = asyncResult.result();
    if (validateHttpStatusOk(asyncResult, promise)) {
      promise.complete(httpResponse.bodyAsJson(UserTenants.class));
    }
  }
}
