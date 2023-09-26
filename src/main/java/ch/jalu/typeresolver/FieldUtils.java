package ch.jalu.typeresolver;

import ch.jalu.typeresolver.classutil.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class with utilities for processing fields.
 */
public final class FieldUtils {

    private FieldUtils() {
    }

    /**
     * Returns a human-friendly representation of a field, e.g. "ArrayList#size".
     *
     * @param field the field to format
     * @return text to reference a field
     */
    public static String formatField(Field field) {
        return ClassUtils.getSemanticName(field.getDeclaringClass()) + "#" + field.getName();
    }

    /**
     * Specifies whether a field is a "regular" (= non-synthetic) instance field. This method exists for brevity and
     * because it can be easily forgotten to exclude synthetic fields.
     *
     * @param field the field to process
     * @return true if the field is not synthetic and not static, false otherwise
     */
    public static boolean isRegularInstanceField(Field field) {
        return !field.isSynthetic() && !Modifier.isStatic(field.getModifiers());
    }

    /**
     * Specifies whether a field is a "regular" (= non-synthetic) static field. This method exists for brevity and
     * because it can be easily forgotten to exclude synthetic fields.
     *
     * @param field the field to process
     * @return true if the field is not synthetic and static, false otherwise
     */
    public static boolean isRegularStaticField(Field field) {
        return !field.isSynthetic() && Modifier.isStatic(field.getModifiers());
    }

    /**
     * Returns all fields from the given class and its parents, recursively. The fields of the top-most parent
     * in this class's hierarchy are returned first.
     *
     * @param clazz the class whose fields (incl. its parents' fields) should be returned
     * @return a stream of all fields, with top-most parent's fields first, and this class's fields last
     */
    public static Stream<Field> getAllFields(Class<?> clazz) {
        return getAllFields(clazz, true);
    }

    /**
     * Returns all fields from the given class and its parents, recursively. Depending on the parameter, fields are
     * either returned from top-to-bottom or bottom-to-top relative to the class's hierarchy.
     *
     * @param clazz the class whose fields (incl. its parents' fields) should be returned
     * @param topParentFirst true if the top-most parent's fields should come first, false for last
     * @return a stream of all fields, in the specified order
     */
    public static Stream<Field> getAllFields(Class<?> clazz, boolean topParentFirst) {
        LinkedList<Class<?>> classes = new LinkedList<>();
        collectParents(clazz, (topParentFirst ? classes::addFirst : classes::addLast));

        return classes.stream()
            .flatMap(clz -> Arrays.stream(clz.getDeclaredFields()));
    }

    /**
     * Returns all non-synthetic, non-static fields of the given class, including all fields from superclasses.
     *
     * @param clazz the class whose instance fields should be retrieved
     * @return all non-synthetic instance fields
     */
    public static List<Field> collectAllRegularInstanceFields(Class<?> clazz) {
        return getAllFields(clazz, true)
            .filter(FieldUtils::isRegularInstanceField)
            .collect(Collectors.toList());
    }

    /**
     * Returns an optional with a field on the given class having the given name, or an empty optional if the name
     * did not match any field. This method does not check the fields of the class's superclasses.
     *
     * @param clazz the class to check
     * @param name the name of the field to resolve
     * @return optional with the field in the class, or empty optional if there is no match
     */
    public static Optional<Field> tryFindField(Class<?> clazz, String name) {
        try {
            return Optional.of(clazz.getDeclaredField(name));
        } catch (NoSuchFieldException ignore) {
            return Optional.empty();
        }
    }

    /**
     * Returns an optional with a field of the given name on the class or one of its superclasses. If multiple fields
     * match the given name (on different superclasses), the field on the class that is lowest in the hierarchy is
     * returned. An empty optional is returned if no field was matched.
     *
     * @param clazz the class (and its superclasses) to inspect
     * @param name the name of the field to look for
     * @return optional with a field of the given name
     */
    public static Optional<Field> tryFindFieldInClassOrParent(Class<?> clazz, String name) {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            Optional<Field> field = tryFindField(currentClass, name);
            if (field.isPresent()) {
                return field;
            }
            currentClass = currentClass.getSuperclass();
        }
        return Optional.empty();
    }

    /**
     * Collector for a stream of fields to group them by name, with the parameter indicating whether the first
     * encountered field with a given name should be retained, or the last one. This collector does not support
     * parallel streams.
     * <p>
     * Note that this collector is especially useful if you are only dealing with instance fields: for a mechanism
     * that processes the instance fields of classes (e.g. for serialization), you may want to only consider the
     * lowest-most declared field per any given name so that behavior can be overridden.
     * <p>
     * To get <b>all fields</b> grouped by name, use {@code stream.collect(Collectors.groupingBy(Field::getName))}.
     *
     * @param firstFieldWins true if the first encountered field with a given name should be kept;
     *                       false to keep the last one
     * @return collector to collect names by field
     */
    public static Collector<Field, ?, LinkedHashMap<String, Field>> collectByName(boolean firstFieldWins) {
        BiConsumer<LinkedHashMap<String, Field>, Field> accumulator = firstFieldWins
            ? (map, field) -> map.putIfAbsent(field.getName(), field)
            : (map, field) -> map.put(field.getName(), field);

        return Collector.of(LinkedHashMap::new, accumulator,
            (a, b) -> { throw new UnsupportedOperationException(); });
    }

    private static void collectParents(Class<?> clazz, Consumer<Class<?>> classAdder) {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            classAdder.accept(currentClass);
            currentClass = currentClass.getSuperclass();
        }
    }
}
