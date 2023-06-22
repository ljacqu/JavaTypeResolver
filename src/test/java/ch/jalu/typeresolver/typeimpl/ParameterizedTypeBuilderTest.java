package ch.jalu.typeresolver.typeimpl;

import ch.jalu.typeresolver.TypeInfo;
import ch.jalu.typeresolver.reference.TypeReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BiFunction;

import static ch.jalu.typeresolver.typeimpl.ParameterizedTypeBuilder.parameterizedTypeBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link ParameterizedTypeBuilder}.
 */
class ParameterizedTypeBuilderTest {

    @Test
    void shouldCreateParameterizedTypeFromClass() {
        // given / when
        ParameterizedTypeImpl comparablePt = parameterizedTypeBuilder(Comparable.class)
            .withTypeVariables()
            .build();
        ParameterizedTypeImpl mapPt = parameterizedTypeBuilder(Map.class)
            .withTypeVariables()
            .build();
        ParameterizedTypeImpl listPt = parameterizedTypeBuilder(List.class)
            .withTypeVariables()
            .build();

        // then
        assertThat(comparablePt.getActualTypeArguments(), arrayContaining(Comparable.class.getTypeParameters()[0]));
        assertThat(comparablePt.getRawType(), equalTo(Comparable.class));
        assertThat(comparablePt.getOwnerType(), nullValue());

        assertEqualToCreationViaGenericInterface(mapPt, Map.class, HashMap.class);
        assertEqualToCreationViaGenericInterface(listPt, List.class, ArrayList.class);
    }

