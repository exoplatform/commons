/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.webui.ext.filter.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.ext.UIExtension;
import org.exoplatform.webui.ext.UIExtensionManager;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.webui.ext.impl.UIExtensionManagerImpl;
import org.exoplatform.webui.ext.impl.UIExtensionManagerTest.UIExtensionFilterException4;
import org.exoplatform.webui.ext.impl.UIExtensionManagerTest.UIExtensionFilterTrue;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com May
 * 29, 2012
 */
public class UserACLFilerTest extends TestCase {
  private UIExtension uiExtensionPrivate;

  private UIExtension uiExtensionPublic;

  public void setUp() throws Exception {
    StandaloneContainer.setConfigurationURL(Thread.currentThread()
                                                  .getContextClassLoader()
                                                  .getResource("conf/standalone/test-extension-configuration.xml")
                                                  .toString());
    StandaloneContainer container = StandaloneContainer.getInstance(Thread.currentThread()
                                                                          .getContextClassLoader());
    UIExtensionManagerImpl manager = (UIExtensionManagerImpl) container.getComponentInstanceOfType(UIExtensionManager.class);

    this.uiExtensionPrivate = manager.getUIExtension(MyOwner.class.getName(), "private-extension");

    this.uiExtensionPublic = manager.getUIExtension(MyOwner.class.getName(), "public-extension");

  }

  public void testAccept() {

    UIExtensionFilter userACLFilterPrivate = uiExtensionPrivate.getExtendedFilters().get(0);

    UIExtensionFilter userACLFilterPublic = uiExtensionPublic.getExtendedFilters().get(0);

    try {
      // Set current user as a GUEST
      ConversationState state = new ConversationState(VUser.getGuest());
      ConversationState.setCurrent(state);

      // Test filter
      assertEquals(false, userACLFilterPrivate.accept(new HashMap<String, Object>()));
      assertEquals(true, userACLFilterPublic.accept(new HashMap<String, Object>()));

    } catch (Exception e) {
      fail();
    }

    try {
      // Set current user as ROOT
      ConversationState state = new ConversationState(VUser.getRoot());
      ConversationState.setCurrent(state);

      // Test filter
      assertEquals(true, userACLFilterPrivate.accept(new HashMap<String, Object>()));
      assertEquals(true, userACLFilterPublic.accept(new HashMap<String, Object>()));

    } catch (Exception e) {
      fail();
    }

  }

  // ===========SIMULATION OBJECTS==========/

  /*
   * Virtual user
   */
  private static class VUser {

    public static Identity getRoot() {
      return new Identity("root");
    }

    public static Identity getJohn() {

      return new Identity("john",
                          Arrays.asList(new MembershipEntry[] { new MembershipEntry("/platform/administrators",
                                                                                    "*") }));
    }

    public static Identity getGuest() {
      return new Identity(IdentityConstants.ANONIM);
    }
  }

  /*
   * WebUI UIContainer
   */
  public static class MyOwner extends UIContainer {

  }

  /*
   * Extension Component
   */
  public static class MyUIExtensionComponent extends UIComponent {
    @UIExtensionFilters
    public List<UIExtensionFilter> getFilterTests() {
      return null;
    }
  }

}
