/**
 * Copyright (C) 2013 eXo Platform SAS.
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
package org.exoplatform.platform.common.service.plugin;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.platform.common.service.MenuConfiguratorService;
import org.exoplatform.portal.config.model.*;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * @author <a href="hzekri@exoplatform.com">hzekri</a>
 */
public class MenuConfiguratorAddNodePlugin extends BaseComponentPlugin {
  private ConfigurationManager    configurationManager;

  private MenuConfiguratorService menuConfiguratorService;

  private String                  navPath;

  private static final String     EXTENDED_SETUP_NAVIGATION_FILE = "extended.setup.navigation.file";

  private static final Log        LOG                            = ExoLogger.getLogger(MenuConfiguratorAddNodePlugin.class);

  public MenuConfiguratorAddNodePlugin(InitParams initParams,
                                       ConfigurationManager configurationManager,
                                       MenuConfiguratorService menuConfiguratorService) {
    this.configurationManager = configurationManager;
    this.menuConfiguratorService = menuConfiguratorService;
    if (initParams.containsKey(EXTENDED_SETUP_NAVIGATION_FILE)) {
      navPath = initParams.getValueParam(EXTENDED_SETUP_NAVIGATION_FILE).getValue();
    }
  }

  public void execute() {
    NavigationFragment extendedFragment = null;
    if (navPath != null && !navPath.isEmpty()) {
      try {
        UnmarshalledObject<PageNavigation> extendedObj = ModelUnmarshaller.unmarshall(PageNavigation.class,
                                                                                      configurationManager.getInputStream(navPath));
        PageNavigation extendedPageNav = extendedObj.getObject();
        List<PageNode> setupPageNodes = menuConfiguratorService.getSetupMenuOriginalPageNodes();
        extendedFragment = extendedPageNav.getFragment();
        for (PageNode extendedPageNode : extendedFragment.getNodes()) {
          insertExtendedNodes(extendedPageNode, setupPageNodes);
        }
      } catch (Exception E) {
        LOG.error("Can not load or read the file with path " + navPath + " Please check the path or the file structure ", E);
      }
    } else {
      LOG.warn("Path for extended setup navigation file not mentioned");
    }
  }

  private void insertExtendedNodes(PageNode nodeToInsert, List<PageNode> existingNodes) {
    if (existingNodes == null) {
      return;
    }
    PageNode existingPageNode = existingNodes.stream()
                                             .filter(pageNode -> StringUtils.equals(pageNode.getName(),
                                                                                    nodeToInsert.getName()))
                                             .findFirst()
                                             .orElse(null);
    if (existingPageNode == null) {
      existingNodes.add(nodeToInsert);
    } else {
      existingPageNode.setPageReference(nodeToInsert.getPageReference());
      existingPageNode.setLabel(nodeToInsert.getLabel());
      List<PageNode> children = nodeToInsert.getChildren();
      if (children != null && !children.isEmpty()) {
        for (PageNode pageNodeToInsert : children) {
          insertExtendedNodes(pageNodeToInsert, existingPageNode.getChildren());
        }
      }
    }
  }
}
