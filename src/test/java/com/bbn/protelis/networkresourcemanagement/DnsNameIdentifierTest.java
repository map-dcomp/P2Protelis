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

import org.junit.Assert;
import org.junit.Test;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Some tests for {@link DnsNameIdentifier}.
 * 
 * @author jschewe
 *
 */
public class DnsNameIdentifierTest {

    /**
     * Ensure that equality is properly defined to compare the names. This
     * checks that when using 2 string names that are equal, but not the same
     * reference produce {@link DnsNameIdentifier} objects that are equal.
     */
    @Test
    @SuppressFBWarnings(value = "DM_STRING_CTOR", justification = "Intentionally want 2 distinct String objects with the same value")
    public void testEquals() {
        final String expectedName = new String("one");
        final String compareName = new String("one");

        Assert.assertEquals(expectedName, compareName);

        // make sure that we really have 2 different string objects
        Assert.assertFalse(expectedName == compareName);

        final DnsNameIdentifier expected = new DnsNameIdentifier(expectedName);
        final DnsNameIdentifier compare = new DnsNameIdentifier(compareName);

        Assert.assertEquals(expected, compare);
    }

    /**
     * Test that 2 names that have different case are equal.
     */
    @Test
    public void testEqualsIgnoreCase() {
        final String expectedName = "one";
        final String compareName = "One";

        final DnsNameIdentifier expected = new DnsNameIdentifier(expectedName);
        final DnsNameIdentifier compare = new DnsNameIdentifier(compareName);

        Assert.assertEquals(expected, compare);
    }

}
