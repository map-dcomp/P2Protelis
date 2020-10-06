/*BBN_LICENSE_START -- DO NOT MODIFY BETWEEN LICENSE_{START,END} Lines
Copyright (c) <2017,2018,2019,2020>, <Raytheon BBN Technologies>
To be applied to the DCOMP/MAP Public Source Code Release dated 2018-04-19, with
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;

import org.protelis.lang.datatype.Tuple;
import org.protelis.vm.CodePathFactory;
import org.protelis.vm.ExecutionEnvironment;
import org.protelis.vm.ProtelisProgram;
import org.protelis.vm.ProtelisVM;
import org.protelis.vm.impl.AbstractExecutionContext;
import org.protelis.vm.impl.HashingCodePathFactory;
import org.protelis.vm.impl.SimpleExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.hash.Hashing;

/**
 * A server in the network.
 * 
 * This class is thread-safe with the exception of access to Protelis data in
 * {@link AbstractExecutionContext}
 */
public class NetworkServer
        implements NetworkStateProvider, RegionNodeStateProvider, RegionServiceStateProvider, NetworkNode {

    private final Object lock = new Object();

    private final Logger logger;

    /** Device numerical identifier */
    private final NodeIdentifier uid;

    /** The Protelis VM to be executed by the device */
    private final ProtelisVM vm;

    private final ExecutionEnvironment environment;

    /**
     * 
     * @return the environment used by the VM
     */
    public ExecutionEnvironment getEnvironment() {
        return environment;
    }

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
        synchronized (lock) {
            pool = v;
        }
    }

    /**
     * 
     * @return true if this Node is a pool of resources
     * @see #EXTRA_DATA_POOL
     */
    public boolean isPool() {
        synchronized (lock) {
            return pool;
        }
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
        synchronized (lock) {
            return sleepTime;
        }
    }

    /**
     * @param v
     *            Specify the sleep time
     * @see #getSleepTime()
     */
    public final void setSleepTime(final long v) {
        synchronized (lock) {
            sleepTime = v;
        }
    }

    /**
     * The neighboring nodes with the bandwidth to the neighbor.
     * 
     * @return Key is the neighbor, value is the datarate in megabits per
     *         second.
     * @see #addNeighbor(NodeIdentifier, double)
     */
    public Map<NodeIdentifier, Double> getNeighborsWithBandwidth() {
        synchronized (lock) {
            return new HashMap<>(neighbors);
        }
    }

    private final Map<NodeIdentifier, Double> neighbors = new HashMap<>();
    private final Set<NodeIdentifier> apNeighbors = new HashSet<>();

    @Override
    @Nonnull
    public final Set<NodeIdentifier> getNeighbors() {
        synchronized (lock) {
            return new HashSet<>(neighbors.keySet());
        }
    }

    /**
     * 
     * @return all neighbors that participate in AP sharing
     */
    public Set<NodeIdentifier> getApNeighbors() {
        synchronized (lock) {
            return new HashSet<>(apNeighbors);
        }
    }

    /**
     * @return if this node has connected to all of it's neighbors for AP
     *         sharing
     * @see NodeNetworkManager#isConnectedToAllNeighbors()
     */
    public boolean isApConnectedToAllNeighbors() {
        return accessNetworkManager().isConnectedToAllNeighbors();
    }

    /**
     * 
     * @return the set of neighbors that are currently connected to AP
     * @see NodeNetworkManager#getConnectedNeighbors()
     */
    public Set<NodeIdentifier> getConnectedNeighbors() {
        return accessNetworkManager().getConnectedNeighbors();
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
        synchronized (lock) {
            apNeighbors.add(v);
            neighbors.put(v, bandwidth);
        }
    }

    @Override
    public final void addNeighbor(@Nonnull final NetworkNode v, final double bandwidth) {
        synchronized (lock) {
            // all NetworkServer instances are involved in AP
            if (v instanceof NetworkServer) {
                apNeighbors.add(v.getNodeIdentifier());
            }
            neighbors.put(v.getNodeIdentifier(), bandwidth);
        }
    }

    /**
     * Public visibility for testing only.
     * 
     * @param bandwidthLinkAttribute
     *            the {@link LinkAttribute} to associate with each bandwidth
     *            value
     * @return link capacity to neighbors in megabits per second
     * @see #addNeighbor(NodeIdentifier, double)
     * @see ResourceReport#getNetworkCapacity()
     */
    @Nonnull
    public ImmutableMap<InterfaceIdentifier, ImmutableMap<LinkAttribute, Double>>
            getNeighborLinkCapacity(final LinkAttribute bandwidthLinkAttribute) {
        final ImmutableMap.Builder<InterfaceIdentifier, ImmutableMap<LinkAttribute, Double>> builder = ImmutableMap
                .builder();
        synchronized (lock) {
            neighbors.forEach((k, v) -> {
                builder.put(BasicResourceManager.createInterfaceIdentifierForNeighbor(k),
                        ImmutableMap.of(bandwidthLinkAttribute, v));
            });
        }
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
        this.uid = name;
        logger = LoggerFactory.getLogger(NetworkServer.class.getName() + "." + name);

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
        networkManager = new NodeNetworkManager(nodeLookupService);
        environment = new SimpleExecutionEnvironment();
        // final CodePathFactory codePathFactory = (stack, sizes) -> new
        // DefaultTimeEfficientCodePath(stack);
        final CodePathFactory codePathFactory = new HashingCodePathFactory(Hashing.murmur3_128());
        vm = new ProtelisVM(program, new ExecutionContext(this, environment, networkManager, codePathFactory));
    }

    /**
     * @return Accessor for virtual machine, to allow external execution
     *         triggering
     */
    public final ProtelisVM getVM() {
        return vm;
    }

    private final NodeNetworkManager networkManager;

    /**
     * Expose the network manager. This is to allow external simulation of
     * network For real devices, the NetworkManager usually runs autonomously in
     * its own thread(s).
     * 
     * @return the node specific version of the network manager
     */
    public final NodeNetworkManager accessNetworkManager() {
        return networkManager;
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
        while (running.get()) {
            try {
                logger.debug("Executing preRunCycle");
                preRunCycle();
                if (!running.get()) {
                    break;
                }

                logger.debug("runCycle");
                getVM().runCycle(); // execute the Protelis program
                incrementExecutionCount();
                if (!running.get()) {
                    break;
                }

                logger.debug("Executing postRunCycle");
                postRunCycle();
                if (!running.get()) {
                    break;
                }

                logger.debug("sleep");
                Thread.sleep(sleepTime);
            } catch (final InterruptedException e) {
                logger.debug("Node " + getName() + " got interrupted, waking up to check if it's time to exit", e);
            } catch (final Throwable e) {
                logger.error("Exception thrown: terminating Protelis on node: " + getName(), e);
                programLoopException = e;
                break;
            }
        }
        logger.info("Exiting Protelis loop on node: {}", getName());
    }

    private final Object executeThreadLock = new Object();

    private Thread executeThread = null;

    /**
     * 
     * @return is the node currently executing?
     */
    public final boolean isExecuting() {
        synchronized (executeThreadLock) {
            return running.get() && null != executeThread && executeThread.isAlive();
        }
    }

    private AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Start the node executing.
     */
    public final void startExecuting() {
        synchronized (executeThreadLock) {
            if (running.get()) {
                throw new IllegalStateException("Already executing, cannot start again!");
            }

            running.set(true);

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
            if (logger.isTraceEnabled()) {
                logger.trace("Before lock in stopExecuting on node: {}", getNodeIdentifier());
            }

            running.set(false);

            if (logger.isTraceEnabled()) {
                logger.trace("Executing preStopExecuting on node: {}", getNodeIdentifier());
            }
            preStopExecuting();

            // stop all network communication
            if (logger.isTraceEnabled()) {
                logger.trace("Stopping network manager on node: {}", getNodeIdentifier());
            }
            accessNetworkManager().stop();

            // store the reference so that we don't hold the lock longer
            // than needed
            Thread executeThreadTemp;
            synchronized (executeThreadLock) {
                executeThreadTemp = executeThread;
                executeThread = null;
            } // lock

            if (null != executeThreadTemp) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Interrupting and then joining node: {}", getNodeIdentifier());
                }
                executeThreadTemp.interrupt();

                try {
                    executeThreadTemp.join(); // may want to have a timeout here

                    if (logger.isTraceEnabled()) {
                        logger.trace("Node finished: {}", getNodeIdentifier());
                    }
                } catch (final InterruptedException e) {
                    logger.debug("Got interrupted waiting for join, probably just time to shutdown", e);
                }
            } // non-null executeThread
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
        synchronized (lock) {
            return hardware;
        }
    }

    @Override
    public void setHardware(final String hardware) {
        synchronized (lock) {
            this.hardware = hardware;
        }
    }

    // ---- NetworkStateProvider
    private final NetworkState networkState;

    @Override
    @Nonnull
    public NetworkState getNetworkState() {
        return networkState;
    }
    // ---- end NetworkStateProvider

    // ---- RegionNodeStateProvider
    private final RegionNodeState regionNodeState;

    @Override
    @Nonnull
    public RegionNodeState getRegionNodeState() {
        return regionNodeState;
    }
    // ---- end RegionNodeStateProvider

    // --- RegionServiceStateProvider
    // separate from the main lock so that we don't hang up stopping
    private final Object regionServiceStateLock = new Object();
    private RegionServiceState regionServiceState;

    @Override
    @Nonnull
    public RegionServiceState getRegionServiceState() {
        synchronized (regionServiceStateLock) {
            return regionServiceState;
        }
    }

    private void setRegionServiceState(final RegionServiceState state) {
        synchronized (regionServiceStateLock) {
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
        final long time = getResourceManager().getClock().getCurrentTime();

        if (logger.isTraceEnabled()) {
            logger.trace("Setting region resource reports. Region: " + getRegionIdentifier());
        }

        final ImmutableSet.Builder<ResourceReport> builder = ImmutableSet.builder();
        for (final Object entry : tuple) {
            final ResourceReport report = (ResourceReport) entry;
            final long propagationDelay = time - report.getTimestamp();

            logger.debug(
                    "Received ResourceReport with timestamp {} from {} at time {} after a propagation delay of {} ms.",
                    report.getTimestamp(), report.getNodeName(), time, propagationDelay);

            builder.add(report);
            if (logger.isTraceEnabled()) {
                logger.trace("Adding report for " + report.getNodeName());
            }
        }
        getRegionNodeState().setResourceReports(builder.build());

        if (logger.isTraceEnabled()) {
            logger.trace("Finished setting region resource reports.");
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
        final long time = getResourceManager().getClock().getCurrentTime();

        if (logger.isTraceEnabled()) {
            logger.trace("Setting region service reports. Region: " + getRegionIdentifier());
        }

        final ImmutableSet.Builder<ServiceReport> builder = ImmutableSet.builder();
        for (final Object entry : tuple) {
            final ServiceReport report = (ServiceReport) entry;
            final long propagationDelay = time - report.getTimestamp();

            logger.debug(
                    "Received ServiceReport with timestamp {} from {} at time {} after a propagation delay of {} ms.",
                    report.getTimestamp(), report.getNodeName(), time, propagationDelay);

            builder.add(report);
            if (logger.isTraceEnabled()) {
                logger.trace("Adding report for " + report.getNodeName());
            }
        }
        setRegionServiceState(new RegionServiceState(getRegionIdentifier(), builder.build()));

        if (logger.isTraceEnabled()) {
            logger.trace("Finished setting region service reports.");
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
