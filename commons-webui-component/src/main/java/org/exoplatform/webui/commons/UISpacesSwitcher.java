/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.PortalContainerInfo;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.web.application.Parameter;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by The eXo Platform SAS
 * Author : Tran Hung Phong
 *          phongth@exoplatform.com
 * Oct 23, 2012
 */
@ComponentConfig(
  lifecycle = Lifecycle.class, 
  template = "classpath:groovy/webui/commons/UISpaceSwitcher.gtmpl",
  events = {@EventConfig(listeners = UISpacesSwitcher.SelectSpaceActionListener.class)}
)
public class UISpacesSwitcher extends UIContainer {

  private static final Log LOG = ExoLogger.getLogger("org.exoplatform.webui.commons.UISpacesSwitcher");

  public static final String SPACE_ID_PARAMETER = "spaceId";
  
  public static final String SELECT_SPACE_ACTION = "SelectSpace";
  
  public static final long DEFAULT_INVALIDING_CACHE_TIME = 60000;
  
  private EventUIComponent eventComponent;
  
  private String currentSpaceName = StringUtils.EMPTY;
  
  protected long invalidingCacheTime;
  
  private boolean isShowPortalSpace = true;
  
  private boolean isShowUserSpace = true;
  
  private boolean isAutoResize = false;
  
  private String mySpaceLabel = null;
  
  private String portalSpaceLabel = null;
  
  public UISpacesSwitcher() throws Exception {
    String invalidingCacheTimeProperty = System.getProperty("commons.spaceswitcher.cache.interval");
    if ((invalidingCacheTimeProperty == null) || invalidingCacheTimeProperty.isEmpty()) {
      invalidingCacheTime = DEFAULT_INVALIDING_CACHE_TIME;
    } else {
      invalidingCacheTime = Long.parseLong(invalidingCacheTimeProperty);
    }
    initPortalSpaceLabel();
  }
  
  public void init(EventUIComponent eventComponent) {
    this.eventComponent = eventComponent;
  }
  
  public EventUIComponent getEventComponent() {
    return eventComponent;
  }
  
  public void setCurrentSpaceName(String currentSpaceName) {
    this.currentSpaceName = currentSpaceName;
  }
  
  public String getCurrentSpaceName() {
    if  (!StringUtils.isEmpty(portalSpaceLabel)) {
      PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
      if (portalRequestContext.getPortalOwner().equalsIgnoreCase(currentSpaceName)) {
        String spaceId = portalRequestContext.getRequestParameter(SPACE_ID_PARAMETER);
        if (isCurrentPortalWiki()) {
          if (StringUtils.isEmpty(spaceId)) {
            return upperFirstCharacter(portalSpaceLabel);
          } else {
            if (spaceId.startsWith("/" + getPortalName())) {
              return upperFirstCharacter(portalSpaceLabel);
            }
          }
        } else {
          if (!StringUtils.isEmpty(spaceId) && spaceId.startsWith("/" + getPortalName())) {
            return upperFirstCharacter(portalSpaceLabel);
          }
        }
      }
    }
    return upperFirstCharacter(currentSpaceName);
  }
  
  public String getMySpaceLabel() {
    if (!StringUtils.isEmpty(mySpaceLabel)) {
      return mySpaceLabel;
    }
    try{
    ResourceBundle bundle = RequestContext.getCurrentInstance().getApplicationResourceBundle();
    return bundle.getString("UISpaceSwitcher.title.my-space");
    }catch(MissingResourceException ex){
        return   "UISpaceSwitcher.title.my-space";

      }
  }
  
  public void setMySpaceLabel(String mySpaceLabel) {
    this.mySpaceLabel = mySpaceLabel;
  }
  
  public boolean isShowPortalSpace() {
    return isShowPortalSpace;
  }

  public void setShowPortalSpace(boolean isShowPortalSpace) {
    this.isShowPortalSpace = isShowPortalSpace;
  }

  public boolean isShowUserSpace() {
    return isShowUserSpace;
  }

  public void setShowUserSpace(boolean isShowUserSpace) {
    this.isShowUserSpace = isShowUserSpace;
  }
  
  public void setAutoResize(boolean isAutoResize) {
    this.isAutoResize = isAutoResize;
  }
  
  public boolean isAutoResize() {
    return isAutoResize;
  }

  public String getUsername() {
    try {
      ConversationState conversationState = ConversationState.getCurrent();
      return conversationState.getIdentity().getUserId();
    }catch(Exception e){
      return "system" ;
    }    
  }
  
  public String getPortalSpaceId() {
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    String portalOwner = portalRequestContext.getPortalOwner();
    String portalName = getPortalName();
    StringBuilder spaceId = new StringBuilder();
    spaceId.append("/");
    spaceId.append(portalName);
    spaceId.append("/");
    spaceId.append(portalOwner);
    return spaceId.toString();
  }
  
  private String getPortalName() {
    ExoContainer container = ExoContainerContext.getCurrentContainer() ; 
    PortalContainerInfo containerInfo = (PortalContainerInfo)container.getComponentInstanceOfType(PortalContainerInfo.class);
    return containerInfo.getContainerName();
  }

