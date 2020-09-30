package org.folio.mappers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.jaxrs.model.Item;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class CirculationToRtacMapper {

  private static final Logger logger = LogManager.getLogger();
  private final SimpleDateFormat dateFormat;

  public CirculationToRtacMapper() {
    dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  /**
   * RTac mapper.
   *
   * @param loansObj loan's representation in json
   * @param items items representation in json
   * @return list of item domain objects
   */
  public List<Item> mapToRtac(JsonObject loansObj, List<Item> items) {
    var updatedItems = new ArrayList<Item>();
    try {
      var totalRecords = loansObj.getValue("totalRecords");
      logger.info(
          "Open loans received from circulation: {}", totalRecords == null ? 0 : totalRecords);
      final JsonArray loans = loansObj.getJsonArray("loans");
      for (Item item : items) {
        final Optional<JsonObject> loan =
            loans.stream()
                .map(a -> (JsonObject) a)
                .filter(a -> a.getString("itemId").equals(item.getId()))
                .findFirst();
        loan.ifPresentOrElse(
            l -> {
              final String dueDateString = l.getString("dueDate");
              logger.debug("Loan {} due date: {}", l.getString("id"), dueDateString);
              Date dueDate = null;
              try {
                dueDate = dateFormat.parse(dueDateString);
              } catch (ParseException e) {
                updatedItems.add(item);
              }
              updatedItems.add(item.withDueDate(dueDate));
            },
            () -> updatedItems.add(item));
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return items;
    }
    return updatedItems;
  }
}
