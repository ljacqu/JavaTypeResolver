package ch.jalu.typeresolver.samples.typeinheritance;

import java.util.Set;

public class IntegerGenericArgProcessor<T> extends AbstractTwoArgProcessor<Integer, T> {
    // T type here is the U type of the parent (done intentionally)

    private Set<T> tSet;

}
