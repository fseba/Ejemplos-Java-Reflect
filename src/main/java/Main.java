import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.xml.internal.ws.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException {
        //--------------------
        //Imprimo el nombre de la clase correspondiente a la instancia 'o'.
        //En este caso, Integer
        //--------------------
        Object o = new Integer(10);
        printClassName(o);

        TestClass testClass = new TestClass();
        testClass.setNombre("Sebastian Flores");
        testClass.setEdad(23);

        ArrayList<Float> numbers = new ArrayList<>();
        numbers.add(new Float(12));
        numbers.add(new Float(57));
        testClass.setNumbers(numbers);

        ArrayList<List<String>> listOfLists = new ArrayList<>();
        ArrayList<String> list1 = new ArrayList<>();
        ArrayList<String> list2 = new ArrayList<>();
        list1.add("Hola");
        list2.add("Mundo");
        list2.add("!!!");
        listOfLists.add(list1);
        listOfLists.add(list2);
        testClass.setListOfLists(listOfLists);

        o = testClass;

        //--------------------
        //Imprimo el nombre de la clase correspondiente a la instancia 'o'.
        //En este caso, TestClass
        //--------------------
        printClassName(o);

        //--------------------
        //Imprimo los nombres de los campos de la clase TestClass
        //--------------------
        printClassFields(TestClass.class);

        //--------------------
        //Imprimo los nombres y los valores de los campos de la instancia 'o'
        //--------------------
        printInstanceState(o);

        //--------------------
        //Convierto un objeto a Json
        //--------------------
        String json = convertObjectToJsonString(o);
        System.out.println("- Conversion de la clase '" + o.getClass().getName() + "' a Json:");
        System.out.println("     * " + json);

        //--------------------
        //Ahora, convierto el Json a objeto
        //--------------------
        TestClass testClass1 = (TestClass) convertJsonToObject(json, TestClass.class);
        printInstanceState(testClass1);
    }

    public static void printClassName(Object object) {
        System.out.println("- Nombre de la clase: " + object.getClass().getName());
    }
    public static void printClassFields(Class c) {
        System.out.println("- Campos de la clase '" + c.getName() +"' :");
        for(Field f : c.getDeclaredFields()) {
            System.out.println("     * " + f.getName());
        }
    }
    public static void printInstanceState (Object obj) throws InvocationTargetException, IllegalAccessException {
        Class objectClass = obj.getClass();

        Map<String,Method> classGetMethods =
                Arrays.stream(objectClass.getDeclaredMethods())
                        .filter(method -> method.getName().startsWith("get"))
                        .distinct()
                        .collect(Collectors.toMap(m-> m.getName(), Function.identity()));

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

    public static String convertObjectToJsonString(Object o) throws InvocationTargetException, IllegalAccessException {
        return convertObjectToJson(o).toString();
    }

    public static JsonNode convertObjectToJson(Object o) throws InvocationTargetException, IllegalAccessException {
        ObjectMapper mapper = new ObjectMapper();
        Class objectClass = o.getClass();

        //Si es un arreglo o lista
        if(objectClass.isArray() || List.class.isAssignableFrom(objectClass)) {
            List<Object> items = objectClass.isArray()
                    ? Arrays.asList(o)
                    : (List) o;
            ArrayNode baseNode = mapper.createArrayNode();

            //Agrego cada uno de los items de la lista al ArrayNode base
            for(Object item : items) {
                baseNode.add(convertObjectToJson(item));
            }
            return baseNode;
        } else {
            //Si es un objeto primitivo
            if(String.class.isAssignableFrom(objectClass) || Number.class.isAssignableFrom(objectClass) || objectClass.isPrimitive()) {
                return mapper.valueToTree(o);
            } else { //Si es un objeto compuesto
                //Obtengo todos los getters
                Map<String,Method> classGetMethods =
                        Arrays.stream(objectClass.getDeclaredMethods())
                                .filter(method -> method.getName().startsWith("get"))
                                .distinct()
                                .collect(Collectors.toMap(m-> m.getName(), Function.identity()));

                //Si no posee ningun getter
                if(classGetMethods.isEmpty()) {
                    return mapper.valueToTree(o);
                } else {
                    ObjectNode baseNode = mapper.createObjectNode();

                    for (Field f : objectClass.getDeclaredFields()) {
                        String getMethodName = "get" + StringUtils.capitalize(f.getName());

                        if (classGetMethods.containsKey(getMethodName)) {
                            baseNode.set(
                                    f.getName(),
                                    convertObjectToJson(classGetMethods.get(getMethodName).invoke(o))
                            );
                        }
                    }

                    return baseNode;
                }
            }
        }
    }

    public static Object convertJsonToObject(String json, Class objectClass) throws IOException, IllegalAccessException, InstantiationException, InvocationTargetException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode baseNode = mapper.readTree(json);
        return convertJsonToObject(baseNode, objectClass);
    }

    public static Object convertJsonToObject(JsonNode baseNode, Class objectClass) throws IOException, IllegalAccessException, InstantiationException, InvocationTargetException {
        //Si es un arreglo, considero que es un arreglo de objetos del tipo pasado como parametro
        if(baseNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) baseNode;

            ArrayList<Object> elements = new ArrayList<>();
            for(Iterator<JsonNode> it = arrayNode.elements(); it.hasNext();) {
                elements.add(convertJsonToObject(it.next(), objectClass));
            }
            return  elements;
        } else if(baseNode.isObject()){

            //Si es un objeto, considero que es un objeto del tipo pasado como parametro
            Object parsedObject = objectClass.newInstance();
            ObjectNode objectNode = (ObjectNode) baseNode;

            Map<String,Field> classFields =
                    Arrays.stream(objectClass.getDeclaredFields())
                            .collect(Collectors.toMap(f-> f.getName(), Function.identity()));

            Map<String,Method> classSetMethods =
                    Arrays.stream(objectClass.getDeclaredMethods())
                            .filter(method -> method.getName().startsWith("set"))
                            .distinct()
                            .collect(Collectors.toMap(m-> m.getName(), Function.identity()));

            ObjectMapper mapper = new ObjectMapper();

            //Invoco a cada setter pasando como parametro el valor del campo del json correspondiente
            for (Iterator<String> it = objectNode.fieldNames(); it.hasNext(); ) {
                String current = it.next();
                String currentMethod = "set" + StringUtils.capitalize(current);

                if(classFields.containsKey(current) && classSetMethods.containsKey(currentMethod)) {
                    classSetMethods.get(currentMethod).invoke(
                            parsedObject,
                            mapper.treeToValue(
                                    objectNode.get(current),
                                    classFields.get(current).getType()
                            )
                    );
                }

            }
            return parsedObject;
        } else {
            return JsonNodeFactory.instance.nullNode();
        }
    }
}
