/**
 * Copyright (C) 2014 eXo Platform SAS.
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

package org.exoplatform.commons.addons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.pom.data.ApplicationData;

public class AddOnServiceImpl implements AddOnService {

    private List<ApplicationDecorator<?>> apps = new ArrayList<ApplicationDecorator<?>>();

    @Override
    public List<Application<?>> getApplications(String containerName) {
        List<Application<?>> apps = new LinkedList<Application<?>>();

        for (ApplicationDecorator<?> app : this.apps) {
          if (app.getContainerName().equals(containerName)) {
            apps.add(app.getApp());
          }
        }
        return apps;
    }

    @Override
    public void addPlugin(AddOnPlugin plugin) {
      for (Application<?> app : plugin.getApplications()) {
        apps.add(new ApplicationDecorator(app, plugin.getPriority(), plugin.getContainerName()));
      }
      Collections.sort(apps, new Comparator<ApplicationDecorator<?>>() {
          @Override
          public int compare(ApplicationDecorator<?> o1, ApplicationDecorator<?> o2) {
            if (o1.getAppPriority() != o2.getAppPriority()) {
              return o1.getAppPriority() - o2.getAppPriority();
            }
            TransientApplicationState<?> s1 = (TransientApplicationState<?>)o1.getApp().getState();
            TransientApplicationState<?> s2 = (TransientApplicationState<?>)o2.getApp().getState();
            return s1.getContentId().compareTo(s2.getContentId());
          }
      });
    }
    
    class ApplicationDecorator<T> extends Application<T> {
      
      private Application<T> app;
      private int appPriority;
      private String containerName;
      
      public ApplicationDecorator(ApplicationData<T> appData) {
        super(appData);
      }

      public ApplicationDecorator(Application<T> app, int priority, String containerName) {
        super(app.getType());
        this.app = app;
        this.appPriority = priority;
        this.containerName = containerName;
      }

      public Application<T> getApp() {
        return app;
      }

      public void setApp(Application<T> app) {
        this.app = app;
      }

      public int getAppPriority() {
        return appPriority;
      }

      public void setAppPriority(int appPriority) {
        this.appPriority = appPriority;
      }

      public String getContainerName() {
        return containerName;
      }

      public void setContainerName(String containerName) {
        this.containerName = containerName;
      }
      
    }
}