package ch.jalu.typeresolver.samples.nestedclasses;

public class AdditionalNestedClassExt<F> extends InnerParameterizedClassesContainer<Double> {

    public class Intermediate<G> {
        public class TypedInnerClassExt extends TypedInnerClass<String> {

        }
    }
}
