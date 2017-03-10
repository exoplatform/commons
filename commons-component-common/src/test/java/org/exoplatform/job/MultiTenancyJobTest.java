/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.job;

import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.JobExecutionContextImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.quartz.spi.TriggerFiredBundle;

import java.util.Date;

import static org.mockito.Mockito.mock;
/**
 * Created by The eXo Platform SAS
 * Author : Canh Pham Van
 *          canhpv@exoplatform.com
 * Oct 18, 2012
 */
@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/test-root-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/test-portal-configuration.xml") })
public class MultiTenancyJobTest extends BaseCommonsTestCase {

  private MultiTenancyJobImpl impl;
  private TriggerFiredBundle firedBundle;
  private JobExecutionContext context;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    impl = new MultiTenancyJobImpl();

    JobDetail jobDetail = new JobDetailImpl();
    context = createContext(jobDetail);
  }

  public void testExecute(){

    try {
      impl.execute(context);
    } catch (JobExecutionException e) {
      fail("testExecute");
    }

  }

/*  public void testRun(){

    try {
      Constructor constructor = impl.getTask().getConstructor(MultiTenancyJobImpl.class, JobExecutionContext.class, String.class);
      Runnable temp = (Runnable) constructor.newInstance(impl, context, repoName);
      temp.run();

    } catch (Exception e) {
      fail("testRun");
    }

  }*/

  private JobExecutionContext createContext(JobDetail jobDetail) {
    firedBundle = new TriggerFiredBundle(jobDetail, new SimpleTriggerImpl(), null, false, new Date(), new Date(), new Date(), new Date());
    return new StubJobExecutionContext();
  }

  @SuppressWarnings("serial")
  private final class StubJobExecutionContext extends JobExecutionContextImpl {

    private StubJobExecutionContext() {
      super(mock(Scheduler.class), firedBundle, mock(Job.class));
    }

  }
}