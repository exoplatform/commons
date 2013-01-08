package org.exoplatform.commons.search.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
    List<String> paramValues = new ArrayList<String>();

    String[] rhsPatterns = {"\"([^\"]+)\"", "([\\S]+)"};
    
    for(String pattern:rhsPatterns){
      Matcher matcher = Pattern.compile("\\b" + param + "\\s*=\\s*" + pattern).matcher(query);
      while (matcher.find()) {
        String founds = matcher.group(1);
        paramValues.addAll(Arrays.asList(founds.split(";\\s*")));
      }
      query = matcher.replaceAll("").trim();
    }
    
    results = paramValues;
    return this;
  }

  public static List<String> parse(String input) {
    List<String> terms = new ArrayList<String>();
    Matcher matcher = Pattern.compile("\"([^\"]+)\"").matcher(input);
    while (matcher.find()) {
      String founds = matcher.group(1);
      terms.add(founds);
    }
    String remain = matcher.replaceAll("").replaceAll("\"", "").trim(); //remove all remaining double quotes
    if(!remain.isEmpty()) terms.addAll(Arrays.asList(remain.split("\\s+")));
    return terms;
  }

  public static String repeat(String format, Collection<String> strArr, String delimiter){
    StringBuilder sb=new StringBuilder();
    String delim = "";
    for(String str:strArr) {
      sb.append(delim).append(String.format(format, str));
      delim = delimiter;
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    return String.format("(\"%s\", %s)", query, results);
  }
}
