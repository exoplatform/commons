/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Dec
 * 15, 2015
 */
public class StringCommonUtils {

  private static final int BUFFER_SIZE = 32;

  public static InputStream compress(String string) throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
    GZIPOutputStream gos = new GZIPOutputStream(os);
    gos.write(string.getBytes());
    gos.close();
    byte[] compressed = os.toByteArray();
    os.close();
    return new ByteArrayInputStream(compressed);
  }

  public static String decompress(InputStream is) throws IOException {
    GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    try {

      byte[] data = new byte[BUFFER_SIZE];
      int bytesRead;

      while ((bytesRead = gis.read(data)) != -1) {
        buffer.write(data, 0, bytesRead);
      }

      return new String(buffer.toByteArray());
    } finally {
      gis.close();
      is.close();
      buffer.close();
    }
  }
}
