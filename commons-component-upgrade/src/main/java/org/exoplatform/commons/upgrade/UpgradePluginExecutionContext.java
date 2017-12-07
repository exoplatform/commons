package org.exoplatform.commons.upgrade;

public class UpgradePluginExecutionContext {
  String version;

  int    executionCount;

  public UpgradePluginExecutionContext(String version, int executionCount) {
    this.version = version;
    this.executionCount = executionCount;
  }

  public UpgradePluginExecutionContext(String versionAndExecutionCount) {
    if (versionAndExecutionCount.indexOf(";") < 0) {
      this.version = versionAndExecutionCount;
    } else {
      String[] versionAndCountArray = versionAndExecutionCount.split(";");
      this.version = versionAndCountArray[0];
      this.executionCount = Integer.parseInt(versionAndCountArray[1]);
    }
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public int getExecutionCount() {
    return executionCount;
  }

  public void setExecutionCount(int executionCount) {
    this.executionCount = executionCount;
  }

  @Override
  public String toString() {
    return this.version + ";" + this.executionCount;
  }
}