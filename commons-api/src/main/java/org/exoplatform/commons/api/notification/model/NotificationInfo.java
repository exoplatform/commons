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

import java.util.*;

import org.apache.commons.lang.ArrayUtils;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.idgenerator.IDGeneratorService;

public class NotificationInfo {
  public static final String        PREFIX_ID      = "NotificationMessage";

  public static final String        FOR_ALL_USER   = "&forAllUser";

  private static IDGeneratorService idGeneratorService;

  private String                    id;

  private PluginKey                 key;                                   //

  private String                    from;

  private String                    to;

  private int                       order;

  private Map<String, String>       ownerParameter = new HashMap<>();

  private List<String>              sendToUserIds  = new ArrayList<>();

  // list users send by frequency
  private String[]                  sendToDaily;

  private String[]                  sendToWeekly;

  private long                      lastModifiedDate;

  private String                    title          = "";

  private ChannelKey                channelKey;

  private Calendar                  dateCreated;

  private boolean                   isOnPopOver    = true;

  private boolean                   read           = false;

  private boolean                   resetOnBadge   = false;

  private boolean                   isUpdate       = false;

  public NotificationInfo() {
    this.id = PREFIX_ID + generate();
    this.sendToDaily = new String[] { "" };
    this.sendToWeekly = new String[] { "" };
    this.lastModifiedDate = System.currentTimeMillis();
    this.setDateCreated(Calendar.getInstance());
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

  public boolean isUpdate() {
    return isUpdate;
  }

  public NotificationInfo setUpdate(boolean isUpdate) {
    this.isUpdate = isUpdate;
    return this;
  }

  public PluginKey getKey() {
    return this.key;
  }

  public NotificationInfo key(PluginKey key) {
    this.key = key;
    return this;
  }

  public NotificationInfo key(String id) {
    this.key = PluginKey.key(id);
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
   * Gets the title of the notification
   * 
   * @return
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the title of the notification
   * 
   * @param title
   */
  public NotificationInfo setTitle(String title) {
    this.title = title;
    return this;
  }

  /**
   * Gets the channel key of the notification
   * 
   * @return
   */
  public ChannelKey getChannelKey() {
    return channelKey;
  }

  /**
   * Sets the channel of the notification
   * 
   * @param channelKey
   */
  public void setChannelKey(ChannelKey channelKey) {
    this.channelKey = channelKey;
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
    if (to == null) {
      to = sendToUserId;
    }
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
    if (ownerParameter.size() == 0)
      return new String[] { "" };

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
   * @param key the ownerParameter key to set
   * @param value the ownerParameter value to set
   */
  public NotificationInfo with(String key, String value) {
    this.ownerParameter.put(key, value);
    return this;
  }

  public NotificationInfo end() {
    return this;
  }

  /**
   * Get the last modified date
   * 
   * @return
   */
  public long getLastModifiedDate() {
    return lastModifiedDate;
  }

  /**
   * @param lastModifiedDate
   */
  public NotificationInfo setLastModifiedDate(Calendar lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate.getTimeInMillis();
    return this;
  }

  /**
   * @param lastModifiedDate
   */
  public NotificationInfo setLastModifiedDate(long lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
    return this;
  }

  public Calendar getDateCreated() {
    return dateCreated;
  }

  public NotificationInfo setDateCreated(Calendar dateCreated) {
    this.dateCreated = dateCreated;
    return this;
  }

  public boolean isOnPopOver() {
    return isOnPopOver;
  }

  public NotificationInfo setOnPopOver(boolean isOnPopOver) {
    this.isOnPopOver = isOnPopOver;
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

  public boolean isResetOnBadge() {
    return resetOnBadge;
  }

  public NotificationInfo setResetOnBadge(boolean resetOnBadge) {
    this.resetOnBadge = resetOnBadge;
    return this;
  }

  public boolean isRead() {
    return read;
  }

  public NotificationInfo setRead(boolean read) {
    this.read = read;
    return this;
  }

  @Override
  public boolean equals(Object o) {

    if (o instanceof NotificationInfo) {
      NotificationInfo m = (NotificationInfo) o;
      if (super.equals(o)) {
        return true;
      }
      if (m.getId() == null || this.id == null) {
        return false;
      }
      if (m.getId().equals(this.id)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder("{");
    buffer.append("providerType: ")
          .append(key)
          .append(", sendToDaily: ")
          .append(Arrays.asList(sendToDaily).toString())
          .append(", sendToWeekly: ")
          .append(Arrays.asList(sendToWeekly).toString());
    return buffer.toString();
  }

  private String[] addMoreItemInArray(String[] src, String element) {
    if (element == null || element.trim().length() == 0) {
      return src;
    }
    //
    List<String> where = new ArrayList<>();
    if (src.length > 1 || (src.length == 1 && !src[0].equals(""))) {
      where = new ArrayList<>(Arrays.asList(src));
    }
    if (!where.contains(element)) {
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
    List<String> where = new ArrayList<>();
    if (src.length > 1 || (src.length == 1 && !src[0].equals(""))) {
      where = new ArrayList<>(Arrays.asList(src));
    }
    if (where.contains(element)) {
      where.remove(element);
      return where.toArray(new String[where.size()]);
    }
    return src;
  }

  @Override
  public NotificationInfo clone() { // NOSONAR
    return clone(false);
  }

  public NotificationInfo clone(boolean isNew) {
    NotificationInfo message = instance();
    message.setFrom(from)
           .key(key)
           .setTitle(title)
           .setUpdate(!isNew)
           .setOrder(order)
           .setOwnerParameter(new HashMap<String, String>(ownerParameter))
           .setSendToDaily(arrayCopy(sendToDaily))
           .setSendToWeekly(arrayCopy(sendToWeekly))
           .setTo(to)
           .setId(isNew ? null : id);
    if (!isNew) {
      message.setOnPopOver(isOnPopOver)
             .setResetOnBadge(resetOnBadge)
             .setRead(read);
    }
    return message;
  }

  /**
   * Copy the array string, if source is empty or null, return array has one
   * empty item.
   * 
   * @param source
   * @return
   */
  private String[] arrayCopy(String[] source) {
    return (source != null && source.length > 0) ? Arrays.asList(source).toArray(new String[source.length]) : new String[] { "" };
  }

  public static IDGeneratorService getIdGeneratorService() {
    if (idGeneratorService == null) {
      idGeneratorService = ExoContainerContext.getService(IDGeneratorService.class);
    }
    return idGeneratorService;
  }

  public static String generate() {
    String generatedString = Long.toString(System.currentTimeMillis());
    if (getIdGeneratorService() == null) {
      return generatedString;
    } else {
      return getIdGeneratorService().generateStringID(generatedString);
    }
  }
}
