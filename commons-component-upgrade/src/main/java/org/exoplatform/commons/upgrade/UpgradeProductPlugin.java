package org.exoplatform.commons.upgrade;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.commons.version.util.VersionComparator;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public abstract class UpgradeProductPlugin extends BaseComponentPlugin {

  public static final String UPGRADE_COMPLETED_STATUS              = "Completed";

  public static final String PRODUCT_GROUP_ID                      = "product.group.id";

  public static final String OLD_PRODUCT_GROUP_ID                  = "old.product.group.id";

  public static final String UPGRADE_PLUGIN_ASYNC                  = "plugin.upgrade.async.execution";

  public static final String UPGRADE_PLUGIN_TARGET_PARAMETER       = "plugin.upgrade.target.version";

  public static final String UPGRADE_PLUGIN_EXECUTE_ONCE_PARAMETER = "plugin.upgrade.execute.once";

  public static final String UPGRADE_PLUGIN_EXECUTION_ORDER        = "plugin.execution.order";

  public static final String UPGRADE_PLUGIN_ENABLE                 = "commons.upgrade.{$0}.enable";

  private static final Log   LOG                                   = ExoLogger.getLogger(UpgradeProductPlugin.class);

  private SettingService     settingService                        = null;

  private int                pluginExecutionOrder                  = 0;

  /**
   * The plugin's product maven group identifier, by example:
   * org.exoplatform.portal for gatein.
   */
  protected String           productGroupId                        = null;

  protected String           oldProductGroupId                     = null;

  /**
   * The target version of this upgrade Plugin.
   */
  protected String           targetVersion                         = null;

  /**
   * True if the upgrade execution should be processed asynchronously
   * else, it will be executed synchronously
   */
  protected boolean          asyncUpgradeExecution                 = false;

  /**
   * Determines whether the plugin should be executed once or even version upgrade.
   * If true, the method shouldProceedToUpgrade will not be called to test if the Upgrade Plugin
   * should be executed or not.
   */
  protected boolean          executeOnlyOnce                       = false;

  public UpgradeProductPlugin(SettingService settingService, InitParams initParams) {
    this(initParams);
    this.settingService = settingService;
  }

  public UpgradeProductPlugin(InitParams initParams) {
    if (!initParams.containsKey(PRODUCT_GROUP_ID)) {
      if(LOG.isErrorEnabled()){
        LOG.error("Couldn't find the init value param: " + PRODUCT_GROUP_ID);
      }
      return;
    }

    //
    productGroupId = initParams.getValueParam(PRODUCT_GROUP_ID).getValue();

    //
    ValueParam vp = initParams.getValueParam(OLD_PRODUCT_GROUP_ID);
    oldProductGroupId = vp != null ? vp.getValue() : productGroupId;

    if (!initParams.containsKey(UPGRADE_PLUGIN_EXECUTION_ORDER)) {
      pluginExecutionOrder = 0;
    }else{
      pluginExecutionOrder = Integer.parseInt(initParams.getValueParam(UPGRADE_PLUGIN_EXECUTION_ORDER).getValue());
    }
    if (initParams.containsKey(UPGRADE_PLUGIN_TARGET_PARAMETER)) {
      targetVersion = initParams.getValueParam(UPGRADE_PLUGIN_TARGET_PARAMETER).getValue();
    }
    if (initParams.containsKey(UPGRADE_PLUGIN_ASYNC)) {
      asyncUpgradeExecution = Boolean.parseBoolean(initParams.getValueParam(UPGRADE_PLUGIN_ASYNC).getValue());
    }
    if (initParams.containsKey(UPGRADE_PLUGIN_EXECUTE_ONCE_PARAMETER)) {
      executeOnlyOnce = Boolean.parseBoolean(initParams.getValueParam(UPGRADE_PLUGIN_EXECUTE_ONCE_PARAMETER).getValue());
    }
  }

  public String getProductGroupId() {
    return productGroupId;
  }

  /**
   * Execute some operations synchronously after the execution of processUpgrade
   * method synchronously or asynchronously
   */
  public void beforeUpgrade() {}

  /**
   * Execute some operations synchronously after the execution of processUpgrade
   * method synchronously or asynchronously
   */
  public void afterUpgrade() {}

  public boolean isAsyncUpgradeExecution() {
    return asyncUpgradeExecution;
  }

  public boolean isExecuteOnlyOnce() {
    return executeOnlyOnce;
  }

  public String getTargetVersion() {
    return targetVersion;
  }

  /**
   * Determines if the plugin is enabled, this method will be called when adding the plugin to the upgradePlugins list.
   * See {@link UpgradeProductService#addUpgradePlugin(UpgradeProductPlugin)}
   *
   * @return
   *          true: if the plugin is enabled: should be added to the upgradePlugins list
   *          false: if the plugin is disabled: should not be added to the upgradePlugins list
   */
  public boolean isEnabled() {
    String isEnabledProperty = PropertyManager.getProperty(UPGRADE_PLUGIN_ENABLE.replace("{$0}", getName()));
    if (StringUtils.isBlank(isEnabledProperty) || isEnabledProperty.equals("true")) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Proceed to the transparent upgrade, this method will be called if the
   * Product version has changed, which means, it will be called once the
   * stored Product Version and the one declared in ProductInfo are different
   *
   * @param oldVersion the old version that is stored in internal datasource
   * @param newVersion the new version read from ProductInfo Service
   */
  public abstract void processUpgrade(String oldVersion, String newVersion);

  /**
   * This method is called when a new version has been detected to decide whether proceed to upgrade or not.
   * It should take care that some versions could be skipped while upgrading, i.e: the upgrade could happen
   * when the product is switched from version 1.0 to 1.3.
   *
   * @param previousVersion
   *          The previous version of plugin's product
   * @param newVersion
   *          The previous version of plugin's product
   * @return
   *          true: if the plugin should be executed when switching product from previousVersion to newVersion
   *          false: if the upgrade isn't necessary
   */
  public boolean shouldProceedToUpgrade(String newVersion, String previousVersion) {
    return true;
  }

  /**
   * This method is called when a new version has been detected to decide
   * whether proceed to upgrade or not. It will test on previous version of
   * artifact, previous version of group and newer version to decide whether to
   * upgrade or not. This method will call
   * shouldProceedToUpgrade(previousVersion, newVersion) to include optional
   * specific check(s).
   *
   * @param newVersion The current version of running server
   * @param previousGroupVersion The previous version of plugin's product group (social, portal...)
   *          This parameter will be equals to '0' if first time it runs.
   * @param previousUpgradePluginExecution The previous version and execution count of plugin (retrieved from last run)
   *          This parameter will be null if first time it runs.
   * @return
   *          true: if the plugin should be executed when switching product from previousVersion to newVersion
   *          false: if the upgrade isn't necessary
   * @return
   */
  public boolean shouldProceedToUpgrade(String newVersion, String previousGroupVersion, UpgradePluginExecutionContext previousUpgradePluginExecution) {
    String previousArtifactVersion = previousUpgradePluginExecution == null ? null : previousUpgradePluginExecution.getVersion();
    int executionCount = previousUpgradePluginExecution == null ? 0 : previousUpgradePluginExecution.getExecutionCount();

    if (StringUtils.isBlank(previousGroupVersion) && StringUtils.isBlank(previousArtifactVersion)) {
      throw new IllegalArgumentException("At least one previous version (artifact or group versions) shouldn't be null (equals to '0') for plugin "
          + getClass().getName());
    }
    if (StringUtils.isBlank(newVersion)) {
      throw new IllegalArgumentException("No declared version for Upgrade plugin " + getClass().getName());
    }

    // If the plugin has to be executed only once, don't upgrade
    if (isExecuteOnlyOnce() && executionCount > 0) {
      return false;
    }

    String previousVersion = StringUtils.isBlank(previousArtifactVersion) ? previousGroupVersion : previousArtifactVersion;

    // If version didn't change or newVersion is greater to previous version,
    // don't upgrade
    if (VersionComparator.isBefore(newVersion, previousVersion)
        || (StringUtils.isNotBlank(previousArtifactVersion) && (VersionComparator.isBefore(newVersion, previousArtifactVersion)
            || VersionComparator.isSame(newVersion, previousArtifactVersion)))) {
      return false;
    }
    // If the plugin has a target version that is before current version
    if (StringUtils.isNotBlank(getTargetVersion()) && (VersionComparator.isBefore(getTargetVersion(), previousVersion)
        || VersionComparator.isSame(getTargetVersion(), previousVersion))) {
      return false;
    }
    return shouldProceedToUpgrade(newVersion, previousVersion);
  }

  /**
   * {@inheritDoc}
   */
  public final boolean equals(Object obj) {
    if (obj != null && obj instanceof UpgradeProductPlugin) {
      return this.getName().equals(((UpgradeProductPlugin) obj).getName());
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public final int hashCode() {
    return this.getName().hashCode();
  }

  public int getPluginExecutionOrder() {
    return pluginExecutionOrder;
  }

  public String getValue(String paramName) {
    if (settingService == null) {
      throw new IllegalStateException("SettingService Service is not set");
    }
    try {
      Scope appId = Scope.APPLICATION.id(getName());
      SettingValue<?> paramValue = settingService.get(Context.GLOBAL, appId, paramName);
      if (paramValue != null && paramValue.getValue() != null) {
        return paramValue.getValue().toString();
      }
      return null;
    } finally {
      Scope.APPLICATION.id(null);
    }
  }

  public void storeValueForPlugin(String paramName, String paramValue) {
    if (settingService == null) {
      throw new IllegalStateException("SettingService Service is not set");
    }
    try {
      settingService.set(Context.GLOBAL, Scope.APPLICATION.id(getName()), paramName, SettingValue.create(paramValue));
    } finally {
      Scope.APPLICATION.id(null);
    }
  }

}
