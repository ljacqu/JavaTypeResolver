package ch.jalu.typeresolver;

import ch.jalu.typeresolver.array.ArrayTypeUtils;
import ch.jalu.typeresolver.reference.TypeReference;
import ch.jalu.typeresolver.typeimpl.ParameterizedTypeImpl;
import ch.jalu.typeresolver.typeimpl.WildcardTypeImpl;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Watchable;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static ch.jalu.typeresolver.TypeInfo.of;
import static ch.jalu.typeresolver.typeimpl.ParameterizedTypeBuilder.parameterizedTypeBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;

/**
 * Test for {@link TypeVisitor}.
 */
class TypeVisitorTest {

    @Test
    void shouldReturnPrimitiveTypeOnly() {
        // given / when / then
        assertThat(TypeVisitor.gatherAllTypes(int.class), contains(int.class));
        assertThat(TypeVisitor.gatherAllTypes(double.class), contains(double.class));
        assertThat(TypeVisitor.gatherAllTypes(char.class), contains(char.class));
    }

    @Test
    void shouldReturnParentsAndInterfaces() {
        // given
        TypeInfo string = of(String.class);
        TypeInfo arrayList = new TypeReference<ArrayList<String>>() {};

        // when
        Set<Type> stringAll = string.getAllTypes();
        Set<TypeInfo> arrayListAll = arrayList.getAllTypeInfos();

        // then
        TypeReference<Comparable<String>> comparableString = new TypeReference<Comparable<String>>() { };
        List<Type> expectedStringTypes = new ArrayList<>(Arrays.asList(
            String.class, CharSequence.class, comparableString.getType(), Serializable.class, Object.class));

        JavaVersionHelper.getConstableClassIfApplicable().ifPresent(constableTypes -> {
            expectedStringTypes.add(constableTypes.getConstableClass());
            expectedStringTypes.add(constableTypes.getConstantDescClass());
        });
        assertThat(stringAll, containsTypesInAnyOrder(expectedStringTypes));

        List<TypeInfo> expectedArrayListTypes = new ArrayList<>(Arrays.asList(
            new TypeReference<ArrayList<String>>() { },
            new TypeReference<AbstractList<String>>() { },
            new TypeReference<AbstractCollection<String>>() { },
            new TypeReference<List<String>>() { },
            new TypeReference<Collection<String>>() { },
            new TypeReference<Iterable<String>>() { },
            of(Cloneable.class), of(Serializable.class), of(RandomAccess.class), of(Object.class)));

        JavaVersionHelper.getSequencedCollectionTypesIfApplicable().ifPresent(sequencedCollTypes -> {
            ParameterizedTypeImpl sequencedStringCollection =
                parameterizedTypeBuilder(sequencedCollTypes.getSequencedCollectionClass())
                    .withTypeArg(0, String.class)
                    .build();
            expectedArrayListTypes.add(TypeInfo.of(sequencedStringCollection));
        });
        assertThat(arrayListAll, containsTypeInfosInAnyOrder(expectedArrayListTypes));
    }

    @Test
    void shouldReturnAllTypesForArrays() {
        // given
        TypeInfo stringArray = of(String[].class);
        TypeInfo primitiveIntArr = of(int[].class);

        // when
        Set<Type> stringArrayAll = stringArray.getAllTypes();
        Set<Type> primitiveIntArrAll = primitiveIntArr.getAllTypes();

        // then
        List<Type> expectedStringArrayTypes = new ArrayList<>(Arrays.asList(
            String[].class, CharSequence[].class, Serializable[].class, new TypeReference<Comparable<String>[]>() { }.getType(), Object[].class,
            Object.class, Cloneable.class, Serializable.class));
        JavaVersionHelper.getConstableClassIfApplicable().ifPresent(constableTypes -> {
            expectedStringArrayTypes.add(ArrayTypeUtils.createArrayType(constableTypes.getConstableClass()));
            expectedStringArrayTypes.add(ArrayTypeUtils.createArrayType(constableTypes.getConstantDescClass()));
        });
        assertThat(stringArrayAll, containsTypesInAnyOrder(expectedStringArrayTypes));

        assertThat(primitiveIntArrAll, containsInAnyOrder(int[].class, Object.class, Cloneable.class, Serializable.class));
    }

