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
import java.nio.file.AccessMode;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static ch.jalu.typeresolver.typeimpl.WildcardTypeImpl.newWildcardExtends;
import static ch.jalu.typeresolver.typeimpl.WildcardTypeImpl.newWildcardSuper;
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
        TypeInfo typeInfo = new TypeInfo(IntegerDoubleArgProcessorExtension.class);

        Method processMethod = OneArgProcessor.class.getDeclaredMethod("process", Object.class);
        Field tuMap = AbstractTwoArgProcessor.class.getDeclaredField("tuMap");

        // when
        TypeInfo processArgMethodType = typeInfo.resolve(processMethod.getGenericParameterTypes()[0]);
        TypeInfo resolvedTuMapTypeInfo = typeInfo.resolve(tuMap.getGenericType());

        // then
        assertType(processArgMethodType, BigDecimal.class);
        assertEquals(resolvedTuMapTypeInfo.toClass(), Map.class);
        assertEquals(resolvedTuMapTypeInfo.getTypeArgumentAsClass(0), Integer.class);
        assertEquals(resolvedTuMapTypeInfo.getTypeArgumentAsClass(1), Double.class);
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
        TypeInfo typeInfo = new TypeInfo(ClassWithTypes.class);
        TypeInfo intCharProcessorResolver = createChildTypeInfo(typeInfo, ClassWithTypes.class, "intCharProcessor");
        Type tSetType = IntegerGenericArgProcessor.class.getDeclaredField("tSet").getGenericType();
        Type uListType = AbstractTwoArgProcessor.class.getDeclaredField("uList").getGenericType();
        Type tuMapType = AbstractTwoArgProcessor.class.getDeclaredField("tuMap").getGenericType();

        TypeInfo floatListProcessorsResolver = createChildTypeInfo(typeInfo, ClassWithTypes.class, "floatListProcessors");
        TypeInfo oneArgProcessorResolver = createChildTypeInfo(floatListProcessorsResolver, ListProcessorContainer.class, "oneArgProcessor");
        Type processParamType = OneArgProcessor.class.getDeclaredMethod("process", Object.class).getGenericParameterTypes()[0];
        TypeInfo twoArgProcessorResolver = createChildTypeInfo(floatListProcessorsResolver, ListProcessorContainer.class, "twoArgProcessor");

        // when
        TypeInfo tSetResolved = intCharProcessorResolver.resolve(tSetType);
        TypeInfo uListResolved = intCharProcessorResolver.resolve(uListType);
        TypeInfo tuMapResolved = intCharProcessorResolver.resolve(tuMapType);
        TypeInfo processParamTypeResolved = oneArgProcessorResolver.resolve(processParamType);
        TypeInfo tSetResolved2 = twoArgProcessorResolver.resolve(tSetType);
        TypeInfo tuMapResolved2 = twoArgProcessorResolver.resolve(tuMapType);

        // then
        assertIsParameterizedType(tSetResolved, Set.class, Character.class);
        assertIsParameterizedType(uListResolved, List.class, Character.class);
        assertIsParameterizedType(tuMapResolved, Map.class, Integer.class, Character.class);
        assertIsParameterizedType(processParamTypeResolved, List.class, Float.class);

        assertEquals(tSetResolved2.getType(), new TypeToken<Set<Map<Float, Set<Float>>>>() { }.getType());
        assertEquals(tuMapResolved2.getType(), new TypeToken<Map<Integer, Map<Float, Set<Float>>>>() { }.getType());
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
        TypeInfo typeInfo = new TypeInfo(type);

        Type listType = TypedContainer.class.getDeclaredField("list").getGenericType();
        Type extendsListType = TypedContainer.class.getDeclaredField("extendsList").getGenericType();
        Type superListType = TypedContainer.class.getDeclaredField("superList").getGenericType();

        TypeInfo comparableYResolver = typeInfo.resolve(
            ClassWithTypeVariables.class.getDeclaredField("comparableYContainer").getGenericType());
        TypeInfo xArrayContainerResolver = typeInfo.resolve(
            ClassWithTypeVariables.class.getDeclaredField("xArrayContainer").getGenericType());
        TypeInfo extendsZArrayContainerResolver = typeInfo.resolve(
            ClassWithTypeVariables.class.getDeclaredField("listExtendsZArray").getGenericType());

        // when
        TypeInfo comparableYList = comparableYResolver.resolve(listType);
        TypeInfo comparableYExtendsList = comparableYResolver.resolve(extendsListType);
        TypeInfo comparableYSuperList = comparableYResolver.resolve(superListType);

        TypeInfo xArrayList = xArrayContainerResolver.resolve(listType);
        TypeInfo xArrayExtendsList = xArrayContainerResolver.resolve(extendsListType);
        TypeInfo xArraySuperList = xArrayContainerResolver.resolve(superListType);

        TypeInfo extendsList = extendsZArrayContainerResolver.resolve(listType);
        TypeInfo extendsExtendsList = extendsZArrayContainerResolver.resolve(extendsListType);
        TypeInfo extendsSuperList = extendsZArrayContainerResolver.resolve(superListType);

        // then
        assertType(comparableYList,        new TypeToken<List<Comparable<Double>>>() { }.getType());
        assertType(comparableYExtendsList, new TypeToken<List<? extends Comparable<Double>>>() { }.getType());
        assertType(comparableYSuperList,   new TypeToken<List<? super Comparable<Double>>>() { }.getType());

        assertType(xArrayList,        new TypeToken<List<Double[][]>>() { }.getType());
        assertType(xArrayExtendsList, new TypeToken<List<? extends Double[][]>>() { }.getType());
        assertType(xArraySuperList,   new TypeToken<List<? super Double[][]>>() { }.getType());

        assertType(extendsList,        new TypeToken<List<? extends TimeUnit[]>>() { }.getType());
        assertType(extendsExtendsList, new TypeToken<List<? extends TimeUnit[]>>() { }.getType());
        assertIsParameterizedType(extendsSuperList, List.class, newWildcardSuper(newWildcardExtends(TimeUnit[].class)));
    }

    @Test
    void shouldHandleQuestionMarkTypeVariable() throws NoSuchFieldException {
        // given
        Type type = new TypeToken<ClassWithTypeVariablesExt<?, ? extends ChronoField>>() {}.getType();
        TypeInfo typeInfo = new TypeInfo(type);

        Type listType = TypedContainer.class.getDeclaredField("list").getGenericType();
        Type extendsListType = TypedContainer.class.getDeclaredField("extendsList").getGenericType();
        Type superListType = TypedContainer.class.getDeclaredField("superList").getGenericType();

        TypeInfo comparableYResolver = typeInfo.resolve(
            ClassWithTypeVariables.class.getDeclaredField("comparableYContainer").getGenericType());
        TypeInfo xArrayContainerResolver = typeInfo.resolve(
            ClassWithTypeVariables.class.getDeclaredField("xArrayContainer").getGenericType());
        TypeInfo extendsZArrayResolver = typeInfo.resolve(
            ClassWithTypeVariables.class.getDeclaredField("listExtendsZArray").getGenericType());

        // when
        TypeInfo comparableYList = comparableYResolver.resolve(listType);
        TypeInfo comparableYExtendsList = comparableYResolver.resolve(extendsListType);
        TypeInfo comparableYSuperList = comparableYResolver.resolve(superListType);

        TypeInfo xArrayList = xArrayContainerResolver.resolve(listType);
        TypeInfo xArrayExtendsList = xArrayContainerResolver.resolve(extendsListType);
        TypeInfo xArraySuperList = xArrayContainerResolver.resolve(superListType);

        TypeInfo extendsList = extendsZArrayResolver.resolve(listType);
        TypeInfo extendsExtendsList = extendsZArrayResolver.resolve(extendsListType);
        TypeInfo extendsSuperList = extendsZArrayResolver.resolve(superListType);

        // then
        assertType(comparableYList,        new TypeToken<List<Comparable<?>>>(){ }.getType());
        assertType(comparableYExtendsList, new TypeToken<List<? extends Comparable<?>>>(){ }.getType());
        assertType(comparableYSuperList,   new TypeToken<List<? super Comparable<?>>>(){ }.getType());

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
        TypeInfo typeInfo = new TypeInfo(ClassWithTypeVariablesContainer.class)
            .resolve(ClassWithTypeVariablesContainer.class.getDeclaredField("foo").getGenericType());

        Type listType = TypedContainer.class.getDeclaredField("list").getGenericType();
        Type extendsListType = TypedContainer.class.getDeclaredField("extendsList").getGenericType();
        Type superListType = TypedContainer.class.getDeclaredField("superList").getGenericType();

        TypeInfo xArrayContainerResolver = typeInfo.resolve(
            ClassWithTypeVariables.class.getDeclaredField("xArrayContainer").getGenericType());
        TypeInfo extendsZArrayResolver = typeInfo.resolve(
            ClassWithTypeVariables.class.getDeclaredField("listExtendsZArray").getGenericType());

        // when
        Type xArrayList = xArrayContainerResolver.resolve(listType).getType();
        Type xArrayExtendsList = xArrayContainerResolver.resolve(extendsListType).getType();
        Type xArraySuperList = xArrayContainerResolver.resolve(superListType).getType();

        Type extendsList = extendsZArrayResolver.resolve(listType).getType();
        Type extendsExtendsList = extendsZArrayResolver.resolve(extendsListType).getType();
        Type extendsSuperList = extendsZArrayResolver.resolve(superListType).getType();

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

    @Test
    void shouldResolveTypesFromNestedTypes() throws NoSuchFieldException {
        // given
        Type type = new TypeToken<ClassWithTypeVariablesExt<Integer, AccessMode>>() {}.getType();
        TypeInfo typeInfo = new TypeInfo(type);

        Type nestedContainerType = ClassWithTypeVariables.class.getDeclaredField("nestedContainer").getGenericType();
        Type listType = TypedContainer.class.getDeclaredField("list").getGenericType();
        Type extendsListType = TypedContainer.class.getDeclaredField("extendsList").getGenericType();
        Type superListType = TypedContainer.class.getDeclaredField("superList").getGenericType();

        // when
        TypeInfo nestedContainerInfo = typeInfo.resolve(nestedContainerType);

        TypeInfo listResolved = nestedContainerInfo.resolve(listType);
        TypeInfo extendsListResolved = nestedContainerInfo.resolve(extendsListType);
        TypeInfo superListResolved = nestedContainerInfo.resolve(superListType);

        // then
        assertType(nestedContainerInfo, new TypeToken<TypedContainer<TypedContainer<? extends AccessMode>>>(){ }.getType());
        assertType(listResolved,        new TypeToken<List<TypedContainer<? extends AccessMode>>>(){ }.getType());
        assertType(extendsListResolved, new TypeToken<List<? extends TypedContainer<? extends AccessMode>>>(){ }.getType());
        assertType(superListResolved,   new TypeToken<List<? super   TypedContainer<? extends AccessMode>>>(){ }.getType());
    }

    private static TypeInfo createChildTypeInfo(TypeInfo parentTypeInfo,
                                                Class<?> clazz, String fieldName) {
        try {
            return parentTypeInfo.resolve(clazz.getDeclaredField(fieldName).getGenericType());
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(clazz + " - " + fieldName, e);
        }
    }

    private static void assertType(TypeInfo actualTypeInfo, Type expectedType) {
        assertEquals(actualTypeInfo.getType(), expectedType);
    }

    private static void assertIsParameterizedType(TypeInfo actualType, Class<?> expectedRawType, Type... expectedTypeArguments) {
        assertIsParameterizedType(actualType.getType(), expectedRawType, expectedTypeArguments);
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