/**
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.commons.api.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WSFilterDefinition {

  private WSFilter               filter;

  private volatile FilterMapping mapping;

  private List<String>           patterns;


  public WSFilterDefinition() {
  }
  
  public WSFilterDefinition(WSFilter filter, List<String> patterns) {
    this.filter = filter;
    this.patterns = patterns;
  }

  public WSFilter getFilter() {
    return filter;
  }

  public boolean match(String path) {
    if (mapping == null) {
      synchronized (this) {
        if (mapping == null) {
          this.mapping = new FilterMapping(patterns);
          this.patterns = null;
        }
      }
    }
    return mapping.match(path);
  }

  private static class FilterMapping {
    private final List<Pattern> patterns;

    public FilterMapping(List<String> strPatterns) {
      if (strPatterns == null || strPatterns.isEmpty()) {
        throw new IllegalArgumentException("The list of patterns cannot be empty");
      }
      this.patterns = new ArrayList<Pattern>(strPatterns.size());
      for (String sPattern : strPatterns) {
        patterns.add(Pattern.compile(sPattern));
      }
    }

    public boolean match(String path) {
      for (int i = 0, length = patterns.size(); i < length; i++) {
        Pattern pattern = patterns.get(i);
        Matcher matcher = pattern.matcher(path);
        if (matcher.matches()) {
          return true;
        }
      }
      return false;
    }

  }
}
