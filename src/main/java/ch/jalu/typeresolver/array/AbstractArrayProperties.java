package ch.jalu.typeresolver.array;

import java.lang.reflect.Type;

public interface AbstractArrayProperties {

    Type getComponentType();

    int getDimension();
}
