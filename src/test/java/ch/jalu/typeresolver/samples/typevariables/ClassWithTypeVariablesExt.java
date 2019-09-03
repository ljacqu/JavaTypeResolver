package ch.jalu.typeresolver.samples.typevariables;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

public class ClassWithTypeVariablesExt<A extends Number, C extends Enum & Serializable>
    extends ClassWithTypeVariables<A, A, C> {

    public Optional<? extends C> optionalExtendsC;
    public Set<? super A> setSuperA;

}
