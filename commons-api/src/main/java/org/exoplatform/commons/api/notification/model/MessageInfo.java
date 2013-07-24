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

public class MessageInfo {
  private String from;

  private String to;

  private String body = "";

  private String subject = "";

  private String foodter;

  public MessageInfo() {
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
   * @return the foodter
   */
  public String getFoodter() {
    return foodter;
  }

  /**
   * @param foodter the foodter to set
   */
  public MessageInfo footer(String foodter) {
    this.foodter = foodter;
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
    message.setBody(body + ((foodter != null && foodter.length() > 0) ? foodter : ""));
    return message;
  }

  public String makeNotification() {
    return toString();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    String body = this.body + ((foodter != null && foodter.length() > 0) ? foodter : ""); 
    builder.append("{ ")
           .append("subject: '").append(subject.replaceAll("'", "&#39;")).append("', ")
           .append("from: '").append(from).append("', ")
           .append("to: '").append(to).append("', ")
           .append("body: '").append(body.replaceAll("'", "&#39;")).append("' ")
           .append("}");
    return builder.toString();
  }

}
