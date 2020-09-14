package org.folio.clients;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.util.Map;
import java.util.stream.Collectors;
import org.folio.mappers.FolioToRtacMapper;
import org.folio.rest.jaxrs.model.RtacHoldingsBatch;
import org.folio.rest.jaxrs.model.RtacRequest;

public class FolioFacade {

  private final InventoryClient inventoryClient;
  private final CirculationClient circulationClient;
  private final Logger logger = LoggerFactory.getLogger(getClass());


  public FolioFacade(Map<String, String> okapiHeaders) {
    this.inventoryClient = new InventoryClient(okapiHeaders);
    this.circulationClient = new CirculationClient(okapiHeaders);
  }

  /**
   * Returns batch info for instances items and holdings.
   *
   * @param rtacRequest - request params
   * @return items and holdings for instances
   */
  public Future<RtacHoldingsBatch> getItemAndHoldingInfo(RtacRequest rtacRequest) {
    Promise<RtacHoldingsBatch> promise = Promise.promise();
    final var folioToRtacMapper = new FolioToRtacMapper(rtacRequest.getFullPeriodicals());
    return inventoryClient.getItemAndHoldingInfo(rtacRequest.getInstanceIds())
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
