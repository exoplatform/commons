package org.exoplatform.commons.cluster;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.spi.ComponentAdapter;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This service allow to running a specific service (implements StartableClusterAware interface) on one cluster node.
 */
public class StartableClusterService implements Startable {

    private static final Log LOG = ExoLogger.getLogger(StartableClusterService.class);

    private ExoContainer container;
    private SettingService settingService;

    /**
     * Registers shutdown hook.
     */
    private final Thread hook = new ShutdownThread();

    /**
     * Timer to schedule check runnable cluster aware service after some idle time.
     */
    private static final Timer CLUSTER_AWARE_TIMER = new Timer("CLUSTER AWARE Timer", true);

    /**
     * Task that is periodically called by {@link #CLUSTER_AWARE_TIMER} and checks if
     * service should be started
     */
    private TimerTask task;


    private static final String CLUSTER_NODE_NAME = "exo.cluster.node.name";
    private static final String CLUSTER_CHECK_PERIOD = "exo.cluster.startable.check.period";
    private static final String CLUSTER_SERVICE_SETTING_GLOBAL_KEY = "CLUSTER_SERVICE_CLUSTER_STARTABLE_SERVICE";

    private static Map<Object,Boolean> services = new HashMap<>();
    private final static String nodeName;
    private final static boolean clusterEnabled;

    /***
     * Check service period
     */
    private static long checkPeriod = 120000;

    static {
        nodeName = PropertyManager.getProperty(CLUSTER_NODE_NAME);
        clusterEnabled = (PropertyManager.getProperty(PropertyManager.RUNTIME_PROFILES) != null ) ? PropertyManager.getProperty(PropertyManager.RUNTIME_PROFILES).contains("cluster") : false;
    }


    public StartableClusterService(InitParams initParams, ExoContainerContext containerContext, SettingService settingService) {
        this.container = containerContext.getContainer();
        this.settingService = settingService;
        if (initParams.getValueParam(CLUSTER_CHECK_PERIOD) != null) {
            this.checkPeriod = Long.parseLong(initParams.getValueParam(CLUSTER_CHECK_PERIOD).getValue());
        }
        if (clusterEnabled && (nodeName == null || nodeName.isEmpty())) {
            LOG.error("Cluster node name cannot be empty, exo.cluster.node.name should be configured");
            throw new IllegalArgumentException("Cluster node name cannot be empty");
        }
    }

