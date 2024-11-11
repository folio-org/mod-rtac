package org.folio.clients;

import static org.folio.rest.RestVerticle.OKAPI_HEADER_TENANT;
import static org.folio.rest.RestVerticle.OKAPI_HEADER_TOKEN;
import static org.folio.rest.RestVerticle.OKAPI_REQUESTID_HEADER;

import io.vertx.ext.web.client.WebClient;
import java.util.Map;
import org.folio.rest.tools.utils.TenantTool;

abstract class FolioClient {

  protected final String tenantId;
  protected final String okapiUrl;
  protected final String okapiToken;
  protected final String requestId;

  protected final WebClient webClient;

  FolioClient(Map<String, String> okapiHeaders, WebClient webClient) {
    this.okapiUrl = okapiHeaders.getOrDefault("X-Okapi-Url", "");
    this.tenantId =
        TenantTool.calculateTenantId(okapiHeaders.get(OKAPI_HEADER_TENANT));
    this.requestId = okapiHeaders.getOrDefault(OKAPI_REQUESTID_HEADER, "");
    this.okapiToken = okapiHeaders.getOrDefault(OKAPI_HEADER_TOKEN, "");
    this.webClient = webClient;
  }
}
