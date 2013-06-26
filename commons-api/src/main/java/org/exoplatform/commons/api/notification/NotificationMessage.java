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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.services.jcr.util.IdGenerator;

public class NotificationMessage {
  public static final String PREFIX_ID = "NotificationMessage";

  private String              id;
  private Date                createData;

  private String              providerType;                                  //

  private String              from;

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

  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the createData
   */
  public Date getCreateData() {
    return createData;
  }

  /**
   * @param createData the createData to set
   */
  public NotificationMessage setCreateData(Date createData) {
    this.createData = createData;
    return this;
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

  private String[] addMoreItemInArray(String[] src, String element) {
    List<String> where = new ArrayList<String>(Arrays.asList(src));
    if (element != null && where.contains(element) == false) {
      where.add(element);
      return where.toArray(new String[where.size()]);
    }
    return src;
  }

}
