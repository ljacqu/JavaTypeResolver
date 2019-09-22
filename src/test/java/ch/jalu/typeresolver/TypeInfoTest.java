package ch.jalu.typeresolver;

import ch.jalu.typeresolver.reference.TypeReference;
import ch.jalu.typeresolver.samples.nestedclasses.InnerParameterizedClassesContainer;
import ch.jalu.typeresolver.samples.nestedclasses.InnerParameterizedClassesContainerExt;
import ch.jalu.typeresolver.samples.typeinheritance.AbstractTwoArgProcessor;
import ch.jalu.typeresolver.samples.typeinheritance.IntegerDoubleArgProcessorExtension;
import ch.jalu.typeresolver.samples.typeinheritance.OneArgProcessor;
import ch.jalu.typeresolver.samples.typeinheritance.StringArgProcessorExtension;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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