package org.folio.mappers;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.folio.rest.jaxrs.resource.RtacBatch.PostRtacBatchResponse.respond403WithTextPlain;
import static org.folio.rest.jaxrs.resource.RtacBatch.PostRtacBatchResponse.respond404WithTextPlain;
import static org.folio.rest.jaxrs.resource.RtacBatch.PostRtacBatchResponse.respond500WithTextPlain;

import io.vertx.core.Future;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import org.folio.rest.jaxrs.model.Error;
import org.folio.rtac.rest.exceptions.HttpException;

public class ErrorMapper {

  private static final String INSTANCE_NOT_FOUND_MESSAGE = "Instance %s can not be retrieved";
  private static final String HOLDINGS_NOT_FOUND_MESSAGE = "Holdings not found for instance %s";

  /**
   * RTac mapper class.
   *
   * @param ids - not found instanceIds
   */
  public List<Error> mapInstanceNotFound(List<String> ids) {
    return ids
        .stream()
        .map(id -> createErrorMessage(id, INSTANCE_NOT_FOUND_MESSAGE))
        .collect(Collectors.toList());
  }
  
  /**
   * RTac mapper class.
   *
   * @param ids - not found instanceIds
   */
  public List<Error> mapHoldingsNotFound(List<String> ids) {
    return ids
        .stream()
        .map(id -> createErrorMessage(id, HOLDINGS_NOT_FOUND_MESSAGE))
        .collect(Collectors.toList());
  }

  private Error createErrorMessage(String instanceId, String message) {
    final var error = new Error();
    error.withCode(String.valueOf(NOT_FOUND.getStatusCode()));
    return error.withMessage(String.format(message, instanceId)).withParameters(null);
  }

  /**
   * Maps an exception to RTAC specific response.
   *
   * @param t - exception
   */
  public static Future<Response> handleError(Throwable t) {
    final Future<Response> result;
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
