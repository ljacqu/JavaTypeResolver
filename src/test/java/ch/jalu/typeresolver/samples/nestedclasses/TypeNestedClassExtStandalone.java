package ch.jalu.typeresolver.samples.nestedclasses;

public class TypeNestedClassExtStandalone extends InnerParameterizedClassesContainer.TypedNestedClass<Float> {

    public static class NestedTypeNestedClassExtStandalone extends InnerParameterizedClassesContainer.TypedNestedClass<String> {

        public String proofOfInferredTypeN() {
            String string = returnsN(); // String and not Float!
            return string;
        }

    }

    public class NestedTypeNestedClassNoParent {

        public Float proofOfInferredTypeN() {
            Float f = returnsN();
            return f;
        }
    }
}
