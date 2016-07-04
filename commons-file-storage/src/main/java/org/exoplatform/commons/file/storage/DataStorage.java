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
package org.exoplatform.commons.file.storage;

import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.model.NameSpace;
import org.exoplatform.commons.file.model.OrphanFile;
import org.exoplatform.commons.file.storage.dao.FileInfoDAO;
import org.exoplatform.commons.file.storage.dao.NameSpaceDAO;
import org.exoplatform.commons.file.storage.dao.OrphanFileDAO;
import org.exoplatform.commons.file.storage.entity.FileInfoEntity;
import org.exoplatform.commons.file.storage.entity.NameSpaceEntity;
import org.exoplatform.commons.file.storage.entity.OrphanFileEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 */
public class DataStorage {
  private FileInfoDAO   fileInfoDAO;

  private NameSpaceDAO  nameSpaceDAO;

  private OrphanFileDAO orphanFileDAO;

  public DataStorage(FileInfoDAO fileInfoDAO, NameSpaceDAO nameSpaceDAO, OrphanFileDAO orphanFileDAO) {
    this.fileInfoDAO = fileInfoDAO;
    this.nameSpaceDAO = nameSpaceDAO;
    this.orphanFileDAO = orphanFileDAO;
  }

  public FileInfo getFileInfo(long id) {
    FileInfoEntity fileInfoEntity = fileInfoDAO.find(id);
    return convertFileEntityToFileInfo(fileInfoEntity);
  }

  public List<FileInfo> getAllFilesInfo(int offset, int limit) {
    List<FileInfoEntity> result = fileInfoDAO.findAllByPage(offset, limit);
    List<FileInfo> listFilesInfo = new ArrayList<FileInfo>();
    for (FileInfoEntity f : result) {
      listFilesInfo.add(convertFileEntityToFileInfo(f));
    }
    return listFilesInfo;
  }

  public NameSpace getNameSpace(long id) {
    NameSpaceEntity nameSpaceEntity = nameSpaceDAO.find(id);
    return convertNameSpace(nameSpaceEntity);
  }

  public NameSpace getNameSpace(String name) {
    NameSpaceEntity nameSpaceEntity = nameSpaceDAO.getNameSpaceByName(name);
    return convertNameSpace(nameSpaceEntity);
  }

  public void createNameSpaces(List<NameSpace> listNameSpace) {
    List<NameSpaceEntity> entityList = new ArrayList<NameSpaceEntity>();
    for (NameSpace s : listNameSpace) {
      NameSpaceEntity n = new NameSpaceEntity(s.getName(), s.getDescription());
      entityList.add(n);
    }
    nameSpaceDAO.createAll(entityList);
  }

  public NameSpace createNameSpace(NameSpace nameSpace) {
    NameSpaceEntity n = new NameSpaceEntity(nameSpace.getName(), nameSpace.getDescription());
    NameSpaceEntity createdNameSpace = nameSpaceDAO.create(n);
    return convertNameSpace(createdNameSpace);
  }

  public FileInfo create(FileInfo fileInfo, NameSpace nameSpace) {
    if (fileInfo == null)
      return null;
    NameSpaceEntity nSpace = new NameSpaceEntity(nameSpace.getId(), nameSpace.getName(), nameSpace.getDescription());
    FileInfoEntity fileInfoEntity = new FileInfoEntity(fileInfo.getName(),
                                                       fileInfo.getMimetype(),
                                                       fileInfo.getSize(),
                                                       fileInfo.getUpdatedDate(),
                                                       fileInfo.getUpdater(),
                                                       fileInfo.getChecksum(),
                                                       fileInfo.isDeleted());
    fileInfoEntity.setNameSpaceEntity(nSpace);
    FileInfoEntity createdFile = fileInfoDAO.create(fileInfoEntity);
    return convertFileEntityToFileInfo(createdFile);
  }

  public FileInfo updateFileInfo(FileInfo fileInfo) {
    if (fileInfo == null)
      return null;
    FileInfoEntity fileInfoEntity = new FileInfoEntity(fileInfo.getId(),
                                                       fileInfo.getName(),
                                                       fileInfo.getMimetype(),
                                                       fileInfo.getSize(),
                                                       fileInfo.getUpdatedDate(),
                                                       fileInfo.getUpdater(),
                                                       fileInfo.getChecksum(),
                                                       fileInfo.isDeleted());
    FileInfoEntity updated = fileInfoDAO.update(fileInfoEntity);
    return convertFileEntityToFileInfo(updated);
  }

