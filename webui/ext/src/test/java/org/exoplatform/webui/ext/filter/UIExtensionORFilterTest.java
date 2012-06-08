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
package org.exoplatform.webui.ext.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.exoplatform.webui.ext.filter.impl.FileFilter;
import org.exoplatform.webui.ext.filter.impl.UserACLFilter;

/**
 * Test class for UIExtensionORFilter
 * Created by The eXo Platform SAS Author : hailt hailt@exoplatform.com 
 * Jun 1, 2012
 */
public class UIExtensionORFilterTest extends TestCase {
  public void setup() throws Exception {
    super.setUp();
  }

  /**
   * Testcase for: UIExtensionORFilter.accept() 
   * Case 01: when have no any filters
   * Expected result: TRUE
   */
  public void testAcceptWhenNoFilters() {
    List<UIExtensionFilter> filters = new ArrayList<UIExtensionFilter>();
    UIExtensionORFilter orFilter = new UIExtensionORFilterDummy(filters);

    Map<String, Object> context = new HashMap<String, Object>();
    context.put("mimeType", "application/pdf");
    try {
      boolean isAccepted = orFilter.accept(context);
      assertEquals(true, isAccepted);
    } catch (Exception e) {
      Assert.fail("testAcceptWhenNoFilters is FAILED because of an unhandled excaption");
    }
  }

  /**
   * Testcase for: UIExtensionORFilter.accept() 
   * Case 02: one of filters accepts the context
   * Expected result: TRUE
   */
  public void testAcceptCasePass() {

    UserACLFilter userFilter = new UserACLFilter();
    FileFilter fileFilter = new FileFilterDummy();
    List<String> mimetypes = new ArrayList<String>();
    mimetypes.add("image/gif");
    mimetypes.add("image/jpeg");
    mimetypes.add("image/png");
    ((FileFilterDummy) fileFilter).setMimeTypes(mimetypes);

    List<UIExtensionFilter> filters = new ArrayList<UIExtensionFilter>();
    filters.add(fileFilter);
    filters.add(userFilter);

    UIExtensionORFilter orFilter = new UIExtensionORFilterDummy(filters);

    Map<String, Object> context = new HashMap<String, Object>();
    context.put("mimeType", "image/gif");
    try {
      boolean isAccepted = orFilter.accept(context);
      assertEquals(true, isAccepted);
    } catch (Exception e) {
      Assert.fail("testAcceptCasePass is FAILED because of an unhandled excaption");
    }
  }

  /**
   * Testcase for: UIExtensionORFilter.accept() 
   * Case 03: no one accepts the context
   * Expected result: FALSE
   */
  public void testAcceptCaseFail() {
    FileFilter fileFilter = new FileFilterDummy();
    List<String> mimetypes = new ArrayList<String>();
    mimetypes.add("image/gif");
    mimetypes.add("image/jpeg");
    mimetypes.add("image/png");
    ((FileFilterDummy) fileFilter).setMimeTypes(mimetypes);

    List<UIExtensionFilter> filters = new ArrayList<UIExtensionFilter>();
    filters.add(fileFilter);

    UIExtensionORFilter orFilter = new UIExtensionORFilterDummy(filters, "Message");

    Map<String, Object> context = new HashMap<String, Object>();
    context.put("mimeType", "application/pdf");
    try {
      boolean isAccepted = orFilter.accept(context);
      assertEquals(false, isAccepted);
    } catch (Exception e) {
      Assert.fail("testAcceptCaseFail is FAILED because of an unhandled excaption");
    }
  }

  public void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * This class is a dummy class for the FileFilter. It allows us to set the
   * value for mimetypes attribute of FileFilter (which is a protected
   * attribute)
   * 
   * @author hailt
   */
  public class FileFilterDummy extends FileFilter {
    public void setMimeTypes(List<String> mimetypes) {
      this.mimeTypes = mimetypes;
    }
  }

  /**
   * This class is a sub class of UIExtensionORFilter
   * @author hailt
   */
  public class UIExtensionORFilterDummy extends UIExtensionORFilter {
    public UIExtensionORFilterDummy(List<UIExtensionFilter> filters) {
      super(filters);
    }

    public UIExtensionORFilterDummy(List<UIExtensionFilter> filters, String messageKey) {
      super(filters, messageKey);
    }

    @Override
    public void onDeny(Map<String, Object> context) throws Exception {
      // do nothing
    }
  }
}
