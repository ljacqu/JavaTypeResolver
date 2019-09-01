package ch.jalu.typeresolver.samples.typeinheritance;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractTwoArgProcessor<T, U> {

    private T[] tArr;
    private List<U> uList;
    private Map<T, U> tuMap;
    private Optional<? extends U> uExtOptional;
    private Comparable<? super T> tSuperComparable;

}
