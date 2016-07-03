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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;

import java.io.*;
import java.net.URL;
import java.security.PrivilegedAction;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 */

public class FileSystemResourceProvider implements ResourceProvider {

  private static final Log    log               = LogFactory.getLog(FileSystemResourceProvider.class);

  private static final String ROOT_PATH_PARAM   = "rootPath";

  protected File              root;

  private TreeFileIOChannel   treeFileIOChannel = null;

  public FileSystemResourceProvider(String rootPath) throws Exception {
    if (StringUtils.isEmpty(rootPath)) {
      throw new Exception("Init param '" + ROOT_PATH_PARAM + "' not defined for " + getClass().getSimpleName());
    }

    if ("/".equals(rootPath)) {
      rootPath = "";
    } else if (!rootPath.endsWith("/")) {
      rootPath = rootPath + "/";
    }
    root = new File(rootPath);
    if (!FileUtils.exists(root)) {
      FileUtils.mkdirs(root);
    }
    treeFileIOChannel = new TreeFileIOChannel(new File(rootPath));
  }

  public FileSystemResourceProvider(InitParams initParams) throws Exception {
    if (initParams == null) {
      throw new IllegalArgumentException("Init params cannot be null, it must define file system root path in '" + ROOT_PATH_PARAM
          + "' value param");
    }
    ValueParam rootPathValueParam = initParams.getValueParam(ROOT_PATH_PARAM);
    if (rootPathValueParam == null) {
      throw new Exception("Missing init param '" + ROOT_PATH_PARAM + "' for " + getClass().getSimpleName());
    }
    String rootPath = rootPathValueParam.getValue();
    if (StringUtils.isEmpty(rootPath)) {
      throw new Exception("Init param '" + ROOT_PATH_PARAM + "' not defined for " + getClass().getSimpleName());
    }

    if ("/".equals(rootPath)) {
      rootPath = "";
    } else if (!rootPath.endsWith("/")) {
      rootPath = rootPath + "/";
    }
    root = new File(rootPath);
    if (!FileUtils.exists(root)) {
      FileUtils.mkdirs(root);
    }
    treeFileIOChannel = new TreeFileIOChannel(new File(rootPath));
  }

  public File getRoot() {
    return root;
  }

  public final File getFile(String name) throws IOException {
    return treeFileIOChannel.getFile(name);
  }

  public boolean exists(String name) throws IOException {
    return FileUtils.exists(getFile(name));
  }

  public long lastModified(String name) throws IOException {
    return getFile(name).lastModified();
  }

  public URL getURL(String name) {
    try {
      File file = getFile(name);
      if (file.exists()) {
        return file.toURI().toURL();
      }
    } catch (IOException e) {
      log.error("Failed to transform file to URL: " + name, e);
    }
    return null;
  }

  public byte[] getBytes(String name) {
    InputStream in = getStream(name);
    if (in != null) {
      try {
        return FileUtils.readBytes(in);
      } catch (IOException e) {
        log.error("Failed to read file: " + name, e);
      }
    }
    return null;
  }

  public InputStream getStream(String name) {
    try {
      return new FileInputStream(getFile(name));
    } catch (IOException e) {
    }
    return null;
  }

  @Override
  public String getFilePath(FileInfo fileInfo) throws IOException {
    if (fileInfo == null || StringUtils.isEmpty(fileInfo.getChecksum()) || fileInfo.getChecksum().length() < 9) {
      return null;
    }
    return getFile(fileInfo.getChecksum()).getAbsolutePath();
  }

  @Override
  public String getFilePath(String name) throws IOException {
    if (name == null || StringUtils.isEmpty(name) || name.length() < 9) {
      return null;
    }
    return getFile(name).getAbsolutePath();
  }

  public void remove(String name) throws IOException {
    PrivilegedAction<Boolean> action = new PrivilegedAction<Boolean>() {
      public Boolean run() {
        try {
          ((TreeFile) getFile(name)).delete();
        } catch (IOException e) {
          return false;
        }
        return true;
      }
    };
    SecurityHelper.doPrivilegedAction(action);
  }

  @Override
  public boolean remove(FileInfo fileInfo) throws IOException {
    File file = getFile(fileInfo.getChecksum());
    if (file == null || !file.exists()) {
      throw new FileNotFoundException("Cannot delete file " + getFilePath(fileInfo) + " since it does not exist");
    } else {
      PrivilegedAction<Boolean> action = new PrivilegedAction<Boolean>() {
        public Boolean run() {
          try {
            ((TreeFile) getFile(fileInfo.getChecksum())).delete();
          } catch (IOException e) {
            return false;
          }
          return true;
        }
      };
      return SecurityHelper.doPrivilegedAction(action);
    }
  }

  public void put(String name, byte[] data) throws IOException {
    boolean created = FileUtils.createNewFile(getFile(name));
    if (created) {
      FileUtils.writeFile(getFile(name), data);
    }
  }

  /**
   * Write a file to the file system. The path is build with the root path, and
   * from the checksum of the file. The folder levels are built from the
   * checkum, and it is also used for the filename. This allows to never update
   * a file, meaning there will be no concurrent issue when creating or updating
   * a file. If a file with the same path already exists, it means that this is
   * the same file and there is no need to update it. If a file with the same
   * path does not exist, the file can be created since it is a nex file or it
   * is an update with a different file binary. This implies that no deletion of
   * old file versions are done on the fly. A scheduled job is needed for that.
   * TODO Scheduled job to delete old files
   *
   * @param name file name.
   * @param data inputStream
   * @throws IOException ignals that an I/O exception of some sort has occurred.
   */
  public void put(String name, InputStream data) throws IOException {
    boolean created = FileUtils.createNewFile(getFile(name));
    if (created) {
      FileUtils.copyToFile(data, getFile(name));
    }
  }

  @Override
  public void put(FileItem fileItem) throws IOException {
    boolean created = FileUtils.createNewFile(getFile(fileItem.getFileInfo().getChecksum()));
    if (created) {
      FileUtils.copyToFile(fileItem.getAsStream(), getFile(fileItem.getFileInfo().getChecksum()));
    }
  }

  public String getLocation() {
    return root.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof FileSystemResourceProvider) {
        FileSystemResourceProvider store = (FileSystemResourceProvider) obj;
      return store.root.equals(root);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return root.hashCode();
  }

  @Override
  public String toString() {
    return "FileSystemResourceProvider: " + root;
  }

}
