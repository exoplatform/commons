package org.exoplatform.commons.notification.storage;

import org.exoplatform.commons.notification.impl.jpa.cache.JPACachedWebNotificationStorage;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;

@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/test-root-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/commons-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/test-portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/test-jpa-cache-configuration.xml") })
public class JPACachedWebNotificationStorageTest extends CachedWebNotificationStorageTest {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    cachedStorage = getService(JPACachedWebNotificationStorage.class);
  }
}
