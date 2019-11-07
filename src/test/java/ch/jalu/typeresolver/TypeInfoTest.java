package ch.jalu.typeresolver;

import ch.jalu.typeresolver.reference.TypeReference;
import ch.jalu.typeresolver.samples.nestedclasses.AdditionalNestedClassExt;
import ch.jalu.typeresolver.samples.nestedclasses.InnerParameterizedClassesContainer;
import ch.jalu.typeresolver.samples.nestedclasses.InnerParameterizedClassesContainerExt;
import ch.jalu.typeresolver.samples.nestedclasses.TypeNestedClassExtStandalone;
import ch.jalu.typeresolver.samples.typeinheritance.AbstractTwoArgProcessor;
import ch.jalu.typeresolver.samples.typeinheritance.IntegerDoubleArgProcessorExtension;
import ch.jalu.typeresolver.samples.typeinheritance.IntegerGenericArgProcessor;
import ch.jalu.typeresolver.samples.typeinheritance.OneArgProcessor;
import ch.jalu.typeresolver.samples.typeinheritance.StringArgProcessorExtension;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static ch.jalu.typeresolver.TypeInfo.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link TypeInfo}.
 */
class TypeInfoTest {

    @Test
    void shouldReturnTypeArgumentInfo() {
        assertThat(getType("stringList").getTypeArgumentInfo(0),
            equalTo(of(String.class)));
        assertThat(getType("numberIntegerMap").getTypeArgumentInfo(1),
            equalTo(of(Integer.class)));
        assertThat(getType("stringListSet").getTypeArgumentInfo(0),
            equalTo(new TypeReference<List<String>>() { }));
    }

    @Test
    void shouldCreateTypeInfoFromGenericFieldType() throws NoSuchFieldException {
        // given
        Field field = ParameterizedTypes.class.getDeclaredField("stringList");

        // when
        TypeInfo fieldTypeInfo = TypeInfo.of(field);

        // then
        assertThat(fieldTypeInfo, equalTo(new TypeReference<List<String>>() { }));
    }

    @Test
    void shouldReturnNullAsTypeArgumentIfNotApplicable() {
        assertNull(getType("stringList").getTypeArgumentInfo(1));
        assertNull(new TypeInfo(String.class).getTypeArgumentInfo(0));
        assertNull(new TypeInfo(null).getTypeArgumentInfo(1));
        assertNull(new TypeInfo(int.class).getTypeArgumentInfo(0));
    }

    @Test
    void shouldReturnTypeArgumentAsClass() {
        assertThat(getType("stringList").getTypeArgumentAsClass(0), equalTo(String.class));
        assertThat(getType("stringListSet").getTypeArgumentAsClass(0), equalTo(List.class));
        assertThat(getType("numberIntegerMap").getTypeArgumentAsClass(1), equalTo(Integer.class));
    }

    @Test
    void shouldReturnNullIfTypeArgumentAsClassIsNotApplicable() {
        assertNull(new TypeInfo(null).getTypeArgumentAsClass(0));
        assertNull(getType("questionMarkMap").getTypeArgumentAsClass(0));
    }

    @Test
    void shouldResolveSuperclass() {
        // given
        TypeInfo typeInfo = new TypeReference<HashMap<String, Double>>() {};

        // when
        TypeInfo mapInfo = typeInfo.resolveSuperclass(Map.class);
        TypeInfo abstractMapInfo = typeInfo.resolveSuperclass(AbstractMap.class);
        TypeInfo collectionInfo = typeInfo.resolveSuperclass(Collection.class);

        // then
        assertThat(mapInfo, equalTo(new TypeReference<Map<String, Double>>() { }));
        assertThat(abstractMapInfo, equalTo(new TypeReference<AbstractMap<String, Double>>() { }));
        assertThat(collectionInfo, nullValue());
    }

    @Test
    void shouldResolveSuperclass2() {
        // given
        TypeInfo typeInfo = new TypeReference<ArrayList<ArrayList<String>>>() {};

        // when
        TypeInfo iterableInfo = typeInfo.resolveSuperclass(Iterable.class);
        TypeInfo abstrCollInfo = typeInfo.resolveSuperclass(AbstractCollection.class);

        // then
        assertThat(iterableInfo, equalTo(new TypeReference<Iterable<ArrayList<String>>>() { }));
        assertThat(abstrCollInfo, equalTo(new TypeReference<AbstractCollection<ArrayList<String>>>() { }));
    }

