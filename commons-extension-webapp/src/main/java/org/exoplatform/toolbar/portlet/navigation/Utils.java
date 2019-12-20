package org.exoplatform.toolbar.portlet.navigation;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.page.UIPageBody;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.NavigationUtils;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.*;

public class Utils {
  private static Log LOG = ExoLogger.getLogger(Utils.class);

  /** The Quick edit attribute for HTTPSession */
  public static final String TURN_ON_QUICK_EDIT = "turnOnQuickEdit";

  private Utils() {
  }

  public static UserNavigation getSelectedNavigation() throws Exception {
    SiteKey siteKey = Util.getUIPortal().getSiteKey();
    return NavigationUtils.getUserNavigation(
                                             Util.getPortalRequestContext().getUserPortalConfig().getUserPortal(),
                                             siteKey);
  }

  /**
   * Creates the popup window. Each portlet have a <code>UIPopupContainer</code>
   * . <br>
   * Every <code>UIPopupWindow</code> created by this method is belong to this
   * container.
   *
   * @param container the current container
   * @param component the component which will be display as a popup
   * @param popupWindowId the popup's ID
   * @param width the width of the popup
   * @throws Exception the exception
   */
  public static void createPopupWindow(UIContainer container,
                                       UIComponent component,
                                       String popupWindowId,
                                       int width) throws Exception {
    UIPopupContainer popupContainer = initPopup(container, component, popupWindowId, width);
    WebuiRequestContext requestContext = RequestContext.getCurrentInstance();
    requestContext.addUIComponentToUpdateByAjax(popupContainer);
  }

  private static UIPopupContainer initPopup(UIContainer container,
                                            UIComponent component,
                                            String popupWindowId,
                                            int width) throws Exception {
    UIPopupContainer popupContainer = getPopupContainer(container);
    popupContainer.removeChildById(popupWindowId);
    popupContainer.removeChildById("UIPopupWindow");
    UIPopupWindow popupWindow = popupContainer.addChild(UIPopupWindow.class, null, popupWindowId);
    popupWindow.setUIComponent(component);
    popupWindow.setWindowSize(width, 0);
    popupWindow.setShow(true);
    popupWindow.setRendered(true);
    popupWindow.setResizable(true);
    popupWindow.setShowMask(true);
    return popupContainer;
  }

  private static UIPopupContainer getPopupContainer(UIContainer container) {
    if (container instanceof UIPortletApplication)
      return container.getChild(UIPopupContainer.class);
    UIPortletApplication portletApplication = container.getAncestorOfType(UIPortletApplication.class);
    return portletApplication.getChild(UIPopupContainer.class);
  }

  public static boolean hasEditPermissionOnNavigation() throws Exception {
    UserNavigation selectedNavigation = getSelectedNavigation();
    if (selectedNavigation == null)
      return false;
    return selectedNavigation.isModifiable();
  }

  public static boolean hasEditPermissionOnPortal() throws Exception {
    UIPortal currentUIPortal = Util.getUIPortal();
    UserACL userACL = currentUIPortal.getApplicationComponent(UserACL.class);
    return userACL.hasEditPermissionOnPortal(currentUIPortal.getSiteKey().getTypeName(),
                                             currentUIPortal.getSiteKey().getName(),
                                             currentUIPortal.getEditPermission());
  }

  public static boolean hasEditPermissionOnPage() throws Exception {
    try {
      UIPortalApplication portalApp = Util.getUIPortalApplication();
      UIWorkingWorkspace uiWorkingWS = portalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
      UIPageBody pageBody = uiWorkingWS.findFirstComponentOfType(UIPageBody.class);
      if (pageBody == null) {
        return false;
      }
      UIPage uiPage = (UIPage) pageBody.getUIComponent();
      UserACL userACL = portalApp.getApplicationComponent(UserACL.class);
      if (uiPage != null) {
        return userACL.hasEditPermissionOnPage(uiPage.getOwnerType(),
                                               uiPage.getOwnerId(),
                                               uiPage.getEditPermission());
      }
      UIPortal currentUIPortal = portalApp.<UIWorkingWorkspace> findComponentById(UIPortalApplication.UI_WORKING_WS_ID)
                                          .findFirstComponentOfType(UIPortal.class);
      UserNode currentNode = currentUIPortal.getSelectedUserNode();
      PageKey pageReference = currentNode.getPageRef();
      if (pageReference == null) {
        return false;
      }
      UserPortalConfigService portalConfigService = portalApp.getApplicationComponent(UserPortalConfigService.class);
      PageContext page = portalConfigService.getPage(pageReference);
      if (page == null) {
        return false;
      }
      return userACL.hasEditPermission(page);
    } catch (Exception e) {
      LOG.warn("Error while retrieving permission of used on current page", e);
      return false;
    }
  }

}
