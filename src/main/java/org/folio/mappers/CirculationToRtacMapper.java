package org.folio.mappers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.IntStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.jaxrs.model.Item;

public class CirculationToRtacMapper {

  private static final Logger logger = LogManager.getLogger();

  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

  static {
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  /**
   * Enriches instance items with with loan due date(if loan exists for an item).
   *
   * @param loanList - Loans in batches of #CirculationClient.CIRCULATION_BATCH_SIZE
   * @param items - instance items
   * @return - instance items enriched with loan due date(if it exists)
   */
  public List<Item> updateItemsWithLoanDueDate(List<JsonObject> loanList, List<Item> items) {
    var updatedItems = new ArrayList<>(items);
    IntStream.range(0, items.size())
        .forEach(
            i -> {
              for (var loansObj : loanList) {
                var totalRecords = loansObj.getValue("totalRecords");
                logger.info(
                    "Open loans received from circulation: {}",
                    totalRecords == null ? 0 : totalRecords);
                final JsonArray loans = loansObj.getJsonArray("loans");
                final Optional<JsonObject> loan =
                    loans.stream()
                        .map(a -> (JsonObject) a)
                        .filter(a -> a.getString("itemId").equals(updatedItems.get(i).getId()))
                        .findFirst();
                loan.ifPresent(
                    l -> {
                      final String dueDateString = l.getString("dueDate");
                      logger.debug("Loan {} due date: {}", l.getString("id"), dueDateString);
                      Date dueDate = null;
                      try {
                        dueDate = dateFormat.parse(dueDateString);
                      } catch (ParseException e) {
                        logger.warn(e.getMessage(), e);
                      }
                      updatedItems.set(i, items.get(i).withDueDate(dueDate));
                    });
              }
            });
    return updatedItems;
  }
}
