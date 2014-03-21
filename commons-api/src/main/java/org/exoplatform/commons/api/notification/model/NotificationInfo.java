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
package org.exoplatform.commons.api.notification.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Value;

import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.services.jcr.util.IdGenerator;

public class NotificationInfo {
  public static final String  PREFIX_ID     = "NotificationMessage";

  public static final String  FOR_ALL_USER  = "&forAllUser";

  private String              id;

  private NotificationKey     key;                                  //

  private String              from           = "";

  private String              to;

  private int                 order;

  private Map<String, String> ownerParameter = new HashMap<String, String>();

  private List<String>        sendToUserIds  = new ArrayList<String>();

  // list users send by frequency
  private String[]            sendToDaily;

  private String[]            sendToWeekly;

  public NotificationInfo() {
    this.id = PREFIX_ID + IdGenerator.generate();
    this.sendToDaily = new String[] { "" };
    this.sendToWeekly = new String[] { "" };
  }
  
  public static NotificationInfo instance() {
    return new NotificationInfo();
  }

  public String getId() {
    return id;
  }

  public NotificationInfo setId(String id) {
    this.id = id;
    return this;
  }

  public NotificationInfo setSendAll(boolean isSendAll) {
    if (isSendAll) {
      setSendToDaily(new String[] { FOR_ALL_USER });
      setSendToWeekly(new String[] { FOR_ALL_USER });
    } else {
      removeOnSendToDaily(FOR_ALL_USER);
      removeOnSendToWeekly(FOR_ALL_USER);
    }
    return this;
  }
  
  public boolean isSendAll() {
    return ArrayUtils.contains(sendToDaily, FOR_ALL_USER) || 
      ArrayUtils.contains(sendToWeekly, FOR_ALL_USER);
  }


  public NotificationKey getKey() {
    return this.key;
  }

  public NotificationInfo key(NotificationKey key) {
    this.key = key;
    return this;
  }
  
  public NotificationInfo key(String id) {
    this.key = NotificationKey.key(id);
    return this;
  }

  public String getFrom() {
    return from;
  }

  public NotificationInfo setFrom(String from) {
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
  public NotificationInfo setTo(String to) {
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
  public NotificationInfo setOrder(int order) {
    this.order = order;
    return this;
  }

  public List<String> getSendToUserIds() {
    return sendToUserIds;
  }

  public NotificationInfo to(List<String> sendToUserIds) {
    this.sendToUserIds = sendToUserIds;
    return this;
  }

  public NotificationInfo to(String sendToUserId) {
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
  public NotificationInfo setOwnerParameter(Map<String, String> ownerParameter) {
    this.ownerParameter = ownerParameter;
    return this;
  }

  /**
   * @param ownerParameter the ownerParameter to set
   */
  public NotificationInfo with(String key, String value) {
    this.ownerParameter.put(key, value);
    return this;
  }
  
  public NotificationInfo end() {
    return this;
  }

  /**
   * @param arrays the value to set ownerParameter
   */
  public NotificationInfo setOwnerParameter(Value[] values) {
    if (values == null || values.length == 0) return this;

    for (Value val : values) {
      try {
        String str = val.getString();
        if (str.indexOf("=") > 0) {
          String key = str.substring(0, str.indexOf("=")).trim();
          String value = str.substring(str.indexOf("=") + 1).trim();
          with(key, value);
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
  public NotificationInfo setSendToDaily(String[] userIds) {
    this.sendToDaily = userIds;
    return this;
  }

  /**
   * @param userId the userId to add into sendToDaily
   */
  public NotificationInfo setSendToDaily(String userId) {
    this.sendToDaily = addMoreItemInArray(sendToDaily, userId);
    return this;
  }

  /**
   * @param userId the userId to remove into sendToDaily
   */
  public NotificationInfo removeOnSendToDaily(String userId) {
    this.sendToDaily = removeItemInArray(sendToDaily, userId);
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
  public NotificationInfo setSendToWeekly(String[] userIds) {
    this.sendToWeekly = userIds;
    return this;
  }
  
  /**
   * @param userId the userId to add into sendToWeekly
   */
  public NotificationInfo setSendToWeekly(String userId) {
    this.sendToWeekly = addMoreItemInArray(sendToWeekly, userId);
    return this;
  }
  
  /**
   * @param userId the userId to remove into sendToWeekly
   */
  public NotificationInfo removeOnSendToWeekly(String userId) {
    this.sendToWeekly = removeItemInArray(sendToWeekly, userId);
    return this;
  }

  
  @Override
  public boolean equals(Object o) {

    if (o instanceof NotificationInfo) {
      NotificationInfo m = (NotificationInfo) o;
      if (super.equals(o)) {
        return true;
      }
      if (m.getId().equals(this.id)) {
        return true;
      }

      if (m.getTo() != null && m.getTo().length() > 0 && m.getTo().equals(to) 
            && m.getKey() != null && m.getKey().equals(key)) {
        return true;
      }
    }
    return false;
  }
  
  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer("{");
    buffer.append("providerType: ").append(key)
    .append(", sendToDaily: ").append(Arrays.asList(sendToDaily).toString())
    .append(", sendToWeekly: ").append(Arrays.asList(sendToWeekly).toString());
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

  private String[] removeItemInArray(String[] src, String element) {
    if (element == null || element.trim().length() == 0) {
      return src;
    }
    //
    List<String> where = new ArrayList<String>();
    if (src.length > 1 || (src.length == 1 && src[0].equals("") == false)) {
      where = new ArrayList<String>(Arrays.asList(src));
    }
    if (where.contains(element)) {
      where.remove(element);
      return where.toArray(new String[where.size()]);
    }
    return src;
  }

  @Override
  public NotificationInfo clone() {
    NotificationInfo message = instance();
    message.setFrom(from)
           .key(key)
           .setId(id)
           .setOrder(order)
           .setOwnerParameter(ownerParameter)
           .setSendToDaily(sendToDaily)
           .setSendToWeekly(sendToWeekly)
           .setTo(to);
    return message;
  }
}
