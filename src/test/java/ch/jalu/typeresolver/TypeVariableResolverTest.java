package ch.jalu.typeresolver;

import ch.jalu.typeresolver.samples.typeinheritance.AbstractTwoArgProcessor;
import ch.jalu.typeresolver.samples.typeinheritance.IntegerDoubleArgProcessor;
import ch.jalu.typeresolver.samples.typeinheritance.IntegerDoubleArgProcessorExtension;
import ch.jalu.typeresolver.samples.typeinheritance.IntegerGenericArgProcessor;
import ch.jalu.typeresolver.samples.typeinheritance.OneArgProcessor;
import ch.jalu.typeresolver.samples.typevariables.ClassWithTypeVariables;
import ch.jalu.typeresolver.samples.typevariables.ClassWithTypeVariablesExt;
import ch.jalu.typeresolver.samples.typevariables.TypedContainer;
import ch.jalu.typeresolver.typeimpl.GenericArrayTypeImpl;
import ch.jalu.typeresolver.typeimpl.WildcardTypeImpl;
import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link TypeVariableResolver}.
 */
class TypeVariableResolverTest {

    @Test
    void shouldResolveTypeVariables() throws NoSuchMethodException, NoSuchFieldException {
        // given
        TypeVariableResolver resolver = new TypeVariableResolver(IntegerDoubleArgProcessorExtension.class);

        Method processMethod = OneArgProcessor.class.getDeclaredMethod("process", Object.class);
        Field tuMap = AbstractTwoArgProcessor.class.getDeclaredField("tuMap");

        // when
        Type processArgMethodType = resolver.resolve(processMethod.getGenericParameterTypes()[0]);
        TypeInfo resolvedTuMapTypeInfo = new TypeInfo(resolver.resolve(tuMap.getGenericType()));

        // then
        assertEquals(processArgMethodType, BigDecimal.class);
        assertEquals(resolvedTuMapTypeInfo.toClass(), Map.class);
        assertEquals(resolvedTuMapTypeInfo.getGenericTypeAsClass(0), Integer.class);
        assertEquals(resolvedTuMapTypeInfo.getGenericTypeAsClass(1), Double.class);
    }

    @Test
    void shouldReturnWildcardTypeIfItCannotBeResolved() throws NoSuchFieldException {
        // given
        TypeVariableResolver resolver = new TypeVariableResolver(IntegerDoubleArgProcessor.class);
        Field optionalValueField = Optional.class.getDeclaredField("value");

        // when
        Type resolvedType = resolver.resolve(optionalValueField.getGenericType());

        // then
        assertEquals(resolvedType, optionalValueField.getGenericType());
    }

    @Test
    void shouldReturnArgumentIfItIsAClass() {
        // given
        TypeVariableResolver resolver = new TypeVariableResolver(IntegerDoubleArgProcessor.class);

        // when / then
        assertEquals(resolver.resolve(String.class), String.class);
        assertEquals(resolver.resolve(int.class), int.class);
        assertEquals(resolver.resolve(BigDecimal[][].class), BigDecimal[][].class);
        assertEquals(resolver.resolve(char[].class), char[].class);
        assertNull(resolver.resolve(null));
    }

    @Test
    void shouldResolveTypesWithChildResolvers() throws Exception {
        // given
        TypeVariableResolver resolver = new TypeVariableResolver(ClassWithTypes.class);
        TypeVariableResolver intCharProcessorResolver = createChildResolver(resolver, ClassWithTypes.class, "intCharProcessor");
        Type tSetType = IntegerGenericArgProcessor.class.getDeclaredField("tSet").getGenericType();
        Type uListType = AbstractTwoArgProcessor.class.getDeclaredField("uList").getGenericType();
        Type tuMapType = AbstractTwoArgProcessor.class.getDeclaredField("tuMap").getGenericType();

        TypeVariableResolver floatListProcessorsResolver = createChildResolver(resolver, ClassWithTypes.class, "floatListProcessors");
        TypeVariableResolver oneArgProcessorResolver = createChildResolver(floatListProcessorsResolver, ListProcessorContainer.class, "oneArgProcessor");
        Type processParamType = OneArgProcessor.class.getDeclaredMethod("process", Object.class).getGenericParameterTypes()[0];
        TypeVariableResolver twoArgProcessorResolver = createChildResolver(floatListProcessorsResolver, ListProcessorContainer.class, "twoArgProcessor");

        // when
        Type tSetResolved = intCharProcessorResolver.resolve(tSetType);
        Type uListResolved = intCharProcessorResolver.resolve(uListType);
        Type tuMapResolved = intCharProcessorResolver.resolve(tuMapType);
        Type processParamTypeResolved = oneArgProcessorResolver.resolve(processParamType);
        Type tSetResolved2 = twoArgProcessorResolver.resolve(tSetType);
        Type tuMapResolved2 = twoArgProcessorResolver.resolve(tuMapType);

        // then
        assertIsParameterizedType(tSetResolved, Set.class, Character.class);
        assertIsParameterizedType(uListResolved, List.class, Character.class);
        assertIsParameterizedType(tuMapResolved, Map.class, Integer.class, Character.class);
        assertIsParameterizedType(processParamTypeResolved, List.class, Float.class);

        assertEquals(tSetResolved2, new TypeToken<Set<Map<Float, Set<Float>>>>() { }.getType());
        assertEquals(tuMapResolved2, new TypeToken<Map<Integer, Map<Float, Set<Float>>>>() { }.getType());
    }

