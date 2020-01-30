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
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.mockito.Mockito;

public class EmbedderTest extends BaseCommonsTestCase {

  private static final Log LOG = ExoLogger.getLogger(EmbedderTest.class);
  
  private Embedder embedder;
 
  public EmbedderTest() throws Exception {
  }
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    WebuiRequestContext context = Mockito.mock(WebuiRequestContext.class);
    WebuiRequestContext.setCurrentInstance(context);
    PortalRequestContext ctx = Mockito.mock(PortalRequestContext.class);
    Mockito.when(Util.getPortalRequestContext()).thenReturn(ctx);
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
   * Test if slideShare link can be shared as video in AS
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
    assertRetrurnedData("http://www.slideshare.net/thanhc0110m/social-media-trends-exo-platform-company");
    assertRetrurnedData("https://www.slideshare.net/thanhc0110m/social-media-trends-exo-platform-company");
    assertRetrurnedData("http://www.slideshare.net/slideshow/embed_code/43654545");
    assertRetrurnedData("https://www.slideshare.net/slideshow/embed_code/43654545");
  }

  public void testDailyMotionShare() {
    String dailyMotion = "http://www.dailymotion.com/video/x2g4jx_exo-platform-theserverside-video-te_news";
    // Dailymotion oembed response
    embedder = EmbedderFactory.getInstance(dailyMotion);
    ExoMedia dailyObj = embedder.getExoMedia();
    if(dailyObj == null) {
      LOG.warn("Can't connect to dailymotion"); 
    }
    assertRetrurnedData("http://www.dailymotion.com/video/x2g4jx_exo-platform-theserverside-video-te_news");
    assertRetrurnedData("https://www.dailymotion.com/video/x2g4jx_exo-platform-theserverside-video-te_news");
    assertRetrurnedData("http://dai.ly/x2g4jx");
    assertRetrurnedData("https://dai.ly/x2g4jx");
    assertRetrurnedData("http://www.dailymotion.com/embed/video/x2g4jx");
    assertRetrurnedData("https://www.dailymotion.com/embed/video/x2g4jx");
  }

  public void testVimeoShare() {
    String dailyMotion = "http://vimeo.com/72407771";
    // Vimeo oembed response
    embedder = EmbedderFactory.getInstance(dailyMotion);
    ExoMedia vimeoObj = embedder.getExoMedia();
    if(vimeoObj == null) {
      LOG.warn("Can't connect to vimeo"); 
    }
    assertRetrurnedData("http://vimeo.com/72407771");
    assertRetrurnedData("https://vimeo.com/72407771");
    assertRetrurnedData("http://player.vimeo.com/video/72407771");
    assertRetrurnedData("https://player.vimeo.com/video/72407771");
  }

  /**
   * test flick link
   */
  public void testFlickr() {
    String flickrURL = "https://www.flickr.com/photos/saarblitz/15515790937";
    // slideshare oembed response
    embedder = EmbedderFactory.getInstance(flickrURL);
    ExoMedia slideObj = embedder.getExoMedia();
    if(slideObj == null) {
      LOG.warn("Can't connect to flickr");
    } else {
      assertEquals("Flickr", slideObj.getProvider());
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
    assertNotNull(videoObj);
    assertNotNull(videoObj.getTitle());
    assertNotNull(videoObj.getHtml());
    assertNotNull(videoObj.getDescription());
  }
}
