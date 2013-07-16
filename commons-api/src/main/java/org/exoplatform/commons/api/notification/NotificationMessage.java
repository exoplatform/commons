/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.api.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Value;

import org.exoplatform.services.jcr.util.IdGenerator;

public class NotificationMessage {
  public static final String PREFIX_ID = "NotificationMessage";

  private String              id;

  private String              providerType;                                  //

  private String              from           = "";

  private String              to;

  private int                 order;

  private Map<String, String> ownerParameter = new HashMap<String, String>();

  private String              messageType;

  private List<String>        sendToUserIds  = new ArrayList<String>();

  // list users send by frequency
  private String[]            sendToDaily;

  private String[]            sendToWeekly;

  private String[]            sendToMonthly;

  public NotificationMessage() {
    this.id = PREFIX_ID + IdGenerator.generate();
    this.sendToDaily = new String[] { "" };
    this.sendToWeekly = new String[] { "" };
    this.sendToMonthly = new String[] { "" };
  }

  public String getId() {
    return id;
  }

  public NotificationMessage setId(String id) {
    this.id = id;
    return this;
  }
  
  public static NotificationMessage getInstance() {
    return new NotificationMessage();
  }

  public String getProviderType() {
    return providerType;
  }

  public NotificationMessage setProviderType(String providerType) {
    this.providerType = providerType;
    return this;
  }

  public String getFrom() {
    return from;
  }

  public NotificationMessage setFrom(String from) {
    this.from = from;
    return this;
  }
  
  
  /**
   * @return the to
   */
  public String getTo() {
    return to;
  }

  /**
   * @param to the to to set
   */
  public NotificationMessage setTo(String to) {
    this.to = to;
    return this;
  }

  /**
   * @return the order
   */
  public int getOrder() {
    return order;
  }

  /**
   * @param order the order to set
   */
  public NotificationMessage setOrder(int order) {
    this.order = order;
    return this;
  }

  public String getMessageType() {
    return messageType;
  }

  public NotificationMessage setMessageType(String messageType) {
    this.messageType = messageType;
    return this;
  }

  public List<String> getSendToUserIds() {
    return sendToUserIds;
  }

  public NotificationMessage setSendToUserIds(List<String> sendToUserIds) {
    this.sendToUserIds = sendToUserIds;
    return this;
  }

  public NotificationMessage addSendToUserId(String sendToUserId) {
    this.sendToUserIds.add(sendToUserId);
    return this;
  }

  /**
   * @return the ownerParameter
   */
  public Map<String, String> getOwnerParameter() {
    return ownerParameter;
  }

  /**
   * @return the value of ownerParameter
   */
  public String getValueOwnerParameter(String key) {
    return ownerParameter.get(key);
  }

  /**
   * @return the array ownerParameter
   */
  public String[] getArrayOwnerParameter() {
    if (ownerParameter.size() == 0) return new String[] {""};

    String[] strs = ownerParameter.toString().split(", ");
    strs[0] = strs[0].replace("{", "");
    strs[strs.length - 1] = strs[strs.length - 1].replace("}", "");
    return strs;
  }

  /**
   * @param ownerParameter the ownerParameter to set
   */
  public NotificationMessage setOwnerParameter(Map<String, String> ownerParameter) {
    this.ownerParameter = ownerParameter;
    return this;
  }

  /**
   * @param ownerParameter the ownerParameter to set
   */
  public NotificationMessage addOwnerParameter(String key, String value) {
    this.ownerParameter.put(key, value);
    return this;
  }

  /**
   * @param arrays the value to set ownerParameter
   */
  public NotificationMessage setOwnerParameter(Value[] values) {
    if (values == null || values.length == 0) return this;

    for (Value val : values) {
      try {
        String str = val.getString();
        if (str.indexOf("=") > 0) {
          String key = str.substring(0, str.indexOf("=")).trim();
          String value = str.substring(str.indexOf("=") + 1).trim();
          addOwnerParameter(key, value);
        }
      } catch (Exception e) {
        continue;
      }

    }
    return this;
  }

  /**
   * @return the sendToDaily
   */
  public String[] getSendToDaily() {
    return sendToDaily;
  }

  /**
   * @param userIds the list userIds to set for sendToDaily
   */
  public NotificationMessage setSendToDaily(String[] userIds) {
    this.sendToDaily = userIds;
    return this;
  }

  /**
   * @param userId the userId to set into sendToDaily
   */
  public NotificationMessage setSendToDaily(String userId) {
    this.sendToDaily = addMoreItemInArray(sendToDaily, userId);
    return this;
  }

  /**
   * @return the sendToWeekly
   */
  public String[] getSendToWeekly() {
    return sendToWeekly;
  }

  /**
   * @param userIds the list userIds to set for sendToWeekly
   */
  public NotificationMessage setSendToWeekly(String[] userIds) {
    this.sendToWeekly = userIds;
    return this;
  }

  /**
   * @param userId the userId to set into sendToWeekly
   */
  public NotificationMessage setSendToWeekly(String userId) {
    this.sendToWeekly = addMoreItemInArray(sendToWeekly, userId);
    return this;
  }

  /**
   * @return the sendToMonthly
   */
  public String[] getSendToMonthly() {
    return sendToMonthly;
  }

  /**
   * @param userIds the list userIds to set for sendToMonthly
   */
  public NotificationMessage setSendToMonthly(String[] userIds) {
    this.sendToMonthly = userIds;
    return this;
  }

  /**
   * @param userId the userId to set into sendToMonthly
   */
  public NotificationMessage setSendToMonthly(String userId) {
    this.sendToMonthly = addMoreItemInArray(sendToMonthly, userId);
    return this;
  }
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof NotificationMessage) {
      return ((NotificationMessage) o).getId().equals(this.getId());
    }
    return false;
  }
  
  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer("{");
    buffer.append("providerType: ").append(providerType)
    .append(", sendToDaily: ").append(Arrays.asList(sendToDaily).toString())
    .append(", sendToWeekly: ").append(Arrays.asList(sendToWeekly).toString())
    .append(", sendToMonthly: ").append(Arrays.asList(sendToMonthly).toString());
    return buffer.toString();
  }

  private String[] addMoreItemInArray(String[] src, String element) {
    if(element == null || element.trim().length() == 0) {
      return src;
    }
    //
    List<String> where = new ArrayList<String>();
    if (src.length > 1 || (src.length == 1 && src[0].equals("") == false)) {
      where = new ArrayList<String>(Arrays.asList(src));
    }
    if (where.contains(element) == false) {
      where.add(element);
      return where.toArray(new String[where.size()]);
    }
    return src;
  }

}
