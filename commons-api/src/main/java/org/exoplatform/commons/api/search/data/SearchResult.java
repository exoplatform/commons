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
  private String url;  //url of this result
  private String title; //title to be displayed on UI
  private String excerpt; //the excerpt to be displayed on UI
  private String detail; //details information
  private String imageUrl; //an image to be displayed on UI
  private long date; //created or modified date, for sorting on UI
  private long relevancy; //the result's relevancy, for sorting on UI
  
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
  
  public String getImageUrl() {
    return imageUrl;
  }
  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }
  
  public long getDate() {
    return date;
  }
  public void setDate(long date) {
    this.date = date;
  }
  
  public long getRelevancy() {
    return relevancy;
  }
  public void setRelevancy(long relevancy) {
    this.relevancy = relevancy;
  }
  
  public SearchResult(String url, String title, String excerpt, String detail, String imageUrl, long date, long relevancy) {
    this.url = url;
    this.title = title;
    this.excerpt = excerpt;
    this.detail = detail;
    this.imageUrl = imageUrl;
    this.date = date;
    this.relevancy = relevancy;
  }
  @Override
  public String toString() {
    return String.format("SearchResult {url=%s, relevancy=%s}", url, relevancy);
  }
}
