package ch.jalu.typeresolver.array;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This class contains methods that can handle arrays of any type ({@code boolean[]}, {@code byte[]}, {@code Object[]},
 * etc.). Most methods delegate to methods in {@link Arrays} with the appropriate signature. The methods in this class
 * are useful when dealing with arrays held as {@code Object} variable whose specific array type is unknown.
 * <p>
 * Methods throw a runtime exception if the given object is not an array. To check if an object is an array, use
 * {@code object.getClass().isArray()}. See individual methods for specific caveats.
 */
@SuppressWarnings("checkstyle:OneStatementPerLine") // Justification: line-by-line aligned casts in switch statements
public final class ArrayUtils {

    private ArrayUtils() {
    }

    /**
     * Delegates to the appropriate method for binary search and returns the result. Depending on the array type,
     * this method calls {@link Arrays#binarySearch(byte[], int, int, byte)},
     * {@link Arrays#binarySearch(char[], int, int, char)}, etc. An exception is thrown if the key does not correspond
     * to the array type, or if it is null. All elements in {@code Object[]} arrays must implement {@link Comparable}
     * and be mutually comparable, or an exception will be thrown.
     * <p>
     * There is no binary search for boolean arrays in {@code Arrays}. This class provides a fill-in implementation.
     *
     * @param array the array object (e.g. String[] or double[])
     * @param key the key to search for (must match the array type and may not be null)
     * @return index of the key, or a negative insertion point (refer to {@link Arrays#binarySearch(byte[], byte)})
     */
    public static int binarySearch(Object array, Object key) {
        int length = Array.getLength(array);
        return binarySearch(array, 0, length, key);
    }

    /**
     * Delegates to the appropriate method for binary search and returns the result. Depending on the array type,
     * this method calls {@link Arrays#binarySearch(byte[], int, int, byte)},
     * {@link Arrays#binarySearch(char[], int, int, char)}, etc. An exception is thrown if the key does not correspond
     * to the array type, or if it is null. All elements in {@code Object[]} arrays must implement {@link Comparable},
     * and be mutually comparable, or an exception will be thrown.
     * <p>
     * There is no binary search for boolean arrays in {@code Arrays}. This class provides a fill-in implementation.
     *
     * @param array the array object (e.g. String[] or double[])
     * @param fromIndex start index of the range to search in (inclusive)
     * @param toIndex end index of the range to search in (exclusive)
     * @param key the key to search for (must match the array type and may not be null)
     * @return index of the key, or a negative insertion point (refer to {@link Arrays#binarySearch(byte[], byte)})
     */
    public static int binarySearch(Object array, int fromIndex, int toIndex, Object key) {
        ArrayComponentType comp = getArrayComponentType(array);
        verifyArgumentMatchesComponentType(key, comp, "key");
        switch (comp) {
            case BOOLEAN: return simpleBooleanArrayBinarySearch((boolean[]) array, fromIndex, toIndex, (boolean) key);
            case BYTE:      return Arrays.binarySearch((byte[]) array,   fromIndex, toIndex, (byte) key);
            case CHARACTER: return Arrays.binarySearch((char[]) array,   fromIndex, toIndex, (char) key);
            case SHORT:     return Arrays.binarySearch((short[]) array,  fromIndex, toIndex, (short) key);
            case INTEGER:   return Arrays.binarySearch((int[]) array,    fromIndex, toIndex, (int) key);
            case LONG:      return Arrays.binarySearch((long[]) array,   fromIndex, toIndex, (long) key);
            case FLOAT:     return Arrays.binarySearch((float[]) array,  fromIndex, toIndex, (float) key);
            case DOUBLE:    return Arrays.binarySearch((double[]) array, fromIndex, toIndex, (double) key);
            default:        return Arrays.binarySearch((Object[]) array, fromIndex, toIndex, key);
        }
    }

