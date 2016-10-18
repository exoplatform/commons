package org.exoplatform.commons.file.storage.dao;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.commons.file.storage.entity.FileBinaryEntity;

/**
 * Data Access Object layer for binary data files.
 */
public interface FileBinaryDAO extends GenericDAO<FileBinaryEntity, Long> {
    FileBinaryEntity findFileBinaryByName(String name);
}
