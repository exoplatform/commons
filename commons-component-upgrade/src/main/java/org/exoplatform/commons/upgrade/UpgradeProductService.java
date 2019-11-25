package org.exoplatform.commons.upgrade;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.cluster.StartableClusterAware;
import org.exoplatform.commons.info.MissingProductInformationException;
import org.exoplatform.commons.info.ProductInformations;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class UpgradeProductService implements StartableClusterAware {

  public static final Context              UPGRADE_PRODUCT_CONTEXT       = Context.GLOBAL.id("UPGRADE_PRODUCT_CONTEXT");

  private static final Log                 LOG                           = ExoLogger.getLogger(UpgradeProductService.class);

  private static final String              UPGRADE_PLUGIN_VERSION_KEY    = "UPGRADE_PLUGIN_VERSION";

  private static final String              PLUGINS_ORDER                 = "commons.upgrade.plugins.order";

  private static final String              PROCEED_UPGRADE_FIRST_RUN_KEY = "proceedUpgradeWhenFirstRun";

  private static final String              PRODUCT_VERSION_ZERO          = "0";

  private ExecutorService executorService = Executors.newCachedThreadPool();

  private PortalContainer portalContainer;

  private SettingService settingService;

  private List<UpgradeProductPlugin> upgradePlugins = new ArrayList<>();
  private Set<UpgradeProductPlugin> allUpgradePlugins= new HashSet<>();
  private ProductInformations productInformations = null;
  private boolean proceedUpgradeFirstRun = false;

  private Comparator<UpgradeProductPlugin> pluginsComparator             = null;

  public UpgradeProductService(PortalContainer portalContainer,
                               ProductInformations productInformations,
                               SettingService settingService,
                               InitParams initParams) {
    this.portalContainer = portalContainer;
    this.productInformations = productInformations;
    this.settingService = settingService;
    if (!initParams.containsKey(PROCEED_UPGRADE_FIRST_RUN_KEY)) {
      LOG.warn("init param '" + PROCEED_UPGRADE_FIRST_RUN_KEY + "' isn't set, use default value (" + proceedUpgradeFirstRun
              + "). Don't proceed upgrade when this service will run for the first time.");
    } else {
      proceedUpgradeFirstRun = Boolean.parseBoolean(initParams.getValueParam(PROCEED_UPGRADE_FIRST_RUN_KEY).getValue());
    }
    // Gets the execution order property: "commons.upgrade.plugins.order".
    String pluginsOrder = PropertyManager.getProperty(PLUGINS_ORDER);

    if (StringUtils.isBlank(pluginsOrder)) {
      // Use plugin execution order of plugins if PLUGINS_ORDER parameter not set
      LOG.info("Property '{}' wasn't set, use execution order of each plugin.", PLUGINS_ORDER);
      pluginsComparator = new Comparator<UpgradeProductPlugin>() {
        public int compare(UpgradeProductPlugin o1, UpgradeProductPlugin o2) {
          int index1 = o1.getPluginExecutionOrder();
          index1 = index1 <= 0 ? upgradePlugins.size() : index1;
          int index2 = o2.getPluginExecutionOrder();
          index2 = index2 <= 0 ? upgradePlugins.size() : index2;
          return index1 - index2;
        }
      };
    } else {
      // If PLUGINS_ORDER parameter is set, use it and ignore plugin execution order
      final List<String> pluginsOrderList = Arrays.asList(pluginsOrder.split(","));
      pluginsComparator = new Comparator<UpgradeProductPlugin>() {
        public int compare(UpgradeProductPlugin o1, UpgradeProductPlugin o2) {
          int index1 = pluginsOrderList.indexOf(o1.getName());
          index1 = index1 < 0 ? upgradePlugins.size() : index1;
          int index2 = pluginsOrderList.indexOf(o2.getName());
          index2 = index2 < 0 ? upgradePlugins.size() : index2;
          return index1 - index2;
        }
      };
    }
  }

  /**
   * Method called by eXo Kernel to inject upgrade plugins
   *
   * @param upgradeProductPlugin
   */
  public void addUpgradePlugin(UpgradeProductPlugin upgradeProductPlugin) {
    // add only enabled plugins
    if (upgradeProductPlugin.isEnabled()) {
      if (upgradePlugins.contains(upgradeProductPlugin)) {
        LOG.warn("Duplicated upgrade plugin '{}'.", upgradeProductPlugin.getName());
      } else {
        LOG.info("Add Product UpgradePlugin '{}'", upgradeProductPlugin.getName());
        upgradePlugins.add(upgradeProductPlugin);
        allUpgradePlugins.add(upgradeProductPlugin);
      }
    } else {
      LOG.info("UpgradePlugin: name = '{}' is disabled.", upgradeProductPlugin.getName());
    }
  }

  /**
   * This method is called by eXo Kernel when starting the parent
   * ExoContainer
   */
  public void start() {
    // Make sure that related services are started before starting this service
    productInformations.start();
    if (productInformations.isFirstRun()) {
      LOG.info("Proceed upgrade on first run = {}", proceedUpgradeFirstRun);

      // If first run of upgrade API, and if disabled on first run, ignore
      // plugins
      if (!proceedUpgradeFirstRun) {
        LOG.info("Ignore all upgrade plugins");
        for (UpgradeProductPlugin upgradeProductPlugin : allUpgradePlugins) {
          // Mark Plugin as executed to avoid that the plugin is executed future
          // version upgrade
          String currentProductPluginVersion = getCurrentVersion(upgradeProductPlugin);
          UpgradePluginExecutionContext currenUpgradePluginExecutionContext =
                                                                            new UpgradePluginExecutionContext(currentProductPluginVersion,
                                                                                                              0);
          storeUpgradePluginVersion(upgradeProductPlugin, currenUpgradePluginExecutionContext);
        }
        return;
      }

      // If first run, set previous version to 0
      productInformations.setPreviousVersionsIfFirstRun(PRODUCT_VERSION_ZERO);
    }

    // Sort the upgrade Plugins to use the execution order
    Collections.sort(upgradePlugins, pluginsComparator);

    LOG.info("Start transparent upgrade framework");

    // If the upgradePluginNames array contains less elements than
    // the upgradePlugins list, execute these remaining plugins.
    for (UpgradeProductPlugin upgradeProductPlugin : upgradePlugins) {
      try {
        // Get stored version for this specific Upgrade Plugin from
        // SettingService
        UpgradePluginExecutionContext previousUpgradePluginExecutionContext =
                                                                            getPreviousUpgradePluginVersion(upgradeProductPlugin);
        String previousUpgradePluginVersion =
                                            previousUpgradePluginExecutionContext == null ? null
                                                                                          : previousUpgradePluginExecutionContext.getVersion();
        // If the specific version is null, get it from GroupId an store it in
        // SettingService
        // The retrieval from GroupId will not be done if checkGroupIdVersion
        // == null
        String previousGroupVersion = getPreviousVersionByGroupId(upgradeProductPlugin);

        String previousVersion = StringUtils.isBlank(previousUpgradePluginVersion) ? previousGroupVersion
                                                                                   : previousUpgradePluginVersion;

        // Get current running version
        String currentVersion = getCurrentVersion(upgradeProductPlugin);

        // The plugin will determine if it should proceed to upgrade
        if (upgradeProductPlugin.shouldProceedToUpgrade(currentVersion,
                                                        previousGroupVersion,
                                                        previousUpgradePluginExecutionContext)) {
          // Store previous version for this specific plugin. This version
          // will be updated to currentVersion
          // only if proceedToUpgrade succeeds, else, the plugin will be
          // executed again.
          // In case of isExecuteOnlyOnce==true, we shouldn't store any
          // information for the specific plugin,
          // else it will not be executed if an error occurs
          if (StringUtils.isBlank(previousUpgradePluginVersion)) {
            previousUpgradePluginExecutionContext = new UpgradePluginExecutionContext(previousGroupVersion, 0);
            storeUpgradePluginVersion(upgradeProductPlugin, previousUpgradePluginExecutionContext);
          }

          // Proceed to upgrade upgrade plugin
          LOG.info("Proceed upgrade the plugin (async = {}): name = {} from version {} to {}",
                   upgradeProductPlugin.isAsyncUpgradeExecution(),
                   upgradeProductPlugin.getName(),
                   previousVersion,
                   currentVersion);
          if (upgradeProductPlugin.isAsyncUpgradeExecution()) {
            final UpgradePluginExecutionContext previousUpgradePluginExecutionContextFinal =
                                                                                           previousUpgradePluginExecutionContext;
            Runnable task = () -> {
              ExoContainerContext.setCurrentContainer(portalContainer);
              RequestLifeCycle.begin(portalContainer);
              try {
                proceedToUpgrade(upgradeProductPlugin,
                                 currentVersion,
                                 previousVersion,
                                 previousUpgradePluginExecutionContextFinal);
              } finally {
                RequestLifeCycle.end();
              }
            };
            executorService.execute(task);
          } else {
            proceedToUpgrade(upgradeProductPlugin, currentVersion, previousVersion, previousUpgradePluginExecutionContext);
          }
        } else {
          LOG.info("Ignore upgrade plugin {} from version {} to {}",
                   upgradeProductPlugin.getName(),
                   previousVersion,
                   currentVersion);
        }
      } catch (Exception e) {
        LOG.error("Error while upgrading plugin with name '" + upgradeProductPlugin.getName()
            + "'. The upgrade plugin will attempt again next startup.", e);
      }
    }
    try {
      productInformations.initProductInformation(productInformations.getProductInformationProperties());
      productInformations.storeProductInformation(productInformations.getProductInformation());
    } catch (Exception e) {
      LOG.error("Error while executing upgrade plugins", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stop() {
    executorService.shutdown();
  }

  @Override
  public boolean isDone() {
    // Avoid start this service in other cluster nodes
    return false;
  }

  /**
   * Re-import all upgrade-plugins for service
   */
  public void resetService()
  {
    //Reset product information
    productInformations.start();

    //Reload list Upgrade-Plugins
    upgradePlugins.clear();
    Iterator<UpgradeProductPlugin> iterator= allUpgradePlugins.iterator();
    while(iterator.hasNext())
    {
      UpgradeProductPlugin upgradeProductPlugin= iterator.next();
      upgradePlugins.add(upgradeProductPlugin);
    }
  }

  private void proceedToUpgrade(UpgradeProductPlugin upgradeProductPlugin, String currentVersion, String previousVersion, UpgradePluginExecutionContext upgradePluginExecutionContext) {
    upgradeProductPlugin.beforeUpgrade();
    try {
      upgradeProductPlugin.processUpgrade(previousVersion, currentVersion);

      upgradePluginExecutionContext.setExecutionCount(upgradePluginExecutionContext.getExecutionCount() + 1);
      upgradePluginExecutionContext.setVersion(currentVersion);
      storeUpgradePluginVersion(upgradeProductPlugin, upgradePluginExecutionContext);
      LOG.info("Upgrade of plugin {} completed.", upgradeProductPlugin.getName());
    } catch (Exception e) {
      LOG.error("Error while upgrading plugin with name '" + upgradeProductPlugin.getName()
              + "'. The upgrade plugin will attempt again next startup.", e);
    } finally {
      upgradeProductPlugin.afterUpgrade();
    }
  }

  private String getCurrentVersion(UpgradeProductPlugin upgradeProductPlugin) {
    String currentUpgradePluginVersion = null;
    try {
      currentUpgradePluginVersion = productInformations.getVersion(upgradeProductPlugin.getName());
    } catch (MissingProductInformationException e) {
      try {
        currentUpgradePluginVersion = productInformations.getVersion(upgradeProductPlugin.getProductGroupId());
      } catch (MissingProductInformationException e1) {
        currentUpgradePluginVersion = PRODUCT_VERSION_ZERO;
      }
    }
    return currentUpgradePluginVersion;
  }

  private String getPreviousVersionByGroupId(UpgradeProductPlugin upgradeProductPlugin) {
    String previousUpgradePluginVersion;
    try {
      previousUpgradePluginVersion = productInformations.getPreviousVersion(upgradeProductPlugin.getName());
    } catch (MissingProductInformationException e) {
      try {
        previousUpgradePluginVersion = productInformations.getPreviousVersion(upgradeProductPlugin.getProductGroupId());
      } catch (MissingProductInformationException e1) {
        previousUpgradePluginVersion = PRODUCT_VERSION_ZERO;
      }
    }
    if (StringUtils.isBlank(previousUpgradePluginVersion)) {
      previousUpgradePluginVersion = PRODUCT_VERSION_ZERO;
    }
    return previousUpgradePluginVersion;
  }

  private void storeUpgradePluginVersion(UpgradeProductPlugin upgradeProductPlugin, UpgradePluginExecutionContext upgradePluginExecution) {
    if (upgradePluginExecution == null) {
      throw new IllegalArgumentException("UpgradePluginExecution is null");
    }
    Scope upgradePluginScope = getUpgradePluginScope(upgradeProductPlugin);
    settingService.set(UPGRADE_PRODUCT_CONTEXT, upgradePluginScope, UPGRADE_PLUGIN_VERSION_KEY, SettingValue.create(upgradePluginExecution.toString()));
  }

  private UpgradePluginExecutionContext getPreviousUpgradePluginVersion(UpgradeProductPlugin upgradeProductPlugin) {
    Scope upgradePluginScope = getUpgradePluginScope(upgradeProductPlugin);
    SettingValue<?> upgradePluginVersion = settingService.get(UPGRADE_PRODUCT_CONTEXT, upgradePluginScope, UPGRADE_PLUGIN_VERSION_KEY);
    return upgradePluginVersion == null ? null : new UpgradePluginExecutionContext(upgradePluginVersion.getValue().toString());
  }

  private Scope getUpgradePluginScope(UpgradeProductPlugin upgradeProductPlugin) {
    return Scope.APPLICATION.id(upgradeProductPlugin.getName());
  }

}
