package org.folio.clients;

import static org.folio.rest.RestVerticle.OKAPI_HEADER_TOKEN;

import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.HttpException;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.HttpStatus;


abstract class FolioClient {
  private static final Logger logger = LogManager.getLogger();

  protected final String okapiUrl;
  protected final String okapiToken;
  protected final WebClient webClient;

  FolioClient(Map<String, String> okapiHeaders, WebClient webClient) {
    this.okapiUrl = okapiHeaders.getOrDefault("X-Okapi-Url", "");
    this.okapiToken = okapiHeaders.getOrDefault(OKAPI_HEADER_TOKEN, "");
    this.webClient = webClient;
  }

  protected <T> boolean validateHttpStatusOk(AsyncResult<HttpResponse<Buffer>> asyncResult,
      Promise<T> promise) {
    final var httpResponse = asyncResult.result();
    if (asyncResult.failed()) {
      promise.fail(asyncResult.cause());
      return false;
    } else if (httpResponse.statusCode() != HttpStatus.HTTP_OK.toInt()) {
      logger.error("Failed with HTTP status: {}", httpResponse.statusCode());
      promise.fail(new HttpException(httpResponse.statusCode(), httpResponse.statusMessage()));
      return false;
    }
    return true;
  }
}
