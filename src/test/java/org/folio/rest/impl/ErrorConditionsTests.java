package org.folio.rest.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.folio.rest.jaxrs.model.Holding;
import org.folio.rest.jaxrs.model.Holdings;
import org.junit.jupiter.api.Test;

class ErrorConditionsTests {
  @Test
  void testGetHoldingsCompletionException() {
    CompletionException exception = assertThrows(CompletionException.class, () ->
        new RtacResourceImpl().getHoldings("id", null, null));
    assertEquals("java.lang.NullPointerException", exception.getMessage());
  }

  @Test
  void testGetItemsCompletionException() {
    CompletionException exception = assertThrows(CompletionException.class, () ->
        new RtacResourceImpl().getItems(new JsonArray().add(new JsonObject()), null, null));
    assertEquals("java.lang.NullPointerException", exception.getMessage());
  }

  @Test
  void testGetItemsNoHoldings() {
    final CompletableFuture<Holdings> holdings = new RtacResourceImpl()
        .getItems(new JsonArray(), null, null);
    assertEquals(0, holdings.join().getHoldings().size());
  }

  @Test
  void testCheckForLoansCompletionException() {
    CompletionException exception = assertThrows(CompletionException.class, () ->
        new RtacResourceImpl().checkForLoans(
            new Holdings().withHoldings(Collections.singletonList(new Holding())), null, null));
    assertEquals("java.lang.NullPointerException", exception.getMessage());
  }

  @Test
  void testAssignDueDateDoesNotThrowException() {
    assertDoesNotThrow(() ->
        new RtacResourceImpl().assignDueDate(
            new Holding().withId("1234"), new JsonObject().put("totalRecords", 2)));
  }
}
