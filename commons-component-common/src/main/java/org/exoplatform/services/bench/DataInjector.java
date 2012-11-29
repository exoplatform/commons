/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.bench;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * 
 * @Author : <a href="mailto:quanglt@exoplatform.com">Le Thanh Quang</a> Jul 20,
 *         2011
 */
public abstract class DataInjector extends BaseComponentPlugin {

  private List<String> users   = Arrays.asList(new String[] { "root", "demo", "mary", "john" });

  private Random       rand    = new Random();

  private LoremIpsum4J textGen = new LoremIpsum4J();
  
  public static final String             ARRAY_SPLIT   = ",";
  
  private String restId;
  
  public String getRestId() {
    return restId;
  }

  public void setRestId(String restId) {
    this.restId = restId;
  }
  
  /**
   * get log object.
   * @return
   */
  public abstract Log getLog();

  /**
   * This function should be implemented to execute tasks that require to response data to client.
   * <br>
   * @param params query parameters of a HTTP GET request.
   * @return object that can be serialized to JSON object.
   * @throws Exception
   */
  public abstract Object execute(HashMap<String , String> params) throws Exception;
  
  /**
   * This function should be implemented to inject data into the product.
   * @param params parameters for injecting. They can be query parameters of a HTTP GET request.  
   * @throws Exception
   */
  public abstract void inject(HashMap<String , String> params) throws Exception;
  
  /**
   * This function should be implemented to clear data that is injected before by {@link #inject()}.
   * @param params parameters for rejecting. They can be query parameters of a HTTP GET request.
   * @throws Exception
   */
  public abstract void reject(HashMap<String , String> params) throws Exception;

  /**
   * get pseudo words.
   * @param amount number of words
   * @return pseudo words
   */
  public final String words(int amount) {
    return textGen.getWords(amount);
  }
  
  /**
   * get pseudo paragraphs.
   * @param amount number of paragraphs
   * @return pseudo paragraphs
   */
  public final String paragraphs(int amount) {
    return textGen.getParagraphs(amount);
  }
  
  /**
   * get random user id.
   */
  public final String randomUser() {
    return users.get(rand.nextInt(4));
  }

  /**
   * get random words.
   * 
   * @param i maximum number of words. the number of words is between 0 and i.
   * @return
   */
  public final String randomWords(int i) {
    int wordCount = rand.nextInt(i + 1) + 1;
    String words = textGen.getWords(wordCount);
    return words;
  }

  /**
   * get random paragraphs
   * 
   * @param i maximum number of paragraphs.
   * @return
   */
  public final String randomParagraphs(int i) {
    int paragraphCount = rand.nextInt(i + 1) + 1;
    String paragraphs = textGen.getParagraphs(paragraphCount);
    return paragraphs.replaceAll("\\n\\n", "<br/><br/>");
  }
  
  /**
   * create text/plain resource by size.
   * @param size in kilobyte
   * @return
   */
  public String createTextResource(int size) {
    int sizeInByte = size * 1024; // byte
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < sizeInByte; i++) {
      sb.append("A"); // each A character spends one byte in UTF-8.
    }
    return sb.toString();
  }  

  public List<String> readGroupsIfExist(HashMap<String, String> queryParams) {
    List<String> groups = new LinkedList<String>();
    String value = queryParams.get("groups");
    if (value != null) {
      String[] groupsString = value.split(ARRAY_SPLIT);
      for (String s : groupsString) {
        if (s.length() > 0)
          groups.add(s.trim());
      }
    }
    return groups;
  }
  
  public List<String> readUsersIfExist(HashMap<String, String> queryParams) {
    List<String> users = new LinkedList<String>();
    String value = queryParams.get("users");
    if (value != null) {
      String[] groupsString = value.split(ARRAY_SPLIT);
      for (String s : groupsString) {
        if (s.length() > 0)
          users.add(s.trim());
      }
    }
    return users;    
  }  

  public List<String> readMembershipIfExist(HashMap<String, String> queryParams) {
    List<String> memberships = new LinkedList<String>();
    
    String value = queryParams.get("memship");
    if (value != null) {
      String[] memshipsString = value.split(ARRAY_SPLIT);
      for (String s : memshipsString) {
        if (s.length() > 0)
          memberships.add(s.trim());
      }
    }
    return memberships;
  }
  
}
