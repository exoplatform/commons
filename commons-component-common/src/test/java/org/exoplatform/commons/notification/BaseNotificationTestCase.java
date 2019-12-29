package org.exoplatform.commons.notification;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.notification.plugin.PluginTest;
import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.component.test.*;
import org.exoplatform.container.ExoContainerContext;

public abstract class BaseNotificationTestCase extends BaseCommonsTestCase {

  protected static final String    NOTIFICATIONS = "notifications";

  protected List<String>           userIds       = new ArrayList<>();

  protected WebNotificationStorage storage;

  @Override
  protected void beforeClass() {
    super.beforeClass();
    ExoContainerContext.setCurrentContainer(getContainer());
    storage = getContainer().getComponentInstanceOfType(WebNotificationStorage.class);
  }

  protected NotificationInfo makeWebNotificationInfo(String userId) {
    NotificationInfo info = NotificationInfo.instance();
    info.key(new PluginKey(PluginTest.ID));
    info.setTitle("The title");
    info.setFrom("mary");
    info.setTo(userId);
    info.with(NotificationMessageUtils.SHOW_POPOVER_PROPERTY.getKey(), "true")
        .with(NotificationMessageUtils.READ_PORPERTY.getKey(), "false")
        .with("activityId", "TheActivityId")
        .with("accessLink", "http://fsdfsdf.com/fsdfsf");
    return info;
  }
}
