package maksimovich.dopp;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

public class Main {
    public static boolean checkForInjection(Class<?> clazz, String fieldName, Class<?> injectedClass) throws NoSuchFieldException, ClassNotFoundException {
        Field field = null;
        for (Field f : clazz.getDeclaredFields()) {
            if (f.getName().equals(fieldName)) {
                field = f;
                break;
            }
        }
        if (field == null) {
            throw new NoSuchFieldException();
        }
        Type fieldType = field.getGenericType();

        Class<?> fieldClass = Class.forName(fieldType.getTypeName().split("<")[0]);
        if (!fieldClass.isAssignableFrom(injectedClass)) {
            return false;
        }
        if (!isGenericType(fieldType)) {
            return true;
        }
        ParameterizedType fieldParamType = (ParameterizedType) fieldType; // т. к. дженерик
        Type[] argsOfGenericField = fieldParamType.getActualTypeArguments();
        Class<?> fieldRawClass = (((Class<?>) ((ParameterizedType) field.getGenericType()).getRawType()));

        if (checkInterfaces(injectedClass, fieldParamType)) {
            return true;
        }

        Type checkingClass = injectedClass.getGenericSuperclass();

        while (checkingClass != Object.class) {
            if (!isGenericType(checkingClass)) {
                if (checkInterfaces((Class<?>) checkingClass, fieldParamType)) {
                    return true;
                } else {
                    checkingClass = ((Class<?>) checkingClass).getGenericSuperclass();
                }
            } else {
                Type[] argsOfCheckingClass = ((ParameterizedType) checkingClass).getActualTypeArguments();
                for (Type checkedClassInterface : Class.forName(checkingClass.getTypeName().split("<")[0]).getGenericInterfaces()) {
                    if (fieldRawClass.isAssignableFrom((Class<?>) ((ParameterizedType) checkedClassInterface).getRawType())) {
                        return checkEquality(argsOfGenericField, argsOfCheckingClass);
                    }
                }
                checkingClass = ((Class<?>) checkingClass).getGenericSuperclass();
            }
        }
        return false;
    }

    public static void main(String[] args) throws NoSuchFieldException, ClassNotFoundException {
        System.out.println(checkForInjection(Empl.class, "map", C.class));
    }

    public static boolean checkInterfaces(Class<?> checkedClass, ParameterizedType fieldType) throws ClassNotFoundException {
        for (Type checkedClassInterface : checkedClass.getGenericInterfaces()) {
            if (Objects.equals(checkedClassInterface.toString(), fieldType.toString())) {
                return true;
            }
        }
        return false;
    }


    public static boolean checkEquality(Type[] t1, Type[] t2) {
        if (t1 == t2) {
            return true;
        }
        if (t1 == null || t2 == null) {
            return false;
        }
        int n = t1.length;
        if (n != t2.length) {
            return false;
        }
        for (int i = 0; i < n; i++)
        {
            if (!t1[i].toString().equals(t2[i].toString())) {
                return false;
            }
        }
        return true;
    }

    public static boolean isGenericType(Type t) {
        try {
            ParameterizedType pt =  (ParameterizedType) t;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
//            if (checkClass(checkingClass, fieldType)) {
//                System.out.println(true);
//                return;
//            } else {
//                try {
//                    paramSuperCheckingClass = (ParameterizedType) (Class.forName(checkingClass.getTypeName().split("<")[0])).getGenericSuperclass();
//                    checkingClass = paramSuperCheckingClass;
//                } catch (Exception e) {
//                    checkingClass = ((Class<?>) checkingClass).getSuperclass();
//                }
//            }


//    public static boolean checkClass(Type checkingClass, Type fieldType) throws ClassNotFoundException {
//        if (checkInterfaces(Class.forName(checkingClass.getTypeName().split("<")[0]), fieldType)) {
//            return true;
//        }
//        try {
//            ParameterizedType fieldParamType = (ParameterizedType) fieldType;
//            ParameterizedType checkingParamClass = (ParameterizedType) checkingClass;
//            Class<?> fieldClass = Class.forName(fieldParamType.getRawType().getTypeName());
//            return fieldClass.isAssignableFrom((Class<?>) checkingClass) & checkEquality(checkingParamClass.getActualTypeArguments(), fieldParamType.getActualTypeArguments());
//        } catch (Exception e) {
//            return false;
//        }
//
//    }



//        try {
//            ParameterizedType fieldParamType = (ParameterizedType) fieldType;
//            Class<?> fieldClass = Class.forName(fieldParamType.getRawType().getTypeName());
//            if (fieldClass.isAssignableFrom(A.class)) {
//                // проверяем интерфейсы
//                if (checkInterfaces(A.class, fieldType)) {
//                    System.out.println(true);
//                }
//                ParameterizedType genericSuperclass = (ParameterizedType) A.class.getGenericSuperclass();
//                if (fieldClass.isAssignableFrom((Class<?>) genericSuperclass.getRawType()) & checkEquality(genericSuperclass.getActualTypeArguments(), fieldParamType.getActualTypeArguments())) {
//                    System.out.println(true);
//                }
//
//            } else {
//                System.out.println(false);
//
//            }
////            System.out.println(fieldClass.isAssignableFrom(A.class));
////            System.out.println(fieldParamType.getRawType().getTypeName());
////            System.out.println(Arrays.toString(fieldParamType.getActualTypeArguments()));
//        } catch (Exception e) {
//            Class<?> cl = field.getType();
//            System.out.println(cl.isAssignableFrom(A.class));
//
//            //System.out.println(t.getTypeName());
//        }
////        try {
//            ParameterizedType at = (ParameterizedType) field.getGenericType();
//            System.out.println(at.getRawType().getTypeName());
//            System.out.println(Arrays.toString(at.getActualTypeArguments()));
//        } catch (Exception e) {
//            Type t = field.getGenericType();
//            System.out.println(t.getTypeName());
//        }
