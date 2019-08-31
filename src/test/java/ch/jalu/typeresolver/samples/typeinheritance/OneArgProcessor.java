package ch.jalu.typeresolver.samples.typeinheritance;

public interface OneArgProcessor<T> {

    default void process(T t) {
    }

}
