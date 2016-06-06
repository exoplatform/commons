/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

    package org.exoplatform.addons.es.job;

import org.exoplatform.addons.es.index.IndexingOperationProcessor;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
@DisallowConcurrentExecution
public class BulkIndexingJob implements Job {
  private static final Log LOG = ExoLogger.getExoLogger(BulkIndexingJob.class);

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    LOG.debug("Running job BulkIndexingJob");
    IndexingOperationProcessor indexingOperationProcessor = CommonsUtils.getService(IndexingOperationProcessor.class);
    indexingOperationProcessor.process();
  }
}