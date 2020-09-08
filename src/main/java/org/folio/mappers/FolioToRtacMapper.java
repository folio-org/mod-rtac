package org.folio.mappers;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.StringJoiner;

import org.folio.rest.jaxrs.model.InventoryHoldingsAndItems;
import org.folio.rest.jaxrs.model.Item;
import org.folio.rest.jaxrs.model.RtacHolding;
import org.folio.rest.jaxrs.model.RtacHoldings;

public class FolioToRtacMapper {

  public RtacHoldings mapToRtac(InventoryHoldingsAndItems instance) {
    final var rtacHoldings = new RtacHoldings();

    final var nested = new ArrayList<RtacHolding>();

    for (Item item : instance.getItems()) {
      final var rtacHolding = new RtacHolding()
        .withId(item.getId())
        .withLocation(mapLocation(item))
        .withCallNumber(mapCallNumber(item))
        .withStatus(item.getStatus())
        .withTemporaryLoanType(item.getTemporaryLoanType())
        .withPermanentLoanType(item.getPermanentLoanType())
        .withDueDate(item.getDueDate())
        .withVolume(mapVolume(item));
      nested.add(rtacHolding);
    }

    return rtacHoldings.withInstanceId(instance.getInstanceId()).withHoldings(nested);
  }

  private String mapLocation(Item item) {
    return item.getLocation().getLocation().getName();
  }

  private String mapCallNumber(Item item) {
    final var callNumber = item.getCallNumber();
    return assembleCallNumber(callNumber.getCallNumber(), callNumber.getPrefix(), callNumber.getSuffix());
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
   * The rules for generating "volume" are as follows:
   * |data set                     |"volume"                    |
   * |-----------------------------|----------------------------|
   * |enumeration                  |(<enumeration>)             |
   * |enumeration chronology       |(<enumeration> <chronology>)|
   * |enumeration chronology volume|(<enumeration> <chronology>)|
   * |volume                       |(<volume>)                  |
   * |chronology volume            |(<volume>)                  |
   * |chronology                   |(<chronology>)              |
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
