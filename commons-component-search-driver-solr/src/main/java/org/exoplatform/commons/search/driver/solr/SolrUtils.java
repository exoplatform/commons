/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.search.driver.solr;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by The eXo Platform SAS
 * Author : Tung Vu Minh
 *          tungvm@exoplatform.com
 * Nov 21, 2012  
 */
public class SolrUtils {
  
  public static Map<String, Object> fromDynamicFields(Map<String, Object> dynaFields){
    HashMap<String, Object> fields = new HashMap<String, Object>();
    Iterator<String> iter = dynaFields.keySet().iterator();
    while(iter.hasNext()){
      String dynaKey = iter.next();
      String key = dynaKey.matches("^.+_[tlbfd]$") ? dynaKey.substring(0, dynaKey.length()-2) : dynaKey; 
      fields.put(key, dynaFields.get(dynaKey));
    }
    return fields;
  }

  public static Map<String, Object> toDynamicFields(Map<String, Object> fields){
    HashMap<String, Object> dynaFields = new HashMap<String, Object>();
    Iterator<String> iter = fields.keySet().iterator();
    while(iter.hasNext()){
      String key = iter.next();
      Object val = fields.get(key);
      if(val instanceof String)
        dynaFields.put(key+"_t", val);
      else if(val instanceof Long)
        dynaFields.put(key+"_l", val);
      else if(val instanceof Boolean)
        dynaFields.put(key+"_b", val);
      else if(val instanceof Float)
        dynaFields.put(key+"_f", val);
      else if(val instanceof Double)
        dynaFields.put(key+"_d", val);
    }
    return dynaFields;
  }
  
}
