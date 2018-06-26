import com.sun.xml.internal.ws.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;

import dto.ComplexClass;
import dto.Factura;
import dto.FacturaProducto;
import dto.Producto;
import spark.Request;
import util.ClassUtils;
import util.ComplexQueryUtils;
import util.JsonUtils;

public class Main {
    public static void main(String[] args) throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException {
        //--------------------
        //Imprimo el nombre de la clase correspondiente a la instancia 'o'.
        //En este caso, Integer
        //--------------------
        Object o = new Integer(10);
        ClassUtils.printObjectClassName(o);

        //--------------------
        //Creo una instancia de la clase ComplexClass y la asigno al objeto 'o'
        //--------------------
        ComplexClass complexClass = ComplexClass.buildTestInstance();
        o = complexClass;

        //--------------------
        //Imprimo el nombre de la clase correspondiente a la instancia 'o'.
        //En este caso, dto.ComplexClass
        //--------------------
        ClassUtils.printObjectClassName(o);

        //--------------------
        //Imprimo los nombres de los campos de la clase dto.ComplexClass
        //--------------------
        ClassUtils.printClassFields(ComplexClass.class);

        //--------------------
        //Imprimo los nombres y los valores de los campos de la instancia 'o'
        //--------------------
        ClassUtils.printInstanceState(o);

        //--------------------
        //Convierto un objeto a Json
        //--------------------
        String json = JsonUtils.convertObjectToJsonString(o);
        System.out.println("- Conversion de la clase '" + o.getClass().getName() + "' a Json:");
        System.out.println("     * " + json);

        //--------------------
        //Ahora, convierto el Json a objeto
        //--------------------
        ComplexClass complexClass1 = (ComplexClass) JsonUtils.convertJsonToObject(json, ComplexClass.class);
        ClassUtils.printInstanceState(complexClass1);

        //--------------------
        //Realizo una consulta compleja
        //--------------------
        System.out.println("- Complex query result: ");
        List<Map<String,Object>> complexQuery = ComplexQueryUtils.makeComplexQuery(
                "facturas.*,facturasProductos.*,productos.*",
                "facturas join facturasProductos join productos",
                null);
        System.out.println("     * " +complexQuery.get(0).keySet());
        complexQuery.forEach(row -> System.out.println("     * " + row.values()));

        //--------------------
        //Mapeo la consulta compleja a objeto
        //--------------------
        Set<Class> classes = new HashSet<>();
        classes.add(Factura.class);
        classes.add(Producto.class);
        classes.add(FacturaProducto.class);

        Map<Class,List<Object>> mappingResult = ComplexQueryUtils.mapComplexQueryToMultipleObjectsList(classes,complexQuery);

        for(Class c : mappingResult.keySet()) {
            List<Object> objects = mappingResult.get(c);
            ClassUtils.printObjectClassName(objects.get(0));
            for (Object object : objects) {
                ClassUtils.printInstanceState(object);
            }
        }
    }

    public Object getParams2POJOsimple (Object obj, Request request) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        String attribute;
        String attributeValue;
        String setMethodName;
        Method setMethod;
        Class objClass = obj.getClass();
        String className = objClass.getName();

        for (Field field : objClass.getDeclaredFields()) {
            attribute = field.getName(); // leer el nombre del atributo
            attributeValue = request.queryParams(className + "." + attribute); // buscar el valor en request (html) del atributo
            if (attributeValue != null) {
                setMethodName = "set" + StringUtils.capitalize(attribute); // PASA LA PRIMERA A MAYUSCULAS
                switch (field.getGenericType().getTypeName()) {
                    case "java.lang.String":
                        setMethod = objClass.getMethod(setMethodName, String.class);
                        setMethod.invoke(obj, attributeValue); // pass arg con y SIN TRANSFORMACION
                        break;
                    case "java.lang.Double":
                        setMethod = objClass.getMethod(setMethodName, Double.class);
                        setMethod.invoke(obj, Double.valueOf(attributeValue));
                        break;
                    case "java.lang.Integer":
                        setMethod = objClass.getMethod(setMethodName, Integer.class);
                        setMethod.invoke(obj, Integer.valueOf(attributeValue));
                        break;
                    case "java.util.Date":
                        System.out.println("Date " + setMethodName);
                        setMethod = objClass.getMethod(setMethodName, Date.class);
                        setMethod.invoke(obj, Date.from(Instant.parse(attributeValue)));
                        break;
                    default:
                        System.out.println(" getParams2POJOsimple: VALOR ILEGAL DEL TIPO DE PARAMNAME " + field.getGenericType().getTypeName());
                        throw new IllegalArgumentException("VALOR ILEGAL DEL TIPO DE PARAMNAME en request: " + field.getGenericType().getTypeName());
                }
            }
        }
        return obj;
    }

}
