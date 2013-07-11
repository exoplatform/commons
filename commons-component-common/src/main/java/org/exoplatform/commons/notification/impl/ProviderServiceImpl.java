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
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.exoplatform.commons.api.notification.ProviderData;
import org.exoplatform.commons.api.notification.plugin.ProviderModel;
import org.exoplatform.commons.api.notification.plugin.ProviderPlugin;
import org.exoplatform.commons.api.notification.service.ProviderService;
import org.exoplatform.commons.notification.AbstractService;
import org.exoplatform.commons.notification.NotificationConfiguration;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

public class ProviderServiceImpl extends AbstractService implements ProviderService, Startable {
  private static final Log LOG = ExoLogger.getLogger(ProviderServiceImpl.class);
  
  private List<ProviderPlugin>       providerPlugins       = new ArrayList<ProviderPlugin>();

  private String                     workspace;

  public ProviderServiceImpl(NotificationConfiguration configuration) {
    this.workspace = configuration.getWorkspace();
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
        ProviderData provider = new ProviderData();
        provider.setType(pm.getType());
        provider.setOrder(Integer.valueOf(pm.getOrder()));

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
  public void saveProvider(ProviderData provider) {
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
      providerNode.setProperty(NTF_ORDER, provider.getOrder());
      
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
  public ProviderData getProvider(String providerType) {
    SessionProvider sProvider = CommonsUtils.getSystemSessionProvider();
    try {
      Node providerHomeNode = getProviderHomeNode(sProvider);
      //
      return getProvider(providerHomeNode.getNode(providerType));
    } catch (Exception e) {
      LOG.error("Can not get the Provider", e);
    }
    return null;
  }

  private ProviderData getProvider(Node providerNode) throws Exception {
    if (providerNode == null) {
      return null;
    }
    ProviderData provider = new ProviderData();
    provider.setType(providerNode.getProperty(NTF_TYPE).getString())
            .setOrder(Integer.valueOf(providerNode.getProperty(NTF_ORDER).getString()));
    return provider;
  }

  @Override
  public List<ProviderData> getAllProviders() {
    SessionProvider sProvider = CommonsUtils.getSystemSessionProvider();
    List<ProviderData> providers = new ArrayList<ProviderData>();
    try {
      Node providerHomeNode = getProviderHomeNode(sProvider);
      StringBuffer queryBuffer = new StringBuffer(JCR_ROOT);
      queryBuffer.append(providerHomeNode.getPath()).append("//element(*,").append(NTF_PROVIDER)
                 .append(") order by @").append(NTF_ORDER).append(ASCENDING);

      QueryManager qm = providerHomeNode.getSession().getWorkspace().getQueryManager();
      Query query = qm.createQuery(queryBuffer.toString(), Query.XPATH);
      NodeIterator iterator = query.execute().getNodes();
      while (iterator.hasNext()) {
        Node node = iterator.nextNode();
        if (node.isNodeType(NTF_PROVIDER)) {
          providers.add(getProvider(node));
        }
      }
    } catch (Exception e) {
      LOG.error("Can not get all the Providers", e);
    }
    return providers;
  }

}
