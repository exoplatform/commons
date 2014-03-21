/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.commons.notification.impl;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

public class NotificationSessionManager {

  private static ThreadLocal<SessionProvider> session_ = new ThreadLocal<SessionProvider>();

  public static SessionProvider getOrCreateSessionProvider() {
    SessionProvider sProvider = session_.get();
    if (sProvider == null) {
      return createSystemProvider();
    }
    return sProvider;
  }

  public static void closeSessionProvider() {
    SessionProvider sProvider = session_.get();
    if (sProvider != null) {
      sProvider.close();
      session_.set(null);
    }
  }

  public static SessionProvider createSystemProvider() {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    session_.set(sProvider);
    return sProvider;
  }
}
