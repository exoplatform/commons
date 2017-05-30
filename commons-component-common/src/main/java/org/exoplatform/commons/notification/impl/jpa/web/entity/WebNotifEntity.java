package org.exoplatform.commons.notification.impl.jpa.web.entity;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 07, 2017
 */
@Entity(name = "NotificationsWebNotifEntity")
@ExoEntity
@Table(name = "NTF_WEB_NOTIFS")
@NamedQueries({
    @NamedQuery(name = "commons.findWebNotifsByPluginFilter", query = "SELECT w FROM NotificationsWebNotifEntity w " +
        "JOIN w.receiver  Receiver " +
        "WHERE w.type= :pluginId " +
        "AND Receiver.receiver = :userId " +
        "AND Receiver.showPopover= :isOnPopover " +
        "ORDER BY Receiver.updateDate DESC "),
    @NamedQuery(name = "commons.findWebNotifsByUserFilter", query = "SELECT w FROM NotificationsWebNotifEntity w " +
        "JOIN w.receiver  Receiver " +
        "WHERE Receiver.receiver = :userId " +
        "ORDER BY Receiver.updateDate DESC "),
    @NamedQuery(name = "commons.findWebNotifsByPopoverFilter", query = "SELECT w FROM NotificationsWebNotifEntity w " +
        "JOIN w.receiver  Receiver " +
        "WHERE Receiver.receiver = :userId " +
        "AND Receiver.showPopover= :isOnPopover " +
        "ORDER BY Receiver.updateDate DESC "),
    @NamedQuery(name = "commons.findNewWebNotifsByUser", query = "SELECT w FROM NotificationsWebNotifEntity w " +
        "JOIN w.receiver  Receiver " +
        "WHERE Receiver.receiver = :userId " +
        "AND Receiver.read = :isRead " +
        "ORDER BY Receiver.updateDate DESC "),
    @NamedQuery(name = "commons.findWebNotifsByLastUpdatedDate", query = "SELECT w FROM NotificationsWebNotifEntity w " +
        "JOIN w.receiver  Receiver " +
        "WHERE Receiver.updateDate < :delayTime "),
    @NamedQuery(name = "commons.findWebNotifsOfUserByLastUpdatedDate", query = "SELECT w FROM NotificationsWebNotifEntity w " +
        "JOIN w.receiver  Receiver " +
        "WHERE Receiver.receiver = :userId " +
        "AND Receiver.updateDate < :calendar "),
    @NamedQuery(name = "commons.findUnreadNotification", query = "SELECT w FROM NotificationsWebNotifEntity w " +
        "JOIN w.parameters  Parameters " +
        "JOIN w.receiver  Receiver " +
        "WHERE w.type= :pluginId " +
        "AND Parameters.name = :activityIdParamName " +
        "AND Parameters.value = :activityId " +
        "AND Receiver.receiver = :owner " +
        "AND Receiver.read = false " +
        "AND Receiver.updateDate > :calendar "),
    @NamedQuery(name = "commons.findWebNotifsOfUserByParam", query = "SELECT w FROM NotificationsWebNotifEntity w " +
        "JOIN w.parameters  Parameters " +
        "JOIN w.receiver  Receiver " +
        "WHERE w.type= :pluginId " +
        "AND Parameters.name = :paramName " +
        "AND Parameters.value LIKE :paramValue " +
        "AND Receiver.receiver = :owner ")
})
public class WebNotifEntity {
  @Id
  @Column(name = "WEB_NOTIF_ID")
  @SequenceGenerator(name="SEQ_NTF_WEB_NOTIFS", sequenceName="SEQ_NTF_WEB_NOTIFS")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_NTF_WEB_NOTIFS")
  private long id;

  @Column(name = "SENDER")
  private String sender;

  @Column(name = "TYPE")
  private String type;

  @Column(name = "CREATION_DATE")
  private Date creationDate;

  @Column(name = "OWNER")
  private String owner;

  @Column(name = "TEXT")
  private String text;

  @OneToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "webNotification")
  private Set<WebParamsEntity> parameters;

  @OneToOne(cascade = CascadeType.ALL, mappedBy = "webNotifications")
  private WebUsersEntity receiver;

  public long getId() {
    return id;
  }

  public String getSender() {
    return sender;
  }

  public WebNotifEntity setSender(String sender) {
    this.sender = sender;
    return this;
  }

  public String getType() {
    return type;
  }

  public WebNotifEntity setType(String type) {
    this.type = type;
    return this;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public WebNotifEntity setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
    return this;
  }

  public String getOwner() {
    return owner;
  }

  public WebNotifEntity setOwner(String owner) {
    this.owner = owner;
    return this;
  }

  public String getText() {
    return text;
  }

  public WebNotifEntity setText(String text) {
    this.text = text;
    return this;
  }

  public Set<WebParamsEntity> getParameters() {
    return parameters;
  }

  public void setParameters(Set<WebParamsEntity> parameters) {
    this.parameters = parameters;
  }

  public void addParameter(WebParamsEntity parameter) {
    this.parameters.add(parameter);
  }

  public WebUsersEntity getReceiver() {
    return receiver;
  }

  public void setReceiver(WebUsersEntity receiver) {
    this.receiver = receiver;
  }
}

