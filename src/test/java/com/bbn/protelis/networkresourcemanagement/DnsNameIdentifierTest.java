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