    /**
     * Delegates to the appropriate method for creating a copy and returns the result. Depending on the array type,
     * this method calls {@link Arrays#copyOf(byte[], int)}, {@link Arrays#copyOf(char[], int)}, etc.
     *
     * @param original the array object to copy (e.g. String[] or double[])
     * @param newLength the length of the copy
     * @return copied array of the same type as the original
     */
    public static Object copyOf(Object original, int newLength) {
        ArrayComponentType comp = getArrayComponentType(original);
        switch (comp) {
            case BOOLEAN:   return Arrays.copyOf((boolean[]) original, newLength);
            case BYTE:      return Arrays.copyOf((byte[]) original,    newLength);
            case CHARACTER: return Arrays.copyOf((char[]) original,    newLength);
            case SHORT:     return Arrays.copyOf((short[]) original,   newLength);
            case INTEGER:   return Arrays.copyOf((int[]) original,     newLength);
            case LONG:      return Arrays.copyOf((long[]) original,    newLength);
            case FLOAT:     return Arrays.copyOf((float[]) original,   newLength);
            case DOUBLE:    return Arrays.copyOf((double[]) original,  newLength);
            default:        return Arrays.copyOf((Object[]) original,  newLength);
        }
    }

    /**
     * Delegates to the appropriate method for creating a copy of a range and returns the result. Depending on the
     * array type, this method calls {@link Arrays#copyOfRange(byte[], int, int)},
     * {@link Arrays#copyOfRange(char[], int, int)}, etc.
     *
     * @param original the array object to copy (e.g. String[] or double[])
     * @param from start index of the range to copy (inclusive)
     * @param to end index of the range to copy (exclusive)
     * @return copy of the specified range, of the same type as the original
     */
    public static Object copyOfRange(Object original, int from, int to) {
        ArrayComponentType comp = getArrayComponentType(original);
        switch (comp) {
            case BOOLEAN:   return Arrays.copyOfRange((boolean[]) original, from, to);
            case BYTE:      return Arrays.copyOfRange((byte[]) original,    from, to);
            case CHARACTER: return Arrays.copyOfRange((char[]) original,    from, to);
            case SHORT:     return Arrays.copyOfRange((short[]) original,   from, to);
            case INTEGER:   return Arrays.copyOfRange((int[]) original,     from, to);
            case LONG:      return Arrays.copyOfRange((long[]) original,    from, to);
            case FLOAT:     return Arrays.copyOfRange((float[]) original,   from, to);
            case DOUBLE:    return Arrays.copyOfRange((double[]) original,  from, to);
            default:        return Arrays.copyOfRange((Object[]) original,  from, to);
        }
    }

    /**
     * Delegates to the appropriate method for checking whether two arrays have the same entries, and returns the
     * result. Depending on the array type, this method calls {@link Arrays#equals(byte[], byte[])},
     * {@link Arrays#equals(char[], char[])}, etc.
     *
     * @param a the array to check for equality (e.g. String[] or double[])
     * @param a2 the array to check the first array with (must have the same type)
     * @return whether the two arrays are equal
     */
    public static boolean equals(Object a, Object a2) {
        ArrayComponentType comp = getArrayComponentType(a);
        switch (comp) {
            case BOOLEAN:   return Arrays.equals((boolean[]) a, (boolean[]) a2);
            case BYTE:      return Arrays.equals((byte[]) a,    (byte[]) a2);
            case CHARACTER: return Arrays.equals((char[]) a,    (char[]) a2);
            case SHORT:     return Arrays.equals((short[]) a,   (short[]) a2);
            case INTEGER:   return Arrays.equals((int[]) a,     (int[]) a2);
            case LONG:      return Arrays.equals((long[]) a,    (long[]) a2);
            case FLOAT:     return Arrays.equals((float[]) a,   (float[]) a2);
            case DOUBLE:    return Arrays.equals((double[]) a,  (double[]) a2);
            default:        return Arrays.equals((Object[]) a,  (Object[]) a2);
        }
    }

