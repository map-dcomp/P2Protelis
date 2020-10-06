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
/**
 * This package contains the basis for a network resource management system
 * using Protelis.
 * 
 * <h1>Interpreting network flow</h1>
 * 
 * Interpreting network flow information with RX and TX values requires that one
 * knows the perspective that these values are with respect to. This document
 * explains how these values should be interpreted with examples to make it
 * clear.
 * 
 * The RX/TX values are from the perspective of
 * {@link com.bbn.protelis.networkresourcemanagement.NodeNetworkFlow#getSource()}.
 * 
 * If we have a node network flow 'f'
 * 
 * <pre>
 * NodeNetworkFlow
 *   nodeOne: A0
 *   nodeTwo: A1
 * </pre>
 * 
 * And a resource report showing network information as follows
 * 
 * <pre>
 *   ResourceReport.networkLoad
 *     f -> { DATARATE_RX=2, DATARATE_TX=6}
 * </pre>
 * 
 * This means that node A0 is sending 6Mbps to node A1 and receiving 2Mbps from
 * node A1.
 * 
 * The same interpretation applies to
 * {@link com.bbn.protelis.networkresourcemanagement.RegionNetworkFlow}. The
 * values are from the perspective of
 * {@link com.bbn.protelis.networkresourcemanagement.RegionNetworkFlow#getServer()}.
 * 
 * 
 * The flow objects also have a "server" property. This specifies which
 * node/region is the server. This may not be the same as node two due to how
 * information is collected in the high fidelity environment.
 * 
 * 
 * When the network information is combined with the neighbor that it is
 * connected to one can gather some topological information. If we have the
 * topology "A - B - C - D - E" and node C sees the following report:
 * 
 * <pre>
 * {
*   neighbor: B
*   flow: E -> A server: E,
*   TX=1,
*   RX=65
* }
 * </pre>
 * 
 * This states that E is transmitting 1 through B to A. Based in this
 * information we know that to get to A from C we go through B.
 */
package com.bbn.protelis.networkresourcemanagement;
