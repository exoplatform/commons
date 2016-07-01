/*
 * Copyright (C) 2016 eXo Platform SAS.
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
package org.exoplatform.commons.file.services.job;

import org.exoplatform.commons.utils.ExoProperties;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.scheduler.CronJob;
import org.quartz.JobDataMap;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 */
public class FileStorageCronJob extends CronJob {
    public static String RETENTION_PARAM  = "retention-time";
    private JobDataMap jdatamap_;

    public FileStorageCronJob(InitParams params) throws Exception {
        super(params);
        ExoProperties props = params.getPropertiesParam("FileStorageCleanJob.Param").getProperties();
        jdatamap_ = new JobDataMap();
        String days = props.getProperty(RETENTION_PARAM).trim();
        jdatamap_.put(RETENTION_PARAM, days);
    }

    public JobDataMap getJobDataMap() {
        return jdatamap_;
    }
}