    /**
     * Delegates to the appropriate method for filling an array with the given value. Depending on the array type,
     * this method calls {@link Arrays#fill(byte[], int, int, byte)}, {@link Arrays#fill(char[], int, int, char)}, etc.
     *
     * @param a the array to fill (e.g. String[] or double[])
     * @param val the value to fill the array with
     */
    public static void fill(Object a, Object val) {
        fill(a, 0, Array.getLength(a), val);
    }


    /**
     * Delegates to the appropriate method for filling an array with the given value. Depending on the array type,
     * this method calls {@link Arrays#fill(byte[], int, int, byte)}, {@link Arrays#fill(char[], int, int, char)}, etc.
     *
     * @param a the array to fill (e.g. String[] or double[])
     * @param fromIndex start index of the range to fill with the value (inclusive)
     * @param toIndex end index of the range to fill with the value (exclusive)
     * @param val the value to fill the array with
     */
    public static void fill(Object a, int fromIndex, int toIndex, Object val) {
        ArrayComponentType comp = getArrayComponentType(a);
        verifyArgumentMatchesComponentType(val, comp, "val");

        switch (comp) {
            case BOOLEAN:   Arrays.fill((boolean[]) a, fromIndex, toIndex, (boolean) val); break;
            case BYTE:      Arrays.fill((byte[]) a,    fromIndex, toIndex, (byte) val);    break;
            case CHARACTER: Arrays.fill((char[]) a,    fromIndex, toIndex, (char) val);    break;
            case SHORT:     Arrays.fill((short[]) a,   fromIndex, toIndex, (short) val);   break;
            case INTEGER:   Arrays.fill((int[]) a,     fromIndex, toIndex, (int) val);     break;
            case LONG:      Arrays.fill((long[]) a,    fromIndex, toIndex, (long) val);    break;
            case FLOAT:     Arrays.fill((float[]) a,   fromIndex, toIndex, (float) val);   break;
            case DOUBLE:    Arrays.fill((double[]) a,  fromIndex, toIndex, (double) val);  break;
            default:        Arrays.fill((Object[]) a,  fromIndex, toIndex, val);
        }
    }

    /**
     * Delegates to the appropriate method for calculating the hash code, and returns the result. Depending on the array
     * type, this method calls {@link Arrays#hashCode(byte[])}, {@link Arrays#hashCode(char[])}, etc.
     *
     * @param a the array to process (e.g. String[] or double[])
     * @return hash code for the array
     */
    public static int hashCode(Object a) {
        ArrayComponentType comp = getArrayComponentType(a);
        switch (comp) {
            case BOOLEAN:   return Arrays.hashCode((boolean[]) a);
            case BYTE:      return Arrays.hashCode((byte[]) a);
            case CHARACTER: return Arrays.hashCode((char[]) a);
            case SHORT:     return Arrays.hashCode((short[]) a);
            case INTEGER:   return Arrays.hashCode((int[]) a);
            case LONG:      return Arrays.hashCode((long[]) a);
            case FLOAT:     return Arrays.hashCode((float[]) a);
            case DOUBLE:    return Arrays.hashCode((double[]) a);
            default:        return Arrays.hashCode((Object[]) a);
        }
    }

    /**
     * Delegates to the appropriate method to parallel-sort the given array. Depending on the array type, this method
     * calls {@link Arrays#parallelSort(byte[], int, int)}, {@link Arrays#parallelSort(char[], int, int)}, etc.
     * <p>
     * The sorting of {@code Object[]} arrays is delegated to {@link Arrays#parallelSort(Comparable[], int, int)},
     * which requires the array component to implement {@link Comparable}. All elements must be mutually comparable.
     * An exception is thrown otherwise.
     * <p>
     * There is no sorting method for boolean arrays in {@code Arrays}. This class provides a fill-in implementation,
     * which does not perform the search in parallel. Developers requiring more efficient sorting of boolean arrays
     * are suggested to use another method.
     *
     * @param a the array to sort (e.g. String[] or double[])
     */
    public static void parallelSort(Object a) {
        parallelSort(a, 0, Array.getLength(a));
    }

