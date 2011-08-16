/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.webui.commons;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 8 Apr 2011  
 */
@ComponentConfig(
  lifecycle = Lifecycle.class,
  template = "classpath:groovy/webui/commons/UIDocumentSelector.gtmpl",
  events = {
    @EventConfig(listeners = UIDocumentSelector.SelectFileActionListener.class),
    @EventConfig(listeners = UIDocumentSelector.SelectFolderActionListener.class)
  }  
 )
public class UIDocumentSelector extends UIContainer {
  
  protected static final Log    logger               = ExoLogger.getLogger(UIDocumentSelector.class);

  protected static final String SELECTFILE           = "SelectFile";

  protected static final String SELECTFOLDER         = "SelectFolder";

  protected static final String EMPTYCOMPONENT       = "EmptyComponent";

  protected static final String RESTPREFIX           = "/managedocument";

  protected static final String GETFOLDERSANDFILES   = RESTPREFIX + "/getFoldersAndFiles";

  protected static final String DELETEFOLDERORFILE   = RESTPREFIX + "/deleteFolderOrFile";

  protected static final String CREATEFOLDER         = RESTPREFIX + "/createFolder";

  protected static final String PARAM_WORKSPACE      = "workspaceName";

  protected static final String PARAM_NODEPATH       = "nodePath";

  protected static final String PARAM_PARENTPATH     = "parentPath";

  protected static final String PARAM_ISFOLDERONLY   = "isFolderOnly";

  protected static final String PARAM_FOLDERNAME     = "folderName";
  
  protected static final String DATA_ID              = "dataId";

  protected String              currentWorkspaceName = "";

  protected String              rootPath             = "/";

  protected String              seletedFile          = "";

  protected String              seletedFolder        = "";

  private boolean               allowAddFolder       = false;

  private boolean               allowDeleteItem      = false;

  
  public UIDocumentSelector() {
    try {
      RepositoryService jcrService_ = (RepositoryService) PortalContainer.getComponent(RepositoryService.class);
      ManageableRepository currentRepo = jcrService_.getCurrentRepository();
      currentWorkspaceName = currentRepo.getConfiguration().getDefaultWorkspaceName();
      this.addChild(UIContainer.class, null, EMPTYCOMPONENT);
    } catch (Exception e) {
      logger.debug("Can't init ui component UIDocumentSelector :  " + e.getMessage());
    }
  }
  
  public String getSeletedFile() {
    return seletedFile;
  }

  public void setSeletedFile(String seletedFile) {
    this.seletedFile = seletedFile;
  }

  public String getSeletedFolder() {
    return seletedFolder;
  }

  public void setSeletedFolder(String seletedFolder) {
    this.seletedFolder = seletedFolder;
  }

  protected String getRestContext() {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    if (!(context instanceof PortalRequestContext)) {
      context = (WebuiRequestContext) context.getParentAppRequestContext();
    }

    String requestURL = ((PortalRequestContext) context).getRequest().getRequestURL().toString();
    String portalName = PortalContainer.getCurrentPortalContainerName();
    String restContextName = PortalContainer.getCurrentRestContextName();
    StringBuilder sb = new StringBuilder();
    sb.append(requestURL.substring(0, requestURL.indexOf(portalName)))
      .append(portalName)
      .append("/")
      .append(restContextName);
    return sb.toString();
  }
  
  public boolean isAllowAddFolder() {
    return allowAddFolder;
  }
  public void setAllowAddFolder(boolean allowAddFolder) {
    this.allowAddFolder = allowAddFolder;
  }
  public boolean isAllowDeleteItem() {
    return allowDeleteItem;
  }
  public void setAllowDeleteItem(boolean allowDeleteItem) {
    this.allowDeleteItem = allowDeleteItem;
  }

  static public class SelectFileActionListener extends EventListener<UIDocumentSelector> {
    public void execute(Event<UIDocumentSelector> event) throws Exception {
      UIDocumentSelector component = event.getSource();
      component.seletedFile = event.getRequestContext().getRequestParameter(DATA_ID);
      event.getRequestContext()
           .addUIComponentToUpdateByAjax(component.getChildById(EMPTYCOMPONENT));
    }
  }

  static public class SelectFolderActionListener extends EventListener<UIDocumentSelector> {
    public void execute(Event<UIDocumentSelector> event) throws Exception {
      UIDocumentSelector component = event.getSource();
      component.seletedFolder = event.getRequestContext().getRequestParameter(DATA_ID);
      event.getRequestContext()
           .addUIComponentToUpdateByAjax(component.getChildById(EMPTYCOMPONENT));
    }
  }
}
