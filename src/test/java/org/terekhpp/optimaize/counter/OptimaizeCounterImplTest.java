package org.terekhpp.optimaize.counter;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.terekhpp.optimaize.counter.impl.OptimaizeCounterImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OptimaizeCounterImplTest {

    @Test
    public void oneThreadPositiveNumbersTest() {
        List<Integer> vals = new ArrayList<>();
        int max = 100000;
        for (int i = 0; i <= max; i++) {
            vals.add(i);
        }

        int count = max + 1;
        BigDecimal sum = new BigDecimal(0);
        for (int val : vals) {
            sum = sum.add(new BigDecimal(val));
        }

        BigDecimal avg = sum.divide(new BigDecimal(count));

        OptimaizeCounter counter = new OptimaizeCounterImpl();
        counter.push(ImmutableList.<Integer>builder().addAll(vals).build());
        //Hmmm
        counter.push(null);

        assertEquals(max, counter.getLargest().orElse(null));
        assertEquals(0, counter.getSmallest().orElse(null));
        assertEquals(avg, counter.getAverage().orElse(null).setScale(avg.scale()));
    }

    @Test
    public void oneThreadPositiveAndNegativeNumbersTest() {
        List<Integer> vals = new ArrayList<>();
        int max = 100601;
        int min = -777;
        for (int i = min; i <= max; i++) {
            vals.add(i);
        }

        int count = max - min + 1;
        BigDecimal sum = new BigDecimal(0);
        for (int val : vals) {
            sum = sum.add(new BigDecimal(val));
        }

        BigDecimal avg = sum.divide(new BigDecimal(count));

        OptimaizeCounter counter = new OptimaizeCounterImpl();
        counter.push(ImmutableList.<Integer>builder().addAll(vals).build());
        assertEquals(max, counter.getLargest().orElse(null));
        assertEquals(min, counter.getSmallest().orElse(null));
        assertEquals(avg, counter.getAverage().orElse(null).setScale(avg.scale()));
    }

    @Test
    public void oneThreadSeveralBatchTest() {
        List<Integer> firstBatch = new ArrayList<>();
        List<Integer> secondBatch = new ArrayList<>();
        List<Integer> thirdBatch = new ArrayList<>();
        List<Integer> fourthBatch = new ArrayList<>();
        int max = 100601;
        int min = -777;
        Integer realMin = null;
        Integer realMax = null;
        BigDecimal sum = new BigDecimal(0);
        int count = 0;

        for (int i = min; i <= max; i++) {
            if (insertByMod(firstBatch, i, 2) || insertByMod(firstBatch, i, 3)
                    || insertByMod(firstBatch, i, 5) || insertByMod(firstBatch, i, 7)) {
                if (realMin == null) {
                    realMin = i;
                }
                if (realMax == null || realMax < i) {
                    realMax = i;
                }
                sum = sum.add(new BigDecimal(i));
                count++;
            }
        }

        BigDecimal avg = sum.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);

        OptimaizeCounter counter = new OptimaizeCounterImpl();
        counter.push(ImmutableList.<Integer>builder().addAll(firstBatch).build());
        counter.push(ImmutableList.<Integer>builder().addAll(secondBatch).build());
        counter.push(ImmutableList.<Integer>builder().addAll(thirdBatch).build());
        counter.push(ImmutableList.<Integer>builder().addAll(fourthBatch).build());
        assertEquals(realMax, counter.getLargest().orElse(null));
        assertEquals(realMin, counter.getSmallest().orElse(null));
        assertEquals(avg, counter.getAverage().orElse(null));
    }

    @Test
    public void multiThreadSeveralBatchTest() throws InterruptedException {
        List<Integer> firstBatch = new ArrayList<>();
        List<Integer> secondBatch = new ArrayList<>();
        List<Integer> thirdBatch = new ArrayList<>();
        List<Integer> fourthBatch = new ArrayList<>();
        int max = 300601;
        int min = -10043787;
        Integer realMin = null;
        Integer realMax = null;
        BigDecimal sum = new BigDecimal(0);
        int count = 0;

        for (int i = min; i <= max; i++) {
            if (insertByMod(firstBatch, i, 2) || insertByMod(secondBatch, i, 3)
                    || insertByMod(thirdBatch, i, 5) || insertByMod(fourthBatch, i, 7)) {
                if (realMin == null) {
                    realMin = i;
                }
                if (realMax == null || realMax < i) {
                    realMax = i;
                }
                sum = sum.add(new BigDecimal(i));
                count++;
            }
        }

        BigDecimal avg = sum.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);

        OptimaizeCounter counter = new OptimaizeCounterImpl();

        ImmutableList<Integer> firstImmutableBatch = ImmutableList.<Integer>builder().addAll(firstBatch).build();
        ImmutableList<Integer> secondImmutableBatch = ImmutableList.<Integer>builder().addAll(secondBatch).build();
        ImmutableList<Integer> thirdImmutableBatch = ImmutableList.<Integer>builder().addAll(thirdBatch).build();
        ImmutableList<Integer> fourthImmutableBatch = ImmutableList.<Integer>builder().addAll(fourthBatch).build();

        Thread firstThread = new Thread(() -> counter.push(firstImmutableBatch));
        Thread secondThread = new Thread(() -> counter.push(secondImmutableBatch));
        Thread thirdThread = new Thread(() -> counter.push(thirdImmutableBatch));
        Thread fourthThread = new Thread(() -> counter.push(fourthImmutableBatch));


        firstThread.start();
        // Try to modify source, to crash counter
        firstBatch.remove(firstBatch.size() -1);
        secondThread.start();
        secondBatch.remove(secondBatch.size() - 1);
        thirdThread.start();
        thirdBatch.remove(thirdBatch.size() - 1);
        fourthThread.start();

        firstThread.join();
        secondThread.join();
        thirdThread.join();
        fourthThread.join();

        assertEquals(realMax, counter.getLargest().orElse(null));
        assertEquals(realMin, counter.getSmallest().orElse(null));
        assertEquals(avg, counter.getAverage().orElse(null));
    }

    private boolean insertByMod(final List<Integer> dst, final Integer src, final int baseOfMod) {
        if (dst != null && src != null && src % baseOfMod == 0) {
            dst.add(src);
            return true;
        }
        return false;
    }
}
