package org.exoplatform.commons.file.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.model.FileInfo;

/**
 *
 */
public class MemoryBinaryProvider implements BinaryProvider {
  private Map<Long, byte[]> binaries = new HashMap<>();

  @Override
  public InputStream readBinary(FileInfo fileInfo) {
    byte[] binary = binaries.get(fileInfo.getId());
    return binary != null ? new ByteArrayInputStream(binary) : null;
  }

  @Override
  public void writeBinary(FileItem file) throws IOException {
    binaries.put(file.getFileInfo().getId(), IOUtils.toByteArray(file.getStream()));
  }

  @Override
  public void deleteBinary(FileInfo fileInfo) {
    binaries.remove(fileInfo.getId());
  }
}
