/*
Copyright (c) 2003-2011, CKSource - Frederico Knabben. All rights reserved.
For licensing, see http://ckeditor.com/license
 */
package org.exoplatform.webui.ckeditor;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Helper class for CKEditor tags.
 */
public class TagHelper {

  private static final String[] CHARS_FROM = { "\\", "/", "\n", "\t", "\r", "\b", "\f", "\"" };

  private static final String[] CHARS_TO   = { "\\\\", "\\/", "\\\n", "\\\t", "\\\r", "\\\b",
      "\\\f", "\\\""                      };

  /**
   * Wraps a String with a JavaScript tag.
   * 
   * @param input input String
   * @return input wrapped with a JavaScript tag
   */
  public static String script(final String input) {    
    StringBuffer out = new StringBuffer();
    out.append("<script type=\"text/javascript\">");
    out.append("//<![CDATA[\n");
    out.append(input);
    out.append("\n//]]>");
    out.append("</script>\n");    
    return out.toString();
  }

  /**
   * Creates JavaScript code for including ckeditor.js.
   * 
   * @param basePath CKEditor base path
   * @param args script arguments
   * @return JavaScript code
   */
  public static String createCKEditorIncJS(final String basePath, final String args) {
    return "<script type=\"text/javascript\" src=\"" + basePath + "ckeditor.js" + args
        + "\"></script>\n";
  }

  /**
   * Provides basic JSON support.
   * 
   * @param o object to encode
   * @return encoded configuration object value
   */
  @SuppressWarnings("unchecked")
  public static String jsEncode(final Object o) {
    if (o == null) {
      return "null";
    }
    if (o instanceof String) {
      return jsEncode((String) o);
    }
    if (o instanceof Number) {
      return jsEncode((Number) o);
    }
    if (o instanceof Boolean) {
      return jsEncode((Boolean) o);
    }
    if (o instanceof Map) {
      return jsEncode((Map<String, Object>) o);
    }
    if (o instanceof List) {
      return jsEncode((List<Object>) o);
    }
    if (o instanceof CKEditorConfig) {
      return jsEncode((CKEditorConfig) o);
    }
    return "";
  }

  /**
   * Provides basic JSON support for String objects.
   * 
   * @param s String object to encode
   * @return encoded configuration String object value
   */
  public static String jsEncode(final String s) {
    if (s.indexOf("@@") == 0) {
      return s.substring(2);
    }
    if (s.length() > 9 && s.substring(0, 9).toUpperCase().equals("CKEDITOR.")) {
      return s;
    }
    return clearString(s);
  }

  /**
   * Provides basic JSON support for Number objects.
   * 
   * @param n Number object to encode
   * @return encoded Number object value
   */
  public static String jsEncode(final Number n) {
    return n.toString().replace(",", ".");
  }

  /**
   * Provides basic JSON support for Boolean objects.
   * 
   * @param b Boolean object to encode
   * @return encoded Boolean object value
   */
  public static String jsEncode(final Boolean b) {
    return b.toString();
  }

  /**
   * Provides basic JSON support for Map objects.
   * 
   * @param map Map object to encode
   * @return encoded Map object value
   */
  public static String jsEncode(final Map<String, Object> map) {
    StringBuilder sb = new StringBuilder("{");

    for (Object obj : map.keySet()) {
      if (sb.length() > 1) {
        sb.append(",");
      }
      sb.append(jsEncode(obj));
      sb.append(":");
      sb.append(jsEncode(map.get(obj)));
    }
    sb.append("}");
    return sb.toString();
  }

  /**
   * Provides basic JSON support for List objects.
   * 
   * @param list List object to encode
   * @return encoded List object value
   */
  public static String jsEncode(final List<Object> list) {
    StringBuilder sb = new StringBuilder("[");
    for (Object obj : list) {
      if (sb.length() > 1) {
        sb.append(",");
      }
      sb.append(jsEncode(obj));
    }
    sb.append("]");
    return sb.toString();
  }

  /**
   * Provides basic JSON support for the configuration object.
   * 
   * @param config configuration object to encode
   * @return encoded configuration object value
   */
  public static String jsEncode(final CKEditorConfig config) {
    StringBuilder sb = new StringBuilder("{");

    for (Object obj : config.getConfigValues().keySet()) {
      if (sb.length() > 1) {
        sb.append(",");
      }
      sb.append(jsEncode(obj));
      sb.append(":");
      sb.append(jsEncode((config.getConfigValue((String) obj))));
    }
    sb.append("}");
    return sb.toString();
  }

  /**
   * Clears a String from special characters and quotes it.
   * 
   * @param s input String
   * @return cleared String
   */
  private static String clearString(final String s) {
    String string = s;
    for (int i = 0; i < CHARS_FROM.length; i++) {
      string = string.replace(CHARS_FROM[i], CHARS_TO[i]);
    }
    if (Pattern.compile("[\\[{].*[\\]}]").matcher(string).matches()) {
      return string;
    } else {
      return "\"" + string + "\"";
    }

  }

}
