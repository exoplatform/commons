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

import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.PortalContainerInfo;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 5 May 2011  
 */
@ComponentConfig(
  lifecycle = Lifecycle.class,
  template = "classpath:groovy/webui/commons/UIAddAttachment.gtmpl",
  events = {
    @EventConfig(listeners = UIAddAttachment.CancelActionListener.class),
    @EventConfig(listeners = UIAddAttachment.AttachFileActionListener.class),
    @EventConfig(listeners = UIAddAttachment.AttachLinkActionListener.class)
  }  
)
public class UIAddAttachment extends UIContainer implements UIPopupComponent {
  
  private static final Log      LOG                = ExoLogger.getLogger(UIAddAttachment.class);

  protected static final String UIDOCUMENTSELECTOR = "UIDocumentSelector";

  protected static final String CANCEL             = "Cancel";

  protected static final String ATTACH             = "AttachFile";

  protected static final String ATTACHLINK         = "AttachLink";

  public static final String    SELECTEDFILE       = "selectedFile";

  public static final String    ISATTACHFILE       = "isAttachFile";

  private EventUIComponent      targetAttachEvent;
  
  public UIAddAttachment() {
    try {
      addChild(UIDocumentSelector.class, null, UIDOCUMENTSELECTOR);
    } catch (Exception e) {  //UIComponent.addChild() throws Exception()
      LOG.error("An exception happens when init UIAddAttachment", e);
    }
  }
  
  public EventUIComponent getTargetAttachEvent() {
    return targetAttachEvent;
  }

  public void setTargetAttachEvent(EventUIComponent targetAttachEvent) {
    this.targetAttachEvent = targetAttachEvent;
  }

  static public class CancelActionListener extends EventListener<UIAddAttachment> {
    public void execute(Event<UIAddAttachment> event) throws Exception {
      UIPopupWindow uiPopupWindow = event.getSource().getParent();
      uiPopupWindow.setUIComponent(null);
      uiPopupWindow.setRendered(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupWindow.getParent());
    }
  }

  static public class AttachFileActionListener extends GenericAttachActionListener {
    public void processEvent(Event<UIAddAttachment> event) throws Exception {
      event.getRequestContext().setAttribute(SELECTEDFILE, getSelectedFile(event));
      event.getRequestContext().setAttribute(ISATTACHFILE, true);
    }

  }

  static public class AttachLinkActionListener extends GenericAttachActionListener {
    public void processEvent(Event<UIAddAttachment> event) throws Exception {
      UIAddAttachment component = event.getSource();
      event.getRequestContext().setAttribute(SELECTEDFILE,
                                             component.getFileURL(getSelectedFile(event)));
      event.getRequestContext().setAttribute(ISATTACHFILE, false);
    }
  }
  
  static public class GenericAttachActionListener extends EventListener<UIAddAttachment> {


    public String getSelectedFile(Event<UIAddAttachment> event) throws Exception {
      return event.getSource().getChild(UIDocumentSelector.class).getSeletedFile();
    }
    
    public void execute(Event<UIAddAttachment> event) throws Exception {      
      UIAddAttachment component = event.getSource();
      EventUIComponent eventUIComponent = component.getTargetAttachEvent();
      UIPortletApplication portlet = component.getAncestorOfType(UIPortletApplication.class);
      UIComponent targerComponent = portlet.findComponentById(eventUIComponent.getId());
      Event<UIComponent> xEvent = targerComponent.createEvent(eventUIComponent.getEventName(),
                                                              Event.Phase.PROCESS,
                                                              event.getRequestContext());
      if (!StringUtils.isEmpty(getSelectedFile(event))) {
        processEvent(event);
      } else {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAddAttachment.msg.not-a-file",
                                                                                       null,
                                                                                       ApplicationMessage.WARNING));
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
        return;
      }
      if (xEvent != null) {
        xEvent.broadcast();
      }
      UIPopupWindow uiPopupWindow = event.getSource().getParent();
      uiPopupWindow.setRendered(false);
      uiPopupWindow.setUIComponent(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupWindow.getParent());
    }
    
    public void processEvent(Event<UIAddAttachment> event) throws Exception {
    }
    
  }
  
  public String getFileURL(String path) throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    if (!(context instanceof PortalRequestContext)) {
      context = (WebuiRequestContext) context.getParentAppRequestContext();
    }
    String portalName = getPortalName();
    String requestURL = ((PortalRequestContext) context).getRequest().getRequestURL().toString();
    String domainURL = requestURL.substring(0, requestURL.indexOf(portalName));
    Session session = getCurrentSession();
    String workspace = session.getWorkspace().getName();
    String repository = ((ManageableRepository) session.getRepository()).getConfiguration()
                                                                        .getName();

    String url = domainURL + portalName + "/" + PortalContainer.getCurrentRestContextName()
        + "/jcr/" + repository + "/" + workspace + path;
    return url;
  }
  
  
  public Session getCurrentSession() throws Exception {
    RepositoryService repoService = (RepositoryService) PortalContainer.getInstance()
                                                                       .getComponentInstanceOfType(RepositoryService.class);
    String defaultWorkspace = repoService.getCurrentRepository()
                                         .getConfiguration()
                                         .getDefaultWorkspaceName();
    return repoService.getDefaultRepository().getSystemSession(defaultWorkspace);
  }
  
  private String getPortalName() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    PortalContainerInfo containerInfo = (PortalContainerInfo) container.getComponentInstanceOfType(PortalContainerInfo.class);
    return containerInfo.getContainerName();
  }

  @Override
  public void activate() throws Exception {
  }

  @Override
  public void deActivate() throws Exception {
  }
}
