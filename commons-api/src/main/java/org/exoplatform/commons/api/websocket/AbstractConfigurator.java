/**
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
  
package org.exoplatform.commons.api.websocket;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

public abstract class AbstractConfigurator extends ServerEndpointConfig.Configurator {
  public static final String HTTP_HEADERS = "exo.httpHeaders";
  
  @Override
  public void modifyHandshake(ServerEndpointConfig sec,
                              HandshakeRequest request,
                              HandshakeResponse response) {
    sec.getUserProperties().put(HTTP_HEADERS, request.getHeaders());
    doModifyHandshake(sec, request, response);
  }

  public abstract void doModifyHandshake(ServerEndpointConfig sec,
                              HandshakeRequest request,
                              HandshakeResponse response);
}
