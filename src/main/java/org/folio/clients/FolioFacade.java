package org.folio.clients;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.folio.mappers.FolioToRtacMapper;
import org.folio.rest.jaxrs.model.RtacHoldingsBatch;

public class FolioFacade {

  private final InventoryClient inventoryClient;
  private final CirculationClient circulationClient;
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final FolioToRtacMapper folioToRtacMapper = new FolioToRtacMapper();

  public FolioFacade(Map<String, String> okapiHeaders) {
    this.inventoryClient = new InventoryClient(okapiHeaders);
    this.circulationClient = new CirculationClient(okapiHeaders);
  }

  /**
   * Returns batch info for instances items and holdings.
   *
   * @param instanceIds passed instances ids
   * @return items and holdings for instances
   */
  public Future<RtacHoldingsBatch> getItemAndHoldingInfo(List<String> instanceIds) {
    Promise<RtacHoldingsBatch> promise = Promise.promise();

    return inventoryClient.getItemAndHoldingInfo(instanceIds)
        .compose(circulationClient::getLoansForItems)
        .compose(instances -> {
                final var rtacHoldingsList = instances.stream()
                    .map(folioToRtacMapper::mapToRtac)
                    .collect(Collectors.toList());
                logger.info("Mapping inventory instances: {}", rtacHoldingsList.size());
                promise.complete(new RtacHoldingsBatch().withHoldings(rtacHoldingsList));
                return promise.future();
              }
        ).onFailure(promise::fail);
  }
}
