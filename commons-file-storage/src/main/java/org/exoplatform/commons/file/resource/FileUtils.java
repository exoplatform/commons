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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.exoplatform.commons.utils.SecurityHelper;

/**
 * This is an utility class : File operation Created by The eXo Platform SAS
 * Author : eXoPlatform exo@exoplatform.com
 */
public final class FileUtils {

  private static final int BUFFER_SIZE     = 1024 * 64;   // 64K

  private static final int MAX_BUFFER_SIZE = 1024 * 1024; // 64K

  private static final int MIN_BUFFER_SIZE = 1024 * 8;    // 64K

  private FileUtils() {
  }

  private static byte[] createBuffer(int preferredSize) {
    if (preferredSize < 1) {
      preferredSize = BUFFER_SIZE;
    }
    if (preferredSize > MAX_BUFFER_SIZE) {
      preferredSize = MAX_BUFFER_SIZE;
    } else if (preferredSize < MIN_BUFFER_SIZE) {
      preferredSize = MIN_BUFFER_SIZE;
    }
    return new byte[preferredSize];
  }

  /**
   * Create new file.
   *
   * @param file new file
   * @return boolean
   * @throws IOException Signals that an I/O exception of some sort has
   *           occurred.
   */
  public static boolean createNewFile(final File file) throws IOException {
    PrivilegedExceptionAction<Boolean> action = new PrivilegedExceptionAction<Boolean>() {
      public Boolean run() throws Exception {
        return file.createNewFile();
      }
    };
    try {
      return SecurityHelper.doPrivilegedExceptionAction(action);
    } catch (PrivilegedActionException pae) {
      Throwable cause = pae.getCause();

      if (cause instanceof IOException) {
        throw (IOException) cause;
      } else if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      } else {
        throw new RuntimeException(cause);
      }
    }
  }

  /**
   * Tests in privileged mode whether the file or directory denoted by this
   * abstract pathname exists.
   *
   * @param file file
   * @return boolean
   */
  public static boolean exists(final File file) {
    PrivilegedAction<Boolean> action = new PrivilegedAction<Boolean>() {
      public Boolean run() {
        return file.exists();
      }
    };
    return SecurityHelper.doPrivilegedAction(action);
  }

  /**
   * Creates the directory in privileged mode.
   *
   * @param file new directory to create
   * @return boolean
   */
  public static boolean mkdirs(final File file) {
    PrivilegedAction<Boolean> action = new PrivilegedAction<Boolean>() {
      public Boolean run() {
        return file.mkdirs();
      }
    };
    return SecurityHelper.doPrivilegedAction(action);
  }

  /**
   * Reads bytes of data from the input stream into an array of bytes.
   *
   * @param in InputStream
   * @throws IOException signals that an I/O exception of some sort has
   *           occurred.
   * @return byte array
   */
  public static byte[] readBytes(InputStream in) throws IOException {
    byte[] buffer = createBuffer(in.available());
    int w = 0;
    try {
      int read = 0;
      int len;
      do {
        w += read;
        len = buffer.length - w;
        if (len <= 0) { // resize buffer
          byte[] b = new byte[buffer.length + BUFFER_SIZE];
          System.arraycopy(buffer, 0, b, 0, w);
          buffer = b;
          len = buffer.length - w;
        }
      } while ((read = in.read(buffer, w, len)) != -1);
    } finally {
      in.close();
    }
    if (buffer.length > w) { // compact buffer
      byte[] b = new byte[w];
      System.arraycopy(buffer, 0, b, 0, w);
      buffer = b;
    }
    return buffer;
  }

  /**
   * Writes bytes from the specified inputStream to output stream.
   *
   * @param in InputStream
   * @param file file
   * @throws IOException signals that an I/O exception of some sort has
   *           occurred.
   */
  public static void copyToFile(InputStream in, File file) throws IOException {
    OutputStream out = null;
    try {
      out = new FileOutputStream(file);
      byte[] buffer = createBuffer(in.available());
      int read;
      while ((read = in.read(buffer)) != -1) {
        out.write(buffer, 0, read);
      }
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }

  /**
   * Writes bytes from byte array to output stream.
   *
   * @param buf byte array
   * @param file file
   * @throws IOException signals that an I/O exception of some sort has
   *           occurred.
   */
  public static void writeFile(File file, byte[] buf) throws IOException {
    writeFile(file, buf, false);
  }

  private static void writeFile(File file, byte[] buf, boolean append) throws IOException {
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(file, append);
      fos.write(buf);
    } finally {
      if (fos != null) {
        fos.close();
      }
    }
  }

}
