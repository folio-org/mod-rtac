package org.folio.rest.impl;

import static org.folio.rest.jaxrs.resource.RtacBatch.PostRtacBatchResponse.respond403WithTextPlain;
import static org.folio.rest.jaxrs.resource.RtacBatch.PostRtacBatchResponse.respond404WithTextPlain;
import static org.folio.rest.jaxrs.resource.RtacBatch.PostRtacBatchResponse.respond500WithTextPlain;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.clients.FolioFacade;
import org.folio.rest.jaxrs.model.RtacRequest;
import org.folio.rest.jaxrs.resource.RtacBatch;
import org.folio.rtac.rest.exceptions.HttpException;

public final class RtacBatchResourceImpl implements RtacBatch {

  private static final Logger logger = LogManager.getLogger();

  @Override
  public void postRtacBatch(
      RtacRequest entity,
      Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler,
      Context vertxContext) {

    final FolioFacade folioFacade = new FolioFacade(okapiHeaders);

    folioFacade
        .getItemAndHoldingInfo(entity.getInstanceIds())
        .onSuccess(
            result ->
                asyncResultHandler.handle(
                    Future.succeededFuture(
                        PostRtacBatchResponse.respond200WithApplicationJson(result))))
        .onFailure(t -> asyncResultHandler.handle(handleError(t)));
  }

  private Future<javax.ws.rs.core.Response> handleError(Throwable t) {
    final Future<Response> result;
    logger.error(t.getMessage(), t);
    if (t instanceof HttpException) {
      final int code = ((HttpException) t).getCode();
      final String message = t.getMessage();
      switch (code) {
        case 403:
          result = Future.succeededFuture(respond403WithTextPlain(message));
          break;
        case 404:
          result = Future.succeededFuture(respond404WithTextPlain(message));
          break;
        case 400:
          // This means that we screwed up something in the request to another
          // module. This API only takes a UUID, so a client side 400 is not
          // possible here, only server side, which the client won't be able to
          // do anything about.
        default:
          result = Future.succeededFuture(respond500WithTextPlain(message));
      }
    } else {
      result = Future.succeededFuture(respond500WithTextPlain(t.getMessage()));
    }

    return result;
  }
}
