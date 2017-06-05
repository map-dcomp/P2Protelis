package com.bbn.protelis.networkresourcemanagement;

import java.util.Collections;
import java.util.HashSet;
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

import com.bbn.protelis.utils.StringUID;

/**
 * A node in the network.
 */
public class Node extends AbstractExecutionContext {

    /**
     * Used when there is no region name.
     */
    public static final String NULL_REGION_NAME = "__null-region__";

    private static final Logger LOGGER = LoggerFactory.getLogger(Node.class);

    /** Device numerical identifier */
    private final StringUID uid;

    /** The Protelis VM to be executed by the device */
    private final ProtelisVM vm;

    /**
     * Default time to sleep between executions. Specified in milliseconds.
     */
    public static final long DEFAULT_SLEEP_TIME_MS = 2 * 1000;

    /**
     * The key into extra data passed to {@link #processExtraData(Map)} that
     * specifies the region for a node.
     */
    public static final String EXTRA_DATA_REGION_KEY = "region";

    private AtomicLong executionCount = new AtomicLong(0);

    private void incrementExecutionCount() {
        executionCount.incrementAndGet();
    }

    /**
     * The number of times this node has executed.
     * 
     * @return the number of times that this {@link Node} has executed the
     *         program.
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
    private final Set<DeviceUID> neighbors = new HashSet<>();

    /**
     * The neighbors of this {@link Node}.
     * 
     * @return unmodifiable set
     */
    public final Set<DeviceUID> getNeighbors() {
        return Collections.unmodifiableSet(neighbors);
    }

    /**
     * Add a neighbor.
     * 
     * @param v
     *            the UID of the neighbor
     */
    public final void addNeighbor(final DeviceUID v) {
        neighbors.add(v);
    }

    /**
     * 
     * @param v
     *            the neighbor to add
     * @see #addNeighbor(DeviceUID)
     */
    public final void addNeighbor(final Node v) {
        addNeighbor(v.getDeviceUID());
    }

    /**
     * @param program
     *            the program to run on the node
     * @param name
     *            the name of the node (must be unique)
     * @param lookupService
     *            How to find other nodes
     */
    public Node(@Nonnull final NodeLookupService lookupService, @Nonnull final ProtelisProgram program,
            @Nonnull final String name) {
        super(new SimpleExecutionEnvironment(), new NodeNetworkManager(lookupService));
        this.uid = new StringUID(name);
        this.regionName = NULL_REGION_NAME;
        this.networkState = new NetworkState(this);

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
        return uid.getUID();
    }

    @Override
    public final StringUID getDeviceUID() {
        return uid;
    }

    @Override
    public final Number getCurrentTime() {
        return System.currentTimeMillis();
    }

    @Override
    protected final AbstractExecutionContext instance() {
        throw new UnsupportedOperationException();
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

    /**
     * Execute the protolis program.
     */
    private void run() {
        while (!Thread.interrupted()) {
            try {
                preRunCycle();

                gatherResourceInformation();

                getVM().runCycle(); // execute the Protelis program
                incrementExecutionCount();

                postRunCycle();

                Thread.sleep(sleepTime);
            } catch (final InterruptedException e) {
                LOGGER.debug("Node " + getName() + " got interrupted, time to quit", e);
                break;
            } catch (final Exception e) {
                LOGGER.error("Exception thrown: terminating Protelis on node: " + getName(), e);
                break;
            }
        }
    }

    private Thread executeThread = null;

    /**
     * 
     * @return is the node currently executing?
     */
    public final boolean isExecuting() {
        return null != executeThread && executeThread.isAlive();
    }

    /**
     * Start the node executing.
     */
    public final void startExecuting() {
        if (null != executeThread) {
            throw new IllegalStateException("Already executing, cannot start again!");
        }

        accessNetworkManager().start(this);

        executeThread = new Thread(() -> run());
        executeThread.setName("Node-" + getName());
        executeThread.start();
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
        if (null != executeThread) {
            preStopExecuting();

            executeThread.interrupt();
            try {
                executeThread.join(); // may want to have a timeout here
            } catch (final InterruptedException e) {
                LOGGER.debug("Got interrupted waiting for join, probably just time to shutdown", e);
            }
            executeThread = null;
        }
    }

    private ResourceReport latestResourceReport = new ResourceReport();

    /**
     * Get the latest resource report. This method should be called once per
     * cycle and then used to make decisions. This method may call directly to
     * the {@link ResourceManager} to provide gather the information.
     * 
     * @return the latest resource report
     */
    @Nonnull
    public ResourceReport getResourceReport() {
        return this.latestResourceReport;
    }

    /**
     * Gather information about the resources used on this node.
     */
    protected void gatherResourceInformation() {
        // FIXME implement to populate latestResourceReport
    }

    private String regionName;

    /**
     * Changing the region has the side effect of resetting the resource
     * summary.
     * 
     * @param region
     *            the new region that this node belongs to
     * @see NetworkState#getRegionSummary()
     */
    public void setRegionName(final String region) {
        this.regionName = region;
        this.getNetworkState().setRegionSummary(ResourceSummary.getNullSummary(region));
    }

    /**
     * @return the name of the region that this node currently belongs to, may
     *         be null
     */
    public String getRegionName() {
        return this.regionName;
    }

    /**
     * Process the extra data that was found when creating the node.
     * 
     * @param extraData
     *            key/value pairs
     * @see NetworkFactory#createNode(String, java.util.Map)
     */
    public void processExtraData(@Nonnull final Map<String, String> extraData) {
        final String region = extraData.get(EXTRA_DATA_REGION_KEY);
        if (null != region) {
            this.setRegionName(region);
        }
    }

    private final NetworkState networkState;

    /**
     * All of the information that this node knows about the state of the
     * network.
     * 
     * @return the network state
     */
    @Nonnull
    public NetworkState getNetworkState() {
        return networkState;
    }
}
