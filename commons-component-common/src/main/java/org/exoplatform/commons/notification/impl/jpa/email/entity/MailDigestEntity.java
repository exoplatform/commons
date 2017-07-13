package org.exoplatform.commons.notification.impl.jpa.email.entity;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 07, 2017
 */
@Entity(name = "NotificationsMailDigestEntity")
@ExoEntity
@Table(name = "NTF_EMAIL_NOTIFS_DIGEST")
@NamedQueries({
    @NamedQuery(name = "commons.findDigestByNotifAndType", query = "SELECT m FROM NotificationsMailDigestEntity m " +
        "WHERE m.notification= :notifId AND m.type= :digestType"),
    @NamedQuery(name = "commons.countDigestByNotifAndType", query = "SELECT COUNT(m) FROM NotificationsMailDigestEntity m " +
        "WHERE m.notification= :notifId AND m.type= :digestType")
})
public class MailDigestEntity {
  @Id
  @Column(name = "EMAIL_NOTIF_DIGEST_ID")
  @SequenceGenerator(name="SEQ_NTF_EMAIL_DIGEST", sequenceName="SEQ_NTF_EMAIL_DIGEST")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_NTF_EMAIL_DIGEST")
  private long id;

  @ManyToOne(cascade = CascadeType.PERSIST)
  @JoinColumn(name = "EMAIL_NOTIF_ID")
  private MailNotifEntity notification;

  @Column(name = "DIGEST_TYPE")
  private String type;

  public long getId() {
    return id;
  }

  public MailNotifEntity getNotification() {
    return notification;
  }

  public MailDigestEntity setNotification(MailNotifEntity notification) {
    this.notification = notification;
    return this;
  }

  public String getType() {
    return type;
  }

  public MailDigestEntity setType(String type) {
    this.type = type;
    return this;
  }
}

