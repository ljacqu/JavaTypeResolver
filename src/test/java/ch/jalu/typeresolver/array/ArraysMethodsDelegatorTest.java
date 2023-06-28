package ch.jalu.typeresolver.array;

import ch.jalu.typeresolver.EnumUtil;
import ch.jalu.typeresolver.array.ArraysMethodsDelegator.ArrayComponentType;
import ch.jalu.typeresolver.primitives.Primitives;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

/**
 * Test for {@link ArraysMethodsDelegator}.
 */
class ArraysMethodsDelegatorTest {

    @ParameterizedTest
    @EnumSource(ArrayComponentType.class)
    void shouldDelegateForBinarySearch(ArrayComponentType componentType) {
        // given
        Object emptyArr = createArray(componentType, ArrayType.EMPTY, false);
        Object sortedArr = createArray(componentType, ArrayType.FIVE_ITEMS, true);

        List<Object> elements = createListWithItems(componentType, true);
        Object searchKey = elements.get(3);

        // when
        int idxOfEmpty = ArraysMethodsDelegator.binarySearch(emptyArr, searchKey);
        int idxOfFull = ArraysMethodsDelegator.binarySearch(sortedArr, searchKey);
        int idxOfPartial = ArraysMethodsDelegator.binarySearch(sortedArr, 2, 5, searchKey);
        int idxOutOfRange = ArraysMethodsDelegator.binarySearch(sortedArr, 0, 3, searchKey);

        // then
        assertThat(idxOfEmpty, lessThan(0));

        // Skip boolean tests because we only have two values -> keys are different. Since it's a custom search impl.,
        // it's covered with dedicated tests anyway.
        if (componentType != ArrayComponentType.BOOLEAN) {
            assertThat(idxOfFull, equalTo(3));
            assertThat(idxOfPartial, equalTo(3));
            assertThat(idxOutOfRange, lessThan(0));
        }
    }

    @ParameterizedTest
    @EnumSource(ArrayComponentType.class)
    void shouldDelegateForCopyOf(ArrayComponentType componentType) {
        // given
        Object emptyArr = createArray(componentType, ArrayType.EMPTY, false);
        Object fiveArr = createArray(componentType, ArrayType.FIVE_ITEMS, false);

        // when
        Object emptyCopy = ArraysMethodsDelegator.copyOf(emptyArr, 0);
        Object sevenArr = ArraysMethodsDelegator.copyOf(fiveArr, 7);
        Object threeArr = ArraysMethodsDelegator.copyOf(emptyArr, 3);

        // then
        assertThat(emptyCopy, instanceOf(emptyArr.getClass()));
        assertThat(emptyCopy, not(sameInstance(emptyArr)));
        assertThat(Array.getLength(emptyCopy), equalTo(0));

        List<Object> elements = createListWithItems(componentType, false);
        Object emptyValue = EnumUtil.tryValueOf(Primitives.class, componentType.name())
            .map(Primitives::getDefaultValue)
            .orElse(null);

        assertThat(sevenArr, instanceOf(fiveArr.getClass()));
        assertThat(Array.getLength(sevenArr), equalTo(7));
        assertThat(Array.get(sevenArr, 2), equalTo(elements.get(2)));
        assertThat(Array.get(sevenArr, 6), equalTo(emptyValue));

        assertThat(threeArr, instanceOf(emptyArr.getClass()));
        assertThat(Array.getLength(threeArr), equalTo(3));
        assertThat(Array.get(threeArr, 1), equalTo(emptyValue));
        assertThat(Array.get(threeArr, 2), equalTo(emptyValue));
    }

