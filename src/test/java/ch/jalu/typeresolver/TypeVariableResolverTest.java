package ch.jalu.typeresolver;

import ch.jalu.typeresolver.reference.TypeReference;
import ch.jalu.typeresolver.samples.nestedclasses.InnerParameterizedClassesContainer;
import ch.jalu.typeresolver.samples.nestedclasses.InnerParameterizedClassesContainerExt;
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
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

import static ch.jalu.typeresolver.ParameterizedTypeTestUtil.isParameterizedType;
import static ch.jalu.typeresolver.ParameterizedTypeTestUtil.ofParameterizedType;
import static ch.jalu.typeresolver.TypeInfo.of;
import static ch.jalu.typeresolver.typeimpl.WildcardTypeImpl.newWildcardExtends;
import static ch.jalu.typeresolver.typeimpl.WildcardTypeImpl.newWildcardSuper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

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
        assertThat(processArgMethodType.getType(), equalTo(BigDecimal.class));
        assertThat(resolvedTuMapTypeInfo.toClass(), equalTo(Map.class));
        assertThat(resolvedTuMapTypeInfo.getTypeArgumentAsClass(0), equalTo(Integer.class));
        assertThat(resolvedTuMapTypeInfo.getTypeArgumentAsClass(1), equalTo(Double.class));
    }

    @Test
    void shouldReturnWildcardTypeIfItCannotBeResolved() throws NoSuchFieldException {
        // given
        TypeVariableResolver resolver = new TypeVariableResolver(IntegerDoubleArgProcessor.class);
        Field optionalValueField = Optional.class.getDeclaredField("value");

        // when
        Type resolvedType = resolver.resolve(optionalValueField.getGenericType());

        // then
        assertThat(resolvedType, equalTo(optionalValueField.getGenericType()));
    }

    @Test
    void shouldReturnArgumentIfItIsAClass() {
        // given
        TypeVariableResolver resolver = new TypeVariableResolver(IntegerDoubleArgProcessor.class);

        // when / then
        assertThat(resolver.resolve(String.class), equalTo(String.class));
        assertThat(resolver.resolve(int.class), equalTo(int.class));
        assertThat(resolver.resolve(BigDecimal[][].class), equalTo(BigDecimal[][].class));
        assertThat(resolver.resolve(char[].class), equalTo(char[].class));
        assertThat(resolver.resolve(null), nullValue());
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
        assertThat(tSetResolved, ofParameterizedType(Set.class, Character.class));
        assertThat(uListResolved, ofParameterizedType(List.class, Character.class));
        assertThat(tuMapResolved, ofParameterizedType(Map.class, Integer.class, Character.class));
        assertThat(processParamTypeResolved, ofParameterizedType(List.class, Float.class));

        assertThat(tSetResolved2.getType(), equalTo(new TypeReference<Set<Map<Float, Set<Float>>>>() { }.getType()));
        assertThat(tuMapResolved2.getType(), equalTo(new TypeReference<Map<Integer, Map<Float, Set<Float>>>>() { }.getType()));
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
        Type expectedOptionalType = new TypeReference<Optional<? extends Double>>() { }.getType();
        assertThat(resolvedOptionalType, equalTo(expectedOptionalType));
        Type expectedComparableType = new TypeReference<Comparable<? super Integer>>() { }.getType();
        assertThat(resolvedComparableType, equalTo(expectedComparableType));
    }

    @Test
    void shouldResolveQuestionMarkTypesRecursively() throws NoSuchFieldException {
        // given
        Type type = new TypeReference<AbstractTwoArgProcessor<? super Serializable, ? extends TimeUnit>>() { }.getType();
        TypeVariableResolver resolver = new TypeVariableResolver(type);
        Type tSuperComparable = AbstractTwoArgProcessor.class.getDeclaredField("tSuperComparable").getGenericType();
        Type uExtOptional = AbstractTwoArgProcessor.class.getDeclaredField("uExtOptional").getGenericType();

        // when
        Type comparableResolved = resolver.resolve(tSuperComparable);
        Type optionalResolved = resolver.resolve(uExtOptional);

        // then
        assertThat(comparableResolved, isParameterizedType(Comparable.class, newWildcardSuper(newWildcardSuper(Serializable.class))));
        assertThat(optionalResolved, isParameterizedType(Optional.class, newWildcardExtends(newWildcardExtends(TimeUnit.class))));
    }

    @Test
    void shouldHandleCombinationOfSuperAndExtends() throws NoSuchFieldException {
        // given
        Type type = new TypeReference<AbstractTwoArgProcessor<? extends Serializable, ? super TimeUnit>>() { }.getType();
        TypeVariableResolver resolver = new TypeVariableResolver(type);
        Type tSuperComparable = AbstractTwoArgProcessor.class.getDeclaredField("tSuperComparable").getGenericType();
        Type uExtOptional = AbstractTwoArgProcessor.class.getDeclaredField("uExtOptional").getGenericType();

        // when
        Type comparableResolved = resolver.resolve(tSuperComparable);
        Type optionalResolved = resolver.resolve(uExtOptional);

        // then
        // Comparable type is: Comparable<capture<? super capture<? extends Serializable>>>
        assertThat(comparableResolved, isParameterizedType(Comparable.class, newWildcardSuper(newWildcardExtends(Serializable.class))));

        // Optional type is: Optional<capture<? extends capture<? super TimeUnit>>>
        assertThat(optionalResolved, isParameterizedType(Optional.class, newWildcardExtends(newWildcardSuper(TimeUnit.class))));
    }

    @Test
    void shouldResolveComplexTypeVariables() throws NoSuchFieldException {
        // given
        Type type = new TypeReference<ClassWithTypeVariablesExt<Double, TimeUnit>>() {}.getType();
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
        assertThat(comparableYList,        equalTo(new TypeReference<List<Comparable<Double>>>() { }));
        assertThat(comparableYExtendsList, equalTo(new TypeReference<List<? extends Comparable<Double>>>() { }));
        assertThat(comparableYSuperList,   equalTo(new TypeReference<List<? super Comparable<Double>>>() { }));

        assertThat(xArrayList,        equalTo(new TypeReference<List<Double[][]>>() { }));
        assertThat(xArrayExtendsList, equalTo(new TypeReference<List<? extends Double[][]>>() { }));
        assertThat(xArraySuperList,   equalTo(new TypeReference<List<? super Double[][]>>() { }));

        assertThat(extendsList,        equalTo(new TypeReference<List<? extends TimeUnit[]>>() { }));
        assertThat(extendsExtendsList, ofParameterizedType(List.class, newWildcardExtends(newWildcardExtends(TimeUnit[].class))));
        assertThat(extendsSuperList,   ofParameterizedType(List.class, newWildcardSuper(newWildcardExtends(TimeUnit[].class))));
    }

    @Test
    void shouldHandleQuestionMarkTypeVariable() throws NoSuchFieldException {
        // given
        Type type = new TypeReference<ClassWithTypeVariablesExt<?, ? extends ChronoField>>() {}.getType();
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
        assertThat(comparableYList,        equalTo(new TypeReference<List<Comparable<?>>>(){ }));
        assertThat(comparableYExtendsList, equalTo(new TypeReference<List<? extends Comparable<?>>>(){ }));
        assertThat(comparableYSuperList,   equalTo(new TypeReference<List<? super Comparable<?>>>(){ }));

        Type wildcardDoubleArray = new GenericArrayTypeImpl(new GenericArrayTypeImpl(
            new WildcardTypeImpl(new Type[]{ Object.class }, new Type[0])));
        assertThat(xArrayList, ofParameterizedType(List.class, wildcardDoubleArray));
        assertThat(xArrayExtendsList, ofParameterizedType(List.class, newWildcardExtends(wildcardDoubleArray)));
        assertThat(xArraySuperList, ofParameterizedType(List.class, newWildcardSuper(wildcardDoubleArray)));

        WildcardType wildcardExtendsArrayOfExtendsChronoField = newWildcardExtends(
            new GenericArrayTypeImpl(newWildcardExtends(ChronoField.class)));
        assertThat(extendsList, ofParameterizedType(List.class, wildcardExtendsArrayOfExtendsChronoField));
        assertThat(extendsExtendsList, ofParameterizedType(List.class, newWildcardExtends(wildcardExtendsArrayOfExtendsChronoField)));
        assertThat(extendsSuperList, ofParameterizedType(List.class, newWildcardSuper(wildcardExtendsArrayOfExtendsChronoField)));
    }

    @Test
    void shouldResolveArrayTypesToTypeVariableArrayType() throws NoSuchFieldException {
        // given
        TypeInfo typeInfo = new TypeInfo(ClassWithTypeVariablesContainer.class)
            .resolve(ClassWithTypeVariablesContainer.class.getDeclaredField("foo").getGenericType());

        Type listType = TypedContainer.class.getDeclaredField("list").getGenericType();
        Type extendsListType = TypedContainer.class.getDeclaredField("extendsList").getGenericType();
        Type superListType = TypedContainer.class.getDeclaredField("superList").getGenericType();

        TypeInfo xArrayContainerType = typeInfo.resolve(
            ClassWithTypeVariables.class.getDeclaredField("xArrayContainer").getGenericType());
        TypeInfo extendsZArrayType = typeInfo.resolve(
            ClassWithTypeVariables.class.getDeclaredField("listExtendsZArray").getGenericType());

        // when
        Type xArrayList = xArrayContainerType.resolve(listType).getType();
        Type xArrayExtendsList = xArrayContainerType.resolve(extendsListType).getType();
        Type xArraySuperList = xArrayContainerType.resolve(superListType).getType();

        Type extendsList = extendsZArrayType.resolve(listType).getType();
        Type extendsExtendsList = extendsZArrayType.resolve(extendsListType).getType();
        Type extendsSuperList = extendsZArrayType.resolve(superListType).getType();

        // then
        Type xTypeVariable = ClassWithTypeVariablesContainer.class.getTypeParameters()[0];
        Type wildcardDoubleArray = new GenericArrayTypeImpl(new GenericArrayTypeImpl(xTypeVariable));
        assertThat(xArrayList, isParameterizedType(List.class, wildcardDoubleArray));
        assertThat(xArrayExtendsList, isParameterizedType(List.class, newWildcardExtends(wildcardDoubleArray)));
        assertThat(xArraySuperList, isParameterizedType(List.class, newWildcardSuper(wildcardDoubleArray)));

        WildcardType wildcardExtendsGArrayType = newWildcardExtends(
            new GenericArrayTypeImpl(ClassWithTypeVariablesContainer.class.getTypeParameters()[1]));
        assertThat(extendsList, isParameterizedType(List.class, wildcardExtendsGArrayType));
        assertThat(extendsExtendsList, isParameterizedType(List.class, newWildcardExtends(wildcardExtendsGArrayType)));
        assertThat(extendsSuperList, isParameterizedType(List.class, newWildcardSuper(wildcardExtendsGArrayType)));
    }

    @Test
    void shouldResolveTypesFromNestedTypes() throws NoSuchFieldException {
        // given
        Type type = new TypeReference<ClassWithTypeVariablesExt<Integer, AccessMode>>() {}.getType();
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
        assertThat(nestedContainerInfo, equalTo(new TypeReference<TypedContainer<TypedContainer<? extends AccessMode>>>(){ }));
        assertThat(listResolved,        equalTo(new TypeReference<List<TypedContainer<? extends AccessMode>>>(){ }));
        assertThat(extendsListResolved, equalTo(new TypeReference<List<? extends TypedContainer<? extends AccessMode>>>(){ }));
        assertThat(superListResolved,   equalTo(new TypeReference<List<? super   TypedContainer<? extends AccessMode>>>(){ }));
    }

    @Test
    void shouldResolveTypeFromEnclosingClass() throws NoSuchFieldException {
        // given
        TypeInfo typeInfo = new TypeReference<InnerParameterizedClassesContainerExt.TypedInnerClassExt.InnerInnerClassExt<Cloneable>>() { };
        Type tField = InnerParameterizedClassesContainer.TypedInnerClass.InnerInnerClass.class.getDeclaredField("tField").getGenericType();
        Type pField = InnerParameterizedClassesContainer.TypedInnerClass.InnerInnerClass.class.getDeclaredField("pField").getGenericType();

        // when
        TypeInfo tFieldRes = typeInfo.resolve(tField);
        TypeInfo pFieldRes = typeInfo.resolve(pField);

        // then
        assertThat(tFieldRes, equalTo(of(String.class)));
        assertThat(pFieldRes, equalTo(of(Integer.class)));
    }

    private static TypeInfo createChildTypeInfo(TypeInfo parentTypeInfo,
                                                Class<?> clazz, String fieldName) {
        try {
            return parentTypeInfo.resolve(clazz.getDeclaredField(fieldName).getGenericType());
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(clazz + " - " + fieldName, e);
        }
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