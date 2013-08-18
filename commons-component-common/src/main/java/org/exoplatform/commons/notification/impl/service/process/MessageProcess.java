/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.commons.notification.impl.service.process;

import java.io.Writer;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.UserSetting;

public abstract class MessageProcess {
  public MessageProcess() {}

  private MessageProcess next;
  
  public void setNext(MessageProcess next) {
    this.next = next;
  }
  
  public MessageProcess getNext() {
    return this.next;
  }
  
  /**
   * Process the notification base on userSetting and notificationMessage.
   * 
   * @param setting the setting
   * @param notification the notificationMessage
   */
  public void process(UserSetting setting, NotificationInfo notification, Writer out) {
    if (isValid(setting, notification)) {
      doProcess(setting, notification);
      
      //next
      processNext(setting, notification, out);
    }
  }
  
  /**
   * Process next
   * @param setting
   * @param notification
   */
  private void processNext(UserSetting setting, NotificationInfo notification, Writer out) {
    if (getNext().isValid(setting, notification) && getNext() != null) {
      getNext().process(setting, notification, out);
    }
  }
  
  /**
   * @param setting
   * @param notification
   */
  abstract void doProcess(UserSetting setting, NotificationInfo notification);
  
  abstract boolean isValid(UserSetting setting, NotificationInfo notification);
}
