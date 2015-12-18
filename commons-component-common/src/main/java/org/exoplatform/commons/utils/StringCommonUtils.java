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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Dec 15, 2015  
 */
public class StringCommonUtils {

  private static final Pattern SCRIPT_TAG_PATTERN = Pattern.compile("(<(/|)?[ ]*(script|img|a|iframe)>|<(a|img|iframe))", Pattern.CASE_INSENSITIVE);
  
  /**
   * Encode the XSS script
   *  
   * @param input the given string to encode
   * 
   * example: "<p><Script>alert(1);</script>bbbb</p>";
   * CommonUtils.encodeScriptMarkup(input);
   * result = "<p>&lt;Script&gt;alert(1);&lt;&#x2f;script&gt;bbbb</p>";
   * @return Only encode the <script> tag. 
   */
  public static String encodeScriptMarkup(String input) {
    if (input != null) {
      Matcher matcher = SCRIPT_TAG_PATTERN.matcher(input);
      StringBuffer str = new StringBuffer(input.length());
      while (matcher.find()) {
        matcher.appendReplacement(str, HTMLEntityEncoder.getInstance().encodeHTMLAttribute(matcher.group()));
      }
      matcher.appendTail(str);
      input = str.toString();
    }
    return input;
  }
}
