package org.folio.models;

import java.util.List;
import org.folio.rest.jaxrs.model.SettingsItem;
import org.folio.rest.jaxrs.model.SettingsResultInfo;

public class Settings {

  private List<SettingsItem> items;
  private SettingsResultInfo resultInfo;

  public List<SettingsItem> getItems() {
    return items;
  }

  public void setItems(List<SettingsItem> items) {
    this.items = items;
  }

  public SettingsResultInfo getResultInfo() {
    return resultInfo;
  }

  public void setResultInfo(SettingsResultInfo resultInfo) {
    this.resultInfo = resultInfo;
  }
}
