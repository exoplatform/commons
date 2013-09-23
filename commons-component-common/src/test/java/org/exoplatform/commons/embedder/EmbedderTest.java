/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.embedder;

import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class EmbedderTest extends BaseCommonsTestCase {

  private static final Log LOG = ExoLogger.getLogger(EmbedderTest.class);
  
  private Embedder embedder;
 
  public EmbedderTest() throws Exception {
  }
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  /**
   * Test youtube link
   */
  public void testYoutube() {
    String youTubeURL = "http://www.youtube.com/watch?v=CZUXUjhXzDo";
    // youtube video link, exist media object
    embedder = EmbedderFactory.getInstance(youTubeURL);
    ExoMedia videoObj = embedder.getExoMedia();
    if(videoObj == null) {
      LOG.warn("Can't connect to youtube");
      return;
    } 
    
    assertRetrurnedData("http://www.youtube.com/watch?v=CZUXUjhXzDo");
    assertRetrurnedData("http://www.youtube.com/watch?feature=player_embedded&v=mhu0cNjWE8I");
    assertRetrurnedData("http://youtu.be/mhu0cNjWE8I");
    assertRetrurnedData("http://www.youtube.com/embed/mhu0cNjWE8I");
    assertRetrurnedData("http://m.youtube.com/watch?v=mhu0cNjWE8I");
    assertRetrurnedData("https://www.youtube.com/watch?v=CZUXUjhXzDo");
    assertRetrurnedData("https://www.youtube.com/watch?feature=player_embedded&v=mhu0cNjWE8I");
    assertRetrurnedData("https://youtu.be/mhu0cNjWE8I");
    assertRetrurnedData("https://www.youtube.com/embed/mhu0cNjWE8I");
    assertRetrurnedData("https://m.youtube.com/watch?v=mhu0cNjWE8I");
  } 
  
  /**
   * test slideshare link
   */
  public void testSlideShare() {
    String slideShareURL = "http://www.slideshare.net/sh1mmer/using-nodejs-to-make-html5-work-for-everyone";
    // slideshare oembed response
    embedder = EmbedderFactory.getInstance(slideShareURL);
    ExoMedia slideObj = embedder.getExoMedia();
    if(slideObj == null) {
      LOG.warn("Can't connect to slideshare");
    } else {
      assertEquals("SlideShare", slideObj.getProvider());
    }
  }
  
  /**
   * test links that dont match any url schemes
   */
  public void testNonMediaLink() {
    String googleSiteURL = "www.google.com";
    // whatever link that does not match any url schemes
    embedder = EmbedderFactory.getInstance(googleSiteURL);
    assertNull(embedder.getExoMedia());
    
    // youtube homepage, get no media object
    String youtubeSiteURL = "www.youtube.com";
    embedder = EmbedderFactory.getInstance(youtubeSiteURL);
    assertNull(embedder.getExoMedia());
  }
  
  private void assertRetrurnedData(String youTubeURL) {
    embedder = EmbedderFactory.getInstance(youTubeURL);
    ExoMedia videoObj = embedder.getExoMedia();
    assertNotNull(videoObj.getTitle());
    assertNotNull(videoObj.getHtml());
    assertNotNull(videoObj.getDescription());
  }
}
