package com.juanlipuma;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ObjectUtils {
    /**
     * Copy the non-null, accessible instance fields listed in {@code properties} from {@code source} that can be copied
     * over to {@code destination}.
     *
     * @param source      Source object. Can be of any type.
     * @param destination Destination object.
     * @param properties  (Optional) The properties to copy. If null or empty, copy over <b>all</b> common properties.
     * @param <T>         Type of destination.
     * @return Destination with the copied-over fields.
     */
    public static <T> T merge(Object source, T destination, List<String> properties) {
        Stream<Field> copiableFieldsStream = getCommonFields(source, destination).stream()
                .filter(f -> {
                    try {
                        return f.get(source) != null;
                    } catch (IllegalAccessException e) {
                        return false;
                    }
                });
        if (properties != null && !properties.isEmpty()) {
            copiableFieldsStream = copiableFieldsStream.filter(f -> properties.contains(f.getName()));
        }
        copiableFieldsStream.forEach(sourceField -> {
            try {
                Field destinationField = destination.getClass().getField(sourceField.getName());
                destinationField.set(destination, sourceField.get(source));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IllegalStateException("Safe field access was checked prior to copy, but exception still thrown", e);
            }
        });
        return destination;
    }

    /**
     * Equivalent to {@link #merge(Object, T, List)} but copies <i>all</i> shared properties.
     */
    public static <T> T merge(Object source, T destination) {
        return merge(source, destination, null);
    }

    /**
     * Get all accessible fields (ie. accessible from here) from the specified object.
     *
     * @param source Object whose fields to get.
     * @return The accessible fields.
     */
    private static List<Field> getAllAccessibleFields(Object source) {
        return Arrays.stream(source.getClass().getDeclaredFields())
                .filter(field -> {
                    try {
                        field.get(source);
                        return true;
                    } catch (IllegalAccessException e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Calls {@link #getAllAccessibleFields(Object)} and gets their name.
     *
     * @param source Object whose field names to get.
     * @return The accessible field names.
     */
    private static List<String> getAllAccessibleFieldNames(Object source) {
        return getAllAccessibleFields(source).stream()
                .map(Field::getName)
                .collect(Collectors.toList());
    }

    /**
     * Get shared fields between the given objects. Fields are considered the same if:
     * <ul>
     *     <li>They have the exact same name (case-sensitive)</li>
     *     <li>They have the same type</li>
     * </ul>
     *
     * @param a First object
     * @param b Second object
     * @return The fields in {@code a} that are shared with {@code b}.
     */
    private static List<Field> getCommonFields(Object a, Object b) {
        final List<Field> aFields = getAllAccessibleFields(a),
                                    bFields = getAllAccessibleFields(b);
        //noinspection DanglingJavadoc
        return aFields.stream().filter(aField -> {
            // Fields must match in name
            Optional<Field> bFieldOptional = bFields.stream().filter(f -> f.getName().equals(aField.getName())).findFirst();
            if (!bFieldOptional.isPresent()) {
                return false;
            }
            /**
             * Fields must also match type (inspired from {@link Field#equals(Object)}, but without the declaring class check)
             */
            Field bField = bFieldOptional.get();
            if (aField.getType().equals(bField.getType())) {
                return true;
            }
            // Check for boxed vs unboxed types
            //noinspection rawtypes
            List<Class> types = new ArrayList<>();
            types.add(aField.getType());
            types.add(bField.getType());
            return types.contains(boolean.class) && types.contains(Boolean.class)
                    || types.contains(byte.class) && types.contains(Byte.class)
                    || types.contains(char.class) && types.contains(Character.class)
                    || types.contains(float.class) && types.contains(Float.class)
                    || types.contains(int.class) && types.contains(Integer.class)
                    || types.contains(long.class) && types.contains(Long.class)
                    || types.contains(short.class) && types.contains(Short.class)
                    || types.contains(double.class) && types.contains(Double.class);
        }).collect(Collectors.toList());
    }

    /**
     * Get the names of all shared fields between two objects.

     * @return The shared field names.
     * @see #getCommonFields(Object, Object)
     */
    private static List<String> getCommonFieldNames(Object a, Object b) {
        return getCommonFields(a, b).stream().map(Field::getName).collect(Collectors.toList());
    }
}
