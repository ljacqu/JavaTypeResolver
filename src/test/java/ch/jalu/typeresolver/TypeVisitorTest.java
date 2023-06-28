package ch.jalu.typeresolver;

import ch.jalu.typeresolver.JavaVersionHelper.ConstableAndConstantDescTypes;
import ch.jalu.typeresolver.array.ArrayTypeUtil;
import ch.jalu.typeresolver.reference.TypeReference;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.RandomAccess;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static ch.jalu.typeresolver.TypeInfo.of;
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
        Optional<ConstableAndConstantDescTypes> constableTypes = JavaVersionHelper.getConstableClassIfApplicable();
        if (constableTypes.isPresent()) {
            Class<?> constable = constableTypes.get().getConstableClass();
            Class<?> constantDesc = constableTypes.get().getConstantDescClass();
            assertThat(stringAll, containsInAnyOrder(String.class, CharSequence.class, comparableString.getType(), Serializable.class, constable, constantDesc, Object.class));
        } else {
            assertThat(stringAll, containsInAnyOrder(String.class, CharSequence.class, comparableString.getType(), Serializable.class, Object.class));
        }

        assertThat(arrayListAll, containsInAnyOrder(
            new TypeReference<ArrayList<String>>() { },
            new TypeReference<AbstractList<String>>() { },
            new TypeReference<AbstractCollection<String>>() { },
            new TypeReference<List<String>>() { },
            new TypeReference<Collection<String>>() { },
            new TypeReference<Iterable<String>>() { },
            of(Cloneable.class), of(Serializable.class), of(RandomAccess.class), of(Object.class)));
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
        Optional<ConstableAndConstantDescTypes> constableTypes = JavaVersionHelper.getConstableClassIfApplicable();
        if (constableTypes.isPresent()) {
            Class<?> constableArray = ArrayTypeUtil.createArrayClass(constableTypes.get().getConstableClass());
            Class<?> constantDescArray = ArrayTypeUtil.createArrayClass(constableTypes.get().getConstantDescClass());

            assertThat(stringArrayAll, containsInAnyOrder(
                String[].class, CharSequence[].class, Serializable[].class, new TypeReference<Comparable<String>[]>() { }.getType(), constableArray, constantDescArray, Object[].class,
                Object.class, Cloneable.class, Serializable.class));
        } else {
            assertThat(stringArrayAll, containsInAnyOrder(
                String[].class, CharSequence[].class, Serializable[].class, new TypeReference<Comparable<String>[]>() { }.getType(), Object[].class,
                Object.class, Cloneable.class, Serializable.class));
        }

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
        assertThat(list2dArrayAll, containsInAnyOrder(
            new TypeReference<List<String>[][]>() { },
            new TypeReference<Collection<String>[][]>() { },
            new TypeReference<Iterable<String>[][]>() { },
            new TypeReference<Object[][]>() { },
            of(Serializable[].class), of(Cloneable[].class), of(Object[].class),
            of(Serializable.class), of(Cloneable.class), of(Object.class)));

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
        Optional<ConstableAndConstantDescTypes> constableTypes = JavaVersionHelper.getConstableClassIfApplicable();
        if (constableTypes.isPresent()) {
            Class<?> constable = constableTypes.get().getConstableClass();
            assertThat(timeUnitAll, containsInAnyOrder(TimeUnit.class, Serializable.class, constable, Object.class,
                new TypeReference<Enum<TimeUnit>>() { }.getType(), new TypeReference<Comparable<TimeUnit>>() { }.getType()));
        } else {
            assertThat(timeUnitAll, containsInAnyOrder(TimeUnit.class, Serializable.class, Object.class,
                new TypeReference<Enum<TimeUnit>>() { }.getType(), new TypeReference<Comparable<TimeUnit>>() { }.getType()));
        }

        assertThat(linkedHashMapAll, containsInAnyOrder(
            new TypeReference<LinkedHashMap<String, ? extends Integer>>() { },
            new TypeReference<HashMap<String, ? extends Integer>>() { },
            new TypeReference<AbstractMap<String, ? extends Integer>>() { },
            new TypeReference<Map<String, ? extends Integer>>() { },
            of(Cloneable.class), of(Serializable.class), of(Object.class)));
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
        Consumer<Type> typeVisitor = type -> typesOfBigDecimal.add(CommonTypeUtil.getDefinitiveClass(type).getSimpleName());

        // when
        typeInfo.visitAllTypes(typeVisitor);

        // then
        assertThat(typesOfBigDecimal, contains("BigDecimal", "Number", "Object", "Serializable", "Comparable"));
    }
}