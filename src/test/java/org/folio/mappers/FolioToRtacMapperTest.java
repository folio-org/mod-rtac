package org.folio.mappers;

import static org.folio.rest.impl.MockData.createInventoryHoldingsAndItemsAndPieces;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import org.folio.rest.jaxrs.model.Item;
import org.junit.jupiter.api.Test;

class FolioToRtacMapperTest {

  @Test
  void testMapToRtacForAPeriodicalWithFullPeriodicalsTrue() {
    final var folioToRtacMapper = new FolioToRtacMapper(true);
    var inventoryHoldingsAndItemsAndPieces = createInventoryHoldingsAndItemsAndPieces();
    var inventoryHoldingsAndItems = inventoryHoldingsAndItemsAndPieces
        .getInventoryHoldingsAndItems().withModeOfIssuance("serial");
    Item item = inventoryHoldingsAndItems.getItems().get(0);
    final var rtacHoldings = folioToRtacMapper.mapToRtac(inventoryHoldingsAndItemsAndPieces);
    final var rtacHolding = rtacHoldings.getHoldings().get(0);

    assertEquals(item.getId(), rtacHolding.getId());
    assertEquals(item.getCallNumber().getCallNumber(), rtacHolding.getCallNumber());
    assertEquals(item.getLocation().getLocation().getName(), rtacHolding.getLocation());
    String expectedVolume = "(" + item.getEnumeration() + " " + item.getChronology() + ")";
    assertEquals(expectedVolume, rtacHolding.getVolume());
    assertEquals(item.getDueDate(), rtacHolding.getDueDate());
  }

  @Test
  void testMapToRtacForAnItemWithDisplaySummary() {
    final var folioToRtacMapper = new FolioToRtacMapper(true);
    var inventoryHoldingsAndItemsAndPieces = createInventoryHoldingsAndItemsAndPieces();
    var inventoryHoldingsAndItems = inventoryHoldingsAndItemsAndPieces
        .getInventoryHoldingsAndItems().withModeOfIssuance("serial");
    Item item = inventoryHoldingsAndItems.getItems().get(1);
    final var rtacHoldings = folioToRtacMapper.mapToRtac(inventoryHoldingsAndItemsAndPieces);
    final var rtacHolding = rtacHoldings.getHoldings().get(1);

    assertEquals(item.getCallNumber().getCallNumber(), rtacHolding.getCallNumber());
    assertEquals(item.getLocation().getLocation().getName(), rtacHolding.getLocation());
    String expectedVolume = "(" + item.getDisplaySummary() + ")";
    assertEquals(expectedVolume, rtacHolding.getVolume());
    assertEquals(item.getDueDate(), rtacHolding.getDueDate());
  }

  @Test
  void testMapToRtacForAPeriodicalWithFullPeriodicalsFalse() {
    final var folioToRtacMapper = new FolioToRtacMapper(false);
    var inventoryHoldingsAndItemsAndPieces = createInventoryHoldingsAndItemsAndPieces();
    var inventoryHoldingsAndItems = inventoryHoldingsAndItemsAndPieces
        .getInventoryHoldingsAndItems().withModeOfIssuance("serial");
    final var holding = inventoryHoldingsAndItems.getHoldings().get(0);
    final var rtacHoldings = folioToRtacMapper.mapToRtac(inventoryHoldingsAndItemsAndPieces);
    final var rtacHolding = rtacHoldings.getHoldings().get(0);

    assertEquals(holding.getId(), rtacHolding.getId());
    assertEquals(holding.getCallNumber().getCallNumber(), rtacHolding.getCallNumber());
    assertEquals(holding.getLocation().getPermanentLocation().getName(), rtacHolding.getLocation());
  }

  @Test
  void testMapToRtacForPieces() {
    final var folioToRtacMapper = new FolioToRtacMapper(false);
    var inventoryHoldingsAndItemsAndPieces = createInventoryHoldingsAndItemsAndPieces();
    var inventoryHoldingsAndItems = inventoryHoldingsAndItemsAndPieces
        .getInventoryHoldingsAndItems()
        .withItems(Collections.emptyList())
        .withModeOfIssuance("serial");
    final var holding = inventoryHoldingsAndItems.getHoldings().get(0);
    final var rtacHoldings = folioToRtacMapper.mapToRtac(inventoryHoldingsAndItemsAndPieces);
    final var rtacHoldingsResponse = rtacHoldings.getHoldings();

    assertEquals(holding.getId(), rtacHoldingsResponse.get(0).getId());
    assertEquals(holding.getId(), rtacHoldingsResponse.get(1).getId());
    assertEquals("Received", rtacHoldingsResponse.get(1).getStatus());
    assertEquals(holding.getId(), rtacHoldingsResponse.get(2).getId());
    assertEquals("Expected", rtacHoldingsResponse.get(2).getStatus());
    assertEquals(holding.getId(), rtacHoldingsResponse.get(3).getId());
    assertEquals("Expected", rtacHoldingsResponse.get(3).getStatus());
  }
}