  public boolean isCurrentPortalWiki() {
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    SiteType portalType = portalRequestContext.getSiteType();
    if (!portalType.equals(SiteType.PORTAL)) {
      return false;
    } else {
      HttpServletRequest request = portalRequestContext.getRequest();
      String requestURL = request.getRequestURL().toString();
      UIPortal uiPortal = Util.getUIPortal();
      String pageNodeSelected = null;
      try
      {
        pageNodeSelected = uiPortal.getSelectedUserNode().getURI();
      } catch (Exception e)
      {
        LOG.error("An error occured while retrieving selected page node: ", e);
      }
      if (!requestURL.contains(pageNodeSelected)) {
        // Happens at the first time processRender() called when add wiki portlet manually
        requestURL = portalRequestContext.getPortalURI() + pageNodeSelected;
      }
      String wikiPageName;
      if (pageNodeSelected == null) {
        wikiPageName = "wiki";
      } else {
        wikiPageName = pageNodeSelected;
      }
      String uri = StringUtils.EMPTY;
      String sign1 = "/" + wikiPageName + "/";
      String sign2 = "/" + wikiPageName;
      if(requestURL.lastIndexOf(sign1) < 0){
        if(requestURL.lastIndexOf(sign2) > 0) {
          uri = requestURL.substring(requestURL.lastIndexOf(sign2) + sign2.length()) ;
        }
      } else{
        uri = requestURL.substring(requestURL.lastIndexOf(sign1) + sign1.length()) ;
      }
      if(uri != null && uri.length() > 0 && (uri.lastIndexOf("/") + 1) == uri.length())
        uri = uri.substring(0, uri.lastIndexOf("/")) ;

      if(uri.indexOf("/") > 0) {
        String[] array = uri.split("/");
        if (array[0].equals(PortalConfig.USER_TYPE)) {
          return false;
        }
      }
      return true;
    }
  }

  public String getPortalSpaceLabel() {
    if (!StringUtils.isEmpty(portalSpaceLabel)) {
      return upperFirstCharacter(portalSpaceLabel);
    }
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    return upperFirstCharacter(portalRequestContext.getPortalOwner());
  }

  public void setPortalSpaceLabel(String portalSpaceLabel) {
    this.portalSpaceLabel = portalSpaceLabel;
  }

  private void initPortalSpaceLabel() throws Exception {
    String portalLabel;
    PortalRequestContext requestContext = Util.getPortalRequestContext();
    UserPortalConfig userPortalConfig = requestContext.getUserPortalConfig();
    PortalConfig portalConfig = userPortalConfig.getPortalConfig();
    if (portalConfig.getType().equals("portal")) {
      portalLabel = portalConfig.getLabel();
    } else {
      String currentPortalName = requestContext.getPortalOwner();
      UserPortalConfigService userPortalConfigService = getApplicationComponent(UserPortalConfigService.class);
      DataStorage dataStorage = userPortalConfigService.getDataStorage();
      portalConfig = dataStorage.getPortalConfig(currentPortalName);
      portalLabel = portalConfig.getLabel();
    }
    if (!StringUtils.isEmpty(portalLabel)) {
      this.portalSpaceLabel = portalLabel;
    }
  }

  private String upperFirstCharacter(String str) {
    if (StringUtils.isEmpty(str)) {
      return str;
    }
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }

  protected String getBaseRestUrl() {
    StringBuilder sb = new StringBuilder();
    sb.append("/").append(PortalContainer.getCurrentPortalContainerName());
    sb.append("/").append(PortalContainer.getCurrentRestContextName());
    return sb.toString();
  }
  
  protected String getSocialBaseRestUrl() {
    StringBuilder sb = new StringBuilder();
    sb.append("/").append(PortalContainer.getCurrentRestContextName());
    sb.append("/").append("private");
    sb.append("/").append(PortalContainer.getCurrentPortalContainerName());
    return sb.toString();
  }
  
  protected String createSelectSpaceEvent(String spaceId) throws Exception {
    Parameter parameter = new Parameter(SPACE_ID_PARAMETER, spaceId);
    return event(SELECT_SPACE_ACTION, null, new Parameter[] {parameter});
  }
  
  public static class SelectSpaceActionListener extends EventListener<UISpacesSwitcher> {
    public void execute(Event<UISpacesSwitcher> event) throws Exception {
      WebuiRequestContext context = event.getRequestContext();
      UISpacesSwitcher spaceSwitcher = event.getSource();
      UIPortletApplication root = spaceSwitcher.getAncestorOfType(UIPortletApplication.class);
      EventUIComponent eventComponent = spaceSwitcher.getEventComponent();
      UIComponent uiComponent = null;
      if (eventComponent.getId() != null) {
        uiComponent = (UIComponent) root.findComponentById(eventComponent.getId());
      } else {
        uiComponent = root;
      }
      String eventName = eventComponent.getEventName();
      Event<UIComponent> xEvent = uiComponent.createEvent(eventName, Event.Phase.PROCESS, context);
      if (xEvent != null) {
        xEvent.broadcast();
      }
    }
  }
}
