package ch.jalu.typeresolver.typeimpl;

/*
 * This<A>
 *  - static SP1<B>
 *    - static SP2<C>
 *    - N
 *      - NP<X>
 *  - static S
 *    - N
 *      - NP<P>
 * - N
 *   - N2
 *     - NP<T>
 */
public class ClassWithTypeParamEnclosingOthers<A> {

    public static class SP1<B> {

        public static class SP2<C> {
            private SP2<C> selfTyped;
        }

        public class N {

            private B b;

            public class NP<X> {
                private NP<X> selfTyped;
            }

        }
    }

    public static class S {

        public class N {

            public class NP<P> {
                private NP<P> selfTyped;
            }

        }
    }

    public class N {

        public class N2 {

            public class NP<T> {
                private NP<T> selfTyped;
            }
        }
    }
}