    @Override
    public void start() {
        /**Select all service implement StartableClusterAware**/
        for (ComponentAdapter componentAdapter : container.getComponentAdaptersOfType(StartableClusterAware.class)) {
            if (componentAdapter != null) {
                Object key = componentAdapter.getComponentKey();
                StartableClusterAware service = (StartableClusterAware) container.getComponentInstance(key);
                services.put(key,false);
                /**Start the service if is not done and is not yet started**/
                if (!service.isDone() && canStart(key)) {
                    if (clusterEnabled) {
                        LOG.info("Start service {} on node {} mode cluster aware ", key, nodeName);
                    }
                    service.start();
                    services.put(key,true);
                }
            }
        }
        /** Unregister node name , If  System.exit() is called before Thread migration is done.**/
        SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>() {
            public Void run() {
                ExoContainerContext.setCurrentContainer(container);
                Runtime.getRuntime().addShutdownHook(hook);
                return null;
            }
        });
        /**Register checker task, verify service state**/
        if (clusterEnabled) {
            boolean initTimer = false;
            /**Start Timer only if exist services already started by other node**/
            for(Object key : services.keySet()){
                if(!services.get(key)){
                    initTimer = true;
                    break;
                }
            }
            //existing services already started by other node or all services is done
            if(initTimer && !checkAllIsDone()) {
                task = new TimerTask() {
                    @Override
                    public void run() {
                        ExoContainerContext.setCurrentContainer(container);
                        //check  if exist at least one service is not done
                        //If all cluster aware services is done, cancel the actual timer Task
                        if(checkAllIsDone()){
                            cancel();
                        }

                        for (Object key : services.keySet()) {
                            if (services.get(key))
                                continue; //This services is already started by current node
                            SettingValue<String> serviceSetting = currentSetting(key.toString());
                            StartableClusterAware service = (StartableClusterAware) container.getComponentInstance(key);
                            if (serviceSetting != null && serviceSetting.getValue().isEmpty()) {
                                if (!service.isDone() && canStart(key)) {
                                    LOG.info("Start service {} on node {} mode cluster aware ", key, nodeName);
                                    service.start();
                                    services.put(key, true);
                                }
                            }
                        }
                    }
                };
                CLUSTER_AWARE_TIMER.schedule(task, 0, checkPeriod);
            }
        }
    }

    @Override
    public void stop() {
        resetSetting();
        for (ComponentAdapter<?> componentAdapter : container.getComponentAdaptersOfType(StartableClusterAware.class)) {
          if (componentAdapter != null) {
              Object key = componentAdapter.getComponentKey();
              StartableClusterAware service = (StartableClusterAware) container.getComponentInstance(key);
              service.stop();
          }
      }
    }

    //***** Internal Methods *****//

    /***
     * Check if current node can start the specific service
     * @param key service key
     * @return
     */
    private boolean canStart(Object key) {
        if (!clusterEnabled)
            return true;
        String name = getServiceSettings(key.toString(), nodeName);
        if (name == null) {
            LOG.error("Unable to get service setting {} ", key.toString());
            return false;
        }
        return nodeName.equalsIgnoreCase(name);
    }

    /**
     * Get or update current value of service
     * @param key service key
     * @param defaultValue current node name
     * @return
     */
    private String getServiceSettings(String key, String defaultValue) {
        try {
            SettingValue<String> serviceSetting = currentSetting(key);
            if (serviceSetting != null && !serviceSetting.getValue().isEmpty()) {
                return serviceSetting.getValue();
            } else {
                updateServiceSettings(key, defaultValue);
                //double checking of state
                serviceSetting = currentSetting(key);
                return serviceSetting.getValue();
            }
        } finally {
            Scope.GLOBAL.id(null);
        }
    }

    /**
     * Get current service state
     * @param key service key
     * @return
     */
    private SettingValue<String> currentSetting(String key) {
        return (SettingValue<String>) settingService.get(Context.GLOBAL, Scope.GLOBAL.id(CLUSTER_SERVICE_SETTING_GLOBAL_KEY), key);
    }

    /**
     * Update service setting
     * @param key service key
     * @param value node name
     */
    private void updateServiceSettings(String key, String value) {
        try {
            RequestLifeCycle.begin(ExoContainerContext.getCurrentContainer());
            settingService.set(Context.GLOBAL, Scope.GLOBAL.id(CLUSTER_SERVICE_SETTING_GLOBAL_KEY), key, SettingValue.create(value));

        } finally {
            RequestLifeCycle.end();
            Scope.GLOBAL.id(null);
        }
    }

    /**
     * Reset all service setting running by the current node
     */
    private void resetSetting() {
        for (Object key : services.keySet()) {
            SettingValue<String> serviceSetting = currentSetting(key.toString());
            if (serviceSetting != null && serviceSetting.getValue().equalsIgnoreCase(nodeName)) {
                updateServiceSettings(key.toString(), "");
            }
        }
    }

    /**
     * Check if all cluster aware services is done
     */
    private boolean checkAllIsDone(){
        for (Object key : services.keySet()) {
            StartableClusterAware service = (StartableClusterAware) container.getComponentInstance(key);
            if (!service.isDone()){
                return  false;
            }
        }
        return true;
    }

    private class ShutdownThread extends Thread {
        @Override
        public void run() {
            SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>() {
                public Void run() {
                    ExoContainerContext.setCurrentContainer(container);
                    resetSetting();
                    return null;
                }
            });
        }
    }
}
