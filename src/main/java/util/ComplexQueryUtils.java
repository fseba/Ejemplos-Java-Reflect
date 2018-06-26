package util;

import com.sun.xml.internal.ws.util.StringUtils;
import org.sql2o.Connection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class ComplexQueryUtils {

    public static List<Map<String,Object>> makeComplexQuery(String select, String from, String where){
        String complexSql = String.format("SELECT %s FROM %s", select, from);

        if(where != null && !where.isEmpty()) {
            complexSql += " WHERE " + where;
        }

        try (Connection con = Sql2oDAO.getSql2o().open()) {
            return con.createQuery(complexSql).executeAndFetchTable().asList();
        }
    }

    ///
    //Este metodo mapea una consulta compleja a una lista de objetos de una diferentes clases,
    // las cuales son pasadas como parametro.
    ///
    public static Map<Class,List<Object>> mapComplexQueryToMultipleObjectsList(Set<Class> objectsClasses, List<Map<String,Object>> complexQuery) {
        return objectsClasses
                .stream()
                .map(objectClass -> mapComplexQueryToObjectList(objectClass, complexQuery))
                .filter(Objects::nonNull)
                .filter(objectList -> !objectList.isEmpty())
                .collect(Collectors.toMap(objectList -> objectList.get(0).getClass(), objectList -> objectList));
    }

    ///
    //Este metodo mapea una consulta compleja a una lista de objetos de una sola clase,
    // la cual es pasada como parametro
    ///
    public static List<Object> mapComplexQueryToObjectList(Class objectClass, List<Map<String,Object>> complexQuery) {
        return complexQuery
                .stream()
                .map(row -> {
                    try {
                        Object item = objectClass.newInstance();

                        for(Field field : objectClass.getDeclaredFields()) {
                            if(row.containsKey(field.getName())) {
                                try {
                                    objectClass.getDeclaredMethod("set" + StringUtils.capitalize(field.getName()), field.getType())
                                            .invoke(item, row.get(field.getName()));
                                } catch (NoSuchMethodException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        return item;
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


}
