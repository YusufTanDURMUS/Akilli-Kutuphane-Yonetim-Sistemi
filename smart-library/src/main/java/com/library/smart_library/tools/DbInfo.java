package com.library.smart_library.tools;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;

public class DbInfo {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user = "root";
        String pass = "12345678";
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            DatabaseMetaData md = conn.getMetaData();
            System.out.println("DatabaseProductName: " + md.getDatabaseProductName());
            System.out.println("DatabaseProductVersion: " + md.getDatabaseProductVersion());
            System.out.println("DriverName: " + md.getDriverName());
            System.out.println("DriverVersion: " + md.getDriverVersion());
        }
    }
}
