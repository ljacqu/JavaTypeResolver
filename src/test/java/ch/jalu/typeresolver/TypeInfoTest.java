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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link TypeInfo}.
 */
class TypeInfoTest {

    @Test
    void shouldReturnTypeArgumentInfo() {
        assertEquals(getType("stringList").getTypeArgumentInfo(0),
            new TypeInfo(String.class));
        assertEquals(getType("numberIntegerMap").getTypeArgumentInfo(1),
            new TypeInfo(Integer.class));
        assertEquals(getType("stringListSet").getTypeArgumentInfo(0),
            getType("stringList"));
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
        assertEquals(getType("stringList").getTypeArgumentAsClass(0), String.class);
        assertEquals(getType("stringListSet").getTypeArgumentAsClass(0), List.class);
        assertEquals(getType("numberIntegerMap").getTypeArgumentAsClass(1), Integer.class);
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
        assertEquals(mapInfo, new TypeReference<Map<String, Double>>() { });
        assertEquals(abstractMapInfo, new TypeReference<AbstractMap<String, Double>>() { });
        assertNull(collectionInfo);
    }

    @Test
    void shouldResolveSuperclass2() {
        // given
        TypeInfo typeInfo = new TypeReference<ArrayList<ArrayList<String>>>() {};

        // when
        TypeInfo iterableInfo = typeInfo.resolveSuperclass(Iterable.class);
        TypeInfo abstrCollInfo = typeInfo.resolveSuperclass(AbstractCollection.class);

        // then
        assertEquals(iterableInfo, new TypeReference<Iterable<ArrayList<String>>>() { });
        assertEquals(abstrCollInfo, new TypeReference<AbstractCollection<ArrayList<String>>>() { });
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
        assertEquals(oneArgProcessorFromString, new TypeReference<OneArgProcessor<String>>() { });
        assertNull(abstrTwoArgProcessorFromString);
        assertEquals(oneArgProcessorFromInt, new TypeReference<OneArgProcessor<BigDecimal>>() { }); // BigDecimal is expected
        assertEquals(abstrTwoArgProcessorFromInt, new TypeReference<AbstractTwoArgProcessor<Integer, Double>>() { });
    }

    @Test
    void shouldResolveParentType() {
        // given
        TypeInfo typeInfo = new TypeInfo(InnerParameterizedClassesContainerExt.class);

        // when
        TypeInfo result = typeInfo.resolveSuperclass(InnerParameterizedClassesContainer.class);

        // then
        assertEquals(result, new TypeReference<InnerParameterizedClassesContainer<Integer>>() { });
    }

    @Test
    void shouldResolveSuperclassOfParameterizedTypeIncludingOwner() {
        // given
        TypeInfo typeInfo = new TypeInfo(InnerParameterizedClassesContainerExt.TypedInnerClassExt.class);

        // when
        TypeInfo nestedClassInfo = typeInfo.resolveSuperclass(InnerParameterizedClassesContainer.TypedInnerClass.class);

        // then
        assertEquals(nestedClassInfo, new TypeReference<InnerParameterizedClassesContainer<Integer>.TypedInnerClass<String>>() { });
    }

    @Test
    void shouldResolveSuperclassOfParameterizedTypeIncludingOwnerRecursively() throws NoSuchFieldException {
        // given
        TypeInfo typeInfo = new TypeInfo(InnerParameterizedClassesContainerExt.class.getDeclaredField("nestedInner").getGenericType());

        // when
        TypeInfo typeNestedInnerInfo = typeInfo.resolveSuperclass(
            InnerParameterizedClassesContainer.TypedNestedClass.TypedNestedInnerClass.class);

        // then
        assertEquals(typeNestedInnerInfo, new TypeReference<InnerParameterizedClassesContainer.TypedNestedClass<Float>.TypedNestedInnerClass<Double>>() { });
    }

