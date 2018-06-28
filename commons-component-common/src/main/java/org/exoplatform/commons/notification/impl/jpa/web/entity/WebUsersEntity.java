package org.exoplatform.commons.notification.impl.jpa.web.entity;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;
import java.util.Calendar;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 07, 2017
 */
@Entity(name = "NotificationsWebUsersEntity")
@ExoEntity
@Table(name = "NTF_WEB_NOTIFS_USERS")
@NamedQueries({
    @NamedQuery(name = "NotificationsWebUsersEntity.getNumberOnBadge", query = "SELECT COUNT(u) FROM NotificationsWebUsersEntity u " +
        "WHERE u.receiver = :userId " +
        "AND u.resetNumberOnBadge = FALSE "),
    @NamedQuery(name = "NotificationsWebUsersEntity.findNotifsWithBadge", query = "SELECT u FROM NotificationsWebUsersEntity u " +
        "WHERE u.receiver = :userId " +
        "AND u.resetNumberOnBadge = FALSE "),
    @NamedQuery(name = "NotificationsWebUsersEntity.markWebNotifsAsReadByUser", query = "UPDATE NotificationsWebUsersEntity u " +
        "SET u.read = TRUE " +
        "WHERE u.receiver = :userId " +
        "AND u.read = FALSE "),
    @NamedQuery(name = "NotificationsWebUsersEntity.findWebNotifsByPluginFilter", query = "SELECT u FROM NotificationsWebUsersEntity u " +
        "JOIN FETCH u.webNotification w " +
        "WHERE u.webNotification.type= :pluginId " +
        "AND u.receiver = :userId " +
        "AND u.showPopover= :isOnPopover " +
        "ORDER BY u.updateDate DESC "),
    @NamedQuery(name = "NotificationsWebUsersEntity.findWebNotifsByUserFilter", query = "SELECT u FROM NotificationsWebUsersEntity u " +
        "JOIN FETCH u.webNotification w " +
        "WHERE u.receiver = :userId " +
        "ORDER BY u.updateDate DESC "),
    @NamedQuery(name = "NotificationsWebUsersEntity.findWebNotifsByPopoverFilter", query = "SELECT u FROM NotificationsWebUsersEntity u " +
        "JOIN FETCH u.webNotification w " +
        "WHERE u.receiver = :userId " +
        "AND u.showPopover= :isOnPopover " +
        "ORDER BY u.updateDate DESC "),
    @NamedQuery(name = "NotificationsWebUsersEntity.findUnreadNotification", query = "SELECT u FROM NotificationsWebUsersEntity u " +
        "JOIN FETCH u.webNotification w " +
        "JOIN u.webNotification.parameters  p " +
        "WHERE w.type= :pluginId " +
        "AND p.name = :paramName " +
        "AND p.value LIKE :paramValue " +
        "AND u.receiver = :userId " +
        "AND u.read = FALSE " +
        "ORDER BY u.updateDate DESC "),
    @NamedQuery(name = "NotificationsWebUsersEntity.findWebNotifsOfUserByLastUpdatedDate", query = "SELECT u FROM NotificationsWebUsersEntity u " +
        "WHERE u.receiver = :userId " +
        "AND u.updateDate < :calendar "),
    @NamedQuery(name = "NotificationsWebUsersEntity.findWebNotifsByLastUpdatedDate", query = "SELECT u FROM NotificationsWebUsersEntity u " +
        "JOIN FETCH u.webNotification w " +
        "WHERE u.updateDate < :calendar "),
    @NamedQuery(name = "NotificationsWebUsersEntity.findNotificationsByTypeAndParams", query = "SELECT distinct(u) FROM NotificationsWebUsersEntity u " +
        "JOIN u.webNotification w " +
        "JOIN u.webNotification.parameters p " +
        "WHERE w.type= :pluginType " +
        "AND p.name= :paramName " +
        "AND p.value= :paramValue " +
        "AND u.receiver= :receiver ")
})
public class WebUsersEntity {
  @Id
  @Column(name = "WEB_NOTIFS_USERS_ID")
  @SequenceGenerator(name="SEQ_NTF_WEB_USERS", sequenceName="SEQ_NTF_WEB_USERS")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_NTF_WEB_USERS")
  private long id;

  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name = "WEB_NOTIF_ID")
  private WebNotifEntity webNotification;

  @Column(name = "RECEIVER")
  private String receiver;

  @Column(name = "UPDATE_DATE")
  private Calendar updateDate;

  @Column(name = "IS_READ")
  private boolean read;

  @Column(name = "SHOW_POPOVER")
  private boolean showPopover;

  @Column(name = "RESET_NUMBER_BADGE")
  private boolean resetNumberOnBadge;

  public long getId() {
    return id;
  }

  public WebNotifEntity getNotification() {
    return webNotification;
  }

  public WebUsersEntity setNotification(WebNotifEntity webNotification) {
    this.webNotification = webNotification;
    webNotification.addReceiver(this);
    return this;
  }
  public String getReceiver() {
    return receiver;
  }

  public WebUsersEntity setReceiver(String receiver) {
    this.receiver = receiver;
    return this;
  }

  public Calendar getUpdateDate() {
    return updateDate;
  }

  public WebUsersEntity setUpdateDate(Calendar updateDate) {
    this.updateDate = updateDate;
    return this;
  }

  public boolean isRead() {
    return read;
  }

  public WebUsersEntity setRead(boolean read) {
    this.read = read;
    return this;
  }

  public boolean isShowPopover() {
    return showPopover;
  }

  public WebUsersEntity setShowPopover(boolean showPopover) {
    this.showPopover = showPopover;
    return this;
  }

  public boolean isResetNumberOnBadge() {
    return resetNumberOnBadge;
  }

  public void setResetNumberOnBadge(boolean resetNumberOnBadge) {
    this.resetNumberOnBadge = resetNumberOnBadge;
  }
}
