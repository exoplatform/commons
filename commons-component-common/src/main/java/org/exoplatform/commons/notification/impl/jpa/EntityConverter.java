package org.exoplatform.commons.notification.impl.jpa;

import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailParamsEntity;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailQueueEntity;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebNotifEntity;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebParamsEntity;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebUsersEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is an utility class used for entity conversion from JPA entities to equivalent JCR ones
 */
public class EntityConverter {
  private static final Log LOG = ExoLogger.getLogger(EntityConverter.class);


  public static Map<String, String> convertParamsEntityToParams(Set<MailParamsEntity> paramsEntityList) {
    Map<String, String> params = new HashMap<String, String>();
    for (MailParamsEntity paramsEntity : paramsEntityList) {
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
    messageInfo.setCreatedTime(mailQueueEntity.getCreationDate().getTime());
    return messageInfo;
  }

  public static NotificationInfo convertWebNotifEntityToNotificationInfo(WebNotifEntity webNotifEntity) {
    NotificationInfo notificationInfo = new NotificationInfo();
    Calendar cal = Calendar.getInstance();
    cal.setTime(webNotifEntity.getCreationDate());

    WebUsersEntity webUsersEntity = webNotifEntity.getReceiver();
    notificationInfo.setLastModifiedDate(webUsersEntity.getUpdateDate().getTime());

    Map<String, String> ownerParameters = new HashMap<String, String>();
    for (WebParamsEntity parameter : webNotifEntity.getParameters()) {
      ownerParameters.put(parameter.getName(), parameter.getValue());
    }
    notificationInfo.setOwnerParameter(ownerParameters);

    notificationInfo.key(new PluginKey(webNotifEntity.getType()));
    notificationInfo.setTitle(webNotifEntity.getText());
    notificationInfo.setFrom(webNotifEntity.getSender());
    notificationInfo.to(webNotifEntity.getOwner());
    notificationInfo.setDateCreated(cal);

    notificationInfo.setId(String.valueOf(webNotifEntity.getId()));
    return notificationInfo;
  }
}
