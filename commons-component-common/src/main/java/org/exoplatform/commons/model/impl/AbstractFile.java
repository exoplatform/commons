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
package org.exoplatform.commons.model.impl;

import java.io.InputStream;
import java.util.Date;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.exoplatform.commons.api.event.data.File;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 25, 2012
 * 3:26:30 PM  
 */
public abstract class AbstractFile<T> extends AbtractBaseObject implements File {

    public AbstractFile(String workspace, String path) {
        super(workspace, path);
    }

    public AbstractFile(String workspace, String path, boolean isSystem) {
        super(workspace, path, isSystem);
    }  
    
    /**
     * {@inheritDoc}
     */
    public InputStream getData() {
        try {
            return getResource().getProperty("jcr:data").getStream();
        } catch (ValueFormatException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        } catch (PathNotFoundException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        } catch (RepositoryException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        }
        return null;
    }

    /**
     * Get Object Parent
     * @throws RepositoryException 
     * @throws PathNotFoundException 
     * @throws AccessDeniedException 
     * @throws ItemNotFoundException 
     */
    public abstract T getParent() throws ItemNotFoundException, AccessDeniedException, PathNotFoundException, RepositoryException;  

    /**
     * {@inheritDoc}
     */
    public String getMimeType() {
        try {
            return getResource().getProperty("jcr:mimeType").getString();
        } catch (ValueFormatException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        } catch (PathNotFoundException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        } catch (RepositoryException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        }
        return null;
    }

    /**
     * Return the child node jcr:content of <code>File</code> instance.
     * @return The <code>javax.jcr.Node</code> object.
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    private Node getResource() throws PathNotFoundException, RepositoryException {
        return getJCRNode().getNode("jcr:content");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getLastModified() {
        try {
            return getResource().getProperty("jcr:lastModified").getDate().getTime();
        } catch (ValueFormatException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        } catch (PathNotFoundException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        } catch (RepositoryException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEncoding() {
        try {
            return getResource().getProperty("jcr:encoding").getString();
        } catch (ValueFormatException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        } catch (PathNotFoundException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        } catch (RepositoryException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDCCreator() {
        try {
            return getResource().getProperty("dc:creator").getString();
        } catch (ValueFormatException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        } catch (PathNotFoundException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        } catch (RepositoryException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDCDescription() {
        try {
            return getResource().getProperty("dc:description").getString();
        } catch (ValueFormatException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        } catch (PathNotFoundException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        } catch (RepositoryException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDCContributor() {
        try {
            return getResource().getProperty("jcr:contributor").getString();
        } catch (ValueFormatException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        } catch (PathNotFoundException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        } catch (RepositoryException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDCPublisher() {
        try {
            return getResource().getProperty("dc:publisher").getString();
        } catch (ValueFormatException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        } catch (PathNotFoundException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        } catch (RepositoryException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDCSubject() {
        try {
            return getResource().getProperty("dc:subject").getString();
        } catch (ValueFormatException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        } catch (PathNotFoundException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        } catch (RepositoryException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getDCDate() {
        try {
            return getResource().getProperty("dc:date").getDate().getTime();
        } catch (ValueFormatException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        } catch (PathNotFoundException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        } catch (RepositoryException e) {
            if(LOG.isDebugEnabled()) {
                LOG.error(e);
            }
        }
        return null;
    }

}
