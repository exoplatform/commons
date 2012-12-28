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

  public QueryParser parseFor(String type) {
    List<String> list = new ArrayList<String>();
    Matcher matcher = Pattern.compile("\\b" + type + "\\s*=\\s*([\\S]+)").matcher(query);
    while (matcher.find()) {
      String founds = matcher.group(1);
      list.addAll(Arrays.asList(founds.split("[;]")));
    }
    query = matcher.replaceAll("").trim();
    results = list;
    return this;
  }

  @Override
  public String toString() {
    return String.format("(\"%s\", %s)", query, results);
  }
}
