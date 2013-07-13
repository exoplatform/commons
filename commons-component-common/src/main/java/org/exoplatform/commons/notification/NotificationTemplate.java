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
package org.exoplatform.commons.notification;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

public class NotificationTemplate {
  private String              language;

  private String              subject;

  private String              simpleDigest;

  private String              digestOne;

  private String              digestThree;

  private String              digestMore;

  private Map<String, String> valueables;

  public NotificationTemplate() {
    valueables = new ConcurrentHashMap<String, String>();
    language = Locale.ENGLISH.getLanguage();
  }

  public static NotificationTemplate getInstance() {
    return new NotificationTemplate();
  }

  /**
   * @return the language
   */

  public String getLanguage() {
    return language;
  }

  /**
   * @param language the language to set
   */
  public NotificationTemplate setLanguage(String language) {
    this.language = language;
    return this;
  }

  /**
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * @param subject the subject to set
   */
  public NotificationTemplate setSubject(String subject) {
    this.subject = subject;
    return this;
  }

  /**
   * @return the simpleDigest
   */
  public String getSimpleDigest() {
    return simpleDigest;
  }

  /**
   * @param simpleDigest the simpleDigest to set
   */
  public NotificationTemplate setSimpleDigest(String simpleDigest) {
    this.simpleDigest = simpleDigest;
    return this;
  }

  /**
   * @return the digestOne
   */
  public String getDigestOne() {
    return digestOne;
  }

  /**
   * @param digestOne the digestOne to set
   */
  public NotificationTemplate setDigestOne(String digestOne) {
    this.digestOne = digestOne;
    return this;
  }

  /**
   * @return the digestThree
   */
  public String getDigestThree() {
    return digestThree;
  }

  /**
   * @param digestThree the digestThree to set
   */
  public NotificationTemplate setDigestThree(String digestThree) {
    this.digestThree = digestThree;
    return this;
  }

  /**
   * @return the digestMore
   */
  public String getDigestMore() {
    return digestMore;
  }

  /**
   * @param digestMore the digestMore to set
   */
  public NotificationTemplate setDigestMore(String digestMore) {
    this.digestMore = digestMore;
    return this;
  }

  /**
   * @return the valueables
   */
  public Map<String, String> getValueables() {
    return valueables;
  }

  /**
   * @param valueables the valueables to set
   */
  public NotificationTemplate setValueables(Map<String, String> valueables) {
    this.valueables = valueables;
    return this;
  }

  /**
   * @param valueables the valueables to set
   */
  public NotificationTemplate addValueables(String key, String value) {
    this.valueables.put(key, value);
    return this;
  }
  
  private String processReplace(String value) {
    for (String findKey : valueables.keySet()) {
      value = StringUtils.replace(value, findKey, valueables.get(findKey));
    }
    return value;
  }
  
  public String processSubject() {
    String subject = getSubject();
    return processReplace(subject);
  }

  public String processDigest(int size) {
    String digest = getSimpleDigest();
    if(size == 1) {
      digest = getDigestOne();
    }else if(size <= 3) {
      digest = getDigestThree();
    }
    if(size > 3) {
      digest = getDigestMore();
    }
    
    return processReplace(digest);
  }

}
