package org.exoplatform.commons.api.search.data;

public class SearchResult {
  private String type;
  private String url;
  private String title;
  private String excerpt;
  private String detail;
  private String avatar;
  
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }
  
  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  
  public String getExcerpt() {
    return excerpt;
  }
  public void setExcerpt(String excerpt) {
    this.excerpt = excerpt;
  }
  
  public String getDetail() {
    return detail;
  }
  public void setDetail(String detail) {
    this.detail = detail;
  }
  
  public String getAvatar() {
    return avatar;
  }
  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }
  
  public SearchResult() {
  }
  
  public SearchResult(String type, String url) {
    this.type = type;
    this.url = url;
  }
  
}
