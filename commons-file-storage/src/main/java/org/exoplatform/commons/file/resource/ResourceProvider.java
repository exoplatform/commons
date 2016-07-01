/*
 * Copyright (C) 2016 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.commons.file.resource;

import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.model.FileItem;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * When implementing a resource store you should implement equals and hashCode method. A store is equals to another if
 * the store location is the same.
 *
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.co
 */
public interface ResourceProvider {

    void put(final String name, final InputStream data) throws IOException;

    void put(final FileItem fileItem) throws IOException;

    void put(final String name, final byte[] data) throws IOException;

    InputStream getStream(final String name);

    public String getFilePath(final FileInfo fileInfo) throws IOException;

    byte[] getBytes(final String name);

    void remove(final String name) throws IOException;

    public boolean remove(final FileInfo fileInfo) throws IOException;

    boolean exists(final String name) throws IOException;

    long lastModified(String name) throws IOException;

    URL getURL(final String name);

    /**
     * A string that uniquely identify the location of that store. Two stores are considered equals if their locations
     * are the same.
     */
    String getLocation();

}