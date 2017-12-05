package org.exoplatform.commons.upgrade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.info.MissingProductInformationException;
import org.exoplatform.commons.info.ProductInformations;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

public class UpgradeProductService implements Startable {

  public static final Context              UPGRADE_PRODUCT_CONTEXT       = Context.GLOBAL.id("UPGRADE_PRODUCT_CONTEXT");

  private static final Log                 LOG                           = ExoLogger.getLogger(UpgradeProductService.class);

  private static final String              UPGRADE_PLUGIN_VERSION_KEY    = "UPGRADE_PLUGIN_VERSION";

  private static final String              PLUGINS_ORDER                 = "commons.upgrade.plugins.order";

  private static final String              PROCEED_UPGRADE_FIRST_RUN_KEY = "proceedUpgradeWhenFirstRun";

  private static final String              PRODUCT_VERSION_ZERO          = "0";

  private ExecutorService executorService = Executors.newCachedThreadPool();

  private PortalContainer portalContainer;

  private List<UpgradeProductPlugin> upgradePlugins = new ArrayList<UpgradeProductPlugin>();
  private Set<UpgradeProductPlugin> allUpgradePlugins= new HashSet<UpgradeProductPlugin>();
  private ProductInformations productInformations = null;
  private SettingService settingService;
  private boolean proceedUpgradeFirstRun = false;

  private Comparator<UpgradeProductPlugin> pluginsComparator             = null;

  /**
   * Constructor called by eXo Kernel
   * 
   * @param productInformations
   */
  public UpgradeProductService(PortalContainer portalContainer, SettingService settingService, ProductInformations productInformations, InitParams initParams) {
    this.productInformations = productInformations;
    this.settingService = settingService;
    this.portalContainer = portalContainer;
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
    if (productInformations.isFirstRun()) {
      LOG.info("Proceed upgrade on first run = {}", proceedUpgradeFirstRun);

      // If first run of upgrade API, and if disabled on first run, ignore plugins
      if (!proceedUpgradeFirstRun) {
        LOG.info("Ignore all upgrade plugins");
        for (UpgradeProductPlugin upgradeProductPlugin : allUpgradePlugins) {
          // Mark Plugin that should be executed only once as executed
          // to avoid that the plugin is executed future version upgrade
          if (upgradeProductPlugin.isExecuteOnlyOnce()) {
            String currentProductPluginVersion = getCurrentVersion(upgradeProductPlugin);
            storeUpgradePluginVersion(upgradeProductPlugin, currentProductPluginVersion);
          }
        }
        return;
      }

      // If first run, set previous version to 0
      productInformations.setPreviousVersionsIfFirstRun(PRODUCT_VERSION_ZERO);
    }

    // Sort the upgrade Plugins to use the execution order
    Collections.sort(upgradePlugins, pluginsComparator);

    LOG.info("Start transparent upgrade framework");

    try {
      // If the upgradePluginNames array contains less elements than
      // the upgradePlugins list, execute these remaining plugins.
      for (UpgradeProductPlugin upgradeProductPlugin : upgradePlugins) {
        // Get stored version for this specific Upgrade Plugin from SettingService
        String previousUpgradePluginVersion = getPreviousUpgradePluginVersion(upgradeProductPlugin);
        // If the specific version is null, get it from GroupId an store it in SettingService
        // The retrieval from GroupId will not be done if checkGroupIdVersion == null
        String previousGroupVersion = getPreviousVersionByGroupId(upgradeProductPlugin);

        String previousVersion = StringUtils.isBlank(previousUpgradePluginVersion) ? previousGroupVersion : previousUpgradePluginVersion;

        // Get current running version
        String currentVersion = getCurrentVersion(upgradeProductPlugin);

        try {
          // The plugin will determine if it should proceed to upgrade
          if (upgradeProductPlugin.shouldProceedToUpgrade(currentVersion, previousGroupVersion, previousUpgradePluginVersion)) {
            // Store previous version for this specific plugin. This version will be updated to currentVersion
            // only if proceedToUpgrade succeeds, else, the plugin will be executed again.
            // In case of isExecuteOnlyOnce==true, we shouldn't store any information for the specific plugin,
            // else it will not be executed if an error occurs 
            if (StringUtils.isBlank(previousUpgradePluginVersion) && !upgradeProductPlugin.isExecuteOnlyOnce()) {
              storeUpgradePluginVersion(upgradeProductPlugin, previousVersion);
            }

            // Proceed to upgrade upgrade plugin
            LOG.info("Proceed upgrade the plugin (async = {}): name = {} from version {} to {}",
                     upgradeProductPlugin.isAsyncUpgradeExecution(),
                     upgradeProductPlugin.getName(),
                     previousVersion,
                     currentVersion);
            if (upgradeProductPlugin.isAsyncUpgradeExecution()) {
              Runnable task = () -> {
                ExoContainerContext.setCurrentContainer(portalContainer);
                RequestLifeCycle.begin(portalContainer);
                try {
                  proceedToUpgrade(upgradeProductPlugin, currentVersion, previousVersion);
                } finally {
                  RequestLifeCycle.end();
                }
              };
              executorService.execute(task);
            } else {
              proceedToUpgrade(upgradeProductPlugin, currentVersion, previousVersion);
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

      productInformations.storeProductsInformationsInJCR();
    } catch (Exception e) {
      LOG.error("Error while executing upgrade plugins", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void stop() {
    executorService.shutdown();
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

  private void proceedToUpgrade(UpgradeProductPlugin upgradeProductPlugin, String currentVersion, String previousVersion) {
    upgradeProductPlugin.beforeUpgrade();
    try {
      upgradeProductPlugin.processUpgrade(previousVersion, currentVersion);

      storeUpgradePluginVersion(upgradeProductPlugin, currentVersion);
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

  private void storeUpgradePluginVersion(UpgradeProductPlugin upgradeProductPlugin, String version) {
    Scope upgradePluginScope = getUpgradePluginScope(upgradeProductPlugin);
    settingService.set(UPGRADE_PRODUCT_CONTEXT, upgradePluginScope, UPGRADE_PLUGIN_VERSION_KEY, SettingValue.create(version));
  }

  private String getPreviousUpgradePluginVersion(UpgradeProductPlugin upgradeProductPlugin) {
    Scope upgradePluginScope = getUpgradePluginScope(upgradeProductPlugin);
    SettingValue<?> upgradePluginVersion = settingService.get(UPGRADE_PRODUCT_CONTEXT, upgradePluginScope, UPGRADE_PLUGIN_VERSION_KEY);
    return upgradePluginVersion == null ? null : upgradePluginVersion.getValue().toString();
  }

  private Scope getUpgradePluginScope(UpgradeProductPlugin upgradeProductPlugin) {
    return Scope.APPLICATION.id(upgradeProductPlugin.getName());
  }
}
