package ch.jalu.typeresolver.samples.nestedclasses;

public class InnerParameterizedClassesContainer<P> {

    public TypedInnerClass<String>.InnerInnerClass<Character> innerInner;
    public TypedNestedClass<Float>.TypedNestedInnerClass<Double> nestedInner;

    public class TypedInnerClass<T> {
        public class InnerInnerClass<I> {
        }
    }

    public static class TypedNestedClass<N> {
        public class TypedNestedInnerClass<C> {
        }
    }
}