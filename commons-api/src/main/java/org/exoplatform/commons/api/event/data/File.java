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
 * File object instance.
 * @LevelAPI Experimental
 */
public interface File extends BaseObject {

    /**
     * Return the binary data of <code>File</code> instance
     * @return The value of jcr:data property which stored inside jcr:content node.
     * @LevelAPI Experimental
     */
    public InputStream getData();

    /**
     * Return the MIMETYPE of <code>File</code> instance.
     * @return The value of jcr:mimeType property which stored inside jcr:content node.
     * @LevelAPI Experimental
     */
    public String getMimeType();
    
    /**
     * Return the last modified date of <code>File</code> instance.
     * @return The value of jcr:lastModified property which stored inside jcr:content node.
     * @LevelAPI Experimental
     */
    public Date getLastModified();
    
    /**
     * Return the encoding type of <code>File</code> instance.
     * @return The value of jcr:encoding property which stored inside jcr:content node.
     * @LevelAPI Experimental
     */
    public String getEncoding();
    
    /**
     * Return the creator of <code>File</code> instance.
     * @return The value of dc:creator property which stored inside dc:elementSet mix-in node type.
     * @LevelAPI Experimental
     */
    public String getDCCreator();
    
    /**
     * Return the description of <code>File</code> instance.
     * @return The value of dc:description property which stored inside dc:elementSet mix-in node type.
     * @LevelAPI Experimental
     */
    public String getDCDescription();
    
    /**
     * Return the contributor of <code>File</code> instance.
     * @return The value of dc:contributor property which stored inside dc:elementSet mix-in node type.
     * @LevelAPI Experimental
     */
    public String getDCContributor();
    
    /**
     * Return the publisher of <code>File</code> instance.
     * @return The value of dc:publisher property which stored inside dc:elementSet mix-in node type.
     * @LevelAPI Experimental
     */
    public String getDCPublisher();
    
    /**
     * Return the subject of <code>File</code> instance.
     * @return The value of dc:subject property which stored inside dc:elementSet mix-in node type.
     * @LevelAPI Experimental
     */
    public String getDCSubject();
    
    /**
     * Return the last updated date of <code>File</code> instance.
     * @return The value of dc:date property which stored inside dc:elementSet mix-in node type.
     * @LevelAPI Experimental
     */
    public Date getDCDate();
    
}
