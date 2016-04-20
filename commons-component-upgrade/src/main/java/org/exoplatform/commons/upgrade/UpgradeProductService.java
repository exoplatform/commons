package org.exoplatform.commons.upgrade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.info.MissingProductInformationException;
import org.exoplatform.commons.info.ProductInformations;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

public class UpgradeProductService implements Startable {

  private static final Log LOG = ExoLogger.getLogger(UpgradeProductService.class);
  private static final String PLUGINS_ORDER = "commons.upgrade.plugins.order";
  private static final String PROCEED_UPGRADE_FIRST_RUN_KEY = "proceedUpgradeWhenFirstRun";
  private static final String PRODUCT_VERSION_ZERO = "0";

  private List<UpgradeProductPlugin> upgradePlugins = new ArrayList<UpgradeProductPlugin>();
  private Set<UpgradeProductPlugin> allUpgradePlugins= new HashSet<UpgradeProductPlugin>();
  private ProductInformations productInformations = null;
  private boolean proceedUpgradeFirstRun = false;

  private Comparator<UpgradeProductPlugin> pluginsComparator             = null;

  /**
   * Constructor called by eXo Kernel
   * 
   * @param productInformations
   */
  public UpgradeProductService(ProductInformations productInformations, InitParams initParams) {
    this.productInformations = productInformations;
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
    for (int i = 0; i < upgradePlugins.size(); i++) {
      UpgradeProductPlugin upgradeProductPlugin = upgradePlugins.get(i);
      String previousProductPluginVersion = getPreviousVersion(upgradeProductPlugin);
      String currentProductPluginVersion = getCurrentVersion(upgradeProductPlugin);

      // The plugin will determine if it should proceed to upgrade
      if (upgradeProductPlugin.shouldProceedToUpgrade(currentProductPluginVersion, previousProductPluginVersion)) {
        LOG.info("Proceed upgrade plugin: name = " + upgradeProductPlugin.getName() + " from version "
            + previousProductPluginVersion + " to " + currentProductPluginVersion);
        // Product upgrade plugin
        upgradeProductPlugin.processUpgrade(previousProductPluginVersion, currentProductPluginVersion);
        LOG.info("Upgrade " + upgradeProductPlugin.getName() + " completed.");
      } else {
        LOG.info("Ignore upgrade plugin {} from version {} to {}",
                 upgradeProductPlugin.getName(),
                 previousProductPluginVersion,
                 currentProductPluginVersion);
      }
    }

    productInformations.storeProductsInformationsInJCR();

    LOG.info("Version upgrade completed.");
  }

  /**
   * {@inheritDoc}
   */
  public void stop() {}
  
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

  private String getPreviousVersion(UpgradeProductPlugin upgradeProductPlugin) {
    String previousUpgradePluginVersion = null;
    try {
      previousUpgradePluginVersion = productInformations.getPreviousVersion(upgradeProductPlugin.getName());
    } catch (MissingProductInformationException e) {
      try {
        previousUpgradePluginVersion = productInformations.getPreviousVersion(upgradeProductPlugin.getProductGroupId());
      } catch (MissingProductInformationException e1) {
        previousUpgradePluginVersion = PRODUCT_VERSION_ZERO;
      }
    }
    return previousUpgradePluginVersion;
  }

}
