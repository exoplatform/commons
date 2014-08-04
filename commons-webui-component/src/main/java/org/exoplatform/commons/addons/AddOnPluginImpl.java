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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.config.serialize.PortletApplication;
import org.exoplatform.portal.pom.data.ApplicationData;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.PortletBuilder;

public class AddOnPluginImpl extends AddOnPlugin {

  private String               containerName;

  private List<Application<?>> apps     = new LinkedList<Application<?>>();

  private int                  priority = 5;

  public AddOnPluginImpl(InitParams params) {
    if (params != null) {
      ValueParam containerParam = params.getValueParam("containerName");
      if (containerParam != null) {
        containerName = containerParam.getValue();
      }

      ValueParam priorityParam = params.getValueParam("priority");
      if (priorityParam != null) {
        priority = Integer.parseInt(priorityParam.getValue());
      }

      List<Application<?>> tmp = params.<Application<?>> getObjectParamValues((Class<Application<?>>) (Class<?>) Application.class);
      if (tmp != null) {
        for (Application<?> app : tmp) {
          apps.add(buildApp(app));
        }
      }
    }
  }

  protected Application<?> buildApp(Application<?> app) {
    if (app instanceof PortletModel) {
      PortletModel portletModel = (PortletModel) app;
      PortletApplication pApp = new PortletApplication((ApplicationData) portletModel.build());

      List<String> permissions = portletModel.getPermissions();
      if (permissions != null) {
        pApp.setAccessPermissions(permissions.toArray(new String[permissions.size()]));
      }

      TransientApplicationState<Portlet> state = new TransientApplicationState<Portlet>(portletModel.getContentId());
      Map<String, Object> prefs = portletModel.getPortletPrefs();
      if (prefs != null) {
        PortletBuilder builder = new PortletBuilder();
        for (String key : prefs.keySet()) {
          Object val = prefs.get(key);
          if (val instanceof String) {
            builder.add(key, (String) val);
          } else if (val instanceof List) {
            builder.add(key, (List) val);
          }
        }
        state.setContentState(builder.build());
      }
      pApp.setState(state);
      return pApp;
    } else {
      return app;
    }
  }

  @Override
  public List<Application<?>> getApplications() {
    return Collections.unmodifiableList(apps);
  }

  @Override
  public int getPriority() {
    return priority;
  }

  @Override
  public String getContainerName() {
    return containerName;
  }

}
