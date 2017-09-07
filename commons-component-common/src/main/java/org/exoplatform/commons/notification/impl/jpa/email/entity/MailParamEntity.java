/*
 * Copyright (C) 2003-2017 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.notification.impl.jpa.email.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.exoplatform.commons.api.persistence.ExoEntity;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 07, 2017
 */
@Entity(name = "NotificationsMailParamsEntity")
@ExoEntity
@Table(name = "NTF_EMAIL_NOTIFS_PARAMS")
@NamedQueries({
  @NamedQuery(name = "NotificationsMailParamsEntity.deleteParamsOfNotifications", query = "DELETE FROM NotificationsMailParamsEntity m " +
      "WHERE m.notification.id IN ( :notifications ) ")
})
public class MailParamEntity {
  @Id
  @Column(name = "EMAIL_NOTIF_PARAMS_ID")
  @SequenceGenerator(name="SEQ_NTF_EMAIL_PARAMS", sequenceName="SEQ_NTF_EMAIL_PARAMS")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_NTF_EMAIL_PARAMS")
  private long id;

  @Column(name = "PARAM_NAME")
  private String name;

  @Column(name = "PARAM_VALUE")
  @Lob
  private String value;

  @ManyToOne
  @JoinColumn(name = "EMAIL_NOTIF_ID")
  private MailNotifEntity notification;

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public MailParamEntity setName(String name) {
    this.name = name;
    return this;
  }

  public String getValue() {
    return value;
  }

  public MailParamEntity setValue(String value) {
    this.value = value;
    return this;
  }

  public MailNotifEntity getNotification() {
    return notification;
  }

  public MailParamEntity setNotification(MailNotifEntity notification) {
    this.notification = notification;
    return this;
  }
}
