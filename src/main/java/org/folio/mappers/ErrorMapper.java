package org.folio.mappers;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.util.List;
import java.util.stream.Collectors;
import org.folio.rest.jaxrs.model.Error;

public class ErrorMapper {

  private static final String NOT_FOUND_MESSAGE = "Instance %s can not be retrieved";

  /**
   * RTac mapper class.
   *
   * @param ids - not found instanceIds
   */
  public List<Error> mapNotFoundInstances(List<String> ids) {
    return ids.stream().map(this::createErrorMessage).collect(Collectors.toList());
  }

  private Error createErrorMessage(String str) {
    final var error = new Error();
    error.withCode(String.valueOf(NOT_FOUND.getStatusCode()));
    return error.withMessage(String.format(NOT_FOUND_MESSAGE, str)).withParameters(null);
  }
}
