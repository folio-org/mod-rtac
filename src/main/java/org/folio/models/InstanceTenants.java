package org.folio.models;

import java.util.List;


public class InstanceTenants {
  private String instanceId;
  private List<String> tenantIds;


  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public List<String> getTenantIds() {
    return tenantIds;
  }

  public void setTenantIds(List<String> tenantIds) {
    this.tenantIds = tenantIds;
  }
}
