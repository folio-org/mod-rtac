package org.folio.mappers;


import static org.folio.rest.impl.MockData.createInventoryHoldingsAndItemsAndNoPieces;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.vertx.core.json.JsonObject;
import java.util.List;
import org.folio.rest.impl.MockData;
import org.junit.jupiter.api.Test;

class CirculationToRtacMapperTest {

  @Test
  void testUpdateItemsWithLoanDueDateIsNull() {
    final var mapper = new CirculationToRtacMapper();
    var inventoryHoldingsAndItemsAndNoPieces = createInventoryHoldingsAndItemsAndNoPieces();
    var items = inventoryHoldingsAndItemsAndNoPieces.getInventoryHoldingsAndItems().getItems();

    var loansResponse = new JsonObject(MockData.LOAN_JSON);
    var loan = loansResponse.getJsonArray("loans").getJsonObject(0);
    loan.put("dueDate", (String) null);

    var updatedItems = mapper.updateItemsWithLoanDueDate(List.of(loansResponse), items);

    assertEquals(items.size(), updatedItems.size());
    updatedItems.forEach(item -> assertNull(item.getDueDate()));
  }

  @Test
  void testUpdateItemsWithLoanDueDateIsNotNull() {
    final var mapper = new CirculationToRtacMapper();
    var inventoryHoldingsAndItemsAndNoPieces = createInventoryHoldingsAndItemsAndNoPieces();
    var items = inventoryHoldingsAndItemsAndNoPieces.getInventoryHoldingsAndItems().getItems();

    var loansResponse = new JsonObject(MockData.LOAN_JSON);
    var loan = loansResponse.getJsonArray("loans").getJsonObject(0);

    var updatedItems = mapper.updateItemsWithLoanDueDate(List.of(loansResponse), items);

    assertEquals(items.size(), updatedItems.size());
    updatedItems.forEach(item -> {
      if (item.getId().equals(loan.getString("itemId"))) {
        assertNotNull(item.getDueDate());
      }
    });
  }

}
