/*BBN_LICENSE_START -- DO NOT MODIFY BETWEEN LICENSE_{START,END} Lines
Copyright (c) <2017,2018,2019>, <Raytheon BBN Technologies>
To be applied to the DCOMP/MAP Public Source Code Release dated 2019-03-14, with
the exception of the dcop implementation identified below (see notes).

Dispersed Computing (DCOMP)
Mission-oriented Adaptive Placement of Task and Data (MAP) 

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
BBN_LICENSE_END*/
package com.bbn.protelis.networkresourcemanagement;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;

import org.protelis.lang.datatype.DeviceUID;
import org.protelis.lang.datatype.Tuple;
import org.protelis.vm.ProtelisProgram;
import org.protelis.vm.ProtelisVM;
import org.protelis.vm.impl.AbstractExecutionContext;
import org.protelis.vm.impl.SimpleExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * A server in the network.
 */
public class NetworkServer extends AbstractExecutionContext
        implements NetworkStateProvider, RegionNodeStateProvider, RegionServiceStateProvider, NetworkNode {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkServer.class);

    /** Device numerical identifier */
    private final NodeIdentifier uid;

    /** The Protelis VM to be executed by the device */
    private final ProtelisVM vm;

    /**
     * Default time to sleep between executions. Specified in milliseconds.
     */
    public static final long DEFAULT_SLEEP_TIME_MS = 2 * 1000;

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
     * @return How many milliseconds between executions of the protelis program.
     *         Defaults to {@link #DEFAULT_SLEEP_TIME_MS}.
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
     * The neighboring nodes with the bandwidth to the neighbor.
     * 
     * @return Key is the neighbor, value is the datarate in megabits per
     *         second.
     * @see #addNeighbor(NodeIdentifier, double)
     */
    public Map<NodeIdentifier, Double> getNeighborsWithBandwidth() {
        return Collections.unmodifiableMap(neighbors);
    }

    private final Map<NodeIdentifier, Double> neighbors = new HashMap<>();
    private final Set<NodeIdentifier> apNeighbors = new HashSet<>();

    @Override
    @Nonnull
    public final Set<NodeIdentifier> getNeighbors() {
        return Collections.unmodifiableSet(neighbors.keySet());
    }

    /**
     * 
     * @return all neighbors that participate in AP sharing
     */
    public Set<NodeIdentifier> getApNeighbors() {
        return Collections.unmodifiableSet(apNeighbors);
    }

    /**
     * @return if this node has connected to all of it's neighbors for AP
     *         sharing
     */
    public boolean isApConnectedToAllNeighbors() {
        return accessNetworkManager().isConnectedToAllNeighbors();
    }

    /**
     * Add an AP neighbor by identifier. This neighbor will be contacted by AP.
     * When possible the method {link {@link #addNeighbor(NetworkNode, double)}
     * should be used. However under certain conditions only the
     * {@link NodeIdentifier} for the neighbor is available.
     * 
     * @param v
     *            the neighbor identifier
     * @param bandwidth
     *            the bandwidth to the neighbor
     */
    public final void addApNeighbor(@Nonnull final NodeIdentifier v, final double bandwidth) {
        apNeighbors.add(v);
        neighbors.put(v, bandwidth);

    }

    @Override
    public final void addNeighbor(@Nonnull final NetworkNode v, final double bandwidth) {
        if (v instanceof AbstractExecutionContext) {
            apNeighbors.add(v.getNodeIdentifier());
        }
        neighbors.put(v.getNodeIdentifier(), bandwidth);
    }

    /**
     * @param bandwidthLinkAttribute
     *            the {@link LinkAttribute} to associate with each bandwidth
     *            value
     * @return link capacity to neighbors in megabits per second
     * @see #addNeighbor(NodeIdentifier, double)
     * @see ResourceReport#getNetworkCapacity()
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>>
            getNeighborLinkCapacity(final LinkAttribute<?> bandwidthLinkAttribute) {
        ImmutableMap.Builder<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> builder = ImmutableMap.builder();
        neighbors.forEach((k, v) -> builder.put(k, ImmutableMap.of(bandwidthLinkAttribute, v)));
        return builder.build();
    }

    /**
     * Creates a {@link NetworkServer}.
     * 
     * @param program
     *            the program to run on the node
     * @param name
     *            the name of the node (must be unique)
     * @param nodeLookupService
     *            How to find other nodes
     * @param regionLookupService
     *            used by {@link #convertToSummary(ResourceReport)}
     * @param manager
     *            see {@link #getResourceManager()}
     * @param extraData
     *            data to help define extra attributes about the node
     */
    public NetworkServer(@Nonnull final NodeLookupService nodeLookupService,
            @Nonnull final RegionLookupService regionLookupService,
            @Nonnull final ProtelisProgram program,
            @Nonnull final NodeIdentifier name,
            @Nonnull final ResourceManager<? extends NetworkServer> manager,
            @Nonnull final Map<String, Object> extraData) {
        super(new SimpleExecutionEnvironment(), new NodeNetworkManager(nodeLookupService));
        this.uid = name;

        final String regionName = NetworkServerProperties.parseRegionName(extraData);
        if (null != regionName) {
            final StringRegionIdentifier region = new StringRegionIdentifier(regionName);
            this.region = region;
        } else {
            this.region = StringRegionIdentifier.UNKNOWN;
        }

        this.networkState = new NetworkState(this.region);
        this.regionNodeState = new RegionNodeState(this.region);
        this.regionServiceState = new RegionServiceState(this.region, ImmutableSet.of());

        this.resourceManager = manager;

        this.regionLookupService = regionLookupService;

        final Object pool = extraData.get(EXTRA_DATA_POOL);
        if (null != pool) {
            this.setPool(Boolean.parseBoolean(pool.toString()));
        }

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
        // make sure all neighbors are connected before running the program
        accessNetworkManager().updateNeighbors();
    }

    /**
     * Executed after {@link ProtelisVM#runCycle()}.
     */
    protected void postRunCycle() {
    }

    private Throwable programLoopException = null;

    /**
     * This is used for test cases. One can check this value to see if the
     * execute loop exited due to an exception.
     * 
     * @return the exception thrown in the program loop, null otherwise
     */
    public Throwable getExceptionThrownInProgramLoop() {
        return programLoopException;
    }

    /**
     * Execute the protolis program.
     */
    private void run() {
        while (running) {
            try {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("{}: Executing preRunCycle", getNodeIdentifier());
                }
                preRunCycle();
                if (!running) {
                    break;
                }

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("{}: runCycle", getNodeIdentifier());
                }
                getVM().runCycle(); // execute the Protelis program
                incrementExecutionCount();
                if (!running) {
                    break;
                }

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("{}: Executing postRunCycle", getNodeIdentifier());
                }
                postRunCycle();
                if (!running) {
                    break;
                }

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("{}: sleep", getNodeIdentifier());
                }
                Thread.sleep(sleepTime);
            } catch (final InterruptedException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Node " + getName() + " got interrupted, waking up to check if it's time to exit", e);
                }
            } catch (final Throwable e) {
                LOGGER.error("Exception thrown: terminating Protelis on node: " + getName(), e);
                programLoopException = e;
                break;
            }
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Exiting Protelis loop on node: " + getName());
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
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Before lock in stopExecuting on node: {}", getNodeIdentifier());
            }

            synchronized (lock) {
                running = false;
            }

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Executing preStopExecuting on node: {}", getNodeIdentifier());
            }
            preStopExecuting();

            // stop all network communication
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Stopping network manager on node: {}", getNodeIdentifier());
            }
            accessNetworkManager().stop();

            synchronized (lock) {
                if (null != executeThread) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Interrupting and then joining node: {}", getNodeIdentifier());
                    }
                    executeThread.interrupt();

                    try {
                        executeThread.join(); // may want to have a timeout here

                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("Node finished: {}", getNodeIdentifier());
                        }
                    } catch (final InterruptedException e) {
                        LOGGER.debug("Got interrupted waiting for join, probably just time to shutdown", e);
                    }
                    executeThread = null;
                } // non-null executeThread
            } // lock
        } // isExecuting
    }

    private final ResourceManager<?> resourceManager;

    /**
     * @return the resource manager for this node
     */
    @Nonnull
    public ResourceManager<?> getResourceManager() {
        return resourceManager;
    }

    /**
     * Get the latest resource report. This method should be called once per
     * cycle and then used to make decisions. This method may call directly to
     * the {@link ResourceManager} to provide gather the information.
     * 
     * @return the latest resource report
     * @param demandWindow
     *            what window size to use for estimating demand
     */
    @Nonnull
    public ResourceReport getResourceReport(@Nonnull final ResourceReport.EstimationWindow demandWindow) {
        return resourceManager.getCurrentResourceReport(demandWindow);
    }

    /**
     * Get the latest service report.
     * 
     * @return the lastest service report
     */
    @Nonnull
    public ServiceReport getServiceReport() {
        return resourceManager.getServiceReport();
    }

    private final RegionIdentifier region;

    @Override
    public RegionIdentifier getRegionIdentifier() {
        return this.region;
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

    // --- RegionServiceStateProvider
    private RegionServiceState regionServiceState;

    @Override
    @Nonnull
    public RegionServiceState getRegionServiceState() {
        synchronized (lock) {
            return regionServiceState;
        }
    }

    private void setRegionServiceState(final RegionServiceState state) {
        synchronized (lock) {
            regionServiceState = state;
        }
    }
    // --- end RegionServiceStateProvider

    @Override
    public String toString() {
        return getName();
    }

    /**
     * This method is used by AP to call
     * {@link RegionNodeState#setResourceReports(com.google.common.collect.ImmutableSet)}.
     * 
     * @param tuple
     *            the list of reports as a tuple
     */
    public void setRegionResourceReports(final Tuple tuple) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Setting region resource reports. Region: " + getRegionIdentifier());
        }

        final ImmutableSet.Builder<ResourceReport> builder = ImmutableSet.builder();
        for (final Object entry : tuple) {
            final ResourceReport report = (ResourceReport) entry;
            builder.add(report);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Adding report for " + report.getNodeName());
            }
        }
        getRegionNodeState().setResourceReports(builder.build());

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Finished setting region resource reports.");
        }
    }

    /**
     * This method is used by AP to call
     * {@link RegionServiceState#setServiceReports(ImmutableSet)}.
     * 
     * @param tuple
     *            the list of reports as a tuple
     */
    public void setRegionServiceReports(final Tuple tuple) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Setting region service reports. Region: " + getRegionIdentifier());
        }

        final ImmutableSet.Builder<ServiceReport> builder = ImmutableSet.builder();
        for (final Object entry : tuple) {
            final ServiceReport report = (ServiceReport) entry;
            builder.add(report);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Adding report for " + report.getNodeName());
            }
        }
        setRegionServiceState(new RegionServiceState(getRegionIdentifier(), builder.build()));

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Finished setting region service reports.");
        }
    }

    private final RegionLookupService regionLookupService;

    /**
     * Allow protelis to convert reports to summaries using the
     * {@link RegionLookupService} passed to this node.
     * 
     * @param report
     *            the report to convert
     * @return the return value from
     *         {@link ResourceSummary#convertToSummary(ResourceReport, RegionLookupService)}
     */
    public ResourceSummary convertToSummary(@Nonnull final ResourceReport report) {
        return ResourceSummary.convertToSummary(report, regionLookupService);
    }

}
