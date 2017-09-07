package org.exoplatform.commons.notification.impl.jpa.web.entity;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.exoplatform.commons.api.persistence.ExoEntity;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 07, 2017
 */
@Entity(name = "NotificationsWebNotifEntity")
@ExoEntity
@Table(name = "NTF_WEB_NOTIFS")
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
  private Calendar creationDate;

  @Column(name = "TEXT")
  @Lob
  private String text;

  @OneToMany(fetch=FetchType.EAGER, mappedBy = "webNotification")
  private Set<WebParamsEntity> parameters = new HashSet<>();

  @OneToMany(fetch=FetchType.LAZY, mappedBy = "webNotification")
  private Set<WebUsersEntity> receivers = new HashSet<>();

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

  public Calendar getCreationDate() {
    return creationDate;
  }

  public WebNotifEntity setCreationDate(Calendar creationDate) {
    this.creationDate = creationDate;
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

  public void addReceiver(WebUsersEntity receiver) {
    this.receivers.add(receiver);
  }

  public Set<WebUsersEntity> getReceivers() {
    return receivers;
  }

  public void setReceivers(Set<WebUsersEntity> receivers) {
    this.receivers = receivers;
  }
}

