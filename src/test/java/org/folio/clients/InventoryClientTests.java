package org.folio.clients;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.folio.rest.jaxrs.model.InventoryHoldingsAndItems;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InventoryClientTests {
  private final WireMockServer fakeWebServer = new WireMockServer();

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
  public void mapInstancesReceivedFromHierarchyApi() {
    final var instanceId = UUID.randomUUID().toString();

    fakeWebServer.stubFor(hierarchyApiEndpointFor(instanceId).willReturn(ok()
        .withBody(dummyJsonResponseBody(instanceId))
        .withHeader("Content-Type", "application/json")));

    final var client = new InventoryClient(Headers.toMap(fakeWebServer.baseUrl()),
        WebClient.create(Vertx.vertx()));

    final var futureResult = client.getItemAndHoldingInfo(List.of(instanceId));

    final var itemsAndHoldings = futureResult.toCompletionStage()
        .toCompletableFuture().get(1, SECONDS);

    final var fetchedInstanceIds = itemsAndHoldings.stream()
        .map(InventoryHoldingsAndItems::getInstanceId)
        .collect(Collectors.toList());

    assertThat(fetchedInstanceIds, contains(instanceId));
  }

  @Test
  @SneakyThrows
  public void returnNoInstancesWhenHierarchyApiResponseBodyIsEmpty() {
    final var instanceId = UUID.randomUUID().toString();

    fakeWebServer.stubFor(hierarchyApiEndpointFor(instanceId).willReturn(ok()
        .withHeader("Content-Type", "application/json")));

    final var client = new InventoryClient(Headers.toMap(fakeWebServer.baseUrl()),
        WebClient.create(Vertx.vertx()));

    final var futureResult = client.getItemAndHoldingInfo(List.of(instanceId));

    final var itemsAndHoldings = futureResult.toCompletionStage()
        .toCompletableFuture().get(1, SECONDS);

    assertThat(itemsAndHoldings, is(empty()));
  }

  private MappingBuilder hierarchyApiEndpointFor(String instanceId) {
    return matchingFolioHeaders(post(urlPathEqualTo("/inventory-hierarchy/items-and-holdings")))
      .withHeader("Content-Type", equalTo("application/json"))
      .withRequestBody(equalToJson(
        dummyJsonRequestBody(List.of(instanceId)).encodePrettily()));
  }

  private MappingBuilder matchingFolioHeaders(MappingBuilder mappingBuilder) {
    return mappingBuilder
      // RMB defines lower case definitions for these headers
      .withHeader("x-okapi-tenant", equalTo(Headers.tenantId))
      .withHeader("x-okapi-token", equalTo(Headers.token));
  }

  private JsonObject dummyJsonRequestBody(List<String> expectedInstanceIds) {
    return new JsonObject()
      .put("instanceIds", new JsonArray(expectedInstanceIds))
      .put("skipSuppressedFromDiscoveryRecords", true);
  }

  private String dummyJsonResponseBody(String expectedInstanceId) {
    // Even though the API can respond with multiple records,
    // it includes them a concatenated JSON objects rather than within an array
    return new JsonObject()
      .put("instanceId", expectedInstanceId)
      .encodePrettily();
  }

  private static class Headers {
    private static final String tenantId = "test-tenant";
    // Cannot be a representative token because it fails checkstyle
    private static final String token = "fake-token";
    
    static Map<String, String> toMap(String okapiUrl) {
      return Map.of(
        "X-Okapi-Url", okapiUrl,
        // RMB defines lower case definitions for these headers
        "x-okapi-tenant", Headers.tenantId,
        "x-okapi-token", Headers.token);
    }
  }
}
