package org.folio.clients;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.HttpException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import lombok.SneakyThrows;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SearchClientTests {

  private final WireMockServer fakeWebServer = new WireMockServer(Options.DYNAMIC_PORT);

  @BeforeEach
  @SneakyThrows
  void beforeEach() {
    fakeWebServer.start();
  }

  @AfterEach
  @SneakyThrows
  void afterEach() {
    fakeWebServer.stop();
  }

  @Test
  @SneakyThrows
  void responseFailed_isCovered_whenSearchReturnsNon200() {
    fakeWebServer.stubFor(
        get(urlPathEqualTo("/search/instances/facets"))
            .withQueryParam("facet", containing("holdings.tenantId"))
            .willReturn(aResponse().withStatus(500).withBody("downstream-error")));

    var client = new SearchClient(Headers.toMap(fakeWebServer.baseUrl()),
        WebClient.create(Vertx.vertx()));

    var future = client.getHoldingsTenantsFacet("instance-id");

    ExecutionException ex =
        assertThrows(
            ExecutionException.class,
            () -> future.toCompletionStage().toCompletableFuture().get(5, SECONDS));
    assertTrue(ex.getCause() instanceof HttpException);
  }

  private static class Headers {

    private static final String TENANT_ID = "test-tenant";
    private static final String TOKEN = "fake-token";

    static Map<String, String> toMap(String okapiUrl) {
      return new CaseInsensitiveMap<>(Map.of(
          "X-Okapi-Url", okapiUrl,
          "x-okapi-tenant", Headers.TENANT_ID,
          "x-okapi-token", Headers.TOKEN));
    }
  }
}
