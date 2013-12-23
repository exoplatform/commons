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
package org.exoplatform.commons.notification.stat;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.exoplatform.commons.api.notification.stat.EntityStatistics;
import org.exoplatform.commons.api.notification.stat.PluginStatistics;
import org.exoplatform.commons.api.notification.stat.QueryStatistics;
import org.exoplatform.commons.api.notification.stat.QueueStatistics;
import org.exoplatform.commons.api.notification.stat.Statistics;
import org.exoplatform.commons.api.notification.stat.StatisticsCollector;
import org.exoplatform.commons.notification.impl.PluginStatisticService;
import org.hibernate.internal.util.collections.ArrayHelper;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Oct 10, 2013  
 */
public class ThreadLocalStatisticsImpl implements Statistics, StatisticsCollector {

  private volatile boolean isStatisticsEnabled;
  private volatile long startTime;
  
  private AtomicLong entityLoadCount = new AtomicLong();
  private AtomicLong entityUpdateCount = new AtomicLong();
  private AtomicLong entityInsertCount = new AtomicLong();
  private AtomicLong entityDeleteCount = new AtomicLong();
  
  private AtomicLong queuePutCount = new AtomicLong();
  private AtomicLong queuePollCount = new AtomicLong();
  
  private AtomicLong messageCreatedCount = new AtomicLong();
  private AtomicLong notificationCreatedCount = new AtomicLong();
  private AtomicLong digestCreatedCount = new AtomicLong();
  
  private AtomicLong queryExecutionCount = new AtomicLong();
  private AtomicLong queryExecutionMaxTime = new AtomicLong();
  private volatile String queryExecutionMaxTimeQueryString;
  
  //
  private final PluginStatisticService pluginStatistic;
  
  
  /**
   * plugin statistics per name
   */
  private final ConcurrentMap<String, PluginStatistics> pluginStatistics = new ConcurrentHashMap<String, PluginStatistics>();
  
  /**
   * queue statistics per name
   */
  private final ConcurrentMap<String, QueueStatistics> queueStatistics = new ConcurrentHashMap<String, QueueStatistics>();
  
  /**
   * entity statistics per name
   */
  private final ConcurrentMap<String, EntityStatistics> entityStatistics = new ConcurrentHashMap<String, EntityStatistics>();
  /**
   * entity statistics per query string
   */
  private final ConcurrentMap<String, QueryStatistics> queryStatistics = new ConcurrentHashMap<String, QueryStatistics>();
  
  public ThreadLocalStatisticsImpl(PluginStatisticService pluginStatistic) {
    clear();
    this.pluginStatistic = pluginStatistic;
  }
  
  @Override
  public void clear() {
    entityLoadCount.set(0);
    entityUpdateCount.set(0);
    entityInsertCount.set(0);
    entityDeleteCount.set(0);
    
    queryExecutionCount.set(0);
    queryExecutionMaxTime.set(0);
    
    entityStatistics.clear();
    queryStatistics.clear();
    
    queuePutCount.set(0);
    queuePollCount.set(0);
    
    startTime = System.currentTimeMillis();
  }
  
  @Override
  public void createMessageInfoCount(String pluginId) {
    messageCreatedCount.incrementAndGet();
    pluginStatistic.increaseCreatedMessageCount(pluginId);
    getPluginStatistics(pluginId).incrementCreateMessageCount();
  }

  @Override
  public void createNotificationInfoCount(String pluginId) {
    notificationCreatedCount.incrementAndGet();
    pluginStatistic.increaseCreatedNotifCount(pluginId);
    getPluginStatistics(pluginId).incrementCreateNotificationCount();
  }

  @Override
  public void createDigestCount(String pluginId) {
    digestCreatedCount.incrementAndGet();
    pluginStatistic.increaseCreatedDigestCount(pluginId);
    getPluginStatistics(pluginId).incrementCreateDigestCount();
  }

  @Override
  public void deleteEntity(String nodeType) {
    entityDeleteCount.incrementAndGet();
    getEntityStatistics(nodeType).incrementDeleteCount();
  }

  @Override
  public void insertEntity(String nodeType) {
    entityInsertCount.incrementAndGet();
    getEntityStatistics(nodeType).incrementInsertCount();
  }

  @Override
  public void updateEntity(String nodeType) {
    entityUpdateCount.incrementAndGet();
    getEntityStatistics(nodeType).incrementUpdateCount();
  }

  @Override
  public void loadEntity(String nodeType) {
    entityLoadCount.incrementAndGet();
    getEntityStatistics(nodeType).incrementLoadCount();
  }

