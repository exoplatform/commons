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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.webui.ext.filter.UIExtensionFilterType;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Test class for FileFilter
 * 
 * Created by The eXo Platform SAS Author : Le Thanh Hai hailt@exoplatform.com 
 * May 24, 2012
 */
public class FileFilterTest extends TestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  /**
   * Testcase for: FileFilter.accept()
   * Case 01: when the mimetypes attribute of FileFilter is null
   * Expected result: return TRUE
   */
  public void testAcceptWhenMimeTypeNull() {
    FileFilter filter = new FileFilterDummy();
    Map<String, Object> context = new HashMap<String, Object>();
    context.put("mimeType", "application/pdf");

    try {
      boolean isAccepted = filter.accept(context);
      assertEquals(isAccepted, true);
    } catch (Exception ex) {
      Assert.fail("testAcceptWhenMimeTypeNull is FAILED because of an unhandled excaption");
    }
  }

  /**
   * Testcase for: FileFilter.accept()
   * Case 02: when the mimetypes attribute of FileFilter doesn't contain the mimetype in the input context
   * Expected result: return FALSE
   */
  public void testAcceptWhenNotContain() {
    FileFilter filter = new FileFilterDummy();
    Map<String, Object> context = new HashMap<String, Object>();
    context.put("mimeType", "application/pdf");

    List<String> mimetypes = new ArrayList<String>();
    mimetypes.add("image/gif");
    mimetypes.add("image/jpeg");
    mimetypes.add("image/png");
    ((FileFilterDummy) filter).setMimeTypes(mimetypes);
    
    try {
      boolean isAccepted = filter.accept(context);
      assertEquals(isAccepted, false);
    } catch (Exception ex) {
      Assert.fail("testAcceptWhenNotContain is FAILED because of an unhandled excaption");
    }
  }

  /**
   * Testcase for: FileFilter.accept()
   * Case 03: when the mimetypes attribute of FileFilter contain the mimetype in the input context
   * Expected result: return TRUE
   */
  public void testAcceptWhenContain() {
    FileFilter filter = new FileFilterDummy();
    Map<String, Object> context = new HashMap<String, Object>();
    context.put("mimeType", "image/gif");

    List<String> mimetypes = new ArrayList<String>();
    mimetypes.add("image/gif");
    mimetypes.add("image/jpeg");
    mimetypes.add("image/png");
    ((FileFilterDummy) filter).setMimeTypes(mimetypes);
    
    try {
      boolean isAccepted = filter.accept(context);
      assertEquals(isAccepted, true);
    } catch (Exception ex) {
      Assert.fail("testAcceptWhenContain is FAILED because of an unhandled excaption");
    }
  }
  
  /**
   * Testcase for: FileFilter.getType()
   * Expected result: 
   *             UIExtensionFilterType.MANDATORY 
   *          or UIExtensionFilterType.REQUISITE
   *          or UIExtensionFilterType.REQUIRED
   *          or UIExtensionFilterType.OPTIONAL
   */
  public void testGetType() {
    FileFilter filter = new FileFilter();
    UIExtensionFilterType type = filter.getType();
    
    if (type != UIExtensionFilterType.MANDATORY && type != UIExtensionFilterType.REQUISITE 
        && type != UIExtensionFilterType.REQUIRED && type != UIExtensionFilterType.OPTIONAL) {
      Assert.fail("testGetType is FAILED. The result is not expected");
    }
  }

  /**
   * This class is a dummy class for the FileFilter. 
   * It allows us to set the value for mimetypes attribute of FileFilter (which is a protected attribute)
   * 
   * @author hailt
   *
   */
  public class FileFilterDummy extends FileFilter {
    public void setMimeTypes(List<String> mimetypes) {
      this.mimeTypes = mimetypes;
    }
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }
}