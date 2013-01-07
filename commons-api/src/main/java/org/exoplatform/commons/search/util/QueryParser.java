package org.exoplatform.commons.search.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryParser {
  private String query;
  private List<String> results;

  public QueryParser(String query) {
    this.query = query;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public List<String> getResults() {
    return results;
  }

  public QueryParser pick(String param) {
    List<String> list = new ArrayList<String>();

    String[] rhsPatterns = {"\"([^\"]+)\"", "([\\S]+)"};
    
    for(String pattern:rhsPatterns){
      Matcher matcher = Pattern.compile("\\b" + param + "\\s*=\\s*" + pattern).matcher(query);
      while (matcher.find()) {
        String founds = matcher.group(1);
        list.addAll(Arrays.asList(founds.split(";\\s*")));
      }
      query = matcher.replaceAll("").trim();
    }
    
    results = list;
    return this;
  }

  public static List<String> parse(String input) {
    List<String> list = new ArrayList<String>();
    Matcher matcher = Pattern.compile("\"([^\"]+)\"").matcher(input);
    while (matcher.find()) {
      String founds = matcher.group(1);
      list.add(founds);
    }
    String remain = matcher.replaceAll("").trim();
    list.addAll(Arrays.asList(remain.split("\\s+")));
    return list;
  }

  @Override
  public String toString() {
    return String.format("(\"%s\", %s)", query, results);
  }
}