    @ParameterizedTest
    @EnumSource(ArrayComponentType.class)
    void shouldDelegateForCopyOfRange(ArrayComponentType componentType) {
        // given
        Object emptyArr = createArray(componentType, ArrayType.EMPTY, false);
        Object fiveArr = createArray(componentType, ArrayType.FIVE_ITEMS, false);

        // when
        Object emptyCopy = ArraysMethodsDelegator.copyOfRange(emptyArr, 0, 3);
        Object arr26 = ArraysMethodsDelegator.copyOfRange(fiveArr, 2, 6);
        Object arr49 = ArraysMethodsDelegator.copyOfRange(fiveArr, 4, 9);

        // then
        List<Object> elements = createListWithItems(componentType, false);
        Object emptyValue = EnumUtil.tryValueOf(Primitives.class, componentType.name())
            .map(Primitives::getDefaultValue)
            .orElse(null);

        assertThat(emptyCopy, instanceOf(emptyArr.getClass()));
        assertThat(Array.getLength(emptyCopy), equalTo(3));
        assertThat(Array.get(emptyCopy, 0), equalTo(emptyValue));

        assertThat(arr26, instanceOf(fiveArr.getClass()));
        assertThat(arr26, not(sameInstance(fiveArr)));
        assertThat(Array.getLength(arr26), equalTo(4));
        assertThat(Array.get(arr26, 1), equalTo(elements.get(3)));
        assertThat(Array.get(arr26, 3), equalTo(emptyValue));

        assertThat(arr49, instanceOf(emptyArr.getClass()));
        assertThat(arr49, not(sameInstance(emptyArr)));
        assertThat(Array.getLength(arr49), equalTo(5));
        assertThat(Array.get(arr49, 0), equalTo(elements.get(4)));
        assertThat(Array.get(arr49, 1), equalTo(emptyValue));
        assertThat(Array.get(arr49, 4), equalTo(emptyValue));
    }

    @ParameterizedTest
    @EnumSource(ArrayComponentType.class)
    void shouldDelegateForEqualityCheck(ArrayComponentType componentType) {
        // given
        Object emptyArr = createArray(componentType, ArrayType.EMPTY, false);
        Object fiveArr = createArray(componentType, ArrayType.FIVE_ITEMS, false);
        Object fiveSortedArr = createArray(componentType, ArrayType.FIVE_ITEMS, true);

        // when
        boolean isEq1 = ArraysMethodsDelegator.equals(fiveArr, fiveArr);
        boolean isEq2 = ArraysMethodsDelegator.equals(fiveArr, fiveSortedArr);
        boolean isEq3 = ArraysMethodsDelegator.equals(emptyArr, fiveArr);

        // then
        assertThat(isEq1, equalTo(true));
        assertThat(isEq2, equalTo(false));
        assertThat(isEq3, equalTo(false));
    }

    @ParameterizedTest
    @EnumSource(ArrayComponentType.class)
    void shouldDelegateForFilling(ArrayComponentType componentType) {
        // given
        Object emptyArr = createArray(componentType, ArrayType.EMPTY, false);
        Object fiveArr1 = createArray(componentType, ArrayType.FIVE_ITEMS, false);
        Object fiveArr2 = createArray(componentType, ArrayType.FIVE_ITEMS, false);
        Object fiveArr3 = createArray(componentType, ArrayType.FIVE_ITEMS, false);
        List<Object> list = createListWithItems(componentType, false);

        // when
        ArraysMethodsDelegator.fill(emptyArr, list.get(1));
        ArraysMethodsDelegator.fill(fiveArr1, list.get(0));
        ArraysMethodsDelegator.fill(fiveArr2, 0, 2, list.get(0));
        ArraysMethodsDelegator.fill(fiveArr3, 2, 5, list.get(3));

        // then
        assertThat(Array.get(fiveArr1, 0), equalTo(list.get(0)));
        assertThat(Array.get(fiveArr1, 1), equalTo(list.get(0)));
        assertThat(Array.get(fiveArr1, 2), equalTo(list.get(0)));
        assertThat(Array.get(fiveArr1, 3), equalTo(list.get(0)));
        assertThat(Array.get(fiveArr1, 4), equalTo(list.get(0)));

        assertThat(Array.get(fiveArr2, 0), equalTo(list.get(0)));
        assertThat(Array.get(fiveArr2, 1), equalTo(list.get(0)));
        assertThat(Array.get(fiveArr2, 2), equalTo(list.get(2)));
        assertThat(Array.get(fiveArr2, 3), equalTo(list.get(3)));
        assertThat(Array.get(fiveArr2, 4), equalTo(list.get(4)));

        assertThat(Array.get(fiveArr3, 0), equalTo(list.get(0)));
        assertThat(Array.get(fiveArr3, 1), equalTo(list.get(1)));
        assertThat(Array.get(fiveArr3, 2), equalTo(list.get(3)));
        assertThat(Array.get(fiveArr3, 3), equalTo(list.get(3)));
        assertThat(Array.get(fiveArr3, 4), equalTo(list.get(3)));
    }

