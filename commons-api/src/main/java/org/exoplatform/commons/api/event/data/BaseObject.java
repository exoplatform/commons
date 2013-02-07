/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
 *
 **************************************************************************/
package org.exoplatform.commons.api.event.data;

import java.util.Calendar;
import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 2, 2012
 * 2:13:57 PM  
 */
/**
 * This Object will be used to get information from a JCR node instead of open a direct access 
 * to <code>javax.jcr.Node</code> object.
 * However, it just provide the basic information of an object which could be used in search result or somewhere else.
 *
 */
public interface BaseObject {

    /**
     * Return the name of Object. The name of an object is the
     * last element in its path, minus any square-bracket index that may exist.
     * @return The name of this Object.
     * 
     */
    public String getName();

    /**
     * * Returns the absolute path to this item.  
     * @return The path of this Object.
     */
    public String getPath();

    /**
     * Return the title value on this Object.
     * @return The value of exo:title property.
     */
    public String getTitle();

    /**
     * Return the value of the created date on this Object.
     * @return The value of exo:dateCreated property.
     */
    public Calendar getCreatedDate();

    /**
     * Return the value of the last modified date on this Object.
     * @return The value of exo:lastModifiedDate property.
     */
    public Calendar getLastModifiedDate();

    /**
     * Return the name of last person who edited this Object. 
     * @return The value of exo:lastModifier property.
     */
    public String getLastModifier();

    /**
     * Return the name of who created this Object.
     * @return The value of exo:owner property
     */
    public String getOwner();

    /**
     * Return the primary type of current Object
     * For example: nt:file, exo:webContent,...
     * @return The value of jcr:primartyType property.
     */
    public String getPrimaryType();

    /**
     * Return a list of node type name which added to this Object as the mix-in.
     * @return List<String> A list contains the mix-in name.
     */
    public List<String> getMixinTypes();

    /**
     * Return the workspace name where stored current object.
     * @return The name of current workspace
     */
    public String getWorkspace();
    
    /**
     * Return a list of tags name which added to this Object
     * @return A list of tagged value.
     */
    public List<String> getTags();
    
    /**
     * Return the rate value for this Object
     * @return The rating number
     */
    public String getRating();

    /**
     * Return the UUID value of this Object. This one just available when already added the mix:referenceable mix-in node type.
     * @return The value of exo:uuid property
     */
    public String getUUID();

    /**
     * This function used to store all the information to the database.
     */
    public void save();

}
