package org.exoplatform.commons.notification.impl.jpa.email.entity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.persistence.ExoEntity;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Mar
 * 07, 2017
 */
@Entity(name = "NotificationsMailNotifEntity")
@ExoEntity
@Table(name = "NTF_EMAIL_NOTIFS")
@NamedQueries({
    @NamedQuery(name = "NotificationsMailNotifEntity.getNotifsByPluginAndDay", query = "SELECT distinct(m) FROM NotificationsMailNotifEntity m " +
        "JOIN m.digests d " +
        "LEFT OUTER JOIN FETCH m.parameters p " +
        "WHERE DAY(m.creationDate)= :day " +
        "AND MONTH(m.creationDate)= :month " +
        "AND YEAR(m.creationDate)= :year " +
        "AND m.type= :pluginId " +
        "AND d.type= 'daily' " +
        "ORDER BY m.order ASC, m.creationDate DESC"),
    @NamedQuery(name = "NotificationsMailNotifEntity.getNotifsByPluginAndWeek", query = "SELECT distinct(m) FROM NotificationsMailNotifEntity m " +
        "JOIN m.digests d " +
        "LEFT OUTER JOIN FETCH m.parameters p " +
        "WHERE m.creationDate> :date " +
        "AND m.type= :pluginId " +
        "AND d.type= 'weekly' " +
        "ORDER BY m.order ASC, m.creationDate DESC"),
    @NamedQuery(name = "NotificationsMailNotifEntity.getAllNotificationsWithoutDigests", query = "SELECT distinct(m) FROM NotificationsMailNotifEntity m " +
        "WHERE m.digests IS EMPTY")
})
public class MailNotifEntity {
  @Id
  @Column(name = "EMAIL_NOTIF_ID")
  @SequenceGenerator(name = "SEQ_NTF_EMAIL_NOTIF", sequenceName = "SEQ_NTF_EMAIL_NOTIF")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_NTF_EMAIL_NOTIF")
  private long                  id;

  @Column(name = "SENDER")
  private String                sender;

  @Column(name = "TYPE")
  private String                type;

  @Column(name = "CREATION_DATE")
  private Calendar              creationDate;

  @Column(name = "SENDING_ORDER")
  private int                   order;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "notification")
  private List<MailDigestEntity> digests = new ArrayList<>();

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "notification")
  private List<MailParamEntity>  parameters = new ArrayList<>();

  public long getId() {
    return id;
  }

  public String getSender() {
    return sender;
  }

  public MailNotifEntity setSender(String sender) {
    if (StringUtils.isBlank(sender)) {
      this.sender = null;
    } else {
      this.sender = sender;
    }
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

  public List<MailParamEntity> getParameters() {
    return parameters;
  }

  public List<MailDigestEntity> getDigests() {
    return digests;
  }
}
