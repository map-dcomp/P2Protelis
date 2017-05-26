package com.bbn.protelis.networkresourcemanagement;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import java8.util.Objects;

/**
 * Simple implementation of resource summarization.
 * 
 * Numbers are added together. Null combined with anything yields the other
 * thing. Everything else is turned into a string with "_" between the two
 * objects.
 */
public class BasicResourceSummarizer implements ResourceSummarizer {

    @Override
    @Nonnull
    public ResourceSummary merge(final ResourceSummary one, final ResourceSummary two) {
        final Map<String, Object> oneState = one.getState();
        final Map<String, Object> twoState = two.getState();

        final Map<String, Object> newState = merge(oneState, twoState);
        return new ResourceSummary(newState);
    }

    @Override
    @Nonnull
    public ResourceSummary merge(final ResourceSummary summary, final ResourceReport report) {
        final Map<String, Object> oneState = summary.getState();
        final Map<String, Object> twoState = report.getState();

        final Map<String, Object> newState = merge(oneState, twoState);
        return new ResourceSummary(newState);
    }

    @Override
    @Nonnull
    public ResourceSummary nullSummary() {
        return new ResourceSummary();
    }

    private Map<String, Object> merge(final Map<String, Object> oneState, final Map<String, Object> twoState) {
        final Map<String, Object> newState = new HashMap<>();
        for (final Map.Entry<String, Object> entry : oneState.entrySet()) {
            if (twoState.containsKey(entry.getKey())) {
                final Object twoValue = twoState.get(entry.getKey());
                final Object newValue = merge(entry.getKey(), entry.getValue(), twoValue);
                newState.put(entry.getKey(), newValue);
            } else {
                newState.put(entry.getKey(), entry.getValue());
            }
        }

        for (final Map.Entry<String, Object> entry : twoState.entrySet()) {
            if (!oneState.containsKey(entry.getKey())) {
                newState.put(entry.getKey(), entry.getValue());
            }
        }

        return newState;
    }

    private Object merge(final String key, final Object oneValue, final Object twoValue) {
        if (null == oneValue) {
            return twoValue;
        } else if (null == twoValue) {
            return oneValue;
        } else if (oneValue instanceof Number && twoValue instanceof Number) {
            return ((Number) oneValue).doubleValue() + ((Number) twoValue).doubleValue();
        } else {
            return Objects.toString(oneValue) + "_" + Objects.toString(twoValue);
        }
    }
}
