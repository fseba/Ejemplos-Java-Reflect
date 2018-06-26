/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import org.sql2o.Sql2o;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Daniel
 * Singleton que obtiene el nexo con la BD
 */
public class Sql2oDAO {
    private static final String DB = "jdbc:mysql://localhost:3306/negocio?useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC";
    private static final String DB_USER = "sflores";
    private static final String DB_PASSWORD = "29Dsk2Myk6z@ISw3";
    static Sql2o sql2o;
    
    public static Sql2o getSql2o() {
        if (sql2o == null) {
            sql2o = new Sql2o(DB, DB_USER, DB_PASSWORD);
            sql2o.setDefaultCaseSensitive(true);

            System.out.println(sql2o);
        }
        return sql2o;
    }
}
