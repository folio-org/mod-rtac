package org.folio.clients;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.mappers.ErrorMapper;
import org.folio.mappers.FolioToRtacMapper;
import org.folio.rest.jaxrs.model.InventoryHoldingsAndItems;
import org.folio.rest.jaxrs.model.RtacHoldings;
import org.folio.rest.jaxrs.model.RtacHoldingsBatch;

public class FolioFacade {

  private final InventoryClient inventoryClient;
  private final CirculationClient circulationClient;
  private final Logger logger = LogManager.getLogger(getClass());
  private final FolioToRtacMapper folioToRtacMapper = new FolioToRtacMapper();
  private final ErrorMapper errorMapper = new ErrorMapper();

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

    return inventoryClient
        .getItemAndHoldingInfo(instanceIds)
        .compose(circulationClient::getLoansForItems)
        .compose(
            instances -> {
              var notFoundInstances = new ArrayList<>(instanceIds);
              final var rtacHoldingsList = new ArrayList<RtacHoldings>();
              for (InventoryHoldingsAndItems instance : instances) {
                RtacHoldings rtacHoldings = folioToRtacMapper.mapToRtac(instance);
                rtacHoldingsList.add(rtacHoldings);
                notFoundInstances.remove(instance.getInstanceId());
              }
              logger.info("Mapping inventory instances: {}", rtacHoldingsList.size());
              final var result = new RtacHoldingsBatch();
              if (!notFoundInstances.isEmpty()) {
                final var errors = errorMapper.mapNotFoundInstances(notFoundInstances);
                logger.info("Mapping errors: {}", errors.size());
                logger.debug("Errors: {}", errors.toString());
                result.withErrors(errors);
              } else {
                result.withErrors(null);
              }

              promise.complete(result.withHoldings(rtacHoldingsList));
              return promise.future();
            })
        .onFailure(promise::fail);
  }
}
