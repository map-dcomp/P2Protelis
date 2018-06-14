package com.bbn.protelis.utils;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * Some utilities for working with immutable collections.
 * 
 * @author jschewe
 *
 */
public final class ImmutableUtils {

    private ImmutableUtils() {
    }

    /**
     * Make an immutable copy of a 2 level map.
     * 
     * @param source
     *            the map to copy
     * @return the immutable copy
     * @param <K1>
     *            key of the first level
     * @param <K2>
     *            the key of the second level
     * @param <V>
     *            the value of the second level
     */
    public static <K1, K2, V>
            ImmutableMap<K1, ImmutableMap<K2, V>>
            makeImmutableMap2(final Map<K1, Map<K2, V>> source) {
        // The final value is not generic to prevent one from using a mutable
        // type as the value type
        final ImmutableMap.Builder<K1, ImmutableMap<K2, V>> immutable = ImmutableMap.builder();
        source.forEach((k1, v1) -> {
            final ImmutableMap<K2, V> immutableV1 = ImmutableMap.copyOf(v1);
            immutable.put(k1, immutableV1);
        });
        return immutable.build();
    }

    /**
     * Make an immutable copy of a 3 level map.
     * 
     * @param source
     *            the map to copy
     * @return the immutable copy
     * @param <K1>
     *            the key of the first level
     * @param <K2>
     *            the key of the second level
     * @param <K3>
     *            the key of the third level
     * @param <V>
     *            the value of the third level
     */
    public static <K1, K2, K3, V> ImmutableMap<K1, ImmutableMap<K2, ImmutableMap<K3, V>>> makeImmutableMap3(
            final Map<K1, Map<K2, Map<K3, V>>> source) {
        // The final value is not generic to prevent one from using a mutable
        // type as the value type
        final ImmutableMap.Builder<K1, ImmutableMap<K2, ImmutableMap<K3, V>>> immutable = ImmutableMap.builder();
        source.forEach((k1, v1) -> {
            final ImmutableMap.Builder<K2, ImmutableMap<K3, V>> immutableV1 = ImmutableMap.builder();
            v1.forEach((k2, v2) -> {
                final ImmutableMap<K3, V> immutableV2 = ImmutableMap.copyOf(v2);
                immutableV1.put(k2, immutableV2);
            });
            immutable.put(k1, immutableV1.build());
        });
        return immutable.build();
    }

    /**
     * Make an immutable copy of a 4 level map.
     * 
     * @param source
     *            the map to copy
     * @return the immutable copy
     * @param <K1>
     *            the key of the first level
     * @param <K2>
     *            the key of the second level
     * @param <K3>
     *            the key of the third level
     * @param <K4>
     *            the key of the fourth level
     * @param <V>
     *            the value of the fourth level
     */
    public static <K1, K2, K3, K4, V>
            ImmutableMap<K1, ImmutableMap<K2, ImmutableMap<K3, ImmutableMap<K4, V>>>>
            makeImmutableMap4(final Map<K1, Map<K2, Map<K3, Map<K4, V>>>> source) {
        // The final value is not generic to prevent one from using a mutable
        // type as the value type
        final ImmutableMap.Builder<K1, ImmutableMap<K2, ImmutableMap<K3, ImmutableMap<K4, V>>>> immutable = ImmutableMap
                .builder();

        source.forEach((k1, v1) -> {
            final ImmutableMap.Builder<K2, ImmutableMap<K3, ImmutableMap<K4, V>>> immutableV1 = ImmutableMap.builder();

            v1.forEach((k2, v2) -> {
                final ImmutableMap.Builder<K3, ImmutableMap<K4, V>> immutableV2 = ImmutableMap.builder();

                v2.forEach((k3, v3) -> {

                    final ImmutableMap<K4, V> immutableV3 = ImmutableMap.copyOf(v3);
                    immutableV2.put(k3, immutableV3);

                });

                immutableV1.put(k2, immutableV2.build());

            });
            immutable.put(k1, immutableV1.build());
        });
        return immutable.build();
    }
}
