package ch.jalu.typeresolver.samples.typevariables;

import java.io.Serializable;
import java.util.List;

public class ClassWithTypeVariables<X, Y extends X, Z extends Enum & Serializable> {

    public Y yField;
    public List<X> xList;
    public TypedContainer<Comparable<Y>> comparableYContainer;
    public TypedContainer<X[][]> xArrayContainer;
    public List<? extends Z[]> listExtendsZArray;

    public TypedContainer<? extends X> extendsContainer;
    public TypedContainer<TypedContainer<? extends Z>> nestedContainer;

    // Note that parameter Z is named the same as the parameter on the class
    public <M extends Y, Z> void methodWithType(M arg1, Z arg2, Z arg3) {
    }
}