    @Test
    void shouldResolveSuperclass3() {
        // given
        TypeInfo stringArgProcessor = new TypeInfo(StringArgProcessorExtension.class);
        TypeInfo intDoubleArgProcessor = new TypeInfo(IntegerDoubleArgProcessorExtension.class);

        // when
        TypeInfo oneArgProcessorFromString = stringArgProcessor.resolveSuperclass(OneArgProcessor.class);
        TypeInfo abstrTwoArgProcessorFromString = stringArgProcessor.resolveSuperclass(AbstractTwoArgProcessor.class);
        TypeInfo oneArgProcessorFromInt = intDoubleArgProcessor.resolveSuperclass(OneArgProcessor.class);
        TypeInfo abstrTwoArgProcessorFromInt = intDoubleArgProcessor.resolveSuperclass(AbstractTwoArgProcessor.class);

        // then
        assertThat(oneArgProcessorFromString, equalTo(new TypeReference<OneArgProcessor<String>>() { }));
        assertThat(abstrTwoArgProcessorFromString, nullValue());
        assertThat(oneArgProcessorFromInt, equalTo(new TypeReference<OneArgProcessor<BigDecimal>>() { })); // BigDecimal is expected
        assertThat(abstrTwoArgProcessorFromInt, equalTo(new TypeReference<AbstractTwoArgProcessor<Integer, Double>>() { }));
    }

    @Test
    void shouldResolveParentType() {
        // given
        TypeInfo typeInfo = new TypeInfo(InnerParameterizedClassesContainerExt.class);

        // when
        TypeInfo result = typeInfo.resolveSuperclass(InnerParameterizedClassesContainer.class);

        // then
        assertThat(result, equalTo(new TypeReference<InnerParameterizedClassesContainer<Integer>>() { }));
    }

    @Test
    void shouldResolveSuperclassOfParameterizedTypeIncludingOwner() {
        // given
        TypeInfo typeInfo = new TypeInfo(InnerParameterizedClassesContainerExt.TypedInnerClassExt.class);

        // when
        TypeInfo nestedClassInfo = typeInfo.resolveSuperclass(InnerParameterizedClassesContainer.TypedInnerClass.class);

        // then
        assertThat(nestedClassInfo, equalTo(new TypeReference<InnerParameterizedClassesContainer<Integer>.TypedInnerClass<String>>() { }));
    }

    @Test
    void shouldResolveSuperclassOfParameterizedTypeIncludingOwnerRecursively() throws NoSuchFieldException {
        // given
        TypeInfo typeInfo = new TypeInfo(InnerParameterizedClassesContainerExt.class.getDeclaredField("nestedInner").getGenericType());

        // when
        TypeInfo typeNestedInnerInfo = typeInfo.resolveSuperclass(
            InnerParameterizedClassesContainer.TypedNestedClass.TypedNestedInnerClass.class);

        // then
        assertThat(typeNestedInnerInfo, equalTo(new TypeReference<InnerParameterizedClassesContainer.TypedNestedClass<Float>.TypedNestedInnerClass<Double>>() { }));
    }

    @Test
    void shouldResolveSuperclassOfParameterizedTypeOfExtensionInSeparateTopLevelClass() {
        // given
        TypeInfo typeInfo = new TypeInfo(TypeNestedClassExtStandalone.class);

        // when
        TypeInfo nestedClassInfo = typeInfo.resolveSuperclass(InnerParameterizedClassesContainer.TypedNestedClass.class);

        // then
        // check both ways to be explicit but to also guarantee our expectation is the right thing
        assertThat(nestedClassInfo, equalTo(new TypeReference<InnerParameterizedClassesContainer.TypedNestedClass<Float>>() { }));
        assertThat(nestedClassInfo, equalTo(of(TypeNestedClassExtStandalone.class.getGenericSuperclass())));
    }

    /**
     * TypedNestedClass' type arguments could be inferred from the generic superclass or from the enclosing class.
     * In the case that both are present, the superclass wins, as demonstrated in
     * {@link TypeNestedClassExtStandalone.NestedTypeNestedClassExtStandalone#proofOfInferredTypeN()}.
     */
    @Test
    void shouldNotReturnEnclosingClassAsParameterizedTypeOwnerIfNotRelevant() {
        // given
        TypeInfo typeInfo = new TypeInfo(TypeNestedClassExtStandalone.NestedTypeNestedClassExtStandalone.class);

        // when
        TypeInfo nestedClassInfo = typeInfo.resolveSuperclass(InnerParameterizedClassesContainer.TypedNestedClass.class);

        // then
        // check both ways to be explicit but to also guarantee our expectation is the right thing
        assertThat(nestedClassInfo, equalTo(new TypeReference<InnerParameterizedClassesContainer.TypedNestedClass<String>>() { }));
        assertThat(nestedClassInfo, equalTo(of(TypeNestedClassExtStandalone.NestedTypeNestedClassExtStandalone.class.getGenericSuperclass())));
    }

