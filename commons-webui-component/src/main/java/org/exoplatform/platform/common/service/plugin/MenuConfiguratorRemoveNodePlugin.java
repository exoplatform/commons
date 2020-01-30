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

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.platform.common.service.MenuConfiguratorService;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * @author <a href="hzekri@exoplatform.com">hzekri</a>
 */
public class MenuConfiguratorRemoveNodePlugin extends BaseComponentPlugin {
  private MenuConfiguratorService menuConfiguratorService;

  private PageNode                targetNode;

  private static final String     TARGET_NODE = "node.config";

  private static final Log        LOG         = ExoLogger.getLogger(MenuConfiguratorRemoveNodePlugin.class);

  public MenuConfiguratorRemoveNodePlugin(MenuConfiguratorService menuConfiguratorService, InitParams initParams) {
    this.menuConfiguratorService = menuConfiguratorService;
    if (initParams.containsKey(TARGET_NODE)) {
      targetNode = (PageNode) initParams.getObjectParam(TARGET_NODE).getObject();
    }

  }

  public void execute() {
    if (targetNode != null) {
      List<PageNode> setupPageNodes = menuConfiguratorService.getSetupMenuOriginalPageNodes();
      boolean isRemoved = removeTargetNode(targetNode, setupPageNodes);
      if (!isRemoved) {
        LOG.debug("Setup menu entry with name '{}' and page reference '{}' not found",
                  targetNode.getName(),
                  targetNode.getPageReference());
      }
    } else {
      LOG.warn("No node removed : target node should be specified");
    }
  }

  private boolean removeTargetNode(PageNode targetNode, List<PageNode> setupPageNodes) {
    Iterator<PageNode> setupPagesIterator = setupPageNodes.iterator();
    boolean removed = false;
    while (setupPagesIterator.hasNext()) {
      PageNode pageNode = setupPagesIterator.next();
      if (StringUtils.equals(pageNode.getName(), targetNode.getName())
          && StringUtils.equals(pageNode.getPageReference(), targetNode.getPageReference())) {
        setupPagesIterator.remove();
        removed = true;
        break;
      }
    }
    return removed;
  }
}
