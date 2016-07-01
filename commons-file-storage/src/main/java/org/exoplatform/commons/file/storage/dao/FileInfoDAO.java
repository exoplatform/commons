package org.exoplatform.commons.file.storage.dao;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.commons.file.storage.entity.FileInfoEntity;
import org.exoplatform.commons.file.storage.entity.NameSpaceEntity;

import java.util.Date;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 */
public interface FileInfoDAO extends GenericDAO<FileInfoEntity, Long> {

    List<FileInfoEntity> findDeletedFiles(Date date);

    void deleteFileInfoByNameSpace(NameSpaceEntity nameSpaceEntity);
}
