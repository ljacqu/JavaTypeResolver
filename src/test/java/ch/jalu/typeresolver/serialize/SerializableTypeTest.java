package ch.jalu.typeresolver.serialize;

import ch.jalu.typeresolver.TypeInfo;
import ch.jalu.typeresolver.reference.TypeReference;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test for {@link SerializableType}.
 */
class SerializableTypeTest<A, B, C> {

    @ParameterizedTest
    @MethodSource("typesToTest")
    void shouldWrapType(TypeInfo typeInfo) {
        // given
        Type type = typeInfo.getType();

        // when
        SerializableType serType = SerializableType.from(type);

        // then
        assertThat(serType.toType(), equalTo(type));
    }

    @Test
    void shouldHaveSerialVersionFieldOnAllImplementations() throws NoSuchFieldException {
        // given
        Class<?>[] nestedClasses = SerializableType.class.getDeclaredClasses();
        assertThat(nestedClasses, arrayWithSize(5)); // Ensure we actually test something

        // when / then
        for (Class<?> nestedClass : nestedClasses) {
            Field serialVersionField = nestedClass.getDeclaredField("serialVersionUID");

            boolean isValid = Modifier.isStatic(serialVersionField.getModifiers())
                && Modifier.isFinal(serialVersionField.getModifiers())
                && serialVersionField.getType().equals(long.class);
            if (!isValid) {
                fail("Expected static final long serialVersionUID field in " + nestedClass);
            }
        }
    }

    @Test
    void shouldThrowForUnknownType() {
        // given
        Type type = new UnknownType();

        // when
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> SerializableType.from(type));

        // then
        assertThat(ex.getMessage(), equalTo("Unknown type implementation: class ch.jalu.typeresolver.serialize.SerializableTypeTest$UnknownType"));
    }

    @Test
    <M> void shouldThrowForTypeVariableNotDeclaredByClass() throws NoSuchMethodException {
        // given
        TypeVariable<Method> methodTypeVariable = SerializableTypeTest.class
            .getDeclaredMethod("shouldThrowForTypeVariableNotDeclaredByClass")
            .getTypeParameters()[0];

        // when
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> SerializableType.from(methodTypeVariable));

        // then
        assertThat(ex.getMessage(), equalTo("Type variable must be declared by a class, but found declarer: void ch.jalu.typeresolver.serialize.SerializableTypeTest.shouldThrowForTypeVariableNotDeclaredByClass() throws java.lang.NoSuchMethodException"));
    }

    @Test
    void shouldThrowIfTypeVariableIndexCannotBeFound() {
        // given
        TypeVariable<?> fakeTypeVar = new TypeVariable<GenericDeclaration>() {
            @Override public GenericDeclaration getGenericDeclaration() { return Map.class; }

            @Override public Type[] getBounds() {                    throw new UnsupportedOperationException(); }
            @Override public String getName() {                      throw new UnsupportedOperationException(); }
            @Override public AnnotatedType[] getAnnotatedBounds() {  throw new UnsupportedOperationException(); }
            @Override public <T extends Annotation> T getAnnotation(@NotNull Class<T> annotationClass) { throw new UnsupportedOperationException(); }
            @Override public Annotation[] getAnnotations() {         throw new UnsupportedOperationException(); }
            @Override public Annotation[] getDeclaredAnnotations() { throw new UnsupportedOperationException(); }
        };


        // when
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> SerializableType.from(fakeTypeVar));

        // then
        assertThat(ex.getMessage(), matchesRegex("Type variable 'ch\\.jalu\\.typeresolver\\.serialize\\.SerializableTypeTest\\$.*?' does not belong to interface java\\.util\\.Map"));
    }

    @Test
    void shouldThrowIfTypeVariableIndexDoesNotMatchOnCreate() {
        // given
        SerializableType.SerializableTypeVariable serTypeVar1 = new SerializableType.SerializableTypeVariable(Map.class, 3);
        SerializableType.SerializableTypeVariable serTypeVar2 = new SerializableType.SerializableTypeVariable(String.class, 0);

        // when
        IllegalStateException ex1 = assertThrows(IllegalStateException.class, serTypeVar1::toType);
        IllegalStateException ex2 = assertThrows(IllegalStateException.class, serTypeVar2::toType);

        // then
        assertThat(ex1.getMessage(), equalTo("No type variable at index 3 for interface java.util.Map"));
        assertThat(ex2.getMessage(), equalTo("No type variable at index 0 for class java.lang.String"));
    }

    static Stream<TypeInfo> typesToTest() {
        return Stream.of(
            TypeInfo.of(String.class),
            TypeInfo.of(void.class),
            TypeInfo.of(double[][].class),
            new TypeReference<List<String>>() { },
            new TypeReference<Map<?, ? extends Integer>>() { },
            new TypeReference<Consumer<Integer>[]>() { },
            typeOfField("listA"),
            typeOfField("mapBC"),
            typeOfField("supplierABC"));
    }

    private List<A> listA;
    private Map<B, Optional<C>> mapBC;
    private Supplier<Function<? super A, Map<B[], Consumer<? extends C>>[]>> supplierABC;

    private static TypeInfo typeOfField(String name) {
        try {
            Field field = SerializableTypeTest.class.getDeclaredField(name);
            return TypeInfo.of(field);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("No field '" + name + "'", e);
        }
    }

    private static class UnknownType implements Type {
    }
}