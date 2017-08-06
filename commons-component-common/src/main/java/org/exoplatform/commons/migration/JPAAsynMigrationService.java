package org.exoplatform.commons.migration;

import org.exoplatform.commons.cluster.StartableClusterAware;

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
    // Cleanup asynchronously
    webNotificationsMigration.migrate();
    mailNotificationsMigration.migrate();

    // Cleanup asynchronously after migration is completely finished
    settingsMigration.cleanup();
    webNotificationsMigration.cleanup();
    mailNotificationsMigration.cleanup();
  }

  @Override
  public boolean isDone() {
    return false;
  }
}
