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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang.StringUtils;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Dec
 * 15, 2015
 */
public class StringCommonUtils {

  private static final int           BUFFER_SIZE                  = 32;

  public static final String         EMPTY_STR                    = "";

  public static final String         SEMICOLON                    = ";";

  public static final String         SPACE                        = " ";

  public static final String         AMP_NUMBER                   = "&#";

  public static final String         LESS_THAN                    = "&lt;";

  public static final String         GREATER_THAN                 = "&gt;";

  public static final String         QUOT                         = "&quot;";

  public static final String         AMP_SPACE                    = "&nbsp;";

  public static final String         AMP_HEX                      = "&#x26;";

  public static final String         AMP                          = "&amp;";

  private static List<String>        tokens                       = new ArrayList<String>();

  private static Map<String, String> charcodes                    = new HashMap<String, String>();

  private static List<String>        ignoreLessThanAndGreaterThan = Arrays.asList(LESS_THAN, GREATER_THAN, AMP);

  /*
   *  The distance code number content special character.
   *  Ex: from ' '(32) to '0'(48): ' ', '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/'
   *  See: http://www.ascii.cl/htmlcodes.htm
  */
  //'0', ' ', 'A', '9', 'a', 'Z', '~', 'z', '\''
  private static int[]               CHAR_CODES                   = new int[] { 48, 32, 65, 57, 97, 90, 127, 122, 39 };

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
        buffer.write(data, 0, bytesRead);
      }

      return new String(buffer.toByteArray());
    } finally {
      gis.close();
      is.close();
      buffer.close();
    }
  }

  /**
   * Encode special character, use for input search
   * @param s the string input
   * @return String 
   */
  public static String encodeSpecialCharInSearchTerm(String s) {
    /*
     * + When all characters in param s is special characters has in charIgnore, we must encode all characters.
     * + If all characters in param s is not special characters, we can ignore some special characters [!#:?=.,+;~`_]
    */
    String charIgnore = "&#<>[]/:?\"'=.,*$%()\\+@!^*-}{;`~_";
    if (StringUtils.isNotBlank(s)) {
      int i = 0;
      while (charIgnore.indexOf(String.valueOf(s.charAt(i))) >= 0) {
        ++i;
        if (i == s.length()) {
          charIgnore = EMPTY_STR;
          break;
        }
      }
    }
    if (StringUtils.isNotBlank(charIgnore)) charIgnore = "&</>!#:?=.,+;~`_";
    return encodeSpecialCharToHTMLnumber(s, charIgnore, true);
  }

  /**
   * Encode special character, use for input title or name of the object.
   * @param s the string input
   * @return String 
   */
  public static String encodeSpecialCharForSimpleInput(String s) {
    /*
     * remove double space 
    */
    if(StringUtils.isNotBlank(s)) {
      while (s.indexOf("  ") >= 0) {
        s = StringUtils.replace(s, "  ", SPACE).trim();
      }
    }
    /*
     * charIgnore: Some special characters we ignore
    */
    String charIgnore = "!#:?=.,+;~`_";
    return encodeSpecialCharToHTMLnumber(s, charIgnore, true);
  }

  /**
   * Encode special character, use for input content of object (only apply for input by CKEditer).
   * @param s the string input
   * @return String 
   */
  public static String encodeSpecialCharInHTML(String s) {
    /*
     * charIgnore: Some special characters we ignore
    */
    String charIgnore = "&#<>[]/:?\"=.,*$%()\\+@!^*-}{;`~_";
    return encodeSpecialCharToHTMLnumber(s, charIgnore, false);
  }

  public static boolean isContainSpecialCharacter(String s) {
    if (StringUtils.isBlank(s)) {
      return false;
    }
    String charIgnore = "&#<>[]/:?\"=.,*$%()\\+@!^*-}{;`~_";
    int i = 0;
    while (i < s.length()) {
      char c = s.charAt(i);
      if (charIgnore.indexOf(String.valueOf(c)) >= 0) {
        return true;
      }
      i++;
    }
    return false;
  }

  /**
   * Encode special character to html number. Ex: '/' is encoded to &#47;
   * @param s the string input
   * @param charIgnore the string content ignore some special character can not encode.
   * @param isTitle the boolean for check convert is title or not.
   * @return String 
   */
  public static String encodeSpecialCharToHTMLnumber(String s, String charIgnore, boolean isTitle) {
    if (StringUtils.isBlank(s)) {
      return EMPTY_STR;
    }
    int i = 0;
    StringBuilder builder = new StringBuilder();
    while (i < s.length()) {
      char c = s.charAt(i);
      if (charIgnore.indexOf(String.valueOf(c)) >= 0) {
        builder.append(c);
      } else {
        int t = s.codePointAt(i);
        if (t < CHAR_CODES[0] && t > CHAR_CODES[1] || t < CHAR_CODES[2] && t > CHAR_CODES[3] ||
            t < CHAR_CODES[4] && t > CHAR_CODES[5] || t < CHAR_CODES[6] && t > CHAR_CODES[7]) {
          if (isTitle && (t == 60 || t == 62)) {
            if (t == 60) {
              builder.append(LESS_THAN);
            } else if (t == 62) {
              builder.append(GREATER_THAN);
            }
          } else {
            builder.append(AMP_NUMBER).append(t).append(SEMICOLON);
          }
        } else {
          builder.append(c);
        }
      }
      ++i;
    }
    return builder.toString();
  }

  public static String decodeSpecialCharToHTMLnumber(String s, List<String> lIgnore) {
    if (StringUtils.isBlank(s)){
      return s;
    }
    for (String token : tokens) {
      if (lIgnore.contains(token)){
        continue;
      }
      while (token != null && s.indexOf(token) >= 0) {
        s = StringUtils.replace(s, token, charcodes.get(token));
      }
    }
    return s;
  }

  static {
    if(tokens.isEmpty()) {
      String token;
      // Tokens by HTML(Decimal) code.
      for (int t = Character.MIN_CODE_POINT; t < Character.MAX_CODE_POINT; t++) {
        if (t < CHAR_CODES[0] && t > CHAR_CODES[1] || t < CHAR_CODES[2] && t > CHAR_CODES[3] || 
            t < CHAR_CODES[4] && t > CHAR_CODES[5] || t < CHAR_CODES[6] && t > CHAR_CODES[7]) {
          token = new StringBuilder(AMP_NUMBER).append(t).append(SEMICOLON).toString();
          tokens.add(token);
          charcodes.put(token, String.valueOf(Character.toChars(t)[0]));
        }
      }
      // Tokens by Entity code.
      tokens.add(LESS_THAN);
      charcodes.put(LESS_THAN, "<");
      tokens.add(GREATER_THAN);
      charcodes.put(GREATER_THAN, ">");
      tokens.add(QUOT);
      charcodes.put(QUOT, "\"");
      tokens.add(AMP_SPACE);
      charcodes.put(AMP_SPACE, SPACE);
      tokens.add(AMP_HEX);
      charcodes.put(AMP_HEX, "&");
      tokens.add(AMP);
      charcodes.put(AMP, "&");
    }
  }

  public static String decodeSpecialCharToHTMLnumber(String s) {
    return decodeSpecialCharToHTMLnumber(s, new ArrayList<String>());
  }

  /**
   *  Decode special chars to HTML number ignore char {@literal Less than '<' and Greater than '>'}
   * 
   * @param str
   * @return
   */
  public static String decodeSpecialCharToHTMLnumberIgnore(String str) {
    return decodeSpecialCharToHTMLnumber(str, ignoreLessThanAndGreaterThan);
  }

}