    @Test
    void shouldThrowForRawTypeWithNoTypeParameters() {
        // given / when
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
            () -> parameterizedTypeBuilder(String.class));
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
            () -> parameterizedTypeBuilder(int.class));
        assertThrows(NullPointerException.class,
            () -> parameterizedTypeBuilder(null));

        // then
        assertThat(ex1.getMessage(), equalTo("Class 'class java.lang.String' has no type arguments"));
        assertThat(ex2.getMessage(), equalTo("Class 'int' has no type arguments"));
    }

    @Test
    void shouldCreateCollectionTypes() {
        // given / when
        ParameterizedType stringList = ParameterizedTypeBuilder.newCollectionType(List.class, String.class);
        ParameterizedType intHashSet = ParameterizedTypeBuilder.newCollectionType(HashSet.class, Integer.class);
        ParameterizedType linkedIntHashSetList = ParameterizedTypeBuilder.newCollectionType(LinkedList.class, intHashSet);

        // then
        assertThat(stringList, equalTo(new TypeReference<List<String>>() { }.getType()));
        assertThat(intHashSet, equalTo(new TypeReference<HashSet<Integer>>() { }.getType()));
        assertThat(linkedIntHashSetList, equalTo(new TypeReference<LinkedList<HashSet<Integer>>>() { }.getType()));
    }

    @Test
    void shouldCreateMapTypes() {
        // given / when
        ParameterizedType mapSi = ParameterizedTypeBuilder.newMapType(Map.class, String.class, Integer.class);
        ParameterizedType hashMapDb = ParameterizedTypeBuilder.newMapType(HashMap.class, Double.class, Boolean.class);
        ParameterizedType treeMapIh = ParameterizedTypeBuilder.newMapType(TreeMap.class, Integer.class, hashMapDb);

        // then
        assertThat(mapSi, equalTo(new TypeReference<Map<String, Integer>>() { }.getType()));
        assertThat(hashMapDb, equalTo(new TypeReference<HashMap<Double, Boolean>>() { }.getType()));
        assertThat(treeMapIh, equalTo(new TypeReference<TreeMap<Integer, HashMap<Double, Boolean>>>() { }.getType()));
    }

    @Test
    void shouldHandleNullForCreatingCollectionOrMapTypes() {
        // given / when
        ParameterizedType list = ParameterizedTypeBuilder.newCollectionType(ArrayList.class, null);
        ParameterizedType map1 = ParameterizedTypeBuilder.newMapType(Map.class, Integer.class, null);
        ParameterizedType map2 = ParameterizedTypeBuilder.newMapType(LinkedHashMap.class, null, null);

        // then
        assertThat(list, equalTo(parameterizedTypeBuilder(ArrayList.class).withTypeVariables().build()));
        assertThat(map1, equalTo(parameterizedTypeBuilder(Map.class).withTypeVariables().withTypeArg(0, Integer.class).build()));
        assertThat(map2, equalTo(parameterizedTypeBuilder(LinkedHashMap.class).withTypeVariables().build()));
    }

    /**
     * The calls in this test don't make any sense; this is just to verify that nothing "horrible" happens :)
     */
    @Test
    void shouldSomehowHandleWrongBaseTypesForCollectionAndMap() {
        // given
        class StringKeyMap<V> extends HashMap<String, V> {
        }

        // when
        ParameterizedType optional = ParameterizedTypeBuilder.newCollectionType((Class) Optional.class, String.class);
        ParameterizedTypeImpl mapEntry = ParameterizedTypeBuilder.newMapType((Class) Map.Entry.class, Double.class, String.class);

        assertThrows(NullPointerException.class, () -> ParameterizedTypeBuilder.newCollectionType(null, String.class));
        assertThrows(NullPointerException.class, () -> ParameterizedTypeBuilder.newMapType(null, String.class, Integer.class));

        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
            () -> ParameterizedTypeBuilder.newCollectionType((Class) String.class, Integer.class));
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
            () -> ParameterizedTypeBuilder.newMapType((Class) String.class, Double.class, Integer.class));
        IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class,
            () -> ParameterizedTypeBuilder.newMapType(StringKeyMap.class, Double.class, Integer.class));

        // then
        assertThat(optional, equalTo(new TypeReference<Optional<String>>() { }.getType()));
        assertThat(mapEntry, equalTo(new TypeReference<Map.Entry<Double, String>>() { }.getType()));

        assertThat(ex1.getMessage(), equalTo("Class 'class java.lang.String' has no type arguments"));
        assertThat(ex2.getMessage(), equalTo("Class 'class java.lang.String' has no type arguments"));
        assertThat(ex3.getMessage(), equalTo("Type parameter index 1 is out of bounds for class ch.jalu.typeresolver.typeimpl.ParameterizedTypeBuilderTest$1StringKeyMap"));
    }

    @Test
    void shouldCreateTypesBasedOnClassAndIndexedArgs() {
        // given / when
        ParameterizedTypeImpl comparable = parameterizedTypeBuilder(Comparable.class)
            .withTypeArg(0, Integer.class)
            .build();
        ParameterizedTypeImpl map = parameterizedTypeBuilder(Map.class)
            .withTypeArg(0, String.class)
            .withTypeArg(1, TypeInfo.of(comparable))
            .build();

        // then
        assertThat(comparable, equalTo(new TypeReference<Comparable<Integer>>() { }.getType()));
        assertThat(map, equalTo(new TypeReference<Map<String, Comparable<Integer>>>() { }.getType()));
    }

    @Test
    void shouldCreateTypeBasedOnExistingParameterizedType() {
        // given
        ParameterizedType comparableInterface = (ParameterizedType) Integer.class.getGenericInterfaces()[0];

        // when
        ParameterizedTypeImpl comparableComparable = new ParameterizedTypeBuilder(comparableInterface)
            .withTypeArg("T", comparableInterface)
            .build();
        ParameterizedTypeImpl comparableExtNumber = new ParameterizedTypeBuilder(comparableComparable)
            .withTypeArg("T", new TypeInfo(WildcardTypeImpl.newWildcardExtends(Number.class)))
            .build();

        // then
        assertThat(comparableComparable, equalTo(new TypeReference<Comparable<Comparable<Integer>>>() { }.getType()));
        assertThat(comparableExtNumber, equalTo(new TypeReference<Comparable<? extends Number>>() { }.getType()));
    }

    @Test
    void shouldCreateTypeWithTypeVariableArgs() {
        // given
        ParameterizedType otherBiFunctionType = (ParameterizedType) new TypeReference<BiFunction<Character, Character, String>>() { }.getType();
        TypeVariable<?>[] typeParams = BiFunction.class.getTypeParameters();

        // when
        ParameterizedTypeImpl builtBiFunctionType = new ParameterizedTypeBuilder(otherBiFunctionType)
            .withTypeArg(typeParams[0], new TypeInfo(String.class))
            .withTypeArg(typeParams[1], ParameterizedTypeBuilder.newCollectionType(List.class, Integer.class))
            .withTypeArg(typeParams[2], Double.class)
            .build();

        // then
        assertThat(builtBiFunctionType, equalTo(new TypeReference<BiFunction<String, List<Integer>, Double>>(){ }.getType()));
    }

    @Test
    void shouldThrowForUnmatchedTypeParams() {
        // given
        TypeVariable<?>[] typeParams = BiFunction.class.getTypeParameters();
        ParameterizedTypeBuilder mapBuilder = parameterizedTypeBuilder(Map.class);

        // when
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
            () -> mapBuilder.withTypeArg(2, String.class));
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
            () -> mapBuilder.withTypeArg(-1, String.class));
        IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class,
            () -> mapBuilder.withTypeArg("Q", String.class));
        IllegalArgumentException ex4 = assertThrows(IllegalArgumentException.class,
            () -> mapBuilder.withTypeArg(typeParams[0], String.class));

        // then
        assertThat(ex1.getMessage(), equalTo("Type parameter index 2 is out of bounds for interface java.util.Map"));
        assertThat(ex2.getMessage(), equalTo("Type parameter index -1 is out of bounds for interface java.util.Map"));
        assertThat(ex3.getMessage(), equalTo("No type parameter 'Q' on interface java.util.Map"));
        assertThat(ex4.getMessage(), equalTo("No type parameter matched 'T' on interface java.util.Map"));
    }

    @Test
    void shouldSetOriginalTypeParameters() {
        // given
        ParameterizedType pt = (ParameterizedType) new TypeReference<BiFunction<String, Double, Long>>() { }.getType();
        ParameterizedTypeBuilder builder1 = new ParameterizedTypeBuilder(pt);
        ParameterizedTypeBuilder builder2 = new ParameterizedTypeBuilder(pt);

        // when
        ParameterizedTypeImpl result1 = builder1
            .withTypeVariables()
            .build();
        ParameterizedTypeImpl result2 = builder2
            .withTypeArg(0, (Type) null)
            .withTypeArg(BiFunction.class.getTypeParameters()[1], (Type) null)
            .withTypeArg("R", (Type) null)
            .build();

        // then
        ParameterizedTypeImpl biFunctionWithTypeVars = new ParameterizedTypeImpl(BiFunction.class, null,
            BiFunction.class.getTypeParameters());
        assertThat(result1, equalTo(biFunctionWithTypeVars));
        assertThat(result2, equalTo(biFunctionWithTypeVars));
    }

    @Test
    void shouldThrowIfTypeParameterHasNotBeenSet() {
        // given
        ParameterizedTypeBuilder builder = parameterizedTypeBuilder(Map.class);

        // when
        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);

        // then
        assertThat(ex.getMessage(), equalTo("Type parameter 'K' at index 0 has not been set"));
    }

    private void assertEqualToCreationViaGenericInterface(ParameterizedTypeImpl typeToCheck,
                                                          Class<?> rawType, Class<?> extendingType) {
        ParameterizedType expectedType = Arrays.stream(extendingType.getGenericInterfaces())
            .filter(intf -> intf instanceof ParameterizedType && ((ParameterizedType) intf).getRawType().equals(rawType))
            .map(intf -> (ParameterizedType) intf)
            .findFirst().get();

        assertThat(typeToCheck.getRawType(), equalTo(expectedType.getRawType()));
        assertThat(typeToCheck.getOwnerType(), equalTo(expectedType.getOwnerType()));

        Type[] expectedArgs = expectedType.getActualTypeArguments();
        Type[] actualArgs = typeToCheck.getActualTypeArguments();
        assertThat(actualArgs.length, equalTo(expectedArgs.length));

        for (int i = 0; i < actualArgs.length; i++) {
            TypeVariable<?> actualArg = (TypeVariable<?>) actualArgs[i];
            TypeVariable<?> expectedArg = (TypeVariable<?>) expectedArgs[i];

            assertThat(actualArg.getName(), equalTo(expectedArg.getName()));
            assertThat(actualArg.getBounds(), equalTo(expectedArg.getBounds()));
            // Annotated bounds not checked for now
            assertThat(actualArg.getGenericDeclaration(), equalTo(rawType));
        }
    }
}