    /**
     * TypeNestedClass, in contrast to {@link #shouldNotReturnEnclosingClassAsParameterizedTypeOwnerIfNotRelevant()}, is
     * inferred from the enclosing class, as demonstrated in {@link TypeNestedClassExtStandalone.NestedTypeNestedClassNoParent#proofOfInferredTypeN()}.
     */
    @Test
    void shouldResolveTypeArgumentFromEnclosingClassSupertype() {
        // given
        TypeInfo typeInfo = new TypeInfo(TypeNestedClassExtStandalone.NestedTypeNestedClassNoParent.class);

        // when
        TypeInfo nestedClassInfo = typeInfo.resolve(InnerParameterizedClassesContainer.TypedNestedClass.class.getTypeParameters()[0]);

        // then
        assertThat(nestedClassInfo, equalTo(of(Float.class)));

        // Check that our expectation also reflects reality
        TypeInfo typeNestedClassInfo = of(TypeNestedClassExtStandalone.NestedTypeNestedClassNoParent.class.getEnclosingClass().getGenericSuperclass());
        assertThat(typeNestedClassInfo.getTypeArgumentAsClass(0), equalTo(Float.class));
    }

    @Test
    void shouldResolveSuperclassIncludingOwnerInMultipleNestedClass() {
        // given
        TypeInfo typeInfo = new TypeInfo(AdditionalNestedClassExt.Intermediate.TypedInnerClassExt.class);

        // when
        TypeInfo innerClassInfo = typeInfo.resolveSuperclass(InnerParameterizedClassesContainer.TypedInnerClass.class);

        // then
        // check both ways to be explicit but to also guarantee our expectation is the right thing
        assertThat(innerClassInfo, equalTo(new TypeReference<InnerParameterizedClassesContainer<Double>.TypedInnerClass<String>>() { }));
        assertThat(innerClassInfo, equalTo(of(AdditionalNestedClassExt.Intermediate.TypedInnerClassExt.class.getGenericSuperclass())));
    }

    @Test
    void shouldResolveSimpleSuperclasses() {
        // given
        TypeInfo string = new TypeInfo(String.class);
        TypeInfo enumType = new TypeInfo(TimeUnit.class);
        TypeInfo doubleArray = new TypeInfo(double[].class);

        // when / then
        assertThat(string.resolveSuperclass(String.class), equalTo(of(String.class)));
        assertThat(string.resolveSuperclass(Object.class), equalTo(of(Object.class)));
        assertThat(enumType.resolveSuperclass(Serializable.class), equalTo(of(Serializable.class)));
        assertThat(doubleArray.resolveSuperclass(double[].class), equalTo(of(double[].class)));
        assertThat(doubleArray.resolveSuperclass(Object.class), equalTo(of(Object.class)));
    }

    @Test
    void shouldResolveArraySuperclasses() {
        // given
        TypeInfo double3dArray = new TypeInfo(double[][][].class);
        TypeInfo stringArr = new TypeInfo(String[].class);
        TypeInfo list2dArray = new TypeReference<ArrayList<Short>[][]>() { };

        // when / then
        assertThat(double3dArray.resolveSuperclass(Object[][].class), equalTo(of(Object[][].class)));
        assertThat(double3dArray.resolveSuperclass(Object[].class), equalTo(of(Object[].class)));
        assertThat(double3dArray.resolveSuperclass(Object.class), equalTo(of(Object.class)));

        assertThat(stringArr.resolveSuperclass(Object[].class), equalTo(of(Object[].class)));
        assertThat(stringArr.resolveSuperclass(Serializable[].class), equalTo(of(Serializable[].class)));

        assertThat(list2dArray.resolveSuperclass(List[][].class), equalTo(new TypeReference<List<Short>[][]>(){ }));
        assertThat(list2dArray.resolveSuperclass(AbstractList[][].class), equalTo(new TypeReference<AbstractList<Short>[][]>(){ }));
        assertThat(list2dArray.resolveSuperclass(RandomAccess[][].class), equalTo(of(RandomAccess[][].class)));
        assertThat(list2dArray.resolveSuperclass(Object[].class), equalTo(of(Object[].class)));
    }

    @Test
    void shouldReturnNullForNotApplicableSuperclass() {
        // given / when / then
        assertNull(of(String.class).resolveSuperclass(Collection.class));
        assertNull(of(double[].class).resolveSuperclass(Object[].class));
        assertNull(of(ArrayList.class).resolveSuperclass(Iterator.class));
        assertNull(of(List.class).resolveSuperclass(ArrayList.class));
    }