    @ParameterizedTest
    @EnumSource(ArrayComponentType.class)
    void shouldDelegateForHashCode(ArrayComponentType componentType) {
        // given
        Object emptyArr = createArray(componentType, ArrayType.EMPTY, false);
        Object fiveArr = createArray(componentType, ArrayType.FIVE_ITEMS, false);
        Object fiveArrSorted = createArray(componentType, ArrayType.FIVE_ITEMS, true);

        // when
        int hashCode1 = ArraysMethodsDelegator.hashCode(emptyArr);
        int hashCode2 = ArraysMethodsDelegator.hashCode(fiveArr);
        int hashCode3 = ArraysMethodsDelegator.hashCode(fiveArrSorted);

        // then
        assertThat(hashCode1, equalTo(1));
        assertThat(hashCode2, not(equalTo(1)));
        assertThat(hashCode3, not(equalTo(1)));
        assertThat(hashCode3, not(equalTo(hashCode2)));
    }

    @ParameterizedTest
    @EnumSource(ArrayComponentType.class)
    void shouldDelegateForParallelSort(ArrayComponentType componentType) {
        // given
        Object emptyArr = createArray(componentType, ArrayType.EMPTY, false);
        Object fiveArr1 = createArray(componentType, ArrayType.FIVE_ITEMS, false);
        Object fiveArr2 = createArray(componentType, ArrayType.FIVE_ITEMS, false);

        // when
        ArraysMethodsDelegator.parallelSort(emptyArr);
        ArraysMethodsDelegator.parallelSort(fiveArr1);
        ArraysMethodsDelegator.parallelSort(fiveArr2, 2, 5);

        // then
        Object sortedFiveArr = createArray(componentType, ArrayType.FIVE_ITEMS, true);
        assertThat(ArraysMethodsDelegator.equals(fiveArr1, sortedFiveArr), equalTo(true));

        List<Object> list = createListWithItems(componentType, false);
        List<Object> list2Thru5Sorted = new ArrayList<>(5);
        list2Thru5Sorted.addAll(list.subList(0, 2));
        list2Thru5Sorted.addAll(list.subList(2, 5).stream().sorted().collect(Collectors.toList()));

        assertThat(Array.get(fiveArr2, 0), equalTo(list2Thru5Sorted.get(0)));
        assertThat(Array.get(fiveArr2, 1), equalTo(list2Thru5Sorted.get(1)));
        assertThat(Array.get(fiveArr2, 2), equalTo(list2Thru5Sorted.get(2)));
        assertThat(Array.get(fiveArr2, 3), equalTo(list2Thru5Sorted.get(3)));
        assertThat(Array.get(fiveArr2, 4), equalTo(list2Thru5Sorted.get(4)));
    }

    @ParameterizedTest
    @EnumSource(ArrayComponentType.class)
    void shouldDelegateForSort(ArrayComponentType componentType) {
        // given
        Object emptyArr = createArray(componentType, ArrayType.EMPTY, false);
        Object fiveArr1 = createArray(componentType, ArrayType.FIVE_ITEMS, false);
        Object fiveArr2 = createArray(componentType, ArrayType.FIVE_ITEMS, false);

        // when
        ArraysMethodsDelegator.sort(emptyArr);
        ArraysMethodsDelegator.sort(fiveArr1);
        ArraysMethodsDelegator.sort(fiveArr2, 2, 5);

        // then
        Object sortedFiveArr = createArray(componentType, ArrayType.FIVE_ITEMS, true);
        assertThat(ArraysMethodsDelegator.equals(fiveArr1, sortedFiveArr), equalTo(true));

        List<Object> list = createListWithItems(componentType, false);
        List<Object> list2Thru5Sorted = new ArrayList<>(5);
        list2Thru5Sorted.addAll(list.subList(0, 2));
        list2Thru5Sorted.addAll(list.subList(2, 5).stream().sorted().collect(Collectors.toList()));

        assertThat(Array.get(fiveArr2, 0), equalTo(list2Thru5Sorted.get(0)));
        assertThat(Array.get(fiveArr2, 1), equalTo(list2Thru5Sorted.get(1)));
        assertThat(Array.get(fiveArr2, 2), equalTo(list2Thru5Sorted.get(2)));
        assertThat(Array.get(fiveArr2, 3), equalTo(list2Thru5Sorted.get(3)));
        assertThat(Array.get(fiveArr2, 4), equalTo(list2Thru5Sorted.get(4)));
    }

