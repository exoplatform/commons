package org.exoplatform.commons.cluster;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.idgenerator.IDGeneratorService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

/**
 * Check cluster node Name and generate auto name if not configured
 */
public class ClusterNodeName implements Startable{

    private static final String CLUSTER_NODE_NAME = "exo.cluster.node.name";
    private static final Log LOG = ExoLogger.getLogger(ClusterNodeName.class);

    private String nodeName;
    private IDGeneratorService idGeneratorService;

    public ClusterNodeName(IDGeneratorService idGeneratorService) {
        this.idGeneratorService = idGeneratorService;
    }

    @Override
    public void start() {
        nodeName = PropertyManager.getProperty(CLUSTER_NODE_NAME);
        boolean clusterEnabled = (PropertyManager.getProperty(PropertyManager.RUNTIME_PROFILES) != null ) ?
                PropertyManager.getProperty(PropertyManager.RUNTIME_PROFILES).contains("cluster") : false;
        if(clusterEnabled && StringUtils.isBlank(nodeName)){
            nodeName = "node-"+ idGeneratorService.generateStringID(Long.toString(System.currentTimeMillis()));
            PropertyManager.setProperty(CLUSTER_NODE_NAME, nodeName);
            LOG.warn("Cluster node name is not configured, node name will be auto generated. Cluster node name {}", nodeName);
        }
    }

    @Override
    public void stop() {

    }

    public String getNodeName() {
        return nodeName;
    }
}

