package ch.jalu.typeresolver;

import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static ch.jalu.typeresolver.typeimpl.WildcardTypeImpl.newWildcardExtends;
import static ch.jalu.typeresolver.typeimpl.WildcardTypeImpl.newWildcardSuper;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for {@link TypeToClassUtil}.
 */
class TypeToClassUtilTest {

    @Test
    void shouldReturnClassIfArgumentIsClass() {
        // given / when / then
        checkHasReadAndWriteClass(String.class,   String.class,   String.class);
        checkHasReadAndWriteClass(List.class,     List.class,     List.class);
        checkHasReadAndWriteClass(int.class,      int.class,      int.class);
        checkHasReadAndWriteClass(Double[].class, Double[].class, Double[].class);
        checkHasReadAndWriteClass(char[].class,   char[].class,   char[].class);
    }

    @Test
    void shouldReturnClassFromParameterizedClass() {
        // given
        Type stringList = new TypeToken<List<String>>(){ }.getType();
        Type questionMarkMap = new TypeToken<Map<?, ?>>(){ }.getType();
        Type stringListArrSet = new TypeToken<Set<List<String>[]>>(){ }.getType();

        // when / then
        checkHasReadAndWriteClass(stringList, List.class, List.class);
        checkHasReadAndWriteClass(questionMarkMap, Map.class, Map.class);
        checkHasReadAndWriteClass(stringListArrSet, Set.class, Set.class);
    }

    @Test
    void shouldReturnClassFromGenericArrayTypes() {
        // given
        Type setArray = new TypeToken<Set<List<String>>[]>(){ }.getType();
        Type list3dArray = new TypeToken<List<String>[][][]>(){ }.getType();
        Type optional2dArray = new TypeToken<Optional<? extends Number>[][]>(){ }.getType();

        // when / then
        checkHasReadAndWriteClass(setArray, Set[].class, Set[].class);
        checkHasReadAndWriteClass(list3dArray, List[][][].class, List[][][].class);
        checkHasReadAndWriteClass(optional2dArray, Optional[][].class, Optional[][].class);
    }

    @Test
    void shouldReturnClassFromWildcards() {
        // given
        Type empty = getFirstGenericType(new TypeToken<List<?>>() { });

        Type superInt = getFirstGenericType(new TypeToken<List<? super Integer>>(){ });
        Type superComparable = getFirstGenericType(new TypeToken<List<? super Comparable<String>>>(){ });
        Type superByteArray = getFirstGenericType(new TypeToken<List<? super byte[]>>(){ });
        Type nestedSuperString = newWildcardSuper(newWildcardSuper(String.class)); // ? super<? super String>
        Type nestedSuperListArr = newWildcardSuper(newWildcardSuper(newWildcardSuper(
            new TypeToken<List<Integer>[]>() { }.getType()))); // ? super<? super<? super List...[]>>

        Type extInt = getFirstGenericType(new TypeToken<List<? extends Integer>>(){ });
        Type extComparable = getFirstGenericType(new TypeToken<List<? extends Comparable<String>>>(){ });
        Type extByteArray = getFirstGenericType(new TypeToken<List<? extends byte[]>>(){ });
        Type nestedExtString = newWildcardExtends(newWildcardExtends(String.class)); // ? extends<? extends String>
        Type nestedExtListArr = newWildcardExtends(newWildcardExtends(newWildcardExtends(
            new TypeToken<List<Integer>[]>() { }.getType()))); // ? extends<? extends<? extends List...[]>>

        // when / then
        checkHasReadAndWriteClass(empty, Object.class, null);

        checkHasReadAndWriteClass(superInt,           Object.class, Integer.class);
        checkHasReadAndWriteClass(superComparable,    Object.class, Comparable.class);
        checkHasReadAndWriteClass(superByteArray,     Object.class, byte[].class);
        checkHasReadAndWriteClass(nestedSuperString,  Object.class, String.class);
        checkHasReadAndWriteClass(nestedSuperListArr, Object.class, List[].class);

        checkHasReadAndWriteClass(extInt,           Integer.class, null);
        checkHasReadAndWriteClass(extComparable,    Comparable.class, null);
        checkHasReadAndWriteClass(extByteArray,     byte[].class, null);
        checkHasReadAndWriteClass(nestedExtString,  String.class, null);
        checkHasReadAndWriteClass(nestedExtListArr, List[].class, null);
    }

    @Test
    void shouldReturnNullIfClassForWritingCannotBeDetermined() {
        // given
        Type unknownType = new Type() { };

        // when / then
        checkHasReadAndWriteClass(null, Object.class, null);
        checkHasReadAndWriteClass(unknownType, Object.class, null);
    }

    @Test
    void shouldReturnClassFromTypeVariables() throws NoSuchMethodException {
        // given
        Type bExtNumber = getClass().getDeclaredMethod("bExtNumber").getGenericReturnType();
        Type cExtArrayList = getClass().getDeclaredMethod("cExtArrayList").getGenericReturnType();
        Type fExtComparableArray = getClass().getDeclaredMethod("fExtComparable").getGenericReturnType();
        Type oUnbound = getClass().getDeclaredMethod("oUnbound").getGenericReturnType();
        Type qArrFieldType = getClass().getDeclaredMethod("qArrUnbound").getGenericReturnType();

        // when / then
        checkHasReadAndWriteClass(bExtNumber, Number.class, null);
        checkHasReadAndWriteClass(cExtArrayList, ArrayList.class, null);
        checkHasReadAndWriteClass(fExtComparableArray, Comparable[].class, null);
        checkHasReadAndWriteClass(oUnbound, Object.class, null);
        checkHasReadAndWriteClass(qArrFieldType, Object[].class, null);
    }

    private static void checkHasReadAndWriteClass(Type givenType, Class<?> expectedSafeToRead, Class<?> expectedSafeToWrite) {
        assertEquals(TypeToClassUtil.getSafeToReadClass(givenType), expectedSafeToRead);
        assertEquals(TypeToClassUtil.getSafeToWriteClass(givenType), expectedSafeToWrite);
    }

    private static Type getFirstGenericType(TypeToken<?> typeToken) {
        return new TypeInfo(typeToken.getType())
            .getTypeArgumentInfo(0)
            .getType();
    }

    // -----------
    // Methods for type variables
    // -----------

    private <B extends Number> B bExtNumber() {
        return null;
    }
    private <C extends ArrayList<String>> C cExtArrayList() {
        return null;
    }
    private <E extends Comparable<E>, F extends E> F[] fExtComparable() {
        return null;
    }
    private <O> O oUnbound() {
        return null;
    }
    private <Q> Q[] qArrUnbound() {
        return null;
    }
}