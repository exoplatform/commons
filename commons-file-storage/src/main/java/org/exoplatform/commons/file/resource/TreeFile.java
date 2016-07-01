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

import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.File;

/*
* Created by The eXo Platform SAS
* Author : eXoPlatform
*          exo@exoplatform.co
*/
public class TreeFile extends File {
  private static Log        fLog             = ExoLogger.getLogger("org.exoplatform.commons.file.resource.TreeFile");

  private static final long serialVersionUID = 5125295927077006487L;

  private final FileCleaner cleaner;

  private final File        rootDir;

  TreeFile(String fileName, FileCleaner cleaner, File rootDir) {
    super(fileName);
    this.cleaner = cleaner;
    this.rootDir = rootDir;
  }

  @Override
  public boolean delete() {
    boolean res = super.delete();
    if (res)
      deleteParent(new File(getParent()));

    return res;
  }

  protected boolean deleteParent(File fp) {
    boolean res = false;
    String fpPath = fp.getAbsolutePath();
    String rootPath = rootDir.getAbsolutePath();
    if (fpPath.startsWith(rootPath) && fpPath.length() > rootPath.length())
      if (fp.isDirectory()) {
        String[] ls = fp.list();
        if (ls != null && ls.length <= 0) {
          if (res = fp.delete()) {
            res = deleteParent(new File(fp.getParent()));
          } else {
            fLog.warn("Parent directory can not be deleted now. " + fp.getAbsolutePath());
            cleaner.addFile(new TreeFile(fp.getAbsolutePath(), cleaner, rootDir));
          }
        }
      } else
        fLog.warn("Parent can not be a file but found " + fp.getAbsolutePath());
    return res;
  }
}
