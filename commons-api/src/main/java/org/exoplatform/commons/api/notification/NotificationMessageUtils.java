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
package org.exoplatform.commons.api.notification;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import java.lang.NumberFormatException;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Dec 30, 2014  
 */
public class NotificationMessageUtils {
  private static int maxItemsInPopover = 0;

  protected static final Log LOG = ExoLogger.getLogger(NotificationMessageUtils.class);
  
  public final static ArgumentLiteral<String> READ_PORPERTY = new ArgumentLiteral<String>(String.class, "read");
  
  public final static ArgumentLiteral<String> SHOW_POPOVER_PROPERTY = new ArgumentLiteral<String>(String.class, "showPopover");
  
  public final static ArgumentLiteral<String> NOT_HIGHLIGHT_COMMENT_PORPERTY = new ArgumentLiteral<String>(String.class, "notHighlightComment");
  
  /**
   * Gets the number of notifications that are displayed in the popover list.<br/>
   * The first time this method is called, it will retrieve it from the configuration, via the property exo.notification.maxitems,
   * or default to 8 if the property is not set.<br/>
   * If the property is set to an incorrect value (negative number, 0, not a number), the default value is used too.
   * @return the number of items (notifications) that are displayed in the popover list.
   */
  public static int getMaxItemsInPopover() {
    if (maxItemsInPopover == 0) {
      String maxItemsProperty = System.getProperty("exo.notification.maxitems", "8");
      try {
        maxItemsInPopover = Integer.valueOf(maxItemsProperty);
        if (maxItemsInPopover <= 0) {
          LOG.warn("The value of the property exo.notification.maxitems cannot be 0 or negative. Using the default value instead: 8.");
          maxItemsInPopover = 8;
        }
      } catch (NumberFormatException e) {
        LOG.warn(String.format("The value of the property exo.notification.maxitems is incorrect: %s. Using the default value instead: 8.", maxItemsProperty));
        maxItemsInPopover = 8;
      }
    }
    return maxItemsInPopover;
  }

}
