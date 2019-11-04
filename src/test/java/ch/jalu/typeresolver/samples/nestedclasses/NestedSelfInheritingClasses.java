package ch.jalu.typeresolver.samples.nestedclasses;

import java.util.concurrent.TimeUnit;

public class NestedSelfInheritingClasses<A> {

    // -------------------
    // Static nested classes
    // -------------------

    public static class StaticLevel1<W> extends NestedSelfInheritingClasses<Short> {
        // Static class - no access to A

        public W returnW() {
            return null;
        }

        public static class StaticLevel2<X> {
            // Static class - no access to W

            public X returnX() {
                return null;
            }

            public static class StaticLevel3<Y> extends StaticLevel1<Float> {
                public Float demoW() {
                    return returnW();
                }

                public Y returnY() {
                    return null;
                }
            }
        }

        public static class StaticLevel1Ext extends StaticLevel1<Character> {
            public Character demoW() {
                return returnW();
            }

            public static class StaticLevel3Ext extends StaticLevel2.StaticLevel3<String> {
                public Float demoW() {
                    return returnW();
                }
            }
        }
    }


    // -------------------
    // Inner classes
    // -------------------

    public class InnerLevel1<B> {

        public A a1() { return null; }
        public B b1() { return null; }

        public class InnerLevel2<C> {

            public A a2() { return a1(); }
            public B b2() { return b1(); }
            public C c2() { return null; }

            public class InnerLevel3S<D> extends StaticLevel1<Float> {
                public A a3() { return a2(); }
                public B b3() { return b2(); }
                public C c3() { return c2(); }
                public D d3() { return null; }
                public Float demoW() { return returnW(); }
            }

            public class InnerLevel3I<D> extends InnerLevel1<Float> {
                public A a3() { return a2(); }
                public Float demoB1() { return b1(); }
                public B b3() { return b2(); }
                public C c3() { return c2(); }
                public D d3() { return null; }
            }
        }

        public class InnerLevel1Ext extends InnerLevel1<Character> {
            public Character demoB() { return b1(); }

            public class WrapperL1L2 {

                public class WrappedInnerL2Ext extends InnerLevel2<Byte> {
                    public Character demoB() { return b2(); }
                    public Byte demoC() { return c2(); }

                    public class InnerL2Ext extends InnerLevel2<Short> {
                        public B returnB() { return null; }
                        public Character demoB1() { return b1(); }
                        public Character demoB2() { return b2(); }

                        public Short demoC() { return c2(); }

                        public class WrapperL2L3<C> {

                            public class InnerL3Ext extends InnerLevel3I<TimeUnit> {

                                public B returnB() { return null; }
                                public Float demoB1() { return b1(); } // from InnerLevel3I -> extends InnerLevel1<Float>
                                public Character demoB2() { return b2(); } // from enclosing class (InnerLevel1Ext)

                                public C returnC() { return null; } // C is here from WrapperL2L3
                                public Short demoC2() { return c2(); }
                                public Short demoC3() { return c3(); }

                                public TimeUnit demoD() { return d3(); }
                            }
                        }
                    }
                }
            }
        }
    }
}
