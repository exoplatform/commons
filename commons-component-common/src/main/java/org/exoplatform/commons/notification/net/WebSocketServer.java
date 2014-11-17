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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.commons.notification.net.router.ExoRouter;
import org.exoplatform.commons.notification.net.router.ExoRouter.Route;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.vertx.java.core.Handler;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.platform.Verticle;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Nov 22, 2014  
 */
public class WebSocketServer extends Verticle {
  public final static String NOTIFICATION_WEB_IDENTIFIER = "notification-web";
  private static final Log logger = ExoLogger.getLogger(WebSocketServer.class);
  private final ObjectMapper mapper = new ObjectMapper();
  private final JsonFactory jsonFactory = mapper.getFactory();
  
  public WebSocketServer() {
    WebSocketBootstrap.setWebSocketServer(this);
  }
  
  @Override
  public void start() {
    vertx.createHttpServer().setAcceptBacklog(10000).websocketHandler(new Handler<ServerWebSocket>() {
      public void handle(final ServerWebSocket ws) {
          if (ws.path().startsWith("/channels/")) {
              ws.dataHandler(new Handler<Buffer>() {
                  public void handle(Buffer data) {
                      try {
                          logger.info(data.toString());
                          JsonParser jp = jsonFactory.createParser(data.toString());
                          JsonNode jsonObj = mapper.readTree(jp);
                          String action = jsonObj.get("action").asText();
                          String requestPath = ws.path();
                          Route route = ExoRouter.route(requestPath);
                          String remoteId = route == null ? "public" : route.localArgs.get("remoteId");
                          String identifier = route == null ? null : route.localArgs.get("identifierId");
                           
                          if (action == null || identifier == null) {
                              return;
                          }
                          if ("subscribe".equals(action)) {
                              logger.info("Connected " + remoteId);
                              Map<String, ServerWebSocket> sockets = WebSocketBootstrap.subscriptions().get(identifier);
                              if (sockets == null) {
                                  sockets = new HashMap<String, ServerWebSocket>();
                                  WebSocketBootstrap.subscriptions().put(identifier, sockets);
                              }
                              sockets.put(remoteId, ws);
                          } else if ("unsubscribe".equals(action)) {
                              Map<String, ServerWebSocket> sockets = WebSocketBootstrap.subscriptions().get(identifier);
                              if (sockets == null) {
                                  return;
                              }
                              sockets.remove(remoteId);
                              if (sockets.size() == 0) {
                                WebSocketBootstrap.subscriptions().remove(identifier);
                              }
                              logger.info(ws.textHandlerID() + " is closed");
                          }
                      } catch (IOException e) {
                          logger.error("Failed to handle " + data, e);
                      }
                  }
              });
              ws.closeHandler(new VoidHandler() {
                public void handle() {
                  logger.info("textHandlerID:" + ws.textHandlerID() + " is closed");
                }
              });
          } else {
              ws.reject();
          }
      }
    }).listen(8181);
  }
  
  /**
   * 
   */
  public void sendMessage(String identifierId, String remoteId, String message) {
    if (WebSocketBootstrap.subscriptions().size() == 0) {
      return;
    }
    if (identifierId == null) {
      return;
    }
    Map<String, ServerWebSocket> sockets = WebSocketBootstrap.subscriptions().get(identifierId);
    if (sockets == null || sockets.size() == 0) {
      return;
    }
    ServerWebSocket ws = sockets.get(remoteId);
    if (ws != null) {
      try {
        ws.writeTextFrame(message);
      } catch (Exception e) {
        WebSocketBootstrap.subscriptions().remove(ws);
        sockets.remove(ws);
        if (sockets.size() == 0) {
          WebSocketBootstrap.subscriptions().remove(identifierId);
        }
        logger.error("Failed to send " + message, e);
      }
    }
  }
  
  /**
   * 
   * @param identifierId
   * @param message
   */
  public void sendMessage(String identifierId, String message) {
    if (WebSocketBootstrap.subscriptions().size() == 0) {
      return;
    }
    if (identifierId == null) {
      return;
    }
    Map<String, ServerWebSocket> sockets = WebSocketBootstrap.subscriptions().get(identifierId);
    if (sockets == null || sockets.size() == 0) {
      return;
    }
    for (ServerWebSocket ws : sockets.values()) {
      try {
        ws.writeTextFrame(message);
      } catch (Exception e) {
        WebSocketBootstrap.subscriptions().remove(ws);
        sockets.remove(ws);
        if (sockets.size() == 0) {
          WebSocketBootstrap.subscriptions().remove(identifierId);
        }
        logger.warn(e.getMessage());
      }
    }
  }

  @Override
  public void stop() {
    
  }

}
