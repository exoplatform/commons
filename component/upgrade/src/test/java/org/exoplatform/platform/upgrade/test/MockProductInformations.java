package org.exoplatform.platform.upgrade.test;

import org.exoplatform.component.product.ProductInformations;

public class MockProductInformations implements ProductInformations{

  private static final String VERSION = "2.0";

  public String getVersion() {
    return VERSION;
  }

  public String getBuildNumber() {
    return null;
  }

  public String getRevision() {
    return null;
  }

}
