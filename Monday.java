package maksimovich.dopp;


import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
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

        Type fieldType = field.getGenericType();
        ParameterizedType genericFieldType;

        try {
            genericFieldType = (ParameterizedType) field.getGenericType();
            //return ((Class<?>) ((ParameterizedType) fieldType).getRawType()).isAssignableFrom(classForInjection);
        } catch (ClassCastException e) {
            return ((Class<?>) fieldType).isAssignableFrom(classForInjection);
        }

        ParameterizedType genericInterface = null;
        Class<?> genericSuperClass = null;
        Type[] classArguments = null;
        String fieldClassSimpleName = genericFieldType.getTypeName().split("<")[0];

        classArguments = getParams(genericFieldType, classForInjection);

//        if (((Class<?>) genericFieldType.getRawType()).isInterface()) {
//            while (classForInjection != Object.class) {
//                for (Type interf : classForInjection.getGenericInterfaces()) {
//                    if (Objects.equals(interf.getTypeName().split("<")[0], fieldClassSimpleName)) {
//                        classArguments = ((ParameterizedType) interf).getActualTypeArguments();
//                        break;
//                    }
//                }
//                try {
//                    classForInjection = (Class<?>) classForInjection.getGenericSuperclass();
//                } catch (Exception e) {
//                    classArguments = ((ParameterizedType) classForInjection.getGenericSuperclass()).getActualTypeArguments();
//                    break;
//                }
//            }
//        } else {
//            while (classForInjection != Object.class) {
//                try {
//                    classForInjection = (Class<?>) classForInjection.getGenericSuperclass();
//                } catch (Exception e) {
//                    classArguments = ((ParameterizedType) classForInjection.getGenericSuperclass()).getActualTypeArguments();
//                    break;
//                }
//            }
//        }
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
                    if (!((Class<?>) fieldArg).isAssignableFrom((Class<?>) classArguments[i])) {
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

    public static Type[] getParams(ParameterizedType genericFieldType, Class<?> classForInjection) {
        String fieldClassSimpleName = genericFieldType.getTypeName().split("<")[0];
        Type[] classArguments;
        if (((Class<?>) genericFieldType.getRawType()).isInterface()) {
            while (classForInjection != Object.class) {
                for (Type interf : classForInjection.getGenericInterfaces()) {
                    if (Objects.equals(interf.getTypeName().split("<")[0], fieldClassSimpleName)) {
                        classArguments = ((ParameterizedType) interf).getActualTypeArguments();
                        return classArguments;
                    }
                }
                try {
                    classForInjection = (Class<?>) classForInjection.getGenericSuperclass();
                } catch (Exception e) {
                    return ((ParameterizedType) classForInjection.getGenericSuperclass()).getActualTypeArguments();

                }
            }
        } else {
            while (classForInjection != Object.class) {
                try {
                    classForInjection = (Class<?>) classForInjection.getGenericSuperclass();
                } catch (Exception e) {
                    return ((ParameterizedType) classForInjection.getGenericSuperclass()).getActualTypeArguments();
                }
            }
        }
        return null;
    }


    public static void main(String[] args) throws NoSuchFieldException, ClassNotFoundException {
        System.out.println(checkField(Empl.class, "ll", Cat.class));

    }
}


