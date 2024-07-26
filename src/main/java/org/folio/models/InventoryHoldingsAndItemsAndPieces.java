package org.folio.models;

import java.util.List;
import org.folio.rest.jaxrs.model.InventoryHoldingsAndItems;
import org.folio.rest.jaxrs.model.Piece;

public class InventoryHoldingsAndItemsAndPieces {
  private InventoryHoldingsAndItems inventoryHoldingsAndItems;
  private List<Piece> pieces;

  public InventoryHoldingsAndItemsAndPieces(InventoryHoldingsAndItems inventoryHoldingsAndItems,
      List<Piece> pieces) {
    this.inventoryHoldingsAndItems = inventoryHoldingsAndItems;
    this.pieces = pieces;
  }

  public InventoryHoldingsAndItems getInventoryHoldingsAndItems() {
    return inventoryHoldingsAndItems;
  }

  public void setInventoryHoldingsAndItems(InventoryHoldingsAndItems inventoryHoldingsAndItems) {
    this.inventoryHoldingsAndItems = inventoryHoldingsAndItems;
  }

  public List<Piece> getPieces() {
    return pieces;
  }

  public void setPieces(List<Piece> pieces) {
    this.pieces = pieces;
  }
}
