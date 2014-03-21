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

import org.exoplatform.services.mail.Message;
import org.json.JSONObject;

public class MessageInfo {
  private String id;

  private String from;

  private String to;

  private String body     = "";

  private String subject  = "";

  private String footer;

  private String pluginId = "digest";
  
  private long createdTime;

  public MessageInfo() {
  }

  /**
   * Sets the uuid of messageInfo node
   * @param id the pluginId to set
   */
  public MessageInfo setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * Gets the uuid of messageInfo node
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @return the pluginId
   */
  public String getPluginId() {
    return pluginId;
  }

  /**
   * @param pluginId the pluginId to set
   */
  public MessageInfo pluginId(String pluginId) {
    this.pluginId = pluginId;
    return this;
  }


  /**
   * @return the from
   */
  public String getFrom() {
    return from;
  }

  /**
   * @param from the from to set
   */
  public MessageInfo from(String from) {
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
  public MessageInfo to(String to) {
    this.to = to;
    return this;
  }

  /**
   * @return the body
   */
  public String getBody() {
    return body;
  }

  /**
   * @param body the body to set
   */
  public MessageInfo body(String body) {
    this.body = body;
    return this;
  }

  /**
   * @return the header
   */
  public String getSubject() {
    return subject;
  }

  /**
   * @param header the header to set
   */
  public MessageInfo subject(String subject) {
    this.subject = subject;
    return this;
  }

  /**
   * @return the footer
   */
  public String getFooter() {
    return footer;
  }

  /**
   * @param footer the foodter to set
   */
  public MessageInfo footer(String footer) {
    this.footer = footer;
    return this;
  }
  
  /**
   * Finishes to assign states to current MessageInfo instance
   * @return
   */
  public MessageInfo end() {
    return this;
  }
  
  
  public Message makeEmailNotification() {
    Message message = new Message();
    message.setMimeType("text/html");
    message.setFrom(from);
    message.setTo(to);
    message.setSubject(subject);
    message.setBody(body + ((footer != null && footer.length() > 0) ? footer : ""));
    return message;
  }

  public String makeNotification() {
    return toString();
  }

  public String toJSON() {
    return new JSONObject(this).toString();
  }

  /**
   * Gets time to create MessageInfo and use it as node name
   * @return
   */
  public long getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(long createdTime) {
    this.createdTime = createdTime;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    String body = this.body + ((footer != null && footer.length() > 0) ? footer : ""); 
    builder.append("{ ")
           .append("subject: '").append(subject.replaceAll("'", "&#39;")).append("', ")
           .append("from: '").append(from).append("', ")
           .append("to: '").append(to).append("', ")
           .append("body: '").append(body.replaceAll("'", "&#39;")).append("' ")
           .append("}");
    return builder.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof MessageInfo && getId().equals(((MessageInfo) obj).getId())) {
      return true;
    }
    return super.equals(obj);
  }
  
  @Override
  public int hashCode() {
    return id.hashCode();
  }
  
}
