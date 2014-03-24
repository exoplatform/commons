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
package org.exoplatform.commons.notification.template;

import junit.framework.TestCase;

public class TemplateUtilsTestCase extends TestCase {

  public TemplateUtilsTestCase() {
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  public void testCleanHtmlTags() {
    String input = null;
    String expected = "";
    assertEquals(expected, TemplateUtils.cleanHtmlTags(input));
    expected = "test clean html test";
    // clean multi spaces
    input = "   test  clean   html  test  ";
    assertEquals(expected, TemplateUtils.cleanHtmlTags(input));
    // clean multi lines
    input = "\n\n\n test \nclean html \n test \n\n\n";
    assertEquals(expected, TemplateUtils.cleanHtmlTags(input));
    // clean style/script
    input = "\n\n\n test \nclean<script type=\"text/javascript\"> alert(1); </script> "
        + "<style type=\"text/css\" languge=\"en\">body {padding: 0px} .menu { color: #fff}</style>"
        + " html\n test \n\n\n";
    assertEquals(expected, TemplateUtils.cleanHtmlTags(input));
    // clean normal tags html.
    input = "\n\n\n test \nclean<script type=\"text/javascript\"> alert(1); </script> "
        + "<style type=\"text/css\" languge=\"en\">body {padding: 0px} .menu { color: #fff}</style>"
        + "  <br> <br ><br/> <br />html\n <b>test</b> \n\n\n";
    assertEquals(expected, TemplateUtils.cleanHtmlTags(input));
  }
  
  public void testGetExcerptSubject() {
    String input = null;
    String expected = "";
    assertEquals(expected, TemplateUtils.getExcerptSubject(input));
    input= "lorem ipsum dolor sit.";
    expected = "lorem ipsum dolor sit.";
    assertEquals(expected, TemplateUtils.getExcerptSubject(input));
    // plain text
    input= "&#21487; &#20197; &#21578; &#35785; &#25105;";
    expected = "可 以 告 诉 我";
    assertEquals(expected, TemplateUtils.getExcerptSubject(input));
    // 
    input= "Lorem ipsum dolor sit amet, consectetuer elit adipiscing, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat." +
    		" Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit";
    expected = "Lorem ipsum dolor sit amet, consectetuer elit...";
    assertEquals(expected, TemplateUtils.getExcerptSubject(input));
  }
  
  
}
