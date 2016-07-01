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
package org.exoplatform.commons.file.storage.entity;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;
import java.util.List;

/**
 * Entity for NameSpace.
 *
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 */
@Entity(name = "NameSpaceEntity")
@ExoEntity
@Table(name = "COMMONS_NAMESPACES")

@NamedQueries(
        @NamedQuery(name = "nameSpace.getNameSpaceByName", query = "SELECT t FROM NameSpaceEntity t WHERE t.name = :name")
)
public class NameSpaceEntity {
    @Id
    @Column(name = "NAMESPACE_ID")
    @SequenceGenerator(name="SEQ_COMMONS_NAMESPACES_NAMESPACE_ID", sequenceName="SEQ_COMMONS_NAMESPACES_NAMESPACE_ID")
    @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_COMMONS_NAMESPACES_NAMESPACE_ID")
    private long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @OneToMany(mappedBy = "nameSpaceEntity", fetch = FetchType.LAZY)
    private List<FileInfoEntity> filesInfo;

    public NameSpaceEntity() {
    }

    public NameSpaceEntity(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public NameSpaceEntity(long id, String name, String description) {
        this(name, description);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<FileInfoEntity> getFilesInfo() {
        return filesInfo;
    }

    public void setFilesInfo(List<FileInfoEntity> filesInfo) {
        this.filesInfo = filesInfo;
    }
}
