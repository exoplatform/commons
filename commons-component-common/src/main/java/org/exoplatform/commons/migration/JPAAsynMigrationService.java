package org.exoplatform.commons.migration;

import javax.servlet.ServletContext;

import org.exoplatform.commons.cluster.StartableClusterAware;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;

/**
 * This service executes the migration and cleanup from JCR to JPA
 * asynchronously
 */
public class JPAAsynMigrationService implements StartableClusterAware {

  private SettingsMigration          settingsMigration;

  private MailNotificationsMigration mailNotificationsMigration;

  private WebNotificationsMigration  webNotificationsMigration;

  public JPAAsynMigrationService(SettingsMigration settingsMigration,
                                 MailNotificationsMigration mailNotificationsMigration,
                                 WebNotificationsMigration webNotificationsMigration) {
    this.mailNotificationsMigration = mailNotificationsMigration;
    this.settingsMigration = settingsMigration;
    this.webNotificationsMigration = webNotificationsMigration;
  }

  @Override
  public void start() {
    PortalContainer.addInitTask(PortalContainer.getInstance().getPortalContext(), new RootContainer.PortalContainerPostInitTask() {
      @Override
      public void execute(ServletContext context, PortalContainer portalContainer) {
        // Migrate & cleanup asynchronously
        webNotificationsMigration.migrate();
        mailNotificationsMigration.migrate();

        // Cleanup asynchronously after migration is completely finished
        settingsMigration.cleanup();
      }
    });
  }

  @Override
  public boolean isDone() {
    return false;
  }
}
