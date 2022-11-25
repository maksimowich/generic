package maksimovich.dopp;


import java.lang.reflect.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class F<T extends List<? extends Comparable<? super String>>> {
    //extends List<? extends Comparable<? super String>>
    public Map<T, Integer[]> t = new HashMap<>();


    public static boolean checkField(Class<?> sourceClass, String stringField, Class<?> classForInjection) throws NoSuchFieldException {
        Field field = null;
        for (Field f : sourceClass.getDeclaredFields()) {
            if (f.getName().equals(stringField)) {
                field = f;
                break;
            }
        }
        if (field == null) {
            throw new NoSuchFieldException();
        }

        if (!(field.getType().isAssignableFrom(classForInjection))) {
            return false;
        }
        Type fieldType = field.getGenericType();
        ParameterizedType genericFieldType;

        try {
            genericFieldType = (ParameterizedType) field.getGenericType();
        } catch (ClassCastException e) {
            return ((Class<?>) fieldType).isAssignableFrom(classForInjection);
        }

        Type[] classArguments = getParams(genericFieldType, classForInjection);
        return checkForInjection(classArguments, genericFieldType.getActualTypeArguments());
    }


    public static boolean checkForInjection(Type[] classArguments, Type[] fieldArguments) {
        int i = 0;
        for (Type fieldArg : fieldArguments) {
            if (fieldArg instanceof WildcardType) {
                Type[] upperBounds = ((WildcardType) fieldArg).getUpperBounds();
                for (Type upperBound : upperBounds) {
                    try {
                        ParameterizedType temp = (ParameterizedType) upperBound;
                    } catch (Exception e) {
                        try {
                            ParameterizedType temp = (ParameterizedType) classArguments[i];
                        } catch (Exception e1) {
                            if (!((Class<?>) upperBound).isAssignableFrom((Class<?>) classArguments[i])) {
                                return false;
                            }
                            continue;
                        }
                        try {
                            if (!((Class<?>) upperBound).isAssignableFrom((Class<?>) ((ParameterizedType) classArguments[i]).getRawType())) {
                                return false;
                            }
                        } catch (Exception ignored) {

                        }
                    }
                    Type[] params = ((ParameterizedType) upperBound).getActualTypeArguments();
                    Type[] temp = null;
                    try {
                        temp = ((ParameterizedType) classArguments[i]).getActualTypeArguments();
                    } catch (Exception e) {
                        Type[] classParams = getParams((ParameterizedType) upperBound, (Class<?>) classArguments[i]);
                        if (classParams == null) {
                            return false;
                        }
                        if (!checkForInjection(classParams, params)) {
                            return false;
                        }
                        continue;
                    }
                    if (!checkForInjection(temp, params)) {
                        return false;
                    }
                    ;
                }
            } else if (fieldArg instanceof ParameterizedType) {
                Type[] params = ((ParameterizedType) fieldArg).getActualTypeArguments();
                Type[] temp = null;
                try {
                    temp = ((ParameterizedType) classArguments[i]).getActualTypeArguments();
                } catch (Exception e) {
                    Type[] classParams = getParams((ParameterizedType) fieldArg, (Class<?>) classArguments[0]);
                    if (classParams == null) {
                        return false;
                    }
                    if (!checkForInjection(classParams, params)) {
                        return false;
                    }

                }
                return checkForInjection(temp, params);
            } else {
                try {
                    if (((Class<?>) fieldArg) != ((Class<?>) classArguments[i])) {
                        return false;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
            i++;
        }
        return true;
    }

    public static Type[] getInterfaceParamsForConcreteClass(String interfaceName, Class<?> classForInjection) {
        for (Type interf : classForInjection.getGenericInterfaces()) {
            if (Objects.equals(interf.getTypeName().split("<")[0], interfaceName)) {
                return ((ParameterizedType) interf).getActualTypeArguments();
            } else {
                try {
                    ParameterizedType paramInterf = (ParameterizedType) interf;
                    return getInterfaceParamsForConcreteClass(interfaceName,((Class<?>) paramInterf.getRawType()));
                } catch (Exception e) {
                    return getInterfaceParamsForConcreteClass(interfaceName, (Class<?>) interf);
                }
            }
        }
        return null;
    }

    public static Type[] getInterfaceParams(String interfaceName, Class<?> classForInjection) {
        Type currentClass = classForInjection;
        while (currentClass != Object.class) {
            Class<?> currentClassRawClass;
            try {
                currentClassRawClass = (Class<?>) ((ParameterizedType) currentClass).getRawType();
            } catch (Exception e) {
                currentClassRawClass = (Class<?>) currentClass;
            }
            for (Type interf : currentClassRawClass.getGenericInterfaces()) {
                if (interf.getTypeName().split("<")[0].equals(interfaceName)) {
                    try {
                        TypeVariable<?> temp = (TypeVariable<?>) ((ParameterizedType) interf).getActualTypeArguments()[0];
                        try {
                            return ((ParameterizedType) currentClass).getActualTypeArguments();
                        } catch (Exception e) {
                            return null;
                        }
                    } catch (Exception ignored) {
                        return ((ParameterizedType) interf).getActualTypeArguments();
                    }
//                    try {
//                        return ((ParameterizedType) currentClass).getActualTypeArguments();
//                    } catch (Exception e) {
//                        return null;
//                    }
                }
            }
            currentClass = currentClassRawClass.getGenericSuperclass();
        }
        return null;
    }

    public static Type[] getClassParams(String fieldClassName, Class<?> classForInjection) {
        Type currentClass = classForInjection;
        while (currentClass != Object.class) {
            Class<?> currentClassRawClass;
            try {
                currentClassRawClass = (Class<?>) ((ParameterizedType) currentClass).getRawType();
            } catch (Exception e) {
                currentClassRawClass = (Class<?>) currentClass;
            }
            if (currentClassRawClass.getTypeName().equals(fieldClassName)) {
                try {
                    return ((ParameterizedType) currentClass).getActualTypeArguments();
                } catch (Exception e) {
                    return null;
                }
            }
            currentClass = currentClassRawClass.getGenericSuperclass();
        }
        return null;
    }


    public static Type[] getParams(ParameterizedType genericFieldType, Class<?> classForInjection) {
        String fieldClassSimpleName = genericFieldType.getTypeName().split("<")[0];
        if (((Class<?>) genericFieldType.getRawType()).isInterface()) {
            return getInterfaceParams(fieldClassSimpleName, classForInjection);
        } else {
            return getClassParams(fieldClassSimpleName, classForInjection);
        }
    }


    public static void main(String[] args) throws NoSuchFieldException, ClassNotFoundException {

        System.out.println(checkField(Empl.class, "object", String.class) + " String in Object");
        System.out.println(checkField(Empl.class, "string", String.class) + " String in String"); // String in String
        System.out.println(checkField(Empl.class, "compObj", String.class) + " String in Comparable<Object>"); // String in Comparable<Object>
        System.out.println(checkField(Empl.class, "compExtObj", String.class) + " String in Comparable<? extends Object>"); // String in Comparable<? extends Object>
        System.out.println(checkField(Empl.class, "comparableString", String.class) + " String in Comparable<String>"); // String in Comparable<String>
        System.out.println(checkField(Empl.class, "comparableExtendsString", String.class ) + " String in Comparable<? extends String>"); // String in Comparable<? extends String>
        System.out.println(checkField(Empl.class, "comparableExtendsComparableString", String.class ) + " String in Comparable<? extends Comparable<String>>"); // String in Comparable<? extends Comparable<String>>
        System.out.println(checkField(Empl.class, "comparableExtendsComparableNumber", String.class ) + " String in Comparable<? extends Comparable<Number>>"); // String in Comparable<? extends Comparable<Number>>
        System.out.println(checkField(Empl.class, "compExCompExComp", String.class ) + " String in Comparable<? extends Comparable<? extends String>>"); // String in Comparable<? extends Comparable<? extends String>>
        System.out.println(checkField(Empl.class, "compExCompExCompObj", String.class) + " String in Comparable<? extends Comparable<? extends Object>>"); // String in Comparable<? extends Comparable<? extends Object>>
        System.out.println("-------------------------------------------");
        System.out.println(checkField(Empl.class, "mapStringLong", B.class) + " B in Map<String, Long>");
        System.out.println(checkField(Empl.class, "mapExStringExNumber", B.class) + " B in Map<? extends String, ? extends Number>");
        System.out.println(checkField(Empl.class, "mapExCompStrExCompInt", B.class) + " B in Map<? extends Comparable<? extends String>, ? extends Comparable<Long>>");
        System.out.println(checkField(Empl.class, "mapExCompExCompStrExCompInt", B.class) + " B in Map<? extends Comparable<? extends Comparable<String>>, ? extends Comparable<Long>>");
        System.out.println(checkField(Empl.class, "mapExCompExCompObjExCompInt", B.class) + " B in Map<? extends Comparable<? extends Comparable<Object>>, ? extends Comparable<Long>>");
        System.out.println("--------------------------------------------");
        System.out.println(checkField(Empl.class, "a", C.class) + " C in A<?>");
        System.out.println(checkField(Empl.class, "aString", C.class) + " C in A<String>");
        System.out.println(checkField(Empl.class, "aObject", C.class) + " C in A<Object>");

        System.out.println(checkField(Empl.class, "listCompString", ListCat.class) + " ListCat in List<? extends Comparable<? extends String>>");

    }
}


