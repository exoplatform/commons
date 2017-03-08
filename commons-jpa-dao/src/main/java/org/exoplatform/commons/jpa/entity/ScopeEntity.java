//package org.exoplatform.settings.jpa;
//
//import org.chromattic.api.annotations.Destroy;
//import org.chromattic.api.annotations.OneToMany;
//import org.chromattic.api.annotations.PrimaryType;
//import org.chromattic.api.annotations.Properties;
//
//import java.util.Map;
//
///**
// * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
// */
//@PrimaryType(name = "stg:scope")
//public abstract class ScopeEntity {
//
//  @OneToMany
//  protected abstract Map<String, ScopeEntity> getInstances();
//
//  @Properties
//  protected abstract Map<String, Object> getProperties();
//
//  @Destroy
//  public abstract void remove();
//
//  public Object getValue(String name) {
//    return getProperties().get(name);
//  }
//  public Object removeValue(String key) {
//    return getProperties().remove(key);
//  }
//
//  public ScopeEntity removeScope(String name) {
//    return getInstances().remove(name);
//  }
//
//  public void setValue(String name, Object value) {
//    getProperties().put(name, value);
//  }
//
//  public ScopeEntity getInstance(String name) {
//    return getInstances().get(name);
//  }
//
//}

package org.exoplatform.commons.jpa.entity;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 07, 2017
 */
@Entity(name = "ScopeEntity")
@ExoEntity
@Table(name = "STG_SCOPE")
//@NamedQueries({
//    @NamedQuery(name = "commons.getAllIds", query = "SELECT w.id FROM WikiWikiEntity w ORDER BY w.id"),
//    @NamedQuery(name = "commons.getWikisByType", query = "SELECT w FROM WikiWikiEntity w WHERE w.type = :type"),
//    @NamedQuery(name = "commons.getWikiByTypeAndOwner", query = "SELECT w FROM WikiWikiEntity w WHERE w.type = :type AND w.owner = :owner")
//})
public class ScopeEntity {
  @Id
  @Column(name = "STG_SCOPE_ID")
  @SequenceGenerator(name="SEQ_STG_SCOPE_COMMON_ID", sequenceName="SEQ_STG_SCOPE_COMMON_ID")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_STG_SCOPE_COMMON_ID")
  private long id;

  @Column(name = "STG_SCOPE_NAME")
  private String name;

  @Column(name = "STG_SCOPE_TYPE")
  private String type;

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public ScopeEntity setName(String name) {
    this.name = name;
    return this;
  }

  public String getType() {
    return type;
  }

  public ScopeEntity setType(String type) {
    this.type = type;
    return this;
  }
}
