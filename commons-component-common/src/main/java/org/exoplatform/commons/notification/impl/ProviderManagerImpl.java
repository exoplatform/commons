/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.notification.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.api.notification.Provider;
import org.exoplatform.commons.api.notification.plugin.ProviderModel;
import org.exoplatform.commons.api.notification.plugin.ProviderPlugin;
import org.exoplatform.commons.api.notification.plugin.Template;
import org.exoplatform.commons.api.notification.service.ProviderManager;
import org.exoplatform.commons.notification.NotificationProperties;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

public class ProviderManagerImpl implements ProviderManager, Startable, NotificationProperties {
  private static final Log LOG = ExoLogger.getLogger(ProviderManagerImpl.class);
  
  private List<ProviderPlugin>       providerPlugins = new ArrayList<ProviderPlugin>();

  private String                     workspace;

  public ProviderManagerImpl(InitParams params) {
    this.workspace = params.getValueParam(NotificationUtils.WORKSPACE_PARAM).getValue();
    if (this.workspace == null) {
      this.workspace = NotificationUtils.DEFAULT_WORKSPACE_NAME;
    }
  }

  @Override
  public void start() {
    initProviders();
  }

  @Override
  public void stop() {

  }

  private Node getProviderHomeNode(SessionProvider sProvider) throws Exception {
    Node homeNode = NotificationUtils.getNotificationHomeNode(sProvider, workspace);
    if (homeNode.hasNode(NotificationUtils.PROVIDER_HOME_NODE) == false) {
      Node node = homeNode.addNode(NotificationUtils.PROVIDER_HOME_NODE, NTF_PROVIDER_HOME);
      homeNode.getSession().save();
      return node;
    }
    return homeNode.getNode(NotificationUtils.PROVIDER_HOME_NODE);
  }

  @Override
  public void registerProviderPlugin(ProviderPlugin providerPlugin) {
    providerPlugins.add(providerPlugin);    
  }
  

  public void initProviders() {
    for (ProviderPlugin pp : providerPlugins) {
      for (ProviderModel pm : pp.getProviderModels()) {
        Provider provider = new Provider();
        provider.setName(pm.getName());
        provider.setIsActive(true);
        provider.setParams(Arrays.asList(pm.getParams().split(",")));
        
        List<Template> templates = pm.getTemplates();
        for (Template template : templates) {
          provider.addSubject(template.getLanguage(), template.getSubject());
          provider.addTemplate(template.getLanguage(), template.getTemplate());
        }

        //
        saveProvier(provider);
      }
    }
    
  }
  
  @Override
  public void saveProvier(Provider provider) {
    SessionProvider sProvider = NotificationUtils.createSystemProvider();
    try {
      Node providerHomeNode = getProviderHomeNode(sProvider);
      Node providerNode;
      if(providerHomeNode.hasNode(provider.getType()) == false) {
        providerNode = providerHomeNode.addNode(provider.getType(), NTF_PROVIDER);
      } else {
        providerNode = providerHomeNode.getNode(provider.getType());
      }

      providerNode.setProperty(NTF_TYPE, provider.getType());
      providerNode.setProperty(NTF_NAME, provider.getName());
      providerNode.setProperty(NTF_PARAMS, provider.getArrayParams());
      providerNode.setProperty(NTF_TEMPLATES, provider.getType());
      providerNode.setProperty(NTF_TYPE, provider.getType());
      
      if(providerHomeNode.isNew()) {
        providerHomeNode.getSession().save();
      } else {
        providerHomeNode.save();
      }
    } catch (Exception e) {
      LOG.error("Can not save the Provider", e);
    }

  }

  @Override
  public Provider getProvier(String providerType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Provider> getActiveProvier(boolean isAdmin) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Provider> getAddProvier() {
    // TODO Auto-generated method stub
    return null;
  }

}
