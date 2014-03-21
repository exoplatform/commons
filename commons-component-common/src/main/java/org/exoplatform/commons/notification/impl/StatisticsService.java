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
package org.exoplatform.commons.notification.impl;

import org.exoplatform.commons.api.notification.stat.EntityStatistics;
import org.exoplatform.commons.api.notification.stat.PluginStatistics;
import org.exoplatform.commons.api.notification.stat.QueryStatistics;
import org.exoplatform.commons.api.notification.stat.QueueStatistics;
import org.exoplatform.commons.api.notification.stat.Statistics;
import org.exoplatform.commons.api.notification.stat.StatisticsCollector;
import org.exoplatform.commons.notification.stat.ThreadLocalStatisticsImpl;
import org.exoplatform.commons.utils.PrivilegedSystemHelper;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Oct 11, 2013  
 */
@Managed
@NameTemplate({@Property(key = "service", value = "notification"), @Property(key = "view", value = "statistic") })
@ManagedDescription("Social notifiaction statistics service.")
public class StatisticsService implements Startable {
  private final Statistics stats;
  
  public StatisticsService(PluginStatisticService pluginStatisticService) {
    stats = new ThreadLocalStatisticsImpl(pluginStatisticService);
  }
  
  public Statistics getStatistics() {
    return this.stats;
  }
  
  public StatisticsCollector getStatisticsCollector() {
    return (StatisticsCollector)this.stats;
  }
  /**
   * @see StatisticsServicen#isStatisticsEnabled()
   */
  @Managed
  @ManagedDescription("Gets notification statistice is enabled")
  public boolean isStatisticsEnabled() {
    return stats.isStatisticsEnabled();
  }

  /**
   * @see StatisticsService#setStatisticsEnabled(boolean)
   */
  @Managed
  @ManagedDescription("Set notification statistice is enabled or disabled: TRUE | FALSE")
  public void setStatisticsEnabled(boolean enable) {
    stats.setStatisticsEnabled(enable);
  }
  
  @Managed
  @ManagedDescription("Set notification statistice is enabled.")
  public void activeStatistics() {
    stats.setStatisticsEnabled(true);
  }
  
  @Managed
  @ManagedDescription("Set notification statistice is disabled.")
  public void deactiveStatistics() {
    stats.setStatisticsEnabled(false);
  }
  
  @Managed
  @ManagedDescription("Reset statistics information")
  public void clear() {
    stats.clear();
  }
  @Managed
  @ManagedDescription("Gets plugin statistics by pluginId")
  public PluginStatistics getPluginStatistics(String pluginId) {
    return stats.getPluginStatistics(pluginId);
  }
  @Managed
  @ManagedDescription("Gets entity statistics by nodetype")
  public EntityStatistics getEntityStatistics(String nodeType) {
    return stats.getEntityStatistics(nodeType);
  }
  @Managed
  @ManagedDescription("Gets queue statistics by pluginId")
  public QueueStatistics getQueueStatistics(String pluginId) {
    return stats.getQueueStatistics(pluginId);
  }
  @Managed
  @ManagedDescription("Gets query statistics by pluginId")
  public QueryStatistics getQueryStatistics(String queryString) {
    return stats.getQueryStatistics(queryString);
  }
  
  /**
   * Get global number of message created
   * @return
   */
  @Managed
  @ManagedDescription("Gets message info created count")
  public long getMessageCreatedCount() {
    return stats.getMessageCreatedCount();
  }
  
  /**
   * Get global number of notification created
   * @return
   */
  @Managed
  @ManagedDescription("Gets notification info created count")
  public long getNotificationCreatedCount() {
    return stats.getNotificationCreatedCount();
  }
  
  /**
   * Get global number of digest created
   * @return
   */
  @Managed
  @ManagedDescription("Gets digest info created count")
  public long getDigestCreatedCount() {
    return stats.getDigestCreatedCount();
  }
  
  /**
   * Get global number of entity deletes
   * @return
   */
  @Managed
  @ManagedDescription("Gets entity deleted count")
  public long getEntityDeleteCount() {
    return stats.getEntityDeleteCount();
  }
  
  /**
   * Get global number of entity load
   * @return
   */
  @Managed
  @ManagedDescription("Gets entity load count")
  public long getEntityLoadCount() {
    return stats.getEntityLoadCount();
  }
  
  /**
   * Get global number of entity insert
   * @return
   */
  @Managed
  @ManagedDescription("Gets entity insert count")
  public long getEntityInsertCount() {
    return stats.getEntityInsertCount();
  }
  
  /**
   * Get global number of executed queries
   * @return
   */
  @Managed
  @ManagedDescription("Gets execute query count")
  public long getQueryExecutionCount() {
    return stats.getQueryExecutionCount();
  }

  /**
   * Get the time in milliseconds of the slowest query.
   * @return
   */
  @Managed
  @ManagedDescription("Gets execute query max time")
  public long getQueryExecutionMaxTime() {
    return stats.getQueryExecutionMaxTime();
  }
  
  /**
   * Get the query string for the slowest query.
   */
  @Managed
  @ManagedDescription("Gets execute query statement longest time")
  public String getQueryExecutionMaxTimeQueryString() {
    return stats.getQueryExecutionMaxTimeQueryString();
  }
  
  /**
   * Get global number of put queue
   * @return
   */
  @Managed
  @ManagedDescription("Gets queue put count")
  public long getQueuePutCount() {
    return stats.getQueuePutCount();
  }
  
  /**
   * Get global number of poll queue
   * @return
   */
  @Managed
  @ManagedDescription("Gets queue poll count")
  public long getQueuePollCount() {
    return stats.getQueuePollCount();
  }
  
  /**
   * Get all executed query strings
   */
  @Managed
  @ManagedDescription("Gets all queue statement have been executed")
  public String[] getQueries() {
    return stats.getQueries();
  }
  /**
   * Get the names of all entities
   */
  @Managed
  @ManagedDescription("Gets all pluginIds have been executed")
  public String[] getPluginNames() {
    return stats.getPluginNames();
  }
  
  @Managed
  @ManagedDescription("Gets start time of statistics.")
  public long getStartTime() {
    return stats.getStartTime();
  }

  @Override
  public void start() {
    boolean stats = "true".equalsIgnoreCase(PrivilegedSystemHelper.getProperty("exo.social.notification.statistics.active", "false"));
    this.setStatisticsEnabled(stats);
  }

  @Override
  public void stop() {
  }

}
