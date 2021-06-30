package org.folio.mappers;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.jaxrs.model.Holding;
import org.folio.rest.jaxrs.model.InventoryHoldingsAndItems;
import org.folio.rest.jaxrs.model.Item;
import org.folio.rest.jaxrs.model.LegacyHolding;
import org.folio.rest.jaxrs.model.LegacyHoldings;
import org.folio.rest.jaxrs.model.RtacHolding;
import org.folio.rest.jaxrs.model.RtacHoldings;

public class FolioToRtacMapper {

  private final Logger logger = LogManager.getLogger(getClass());
  private final boolean fullPeriodicals;
  private static final List<String> periodicalNames = List.of("journal", "newspaper");

  public FolioToRtacMapper(boolean fullPeriodicals) {
    this.fullPeriodicals = fullPeriodicals;
  }

  /**
   * RTac mapper class.
   *
   * @param instance items and holdings
   * @return RTac holdings
   */
  public RtacHoldings mapToRtac(InventoryHoldingsAndItems instance) {
    final var rtacHoldings = new RtacHoldings();
    final var nested = new ArrayList<RtacHolding>();
    logger.info("Rtac handling periodicals: {}", fullPeriodicals);
    final var periodical = isPeriodical(instance);

    if (instance.getItems().size() == 0 && instance.getHoldings().size() == 0) {
      logger.info("{} has no items or holdings, skipping item/holdings mapping", 
          instance.getInstanceId());
      return rtacHoldings.withInstanceId(instance.getInstanceId());
    } else if (instance.getItems().size() == 0 && instance.getHoldings().size() > 0) {
      logger.info("{} has no items, mapping holdings data.", instance.getInstanceId());
      instance.getHoldings().stream().map(fromHoldingToRtacHolding).forEach(nested::add);
      return rtacHoldings.withInstanceId(instance.getInstanceId()).withHoldings(nested);
    } else if ((!periodical) || periodical && fullPeriodicals) {
      logger.info("{} is a periodical with full item data requested,", 
          instance.getInstanceId());  
      logger.info("or a non-periodical. Mapping all holdings and item data.");
      instance.getItems().stream().map(fromItemToRtacHolding).forEach(nested::add);
      return rtacHoldings.withInstanceId(instance.getInstanceId()).withHoldings(nested);
    } else {
      logger.info("{} is a periodical with full item data not requested,", 
          instance.getInstanceId());
      instance.getHoldings().stream().map(fromHoldingToRtacHolding).forEach(nested::add);
      return rtacHoldings.withInstanceId(instance.getInstanceId()).withHoldings(nested);
    }
  }

  private final Function<Item, RtacHolding> fromItemToRtacHolding =
      item ->
          new RtacHolding()
              .withId(item.getId())
              .withLocation(mapLocation(item))
              .withCallNumber(mapCallNumber(item))
              .withStatus(item.getStatus())
              .withTemporaryLoanType(item.getTemporaryLoanType())
              .withPermanentLoanType(item.getPermanentLoanType())
              .withDueDate(item.getDueDate())
              .withVolume(mapVolume(item));

  /**
   * This function is populating holding-level information for periodicals.
   * The following php script was taken as an example:
   * function holdingsLoad($response){
   * $data = json_decode($response,true);
   * $holdings = $data['holdingsRecords'];
   * echo '<holdings>';
   * foreach ($holdings as $holding){
   * $statement = $holding['holdingsStatements'][0]['statement'];
   * if ($statement == null){
   * $statement = 'Multi';
   * };
   * echo '
   * <holding>
   * <id>'.$holding['id'] .'</id>
   * <callNumber>'.htmlspecialchars($holding['callNumber']).'</callNumber>
   * <location>'.discoveryName($holding['permanentLocationId']).'</location>
   * <status>'.$statement.'</status>
   * <dueDate></dueDate>
   * </holding>';
   * };
   * echo '</holdings>';
   * }
   *
   * @param holding - folio holding
   * @return holding information to be returned to the caller
   * @see <a href=https://issues.folio.org/browse/MODRTAC-37>MODRTAC-37</a>
   */
  private final Function<Holding, RtacHolding> fromHoldingToRtacHolding =
      holding ->
          new RtacHolding()
              .withId(holding.getId())
              .withCallNumber(mapCallNumber(holding))
              .withLocation(mapLocation(holding))
              .withStatus(mapHoldingStatements(holding))
              .withDueDate(null);

