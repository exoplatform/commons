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
package org.exoplatform.commons.file.services.util;

import org.exoplatform.commons.utils.PropertyManager;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Offers to calculate Checksum
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 */
public final class FileChecksum {

  private static String digestAlgorithm = "MD5";

  /**
   * MD5-Checksum for a string.
   *
   * @param string chain  to calculate Checksum
   * @return Checksum
   * @throws Exception exception
   */
  public static String getChecksum(String string) throws Exception {
    MessageDigest digest = java.security.MessageDigest.getInstance(digestAlgorithm);
    digest.update(string.getBytes());
    byte messageDigest[] = digest.digest();
    //This bytes[] has bytes in decimal format;
    //Convert it to hexadecimal format
    StringBuilder sb = new StringBuilder();
    for(int i=0; i< messageDigest.length ;i++)
    {
      sb.append(Integer.toString((messageDigest[i] & 0xff) + 0x100, 16).substring(1));
    }
    //return complete hash
    return sb.toString();
  }

  /**
   * MD5-Checksum for a file.
   *
   * @param fis InputStream
   * @return Checksum
   * @throws Exception exception
   */
  public static String getChecksum(InputStream fis) throws Exception {
    MessageDigest digest = java.security.MessageDigest.getInstance(digestAlgorithm);
    //Create byte array to read data in chunks
    byte[] byteArray = new byte[1024];
    int bytesCount = 0;

    //Read file data and update in message digest
    while ((bytesCount = fis.read(byteArray)) != -1) {
      digest.update(byteArray, 0, bytesCount);
    };

    //close the stream; We don't need it now.
    fis.close();

    //Get the hash's bytes
    byte[] bytes = digest.digest();

    //This bytes[] has bytes in decimal format;
    //Convert it to hexadecimal format
    StringBuilder sb = new StringBuilder();
    for(int i=0; i< bytes.length ;i++)
    {
      sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
    }

    //return complete hash
    return sb.toString();
  }

}
