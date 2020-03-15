package org.terekhpp.optimaize.counter.impl;

import com.google.common.collect.ImmutableList;
import org.terekhpp.optimaize.counter.OptimaizeCounter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@inheritDoc}
 */
public class OptimaizeCounterImpl implements OptimaizeCounter {

    private final AtomicReference<Integer> LARGEST = new AtomicReference<>();
    private final AtomicReference<Integer> SMALLEST = new AtomicReference<>();
    private final AtomicReference<AvearageContainer> AVERAGE = new AtomicReference<>();
    private final int DEFAULT_SCALE = 2;
    private final RoundingMode DEFAULT_ROUNDING_MOD = RoundingMode.HALF_UP;

    /**
     * {@inheritDoc}
     */
    public void push(final ImmutableList<Integer> nums) {
        if (nums == null) {
            return;
        }
        for (Integer num : nums) {
            if (num == null) {
                continue;
            }
            compareAndSetLarges(num);
            compareAndSetSmallest(num);
            calcAvg(num);
        }
    }

    private void compareAndSetLarges(final Integer num) {
        Integer prev;
        do {
            prev = LARGEST.get();
        } while ((prev == null || num > prev) && !LARGEST.compareAndSet(prev, num));
    }

    private void compareAndSetSmallest(final Integer num) {
        Integer prev;
        do {
            prev = SMALLEST.get();
        } while ((prev == null || num < prev) && !SMALLEST.compareAndSet(prev, num));
    }

    private void calcAvg(final Integer num) {
        AvearageContainer prevAvgContainer;
        AvearageContainer newAvgContainer;
        do {
            prevAvgContainer = AVERAGE.get();
            newAvgContainer = new AvearageContainer();
            if (prevAvgContainer == null || prevAvgContainer.numbersCount == null || prevAvgContainer.sum == null) {
                newAvgContainer.numbersCount = BigInteger.ONE;
                newAvgContainer.sum = BigInteger.valueOf(num);
            } else {
                newAvgContainer.numbersCount = prevAvgContainer.numbersCount.add(BigInteger.ONE);
                newAvgContainer.sum = prevAvgContainer.sum.add(BigInteger.valueOf(num));
            }
        }
        while (!AVERAGE.compareAndSet(prevAvgContainer, newAvgContainer));
    }

    /**
     * {@inheritDoc}
     */
    public Optional<Integer> getSmallest() {
        final Integer val = SMALLEST.get();
        return val != null ? Optional.of(val) : Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    public Optional<Integer> getLargest() {
        final Integer val = LARGEST.get();
        return val != null ? Optional.of(val) : Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    public Optional<BigDecimal> getAverage() {
        AvearageContainer container = AVERAGE.get();
        if (container == null || container.sum == null || container.numbersCount == null) {
            return Optional.empty();
        }
        final BigDecimal res = new BigDecimal(container.sum)
                .divide(new BigDecimal(container.numbersCount), DEFAULT_SCALE, DEFAULT_ROUNDING_MOD);
        return Optional.of(res);
    }

    /**
     * Data class to store data for average calculation.
     * Also this class was created to simplify CAS logic.
     */
    private class AvearageContainer {
        BigInteger numbersCount;
        BigInteger sum;
    }
}
