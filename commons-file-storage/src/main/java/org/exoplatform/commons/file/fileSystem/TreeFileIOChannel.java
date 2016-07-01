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
package org.exoplatform.commons.file.fileSystem;

import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 */

public class TreeFileIOChannel {
  private static final ConcurrentMap<String, Lock> locks = new ConcurrentHashMap<String, Lock>(64, 0.75f, 64);

  /**
   * Storage root dir.
   */
  protected final File                             rootDir;

  /**
   * File cleaner used to clean files.
   */
  private static final FileCleaner cleaner = new FileCleaner(null);

  public TreeFileIOChannel(File rootDir) {
    this.rootDir = rootDir;
  }

  protected File getFile(final String resourceId) throws IOException {
    final TreeFile tfile = new TreeFile(rootDir.getAbsolutePath() + makeFilePath(resourceId), cleaner, rootDir);
    mkdirs(tfile.getParentFile()); // make dirs on path
    return tfile;
  }

  protected File[] getFiles(final String propertyId) throws IOException {
    final File dir = new File(rootDir.getAbsolutePath() + buildPath(propertyId));
    String[] fileNames = dir.list();
    File[] files = new File[0];
    if (fileNames != null) {
      files = new File[fileNames.length];

      for (int i = 0; i < fileNames.length; i++) {
        files[i] = new TreeFile(dir.getAbsolutePath() + File.separator + fileNames[i], cleaner, rootDir);
      }
    }
    return files;
  }

  protected String makeFilePath(final String resourceId) {
    return buildPath(resourceId) + File.separator + resourceId;
  }

  protected String buildPath(String fileName) {
    return buildPathX8(fileName);
  }

  protected String buildPathX8(String fileName) {
    final int xLength = 8;
    char[] chs = fileName.toCharArray();
    StringBuilder path = new StringBuilder();
    for (int i = 0; i < xLength; i++) {
      path.append(File.separator).append(chs[i]);
    }
    return path.toString();
  }

  private static void mkdirs(File dir) {
    if (dir.exists()) {
      return;
    }
    List<File> dir2Create = new ArrayList<File>();
    dir2Create.add(dir);
    dir = dir.getParentFile();
    while (dir != null && !dir.exists()) {
      dir2Create.add(0, dir);
      dir = dir.getParentFile();
    }
    for (int i = 0, length = dir2Create.size(); i < length; i++) {
      mkdir(dir2Create.get(i));
    }
  }

  private static void mkdir(File dir) {
    String path = dir.getAbsolutePath();
    Lock lock = locks.get(path);
    if (lock == null) {
      lock = new ReentrantLock();
      Lock prevLock = locks.putIfAbsent(path, lock);
      if (prevLock != null) {
        lock = prevLock;
      }
    }
    lock.lock();
    try {
      if (!dir.exists()) {
        dir.mkdir();
      }
    } finally {
      lock.unlock();
      locks.remove(path, lock);
    }
  }

}