    /**
     * Delegates to the appropriate method to parallel-sort the given array. Depending on the array type, this method
     * calls {@link Arrays#parallelSort(byte[], int, int)}, {@link Arrays#parallelSort(char[], int, int)}, etc.
     * <p>
     * The sorting of {@code Object[]} arrays is delegated to {@link Arrays#parallelSort(Comparable[], int, int)},
     * which requires the array component to implement {@link Comparable}. All elements must be mutually comparable.
     * An exception is thrown otherwise.
     * <p>
     * There is no sorting method for boolean arrays in {@code Arrays}. This class provides a fill-in implementation,
     * which does not perform the search in parallel. Developers requiring more efficient sorting of boolean arrays
     * are suggested to use another method.
     *
     * @param a the array to sort (e.g. String[] or double[])
     * @param fromIndex start index (inclusive) of the range that should be sorted
     * @param toIndex end index (exclusive) of the range that should be sorted
     */
    public static void parallelSort(Object a, int fromIndex, int toIndex) {
        ArrayComponentType comp = getArrayComponentType(a);
        switch (comp) {
            case BOOLEAN: simpleBooleanArraySort((boolean[]) a, fromIndex, toIndex); break;
            case BYTE:      Arrays.parallelSort((byte[]) a,   fromIndex, toIndex); break;
            case CHARACTER: Arrays.parallelSort((char[]) a,   fromIndex, toIndex); break;
            case SHORT:     Arrays.parallelSort((short[]) a,  fromIndex, toIndex); break;
            case INTEGER:   Arrays.parallelSort((int[]) a,    fromIndex, toIndex); break;
            case LONG:      Arrays.parallelSort((long[]) a,   fromIndex, toIndex); break;
            case FLOAT:     Arrays.parallelSort((float[]) a,  fromIndex, toIndex); break;
            case DOUBLE:    Arrays.parallelSort((double[]) a, fromIndex, toIndex); break;
            default:    Arrays.parallelSort((Comparable[]) a, fromIndex, toIndex);
        }
    }

    /**
     * Returns a stream of the objects in the given array. Because the streaming methods in {@code Arrays}, such as
     * {@link Arrays#stream(int[])}, return different types of streams, this method returns an object stream which is
     * constructed by getting the elements of the array by index.
     * <p>
     * It may be more efficient to handle array types individually with their respective stream types when performance
     * is crucial.
     *
     * @param array the array to stream over
     * @return stream of the array's elements (as reference types)
     */
    public static Stream<Object> stream(Object array) {
        ArrayComponentType comp = getArrayComponentType(array);
        if (comp == ArrayComponentType.OBJECT) {
            return Arrays.stream((Object[]) array);
        }

        int length = Array.getLength(array);
        return IntStream.range(0, length)
            .mapToObj(idx -> Array.get(array, idx));
    }

    /**
     * Delegates to the appropriate method to sort the given array. Depending on the array type, this method
     * calls {@link Arrays#sort(byte[], int, int)}, {@link Arrays#sort(char[], int, int)}, etc.
     * <p>
     * The sorting of {@code Object[]} arrays is delegated to {@link Arrays#sort(Object[], int, int)},
     * which requires that all elements implement {@link Comparable} and that they be mutually comparable. An exception
     * is thrown otherwise.
     * <p>
     * There is no sorting method for boolean arrays in {@code Arrays}. This class provides a fill-in implementation.
     * Developers requiring more efficient sorting of boolean arrays are suggested to use another implementation.
     *
     * @param a the array to sort (e.g. String[] or double[])
     */
    public static void sort(Object a) {
        sort(a, 0, Array.getLength(a));
    }

