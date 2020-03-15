package org.terekhpp.optimaize.counter;

import com.google.common.collect.ImmutableList;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Counter for Optimaize-GmbH test issue.
 */
public interface OptimaizeCounter {

    /**
     * Add elements to statistic.
     * Method is threadsafe.
     *
     * @param nums Iterable of integers.
     */
    void push(ImmutableList<Integer> nums);

    /**
     * Smallest number ever.
     *
     * @return Smallest number.
     */
    Optional<Integer> getSmallest();

    /**
     * Largest number ever.
     *
     * @return Largest number.
     */
    Optional<Integer> getLargest();

    /**
     * Average number of all numbers ever.
     *
     * @return Avg.
     */
    Optional<BigDecimal> getAverage();
}
