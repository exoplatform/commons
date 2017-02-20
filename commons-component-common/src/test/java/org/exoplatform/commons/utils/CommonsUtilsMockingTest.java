/*
 * Copyright (C) 2015 eXo Platform SAS.
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

package org.exoplatform.commons.utils;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.webui.util.Util;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Test class for {@link CommonsUtils}
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({CommonsUtils.class, Util.class})
public class CommonsUtilsMockingTest {

  @Test
  public void testShouldReturnDefaultPortalSite() {
    UserPortalConfigService userPortalConfig = mock(UserPortalConfigService.class);
    when(userPortalConfig.getDefaultPortal()).thenReturn("intranet");

    PowerMockito.mockStatic(CommonsUtils.class);
    when(CommonsUtils.getService(UserPortalConfigService.class)).thenReturn(userPortalConfig);
    when(CommonsUtils.getCurrentSite()).thenCallRealMethod();

    SiteKey site = CommonsUtils.getCurrentSite();
    assertEquals("intranet", site.getName());
    assertEquals(SiteType.PORTAL, site.getType());
  }

  @Test
  public void testShouldReturnCurrentSite() {
    UserPortalConfigService userPortalConfig = mock(UserPortalConfigService.class);
    when(userPortalConfig.getDefaultPortal()).thenReturn("intranet");

    PortalRequestContext requestContext = mock(PortalRequestContext.class);

    PowerMockito.mockStatic(Util.class);
    when(Util.getPortalRequestContext()).thenReturn(requestContext);

    PowerMockito.mockStatic(CommonsUtils.class);
    when(CommonsUtils.getService(UserPortalConfigService.class)).thenReturn(userPortalConfig);
    when(CommonsUtils.getCurrentSite()).thenCallRealMethod();

    when(requestContext.getSiteKey()).thenReturn(SiteKey.portal("test_site"));
    SiteKey site = CommonsUtils.getCurrentSite();
    assertEquals("test_site", site.getName());
    assertEquals(SiteType.PORTAL, site.getType());

    when(requestContext.getSiteKey()).thenReturn(SiteKey.group("group_site"));
    site = CommonsUtils.getCurrentSite();
    assertEquals("group_site", site.getName());
    assertEquals(SiteType.GROUP, site.getType());

    when(requestContext.getSiteKey()).thenReturn(SiteKey.user("user_site"));
    site = CommonsUtils.getCurrentSite();
    assertEquals("user_site", site.getName());
    assertEquals(SiteType.USER, site.getType());
  }
}
