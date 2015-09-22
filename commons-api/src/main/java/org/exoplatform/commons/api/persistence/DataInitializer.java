package org.exoplatform.commons.api.persistence;

/**
 * Interface for data initialization
 */
public interface DataInitializer {
  public void initData();

  public void initData(String datasourceName);
}
