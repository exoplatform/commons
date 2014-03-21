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

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.exoplatform.commons.api.notification.stat.QueryStatistics;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Oct 11, 2013  
 */
public class ThreadLocalQueryStatisticsImpl implements QueryStatistics {

  private final AtomicLong executionCount = new AtomicLong();
  private final AtomicLong executionRowCount = new AtomicLong();
  private final AtomicLong executionMaxTime = new AtomicLong();
  private final AtomicLong executionMinTime = new AtomicLong(Long.MAX_VALUE);
  private final AtomicLong totalExecutionTime = new AtomicLong();
  
  private final Lock readLock;
  private final Lock writeLock;
  
  {
    ReadWriteLock lock = new ReentrantReadWriteLock();
    readLock = lock.readLock();
    writeLock = lock.writeLock();
  }

  
  @Override
  public long getExecutionCount() {
    return executionCount.get();
  }

  @Override
  public long getExecutionRowCount() {
    return executionRowCount.get();
  }

  @Override
  public long getExecutionAvgTime() {
 // We write lock here to be sure that we always calculate the average time
    // with all updates from the executed applied: executionCount and totalExecutionTime
    // both used in the calculation
    writeLock.lock();
    try {
      long avgExecutionTime = 0;
      if (executionCount.get() > 0) {
        avgExecutionTime = totalExecutionTime.get() / executionCount.get();
      }
      return avgExecutionTime;
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public long getExecutionMaxTime() {
    return executionMaxTime.get();
  }

  @Override
  public long getExecutionMinTime() {
    return executionMinTime.get();
  }

  @Override
  public void executed(long rows, long time) {
 // read lock is enough, concurrent updates are supported by the underlying type AtomicLong
    // this only guards executed(long, long) to be called, when another thread is executing getExecutionAvgTime()
    readLock.lock();
    try {
      // Less chances for a context switch
      for (long old = executionMinTime.get(); (time < old) && !executionMinTime.compareAndSet(old, time); old = executionMinTime.get());
      for (long old = executionMaxTime.get(); (time > old) && !executionMaxTime.compareAndSet(old, time); old = executionMaxTime.get());
      executionCount.getAndIncrement();
      executionRowCount.addAndGet(rows);
      totalExecutionTime.addAndGet(time);
    } finally {
      readLock.unlock();
    }
  }
  
  @Override
  public String toString() {
    return new StringBuilder()
        .append("QueryStatistics[")
        .append("executionCount=").append(this.executionCount)
        .append(",executionRowCount=").append(this.executionRowCount)
        .append(",executionAvgTime=").append(this.getExecutionAvgTime())
        .append(",executionMaxTime=").append(this.executionMaxTime)
        .append(",executionMinTime=").append(this.executionMinTime)
        .append(']')
        .toString();
  }

}
