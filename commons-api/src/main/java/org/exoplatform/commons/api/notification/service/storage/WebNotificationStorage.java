package org.exoplatform.commons.api.notification.service.storage;

import java.util.List;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebFilter;

public interface WebNotificationStorage {
  /**
   * Saves information of a notification.
   * 
   * @param notification The notification to be saved.
   * @throws Exception
   */
  void save(NotificationInfo notification);

  /**
   * @param userId
   * @param notificationId the NotificationInfo's id
   * @throws Exception
   */
  void markRead(String notificationId);

  /**
   * @param userId
   * @throws Exception
   */
  void markReadAll(String userId);

  /**
   * @param notificationId
   */
  void hidePopover(String notificationId);

  /**
   * @param filter the filter to set web notifications
   * @throws Exception
   */
  List<NotificationInfo> get(WebFilter filter);

  /**
   * @param userId
   * @param notificationId the NotificationInfo's id
   * @return the status removed or not
   * @throws Exception
   */
  boolean remove(String notificationId);

  /**
   * Remove the NotificationInfo live after X days
   * @param days 
   * @return the status removed or not
   * @throws Exception
   */
  boolean remove(String userId, int days);
}
