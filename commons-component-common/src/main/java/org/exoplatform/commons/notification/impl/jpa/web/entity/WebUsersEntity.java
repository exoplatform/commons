package org.exoplatform.commons.notification.impl.jpa.web.entity;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;
import java.util.Date;

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
    @NamedQuery(name = "commons.findWebNotifsByUser", query = "SELECT u FROM NotificationsWebUsersEntity u " +
        "WHERE u.receiver = :userId "),
    @NamedQuery(name = "commons.findWebNotifsByUserAndRead", query = "SELECT u FROM NotificationsWebUsersEntity u " +
        "JOIN u.webNotification WebNotif " +
        "WHERE u.receiver = :userId " +
        "AND u.read = :isRead " +
        "ORDER BY WebNotif.creationDate DESC "),
    @NamedQuery(name = "commons.getNumberOnBadge", query = "SELECT COUNT(u) FROM NotificationsWebUsersEntity u " +
        "WHERE u.receiver = :userId " +
        "AND u.resetNumberOnBadge = FALSE ")
})
public class WebUsersEntity {
  @Id
  @Column(name = "WEB_NOTIFS_USERS_ID")
  @SequenceGenerator(name="SEQ_NTF_WEB_USERS", sequenceName="SEQ_NTF_WEB_USERS")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_NTF_WEB_USERS")
  private long id;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "WEB_NOTIF_ID")
  private WebNotifEntity webNotification;

  @Column(name = "RECEIVER")
  private String receiver;

  @Column(name = "UPDATE_DATE")
  private Date updateDate;

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

  public Date getUpdateDate() {
    return updateDate;
  }

  public WebUsersEntity setUpdateDate(Date updateDate) {
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
