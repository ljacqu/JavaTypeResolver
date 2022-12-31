package ch.jalu.typeresolver.typeimpl;

/*
 * This<A>
 *  - static SP1<B>
 *    - static SP2<C>
 *    - N2
 *      - NP3<X>
 *  - static S1
 *    - N2
 *      - NP3<P>
 * - N1
 *   - N2
 *     - NP3<T>
 * - NP1
 */
public class ClassWithTypeParamEnclosingOthers<A> {

    public static class SP1<B> {
        public SP1<B> selfTyped;

        public static class SP2<C> {
            public SP2<C> selfTyped;
        }

        public class N2 {

            public class NP3<X> {
                public NP3<X> selfTyped;
            }

        }
    }

    public static class S1 {

        public class N2 {

            public class NP3<P> {
                public NP3<P> selfTyped;
            }

        }
    }

    public class N1 {

        public class N2 {

            public class NP3<T> {
                public NP3<T> selfTyped;
            }
        }
    }

    public class NP1<Q> {
        public NP1<Q> selfTyped;
    }
}
