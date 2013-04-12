/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.webui.utils;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.application.RequestNavigationData;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.common.router.ExoRouter;
import org.exoplatform.social.common.router.ExoRouter.Route;

 /**
   * Gets the space pretty name based on the current context.
   * 
   * @return Pretty name of space
   * @since 1.2.11
   */
public class Utils
{
  public static String getSpacePrettyNameByContext()
  {
   PortalRequestContext plcontext = Util.getPortalRequestContext();
   String requestPath = plcontext.getControllerContext().getParameter(RequestNavigationData.REQUEST_PATH);
   Route route = ExoRouter.route(requestPath);
   if (route == null) {
     return null;
   }
   return route.localArgs.get("spacePrettyName");
  }
}
