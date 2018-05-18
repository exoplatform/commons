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

import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Dec
 * 15, 2015
 */
public class StringCommonUtils {

  private static final int BUFFER_SIZE = 32;

  public static final String         EMPTY_STR                    = "";

  public static final String         SEMICOLON                    = ";";

  public static final String         LESS_THAN                    = "&lt;";

  public static final String         GREATER_THAN                 = "&gt;";

  public static final String         AMP_NUMBER                   = "&#";

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

  public static String encodeSpecialCharInHTML(String s) {
    /*
     * charIgnore: Some special characters we ignore
     */
    String charIgnore = "&#<>[]/:?\"=.,*$%()\\+@!^*-}{;`~_";
    return encodeSpecialCharToHTMLnumber(s, charIgnore, false);
  }

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
}
