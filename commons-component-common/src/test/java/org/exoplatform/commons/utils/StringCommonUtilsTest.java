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

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Dec
 * 15, 2015
 */
public class StringCommonUtilsTest extends TestCase {

  public void testCompressDecompress() throws Exception {

    String initialString = "abcdefghijklmnopqrstuvwxyzabcdeéabcd";
    InputStream is = StringCommonUtils.compress(initialString);
    String result = StringCommonUtils.decompress(is);

    assertTrue(result.equals(initialString));

  }

  public void testEncodeSpecialCharInSearchTerm() {
    //test for text null
    String s = null;
    assertEquals("",StringCommonUtils.encodeSpecialCharInSearchTerm(s));
    //test for text empty
    s = "";
    assertEquals("",StringCommonUtils.encodeSpecialCharInSearchTerm(s));
    // normal text
    s = "normal text";
    assertEquals("normal text",StringCommonUtils.encodeSpecialCharInSearchTerm(s));
    // all characters is special characters.
    s = "@#$%^&*()\"/-=~`'.,";
    assertEquals("&#64;&#35;&#36;&#37;&#94;&#38;&#42;&#40;&#41;&#34;&#47;&#45;&#61;&#126;&#96;&#39;&#46;&#44;",StringCommonUtils.encodeSpecialCharInSearchTerm(s));
    // has ignore special characters.
    s = "abc !#:? =., +;";
    assertEquals("abc !#:? =., +;",StringCommonUtils.encodeSpecialCharInSearchTerm(s));
    // has ignore and not ignore special characters.
    s = "abc !#: ()\" ' | ] [";
    assertEquals("abc !#: &#40;&#41;&#34; &#39; &#124; &#93; &#91;",StringCommonUtils.encodeSpecialCharInSearchTerm(s));
  }

  public void testencodeSpecialCharForSimpleInput() {
    //test for text null
    String s = null;
    assertEquals("",StringCommonUtils.encodeSpecialCharForSimpleInput(s));
    //test for text empty
    s = "";
    assertEquals("",StringCommonUtils.encodeSpecialCharForSimpleInput(s));
    // normal text
    s = "normal text";
    assertEquals("normal text",StringCommonUtils.encodeSpecialCharForSimpleInput(s));
    // has double space .
    s = "   abc   aa s   s";
    assertEquals("abc aa s s", StringCommonUtils.encodeSpecialCharForSimpleInput(s));
    // has ignore special characters.
    s = "abc !#:?=.,()+; ddd";
    assertEquals("abc !#:?=.,&#40;&#41;+; ddd",StringCommonUtils.encodeSpecialCharForSimpleInput(s));
    // has ignore and not ignore special characters.
    s = "abc !# :?=.,' | ] [";
    assertEquals("abc !# :?=.,&#39; &#124; &#93; &#91;",StringCommonUtils.encodeSpecialCharForSimpleInput(s));
  }
  
  public void testEncodeSpecialCharInHTML() {
    //test for text null
    String s = null;
    assertEquals("",StringCommonUtils.encodeSpecialCharInHTML(s));
    //test for text empty
    s = "";
    assertEquals("",StringCommonUtils.encodeSpecialCharInHTML(s));
    // normal text
    s = "normal text";
    assertEquals("normal text",StringCommonUtils.encodeSpecialCharInHTML(s));
        // has ignore special characters.
    s = "abc &#<>[]/:?\"=.,*$%()\\+@!^*-}{;`~_ ddd";
    assertEquals("abc &#<>[]/:?\"=.,*$%()\\+@!^*-}{;`~_ ddd",StringCommonUtils.encodeSpecialCharInHTML(s));
    // has ignore and not ignore special characters.
    s = "abc !# :?=.,' | ] [";
    assertEquals("abc !# :?=.,&#39; &#124; ] [",StringCommonUtils.encodeSpecialCharInHTML(s));
  }

  public void testDecodeSpecialCharToHTMLnumber() throws Exception {
    String input = null;
    assertEquals(null, StringCommonUtils.decodeSpecialCharToHTMLnumber(input));
    input = "";
    assertEquals(input, StringCommonUtils.decodeSpecialCharToHTMLnumber(input));
    input = "Normal text abc";
    assertEquals(input, StringCommonUtils.decodeSpecialCharToHTMLnumber(input));
    input = "Text ...&#60;&#64;&#35;&#36;&#37;&#94;&#38;&#42;&#40;&#41;&#34;&#47;&#45;&#61;&#126;&#96;&#39;&#46;&#44;&#62; too";
    assertEquals("Text ...<@#$%^&*()\"/-=~`'.,> too", StringCommonUtils.decodeSpecialCharToHTMLnumber(input));
    //content extend token
    input = "Text ...&lt;div class=&quot;&amp;XZY&quot;&gt;Test&lt;&#47;div&gt;&#40;&#41;&#34;&#47;&#45;&#61;&#126;&#96;&#39;&#46;&#44;&#60;strong&#62;too&#60;&#47;strong&#62;";
    assertEquals("Text ...<div class=\"&XZY\">Test</div>()\"/-=~`'.,<strong>too</strong>", StringCommonUtils.decodeSpecialCharToHTMLnumber(input));
    
    // ignore case
    List<String> ig = Arrays.asList(new String[]{"&gt;", "&lt;", "&#46;"});
    assertEquals("Text ...&lt;div class=\"&XZY\"&gt;Test&lt;/div&gt;()\"/-=~`'&#46;,<strong>too</strong>", StringCommonUtils.decodeSpecialCharToHTMLnumber(input, ig));
    
    //
    assertEquals("Text ...&lt;div class=\"&amp;XZY\"&gt;Test&lt;/div&gt;()\"/-=~`'.,<strong>too</strong>", StringCommonUtils.decodeSpecialCharToHTMLnumberIgnore(input));
  }
  
  public void testIsContainSpecialCharacter() {
    String input = null;
    assertEquals(false, StringCommonUtils.isContainSpecialCharacter(input));
    input = "";
    assertEquals(false, StringCommonUtils.isContainSpecialCharacter(input));
    input = "abcgde";
    assertEquals(false, StringCommonUtils.isContainSpecialCharacter(input));
    input = "abcg#$de";
    assertEquals(true, StringCommonUtils.isContainSpecialCharacter(input));
    input = "!@#abcgde";
    assertEquals(true, StringCommonUtils.isContainSpecialCharacter(input));
    input = "&#<>[]/:?\"=.,*$%()\\+@!^*-}{;`~_";
    assertEquals(true, StringCommonUtils.isContainSpecialCharacter(input));
  }

}
