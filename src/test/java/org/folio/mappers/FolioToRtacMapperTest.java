package org.folio.mappers;

import static org.folio.rest.impl.MockData.createInventoryHoldingsAndItemsAndNoPieces;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.folio.models.InventoryHoldingsAndItemsAndPieces;
import org.folio.rest.impl.MockData;
import org.folio.rest.jaxrs.model.InventoryHoldingsAndItems;
import org.folio.rest.jaxrs.model.Item;
import org.junit.jupiter.api.Test;

class FolioToRtacMapperTest {

  @Test
  void testMapToRtacForAPeriodicalWithFullPeriodicalsTrue() {
    final var folioToRtacMapper = new FolioToRtacMapper(true);
    var inventoryHoldingsAndItemsAndNoPieces = createInventoryHoldingsAndItemsAndNoPieces();
    var inventoryHoldingsAndItems = inventoryHoldingsAndItemsAndNoPieces
        .getInventoryHoldingsAndItems().withModeOfIssuance("serial");
    Item item = inventoryHoldingsAndItems.getItems().get(0);
    final var rtacHoldings = folioToRtacMapper.mapToRtac(inventoryHoldingsAndItemsAndNoPieces);
    final var rtacHolding = rtacHoldings.getHoldings().get(0);

    assertEquals(item.getId(), rtacHolding.getId());
    assertEquals(item.getCallNumber().getCallNumber(), rtacHolding.getCallNumber());
    assertEquals(item.getLocation().getLocation().getName(), rtacHolding.getLocation());
    assertEquals(item.getOrder(), rtacHolding.getItemDisplayOrder());
    String expectedVolume = "(" + item.getEnumeration() + " " + item.getChronology() + ")";
    assertEquals(expectedVolume, rtacHolding.getVolume());
    assertEquals(item.getDueDate(), rtacHolding.getDueDate());
    assertFalse(rtacHolding.getNotes().isEmpty());
    assertEquals(inventoryHoldingsAndItems.getHoldings().get(0).getNotes(), rtacHolding.getNotes());
  }

  @Test
  void testMapToRtacForAnItemWithDisplaySummary() {
    final var folioToRtacMapper = new FolioToRtacMapper(true);
    var inventoryHoldingsAndItemsAndNoPieces = createInventoryHoldingsAndItemsAndNoPieces();
    var inventoryHoldingsAndItems = inventoryHoldingsAndItemsAndNoPieces
        .getInventoryHoldingsAndItems().withModeOfIssuance("serial");
    Item item = inventoryHoldingsAndItems.getItems().get(1);
    final var rtacHoldings = folioToRtacMapper.mapToRtac(inventoryHoldingsAndItemsAndNoPieces);
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
    var inventoryHoldingsAndItemsAndNoPieces = createInventoryHoldingsAndItemsAndNoPieces();
    var inventoryHoldingsAndItems = inventoryHoldingsAndItemsAndNoPieces
        .getInventoryHoldingsAndItems().withModeOfIssuance("serial");
    final var holding = inventoryHoldingsAndItems.getHoldings().get(0);
    final var rtacHoldings = folioToRtacMapper.mapToRtac(inventoryHoldingsAndItemsAndNoPieces);
    final var rtacHolding = rtacHoldings.getHoldings().get(0);

    assertEquals(holding.getId(), rtacHolding.getId());
    assertEquals(holding.getCallNumber().getCallNumber(), rtacHolding.getCallNumber());
    assertEquals(holding.getLocation().getPermanentLocation().getName(), rtacHolding.getLocation());
  }

  @Test
  void testMapToRtacForPieces() {
    final var folioToRtacMapper = new FolioToRtacMapper(false);
    var inventoryHoldingsAndItemsAndPieces = new InventoryHoldingsAndItemsAndPieces(
        MockData.INSTANCE_WITH_HOLDINGS_AND_PIECES, MockData.PIECE_COLLECTION.getPieces());
    var inventoryHoldingsAndItems = inventoryHoldingsAndItemsAndPieces
        .getInventoryHoldingsAndItems().withModeOfIssuance("serial");
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

  @Test
  void testMapToRtacForItemWithNullCallNumber_returnsNullInsteadOfThrowing() {
    final var objectMapper = new ObjectMapper();
    final var instance = objectMapper.convertValue(
        MockData.createInventoryHoldingsAndItemsAndNoPieces().getInventoryHoldingsAndItems(),
        InventoryHoldingsAndItems.class);
    instance.getItems().get(0).setCallNumber(null);
    final var instanceAndPieces =
        new InventoryHoldingsAndItemsAndPieces(instance, Collections.emptyList());
    final var folioToRtacMapper = new FolioToRtacMapper(true);

    final var rtacHoldings = folioToRtacMapper.mapToRtac(instanceAndPieces);

    assertNull(rtacHoldings.getHoldings().get(0).getCallNumber());
  }

  @Test
  void testMapToRtacForHoldingWithNullCallNumber_returnsNullInsteadOfThrowing() {
    final var objectMapper = new ObjectMapper();
    final var instance = objectMapper.convertValue(
        MockData.createInventoryHoldingsAndItemsAndNoPieces().getInventoryHoldingsAndItems(),
        InventoryHoldingsAndItems.class);
    instance.setItems(Collections.emptyList());
    instance.getHoldings().get(0).setCallNumber(null);
    final var instanceAndPieces =
        new InventoryHoldingsAndItemsAndPieces(instance, Collections.emptyList());
    final var folioToRtacMapper = new FolioToRtacMapper(false);

    final var rtacHoldings = folioToRtacMapper.mapToRtac(instanceAndPieces);

    assertNull(rtacHoldings.getHoldings().get(0).getCallNumber());
  }
}