    @Test
    void shouldResolveSuperclassOfParameterizedTypeOfExtensionInSeparateTopLevelClass() {
        // given
        TypeInfo typeInfo = new TypeInfo(TypeNestedClassExtStandalone.class);

        // when
        TypeInfo nestedClassInfo = typeInfo.resolveSuperclass(InnerParameterizedClassesContainer.TypedNestedClass.class);

        // then
        // check both ways to be explicit but to also guarantee our expectation is the right thing
        assertEquals(nestedClassInfo, new TypeReference<InnerParameterizedClassesContainer.TypedNestedClass<Float>>() { });
        assertEquals(nestedClassInfo, of(TypeNestedClassExtStandalone.class.getGenericSuperclass()));
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
        assertEquals(nestedClassInfo, new TypeReference<InnerParameterizedClassesContainer.TypedNestedClass<String>>() { });
        assertEquals(nestedClassInfo, of(TypeNestedClassExtStandalone.NestedTypeNestedClassExtStandalone.class.getGenericSuperclass()));
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
        assertEquals(nestedClassInfo, of(Float.class));

        // Check that our expectation also reflects reality
        TypeInfo typeNestedClassInfo = of(TypeNestedClassExtStandalone.NestedTypeNestedClassNoParent.class.getEnclosingClass().getGenericSuperclass());
        assertEquals(Float.class, typeNestedClassInfo.getTypeArgumentAsClass(0));
    }

    @Test
    void shouldResolveSuperclassIncludingOwnerInMultipleNestedClass() {
        // given
        TypeInfo typeInfo = new TypeInfo(AdditionalNestedClassExt.Intermediate.TypedInnerClassExt.class);

        // when
        TypeInfo innerClassInfo = typeInfo.resolveSuperclass(InnerParameterizedClassesContainer.TypedInnerClass.class);

        // then
        // check both ways to be explicit but to also guarantee our expectation is the right thing
        assertEquals(innerClassInfo, new TypeReference<InnerParameterizedClassesContainer<Double>.TypedInnerClass<String>>() { });
        assertEquals(innerClassInfo, of(AdditionalNestedClassExt.Intermediate.TypedInnerClassExt.class.getGenericSuperclass()));
    }

    @Test
    void shouldResolveSimpleSuperclasses() {
        // given
        TypeInfo string = new TypeInfo(String.class);
        TypeInfo enumType = new TypeInfo(TimeUnit.class);
        TypeInfo doubleArray = new TypeInfo(double[].class);

        // when / then
        assertEquals(string.resolveSuperclass(String.class), of(String.class));
        assertEquals(string.resolveSuperclass(Object.class), of(Object.class));
        assertEquals(enumType.resolveSuperclass(Serializable.class), of(Serializable.class));
        assertEquals(doubleArray.resolveSuperclass(double[].class), of(double[].class));
        assertEquals(doubleArray.resolveSuperclass(Object.class), of(Object.class));
    }

    @Test
    void shouldResolveArraySuperclasses() {
        // given
        TypeInfo double3dArray = new TypeInfo(double[][][].class);
        TypeInfo stringArr = new TypeInfo(String[].class);
        TypeInfo list2dArray = new TypeReference<ArrayList<Short>[][]>() { };

        // when / then
        assertEquals(double3dArray.resolveSuperclass(Object[][].class), of(Object[][].class));
        assertEquals(double3dArray.resolveSuperclass(Object[].class), of(Object[].class));
        assertEquals(double3dArray.resolveSuperclass(Object.class), of(Object.class));

        assertEquals(stringArr.resolveSuperclass(Object[].class), of(Object[].class));
        assertEquals(stringArr.resolveSuperclass(Serializable[].class), of(Serializable[].class));

        assertEquals(list2dArray.resolveSuperclass(List[][].class), new TypeReference<List<Short>[][]>(){ });
        assertEquals(list2dArray.resolveSuperclass(AbstractList[][].class), new TypeReference<AbstractList<Short>[][]>(){ });
        assertEquals(list2dArray.resolveSuperclass(RandomAccess[][].class), of(RandomAccess[][].class));
        assertEquals(list2dArray.resolveSuperclass(Object[].class), of(Object[].class));
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