# Java type resolver
[![Build Status](https://travis-ci.org/ljacqu/JavaTypeResolver.svg?branch=master)](https://travis-ci.org/ljacqu/JavaTypeResolver)
[![Coverage Status](https://coveralls.io/repos/github/ljacqu/JavaTypeResolver/badge.svg?branch=master)](https://coveralls.io/github/ljacqu/JavaTypeResolver?branch=master)
[![Maintainability](https://api.codeclimate.com/v1/badges/a19c4b3ca6ea5ed5d083/maintainability)](https://codeclimate.com/github/ljacqu/JavaTypeResolver/maintainability)

Resolves types from context for Java 8 and above, among other utilities.

## Accessing types and type information

Main features:

1. Resolve generic types from context
1. Access generic type arguments safely & conveniently
1. Convert a type to another type, keeping the generic type info

Example:
```java
public class Demo {
  private List<String> list;

  public static void main(String... args) throws Exception {
    TypeInfo listType = TypeInfo.of(Demo.class.getDeclaredField("list"));

    // Outputs: "Generic type of List is class java.lang.String"
    System.out.println("Generic type of List is " + listType.getTypeArgumentAsClass(0));

    // Outputs: "As an Iterable: java.lang.Iterable<java.lang.String>"
    System.out.println("As an Iterable: " + listType.resolveSuperclass(Iterable.class).getType());

    // Outputs: "List#get returns E, which we know is class java.lang.String"
    Type getReturnRaw = List.class.getDeclaredMethod("get", int.class).getGenericReturnType();
    Type getReturnActual = listType.resolve(getReturnRaw).getType();
    System.out.println("List#get returns " + getReturnRaw + ", which we know is " + getReturnActual);
  }
}
```

## Creating a Type of any implementation

You can use `TypeReference` and `NestedTypeReference` to create
Type instances of any subclass in a compile-safe manner:

1. Create types on the fly with `new TypeReference<Map<String, Double>>() { }`
1. Implementation of all `Type` interfaces with equals and hashCode in line with the implementations from the JRE

```java
public static void main(String... args) {
  TypeInfo mySetType = new TypeReference<Set<Double>>() { };
  // Outputs: "Created set type: java.util.Set<java.lang.Double>"
  System.out.println("Created set type: " + mySetType.getType());

  Type myWildcard = WildcardTypeImpl.newWildcardExtends(String.class);
  // Outputs: "Created wildcard: ? extends java.lang.String"
  System.out.println("Created wildcard: " + myWildcard);

  // Implementations of Type interfaces are available which have the same #equals and #hashCode as the JRE impl.
  ParameterizedType pt = new ParameterizedTypeImpl(Set.class, null, Double.class);
  // Outputs: "Set types equal -> true"
  System.out.println("Set types equal -> " + pt.equals(mySetType.getType()));
}
```

## Array type analysis and creation

1. Get dimension & type of array class
1. Create array class of your definition

```java
public static void main(String... args) {
  AbstractArrayProperties arrayInfo = CommonTypeUtil.getArrayProperty(String[][].class);
  // Outputs: "Component = class java.lang.String, dimension = 2"
  System.out.println("Component = " + arrayInfo.getComponentType() + ", dimension = " + arrayInfo.getDimension());

  Type listType = new TypeReference<List<String>>() { }.getType();
  Type doubleArr = CommonTypeUtil.createArrayType(listType, 3);
  // Outputs: "Created type: java.util.List<java.lang.String>[][][]"
  System.out.println("Created type: " + doubleArr);
}
```