/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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

package org.exoplatform.commons.notification.impl.service.storage.cache.model;

import com.google.common.base.Objects;
import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebNotifInfoData implements Serializable {
  private static final long serialVersionUID = 1L;

  private String              id;
  private PluginKey           key;                                           
  private String              from           = "";
  private String              to;
  private int                 order;
  private Map<String, String> ownerParameter = new HashMap<String, String>();
  private List<String>        sendToUserIds  = new ArrayList<String>();
  private String[]            sendToDaily;
  private String[]            sendToWeekly;
  private long                lastModifiedDate;
  private String              title = "";
  private ChannelKey          channelKey;
  
  public WebNotifInfoData(NotificationInfo notificationInfo) {
    this.id = notificationInfo.getId();
    this.key = notificationInfo.getKey();                                          
    this.from = notificationInfo.getFrom();
    this.to = notificationInfo.getTo();
    this.order = notificationInfo.getOrder();
    this.ownerParameter = notificationInfo.getOwnerParameter();
    this.sendToUserIds = notificationInfo.getSendToUserIds();
    this.sendToDaily = notificationInfo.getSendToDaily();
    this.sendToWeekly = notificationInfo.getSendToWeekly();
    this.lastModifiedDate = notificationInfo.getLastModifiedDate();
    this.title = notificationInfo.getTitle();
    this.channelKey = notificationInfo.getChannelKey();
  }
  
  public NotificationInfo build() {
    NotificationInfo notificationInfo = new NotificationInfo();
    notificationInfo.setId(this.id);
    notificationInfo.key(this.key);                                          
    notificationInfo.setFrom(this.from);
    notificationInfo.setTo(this.to);
    notificationInfo.setOrder(this.order);
    notificationInfo.setOwnerParameter(this.ownerParameter);
    notificationInfo.to(this.sendToUserIds);
    notificationInfo.setSendToDaily(this.sendToDaily);
    notificationInfo.setSendToWeekly(this.sendToWeekly);
    notificationInfo.setLastModifiedDate(this.lastModifiedDate);
    notificationInfo.setTitle(this.title);
    notificationInfo.setChannelKey(this.channelKey);
    if (ownerParameter != null && ownerParameter.containsKey(NotificationMessageUtils.SHOW_POPOVER_PROPERTY.getKey())) {
      notificationInfo.setOnPopOver(Boolean.valueOf(ownerParameter.get(NotificationMessageUtils.SHOW_POPOVER_PROPERTY.getKey())));
    }
    if (ownerParameter != null && ownerParameter.containsKey(NotificationMessageUtils.READ_PORPERTY.getKey())) {
      notificationInfo.setRead(Boolean.valueOf(ownerParameter.get(NotificationMessageUtils.READ_PORPERTY.getKey())));
    }

    return notificationInfo;
  }

  public String getTo() {
    return to;
  }

  public WebNotifInfoData updateRead(boolean isRead) {
    if (ownerParameter == null) {
      ownerParameter = new HashMap<String, String>();
    }
    ownerParameter.put(NotificationMessageUtils.READ_PORPERTY.getKey(), String.valueOf(isRead));
    return this;
  }

  public WebNotifInfoData updateShowPopover(boolean isShow) {
    if (ownerParameter == null) {
      ownerParameter = new HashMap<String, String>();
    }
    ownerParameter.put(NotificationMessageUtils.SHOW_POPOVER_PROPERTY.getKey(), String.valueOf(isShow));
    return this;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("WebNotif{")
      .append("title: ")
      .append(title)
      .append("from: ")
      .append(from)
      .append("to: ")
      .append(to)
      .append("channelKey: ")
      .append(channelKey)
      .append(", showPopover: ")
      .append(ownerParameter.get(NotificationMessageUtils.SHOW_POPOVER_PROPERTY.getKey()));
    return sb.toString();

  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    WebNotifInfoData that = (WebNotifInfoData) o;
    return order == that.order &&
            lastModifiedDate == that.lastModifiedDate &&
            Objects.equal(id, that.id) &&
            Objects.equal(key, that.key) &&
            Objects.equal(from, that.from) &&
            Objects.equal(to, that.to) &&
            Objects.equal(ownerParameter, that.ownerParameter) &&
            Objects.equal(sendToUserIds, that.sendToUserIds) &&
            Objects.equal(sendToDaily, that.sendToDaily) &&
            Objects.equal(sendToWeekly, that.sendToWeekly) &&
            Objects.equal(title, that.title) &&
            Objects.equal(channelKey, that.channelKey);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, key, from, to, order, ownerParameter, sendToUserIds, sendToDaily, sendToWeekly, lastModifiedDate, title, channelKey);
  }
}
