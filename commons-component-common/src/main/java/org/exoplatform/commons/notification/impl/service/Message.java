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
package org.exoplatform.commons.notification.impl.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Nov 17, 2014  
 */
public class Message {
  
  private String              from;

  private String              to;

  private String              pluginId;

  private Map<String, String> ownerParameter;
  
  private String              message;
  
  public Message(final String to, final String pluginId) {
    this.to = to;
    this.pluginId = pluginId;
  }

  public Message() {
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public String getPluginId() {
    return pluginId;
  }

  public void setPluginId(String pluginId) {
    this.pluginId = pluginId;
  }

  public Map<String, String> getOwnerParameter() {
    return ownerParameter;
  }

  public void setOwnerParameter(Map<String, String> ownerParameter) {
    this.ownerParameter = ownerParameter;
  }
  
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String buildStringFromMap(Map<String, String> ownerParameter) {
    StringBuilder sb = new StringBuilder();
    boolean hasNext = false;
    for (Entry<String, String> entry : ownerParameter.entrySet()) {
      if (hasNext) {
        sb.append(", ");
      }
      sb.append(entry.getKey()).append(" : ").append(entry.getValue());
      hasNext = ownerParameter.size() > 1;
    }
    return sb.toString();
  }
  
  public Map<String, String> buildMapFromString(String input) {
    Map<String, String> ownerParameter = new HashMap<String, String>();
    for (String element : input.split(",")) {
      String[] entry = element.split(":");
      ownerParameter.put(entry[0], entry.length > 1 ? entry[1] : "");
    }
    return ownerParameter;
  }
  
  public String toString() {
    return to + " : " + pluginId;
  }
  

}