    /**
     * Delegates to the appropriate method to sort the given array. Depending on the array type, this method
     * calls {@link Arrays#sort(byte[], int, int)}, {@link Arrays#sort(char[], int, int)}, etc.
     * <p>
     * The sorting of {@code Object[]} arrays is delegated to {@link Arrays#sort(Object[], int, int)},
     * which requires that all elements implement {@link Comparable} and that they be mutually comparable. An exception
     * is thrown otherwise.
     * <p>
     * There is no sorting method for boolean arrays in {@code Arrays}. This class provides a fill-in implementation.
     * Developers requiring more efficient sorting of boolean arrays are suggested to use another implementation.
     *
     * @param a the array to sort (e.g. String[] or double[])
     * @param fromIndex start index (inclusive) of the range that should be sorted
     * @param toIndex end index (exclusive) of the range that should be sorted
     */
    public static void sort(Object a, int fromIndex, int toIndex) {
        ArrayComponentType comp = getArrayComponentType(a);
        switch (comp) {
            case BOOLEAN: simpleBooleanArraySort((boolean[]) a, fromIndex, toIndex); break;
            case BYTE:      Arrays.sort((byte[]) a,   fromIndex, toIndex); break;
            case CHARACTER: Arrays.sort((char[]) a,   fromIndex, toIndex); break;
            case SHORT:     Arrays.sort((short[]) a,  fromIndex, toIndex); break;
            case INTEGER:   Arrays.sort((int[]) a,    fromIndex, toIndex); break;
            case LONG:      Arrays.sort((long[]) a,   fromIndex, toIndex); break;
            case FLOAT:     Arrays.sort((float[]) a,  fromIndex, toIndex); break;
            case DOUBLE:    Arrays.sort((double[]) a, fromIndex, toIndex); break;
            default:        Arrays.sort((Object[]) a, fromIndex, toIndex);
        }
    }

    /**
     * Delegates to the appropriate method to generate a String representation of the given array. Depending on the
     * array type, this method calls {@link Arrays#toString(byte[])}, {@link Arrays#toString(char[])}, etc.
     *
     * @param a the array to sort (e.g. String[] or double[])
     * @return toString of the array
     */
    public static String toString(Object a) {
        ArrayComponentType comp = getArrayComponentType(a);
        switch (comp) {
            case BOOLEAN:   return Arrays.toString((boolean[]) a);
            case BYTE:      return Arrays.toString((byte[]) a);
            case CHARACTER: return Arrays.toString((char[]) a);
            case SHORT:     return Arrays.toString((short[]) a);
            case INTEGER:   return Arrays.toString((int[]) a);
            case LONG:      return Arrays.toString((long[]) a);
            case FLOAT:     return Arrays.toString((float[]) a);
            case DOUBLE:    return Arrays.toString((double[]) a);
            default:        return Arrays.toString((Object[]) a);
        }
    }

    /**
     * Fill-in implementation for a boolean array binary search: allows to search for a value in a boolean array that
     * has been sorted. The result is the index in the array of a matching value (no guarantee which index it is if
     * there are multiple matching values). If the value could not be matched, a negative number is returned as
     * described in binary search methods of {@link Arrays} (e.g. {@link Arrays#binarySearch(char[], int, int, char)}).
     * <p>
     * If the value is not matched, it is guaranteed that this method returns the same negative value as
     * {@link Arrays#binarySearch(Object[], int, int, Object)} if the given array were converted to {@code Boolean[]}.
     * If there is a match, a valid index with the given value is returned, but it is not guaranteed to be the same
     * value as returned by {@code Arrays#binarySearch}.
     *
     * @param array the boolean array to search in
     * @param fromIndex the start index (inclusive) of the range to search in
     * @param toIndex the end index (exclusive) of the range to search in
     * @param value the value to search for
     * @return the matched index, or the "insertion index" as a negative number
     *         (see {@link Arrays#binarySearch(char[], int, int, char)})
     */
    public static int simpleBooleanArrayBinarySearch(boolean[] array, int fromIndex, int toIndex, boolean value) {
        if (fromIndex < toIndex && array[fromIndex] == value) {
            return fromIndex;
        } else if (value) {
            int lastIndex = toIndex - 1;
            if (lastIndex > fromIndex && array[lastIndex]) {
                return lastIndex;
            }
        }
        return value ? (-toIndex - 1) : (-fromIndex - 1);
    }

