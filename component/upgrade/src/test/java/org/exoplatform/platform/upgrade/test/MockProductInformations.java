package org.exoplatform.platform.upgrade.test;

import org.exoplatform.component.product.ProductInformations;

public class MockProductInformations implements ProductInformations{

  private static final String VERSION = "2.0";

  @Override
  public String getVersion() {
    return VERSION;
  }

  @Override
  public String getBuildNumber() {
    return null;
  }

  @Override
  public String getRevision() {
    return null;
  }

}