    @Test
    void shouldReturnAllTypes() {
        // given
        TypeInfo string = of(String.class);
        TypeInfo stringArray = of(String[].class);
        TypeInfo primitiveIntArr = of(int[].class);

        // when
        Set<Type> stringAll = string.getAllTypes();
        Set<Type> stringArrayAll = stringArray.getAllTypes();
        Set<TypeInfo> primitiveIntArrAll = primitiveIntArr.getAllTypeInfos();

        // then
        TypeReference<Comparable<String>> comparableString = new TypeReference<Comparable<String>>() { };
        assertThat(stringAll, containsInAnyOrder(String.class, CharSequence.class, comparableString.getType(), Serializable.class, Object.class));
        assertThat(stringArrayAll, containsInAnyOrder(
            String[].class, CharSequence[].class, Serializable[].class, new TypeReference<Comparable<String>[]>() { }.getType(), Object[].class,
            Object.class, Cloneable.class, Serializable.class));
        assertThat(primitiveIntArrAll, containsInAnyOrder(of(int[].class), of(Object.class), of(Cloneable.class), of(Serializable.class)));
    }

    @Test
    void shouldReturnIfIsAssignableToClass() {
        // given
        TypeInfo list = of(List.class);
        TypeInfo collection = of(Collection.class);
        Type genericList = new TypeReference<List<String>>() { }.getType();
        Type genericCollection = new TypeReference<Collection<String>>() { }.getType();

        // when / then
        assertTrue(list.isAssignableTo(Collection.class));
        assertFalse(list.isAssignableTo(genericCollection));
        assertTrue(list.isAssignableTo(List.class));
        assertFalse(list.isAssignableTo(genericList));

        assertFalse(collection.isAssignableTo(List.class));
        assertFalse(list.isAssignableTo(Set.class));
        assertFalse(list.isAssignableTo(ArrayList.class));
    }

    @Test
    void shouldReturnIfIsAssignableToParameterizedType() {
        // given
        TypeInfo genericList = new TypeReference<List<String>>() { };
        TypeInfo genericCollection = new TypeReference<Collection<String>>() { };

        // when / then
        assertTrue(genericList.isAssignableTo(List.class));
        assertTrue(genericList.isAssignableTo(Collection.class));
        assertTrue(genericList.isAssignableTo(genericList));
        assertTrue(genericList.isAssignableTo(genericCollection));

        assertFalse(genericList.isAssignableTo(Set.class));
        assertFalse(genericList.isAssignableTo(new TypeReference<List<Double>>() { }));
        assertFalse(genericList.isAssignableTo(new TypeReference<Collection<Float>>() { }));
    }

    @Test
    void shouldReturnIfIsAssignableWithDifferentParameterizedTypeParents() {
        // given
        TypeInfo intDblArgProcessor = of(IntegerDoubleArgProcessorExtension.class);
        TypeInfo bigDecimalOneArgProcessor = new TypeReference<OneArgProcessor<BigDecimal>>() {};
        TypeInfo stringOneArgProcessor = new TypeReference<OneArgProcessor<String>>() {};
        TypeInfo twoArgProcessor1 = new TypeReference<AbstractTwoArgProcessor<Integer, Double>>() {};
        TypeInfo twoArgProcessor2 = new TypeReference<AbstractTwoArgProcessor<Integer, String>>() {};
        TypeInfo twoArgProcessor3 = new TypeReference<AbstractTwoArgProcessor<String, Double>>() {};

        // when / then
        assertTrue(intDblArgProcessor.isAssignableTo(bigDecimalOneArgProcessor));
        assertFalse(intDblArgProcessor.isAssignableTo(stringOneArgProcessor));
        assertTrue(intDblArgProcessor.isAssignableTo(OneArgProcessor.class));

        assertTrue(intDblArgProcessor.isAssignableTo(twoArgProcessor1));
        assertTrue(intDblArgProcessor.isAssignableTo(AbstractTwoArgProcessor.class));
        assertFalse(intDblArgProcessor.isAssignableTo(twoArgProcessor2));
        assertFalse(intDblArgProcessor.isAssignableTo(twoArgProcessor3));
        assertTrue(intDblArgProcessor.isAssignableTo(IntegerGenericArgProcessor.class));
        assertTrue(intDblArgProcessor.isAssignableTo(new TypeReference<IntegerGenericArgProcessor<Double>>() { }));
        assertFalse(intDblArgProcessor.isAssignableTo(new TypeReference<IntegerGenericArgProcessor<BigDecimal>>() { }));
    }

    private static TypeInfo getType(String fieldName) {
        try {
            return new TypeInfo(ParameterizedTypes.class.getDeclaredField(fieldName).getGenericType());
        } catch (Exception e) {
            throw new IllegalStateException(fieldName, e);
        }
    }

    private static final class ParameterizedTypes<V> {
        private List<String> stringList;
        private Map<?, ?> questionMarkMap;
        private Set<List<String>> stringListSet;
        private Map<Number, Integer> numberIntegerMap;
    }
}