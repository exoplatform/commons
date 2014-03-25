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
package org.exoplatform.commons.api.notification.stat;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Oct 10, 2013  
 */
public interface Statistics {
  
  /**
   * Reset all statistics
   */
  public void clear();
  
  /**
   * Gets PluginStatistics by Id
   * @return
   */
  public PluginStatistics getPluginStatistics(String pluginId);
  
  /**
   * Gets EntityStatistics by nodeType
   * @return
   */
  public EntityStatistics getEntityStatistics(String nodeType);
  
  /**
   * Gets QueueStatistics by Id
   * @return
   */
  public QueueStatistics getQueueStatistics(String pluginId);
  
  /**
   * Gets QueryStatistics by queryString
   * @param queryString
   * @return
   */
  public QueryStatistics getQueryStatistics(String queryString);
  
  /**
   * Get global number of entity deletes
   * @return
   */
  public long getMessageCreatedCount();
  
  /**
   * Get global number of entity load
   * @return
   */
  public long getNotificationCreatedCount();
  
  /**
   * Get global number of entity insert
   * @return
   */
  public long getDigestCreatedCount();
  
  /**
   * Get global number of entity deletes
   * @return
   */
  public long getEntityDeleteCount();
  
  /**
   * Get global number of entity load
   * @return
   */
  public long getEntityLoadCount();
  
  /**
   * Get global number of entity insert
   * @return
   */
  public long getEntityInsertCount();
  
  /**
   * Get global number of executed queries
   * @return
   */
  public long getQueryExecutionCount();

  /**
   * Get the time in milliseconds of the slowest query.
   * @return
   */
  public long getQueryExecutionMaxTime();
  
  /**
   * Get the query string for the slowest query.
   */
  public String getQueryExecutionMaxTimeQueryString();
  
  /**
   * Get global number of put queue
   * @return
   */
  public long getQueuePutCount();
  
  /**
   * Get global number of poll queue
   * @return
   */
  public long getQueuePollCount();
  
  /**
   * Are statistics logged
   */
  public boolean isStatisticsEnabled();
  
  /**
   * Get all executed query strings
   */
  public String[] getQueries();
  /**
   * Get the names of all entities
   */
  public String[] getPluginNames();
  
  /**
   * Gets the start time of statistics process
   * @return
   */
  public long getStartTime();
  
  /**
   * Sets is running on statistic mode.
   * @param enable
   */
  public void setStatisticsEnabled(boolean enable);
  
}
