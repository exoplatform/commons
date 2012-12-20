package org.exoplatform.commons.search;

public class SearchResult {
  private String type;
  private String html;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getHtml() {
    return html;
  }

  public void setHtml(String html) {
    this.html = html;
  }

  @Override
  public String toString() {
    return html;
  }
}
