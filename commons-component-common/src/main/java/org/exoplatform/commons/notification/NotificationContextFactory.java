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
package org.exoplatform.commons.notification;

import org.exoplatform.commons.api.notification.stat.Statistics;
import org.exoplatform.commons.api.notification.stat.StatisticsCollector;
import org.exoplatform.commons.notification.impl.StatisticsService;
import org.exoplatform.commons.utils.CommonsUtils;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Oct 11, 2013  
 */
public class NotificationContextFactory {
  
  private final StatisticsService statisticsService;
  
  private static NotificationContextFactory instance = null;
  
  public NotificationContextFactory(StatisticsService statisticsService) {
    this.statisticsService = statisticsService;
  }
  
  public StatisticsService getStatisticsService() {
    return this.statisticsService;
  }
  
  public StatisticsCollector getStatisticsCollector() {
    return this.statisticsService.getStatisticsCollector();
  }
  
  public Statistics getStatistics() {
    return this.statisticsService.getStatistics();
  }

  public static NotificationContextFactory getInstance() {
    if (instance == null) {
      instance = CommonsUtils.getService(NotificationContextFactory.class);
    }
    return instance;
  }

}
