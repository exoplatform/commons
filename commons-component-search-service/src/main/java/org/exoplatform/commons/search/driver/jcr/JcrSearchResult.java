package org.exoplatform.commons.search.driver.jcr;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;

public class JcrSearchResult {
  private String repositoryName;
  private String workspaceName;
  private String path;
  private String primaryType;
  private String excerpt;
  private long score;
  
  public String getRepository() {
    return repositoryName;
  }
  public void setRepository(String repository) {
    this.repositoryName = repository;
  }
  public String getWorkspace() {
    return workspaceName;
  }
  public void setWorkspace(String workspace) {
    this.workspaceName = workspace;
  }
  public String getPath() {
    return path;
  }
  public void setPath(String path) {
    this.path = path;
  }
  public String getPrimaryType() {
    return primaryType;
  }
  public void setPrimaryType(String primaryType) {
    this.primaryType = primaryType;
  }
  public String getExcerpt() {
    return excerpt;
  }
  public void setExcerpt(String excerpt) {
    this.excerpt = excerpt;
  }
  public long getScore() {
    return score;
  }
  public void setScore(long score) {
    this.score = score;
  }
  
  public Object getProperty(String propertyName) throws Exception {
    return getProperties().get(propertyName);
  }

  public Map<String, Object> getProperties() throws Exception {
    return JcrSearch.getJcrNodeProperties(repositoryName + "/" + workspaceName + path);
  }

}
