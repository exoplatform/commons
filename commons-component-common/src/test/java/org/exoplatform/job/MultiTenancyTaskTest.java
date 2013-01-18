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

import static org.easymock.EasyMock.createNiceMock;

import java.lang.reflect.Constructor;
import java.util.Date;

import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.JobExecutionContextImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.quartz.spi.TriggerFiredBundle;

/**
 * Created by The eXo Platform SAS
 * Author : Canh Pham Van
 *          canhpv@exoplatform.com
 * Oct 19, 2012  
 */
public class MultiTenancyTaskTest extends BaseCommonsTestCase{
  
  private MultiTenancyJobImpl impl; 
  private TriggerFiredBundle firedBundle; 
  private String repoName = "repository";
  private JobExecutionContext context;
   
   @Override
   public void setUp() throws Exception {
     super.setUp();
     impl = new MultiTenancyJobImpl();    
     
     JobDetail jobDetail = new JobDetailImpl();
     ((JobDetailImpl) jobDetail).setGroup(container.getName());
     context = createContext(jobDetail);
   }

   public void testRun(){
     
     try {      
       Constructor constructor = impl.getTask().getConstructor(MultiTenancyJobImpl.class, JobExecutionContext.class, String.class);
       Runnable temp = (Runnable) constructor.newInstance(impl, context, repoName);
       temp.run();
       
     } catch (Exception e) {
       fail("testRun");
     }
     
   }
   
   @Override
   protected void tearDown() throws Exception {
     super.tearDown();
   }
   
   
   private JobExecutionContext createContext(JobDetail jobDetail) {
     firedBundle = new TriggerFiredBundle(jobDetail, new SimpleTriggerImpl(), null, false, new Date(), new Date(), new Date(), new Date());
     return new StubJobExecutionContext();
   }

   @SuppressWarnings("serial")
   private final class StubJobExecutionContext extends JobExecutionContextImpl {

     private StubJobExecutionContext() {
       super(createNiceMock(Scheduler.class), firedBundle, createNiceMock(Job.class));
     }

   }    

}
