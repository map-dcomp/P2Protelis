package com.bbn.protelis.networkresourcemanagement;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;

import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.ProtelisProgram;
import org.protelis.vm.ProtelisVM;
import org.protelis.vm.impl.AbstractExecutionContext;
import org.protelis.vm.impl.SimpleExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

/**
 * A server in the network.
 */
public class NetworkServer extends AbstractExecutionContext
        implements NetworkStateProvider, RegionNodeStateProvider, NetworkNode {

    /**
     * Used when there is no region name.
     */
    public static final StringRegionIdentifier NULL_REGION = new StringRegionIdentifier("__null-region__");

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkServer.class);

    /** Device numerical identifier */
    private final StringNodeIdentifier uid;

    /** The Protelis VM to be executed by the device */
    private final ProtelisVM vm;

    /**
     * Default time to sleep between executions. Specified in milliseconds.
     */
    public static final long DEFAULT_SLEEP_TIME_MS = 2 * 1000;

    /**
     * The key into extra data passed to {@link #processExtraData(Map)} that
     * specifies the region for a node. This will create a
     * {@link StringRegionIdentifier}.
     */
    public static final String EXTRA_DATA_REGION_KEY = "region";

    /**
     * Extra data key to specify if the node is a single server or a pool of
     * servers.
     */
    public static final String EXTRA_DATA_POOL = "pool";

    private AtomicLong executionCount = new AtomicLong(0);

    private void incrementExecutionCount() {
        executionCount.incrementAndGet();
    }

    private boolean pool = false;

    private void setPool(final boolean v) {
        pool = v;
    }

    /**
     * 
     * @return true if this Node is a pool of resources
     * @see #EXTRA_DATA_POOL
     */
    public boolean isPool() {
        return pool;
    }

    /**
     * The number of times this node has executed.
     * 
     * @return the number of times that this {@link NetworkServer} has executed
     *         the program.
     */
    public final long getExecutionCount() {
        return executionCount.get();
    }

    private long sleepTime = DEFAULT_SLEEP_TIME_MS;

    /**
     * @return How long between executions of the protelis program. Defaults to
     *         {@link #DEFAULT_SLEEP_TIME_MS}.
     */
    public final long getSleepTime() {
        return sleepTime;
    }

    /**
     * @param v
     *            Specify the sleep time
     * @see #getSleepTime()
     */
    public final void setSleepTime(final long v) {
        sleepTime = v;
    }

    /**
     * The neighboring nodes.
     */
    private final Map<NodeIdentifier, Double> neighbors = new HashMap<>();

    @Override
    @Nonnull
    public final Set<NodeIdentifier> getNeighbors() {
        return Collections.unmodifiableSet(neighbors.keySet());
    }

    @Override
    public final void addNeighbor(@Nonnull final NodeIdentifier v, final double bandwidth) {
        neighbors.put(v, bandwidth);
    }

    @Override
    public final void addNeighbor(@Nonnull final NetworkNode v, final double bandwidth) {
        addNeighbor(v.getNodeIdentifier(), bandwidth);
    }

    /**
     * @return link capacity to neighbors
     * @see #addNeighbor(NodeIdentifier, double)
     * @see ResourceReport#getNeighborLinkCapacity()
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute, Double>> getNeighborLinkCapacity() {
        ImmutableMap.Builder<NodeIdentifier, ImmutableMap<LinkAttribute, Double>> builder = ImmutableMap.builder();
        neighbors.forEach((k, v) -> builder.put(k, ImmutableMap.of(LinkAttribute.DATARATE, v)));
        return builder.build();
    }

    /**
     * Creates a {@link NetworkServer} with a {@link NullResourceManager}.
     * 
     * @param program
     *            the program to run on the node
     * @param name
     *            the name of the node (must be unique)
     * @param lookupService
     *            How to find other nodes
     */
    public NetworkServer(@Nonnull final NodeLookupService lookupService,
            @Nonnull final ProtelisProgram program,
            @Nonnull final String name) {
        super(new SimpleExecutionEnvironment(), new NodeNetworkManager(lookupService));
        this.uid = new StringNodeIdentifier(name);
        this.region = NULL_REGION;
        this.networkState = new NetworkState(this.region);
        this.regionNodeState = new RegionNodeState(this.region);
        this.resourceManager = new NullResourceManager(this.uid);

        // Finish making the new device and add it to our collection
        vm = new ProtelisVM(program, this);
    }

    /**
     * @return Accessor for virtual machine, to allow external execution
     *         triggering
     */
    public final ProtelisVM getVM() {
        return vm;
    }

    /**
     * Expose the network manager. This is to allow external simulation of
     * network For real devices, the NetworkManager usually runs autonomously in
     * its own thread(s).
     * 
     * @return the node specific version of the network manager
     */
    public final NodeNetworkManager accessNetworkManager() {
        return (NodeNetworkManager) super.getNetworkManager();
    }

    /**
     * 
     * @return the name of the node
     */
    public final String getName() {
        return uid.getName();
    }

    @Override
    public final NodeIdentifier getNodeIdentifier() {
        return uid;
    }

    @Override
    public final DeviceUID getDeviceUID() {
        return uid;
    }

    @Override
    public final Number getCurrentTime() {
        return System.currentTimeMillis();
    }

    /**
     * Child context used for {@link NetworkServer#instance()}.
     */
    public class ChildContext extends AbstractExecutionContext {
        private NetworkServer parent;

        /**
         * Create a child context.
         * 
         * @param parent
         *            the parent environment to get information from.
         */
        public ChildContext(final NetworkServer parent) {
            super(parent.getExecutionEnvironment(), parent.getNetworkManager());
            this.parent = parent;
        }

        @Override
        public Number getCurrentTime() {
            return parent.getCurrentTime();
        }

        @Override
        public NodeIdentifier getDeviceUID() {
            return parent.getNodeIdentifier();
        }

        @Override
        public double nextRandomDouble() {
            return parent.nextRandomDouble();
        }

        @Override
        protected AbstractExecutionContext instance() {
            return new ChildContext(parent);
        }

        // TODO: need to better fix the whole ChildContext architecture
        /**
         * @return the region of the parent
         */
        public RegionIdentifier getRegion() {
            return parent.getRegionIdentifier();
        }

        /**
         * 
         * @return parent network state
         */
        public NetworkState getNetworkState() {
            return parent.getNetworkState();
        }

        /**
         * 
         * @return parent region node state
         */
        public RegionNodeState getRegionNodeState() {
            return parent.getRegionNodeState();
        }
    }

    @Override
    protected AbstractExecutionContext instance() {
        return new ChildContext(this);
    }

    @Override
    public final double nextRandomDouble() {
        return Math.random();
    }

    /**
     * Executed before {@link ProtelisVM#runCycle()}.
     */
    protected void preRunCycle() {
    }

    /**
     * Executed after {@link ProtelisVM#runCycle()}.
     */
    protected void postRunCycle() {
    }

    private Exception programLoopException = null;

    /**
     * This is used for test cases. One can check this value to see if the
     * execute loop exited due to an exception.
     * 
     * @return the exception thrown in the program loop, null otherwise
     */
    public Exception getExceptionThrownInProgramLoop() {
        return programLoopException;
    }

    /**
     * Execute the protolis program.
     */
    private void run() {
        while (!Thread.interrupted()) {
            try {
                preRunCycle();

                getVM().runCycle(); // execute the Protelis program
                incrementExecutionCount();

                postRunCycle();

                Thread.sleep(sleepTime);
            } catch (final InterruptedException e) {
                LOGGER.debug("Node " + getName() + " got interrupted, time to quit", e);
                break;
            } catch (final Exception e) {
                LOGGER.error("Exception thrown: terminating Protelis on node: " + getName(), e);
                programLoopException = e;
                break;
            }
        }
    }

    private final Object lock = new Object();

    private Thread executeThread = null;

    /**
     * 
     * @return is the node currently executing?
     */
    public final boolean isExecuting() {
        synchronized (lock) {
            return running && null != executeThread && executeThread.isAlive();
        }
    }

    private boolean running = false;

    /**
     * Start the node executing.
     */
    public final void startExecuting() {
        synchronized (lock) {
            if (running) {
                throw new IllegalStateException("Already executing, cannot start again!");
            }

            running = true;

            accessNetworkManager().start(this);

            executeThread = new Thread(() -> run());
            executeThread.setName("Node-" + getName());
            executeThread.start();
        }
    }

    /**
     * Called at the top of {@link #stopExecuting()}.
     */
    protected void preStopExecuting() {
    }

    /**
     * Stop the node executing and wait for the stop.
     */
    public final void stopExecuting() {
        if (isExecuting()) {
            preStopExecuting();

            synchronized (lock) {
                running = false;
                if (null != executeThread) {
                    executeThread.interrupt();
                    try {
                        executeThread.join(); // may want to have a timeout here
                    } catch (final InterruptedException e) {
                        LOGGER.debug("Got interrupted waiting for join, probably just time to shutdown", e);
                    }
                    executeThread = null;
                } // non-null executeThread
            } // lock
        } // isExecuting
    }

    private ResourceManager resourceManager;

    /**
     * 
     * @param resourceManager
     *            the new {@link ResourceManager} for this node
     */
    public void setResourceManager(@Nonnull final ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    /**
     * Get the latest resource report. This method should be called once per
     * cycle and then used to make decisions. This method may call directly to
     * the {@link ResourceManager} to provide gather the information.
     * 
     * @return the latest resource report
     */
    @Nonnull
    public ResourceReport getResourceReport() {
        return resourceManager.getCurrentResourceReport();
    }

    private RegionIdentifier region;

    /**
     * Changing the region has the side effect of resetting the network state
     * and the regional node state.
     * 
     * @param region
     *            the new region that this node belongs to
     * @see #getNetworkState()
     */
    public void setRegion(final RegionIdentifier region) {
        this.region = region;
        this.networkState = new NetworkState(this.region);
        this.regionNodeState = new RegionNodeState(this.region);
    }

    @Override
    public RegionIdentifier getRegionIdentifier() {
        return this.region;
    }

    @Override
    public void processExtraData(@Nonnull final Map<String, Object> extraData) {
        final Object regionValue = extraData.get(EXTRA_DATA_REGION_KEY);
        if (null != regionValue) {
            final String regionName = regionValue.toString();
            final StringRegionIdentifier region = new StringRegionIdentifier(regionName);
            this.setRegion(region);
        }

        final Object pool = extraData.get(EXTRA_DATA_POOL);
        if (null != pool) {
            this.setPool(Boolean.parseBoolean(pool.toString()));
        }
    }

    private String hardware;

    @Override
    public String getHardware() {
        return hardware;
    }

    @Override
    public void setHardware(final String hardware) {
        this.hardware = hardware;
    }

    // ---- NetworkStateProvider
    private NetworkState networkState;

    @Override
    @Nonnull
    public NetworkState getNetworkState() {
        return networkState;
    }
    // ---- end NetworkStateProvider

    // ---- RegionNodeStateProvider
    private RegionNodeState regionNodeState;

    @Override
    @Nonnull
    public RegionNodeState getRegionNodeState() {
        return regionNodeState;
    }
    // ---- end RegionNodeStateProvider

    @Override
    public String toString() {
        return getName();
    }

}
