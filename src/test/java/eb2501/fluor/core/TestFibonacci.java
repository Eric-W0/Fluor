package eb2501.fluor.core;

import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;

@Test
public class TestFibonacci {

    private static class Link {
        //<editor-fold desc="public Cell<Link> LinkMinus1">
        private Cell<Link> linkMinus1;

        private static final Link DEFAULT_LINK_MINUS_1 = null;

        public Link getLinkMinus1() {
            if (linkMinus1 == null) {
                linkMinus1 = new ValueCell<>(DEFAULT_LINK_MINUS_1);
            }
            return linkMinus1.get();
        }

        public Link setLinkMinus1(final Link _value_) {
            if (linkMinus1 == null) {
                linkMinus1 = new ValueCell<>(_value_);
            } else {
                linkMinus1.set(_value_);
            }
            return this;
        }

        public Link bindLinkMinus1(final Cell<Link> _cell_) {
            if (linkMinus1 != null) {
                throw new IllegalStateException("Cell already bound");
            }
            linkMinus1 = _cell_;
            return this;
        }

        public Cell<Link> getLinkMinus1Property() {
            if (linkMinus1 == null) {
                linkMinus1 = new ValueCell<>(DEFAULT_LINK_MINUS_1);
            }
            return linkMinus1;
        }
        //</editor-fold>
        //<editor-fold desc="public Cell<Link> LinkMinus2">
        private Cell<Link> linkMinus2;

        private static final Link DEFAULT_LINK_MINUS_2 = null;

        public Link getLinkMinus2() {
            if (linkMinus2 == null) {
                linkMinus2 = new ValueCell<>(DEFAULT_LINK_MINUS_2);
            }
            return linkMinus2.get();
        }

        public Link setLinkMinus2(final Link _value_) {
            if (linkMinus2 == null) {
                linkMinus2 = new ValueCell<>(_value_);
            } else {
                linkMinus2.set(_value_);
            }
            return this;
        }

        public Link bindLinkMinus2(final Cell<Link> _cell_) {
            if (linkMinus2 != null) {
                throw new IllegalStateException("Cell already bound");
            }
            linkMinus2 = _cell_;
            return this;
        }

        public Cell<Link> getLinkMinus2Property() {
            if (linkMinus2 == null) {
                linkMinus2 = new ValueCell<>(DEFAULT_LINK_MINUS_2);
            }
            return linkMinus2;
        }
        //</editor-fold>
        //<editor-fold desc="public Node<Long> Value">
        private Cell<Long> value;

        private Long buildValue() {
            final var lm1 = getLinkMinus1();
            final var lm2 = getLinkMinus2();
            if ((lm1 == null) || (lm2 == null)) {
                return 1L;
            } else {
                return lm1.getValue() + lm2.getValue();
            }
        }

        public Long getValue() {
            if (value == null) {
                value = new SupplierCell<>(this::buildValue);
            }
            return value.get();
        }

        public Link setValue(final Long _value_) {
            if (value == null) {
                value = new SupplierCell<>(this::buildValue);
            }
            value.set(_value_);
            return this;
        }

        public Link bindValue(final Cell<Long> _cell_) {
            if (value != null) {
                throw new IllegalStateException("Cell already bound");
            }
            value = _cell_;
            return this;
        }

        public Cell<Long> getValueProperty() {
            if (value == null) {
                value = new SupplierCell<>(this::buildValue);
            }
            return value;
        }
        //</editor-fold>
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
                var newLink = new Link()
                        .setLinkMinus1(linkMinus1)
                        .setLinkMinus2(linkMinus2);
                linkMinus2 = linkMinus1;
                linkMinus1 = newLink;
            }
            lastLink = linkMinus1;
        }

        @Override
        public void setSeed(long seed) {
            secondLink.setValue(seed);
        }

        @Override
        public long getResult() {
            return lastLink.getValue();
        }
    }

    private static final int LOOP_COUNT = 1_000;

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
        measure("Fluor", new FluorCalculation());
    }
}