    @ParameterizedTest
    @EnumSource(ArrayComponentType.class)
    void shouldDelegateForToString(ArrayComponentType componentType) {
        // given
        Object emptyArr = createArray(componentType, ArrayType.EMPTY, false);
        Object fiveArr = createArray(componentType, ArrayType.FIVE_ITEMS, false);

        // when
        String toString1 = ArraysMethodsDelegator.toString(emptyArr);
        String toString2 = ArraysMethodsDelegator.toString(fiveArr);

        // then
        assertThat(toString1, equalTo("[]"));

        String expectedArrString = getExpectedToString(componentType);
        assertThat(toString2, equalTo(expectedArrString));
    }

    @ParameterizedTest
    @EnumSource(ArrayComponentType.class)
    void shouldCreateStreamForArray(ArrayComponentType componentType) {
        // given
        Object emptyArr = createArray(componentType, ArrayType.EMPTY, false);
        Object fiveArr = createArray(componentType, ArrayType.FIVE_ITEMS, false);

        // when
        List<Object> listOfEmptyArr = ArraysMethodsDelegator.stream(emptyArr)
            .collect(Collectors.toList());
        String toStringViaStream = "[" + ArraysMethodsDelegator.stream(fiveArr)
            .map(String::valueOf)
            .collect(Collectors.joining(", ")) + "]";

        // then
        assertThat(listOfEmptyArr, empty());
        String expectedArrString = getExpectedToString(componentType);
        assertThat(toStringViaStream, equalTo(expectedArrString));
    }

    private static String getExpectedToString(ArrayComponentType componentType) {
        switch (componentType) {
            case BOOLEAN:   return "[true, false, true, false, true]";
            case BYTE:      return "[120, -2, 37, 8, 66]";
            case CHARACTER: return "[p, o, w, e, d]";
            case SHORT:     return "[400, 12, 317, 202, -44]";
            case INTEGER:   return "[17, -4050, 68, 25000, -42]";
            case LONG:      return "[17, 320, -13, 6, 562398]";
            case FLOAT:     return "[872.25, -12.5, 3.0, -2.75, 2364.25]";
            case DOUBLE:    return "[54.2, 15.25, -16.36, 0.33, 8.64]";
            case OBJECT:    return "[Paris, London, Zürich, Ljubljana, Antwerp]";
            default:
                throw new IllegalStateException("Unexpected value: " + componentType);
        }
    }

    @Nested
    class BooleanArrayFillIns {

        @Test
        void shouldSearchInBooleanArray() {
            boolean[] arr1 = new boolean[]{ false, false, true, true };
            boolean[] arr2 = new boolean[]{ false, false, false, false };
            boolean[] arr3 = new boolean[]{ true, true, true, true};
            boolean[] arr4 = new boolean[]{ false, true, true, true, true, true, true, true, true, true, true, true, true, true };
            boolean[] arr5 = new boolean[]{ false, false, false };
            boolean[] arr6 = new boolean[]{ false, false };
            boolean[] arr7 = new boolean[]{ false };
            boolean[] arr8 = new boolean[0];
            boolean[] arr9 = new boolean[]{ false, false, false, false, false, false, true, false, false, false, false, false }; // not sorted correctly


            verifyFallbackSearchWithBinarySearch(arr1, 0, arr1.length, true);
            verifyFallbackSearchWithBinarySearch(arr2, 0, arr2.length, true);
            verifyFallbackSearchWithBinarySearch(arr3, 0, arr3.length, true);
            verifyFallbackSearchWithBinarySearch(arr4, 0, arr4.length, true);
            verifyFallbackSearchWithBinarySearch(arr5, 0, arr5.length, true);
            verifyFallbackSearchWithBinarySearch(arr6, 0, arr6.length, true);
            verifyFallbackSearchWithBinarySearch(arr7, 0, arr7.length, true);
            verifyFallbackSearchWithBinarySearch(arr8, 0, arr8.length, true);
            verifyFallbackSearchWithBinarySearch(arr9, 0, arr9.length, true);

            verifyFallbackSearchWithBinarySearch(arr1, 0, arr1.length, false);
            verifyFallbackSearchWithBinarySearch(arr2, 0, arr2.length, false);
            verifyFallbackSearchWithBinarySearch(arr3, 0, arr3.length, false);
            verifyFallbackSearchWithBinarySearch(arr4, 0, arr4.length, false);
            verifyFallbackSearchWithBinarySearch(arr5, 0, arr5.length, false);
            verifyFallbackSearchWithBinarySearch(arr6, 0, arr6.length, false);
            verifyFallbackSearchWithBinarySearch(arr7, 0, arr7.length, false);
            verifyFallbackSearchWithBinarySearch(arr8, 0, arr8.length, false);
            verifyFallbackSearchWithBinarySearch(arr9, 0, arr9.length, false);
        }

