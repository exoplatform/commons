package org.exoplatform.commons.cluster;

/**
 * A class implementing this interface is executed will be started (execution of the method start())
 * during the startup of the platform but on one node only if running in cluster.
 * If the node running the cluster aware service is stopped during the execution of the service,
 * it will be started again on another cluster node, unless the isDone() method returns true.
 */
public interface StartableClusterAware {
    /**
     * Start service by the current node
     */
    void start();

    /**
     * Check if service is done
     * @return
     */
    boolean isDone();

    /**
     * Stop service by the current node
     */
    default void stop() {}
}
