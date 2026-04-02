package org.folio.clients;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class FolioClientTest {

  private static final class TestFolioClient extends FolioClient {
    TestFolioClient(WebClient webClient) {
      super(Collections.emptyMap(), webClient);
    }
  }

  @Test
  void responseFailed_whenAsyncResultFailed_failsPromise() {
    WebClient webClient = null;
    TestFolioClient client = new TestFolioClient(webClient);
    RuntimeException cause = new RuntimeException("boom");
    Promise<Void> promise = Promise.promise();

    AsyncResult<HttpResponse<Buffer>> asyncResult =
        new AsyncResult<>() {
          @Override
          public HttpResponse<Buffer> result() {
            return null;
          }

          @Override
          public Throwable cause() {
            return cause;
          }

          @Override
          public boolean succeeded() {
            return false;
          }

          @Override
          public boolean failed() {
            return true;
          }
        };

    assertTrue(client.responseFailed(asyncResult, promise, "Test operation"));
    promise
        .future()
        .onComplete(ar -> {
          assertTrue(ar.failed());
          assertSame(cause, ar.cause());
        });
  }

  @Test
  void responseFailed_whenNon200Response_failsPromiseWithHttpException(
      Vertx vertx, VertxTestContext testContext) {
    WebClient webClient = WebClient.create(vertx);
    TestFolioClient client = new TestFolioClient(webClient);

    HttpServer server =
        vertx
            .createHttpServer()
            .requestHandler(
                req ->
                    req.response()
                        .setStatusCode(500)
                        .setStatusMessage("Downstream error")
                        .end("fail-body"));

    server
        .listen(0)
        .compose(
            httpServer -> {
              int port = httpServer.actualPort();
              return webClient.get(port, "localhost", "/").send();
            })
        .onComplete(
            httpAr -> {
              Promise<Void> promise = Promise.promise();
              boolean failed = client.responseFailed(httpAr, promise, "Test operation");

              testContext.verify(() -> assertTrue(failed));
              promise
                  .future()
                  .onComplete(
                      promiseAr -> {
                        testContext.verify(
                            () -> {
                              assertTrue(promiseAr.failed());
                              assertThat(promiseAr.cause(), instanceOf(HttpException.class));
                              HttpException httpException = (HttpException) promiseAr.cause();
                              assertEquals(500, httpException.getStatusCode());
                            });
                        server.close().onComplete(testContext.succeedingThenComplete());
                      });
            });
  }

  @Test
  void responseFailed_whenOkResponse_doesNotFailPromise(Vertx vertx, VertxTestContext testContext) {
    WebClient webClient = WebClient.create(vertx);
    TestFolioClient client = new TestFolioClient(webClient);

    HttpServer server =
        vertx
            .createHttpServer()
            .requestHandler(req -> req.response().setStatusCode(200).end("ok"));

    server
        .listen(0)
        .compose(
            httpServer -> {
              int port = httpServer.actualPort();
              return webClient.get(port, "localhost", "/").send();
            })
        .onComplete(
            httpAr -> {
              Promise<String> promise = Promise.promise();
              boolean failed = client.responseFailed(httpAr, promise, "Test operation");

              testContext.verify(() -> assertFalse(failed));
              promise.complete("done");
              promise
                  .future()
                  .onComplete(
                      promiseAr -> {
                        testContext.verify(
                            () -> {
                              assertTrue(promiseAr.succeeded());
                              assertEquals("done", promiseAr.result());
                            });
                        server.close().onComplete(testContext.succeedingThenComplete());
                      });
            });
  }
}

