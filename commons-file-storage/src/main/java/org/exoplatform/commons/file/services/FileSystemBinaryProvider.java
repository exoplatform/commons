package org.exoplatform.commons.file.services;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;

/**
 *
 */
public class FileSystemBinaryProvider implements BinaryProvider {

  public static final String ROOT_PATH_PARAM = "rootPath";

  private String rootPath;

  public FileSystemBinaryProvider(String rootPath) throws Exception {
    this.rootPath = rootPath;
    if(StringUtils.isEmpty(this.rootPath)) {
      throw new Exception("Init param '" + ROOT_PATH_PARAM + "' not defined for " + getClass().getSimpleName());
    }

    if ("/".equals(this.rootPath)) {
      this.rootPath = "";
    } else if (!this.rootPath.endsWith("/")) {
      this.rootPath = this.rootPath + "/";
    }
  }

  public FileSystemBinaryProvider(InitParams initParams) throws Exception {
    if(initParams == null) {
      throw new IllegalArgumentException("Init params cannot be null, it must define file system root path in '" + ROOT_PATH_PARAM + "' value param");
    }
    ValueParam rootPathValueParam = initParams.getValueParam(ROOT_PATH_PARAM);
    if(rootPathValueParam == null) {
      throw new Exception("Missing init param '" + ROOT_PATH_PARAM + "' for " + getClass().getSimpleName());
    }
    this.rootPath = rootPathValueParam.getValue();
    if(StringUtils.isEmpty(this.rootPath)) {
      throw new Exception("Init param '" + ROOT_PATH_PARAM + "' not defined for " + getClass().getSimpleName());
    }

    if ("/".equals(this.rootPath)) {
      this.rootPath = "";
    } else if (!this.rootPath.endsWith("/")) {
      this.rootPath = this.rootPath + "/";
    }
  }

  @Override
  public InputStream readBinary(FileInfo fileInfo) throws IOException {
    return Files.newInputStream(Paths.get(getFilePath(fileInfo)));
  }

  @Override
  public void writeBinary(FileItem file) throws IOException {
     if(file.getFileInfo() == null || StringUtils.isEmpty(file.getFileInfo().getChecksum())) {
       throw new IllegalArgumentException("Checksum is required to persist the binary");
     }

    String filePath = getFilePath(file.getFileInfo());

    // check if a file with the same path and checksum exists -> in this case, no need to update it, it is the same file
    File existingFile = new File(filePath);
    if(existingFile == null || !existingFile.exists()) {
      Path path = Paths.get(filePath);
      Files.createDirectories(path.getParent());
      Files.write(path, IOUtils.toByteArray(file.getStream()));
    }
  }

  @Override
  public void deleteBinary(FileInfo fileInfo) throws FileNotFoundException {
    String filePath = getFilePath(fileInfo);
    File file = new File(filePath);
    if(file == null || !file.exists()) {
      throw new FileNotFoundException("Cannot delete file " + filePath + " since it does not exist");
    } else {
      file.delete();
    }
  }

  public String getFilePath(FileInfo fileInfo) {
    if(fileInfo == null || StringUtils.isEmpty(fileInfo.getChecksum()) || fileInfo.getChecksum().length() < 5) {
      return null;
    }
    String checksum = fileInfo.getChecksum();
    return this.rootPath + checksum.substring(0, 2) + "/" + checksum.substring(2, 4) + "/" + checksum;
  }
}
