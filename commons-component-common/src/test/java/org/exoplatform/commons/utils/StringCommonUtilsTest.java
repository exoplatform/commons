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
    assertEquals("&lt;a >xxs link&lt;&#x2f;a&gt;",
                 StringCommonUtils.encodeScriptMarkup(input2));
    
    String input3 = "<IMG SRC='/' onerror='alert('XSS')'></img>";
    assertEquals("&lt;IMG >&lt;&#x2f;img&gt;", StringCommonUtils.encodeScriptMarkup(input3));
    
    String input4 = "<p>www<iframe src='javascript:alert('XSS');'></iframe></p>";
    assertEquals("<p>www&lt;iframe >&lt;&#x2f;iframe&gt;</p>", StringCommonUtils.encodeScriptMarkup(input4));
    
    String input5 = "<TABLE BACKGROUND=\"javascript:alert('XSS')\">";
    assertEquals("<TABLE >", StringCommonUtils.encodeScriptMarkup(input5));
    
    String input6 = "<DIV STYLE=\"background-image: url(javascript:alert('XSS'))\">";
    assertEquals("<DIV STYLE=\">", StringCommonUtils.encodeScriptMarkup(input6));
    
    String input7 = "<DIV STYLE=\"background-image:\0075\0072\006C\0028'\006a\0061\0076\0061\0073\0063\0072\0069\0070\0074\003a\0061\006c\0065\0072\0074\0028.1027\0058.1053\0053\0027\0029'\0029\">";
    assertEquals("<DIV STYLE=\">", StringCommonUtils.encodeScriptMarkup(input7));
    
    String input8 = "<DIV STYLE=\"background-image: url(&#1;javascript:alert('XSS'))\">";
    assertEquals("<DIV STYLE=\">", StringCommonUtils.encodeScriptMarkup(input8));
    
    String input9 = "<DIV STYLE=\"width: expression(alert('XSS'));\">";
    assertEquals("<DIV STYLE=\"width: >", StringCommonUtils.encodeScriptMarkup(input9));
    
    String input10 = "<DIV STYLE=\"width: expression(alert('XSS'));\">";
    assertEquals("<DIV STYLE=\"width: >", StringCommonUtils.encodeScriptMarkup(input10));
    
    String input11 = "<BASE HREF=\"javascript:alert('XSS');\">";
    assertEquals("<BASE HREF=\"\">", StringCommonUtils.encodeScriptMarkup(input11));
  }
  
  public void testRemoveEventAttribute() {
    
    String input3 = "<IMG onerror='alert('XSS')'></img>";
    assertEquals("&lt;IMG >&lt;&#x2f;img&gt;", StringCommonUtils.encodeScriptMarkup(input3));
    
    String input4 = "<table><tr onmouseover='alert(1)'></tr<table>";
    assertEquals("<table><tr ></tr<table>", StringCommonUtils.encodeScriptMarkup(input4));
    
  }

}
