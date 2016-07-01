package org.exoplatform.commons.file.fileSystem;

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.model.FileInfo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 */
public interface BinaryProvider {

  public InputStream readBinary(FileInfo fileInfo) throws IOException;

  public void writeBinary(FileItem file) throws IOException;

  public void deleteBinary(FileInfo fileInfo) throws FileNotFoundException;
}
