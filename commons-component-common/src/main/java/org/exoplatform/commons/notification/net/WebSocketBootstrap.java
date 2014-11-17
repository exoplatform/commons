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

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Nov 23, 2014  
 */
public class WebSocketBootstrap implements Startable {
  private static final Log logger = ExoLogger.getLogger(WebSocketBootstrap.class);
  private final PlatformManager pm = PlatformLocator.factory.createPlatformManager();
  private static WebSocketServer ws;
  private final static Map<String, Map<String, ServerWebSocket>> subscriptions = new ConcurrentHashMap<String, Map<String, ServerWebSocket>>();
  
  @Override
  public void start() {
    logger.info("STARTING TO INIT WEBSOCKET");
    JsonObject config = new JsonObject();
    config.putString("test", "test");

    pm.deployVerticle("org.exoplatform.commons.notification.net.WebSocketServer", config, new URL[0], 10, null, new AsyncResultHandler<String>() {
        public void handle(AsyncResult<String> asyncResult) {
        if (asyncResult.succeeded()) {
          logger.info("Deployment ID is " + asyncResult.result());
        } else {
          logger.info(asyncResult.cause());
        }
      }
    });
  }

  @Override
  public void stop() {
    
  }
  
  public static void setWebSocketServer(WebSocketServer server) {
    ws = server; 
  }
  
  /**
   * Send the message to the client
   * 
   * @param identifierId the identifier
   * @param remoteId the remoteId
   * @param message the message will be sent
   */
  public static void sendMessage(String identifierId, String remoteId, String message) {
    if (ws != null) {
      ws.sendMessage(identifierId, remoteId, message);
    }
    
  }
  /**
   * Sending the message to the client without remoteId
   * the message will be sent to all of client what has the same the identifierId
   * 
   * @param identifierId
   * @param message
   */
  public static void sendMessage(String identifierId, String message) {
    if (ws != null) {
      ws.sendMessage(identifierId, message);
    }
  }
  
  public static Map<String, Map<String, ServerWebSocket>> subscriptions() {
    return subscriptions;
  }
  

}