    /**
     * Naive implementation of an array sort for boolean arrays. Sorts the entries of the array in the specified range.
     * <p>
     * Background: {@link Arrays} has methods for sorting all array types (e.g. {@link Arrays#sort(byte[], int, int)})
     * except for boolean arrays.
     *
     * @param array the array to sort
     * @param fromIndex the start index (inclusive) of the range that should be sorted
     * @param toIndex the end index (exclusive) of the range that should be sorted
     */
    public static void simpleBooleanArraySort(boolean[] array, int fromIndex, int toIndex) {
        int numberOfFalse = 0;
        for (int i = fromIndex; i < toIndex; ++i) {
            if (!array[i]) {
                ++numberOfFalse;
            }
        }

        Arrays.fill(array, fromIndex, fromIndex + numberOfFalse, false);
        Arrays.fill(array, fromIndex + numberOfFalse, toIndex, true);
    }

    private static ArrayComponentType getArrayComponentType(Object array) {
        if (array == null) {
            throw new NullPointerException("array");
        }
        ArrayComponentType componentType = ArrayComponentType.resolveComponentForPrimitiveArrays(array.getClass());
        if (componentType != null) {
            return componentType;
        }
        if (Object[].class.isAssignableFrom(array.getClass())) {
            return ArrayComponentType.OBJECT;
        }
        throw new IllegalArgumentException("Expected an array as argument, but got: " + array.getClass());
    }

    private static void verifyArgumentMatchesComponentType(Object argument, ArrayComponentType componentType,
                                                           String argumentName) {
        if (argument == null) {
            throw new NullPointerException(argumentName);
        } else if (!componentType.matches(argument)) {
            throw new ClassCastException("Expected " + argumentName + " to be a "
                + componentType.name().toLowerCase(Locale.ROOT) + ", instead found: " + argument.getClass());
        }
    }

    /**
     * Represents the component type an array can have.
     */
    enum ArrayComponentType {

        BOOLEAN(Boolean.class),
        BYTE(Byte.class),
        CHARACTER(Character.class),
        SHORT(Short.class),
        INTEGER(Integer.class),
        LONG(Long.class),
        FLOAT(Float.class),
        DOUBLE(Double.class),
        /** Means any extension of Object, such as the component of {@code String[]}, or even {@code int[][]}. */
        OBJECT(Object.class);

        private static final Map<Class<?>, ArrayComponentType> PRIMITIVE_ARRAY_TO_COMPONENT =
            createPrimitiveArrayTypesToComponentMap();
        private final Class<?> referenceType;

        ArrayComponentType(Class<?> referenceType) {
            this.referenceType = referenceType;
        }

        boolean matches(Object obj) {
            return referenceType.isInstance(obj);
        }

        @Nullable
        static ArrayComponentType resolveComponentForPrimitiveArrays(Class<?> clazz) {
            return PRIMITIVE_ARRAY_TO_COMPONENT.get(clazz);
        }

        private static Map<Class<?>, ArrayComponentType> createPrimitiveArrayTypesToComponentMap() {
            Map<Class<?>, ArrayComponentType> map = new HashMap<>();
            map.put(boolean[].class, BOOLEAN);
            map.put(byte[].class, BYTE);
            map.put(char[].class, CHARACTER);
            map.put(short[].class, SHORT);
            map.put(int[].class, INTEGER);
            map.put(long[].class, LONG);
            map.put(float[].class, FLOAT);
            map.put(double[].class, DOUBLE);
            return Collections.unmodifiableMap(map);
        }
    }
}