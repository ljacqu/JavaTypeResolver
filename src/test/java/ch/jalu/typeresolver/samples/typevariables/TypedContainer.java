package ch.jalu.typeresolver.samples.typevariables;

import java.util.List;

public class TypedContainer<T> {

    public List<T> list;
    public List<? extends T> extendsList;
    public List<? super T> superList;

}
