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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.impl.JobDetailImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by The eXo Platform SAS Author : Lai Trung Hieu
 * hieult@exoplatform.com Aug 5, 2011
 */
public abstract class MultiTenancyJob implements Job {

  private static Log         LOG  = ExoLogger.getLogger(MultiTenancyJob.class);

  private static final Class<?> TENANTS_SERVICE_CLASS;
  private static final Method GET_CURRENT_TENANT_METHOD;
  private static final Method GET_NAME_METHOD;
  private static final boolean TENANT_MODE;
  static {
    Class<?> c = null;
    Method m1 = null;
    Method m2 = null;
    try {
      c = Class.forName("org.exoplatform.container.multitenancy.TenantsService");
      LOG.debug("Could find the class TenantsService, so we assume that we are in multitenant mode");
      m1 = c.getMethod("getCurrentTanant");
      LOG.debug("Could find the method allowing to get the current tenant");
      m2 = m1.getReturnType().getMethod("getName");
      LOG.debug("Could find the method allowing to get the name of the current tenant");
      LOG.debug("Could find anything needed for the multitenant mode");
    } catch (ClassNotFoundException e) {
      LOG.debug("Could not find a class needed for the tenant mode, so we assume that we are in normal mode");
    } catch (NoSuchMethodException e) {
      LOG.error("Could not find a method needed to get the current tenant", e);
    } catch (SecurityException e) {
      LOG.error("Could not get what is required to get the current tenant", e);
    }
    TENANTS_SERVICE_CLASS = c;
    GET_CURRENT_TENANT_METHOD = m1;
    GET_NAME_METHOD = m2;
    TENANT_MODE = TENANTS_SERVICE_CLASS != null && GET_CURRENT_TENANT_METHOD != null && GET_NAME_METHOD != null;
  }
  public static final String COLON = ":".intern();

  public abstract Class<? extends MultiTenancyTask> getTask();

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    RepositoryService repoService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    String tenantName = null;
    if (TENANT_MODE) {
      Object tenantsService = container.getComponentInstanceOfType(TENANTS_SERVICE_CLASS);
      if (tenantsService == null)
        LOG.debug("Could not find any instance of type TenantsService, so we assume that we are in normal mode");
      else {
        try {
          Object o = GET_CURRENT_TENANT_METHOD.invoke(tenantsService);
          tenantName = (String)GET_NAME_METHOD.invoke(o);
        } catch (Exception e) {
          LOG.error("Could not get the name of the current tenant: " + e.getMessage());
          LOG.debug("Could not get the name of the current tenant", e);
        }
      }
    }
    List<RepositoryEntry> entries = repoService.getConfig().getRepositoryConfigurations();
    for (RepositoryEntry repositoryEntry : entries) {
      if (tenantName != null && !tenantName.equals(repositoryEntry.getName()))
        continue;
      try {
        @SuppressWarnings("unchecked")
        Constructor<MultiTenancyTask> constructor = (Constructor<MultiTenancyTask>)getTask()
                                                                                   .getConstructor(this.getClass(), 
                                                                                                   JobExecutionContext.class, 
                                                                                                   String.class);
        constructor.newInstance(this, context, repositoryEntry.getName()).run();
      } catch (Exception e) {
        LOG.error("Exception when looking for multi-tenancy task", e);
      }
    }
  }

  public class MultiTenancyTask implements Runnable {

    protected JobExecutionContext context;

    protected PortalContainer     container;

    protected String              repoName;

    public MultiTenancyTask(JobExecutionContext context, String repoName) {
      this.context = context;
      this.repoName = repoName;
    }

    @Override
    public void run() {
      this.container = getPortalContainer(context);

      if (container == null) {
        throw new IllegalStateException("Container is empty");
      }
      ExoContainerContext.setCurrentContainer(container);
      RepositoryService repoService = (RepositoryService) ExoContainerContext.getCurrentContainer()
                                                                             .getComponentInstanceOfType(RepositoryService.class);
      try {
        repoService.setCurrentRepositoryName(repoName);
      } catch (RepositoryConfigurationException e) {
        LOG.error("Repository is error", e);
      }
    }
  }

  public static PortalContainer getPortalContainer(JobExecutionContext context) {
    if (context == null)
      return null;
    String portalName = ((JobDetailImpl)context.getJobDetail()).getGroup();
    if (portalName == null)
      return null;
    if (portalName.indexOf(COLON) > 0)
      portalName = portalName.substring(0, portalName.indexOf(":"));
    return RootContainer.getInstance().getPortalContainer(portalName);
  }
}
