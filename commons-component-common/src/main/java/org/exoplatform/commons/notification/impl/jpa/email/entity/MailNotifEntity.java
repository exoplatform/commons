package org.exoplatform.commons.notification.impl.jpa.email.entity;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Set;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 07, 2017
 */
@Entity(name = "MailNotifEntity")
@ExoEntity
@Table(name = "NTF_EMAIL_NOTIFS")
@NamedQueries({
    @NamedQuery(name = "commons.getNotifsByPluginAndDay", query = "SELECT m FROM MailNotifEntity m " +
        "WHERE DAY(m.creationDate)= :day " +
        "AND MONTH(m.creationDate)= :month " +
        "AND YEAR(m.creationDate)= :year " +
        "AND m.type= :pluginId " +
        "ORDER BY m.order ASC, m.creationDate DESC"),
    @NamedQuery(name = "commons.getNotifsByPluginAndWeek", query = "SELECT m FROM MailNotifEntity m " +
        "WHERE m.creationDate> :date " +
        "AND m.type= :pluginId " +
        "ORDER BY m.order ASC, m.creationDate DESC")
})
public class MailNotifEntity {
  @Id
  @Column(name = "NTF_EMAIL_NOTIF_ID")
  @SequenceGenerator(name="SEQ_NTF_EMAIL_NOTIF", sequenceName="SEQ_NTF_EMAIL_NOTIF")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_NTF_EMAIL_NOTIF")
  private long id;

  @Column(name = "NTF_SENDER")
  private String sender;

  @Column(name = "NTF_TYPE")
  private String type;

  @Column(name = "NTF_CREATION_DATE")
  private Calendar creationDate;

  @Column(name = "NTF_ORDER")
  private int order;

  @OneToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "mailNotification")
  private Set<MailParamsEntity> ownerParameter;

  @OneToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "notification")
  private Set<MailDigestEntity> digestSent;

  public long getId() {
    return id;
  }

  public String getSender() {
    return sender;
  }

  public MailNotifEntity setSender(String sender) {
    this.sender = sender;
    return this;
  }

  public String getType() {
    return type;
  }

  public MailNotifEntity setType(String type) {
    this.type = type;
    return this;
  }

  public Calendar getCreationDate() {
    return creationDate;
  }

  public MailNotifEntity setCreationDate(Calendar creationDate) {
    this.creationDate = creationDate;
    return this;
  }

  public int getOrder() {
    return order;
  }

  public MailNotifEntity setOrder(int order) {
    this.order = order;
    return this;
  }

  public Set<MailParamsEntity> getArrayOwnerParameter() {
    return ownerParameter;
  }

  public void setArrayOwnerParameter(Set<MailParamsEntity> ownerParameter) {
    this.ownerParameter = ownerParameter;
  }

  public Set<MailDigestEntity> getMailDigestSent() {
    return digestSent;
  }

  public void addMailDigestSent(MailDigestEntity digestSent) {
    this.digestSent.add(digestSent);
  }

}