        @Test
        void shouldSearchInBooleanArrayWithRange() {
            // given                         0      1      2      3      4      5     6     7     8
            boolean[] arr = new boolean[]{ false, false, false, false, false, false, true, true, true  };

            // when / then
            verifyFallbackSearchWithBinarySearch(arr, 0, 2, true);
            verifyFallbackSearchWithBinarySearch(arr, 0, 0, true);
            verifyFallbackSearchWithBinarySearch(arr, 0, 4, true);
            verifyFallbackSearchWithBinarySearch(arr, 0, 6, true);
            verifyFallbackSearchWithBinarySearch(arr, 2, 4, true);
            verifyFallbackSearchWithBinarySearch(arr, 2, 5, true);
            verifyFallbackSearchWithBinarySearch(arr, 4, 6, true);
            verifyFallbackSearchWithBinarySearch(arr, 4, 8, true);
            verifyFallbackSearchWithBinarySearch(arr, 7, 8, true);
            verifyFallbackSearchWithBinarySearch(arr, 8, 8, true);
            verifyFallbackSearchWithBinarySearch(arr, 8, 9, true);
            verifyFallbackSearchWithBinarySearch(arr, 9, 9, true);
            verifyFallbackSearchWithBinarySearch(arr, 0, 9, true);

            verifyFallbackSearchWithBinarySearch(arr, 0, 2, false);
            verifyFallbackSearchWithBinarySearch(arr, 0, 0, false);
            verifyFallbackSearchWithBinarySearch(arr, 0, 4, false);
            verifyFallbackSearchWithBinarySearch(arr, 0, 6, false);
            verifyFallbackSearchWithBinarySearch(arr, 2, 4, false);
            verifyFallbackSearchWithBinarySearch(arr, 2, 5, false);
            verifyFallbackSearchWithBinarySearch(arr, 4, 6, false);
            verifyFallbackSearchWithBinarySearch(arr, 4, 8, false);
            verifyFallbackSearchWithBinarySearch(arr, 7, 8, false);
            verifyFallbackSearchWithBinarySearch(arr, 8, 8, false);
            verifyFallbackSearchWithBinarySearch(arr, 8, 9, false);
            verifyFallbackSearchWithBinarySearch(arr, 9, 9, false);
            verifyFallbackSearchWithBinarySearch(arr, 0, 9, false);
        }

        @Test
        void shouldSortBooleanArray() {
            // given
            boolean[] arr1 = new boolean[]{ true, false, true, false };
            boolean[] arr2 = new boolean[]{ false, false, true };
            boolean[] arr3 = new boolean[]{ true, false, false };
            boolean[] arr4 = new boolean[]{ true, true, true, true };
            boolean[] arr5 = new boolean[]{ false, false, false, false, false };
            boolean[] arr6 = new boolean[0];

            // when / then
            verifyFallbackSortWithObjectArraySort(arr1, 0, arr1.length);
            verifyFallbackSortWithObjectArraySort(arr2, 0, arr2.length);
            verifyFallbackSortWithObjectArraySort(arr3, 0, arr3.length);
            verifyFallbackSortWithObjectArraySort(arr4, 0, arr4.length);
            verifyFallbackSortWithObjectArraySort(arr5, 0, arr5.length);
            verifyFallbackSortWithObjectArraySort(arr6, 0, arr6.length);
        }

        @Test
        void shouldSortBooleanArrayWithinRange() {
            // given
            boolean[] arr1 = new boolean[0];
            boolean[] arr2 = new boolean[]{ true, false, true, false, true, false };
            boolean[] arr3 = new boolean[]{ true, true, true, true, false, false, false, false };

            // when / then
            verifyFallbackSortWithObjectArraySort(arr1, 0, 0);
            verifyFallbackSortWithObjectArraySort(arr2, 2, 3);
            verifyFallbackSortWithObjectArraySort(arr2, 2, 6);
            verifyFallbackSortWithObjectArraySort(arr3, 0, 3);
            verifyFallbackSortWithObjectArraySort(arr3, 2, 6);
            verifyFallbackSortWithObjectArraySort(arr3, 2, 4);
            verifyFallbackSortWithObjectArraySort(arr3, 3, 8);
        }

