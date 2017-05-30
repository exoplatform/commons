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
public class WebUsersEntity {
  @Id
  @Column(name = "WEB_NOTIFS_USERS_ID")
  @SequenceGenerator(name="SEQ_NTF_WEB_USERS", sequenceName="SEQ_NTF_WEB_USERS")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_NTF_WEB_USERS")
  private long id;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "WEB_NOTIF_ID")
  private WebNotifEntity webNotifications;

  @Column(name = "RECEIVER")
  private String receiver;

  @Column(name = "UPDATE_DATE")
  private Date updateDate;

  @Column(name = "IS_READ")
  private boolean read;

  @Column(name = "SHOW_POPOVER")
  private boolean showPopover;

  public long getId() {
    return id;
  }

  public WebNotifEntity getNotification() {
    return webNotifications;
  }

  public WebUsersEntity setNotification(WebNotifEntity webNotification) {
    this.webNotifications = webNotification;
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
}
