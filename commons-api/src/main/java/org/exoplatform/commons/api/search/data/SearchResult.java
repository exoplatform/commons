package org.exoplatform.commons.api.search.data;

/**
 * Search result returned by SearchService and all of its connectors, for rendering their search results on UI in a unified format.
 *   
 * @LevelAPI Experimental  
 */
public class SearchResult {
  private String url;  //url of this result
  private String previewUrl;  //preview url of this result
  private String title; //title to be displayed on UI
  private String excerpt; //the excerpt to be displayed on UI
  private String detail; //details information
  private String imageUrl; //an image to be displayed on UI
  private long date; //created or modified date, for sorting on UI
  private long relevancy; //the result's relevancy, for sorting on UI
  
  /**
   * Get url of result 
   * @return String
   * @LevelAPI Experimental
   */
  public String getUrl() {
    return url;
  }
  /**
   * Set url for result
   * @param url
   * @LevelAPI Experimental
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Get preview url of result
   * @return String
   * @LevelAPI Experimental
   */
  public String getPreviewUrl() {
    return previewUrl;
  }
  /**
   * Set preview url for result
   * @param previewUrl
   * @LevelAPI Experimental
   */
  public void setPreviewUrl(String previewUrl) {
    this.previewUrl = previewUrl;
  }

  /**
   * Get title of result
   * @return String
   * @LevelAPI Experimental
   */
  public String getTitle() {
    return title;
  }
  /**
   * Set title for result
   * @param title
   * @LevelAPI Experimental
   */
  public void setTitle(String title) {
    this.title = title;
  }
  
  /**
   * Get excerpt of result
   * @return String
   * @LevelAPI Experimental
   */
  public String getExcerpt() {
    return excerpt;
  }
  /**
   * Set excerpt for result
   * @param excerpt
   * @LevelAPI Experimental
   */
  public void setExcerpt(String excerpt) {
    this.excerpt = excerpt;
  }
  
  /**
   * Get detail of result
   * @return String
   * @LevelAPI Experimental
   */
  public String getDetail() {
    return detail;
  }
  /**
   * Set detail for result
   * @param detail
   * @LevelAPI Experimental
   */
  public void setDetail(String detail) {
    this.detail = detail;
  }
  
  /**
   * Get image url of avatar
   * @return String
   * @LevelAPI Experimental
   */
  public String getImageUrl() {
    return imageUrl;
  }
  /**
   * Set image url for avatar
   * @param imageUrl
   * @LevelAPI Experimental
   */
  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }
  /**
   * Get date of result
   * @return Long
   * @LevelAPI Experimental
   */
  public long getDate() {
    return date;
  }
  /**
   * Set data for result
   * @param date
   * @LevelAPI Experimental
   */
  public void setDate(long date) {
    this.date = date;
  }
  /**
   * Get relevancy of result
   * @return Long
   * @LevelAPI Experimental
   */
  public long getRelevancy() {
    return relevancy;
  }
  /**
   * Set relevancy
   * @param relevancy
   * @LevelAPI Experimental
   */
  public void setRelevancy(long relevancy) {
    this.relevancy = relevancy;
  }
  
  /**
   * Constructor that helps to create search result by the unique way
   * @param url Url of this result
   * @param title Title to be displayed on UI
   * @param excerpt The excerpt to be displayed on UI
   * @param detail Details information
   * @param imageUrl An image to be displayed on UI
   * @param date Created or modified date, for sorting on UI
   * @param relevancy The result's relevancy, for sorting on UI
   * @LevelAPI Experimental
   */
  public SearchResult(String url, String title, String excerpt, String detail, String imageUrl, long date, long relevancy) {
    this.url = url;
    this.title = title;
    this.excerpt = excerpt;
    this.detail = detail;
    this.imageUrl = imageUrl;
    this.date = date;
    this.relevancy = relevancy;
  }

  /**
   * Constructor that helps to create search result by the unique way
   * (keeping the other constructor without previewUrl for backward compatibility reasons)
   * @param url Url of this result
   * @param previewUrl Preview url of this result
   * @param title Title to be displayed on UI
   * @param excerpt The excerpt to be displayed on UI
   * @param detail Details information
   * @param imageUrl An image to be displayed on UI
   * @param date Created or modified date, for sorting on UI
   * @param relevancy The result's relevancy, for sorting on UI
   * @LevelAPI Experimental
   */
  public SearchResult(String url, String previewUrl, String title, String excerpt, String detail, String imageUrl, long date, long relevancy) {
    this(url, title, excerpt, detail, imageUrl, date, relevancy);
    this.previewUrl = previewUrl;
  }

  @Override
  public String toString() {
    return String.format("SearchResult {url=%s, relevancy=%s}", url, relevancy);
  }
}
