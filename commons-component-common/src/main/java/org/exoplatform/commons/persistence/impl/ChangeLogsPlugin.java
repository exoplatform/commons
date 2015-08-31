package org.exoplatform.commons.persistence.impl;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Changelog plugin to add Liquibase changelog path during the data initialization
 */
public class ChangeLogsPlugin extends BaseComponentPlugin {

  public static final String CHANGELOGS_PARAM_NAME = "changelogs";

  private List<String> changelogPaths = new ArrayList<String>();

  public ChangeLogsPlugin(InitParams initParams) {
    if(initParams != null) {
      ValuesParam changelogs = initParams.getValuesParam(CHANGELOGS_PARAM_NAME);

      if (changelogs != null) {
        changelogPaths.addAll(changelogs.getValues());
      }
    }
  }

  public List<String> getChangelogPaths() {
    return changelogPaths;
  }

  public void setChangelogPaths(List<String> changelogPaths) {
    this.changelogPaths = changelogPaths;
  }

}
