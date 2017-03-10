package org.exoplatform.commons.notification.impl.jpa.email.entity;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 07, 2017
 */
@Entity(name = "MailDigestEntity")
@ExoEntity
@Table(name = "NTF_EMAIL_NOTIFS_DIGEST")
@NamedQueries({
    @NamedQuery(name = "commons.findDigestByNotifAndType", query = "SELECT m FROM MailDigestEntity m " +
        "WHERE m.notification= :notifId AND m.type= :digestType")
})
public class MailDigestEntity {
  @Id
  @Column(name = "NTF_EMAIL_NOTIF_DIGEST_ID")
  @SequenceGenerator(name="SEQ_NTF_EMAIL_DIGEST", sequenceName="SEQ_NTF_EMAIL_DIGEST")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_NTF_EMAIL_DIGEST")
  private long id;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "NTF_EMAIL_NOTIF_ID")
  private MailNotifEntity notification;

  @Column(name = "NTF_DIGEST_TYPE")
  private String type;

  @Column(name = "NTF_DIGEST_TO_USER")
  private String user;

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

  public String getUser() {
    return user;
  }

  public MailDigestEntity setUser(String user) {
    this.user = user;
    return this;
  }
}