    @Test
    void shouldResolveQuestionMarkTypes() throws Exception {
        // given
        TypeVariableResolver resolver = new TypeVariableResolver(IntegerDoubleArgProcessor.class);
        Type optionalType = AbstractTwoArgProcessor.class.getDeclaredField("uExtOptional").getGenericType();
        Type comparableType = AbstractTwoArgProcessor.class.getDeclaredField("tSuperComparable").getGenericType();

        // when
        Type resolvedOptionalType = resolver.resolve(optionalType);
        Type resolvedComparableType = resolver.resolve(comparableType);

        // then
        Type expectedOptionalType = new TypeToken<Optional<? extends Double>>() { }.getType();
        assertEquals(resolvedOptionalType, expectedOptionalType);
        Type expectedComparableType = new TypeToken<Comparable<? super Integer>>() { }.getType();
        assertEquals(resolvedComparableType, expectedComparableType);
    }

    @Test
    void shouldResolveQuestionMarkTypesRecursively() throws NoSuchFieldException {
        // given
        Type type = new TypeToken<AbstractTwoArgProcessor<? super Serializable, ? extends TimeUnit>>() { }.getType();
        TypeVariableResolver resolver = new TypeVariableResolver(type);
        Type tSuperComparable = AbstractTwoArgProcessor.class.getDeclaredField("tSuperComparable").getGenericType();
        Type uExtOptional = AbstractTwoArgProcessor.class.getDeclaredField("uExtOptional").getGenericType();

        // when
        Type comparableResolved = resolver.resolve(tSuperComparable);
        Type optionalResolved = resolver.resolve(uExtOptional);

        // then
        assertEquals(comparableResolved, new TypeToken<Comparable<? super Serializable>>() { }.getType());
        assertEquals(optionalResolved, new TypeToken<Optional<? extends TimeUnit>>() { }.getType());
    }

    @Test
    void shouldHandleCombinationOfSuperAndExtends() throws NoSuchFieldException {
        // given
        Type type = new TypeToken<AbstractTwoArgProcessor<? extends Serializable, ? super TimeUnit>>() { }.getType();
        TypeVariableResolver resolver = new TypeVariableResolver(type);
        Type tSuperComparable = AbstractTwoArgProcessor.class.getDeclaredField("tSuperComparable").getGenericType();
        Type uExtOptional = AbstractTwoArgProcessor.class.getDeclaredField("uExtOptional").getGenericType();

        // when
        Type comparableResolved = resolver.resolve(tSuperComparable);
        Type optionalResolved = resolver.resolve(uExtOptional);

        // then
        // Comparable type is: Comparable<capture<? super capture<? extends Serializable>>>
        assertIsParameterizedType(comparableResolved, Comparable.class, newWildcardSuper(newWildcardExtends(Serializable.class)));

        // Optional type is: Optional<capture<? extends capture<? super TimeUnit>>>
        assertIsParameterizedType(optionalResolved, Optional.class, newWildcardExtends(newWildcardSuper(TimeUnit.class)));
    }

