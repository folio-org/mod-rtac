package org.folio.mappers;

import static org.folio.rest.impl.MockData.createInventoryHoldingsAndItems;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.folio.rest.jaxrs.model.Item;
import org.junit.jupiter.api.Test;

class FolioToRtacMapperTest {

  @Test
  void testMapToRtacForAPeriodicalWithFullPeriodicalsTrue() {
    final var folioToRtacMapper = new FolioToRtacMapper(true);
    var inventoryHoldingsAndItems = createInventoryHoldingsAndItems().withModeOfIssuance("serial");
    Item item = inventoryHoldingsAndItems.getItems().get(0);
    final var rtacHoldings = folioToRtacMapper.mapToRtac(inventoryHoldingsAndItems);
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
    var inventoryHoldingsAndItems = createInventoryHoldingsAndItems().withModeOfIssuance("serial");
    Item item = inventoryHoldingsAndItems.getItems().get(1);
    final var rtacHoldings = folioToRtacMapper.mapToRtac(inventoryHoldingsAndItems);
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
    var inventoryHoldingsAndItems = createInventoryHoldingsAndItems().withModeOfIssuance("serial");
    final var holding = inventoryHoldingsAndItems.getHoldings().get(0);
    final var rtacHoldings = folioToRtacMapper.mapToRtac(inventoryHoldingsAndItems);
    final var rtacHolding = rtacHoldings.getHoldings().get(0);

    assertEquals(holding.getId(), rtacHolding.getId());
    assertEquals(holding.getCallNumber().getCallNumber(), rtacHolding.getCallNumber());
    assertEquals(holding.getLocation().getPermanentLocation().getName(), rtacHolding.getLocation());
  }
}
