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
package org.exoplatform.commons.api.notification.plugin;

import java.util.HashMap;
import java.util.Map;

public class MappingKey {
  /*
      String srcResource = mappingKey.getSrcResouce();
    String subjectKey = mappingKey.getKeyValue("subject", getDefaultKey(DEFAULT_SUBJECT_KEY, providerId));
    String templateKey = mappingKey.getKeyValue("template", getDefaultKey(DEFAULT_TEMPLATE_KEY, providerId));
    String digestKey = mappingKey.getKeyValue("digest", getDefaultKey(DEFAULT_SIMPLE_DIGEST_KEY, providerId));
    String digestOneKey = mappingKey.getKeyValue("digest.one", getDefaultKey(DEFAULT_DIGEST_ONE_KEY, providerId));
    String digestThreeKey = mappingKey.getKeyValue("digest.three", getDefaultKey(DEFAULT_DIGEST_THREE_KEY, providerId));
    String digestMoreKey = mappingKey.getKeyValue("digest.more", getDefaultKey(DEFAULT_DIGEST_MORE_KEY, providerId));
    String footer = mappingKey.getKeyValue("footer", getDefaultKey(DEFAULT_FOOTER_KEY, providerId));
  
  */
  public static final String DEFAULT_SRC_RESOURCE_KEY  = "locale.notification.template.NotificationTemplate";

  public static final String  SUBJECT_KEY              = "subject";

  public static final String  TEMPLATE_KEY             = "template";

  public static final String  DIGEST_KEY               = "digest";

  public static final String  DIGEST_ONE_KEY           = "digest.one";

  public static final String  DIGEST_THREE_KEY         = "digest.three";

  public static final String  DIGEST_MORE_KEY          = "digest.more";

  public static final String  FOOTER_KEY               = "footer";
  
  private String providerId;
  private String srcResouce;
  private Map<String, String> keyMapping = new HashMap<String, String>();
  public MappingKey() {
    srcResouce = DEFAULT_SRC_RESOURCE_KEY;
  }
  /**
   * @return the providerId
   */
  public String getProviderId() {
    return providerId;
  }
  /**
   * @param providerId the providerId to set
   */
  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }
  
  /**
   * @return the srcResouce
   */
  public String getSrcResouce() {
    return srcResouce;
  }
  /**
   * @param srcResouce the srcResouce to set
   */
  public void setSrcResouce(String srcResouce) {
    this.srcResouce = srcResouce;
  }
  /**
   * @return the keyMapping
   */
  public Map<String, String> getKeyMapping() {
    return keyMapping;
  }
  /**
   * @param keyMapping the keyMapping to set
   */
  public void setKeyMapping(Map<String, String> keyMapping) {
    this.keyMapping = keyMapping;
  }

  public MappingKey addKeyMapping(String key, String value) {
    this.keyMapping.put(key, value);
    return this;
  }
  
  public String getKeyValue(String key, String defaultValue) {
    if (keyMapping.containsKey(key)) {
      return keyMapping.get(key);
    }
    return defaultValue;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof MappingKey) {
      return ((MappingKey) obj).getProviderId().equals(this.getProviderId());
    }
    return false;
  }
  
}
