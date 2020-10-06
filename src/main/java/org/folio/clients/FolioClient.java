package org.folio.clients;

import static org.folio.rest.RestVerticle.OKAPI_HEADER_TENANT;
import static org.folio.rest.RestVerticle.OKAPI_HEADER_TOKEN;

import java.util.Map;
import org.folio.rest.tools.utils.TenantTool;

abstract class FolioClient {

  protected final String tenantId;
  protected final String okapiUrl;
  protected final String okapiToken;

  FolioClient(Map<String, String> okapiHeaders) {
    this.okapiUrl = okapiHeaders.getOrDefault("X-Okapi-Url", "");
    this.tenantId =
        TenantTool.calculateTenantId(okapiHeaders.get(OKAPI_HEADER_TENANT));
    this.okapiToken = okapiHeaders.getOrDefault(OKAPI_HEADER_TOKEN, "");
  }
}
