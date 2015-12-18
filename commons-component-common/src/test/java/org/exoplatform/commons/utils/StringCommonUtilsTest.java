/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.commons.utils;

import junit.framework.TestCase;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Dec 15, 2015  
 */
public class StringCommonUtilsTest extends TestCase {
  
  public void testEncodeScriptMarkup() {
    //
    String input = "<p><Script>alert(1);</script>bbbb</p>";
    String done = StringCommonUtils.encodeScriptMarkup(input);
    assertEquals("<p>&lt;Script&gt;alert(1);&lt;&#x2f;script&gt;bbbb</p>", done);
    
    String input1 = "<p><Script>alert(1);</SCRIPT>bbbb</p>";
    String done1 = StringCommonUtils.encodeScriptMarkup(input1);
    assertEquals("<p>&lt;Script&gt;alert(1);&lt;&#x2f;SCRIPT&gt;bbbb</p>", done1);
    
    String input2 = "<a onmouseover='alert(document.cookie)'>xxs link</a>";
    assertEquals("&lt;a onmouseover='alert(document.cookie)'>xxs link&lt;&#x2f;a&gt;",
                 StringCommonUtils.encodeScriptMarkup(input2));
    
    String input3 = "<IMG SRC=/ onerror='alert('XSS')'></img>";
    assertEquals("&lt;IMG SRC=/ onerror='alert('XSS')'>&lt;&#x2f;img&gt;", StringCommonUtils.encodeScriptMarkup(input3));
    
    String input4 = "<p>www<iframe src='javascript:alert('XSS');'></iframe></p>";
    assertEquals("<p>www&lt;iframe src='javascript:alert('XSS');'>&lt;&#x2f;iframe&gt;</p>", StringCommonUtils.encodeScriptMarkup(input4));
    
  }

}
