package org.folio.mappers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.folio.rest.jaxrs.model.Item;

public class CirculationToRtacMapper {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * RTac mapper.
   *
   * @param loansObj loan's representation in json
   * @param items items  representation in json
   * @return list of item domain objects
   */
  public List<Item> mapToRtac(JsonObject loansObj, List<Item> items) {
    var totalRecords = loansObj.getValue("totalRecords");
    logger
        .info("Open loans received from circulation: {}", totalRecords == null ? 0 : totalRecords);
    final JsonArray loans = loansObj.getJsonArray("loans");
    var updatedItems = new ArrayList<Item>();
    try {
      for (Item item : items) {
        final Optional<JsonObject> loan = loans.stream()
            .map(a -> (JsonObject) a)
            .filter(a -> a.getString("itemId").equals(item.getId())).findFirst();
        loan.ifPresentOrElse(l -> {
          final String dueDateString = l.getString("dueDate");
          updatedItems.add(item.withDueDate(dueDateString));
        }, () -> updatedItems.add(item));
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return items;
    }
    return updatedItems;
  }

}
