package ch.jalu.typeresolver.samples.nestedclasses;

public class InnerParameterizedClassesContainerExt extends InnerParameterizedClassesContainer<Integer> {

    public TypedInnerClassExt.InnerInnerClassExt<Character> innerInner;
    public FloatNestedClassExt.TypedNestedInnerClassExt<Double> nestedInner;

    public class TypedInnerClassExt extends TypedInnerClass<String> {
        public class InnerInnerClassExt<I> extends InnerInnerClass<I> {
        }
    }
    
    public static class TypedNestedClassExt<N> extends TypedNestedClass<N> {
        public class TypedNestedInnerClassExt<C> extends TypedNestedInnerClass<C> {
        }
    }

    public static class FloatNestedClassExt extends TypedNestedClassExt<Float> {
    }
}