  @Override
  public void queryExecuted(String statement, long rows, long time) {
    queryExecutionCount.getAndIncrement();
    boolean isLongestQuery = false;
    
    for ( long old = queryExecutionMaxTime.get();
        ( isLongestQuery = time > old ) && ( !queryExecutionMaxTime.compareAndSet( old, time ) );
        old = queryExecutionMaxTime.get() ) {
    }
    
    if ( isLongestQuery ) {
      queryExecutionMaxTimeQueryString = statement;
    }
    if ( statement != null ) {
      getQueryStatistics(statement).executed(rows, time);
    }
    
  }
  
  @Override
  public void pollQueue(String pluginId) {
    queuePollCount.incrementAndGet();
    getQueueStatistics(pluginId).incrementPollCount();
  }

  @Override
  public void putQueue(String pluginId) {
    queuePutCount.incrementAndGet();
    getQueueStatistics(pluginId).incrementPutCount();
  }

  @Override
  public PluginStatistics getPluginStatistics(String pluginId) {
    PluginStatistics ps = pluginStatistics.get(pluginId);
    if ( ps == null ) {
      ps = new ThreadLocalPluginStatisticsImpl();
      PluginStatistics previous;
      if ( ( previous = pluginStatistics.putIfAbsent(pluginId, ps)) != null ) {
        ps = previous;
      }
    }
    return ps;
  }

  @Override
  public EntityStatistics getEntityStatistics(String nodeType) {
    EntityStatistics es = entityStatistics.get(nodeType);
    if ( es == null ) {
      es = new ThreadLocalEntityStatisticsImpl();
      EntityStatistics previous;
      if ( ( previous = entityStatistics.putIfAbsent(nodeType, es)) != null ) {
        es = previous;
      }
    }
    return es;
  }

  @Override
  public QueueStatistics getQueueStatistics(String pluginId) {
    QueueStatistics qs = queueStatistics.get(pluginId);
    if ( qs == null ) {
      qs = new ThreadLocalQueueStatisticsImpl();
      QueueStatistics previous;
      if ( ( previous = queueStatistics.putIfAbsent(pluginId, qs)) != null ) {
        qs = previous;
      }
    }
    return qs;
  }

  @Override
  public QueryStatistics getQueryStatistics(String queryString) {
    QueryStatistics qs = queryStatistics.get(queryString);
    if ( qs == null ) {
      qs = new ThreadLocalQueryStatisticsImpl();
      QueryStatistics previous;
      if ( ( previous = queryStatistics.putIfAbsent(queryString, qs)) != null ) {
        qs = previous;
      }
    }
    return qs;
  }

  @Override
  public long getEntityDeleteCount() {
    return entityDeleteCount.get();
  }

  @Override
  public long getEntityLoadCount() {
    return entityLoadCount.get();
  }

  @Override
  public long getEntityInsertCount() {
    return entityInsertCount.get();
  }

  @Override
  public long getQueryExecutionCount() {
    return queryExecutionCount.get();
  }

  @Override
  public long getQueryExecutionMaxTime() {
    return queryExecutionMaxTime.get();
  }

  @Override
  public String getQueryExecutionMaxTimeQueryString() {
    return queryExecutionMaxTimeQueryString;
  }

  @Override
  public boolean isStatisticsEnabled() {
    return isStatisticsEnabled;
  }

  @Override
  public String[] getQueries() {
    return ArrayHelper.toStringArray( queryStatistics.keySet() );
  }

  @Override
  public String[] getPluginNames() {
    return ArrayHelper.toStringArray( pluginStatistics.keySet() );
  }
  
  public long getStartTime() {
    return startTime;
  }
  
  public String toString() {
    return new StringBuilder()
        .append( "Statistics[" )
        .append( "start time=" ).append( startTime )
        .append( ",message created=" ).append( messageCreatedCount )
        .append( ",notification created=" ).append( notificationCreatedCount )
        .append( ",digest created=" ).append( digestCreatedCount )
        .append( ",queue put=" ).append( queuePutCount )
        .append( ",queue poll=" ).append( queuePollCount )
        .append( ",entities loaded=" ).append( entityLoadCount )
        .append( ",entities updated=" ).append( entityUpdateCount )
        .append( ",entities inserted=" ).append( entityInsertCount )
        .append( ",entities deleted=" ).append( entityDeleteCount )
        .append( ",queries executed to database=" ).append( queryExecutionCount )
        .append( ",max query time=" ).append( queryExecutionMaxTime )
        .append( ']' )
        .toString();
  }

  @Override
  public long getQueuePutCount() {
    return queuePutCount.get();
  }

  @Override
  public long getQueuePollCount() {
    return queuePollCount.get();
  }

  @Override
  public long getMessageCreatedCount() {
    return messageCreatedCount.get();
  }

  @Override
  public long getNotificationCreatedCount() {
    return notificationCreatedCount.get();
  }

  @Override
  public long getDigestCreatedCount() {
    return digestCreatedCount.get();
  }

  @Override
  public void setStatisticsEnabled(boolean enable) {
    this.isStatisticsEnabled = enable;
  }

}
