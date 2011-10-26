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

import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 5 May 2011  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "classpath:groovy/webui/commons/UISaveAttachment.gtmpl",
  events = {
    @EventConfig(phase = Phase.DECODE, listeners = UISaveAttachment.CancelActionListener.class),
    @EventConfig(listeners = UISaveAttachment.SaveFileActionListener.class)      
  }  
)
public class UISaveAttachment extends UIForm implements UIPopupComponent {
  
  protected static Log log = ExoLogger.getLogger(UISaveAttachment.class);
  
  protected static final String UIDOCUMENTSELECTOR = "UIDocumentSelector";

  protected static final String FIELD_INPUT        = "FileNameInput";

  protected static final String CANCEL             = "Cancel";

  protected static final String SAVEFILE           = "SaveFile";

  private String                filePath           = "";
  
  private String                fileName           = "";
  
  private static final String   invalidCharacters  = ": @ / \\ | ^ # ; [ ] { } < > * ' \" + ? &";
  
  public UISaveAttachment() {
    try {
      addUIFormInput(new UIFormStringInput(FIELD_INPUT, null, null));
      UIDocumentSelector documentSelector = addChild(UIDocumentSelector.class, null, UIDOCUMENTSELECTOR);
      documentSelector.setAllowAddFolder(true);
      documentSelector.setAllowDeleteItem(true);
    } catch (Exception e) {
      log.error("An exception happens when init UISaveAttachment", e);
    }
  }
  
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    getUIStringInput(FIELD_INPUT).setValue(fileName);
    super.processRender(context);
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  static public class CancelActionListener extends EventListener<UISaveAttachment> {
    public void execute(Event<UISaveAttachment> event) throws Exception {
      UIPopupWindow uiPopupWindow = event.getSource().getParent();
      uiPopupWindow.setUIComponent(null);
      uiPopupWindow.setRendered(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupWindow.getParent());
    }
  }

  static public class SaveFileActionListener extends EventListener<UISaveAttachment> {
    public void execute(Event<UISaveAttachment> event) throws Exception {
      UISaveAttachment component = event.getSource();
      UIDocumentSelector selector = component.getChildById(UIDOCUMENTSELECTOR);
      UIFormStringInput nameInput = component.getChildById(FIELD_INPUT);
      String fileName = nameInput.getValue();
      String tempPath = component.filePath.substring(1);
      String workspaceName = tempPath.substring(0, tempPath.indexOf("/"));
      if (fileName == null || fileName.trim().length() == 0) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UISaveAttachment.msg.file-name-not-null",
                                                                                       null,
                                                                                       ApplicationMessage.WARNING));
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).setFullRender(true);
        return;
      } else {
        String nodePath = tempPath.substring(tempPath.indexOf("/"));
        Session srcSession = component.getSession(workspaceName);
        Node srcNode = (Node) srcSession.getItem(nodePath);
        Node srcContent = srcNode.getNode("jcr:content");
        Value value = srcContent.getProperty("jcr:data").getValue();
        String mimeType = srcContent.getProperty("jcr:mimeType").getString();
        srcSession.logout();
        Session desSession = component.getDefaultSession();
        String selectedFolder = selector.getSeletedFolder();
        if (StringUtils.isEmpty(selectedFolder)) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UISaveAttachment.msg.not-a-folder",
                                                                                         null,
                                                                                         ApplicationMessage.WARNING));
          ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).setFullRender(true);
          return;
        }
        Node desNode = (Node) desSession.getItem(selector.getSeletedFolder());
        try {
          validate(fileName);
        } catch (IllegalNameException e) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UISaveAttachment.msg.not-valid-name",
                                                  new String[] { invalidCharacters },
                                                  ApplicationMessage.WARNING));
          ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).setFullRender(true);
          return;
        }
        Node file = desNode.addNode(fileName, "nt:file");
        Node jcrContent = file.addNode("jcr:content", "nt:resource");
        jcrContent.setProperty("jcr:data", value);
        jcrContent.setProperty("jcr:lastModified", new GregorianCalendar());
        jcrContent.setProperty("jcr:mimeType", mimeType);
        desSession.save();
        desSession.logout();

        UIPopupWindow uiPopupWindow = event.getSource().getParent();
        uiPopupWindow.setUIComponent(null);
        uiPopupWindow.setRendered(false);
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UISaveAttachment.msg.saved-successfully",
                                                                                       null,
                                                                                       ApplicationMessage.INFO));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupWindow.getParent());
      }
    }
  }
  
  public Session getSession(String workspace) throws Exception {
    ManageableRepository repository = getCurrentRepository();
    return repository.getSystemSession(workspace);
  }
  
  public Session getDefaultSession() throws Exception {
    ManageableRepository repository = getCurrentRepository();
    String defaultWorkspace = repository.getConfiguration().getDefaultWorkspaceName();
    return repository.getSystemSession(defaultWorkspace);
  }
  
  private ManageableRepository getCurrentRepository() throws RepositoryException {
    RepositoryService repoService = (RepositoryService) PortalContainer.getInstance()
                                                                       .getComponentInstanceOfType(RepositoryService.class);
    return repoService.getCurrentRepository();
  }
  
  public static void validate(String s) throws IllegalNameException {
    StringTokenizer tokens;
    if (s == null || s.trim().length() == 0) {
      throw new IllegalNameException();
    }
    for (int i = 0; i < s.length(); i++) {
      tokens = new StringTokenizer(invalidCharacters);
      char c = s.charAt(i);
      boolean isInvalid = false;
      while (tokens.hasMoreTokens()) {
        String test = tokens.nextToken();
        isInvalid = test.equals(String.valueOf(c));
        if (isInvalid == true)
          break;
      }
      if (Character.isLetter(c) || Character.isDigit(c) || (!isInvalid)) {
        continue;
      } else {
        throw new IllegalNameException(invalidCharacters);
      }
    }     
  }  
  
  @Override
  public void activate() throws Exception {
  }

  @Override
  public void deActivate() throws Exception {
  }
}
