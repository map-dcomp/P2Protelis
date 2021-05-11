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

import javax.annotation.Nonnull;

/**
 * Identifier that represents a DNS name. The name is compared in a
 * case-insensitive manner.
 */
public class DnsNameIdentifier implements NodeIdentifier, Comparable<DnsNameIdentifier> {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String nameLower;

    /**
     * 
     * @param name
     *            the name for the node
     */
    public DnsNameIdentifier(@Nonnull final String name) {
        this.name = name;
        this.nameLower = this.name.toLowerCase();
        this.hashCode = this.nameLower.hashCode();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(final Object alt) {
        if (null == alt) {
            return false;
        } else if (this == alt) {
            return true;
        } else if (getClass().equals(alt.getClass())) {
            final DnsNameIdentifier other = (DnsNameIdentifier) alt;
            if (other.hashCode() != this.hashCode()) {
                return false;
            } else {
                return this.nameLower.equals(other.nameLower);
            }
        } else {
            return false;
        }
    }

    private final int hashCode;

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(final DnsNameIdentifier other) {
        return this.nameLower.compareTo(other.nameLower);
    }
}
