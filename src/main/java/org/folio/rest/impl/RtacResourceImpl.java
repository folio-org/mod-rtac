package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.util.Map;
import org.folio.clients.FolioFacade;
import org.folio.rest.jaxrs.model.RtacRequest;
import org.folio.rest.jaxrs.resource.Rtac;
import org.folio.rtac.rest.exceptions.HttpException;

public final class RtacResourceImpl implements Rtac {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public void postRtacBatch(
      RtacRequest entity,
      Map<String, String> okapiHeaders,
      Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler,
      Context vertxContext) {

    final FolioFacade folioFacade = new FolioFacade(okapiHeaders);

    folioFacade
        .getItemAndHoldingInfo(entity.getInstanceIds())
        .onSuccess(
            result ->
                asyncResultHandler.handle(
                    Future.succeededFuture(
                        Rtac.PostRtacBatchResponse.respond200WithApplicationJson(result))))
        .onFailure(t -> asyncResultHandler.handle(handleError(t)));
  }

  private Future<javax.ws.rs.core.Response> handleError(Throwable t) {
    final Future<javax.ws.rs.core.Response> result;
    logger.error(t.getMessage(), t);
    if (t instanceof HttpException) {
      final int code = ((HttpException) t).getCode();
      final String message = t.getMessage();
      switch (code) {
        case 400:
          // This means that we screwed up something in the request to another
          // module. This API only takes a UUID, so a client side 400 is not
          // possible here, only server side, which the client won't be able to
          // do anything about.
          result =
              Future.succeededFuture(Rtac.PostRtacBatchResponse.respond500WithTextPlain(message));
          break;
        case 403:
          result =
              Future.succeededFuture(Rtac.PostRtacBatchResponse.respond403WithTextPlain(message));
          break;
        case 404:
          result =
              Future.succeededFuture(Rtac.PostRtacBatchResponse.respond404WithTextPlain(message));
          break;
        default:
          result =
              Future.succeededFuture(Rtac.PostRtacBatchResponse.respond500WithTextPlain(message));
      }
    } else {
      result =
          Future.succeededFuture(
              Rtac.PostRtacBatchResponse.respond500WithTextPlain(t.getMessage()));
    }

    return result;
  }
}