  private String mapHoldingStatements(Holding holding) {
    final var holdingsStatements = holding.getHoldingsStatements();
    var result = "Multi";
    if (!holdingsStatements.isEmpty()) {
      result = holdingsStatements.get(0).getStatement();
    }
    return result;
  }

  private String mapCallNumber(Holding holding) {
    return holding.getCallNumber().getCallNumber();
  }

  private String mapCallNumber(Item item) {
    final var callNumber = item.getCallNumber();
    return assembleCallNumber(
      callNumber.getCallNumber(), callNumber.getPrefix(), callNumber.getSuffix());
  }

  private String mapLocation(Holding holding) {
    return holding.getLocation().getPermanentLocation().getName();
  }

  private String mapLocation(Item item) {
    return item.getLocation().getLocation().getName();
  }

  private boolean isPeriodical(InventoryHoldingsAndItems instance) {
    return isPeriodicalByNatureOfContent(instance)
        || Objects.equals(instance.getModeOfIssuance(), "serial");
  }

  private boolean isPeriodicalByNatureOfContent(InventoryHoldingsAndItems instance) {
    return instance.getNatureOfContent().stream()
        .map(String::toLowerCase)
        .anyMatch(periodicalNames::contains);
  }

  /**
   * RTac mapper class.
   *
   * @param instance items and holdings
   * @return Holdings
   * @deprecated this will be removed soon.
   */
  @Deprecated(since = "1.6.0")
  public LegacyHoldings mapToLegacy(InventoryHoldingsAndItems instance) {
    final var rtacHoldings = new LegacyHoldings();

    final var nested = new ArrayList<LegacyHolding>();

    for (Item item : instance.getItems()) {
      final var rtacHolding =
          new LegacyHolding()
              .withId(item.getId())
              .withLocation(mapLocation(item))
              .withCallNumber(mapCallNumber(item))
              .withStatus(item.getStatus())
              .withDueDate(item.getDueDate())
              .withVolume(mapVolume(item));

      nested.add(rtacHolding);
    }

    return rtacHoldings.withHoldings(nested);
  }

  private String assembleCallNumber(String callNumber, String prefix, String suffix) {
    if (isNotEmpty(prefix)) {
      callNumber = prefix + " " + callNumber;
    }
    if (isNotEmpty(suffix)) {
      callNumber = callNumber + " " + suffix;
    }
    return callNumber;
  }

  /**
   * Generating rules.
   * <p/>
   * The rules for generating "volume" are as follows:
   * |data set                     |"volume"                    |
   * |-----------------------------|----------------------------|
   * |enumeration                  |(enumeration)               |
   * |enumeration chronology       |(enumeration chronology)    |
   * |enumeration chronology volume|(enumeration chronology)    |
   * |volume                       |(volume)                    |
   * |chronology volume            |(volume)                    |
   * |chronology                   |(chronology)                |
   *
   * @param item - folio inventory item
   */
  private String mapVolume(Item item) {
    final String enumeration = item.getEnumeration();
    final String chronology = item.getChronology();
    final String volume = item.getVolume();

    final StringJoiner sj = new StringJoiner(" ", "(", ")").setEmptyValue("");

    if (isNotBlank(enumeration)) {
      sj.add(enumeration);
      if (isNotBlank(chronology)) {
        sj.add(chronology);
      }
    } else if (isNotBlank(volume)) {
      sj.add(volume);
    } else if (isNotBlank(chronology)) {
      sj.add(chronology);
    }

    return defaultIfEmpty(sj.toString(), null);
  }
}
