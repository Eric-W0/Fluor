package eb2501.fluor.core;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class TestManual {

    /*!
        # Introduction
     */

    public void testMotivation() {

        /*!
            Let's consider a spreadsheet:

            |     | A | B | ... |
            |-----|---|---|-----|
            |  1  |   |   |     |
            |  2  |   |   |     |
            | ... |   |   |     |

            If we set the `A1` cell to 2, `A2` to 3 and assign `B1` the formula `=A1+A2`, we'll get something like
            this:

            |     | A | B | ... |
            |-----|---|---|-----|
            |  1  | 2 | 5 |     |
            |  2  | 3 |   |     |
            | ... |   |   |     |

            The benefit of a computerized version lays in the fact that if we change `A2` to 7, the value of `B`
            will automatically be updated:

            |     | A | B | ... |
            |-----|---|---|-----|
            |  1  | 2 | 9 |     |
            |  2  | 7 |   |     |
            | ... |   |   |     |

            This is the first important trait we'd like to emphasize: _automatic update_.

            Let's also set `B2` to another formula `=B1-1`:

            |     | A | B | ... |
            |-----|---|---|-----|
            |  1  | 2 | 9 |     |
            |  2  | 7 | 8 |     |
            | ... |   |   |     |

            Once we change `A2` to 6, both `B1` and `B2` are updated:

            |     | A | B | ... |
            |-----|---|---|-----|
            |  1  | 2 | 8 |     |
            |  2  | 6 | 7 |     |
            | ... |   |   |     |

            We just saw in action the second fundamental trait: _update propagation_.

            Finally, let's change `B2` to `=A2-1`:

            |     | A | B | ... |
            |-----|---|---|-----|
            |  1  | 2 | 8 |     |
            |  2  | 6 | 5 |     |
            | ... |   |   |     |

            Updating `A1` to 3 will change the value of `B1`, but `B2` will be left unchanged:

            |     | A | B | ... |
            |-----|---|---|-----|
            |  1  | 3 | 9 |     |
            |  2  | 6 | 5 |     |
            | ... |   |   |     |

            We just demonstrated the third last trait: _locality_.

            The goal of _Fluor_ is to bring this spreadsheet-like experience to the Java programming language:
         */

        var a1 = new ValueCell<>(2);
        var a2 = new ValueCell<>(3);
        var b1 = new SupplierCell<>(() -> a1.get() + a2.get());

        //
        // Automatic update
        //

        Assert.assertEquals((int)b1.get(), 5);

        a2.set(7);
        Assert.assertEquals((int)b1.get(), 9);

        //
        // Update propagation
        //

        var b2 = new SupplierCell<>(() -> b1.get() - 1);
        Assert.assertEquals((int)b2.get(), 8);

        a2.set(6);
        Assert.assertEquals((int)b1.get(), 8);
        Assert.assertEquals((int)b2.get(), 7);

        //
        // Locality
        //

        b2 = new SupplierCell<>(() -> a2.get() - 1);
        Assert.assertEquals((int)b2.get(), 5);

        a1.set(3);
        Assert.assertEquals((int)b1.get(), 9);
        Assert.assertEquals((int)b2.get(), 5);
    }

    /*!
        # The Components

        ## The `ValueCell` object
     */

    @Test
    public void testValueCell() {

        /*!
            `ValueCell` objects are used to store values like class members or variables:
         */

        var vc = new ValueCell<>(12);
        Assert.assertEquals((int)vc.get(), 12);

        vc.set(43);
        Assert.assertEquals((int)vc.get(), 43);

        /*!
            The big difference with members or variables is that other _Fluor_ components being made aware that
            something has changed when you call the `set()` method, and react accordingly.
        */
    }

    /*!
        ## The `Cache` object
     */

    @Test
    public void testCache() {

        /*!
            `Cache` objects are "read-only formula" equivalents:
         */

        int result;
        var vc = new ValueCell<>(12);
        var ca = new Cache<>(() -> vc.get() * 2);

        result = ca.get();
        Assert.assertEquals(result, 24);

        /*!
            The provided supplier is only called when needed (_lazy evaluation_):
         */

        int[] count = new int[1];
        var ca2 = new Cache<>(() -> {
            count[0]++;
            return vc.get() * 2;
        });

        Assert.assertEquals(count[0], 0);

        ca2.get();
        Assert.assertEquals(count[0], 1);

        /*!
            Once the value has been calculated, it is retained in an internal cache:
         */
        Assert.assertTrue(ca2.isCached());

        ca2.get();
        Assert.assertEquals(count[0], 1); // We just provide the cached value

        /*!
            It is clear that `ca2`'s calculation was using the value in `vc`. We will by extension
            say that `ca2` is _dependent_ on `vc` (or that `vc` is a dependency of `ca2`).

            The cached value is _invalidated_ when one of the dependencies has been updated:
         */

        vc.set(32);
        Assert.assertFalse(ca2.isCached());

        /*!
            Note that an invalidation doesn't immediately trigger a recalculation:
         */

        Assert.assertEquals(count[0], 1);

        /*!
            When a cache has been invalidated, it no longer depends on its previous
            dependencies (it is said to be in _detached_ state).

            A recalculation will only happen explicitly:
         */

        ca2.get();
        Assert.assertEquals(count[0], 2);
        Assert.assertTrue(ca2.isCached());

        /*!
            Invalidations do propagate along dependency chains:
         */

        var ca3 = new Cache<>(() -> ca2.get() + 2);

        result = ca3.get();
        Assert.assertEquals(result, 66);
        Assert.assertTrue(ca3.isCached());

        vc.set(3); // 'vc' will invalidate 'ca2' which in turn will invalidate 'ca3'
        Assert.assertFalse(ca2.isCached());
        Assert.assertFalse(ca3.isCached());

        /*!
            If you think about it, evaluations work the other way round:
         */

        ca3.get(); // 'ca3' evaluation will lead to 'ca2' being evaluated, which in
                   // turn will cause 'vc' to be read

        /*!
            One thing you can do is manually invalidate a cache:
         */

        ca2.invalidate();
        Assert.assertFalse(ca2.isCached());

        /*!
            As a consequence of doing that, all dependents are also being invalidated:
         */

        Assert.assertFalse(ca3.isCached());
    }

    /*!
        ## The `SupplierCell` object
     */

    public void testSupplierCell() {

        /*!
            `SupplierCell` can be thought as the combination between a `Cache` and a `ValueCell`:
         */

        var vc = new ValueCell<>(12);
        var sc = new SupplierCell<>(() -> vc.get() * 2);

        Assert.assertFalse(sc.isCached());

        int result = sc.get();
        Assert.assertEquals(result, 24);
        Assert.assertTrue(sc.isCached());

        sc.set(23);
        result = sc.get();
        Assert.assertEquals(result, 23);

        /*!
            The set value takes precedence over the default calculation provided at construction
            time. As a consequence, the `SupplierCell` no longer has any dependency:
         */

        vc.set(5);
        Assert.assertTrue(sc.isCached());
        result = sc.get();
        Assert.assertEquals(result, 23);

        /*!
            If you invalidate it, the value previously set goes away and the behavior goes back to
            what it was before:
         */

        sc.invalidate();
        Assert.assertFalse(sc.isCached());
        result = sc.get();
        Assert.assertEquals(result, 10);
        Assert.assertTrue(sc.isCached());
    }

    /*!
        ## The 'Loop' object
     */

    public void testLoop() {

        /*!
            A loop is essentially a runnable that is sensitive to invalidations:
         */

        var vc = new ValueCell<>(12);

        int[] count = new int[1];
        var l = new Loop(() -> count[0] += vc.get());
        Assert.assertEquals(count[0], 12);

        vc.set(2);
        Assert.assertEquals(count[0], 14);
    }
}
