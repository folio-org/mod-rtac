package org.folio.clients;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.folio.rest.impl.MockData.createInventoryHoldingsAndItemsForHoldCount;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.folio.rest.impl.MockData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CirculationRequestClientTests {

  private final WireMockServer fakeWebServer = new WireMockServer(Options.DYNAMIC_PORT);

  @BeforeEach
  @SneakyThrows
  void beforeEach() {
    fakeWebServer.start();
  }

  @SneakyThrows
  @AfterEach
  void afterEach() {
    fakeWebServer.stop();
  }

  @Test
  @SneakyThrows
  void holdCountCheck() {
    String circRequestUrl = "/circulation/requests?"
        + "query=itemId%3D%3D%283be8c001-3ff5-5f59-9507-8680a6572651%29&limit=10000";
    fakeWebServer.stubFor(
        get(urlEqualTo(circRequestUrl))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(MockData.REQUESTS_HOLD_COUNT_JSON)));

    var inventoryHoldingsAndItems = createInventoryHoldingsAndItemsForHoldCount();
    final var client = new CirculationRequestClient(Headers.toMap(fakeWebServer.baseUrl()),
        WebClient.create(Vertx.vertx()));
    final var futureResult = client.updateInstanceItemsWithRequestsCount(
        List.of(inventoryHoldingsAndItems),"test-tenant"
    );
    final var updatedInstanceItems = futureResult
        .toCompletionStage().toCompletableFuture().get(5, SECONDS);
    assertEquals(0, updatedInstanceItems.get(0).getItems().get(0).getTotalHoldRequests());
  }

  private static class Headers {

    private static final String TENANT_ID = "test-tenant";
    // Cannot be a representative token because it fails checkstyle
    private static final String TOKEN = "fake-token";

    static Map<String, String> toMap(String okapiUrl) {
      return new CaseInsensitiveMap<>(Map.of(
          "X-Okapi-Url", okapiUrl,
          "x-okapi-tenant", Headers.TENANT_ID,
          "x-okapi-token", Headers.TOKEN));
    }
  }
}
