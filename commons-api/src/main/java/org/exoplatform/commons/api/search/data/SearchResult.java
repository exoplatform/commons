package org.exoplatform.commons.api.search.data;

/**
 * Search result returned by SearchService and all of its connectors, for rendering their search results on UI in a unified format
 *  
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Nov 21, 2012  
 */
public class SearchResult {
  private String type; //the search type (e.g people, wiki...)
  private String url;  //url of this result
  private String title; //title to be displayed on UI
  private String excerpt; //the excerpt to be displayed on UI
  private String detail; //details information
  private String avatar; //an image to be displayed on UI
  
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
