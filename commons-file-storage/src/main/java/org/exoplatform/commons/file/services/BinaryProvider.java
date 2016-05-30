package org.exoplatform.commons.file.services;

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.model.FileInfo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public interface BinaryProvider {

  public InputStream readBinary(FileInfo fileInfo) throws IOException;

  public void writeBinary(FileItem file) throws IOException;

  public void deleteBinary(FileInfo fileInfo) throws FileNotFoundException;
}
