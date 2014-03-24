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
public interface StatisticsCollector {

  /**
   * Callback about a message info being created.
   * @param pluginId Notification Plugin is using created message
   */
  public void createMessageInfoCount(String pluginId);
  
  /**
   * Callback about a notification info being created.
   * @param pluginId Notification Plugin is using created notification
   */
  public void createNotificationInfoCount(String pluginId);
  
  /**
   * Callback about a digest message being created.
   * @param pluginId Notification Plugin is using created digest message
   */
  public void createDigestCount(String pluginId);
  
  /**
   * Callback about a entity being deleted
   * @param nodeType nodetype has been deleted
   */
  public void deleteEntity(String nodeType);
  
  /**
   * Callback about a entity being inserted
   * @param nodeType nodetype has been deleted
   */
  public void insertEntity(String nodeType);
  
  /**
   * Callback about a entity being updated
   * @param nodeType nodetype has been updated
   */
  public void updateEntity(String nodeType);
  
  /**
   * Callback about a entity being loaded
   * @param nodeType nodetype has been loaded
   */
  public void loadEntity(String nodeType);
  
  /**
   * Callback indicating execution of a query statement
   *
   * @param statement The query
   * @param rows Number of rows returned
   * @param time execution time
   */
  public void queryExecuted(String statement, long rows, long time);
  /**
   * Callback indicating execution of poll queue
   * @param pluginId
   */
  public void pollQueue(String pluginId);

  /**
   * Callback indicating execution of put queue
   * @param pluginId
   */
  public void putQueue(String pluginId);
}
