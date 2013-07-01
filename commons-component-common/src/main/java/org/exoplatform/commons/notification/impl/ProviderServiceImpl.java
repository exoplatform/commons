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
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.commons.api.notification.Provider;
import org.exoplatform.commons.api.notification.plugin.ProviderModel;
import org.exoplatform.commons.api.notification.plugin.ProviderPlugin;
import org.exoplatform.commons.api.notification.plugin.Template;
import org.exoplatform.commons.api.notification.service.ProviderService;
import org.exoplatform.commons.notification.AbstractService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

public class ProviderServiceImpl extends AbstractService implements ProviderService, Startable {
  private static final Log LOG = ExoLogger.getLogger(ProviderServiceImpl.class);
  
  private List<ProviderPlugin>       providerPlugins       = new ArrayList<ProviderPlugin>();

  private String                     workspace;

  public ProviderServiceImpl(InitParams params) {
    this.workspace = params.getValueParam(WORKSPACE_PARAM).getValue();
    if (this.workspace == null) {
      this.workspace = DEFAULT_WORKSPACE_NAME;
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
    Node homeNode = getNotificationHomeNode(sProvider, workspace);
    if (homeNode.hasNode(PROVIDER_HOME_NODE) == false) {
      Node node = homeNode.addNode(PROVIDER_HOME_NODE, NTF_PROVIDER_HOME);
      homeNode.getSession().save();
      return node;
    }
    return homeNode.getNode(PROVIDER_HOME_NODE);
  }

  @Override
  public void registerProviderPlugin(ProviderPlugin providerPlugin) {
    providerPlugins.add(providerPlugin);    
  }

  private void initProviders() {
    SessionProvider sProvider = CommonsUtils.getSystemSessionProvider();
    for (ProviderPlugin pp : providerPlugins) {
      for (ProviderModel pm : pp.getProviderModels()) {
        //
        if (isExistingProvider(sProvider, pm.getType())) {
          continue;
        }
        //
        Provider provider = new Provider();
        provider.setType(pm.getType());
        provider.setName(pm.getName());
        provider.setParams(pm.getParams());

        List<Template> templates = pm.getTemplates();
        for (Template template : templates) {
          provider.addSubject(template.getLanguage(), template.getSubject());
          provider.addTemplate(template.getLanguage(), template.getTemplate());
        }

        //
        saveProvider(provider);
      }
    }

  }
  
  private boolean isExistingProvider(SessionProvider sProvider, String providerType) {
    try {
      Node providerHomeNode = getProviderHomeNode(sProvider);
      return (providerHomeNode.hasNode(providerType)) ? true : false;
    } catch (Exception e) {
      return false;
    }
  }
  
  @Override
  public void saveProvider(Provider provider) {
    SessionProvider sProvider = CommonsUtils.getSystemSessionProvider();
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
      providerNode.setProperty(NTF_TEMPLATES, provider.getArrayTemplates());
      providerNode.setProperty(NTF_SUBJECTS, provider.getArraySubjects());
      
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
  public Provider getProvider(String providerType) {
    SessionProvider sProvider = CommonsUtils.getSystemSessionProvider();
    try {
      Node providerHomeNode = getProviderHomeNode(sProvider);

      return getProvider(providerHomeNode.getNode(providerType));
    } catch (Exception e) {
      LOG.error("Can not save the Provider", e);
    }
    return null;
  }

  private Provider getProvider(Node providerNode) throws Exception {
    if (providerNode == null) {
      return null;
    }
    Provider provider = new Provider();
    provider.setType(providerNode.getProperty(NTF_TYPE).getString())
            .setName(providerNode.getProperty(NTF_NAME).getString())

            .setParams(providerNode.getProperty(NTF_PARAMS).getValues())

            .setSubjects(providerNode.getProperty(NTF_SUBJECTS).getValues())
            .setTemplates(providerNode.getProperty(NTF_TEMPLATES).getValues());
    return provider;
  }

  @Override
  public List<Provider> getAllProviders() {
    SessionProvider sProvider = CommonsUtils.getSystemSessionProvider();
    List<Provider> providers = new ArrayList<Provider>();
    try {
      Node providerHomeNode = getProviderHomeNode(sProvider);
      NodeIterator iterator = providerHomeNode.getNodes();
      while (iterator.hasNext()) {
        Node node = iterator.nextNode();
        if (node.isNodeType(NTF_PROVIDER)) {
          providers.add(getProvider(node));
        }
      }
    } catch (Exception e) {
      LOG.error("Can not save the Provider", e);
    }
    return providers;
  }

}