    @Test
    void shouldResolveComplexTypeVariables() throws NoSuchFieldException {
        // given
        Type type = new TypeToken<ClassWithTypeVariablesExt<Double, TimeUnit>>() {}.getType();
        TypeVariableResolver resolver = new TypeVariableResolver(type);

        Type listType = TypedContainer.class.getDeclaredField("list").getGenericType();
        Type extendsListType = TypedContainer.class.getDeclaredField("extendsList").getGenericType();
        Type superListType = TypedContainer.class.getDeclaredField("superList").getGenericType();

        TypeVariableResolver comparableYResolver = resolver.createChildResolver(
            ClassWithTypeVariables.class.getDeclaredField("comparableYContainer").getGenericType());
        TypeVariableResolver xArrayContainerResolver = resolver.createChildResolver(
            ClassWithTypeVariables.class.getDeclaredField("xArrayContainer").getGenericType());
        TypeVariableResolver extendsZArrayContainerResolver = resolver.createChildResolver(
            ClassWithTypeVariables.class.getDeclaredField("listExtendsZArray").getGenericType());

        // when
        Type comparableYList = comparableYResolver.resolve(listType);
        Type comparableYExtendsList = comparableYResolver.resolve(extendsListType);
        Type comparableYSuperList = comparableYResolver.resolve(superListType);

        Type xArrayList = xArrayContainerResolver.resolve(listType);
        Type xArrayExtendsList = xArrayContainerResolver.resolve(extendsListType);
        Type xArraySuperList = xArrayContainerResolver.resolve(superListType);

        Type extendsList = extendsZArrayContainerResolver.resolve(listType);
        Type extendsExtendsList = extendsZArrayContainerResolver.resolve(extendsListType);
        Type extendsSuperList = extendsZArrayContainerResolver.resolve(superListType);

        // then
        assertEquals(comparableYList,        new TypeToken<List<Comparable<Double>>>() { }.getType());
        assertEquals(comparableYExtendsList, new TypeToken<List<? extends Comparable<Double>>>() { }.getType());
        assertEquals(comparableYSuperList,   new TypeToken<List<? super Comparable<Double>>>() { }.getType());

        assertEquals(xArrayList,        new TypeToken<List<Double[][]>>() { }.getType());
        assertEquals(xArrayExtendsList, new TypeToken<List<? extends Double[][]>>() { }.getType());
        assertEquals(xArraySuperList,   new TypeToken<List<? super Double[][]>>() { }.getType());

        assertEquals(extendsList,        new TypeToken<List<? extends TimeUnit[]>>() { }.getType());
        assertEquals(extendsExtendsList, new TypeToken<List<? extends TimeUnit[]>>() { }.getType());
        assertIsParameterizedType(extendsSuperList, List.class, newWildcardSuper(newWildcardExtends(TimeUnit[].class)));
    }

    @Test
    void shouldHandleQuestionMarkTypeVariable() throws NoSuchFieldException {
        // given
        Type type = new TypeToken<ClassWithTypeVariablesExt<?, ? extends ChronoField>>() {}.getType();
        TypeVariableResolver resolver = new TypeVariableResolver(type);

        Type listType = TypedContainer.class.getDeclaredField("list").getGenericType();
        Type extendsListType = TypedContainer.class.getDeclaredField("extendsList").getGenericType();
        Type superListType = TypedContainer.class.getDeclaredField("superList").getGenericType();

        TypeVariableResolver comparableYResolver = resolver.createChildResolver(
            ClassWithTypeVariables.class.getDeclaredField("comparableYContainer").getGenericType());
        TypeVariableResolver xArrayContainerResolver = resolver.createChildResolver(
            ClassWithTypeVariables.class.getDeclaredField("xArrayContainer").getGenericType());
        TypeVariableResolver extendsZArrayResolver = resolver.createChildResolver(
            ClassWithTypeVariables.class.getDeclaredField("listExtendsZArray").getGenericType());

        // when
        Type comparableYList = comparableYResolver.resolve(listType);
        Type comparableYExtendsList = comparableYResolver.resolve(extendsListType);
        Type comparableYSuperList = comparableYResolver.resolve(superListType);

        Type xArrayList = xArrayContainerResolver.resolve(listType);
        Type xArrayExtendsList = xArrayContainerResolver.resolve(extendsListType);
        Type xArraySuperList = xArrayContainerResolver.resolve(superListType);

        Type extendsList = extendsZArrayResolver.resolve(listType);
        Type extendsExtendsList = extendsZArrayResolver.resolve(extendsListType);
        Type extendsSuperList = extendsZArrayResolver.resolve(superListType);

        // then
        assertEquals(comparableYList,        new TypeToken<List<Comparable<?>>>(){ }.getType());
        assertEquals(comparableYExtendsList, new TypeToken<List<? extends Comparable<?>>>(){ }.getType());
        assertEquals(comparableYSuperList,   new TypeToken<List<? super Comparable<?>>>(){ }.getType());

        Type wildcardDoubleArray = new GenericArrayTypeImpl(new GenericArrayTypeImpl(
            new WildcardTypeImpl(new Type[]{ Object.class }, new Type[0])));
        assertIsParameterizedType(xArrayList, List.class, wildcardDoubleArray);
        assertIsParameterizedType(xArrayExtendsList, List.class, newWildcardExtends(wildcardDoubleArray));
        assertIsParameterizedType(xArraySuperList, List.class, newWildcardSuper(wildcardDoubleArray));

        WildcardType wildcardExtendsArrayOfExtendsChronoField = newWildcardExtends(
            new GenericArrayTypeImpl(newWildcardExtends(ChronoField.class)));
        assertIsParameterizedType(extendsList, List.class, wildcardExtendsArrayOfExtendsChronoField);
        assertIsParameterizedType(extendsExtendsList, List.class, wildcardExtendsArrayOfExtendsChronoField);
        assertIsParameterizedType(extendsSuperList, List.class, newWildcardSuper(wildcardExtendsArrayOfExtendsChronoField));
    }