  public void deleteFileInfo(long id) {
    FileInfoEntity fileInfoEntity = fileInfoDAO.find(id);
    if (fileInfoEntity == null)
      return;
    fileInfoDAO.delete(fileInfoEntity);
  }

  public void createOrphanFile(FileInfo fileInfo) {
    FileInfoEntity fileInfoEntity = new FileInfoEntity(fileInfo.getId(),
                                                       fileInfo.getName(),
                                                       fileInfo.getMimetype(),
                                                       fileInfo.getSize(),
                                                       fileInfo.getUpdatedDate(),
                                                       fileInfo.getUpdater(),
                                                       fileInfo.getChecksum(),
                                                       fileInfo.isDeleted());

    OrphanFileEntity deletedFileEntity = new OrphanFileEntity();
    deletedFileEntity.setChecksum(fileInfo.getChecksum());
    deletedFileEntity.setFileInfoEntity(fileInfoEntity);
    deletedFileEntity.setDeletedDate(new Date());
    orphanFileDAO.create(deletedFileEntity);
  }

  public void deleteOrphanFile(long id) {
    OrphanFileEntity orphanFileEntity = orphanFileDAO.find(id);
    if (orphanFileEntity != null) {
      orphanFileDAO.delete(orphanFileEntity);
    }
  }

  public List<FileInfo> getAllDeletedFiles(Date date) {
    List<FileInfoEntity> result = fileInfoDAO.findDeletedFiles(date);
    List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
    for (FileInfoEntity f : result) {
      FileInfo obj = convertFileEntityToFileInfo(f);
      if (obj != null) {
        fileInfoList.add(convertFileEntityToFileInfo(f));
      }
    }
    return fileInfoList;
  }

  public List<OrphanFile> getAllOrphanFile(Date date) {
    List<OrphanFileEntity> result = orphanFileDAO.findDeletedFiles(date);
    List<OrphanFile> orphanFileList = new ArrayList<OrphanFile>();
    for (OrphanFileEntity o : result) {
      OrphanFile file = convertOrphanFileEntity(o);
      if (file != null) {
        orphanFileList.add(file);
      }
    }
    return orphanFileList;
  }

  private FileInfo convertFileEntityToFileInfo(FileInfoEntity fileInfoEntity) {
    if (fileInfoEntity == null) {
      return null;
    }

    FileInfo fileInfo = new FileInfo(fileInfoEntity.getId(),
                                     fileInfoEntity.getName(),
                                     fileInfoEntity.getMimetype(),
                                     fileInfoEntity.getNameSpaceEntity().getName(),
                                     fileInfoEntity.getSize(),
                                     fileInfoEntity.getUpdatedDate(),
                                     fileInfoEntity.getUpdater(),
                                     fileInfoEntity.getChecksum(),
                                     fileInfoEntity.isDeleted());
    return fileInfo;
  }

  private NameSpace convertNameSpace(NameSpaceEntity nameSpaceEntity) {
    if (nameSpaceEntity == null) {
      return null;
    }
    return new NameSpace(nameSpaceEntity.getId(), nameSpaceEntity.getName(), nameSpaceEntity.getDescription());
  }

  private OrphanFile convertOrphanFileEntity(OrphanFileEntity orphanFileEntity) {
    if (orphanFileEntity == null)
      return null;
    OrphanFile orphanFile;
    if (orphanFileEntity.getFileInfoEntity() != null) {
      orphanFile = new OrphanFile(orphanFileEntity.getId(),
                                  orphanFileEntity.getFileInfoEntity().getId(),
                                  orphanFileEntity.getChecksum(),
                                  orphanFileEntity.getDeletedDate());
    } else {
      orphanFile =
                 new OrphanFile(orphanFileEntity.getId(), -1, orphanFileEntity.getChecksum(), orphanFileEntity.getDeletedDate());
    }
    return orphanFile;
  }

}
