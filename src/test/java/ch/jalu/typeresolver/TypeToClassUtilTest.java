package ch.jalu.typeresolver;

import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ch.jalu.typeresolver.typeimpl.WildcardTypeImpl.newWildcardExtends;
import static ch.jalu.typeresolver.typeimpl.WildcardTypeImpl.newWildcardSuper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test for {@link TypeToClassUtil}.
 */
class TypeToClassUtilTest {

    // TODO: Consider merging safe-to-write and safe-to-read tests cases together

    @Test
    void shouldReturnClassIfArgumentIsClass() {
        // given / when / then
        assertEquals(TypeToClassUtil.getSafeToWriteClass(String.class), String.class);
        assertEquals(TypeToClassUtil.getSafeToWriteClass(List.class), List.class);
        assertEquals(TypeToClassUtil.getSafeToWriteClass(int.class), int.class);
        assertEquals(TypeToClassUtil.getSafeToWriteClass(Double[].class), Double[].class);
        assertEquals(TypeToClassUtil.getSafeToWriteClass(char[].class), char[].class);

        assertEquals(TypeToClassUtil.getSafeToReadClass(String.class), String.class);
        assertEquals(TypeToClassUtil.getSafeToReadClass(List.class), List.class);
        assertEquals(TypeToClassUtil.getSafeToReadClass(int.class), int.class);
        assertEquals(TypeToClassUtil.getSafeToReadClass(Double[].class), Double[].class);
        assertEquals(TypeToClassUtil.getSafeToReadClass(char[].class), char[].class);
    }

    @Test
    void shouldReturnClassFromParameterizedClass() {
        // given
        Type stringList = new TypeToken<List<String>>(){ }.getType();
        Type questionMarkMap = new TypeToken<Map<?, ?>>(){ }.getType();
        Type stringListArrSet = new TypeToken<Set<List<String>[]>>(){ }.getType();

        // when / then
        assertEquals(TypeToClassUtil.getSafeToWriteClass(stringList), List.class);
        assertEquals(TypeToClassUtil.getSafeToWriteClass(questionMarkMap), Map.class);
        assertEquals(TypeToClassUtil.getSafeToWriteClass(stringListArrSet), Set.class);

        assertEquals(TypeToClassUtil.getSafeToReadClass(stringList), List.class);
        assertEquals(TypeToClassUtil.getSafeToReadClass(questionMarkMap), Map.class);
        assertEquals(TypeToClassUtil.getSafeToReadClass(stringListArrSet), Set.class);
    }

    @Test
    void shouldReturnSafeToWriteClassFromGenericArrayTypes() {
        // given
        Type setArray = new TypeToken<Set<List<String>>[]>(){ }.getType();
        Type list3dArray = new TypeToken<List<String>[][][]>(){ }.getType();

        // when / then
        assertEquals(TypeToClassUtil.getSafeToWriteClass(setArray), Set[].class);
        assertEquals(TypeToClassUtil.getSafeToWriteClass(list3dArray), List[][][].class);
    }

    @Test
    void shouldReturnSafeToWriteClassFromWildcards() {
        // given
        Type superInt = getFirstGenericType(new TypeToken<List<? super Integer>>(){ });
        Type superByteArray = getFirstGenericType(new TypeToken<List<? super byte[]>>(){ });
        Type nestedSuperString = newWildcardSuper(newWildcardSuper(String.class));
        Type nestedSuperListArr = newWildcardSuper(newWildcardSuper(newWildcardSuper(
            new TypeToken<List<Integer>[]>() { }.getType())));

        // when / then
        assertEquals(TypeToClassUtil.getSafeToWriteClass(superInt), Integer.class);
        assertEquals(TypeToClassUtil.getSafeToWriteClass(superByteArray), byte[].class);
        assertEquals(TypeToClassUtil.getSafeToWriteClass(nestedSuperString), String.class);
        assertEquals(TypeToClassUtil.getSafeToWriteClass(nestedSuperListArr), List[].class);
    }

    @Test
    void shouldReturnNullIfClassForWritingCannotBeDetermined() {
        // given
        Type questionMark = getFirstGenericType(new TypeToken<List<?>>(){ });
        Type extComparable = getFirstGenericType(new TypeToken<List<? extends Comparable>>(){ });
        Type unknownType = new Type() { };

        // when / then
        assertNull(TypeToClassUtil.getSafeToWriteClass(null));
        assertNull(TypeToClassUtil.getSafeToWriteClass(questionMark));
        assertNull(TypeToClassUtil.getSafeToWriteClass(extComparable));
        assertNull(TypeToClassUtil.getSafeToWriteClass(unknownType));
    }

    @Test
    void shouldReturnSafeToReadClassFromGenericArrays() {
        // given
        Type list2dArray = new TypeToken<List<? extends Number>[][]>(){ }.getType();
        Type set3dArray = new TypeToken<Set<Double>[][][]>() { }.getType();
        Type extendsSet3dArr = newWildcardExtends(set3dArray);

        // when / then
        assertEquals(TypeToClassUtil.getSafeToReadClass(list2dArray), List[][].class);
        assertEquals(TypeToClassUtil.getSafeToReadClass(extendsSet3dArr), Set[][][].class);
    }

    @Test
    void shouldReturnSafeToReadClassFromWildcards() throws NoSuchMethodException {
        // given
        Type questionMark = getFirstGenericType(new TypeToken<List<?>>(){ });
        Type superInt = getFirstGenericType(new TypeToken<List<? super Integer>>(){ });
        Type extComparable = getFirstGenericType(new TypeToken<List<? extends Comparable>>(){ });

        // when / then
        assertEquals(TypeToClassUtil.getSafeToReadClass(questionMark), Object.class);
        assertEquals(TypeToClassUtil.getSafeToReadClass(superInt), Object.class);
        assertEquals(TypeToClassUtil.getSafeToReadClass(extComparable), Comparable.class);

        Type boundTypeVariable = getClass().getDeclaredMethod("bExtNumber").getGenericReturnType();
        assertEquals(TypeToClassUtil.getSafeToReadClass(boundTypeVariable), Number.class);
    }

    private static Type getFirstGenericType(TypeToken<?> typeToken) {
        return new TypeInfo(typeToken.getType())
            .getGenericTypeInfo(0)
            .getType();
    }

    private <B extends Number> B bExtNumber() {
        return null;
    }
}