package org.exoplatform.commons.notification.impl.jpa;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailParamEntity;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailQueueEntity;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebNotifEntity;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebParamsEntity;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebUsersEntity;

/**
 * This is an utility class used for entity conversion from JPA entities to equivalent JCR ones
 */
public class EntityConverter {
  public static Map<String, String> convertParamsEntityToParams(Collection<MailParamEntity> paramsEntityList) {
    Map<String, String> params = new HashMap<String, String>();
    for (MailParamEntity paramsEntity : paramsEntityList) {
      params.put(paramsEntity.getName(), paramsEntity.getValue());
    }
    return params;
  }

  public static MessageInfo convertQueueEntityToMessageInfo(MailQueueEntity mailQueueEntity) {
    MessageInfo messageInfo = new MessageInfo();
    messageInfo.setId(String.valueOf(mailQueueEntity.getId()));
    messageInfo.pluginId(mailQueueEntity.getType());
    messageInfo.from(mailQueueEntity.getFrom());
    messageInfo.to(mailQueueEntity.getTo());
    messageInfo.subject(mailQueueEntity.getSubject());
    messageInfo.body(mailQueueEntity.getBody());
    messageInfo.footer(mailQueueEntity.getFooter());
    messageInfo.setCreatedTime(mailQueueEntity.getCreationDate().getTimeInMillis());
    return messageInfo;
  }

  /**
   * Convert user web notification entity to notification DTO
   * NOTE: The annotation {@link ExoTransactional} is used to
   * allow fetching parameters lazily
   * 
   * @param webUsersEntity user web notification
   * @return notification DTO
   */
  public static NotificationInfo convertWebNotifEntityToNotificationInfo(WebUsersEntity webUsersEntity) {
    NotificationInfo notificationInfo = new NotificationInfo();
    WebNotifEntity notification = webUsersEntity.getNotification();

    notificationInfo.setLastModifiedDate(webUsersEntity.getUpdateDate());

    Set<WebParamsEntity> parameters = notification.getParameters();
    Map<String, String> ownerParameters =
                                        parameters.stream()
                                                  .collect(Collectors.toMap(WebParamsEntity::getName, WebParamsEntity::getValue));
    ownerParameters.put(NotificationMessageUtils.READ_PORPERTY.getKey(), String.valueOf(webUsersEntity.isRead()));
    notificationInfo.setOwnerParameter(ownerParameters);

    notificationInfo.key(new PluginKey(notification.getType()));
    notificationInfo.setTitle(notification.getText());
    notificationInfo.setFrom(notification.getSender());
    notificationInfo.to(webUsersEntity.getReceiver());
    notificationInfo.setRead(webUsersEntity.isRead());
    notificationInfo.setOnPopOver(webUsersEntity.isShowPopover());
    notificationInfo.setResetOnBadge(webUsersEntity.isResetNumberOnBadge());
    notificationInfo.setDateCreated(notification.getCreationDate());
    notificationInfo.setId(String.valueOf(webUsersEntity.getId()));
    return notificationInfo;
  }
}
