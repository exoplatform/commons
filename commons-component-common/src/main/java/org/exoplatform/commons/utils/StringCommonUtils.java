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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Dec 15, 2015  
 */
public class StringCommonUtils {

  private static final Pattern SCRIPT_TAG_PATTERN = Pattern.compile("(<(/|)?[ ]*(script|iframe|object|embed)>|<(iframe|object|embed)|((background|expression|style)=)|javascript:\\w+|(on\\w+=))",
                                                                    Pattern.CASE_INSENSITIVE);
  private static final String  MACRO_START_TAG    = "startmacro";
  private static final String  MACRO_END_TAG    = "stopmacro";
  private static final Pattern MACRO_REGEX      =
          Pattern.compile("<!--" + MACRO_START_TAG + "(.*?)-->(.*?)<!--" + MACRO_END_TAG + "-->",Pattern.DOTALL);

  private static final int               BUFFER_SIZE           = 32;

  @SuppressWarnings("serial")
  private static List<String> MATCHED_WORDS = new ArrayList<String>() {
    {
      add("on");
      add("javascript");
      add("background");
      add("src");
      add("style");
    }
  };
  
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
      String decodeInput = StringEscapeUtils.unescapeHtml(input);
      Matcher matcher = SCRIPT_TAG_PATTERN.matcher(decodeInput);
      StringBuffer str = new StringBuffer(decodeInput.length());
      while (matcher.find()) {
        //removes in the case matched in the word list
        if (matchedWord(matcher.group())) {
          matcher.appendReplacement(str, "");
        } else {
          matcher.appendReplacement(str, HTMLEntityEncoder.getInstance().encodeHTMLAttribute(matcher.group()));
        }
      }
      matcher.appendTail(str);
      input = str.toString();
    }
    return input;
  }

  /**
   * Encode the XSS script from xhtmlContent of wiki page and skip encoding macro as a trusted third party    
   *  
   * @param input the given string to encode
   * 
   */
  public static String encodeWikiScriptMarkup(String input) {
    if (input != null) {
      String decodeInput = StringEscapeUtils.unescapeHtml(input);
      Matcher matcher = SCRIPT_TAG_PATTERN.matcher(decodeInput);
      StringBuffer str = new StringBuffer(decodeInput.length());
      Matcher macroMatcher = MACRO_REGEX.matcher(decodeInput);
      ArrayList<Integer> startIndex = new ArrayList<Integer>();
      ArrayList<Integer> endIndex = new ArrayList<Integer>();
      while (macroMatcher.find()) {
        startIndex.add(macroMatcher.start());
        endIndex.add(macroMatcher.end());
      }
      boolean skip = false;
      while (matcher.find()) {
        int styleStartPos = matcher.start();
        for (int i = 0; i < startIndex.size(); i++) {
          int firstIndex = startIndex.get(i);
          int lastIndex = endIndex.get(i);
          if (styleStartPos > firstIndex && styleStartPos < lastIndex)
            skip = true;
        }
        if (!skip) {
          if (matchedWord(matcher.group()))
            matcher.appendReplacement(str, "");
          else
            matcher.appendReplacement(str, HTMLEntityEncoder.getInstance().encodeHTMLAttribute(matcher.group()));
          skip = false;
        }
      }
      matcher.appendTail(str);
      input = str.toString();
    }
    return input;
  }

  private static boolean matchedWord(String input) {
    if (input == null || input.length() == 0) return true;
    //
    String lowerStr = input.toLowerCase();
    for (String word : MATCHED_WORDS) {
      if (lowerStr.contains(word)) {
        return true;
      }
    }
    
    return false;
  }

  public static InputStream compress(String string) throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
    GZIPOutputStream gos = new GZIPOutputStream(os);
    gos.write(string.getBytes());
    gos.close();
    byte[] compressed = os.toByteArray();
    os.close();
    return new ByteArrayInputStream(compressed);
  }

  public static String decompress(InputStream is) throws IOException {
    GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    try {

      byte[] data = new byte[BUFFER_SIZE];
      int bytesRead;

      while ((bytesRead = gis.read(data)) != -1) {
        buffer.write(data,0,bytesRead);
      }

      return new String(buffer.toByteArray());
    }finally{
      gis.close();
      is.close();
      buffer.close();
    }
  }
}
