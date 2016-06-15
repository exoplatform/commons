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

import com.google.common.base.Throwables;
import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.owasp.html.*;

import javax.annotation.Nullable;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Dec 15, 2015  
 */
public class StringCommonUtils {

    private static final Log LOG = ExoLogger.getLogger(StringCommonUtils.class);


  private static final Pattern SCRIPT_TAG_PATTERN = Pattern.compile("(<(/|)?[ ]*(script|iframe|object|embed)>|<(iframe|object|embed)|((background|expression|style)=)|javascript:\\w+|(on\\w+=))",
                                                                    Pattern.CASE_INSENSITIVE);

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
   * example: {@code <p><Script>alert(1);</script>bbbb</p>}
   * CommonUtils.encodeScriptMarkup(input);
   * result = {@code <p>&lt;Script&gt;alert(1);&lt;&#x2f;script&gt;bbbb</p>}
   * @return Only encode the {@code <script>} tag.
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
  public static String sanitize(@Nullable String html) throws Exception{
    StringBuilder sb = new StringBuilder();

      // Set up an output channel to receive the sanitized HTML.
      HtmlStreamRenderer renderer = HtmlStreamRenderer.create(
            sb,
              // Receives notifications on a failure to write to the output.
            new Handler<IOException>() {
                public void handle(IOException ex) {
                    Throwables.propagate(ex);
                }
            },
            // Our HTML parser is very lenient, but this receives
            // notifications on
            // truly bizarre inputs.
            new Handler<String>() {
                public void handle(String x) {
                    throw new AssertionError(x);
                }
            });

    HtmlSanitizer.Policy policy = new HtmlPolicyBuilder()
            // Allow these tags.
            .allowElements(
                    "a", "b", "br", "div", "i", "iframe", "img", "input", "li",
                    "ol", "p", "span", "ul", "noscript", "noframes", "noembed", "noxss")
                    // And these attributes.
            .allowAttributes(
                    "dir", "checked", "class", "href", "id", "target", "title", "type")
            .globally()
                    // Cleanup IDs and CLASSes and prefix them with p- to move to a separate
                    // name-space.
            .allowAttributes("id", "class")
            .matching(
                    new AttributePolicy() {
                      public String apply(
                              String elementName, String attributeName, String value) {
                        return value.replaceAll("(?:^|\\s)([a-zA-Z])", " p-$1")
                                .replaceAll("\\s+", " ")
                                .trim();
                      }
                    })
            .globally()
            .allowStyling()
                    // Don't throw out useless <img> and <input> elements to ease debugging.
            .allowWithoutAttributes("img", "input")
            .build(renderer);

    HtmlSanitizer.sanitize(html, policy);
      return sb.toString();
  }
}
