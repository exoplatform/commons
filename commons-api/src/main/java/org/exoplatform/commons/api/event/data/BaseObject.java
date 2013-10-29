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
 * Gets information from a JCR node instead of opening a direct access
 * to the <code>javax.jcr.Node</code> object.
 * However, the BaseObject just provides basic information of an object that could be used in search result or somewhere else.
 * @LevelAPI Experimental
 */
public interface BaseObject {

    /**
     * Gets name of an object. The name is the
     * last element in its path, excluding any square-bracket index (if any).
     * @return The object name.
     * @LevelAPI Experimental
     */
    public String getName();

    /**
     * Gets the absolute path of the BaseObject.
     * @return The BaseObject path.
     * @LevelAPI Experimental
     */
    public String getPath();

    /**
     * Gets title of the BaseObject.
     * @return The value of exo:title property.
     * @LevelAPI Experimental
     */
    public String getTitle();

    /**
     * Gets the created date of the BaseObject.
     * @return The value of exo:dateCreated property.
     * @LevelAPI Experimental
     */
    public Calendar getCreatedDate();

    /**
     * Gets the last modified date of the BaseObject.
     * @return The value of exo:lastModifiedDate property.
     * @LevelAPI Experimental
     */
    public Calendar getLastModifiedDate();

    /**
     * Gets name of the user who did the last modification on the BaseObject.
     * @return The value of exo:lastModifier property.
     * @LevelAPI Experimental
     */
    public String getLastModifier();

    /**
     * Gets name of the user who created the BaseObject.
     * @return The value of exo:owner property.
     * @LevelAPI Experimental
     */
    public String getOwner();

    /**
     * Gets the primary type of the BaseObject.
     * For example: nt:file, exo:webContent.
     * @return The value of jcr:primartyType property.
     * @LevelAPI Experimental
     */
    public String getPrimaryType();

    /**
     * Gets a list of node type names which are added to the BaseObject as mixin.
     * @return The list of mixin names.
     * @LevelAPI Experimental
     */
    public List<String> getMixinTypes();

    /**
     * Gets the workspace name where the BaseObject is stored.
     * @return The workspace name.
     * @LevelAPI Experimental
     */
    public String getWorkspace();
    
    /**
     * Gets a list of tag names which are added to the BaseObject.
     * @return The list of tag names.
     * @LevelAPI Experimental
     */
    public List<String> getTags();
    
    /**
     * Gets the rating value for the BaseObject.
     * @return The rating value.
     * @LevelAPI Experimental
     */
    public String getRating();

    /**
     * Gets the UUID value of the BaseObject. 
	 * This is just available when the mix:referenceable mixin node type has already been added.
     * @return Value of the exo:uuid property.
     * @LevelAPI Experimental
     */
    public String getUUID();

    /**
     * Stores all information to the database.
     * @LevelAPI Experimental
     */
    public void save();

}
