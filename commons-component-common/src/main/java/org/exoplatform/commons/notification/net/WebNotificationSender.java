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
package org.exoplatform.commons.notification.net;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;
import org.exoplatform.ws.frameworks.json.value.JsonValue;
import org.exoplatform.ws.frameworks.json.value.impl.StringValue;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Nov 23, 2014  
 */
public class WebNotificationSender {
  private static final Log LOG = ExoLogger.getLogger(WebNotificationSender.class);
  /**
   * @param identifierId
   * @param message
   */
  public static void sendJsonMessage(String remoteId, Object message) {
    try {
      if (message != null) {
        ContinuationService continuation = CommonsUtils.getService(ContinuationService.class);
        if (message instanceof String) {
          JsonValue json = new StringValue("{ \"body\" : " + message.toString() + "}");
          continuation.sendMessage(remoteId, "/eXo/Application/web/NotificationMessage", json, String.valueOf(message.hashCode()));
        } else {
          JsonValue json = new JsonGeneratorImpl().createJsonObject(message);
          continuation.sendMessage(remoteId, "/eXo/Application/web/NotificationMessage", json, String.valueOf(message.hashCode()));
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to send notification message:" +  e.getMessage());
    }
  }

}