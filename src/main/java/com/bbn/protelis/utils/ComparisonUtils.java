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
package com.bbn.protelis.utils;

import java.util.Map;
import java.util.Objects;

import com.bbn.protelis.networkresourcemanagement.LinkAttribute;
import com.bbn.protelis.networkresourcemanagement.NodeAttribute;

/**
 * Some utilities for doing comparisons.
 * 
 * @author jschewe
 *
 */
public final class ComparisonUtils {

    /**
     * Weights are considered equal when within this tolerance.
     */
    public static final double WEIGHT_COMPARISON_TOLERANCE = 1E-3;

    /**
     * {@link NodeAttribute} values are considered equal when within this
     * tolerance.
     */
    public static final double NODE_ATTRIBUTE_COMPARISON_TOLERANCE = 1E-3;

    /**
     * {@link LinkAttribute} values are considered equal when within this
     * tolerance.
     */
    public static final double LINK_ATTRIBUTE_COMPARISON_TOLERANCE = 1E-3;

    private ComparisonUtils() {
    }

    /**
     * Compare 2 maps with a tolerance. The keys are compared with
     * {@link Objects#equals(Object, Object)} and the values are checked based
     * on the tolerance.
     * 
     * @param one
     *            the first map to compare
     * @param two
     *            the second map to compare
     * @param tolerance
     *            the tolerance for equality of the values
     * @return if the maps are equivalent
     * @param <T>
     *            type of the keys
     */
    public static <T> boolean doubleMapEquals(final Map<T, Double> one,
            final Map<T, Double> two,
            final double tolerance) {
        if (one == two) {
            return true;
        } else if (null == one || null == two) {
            // one of the maps is null and the other is not
            return false;
        } else {
            if (one.size() != two.size()) {
                return false;
            } else {
                return one.entrySet().stream() //
                        .allMatch(entry -> {
                            final T oneKey = entry.getKey();
                            final Double oneValue = entry.getValue();
                            if (!two.containsKey(oneKey)) {
                                return false;
                            } else {
                                final Double twoValue = two.get(oneKey);
                                if (null == oneValue && null == twoValue) {
                                    return true;
                                } else if (null == oneValue || null == twoValue) {
                                    return false;
                                } else {
                                    return Math.abs(oneValue - twoValue) < tolerance;
                                }
                            }
                        });
            } // equal size
        } // not null
    }

    /**
     * Compare maps that are 2 levels deep where the second map uses a double
     * for a value.
     * 
     * @param mapOne
     *            the first map
     * @param mapTwo
     *            the second map
     * @param tolerance
     *            tolerance for the double values
     * @return if they are equivalent
     * @param <K1>
     *            key of first level map
     * @param <K2>
     *            key of second level map
     * @param <M1>
     *            supports subclasses of {@link Map} for the inner map
     */
    public static <K1, K2, M1 extends Map<K2, Double>> boolean doubleMapEquals2(final Map<K1, M1> mapOne,
            final Map<K1, M1> mapTwo,
            final double tolerance) {
        if (mapOne == mapTwo) {
            return true;
        } else if (null == mapOne || null == mapTwo) {
            return false;
        } else {
            if (mapOne.size() != mapTwo.size()) {
                return false;
            } else {
                return mapOne.entrySet().stream() //
                        .allMatch(entry -> {
                            final K1 oneKey = entry.getKey();
                            if (!mapTwo.containsKey(oneKey)) {
                                return false;
                            } else {
                                final Map<K2, Double> oneValue = entry.getValue();
                                final Map<K2, Double> twoValue = mapTwo.get(oneKey);
                                if (oneValue == twoValue) {
                                    return true;
                                } else if (null == oneValue || null == twoValue) {
                                    return false;
                                } else {
                                    return doubleMapEquals(oneValue, twoValue, tolerance);
                                }
                            }
                        });
            } // equal size
        } // no null
    }

    /**
     * Compare maps that are 3 levels deep where the last map uses a double for
     * a value.
     * 
     * @param mapOne
     *            the first map
     * @param mapTwo
     *            the second map
     * @param tolerance
     *            tolerance for the double values
     * @return if they are equivalent
     * @param <K1>
     *            key of first level map
     * @param <K2>
     *            key of second level map
     * @param <K3>
     *            the key of the third level map
     * @param <M1>
     *            supports subclasses of {@link Map} for the second level map
     * @param <M2>
     *            supports subclasses of {@link Map} for the third level map
     */
    public static <K1, K2, K3, M1 extends Map<K2, M2>, M2 extends Map<K3, Double>>
            boolean
            doubleMapEquals3(final Map<K1, M1> mapOne, final Map<K1, M1> mapTwo, final double tolerance) {
        if (mapOne == mapTwo) {
            return true;
        } else if (null == mapOne || null == mapTwo) {
            return false;
        } else {
            if (mapOne.size() != mapTwo.size()) {
                return false;
            } else {
                return mapOne.entrySet().stream() //
                        .allMatch(entry -> {
                            final K1 oneKey = entry.getKey();
                            if (!mapTwo.containsKey(oneKey)) {
                                return false;
                            } else {
                                final M1 oneValue = entry.getValue();
                                final M1 twoValue = mapTwo.get(oneKey);
                                return doubleMapEquals2(oneValue, twoValue, tolerance);
                            }
                        });
            } // equal size
        } // no null
    }

    /**
     * Compare maps that are 4 levels deep where the last map uses a double for
     * a value.
     * 
     * @param mapOne
     *            the first map
     * @param mapTwo
     *            the second map
     * @param tolerance
     *            tolerance for the double values
     * @return if they are equivalent
     * @param <K1>
     *            key of first level map
     * @param <K2>
     *            key of second level map
     * @param <K3>
     *            the key of the third level map
     * @param <K4>
     *            the key of the fourth level map
     * @param <M1>
     *            supports subclasses of {@link Map} for the second level map
     * @param <M2>
     *            supports subclasses of {@link Map} for the third level map
     * @param <M3>
     *            supports subclasses of {@link Map} for the fourth level map
     */
    public static <K1, K2, K3, K4, M1 extends Map<K2, M2>, M2 extends Map<K3, M3>, M3 extends Map<K4, Double>>
            boolean
            doubleMapEquals4(final Map<K1, M1> mapOne, final Map<K1, M1> mapTwo, final double tolerance) {
        if (mapOne == mapTwo) {
            return true;
        } else if (null == mapOne || null == mapTwo) {
            return false;
        } else {
            if (mapOne.size() != mapTwo.size()) {
                return false;
            } else {
                return mapOne.entrySet().stream() //
                        .allMatch(entry -> {
                            final K1 oneKey = entry.getKey();
                            if (!mapTwo.containsKey(oneKey)) {
                                return false;
                            } else {
                                final M1 oneValue = entry.getValue();
                                final M1 twoValue = mapTwo.get(oneKey);
                                return doubleMapEquals3(oneValue, twoValue, tolerance);
                            }
                        });
            } // equal size
        } // no null
    }

}
