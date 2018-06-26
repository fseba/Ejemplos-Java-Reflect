package util;

import com.sun.xml.internal.ws.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ClassUtils {
    public static void printObjectClassName(Object object) {
        System.out.println("- Nombre de la clase: " + object.getClass().getName());
    }
    public static void printClassFields(Class c) {
        System.out.println("- Campos de la clase '" + c.getName() +"' :");
        for(Field f : c.getDeclaredFields()) {
            System.out.println("     * " + f.getName());
        }
    }
    public static Map<String,Method> extractGetMethods(Class cls) {
        return Arrays
                .stream(cls.getDeclaredMethods())
                .filter(method -> method.getName().startsWith("get"))
                .distinct()
                .collect(Collectors.toMap(m-> m.getName(), Function.identity()));
    }
    public static Map<String,Method> extractSetMethods(Class cls) {
        return Arrays
                .stream(cls.getDeclaredMethods())
                .filter(method -> method.getName().startsWith("set"))
                .distinct()
                .collect(Collectors.toMap(m-> m.getName(), Function.identity()));
    }
    public static void printInstanceState (Object obj) throws InvocationTargetException, IllegalAccessException {
        Class objectClass = obj.getClass();
        Map<String,Method> classGetMethods = extractGetMethods(objectClass);

        System.out.println("- Campos y valores de la clase '" + objectClass.getName() +"':");
        for(Field f : objectClass.getDeclaredFields()) {
            String getMethodName = "get" + StringUtils.capitalize(f.getName());

            if(classGetMethods.containsKey(getMethodName)) {
                System.out.println("     * " + objectClass.getName() + "." + f.getName() + " = "
                        + classGetMethods.get(getMethodName).invoke(obj));
            } else {
                System.out.println("     * " + objectClass.getName() + "." + f.getName());
            }
        }
    }
}
