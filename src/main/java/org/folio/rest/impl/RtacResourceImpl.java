package org.folio.rest.impl;

import java.util.Map;

import org.folio.clients.FolioFacade;
import org.folio.rest.jaxrs.model.RtacRequest;
import org.folio.rest.jaxrs.resource.Rtac;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public final class RtacResourceImpl implements Rtac {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public void postRtacBatch(RtacRequest entity, Map<String, String> okapiHeaders, Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler, Context vertxContext) {

    final FolioFacade folioFacade = new FolioFacade(okapiHeaders);

    folioFacade.getItemAndHoldingInfo(entity.getInstanceIds())
      .onSuccess(result -> asyncResultHandler.handle(Future.succeededFuture(
        Rtac.PostRtacBatchResponse.respond200WithApplicationJson(result))))
      .onFailure(t -> {
        logger.error(t.getMessage(), t);
        asyncResultHandler.handle(Future.failedFuture(t));
      });
  }
}