    @Test
    void shouldHandleMultiDimensionalArrays() {
        // given
        TypeInfo list2dArray = new TypeReference<List<String>[][]>() { };
        TypeInfo float3dArray = of(float[][][].class);

        // when
        Set<TypeInfo> list2dArrayAll = list2dArray.getAllTypeInfos();
        Set<Type> float3dArrayAll = float3dArray.getAllTypes();

        // then
        List<TypeInfo> expectedListTypeInfos = new ArrayList<>(Arrays.asList(
            new TypeReference<List<String>[][]>() { },
            new TypeReference<Collection<String>[][]>() { },
            new TypeReference<Iterable<String>[][]>() { },
            new TypeReference<Object[][]>() { },
            of(Serializable[].class), of(Cloneable[].class), of(Object[].class),
            of(Serializable.class), of(Cloneable.class), of(Object.class)));

        JavaVersionHelper.getSequencedCollectionTypesIfApplicable().ifPresent(sequencedCollTypes -> {
            // java.util.SequencedCollection<java.lang.String>[][]
            ParameterizedTypeImpl sequencedStringCollection =
                parameterizedTypeBuilder(sequencedCollTypes.getSequencedCollectionClass())
                    .withTypeArg(0, String.class)
                    .build();
            Type sequencedColl2dType = ArrayTypeUtils.createArrayType(sequencedStringCollection, 2);
            expectedListTypeInfos.add(new TypeInfo(sequencedColl2dType));
        });

        assertThat(list2dArrayAll, containsTypeInfosInAnyOrder(expectedListTypeInfos));

        assertThat(float3dArrayAll, containsInAnyOrder(
            float[][][].class,
            Serializable[][].class, Cloneable[][].class, Object[][].class,
            Serializable[].class,   Cloneable[].class,   Object[].class,
            Serializable.class,     Cloneable.class,     Object.class));
    }

    @Test
    void shouldReturnAllTypes2() {
        // given
        TypeInfo timeUnit = of(TimeUnit.class);
        TypeInfo linkedHashMap = new TypeReference<LinkedHashMap<String, ? extends Integer>>() { };

        // when
        Set<Type> timeUnitAll = timeUnit.getAllTypes();
        Set<TypeInfo> linkedHashMapAll = linkedHashMap.getAllTypeInfos();

        // then
        List<Type> expectedTimeUnitTypes = new ArrayList<>(Arrays.asList(
            TimeUnit.class, Serializable.class, Object.class,
            new TypeReference<Enum<TimeUnit>>() { }.getType(), new TypeReference<Comparable<TimeUnit>>() { }.getType()));

        JavaVersionHelper.getConstableClassIfApplicable().ifPresent(constableTypes -> {
            expectedTimeUnitTypes.add(constableTypes.getConstableClass());
        });
        assertThat(timeUnitAll, containsTypesInAnyOrder(expectedTimeUnitTypes));

        List<TypeInfo> expectedLinkedHashMapTypes = new ArrayList<>(Arrays.asList(
            new TypeReference<LinkedHashMap<String, ? extends Integer>>() { },
            new TypeReference<HashMap<String, ? extends Integer>>() { },
            new TypeReference<AbstractMap<String, ? extends Integer>>() { },
            new TypeReference<Map<String, ? extends Integer>>() { },
            of(Cloneable.class), of(Serializable.class), of(Object.class)));

        JavaVersionHelper.getSequencedCollectionTypesIfApplicable().ifPresent(seqCollectionTypes -> {
            ParameterizedTypeImpl sequencedMapTypeEquivalent =
                parameterizedTypeBuilder(seqCollectionTypes.getSequencedMapClass())
                    .withTypeArg(0, String.class)
                    .withTypeArg(1, WildcardTypeImpl.newWildcardExtends(Integer.class))
                    .build();
            expectedLinkedHashMapTypes.add(TypeInfo.of(sequencedMapTypeEquivalent));
        });
        assertThat(linkedHashMapAll, containsTypeInfosInAnyOrder(expectedLinkedHashMapTypes));
    }

    @Test
    void shouldConsiderInterfaceOfInterfaces() {
        // given
        Class<?> pathClass = Paths.get("").getClass();

        // when
        Set<TypeInfo> pathsAll = TypeVisitor.gatherAllTypes(
            pathClass, new TypeVariableResolver(pathClass), new HashSet<>(), TypeInfo::new);

        // then
        assertThat(pathsAll, hasItems(of(Path.class),
            new TypeReference<Comparable<Path>>() { },
            new TypeReference<Iterable<Path>>() { },
            of(Watchable.class), of(Object.class)));
    }

    @Test
    void shouldReturnTypesWithGivenCollectionAndFunction() {
        // given
        TypeInfo typeInfo = new TypeInfo(BigDecimal.class);
        List<String> typesOfBigDecimal = new ArrayList<>();
        Consumer<Type> typeVisitor = type -> typesOfBigDecimal.add(CommonTypeUtils.getDefinitiveClass(type).getSimpleName());

        // when
        typeInfo.visitAllTypes(typeVisitor);

        // then
        assertThat(typesOfBigDecimal, contains("BigDecimal", "Number", "Object", "Serializable", "Comparable"));
    }

    private Matcher<Iterable<? extends Type>> containsTypesInAnyOrder(List<Type> items) {
        return containsInAnyOrder(items.toArray(new Type[0]));
    }

    private Matcher<Iterable<? extends TypeInfo>> containsTypeInfosInAnyOrder(List<TypeInfo> items) {
        return containsInAnyOrder(items.toArray(new TypeInfo[0]));
    }
}