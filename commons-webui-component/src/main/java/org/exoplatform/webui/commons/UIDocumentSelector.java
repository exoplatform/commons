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

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
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
  
  protected static final String UPLOAD_AREA            = "UPLOAD_AREA";
  
  protected static final String SELECT_FILE            = "SelectFile";

  protected static final String SELECT_FOLDER          = "SelectFolder";

  protected static final String REST_PREFIX           = "/managedocument";

  protected static final String GET_DRIVES           = REST_PREFIX + "/getDrives";

  protected static final String GET_FOLDERS_AND_FILES = REST_PREFIX + "/getFoldersAndFiles";

  protected static final String DELETE_FOLDER_OR_FILE = REST_PREFIX + "/deleteFolderOrFile";

  protected static final String CREATE_FOLDER         = REST_PREFIX + "/createFolder";

  protected static final String PARAM_DRIVE_TYPE     = "driveType";

  protected static final String PARAM_DRIVE_NAME     = "driveName";

  protected static final String PARAM_WORKSPACE_NAME  = "workspaceName";

  protected static final String PARAM_CURRENT_FOLDER  = "currentFolder";

  protected static final String PARAM_IS_FOLDER_ONLY  = "isFolderOnly";

  protected static final String PARAM_FOLDER_NAME     = "folderName";

  protected static final String PARAM_ITEM_PATH       = "itemPath";

  protected static final String DATA_ID               = "dataId";

  protected String              seletedFile           = "";

  protected String              seletedFolder         = "";
  
  private boolean               isShowUpload          = true;
  
  public UIDocumentSelector() throws Exception {
    super();
    addChild(UIUploadArea.class, null, UPLOAD_AREA);
  }

  public boolean isShowUpload() {
    return isShowUpload;
  }

  public void setShowUpload(boolean isShowUpload) {
    this.isShowUpload = isShowUpload;
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

  static public class SelectFileActionListener extends EventListener<UIDocumentSelector> {
    public void execute(Event<UIDocumentSelector> event) throws Exception {
      UIDocumentSelector component = event.getSource();
      component.seletedFile = event.getRequestContext().getRequestParameter(DATA_ID);
      component.seletedFolder = StringUtils.EMPTY;
      ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
    }
  }

  static public class SelectFolderActionListener extends EventListener<UIDocumentSelector> {
    public void execute(Event<UIDocumentSelector> event) throws Exception {
      UIDocumentSelector component = event.getSource();
      component.seletedFolder = event.getRequestContext().getRequestParameter(DATA_ID);
      component.seletedFile = StringUtils.EMPTY;
      ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
    }
  }
}
