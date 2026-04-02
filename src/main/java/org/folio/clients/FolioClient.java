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

  protected <T> boolean responseFailed(AsyncResult<HttpResponse<Buffer>> asyncResult,
                                       Promise<T> promise, String operation) {
    if (asyncResult.failed()) {
      logger.error("{} failed", operation, asyncResult.cause());
      promise.fail(asyncResult.cause());
      return true;
    }

    final var httpResponse = asyncResult.result();
    final int status = httpResponse.statusCode();
    final String message = httpResponse.statusMessage();
    if (status != HttpStatus.HTTP_OK.toInt()) {
      final String body = httpResponse.bodyAsString();
      logger.error("{} failed: status={} message={} body={}", operation, status, message, body);
      promise.fail(new HttpException(status, message));
      return true;
    }

    return false;
  }
}
