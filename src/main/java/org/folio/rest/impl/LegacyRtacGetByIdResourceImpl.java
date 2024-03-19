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
import org.folio.rest.jaxrs.resource.Rtac;

public final class LegacyRtacGetByIdResourceImpl implements Rtac {

  private static final Logger logger = LogManager.getLogger();

  @Override
  @Deprecated
  public void getRtacById(
      String id,
      Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler,
      Context vertxContext) {

    logger.warn(
        "Deprecated API called. It will be removed soon! Please migrate to POST /rtac/batch");
    logger.info("Legacy Get By Id called: {}", id);
    final FolioFacade folioFacade = new FolioFacade(okapiHeaders);

    folioFacade
        .getItemAndHoldingInfo(id)
        .onSuccess(
            result ->
                asyncResultHandler.handle(
                    Future.succeededFuture(
                        Rtac.GetRtacByIdResponse.respond200WithApplicationJson(result))))
        .onFailure(
            t -> {
              logger.error("Rtac failed", t);
              asyncResultHandler.handle(ErrorMapper.handleError(t));
            });
  }
}
