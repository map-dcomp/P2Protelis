/*BBN_LICENSE_START -- DO NOT MODIFY BETWEEN LICENSE_{START,END} Lines
Copyright (c) <2017,2018,2019,2020,2021>, <Raytheon BBN Technologies>
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

import org.protelis.vm.CodePathFactory;
import org.protelis.vm.ExecutionEnvironment;
import org.protelis.vm.NetworkManager;
import org.protelis.vm.impl.AbstractExecutionContext;

/**
 * Execution context for Protelis.
 */
public class ExecutionContext extends AbstractExecutionContext<ExecutionContext> {
    private final NetworkServer device;
    private final NetworkManager networkManager;
    private final CodePathFactory codePathFactory;

    /**
     * Create a child context.
     * 
     * @param device
     *            see {@link #getDevice()}
     * @param networkManager
     *            passed to parent constructor
     * @param codePathFactory
     *            passed to parent constructor
     * @param environment
     *            passed to parent constructor
     */
    public ExecutionContext(final NetworkServer device,
            final ExecutionEnvironment environment,
            final NetworkManager networkManager,
            final CodePathFactory codePathFactory) {
        super(environment, networkManager, codePathFactory);
        this.device = device;
        this.networkManager = networkManager;
        this.codePathFactory = codePathFactory;
    }

    @Override
    public Number getCurrentTime() {
        return System.currentTimeMillis();
    }

    @Override
    public NodeIdentifier getDeviceUID() {
        return device.getNodeIdentifier();
    }

    @Override
    public double nextRandomDouble() {
        return Math.random();
    }

    @Override
    protected ExecutionContext instance() {
        return new ExecutionContext(device, getExecutionEnvironment(), networkManager, codePathFactory);
    }

    /**
     * @return the device that Protelis is working with
     */
    public NetworkServer getDevice() {
        return device;
    }
}