    @Test
    void shouldResolveArrayTypesToTypeVariableArrayType() throws NoSuchFieldException {
        // given
        TypeVariableResolver resolver = new TypeVariableResolver(ClassWithTypeVariablesContainer.class)
            .createChildResolver(ClassWithTypeVariablesContainer.class.getDeclaredField("foo").getGenericType());

        Type listType = TypedContainer.class.getDeclaredField("list").getGenericType();
        Type extendsListType = TypedContainer.class.getDeclaredField("extendsList").getGenericType();
        Type superListType = TypedContainer.class.getDeclaredField("superList").getGenericType();

        TypeVariableResolver xArrayContainerResolver = resolver.createChildResolver(
            ClassWithTypeVariables.class.getDeclaredField("xArrayContainer").getGenericType());
        TypeVariableResolver extendsZArrayResolver = resolver.createChildResolver(
            ClassWithTypeVariables.class.getDeclaredField("listExtendsZArray").getGenericType());

        // when
        Type xArrayList = xArrayContainerResolver.resolve(listType);
        Type xArrayExtendsList = xArrayContainerResolver.resolve(extendsListType);
        Type xArraySuperList = xArrayContainerResolver.resolve(superListType);

        Type extendsList = extendsZArrayResolver.resolve(listType);
        Type extendsExtendsList = extendsZArrayResolver.resolve(extendsListType);
        Type extendsSuperList = extendsZArrayResolver.resolve(superListType);

        // then
        Type xTypeVariable = ClassWithTypeVariablesContainer.class.getTypeParameters()[0];
        Type wildcardDoubleArray = new GenericArrayTypeImpl(new GenericArrayTypeImpl(xTypeVariable));
        assertIsParameterizedType(xArrayList, List.class, wildcardDoubleArray);
        assertIsParameterizedType(xArrayExtendsList, List.class, newWildcardExtends(wildcardDoubleArray));
        assertIsParameterizedType(xArraySuperList, List.class, newWildcardSuper(wildcardDoubleArray));

        WildcardType wildcardExtendsGArrayType = newWildcardExtends(
            new GenericArrayTypeImpl(ClassWithTypeVariablesContainer.class.getTypeParameters()[1]));
        assertIsParameterizedType(extendsList, List.class, wildcardExtendsGArrayType);
        assertIsParameterizedType(extendsExtendsList, List.class, wildcardExtendsGArrayType);
        assertIsParameterizedType(extendsSuperList, List.class, newWildcardSuper(wildcardExtendsGArrayType));
    }

    /** Creates a type "? extends T" where T is the given upperBound. */
    private static WildcardType newWildcardExtends(Type upperBound) {
        return new WildcardTypeImpl(new Type[]{ upperBound }, new Type[0]);
    }

    /** Creates a type "? super T" where T is the given lowerBound. */
    private static WildcardType newWildcardSuper(Type lowerBound) {
        return new WildcardTypeImpl(new Type[]{ Object.class }, new Type[]{ lowerBound });
    }

    private static TypeVariableResolver createChildResolver(TypeVariableResolver parentResolver,
                                                            Class<?> clazz, String fieldName) {
        try {
            return parentResolver.createChildResolver(clazz.getDeclaredField(fieldName).getGenericType());
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(clazz + " - " + fieldName, e);
        }
    }

    private static void assertIsParameterizedType(Type actualType, Class<?> expectedRawType, Type... expectedTypeArguments) {
        assertTrue(actualType instanceof ParameterizedType);
        ParameterizedType pt = (ParameterizedType) actualType;
        assertEquals(expectedRawType, pt.getRawType());
        assertArrayEquals(expectedTypeArguments, pt.getActualTypeArguments());
    }

    private static class ClassWithTypes {

        private IntegerGenericArgProcessor<Character> intCharProcessor;
        private ListProcessorContainer<Float> floatListProcessors;

    }

    private static class ListProcessorContainer<T> {

        private OneArgProcessor<List<T>> oneArgProcessor;
        private IntegerGenericArgProcessor<Map<T, Set<T>>> twoArgProcessor;

    }

    private static class ClassWithTypeVariablesContainer<F, G extends Enum<?>> {

        private ClassWithTypeVariables<F, F, G> foo;

    }
}