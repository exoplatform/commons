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

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MemoryProvider implements ResourceProvider {

  protected final Map<String, byte[]> store;

  protected String                    location;

  public MemoryProvider() {
    this(new HashMap<String, byte[]>());
  }

  public MemoryProvider(Map<String, byte[]> store) {
    this.store = store;
    this.location = "java:" + System.identityHashCode(this);
  }

  public boolean exists(String name) {
    return store.containsKey(name);
  }

  public byte[] getBytes(String name) {
    return store.get(name);
  }

  public InputStream getStream(String name) {
    byte[] data = store.get(name);
    return data == null ? null : new ByteArrayInputStream(data);
  }

  @Override
  public String getFilePath(FileInfo fileInfo) throws IOException {
    return "java";
  }

  public URL getURL(String name) {
    return null;
  }

  public long lastModified(String name) {
    return 0;
  }

  public void put(String name, InputStream data) throws IOException {
    store.put(name, FileUtils.readBytes(data));
  }

  @Override
  public void put(FileItem fileItem) throws IOException {
    store.put(fileItem.getFileInfo().getChecksum(), FileUtils.readBytes(fileItem.getAsStream()));
  }

  public void put(String name, byte[] data) throws IOException {
    store.put(name, data);
  }

  public void remove(String name) {
    store.remove(name);
  }

  @Override
  public boolean remove(FileInfo fileInfo) throws FileNotFoundException {
    return (store.remove(fileInfo.getChecksum())) != null;
  }

  public String getLocation() {
    return "java";
  }

  @Override
  public String toString() {
    return getLocation();
  }
}