        private void verifyFallbackSearchWithBinarySearch(boolean[] array, int from, int to, boolean value) {
            Boolean[] objArray = copyToReferenceBooleanArrayType(array);

            int actualIndex = ArraysMethodsDelegator.fillInBoolArrayBinarySearch(array, from, to, value);
            int expectedIndex = Arrays.binarySearch(objArray, from, to, value);
            if (expectedIndex >= 0) {
                assertThat(actualIndex, greaterThanOrEqualTo(0));
                assertThat(array[actualIndex], equalTo(value));
            } else {
                assertThat(actualIndex, equalTo(expectedIndex));
            }
        }

        private void verifyFallbackSortWithObjectArraySort(boolean[] array, int from, int to) {
            Boolean[] objArray = copyToReferenceBooleanArrayType(array);

            Arrays.sort(objArray, from, to);
            ArraysMethodsDelegator.fillInBoolArraySort(array, from, to);

            if (array.length > 0) {
                Boolean[] arrayAfterSorting = copyToReferenceBooleanArrayType(array);
                assertThat(arrayAfterSorting, arrayContaining(objArray));
            }
        }

        private Boolean[] copyToReferenceBooleanArrayType(boolean[] array) {
            Boolean[] objArray = new Boolean[array.length];
            for (int i = 0; i < array.length; ++i) {
                objArray[i] = array[i];
            }
            return objArray;
        }
    }

    private enum ArrayType {
        EMPTY,
        FIVE_ITEMS,
        LIST
    }

    private static List<Object> createListWithItems(ArrayComponentType componentType, boolean sorted) {
        return (List<Object>) createArray(componentType, ArrayType.LIST, sorted);
    }

    private static Object createArray(ArrayComponentType componentType, ArrayType arrayType, boolean sorted) {
        switch (componentType) {
            case BOOLEAN:
                return createArray(boolean[]::new, arrayType, sorted, true, false, true, false, true);
            case BYTE:
                return createArray(byte[]::new, arrayType, sorted, (byte) 120, (byte) -2, (byte) 37, (byte) 8, (byte) 66);
            case CHARACTER:
                return createArray(char[]::new, arrayType, sorted, 'p', 'o', 'w', 'e', 'd');
            case SHORT:
                return createArray(short[]::new, arrayType, sorted, (short) 400, (short) 12, (short) 317, (short) 202, (short) -44);
            case INTEGER:
                return createArray(int[]::new, arrayType, sorted, 17, -4050, 68, 25000, -42);
            case LONG:
                return createArray(long[]::new, arrayType, sorted, 17L, 320L, -13L, 6L, 562398L);
            case FLOAT:
                return createArray(float[]::new, arrayType, sorted, 872.25f, -12.5f, 3f, -2.75f, 2364.25f);
            case DOUBLE:
                return createArray(double[]::new, arrayType, sorted, 54.2d, 15.25d, -16.36d, 0.33d, 8.64d);
            case OBJECT:
                return createArray(String[]::new, arrayType, sorted, "Paris", "London", "Zürich", "Ljubljana", "Antwerp");
            default:
                throw new IllegalStateException("Unexpected value: " + componentType);
        }
    }

    private static Object createArray(IntFunction<Object> arrayCreator,
                                      ArrayType arrayType,
                                      boolean sorted,
                                      Object... items) {
        if (items.length != 5) {
            throw new IllegalArgumentException("Expected five items");
        }

        if (arrayType == ArrayType.EMPTY) {
            return arrayCreator.apply(0);
        }

        Object[] elements = sorted
            ? Arrays.stream(items).sorted().toArray()
            : items;

        switch (arrayType) {
            case FIVE_ITEMS:
                Object arr = arrayCreator.apply(items.length);
                for (int idx = 0; idx < elements.length; idx++) {
                    Array.set(arr, idx, elements[idx]);
                }
                return arr;
            case LIST:
                return Arrays.asList(elements);
            default:
                throw new IllegalStateException("Unexpected value: " + arrayType);
        }
    }
}