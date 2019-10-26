package ch.jalu.typeresolver.samples.nestedclasses;

public class InnerParameterizedClassesContainer<P> {

    public TypedInnerClass<String>.InnerInnerClass<Character> innerInner;
    public TypedNestedClass<Float>.TypedNestedInnerClass<Double> nestedInner;

    public class TypedInnerClass<T> {
        public class InnerInnerClass<I> {
            public T tField;
            public P pField;
        }
    }

    public static class TypedNestedClass<N> {
        public N returnsN() {
            return null;
        }

        public class TypedNestedInnerClass<C> {
            public N nField;
        }
    }
}