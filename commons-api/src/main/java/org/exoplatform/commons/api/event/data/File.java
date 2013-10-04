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

import java.io.InputStream;
import java.util.Date;

/**
 * An abstract representation of file.
 * @LevelAPI Experimental
 */
public interface File extends BaseObject {

    /**
     * Gets the binary data of the <code>File</code> instance.
     * @return Value of the jcr:data property which is stored inside the jcr:content node.
     * @LevelAPI Experimental
     */
    public InputStream getData();

    /**
     * Gets the MIMETYPE of the <code>File</code> instance.
     * @return Value of the jcr:mimeType property which is stored inside the jcr:content node.
     * @LevelAPI Experimental
     */
    public String getMimeType();
    
    /**
     * Gets the last modified date of the <code>File</code> instance.
     * @return Value of the jcr:lastModified property which is stored inside the jcr:content node.
     * @LevelAPI Experimental
     */
    public Date getLastModified();
    
    /**
     * Gets the encoding type of the <code>File</code> instance.
     * @return Value of the jcr:encoding property which is stored inside the jcr:content node.
     * @LevelAPI Experimental
     */
    public String getEncoding();
    
    /**
     * Gets the creator of the <code>File</code> instance.
     * @return Value of the dc:creator property which is stored inside the dc:elementSet mixin node type.
     * @LevelAPI Experimental
     */
    public String getDCCreator();
    
    /**
     * Gets the description of the <code>File</code> instance.
     * @return Value of the dc:description property which is stored inside the dc:elementSet mixin node type.
     * @LevelAPI Experimental
     */
    public String getDCDescription();
    
    /**
     * Gets the contributor of the <code>File</code> instance.
     * @return Value of the dc:contributor property which is stored inside the dc:elementSet mixin node type.
     * @LevelAPI Experimental
     */
    public String getDCContributor();
    
    /**
     * Gets the publisher of the <code>File</code> instance.
     * @return Value of the dc:publisher property which is stored inside the dc:elementSet mixin node type.
     * @LevelAPI Experimental
     */
    public String getDCPublisher();
    
    /**
     * Gets the subject of the <code>File</code> instance.
     * @return Value of the dc:subject property which is stored inside the dc:elementSet mixin node type.
     * @LevelAPI Experimental
     */
    public String getDCSubject();
    
    /**
     * Gets the last updated date of the <code>File</code> instance.
     * @return Value of the dc:date property which is stored inside the dc:elementSet mixin node type.
     * @LevelAPI Experimental
     */
    public Date getDCDate();
    
}
