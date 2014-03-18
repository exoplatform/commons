/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.notification;

import org.exoplatform.commons.api.notification.service.setting.PluginContainer;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.testing.BaseCommonsTestCase;

public class PluginContainerTest extends BaseCommonsTestCase {
  
  private PluginContainer container;
  public PluginContainerTest() {
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    container = getService(PluginContainer.class);
    assertNotNull(container);
    
  }
  
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }
  
  public void testRenderPlugin() throws Exception {
    TemplateContext ctx = new TemplateContext("DigestDailyPlugin", null);
    ctx.put("FIRSTNAME", "User ROOT");
    ctx.put("USER", "root");
    ctx.put("ACTIVITY", "Content of Activity");
    String s = TemplateUtils.processGroovy(ctx);
    // check process resource-bundle
    assertEquals(true, s.indexOf("Test resource bundle.") > 0);
    // check process Groovy
    assertEquals(true, s.indexOf("Content of Activity") > 0);
  }
}