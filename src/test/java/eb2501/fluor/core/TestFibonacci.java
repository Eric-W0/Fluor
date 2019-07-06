package eb2501.fluor.core;

import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;

@Test
public class TestFibonacci {

    private static class Link extends Page {
        public final Cell<Link> linkMinus1 = cell();
        public final Cell<Link> linkMinus2 = cell();

        public final Cell<Long> value = cell(() -> {
            final var lm1 = linkMinus1.get();
            final var lm2 = linkMinus2.get();
            if ((lm1 == null) || (lm2 == null)) {
                return 1L;
            } else {
                return lm1.value.get() + lm2.value.get();
            }
        });
    }

    private interface Calculation {
        void setSeed(long seed);
        long getResult();
    }

    private static final int CALCULATION_DEPTH = 100;

    private static class NativeCalculation implements Calculation {
        private long seed;

        @Override
        public void setSeed(long seed) {
            this.seed = seed;
        }

        @Override
        public long getResult() {
            long minus1 = seed;
            long minus2 = 1L;
            for (int i = 3; i <= CALCULATION_DEPTH; i++) {
                long temp = minus1 + minus2;
                minus2 = minus1;
                minus1 = temp;
            }
            return minus1;
        }
    }

    private static class FluorCalculation implements Calculation {
        private Link secondLink;
        private Link lastLink;

        public FluorCalculation() {
            final var firstLink = new Link();
            secondLink = new Link();
            var linkMinus1 = secondLink;
            var linkMinus2 = firstLink;
            for (int i = 3; i <= CALCULATION_DEPTH; i++) {
                var newLink = Chain.create(new Link())
                        .set(o -> o.linkMinus1, linkMinus1)
                        .set(o -> o.linkMinus2, linkMinus2)
                        .done();
                linkMinus2 = linkMinus1;
                linkMinus1 = newLink;
            }
            lastLink = linkMinus1;
        }

        @Override
        public void setSeed(long seed) {
            secondLink.value.set(seed);
        }

        @Override
        public long getResult() {
            return lastLink.value.get();
        }
    }

    private static final int LOOP_COUNT = 100_000;

    private void measure(String name, Calculation calculation) {
        System.out.println("Measuring [" + name + "]...");
        long[] results = new long[2];
        Instant before = Instant.now();
        for (int i = 0; i < LOOP_COUNT; i++) {
            calculation.setSeed((i % 2) == 0 ? 1L : 2L);
            long result = calculation.getResult();
            if (i < 2) {
                results[i] = result;
            }
        }
        Instant after = Instant.now();
        System.out.println("Duration: " + Duration.between(before, after));
        for (int i = 0; i < 2; i++) {
            System.out.println("Actual value for [" + i + "] is [" + results[i] + "]");
        }
    }

    public void testSpeed() {
        measure("Native", new NativeCalculation());
        measure("Context", new FluorCalculation());
    }
}
