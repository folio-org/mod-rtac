package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.clients.FolioFacade;
import org.folio.mappers.ErrorMapper;
import org.folio.rest.jaxrs.model.RtacRequest;
import org.folio.rest.jaxrs.resource.RtacBatch;

public final class RtacBatchResourceImpl implements RtacBatch {

  private static final Logger logger = LogManager.getLogger();

  @Override
  public void postRtacBatch(
      RtacRequest entity,
      Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler,
      Context vertxContext) {

    logger.info("Batch API called: {}", entity);

    final FolioFacade folioFacade = new FolioFacade(okapiHeaders);

    folioFacade
        .getItemAndHoldingInfo(entity)
        .onSuccess(
            result ->
                asyncResultHandler.handle(
                    Future.succeededFuture(
                        PostRtacBatchResponse.respond200WithApplicationJson(result))))
        .onFailure(
            t -> {
              logger.error("Rtac failed", t);
              asyncResultHandler.handle(ErrorMapper.handleError(t));
            });
  }
}
