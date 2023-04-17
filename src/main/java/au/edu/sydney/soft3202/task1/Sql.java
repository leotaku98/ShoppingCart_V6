package au.edu.sydney.soft3202.task1;

import java.io.File;
import java.sql.*;
import java.util.HashMap;

public class Sql {
    private static String dbName = "shoppingbasket.db";
    private static String dbURL = "jdbc:sqlite:" + dbName;

    public Sql(String dbName, String dbURL) {
        this.dbURL = dbURL;
        this.dbName = dbName;
    }

    public static void createDB() {
        File dbFile = new File(dbName);
        if (dbFile.exists()) {
            System.out.println("Database already created");
            return;
        }
        try (Connection ignored = DriverManager.getConnection(dbURL)) {
            // If we get here that means no exception raised from getConnection - meaning it worked
            System.out.println("A new database has been created.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    public static void removeDB() {
        File dbFile = new File(dbName);
        if (dbFile.exists()) {
            boolean result = dbFile.delete();
            if (!result) {
                System.out.println("Couldn't delete existing db file");
                System.exit(-1);
            } else {
                System.out.println("Removed existing DB file.");
            }
        } else {
            System.out.println("No existing DB file.");
        }
    }

    public static void setupDB() {
        String createUserTable = "CREATE TABLE IF NOT EXISTS users ("
                + "user TEXT PRIMARY KEY)";

        String createShoppingBasketTable = "CREATE TABLE IF NOT EXISTS sb ("
                + "user TEXT NOT NULL,"
                + "item TEXT UNIQUE NOT NULL,"
                + "count INTEGER NOT NULL,"
                + "cost REAL NOT NULL,"
                + "PRIMARY KEY(user, item),"
                + "FOREIGN KEY(user) REFERENCES users(user))";

        try (Connection conn = DriverManager.getConnection(dbURL);
             Statement statement = conn.createStatement()) {
            statement.execute(createUserTable);
            statement.execute(createShoppingBasketTable);

            System.out.println("Created tables");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    public static void addStartingData() {

        String addAdmin = """
            INSERT INTO user(user) VALUES ('Admin')
        """;
        String addItem = """
            INSERT INTO shoppingcart(user, item, count, cost) VALUES
                ('Admin', 'apple', 0, 2.5),
                ('Admin', 'orange', 0, 1.25),
                ('Admin', 'pear', 0, 3.0),
                ('Admin', 'banana', 0, 4.95)
            """;

        try (Connection conn = DriverManager.getConnection(dbURL);
             Statement statement = conn.createStatement()) {
            statement.execute(addAdmin);
            statement.execute(addItem);
            System.out.println("Added starting data");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    public static void addDataFromQuestionableSource(String firstName, String lastName, double wam) {
        String addSingleStudentWithParametersSQL =
                """
                INSERT INTO students(first_name, last_name, wam) VALUES
                    (?, ?, ?)
                """;

        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement preparedStatement = conn.prepareStatement(addSingleStudentWithParametersSQL)) {
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setDouble(3, wam);
            preparedStatement.executeUpdate();

            System.out.println("Added questionable data");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    public static void queryDataSimple(double minWAM, double maxWAM) {
        String studentRangeSQL =
                """
                SELECT first_name, last_name
                FROM students WHERE wam > ? AND wam < ?
                """;

        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement preparedStatement = conn.prepareStatement(studentRangeSQL)) {
            preparedStatement.setDouble(1, minWAM);
            preparedStatement.setDouble(2, maxWAM);
            ResultSet results = preparedStatement.executeQuery();

            while (results.next()) {
                System.out.println(
                        results.getString("first_name") + " " +
                                results.getString("last_name"));
            }

            System.out.println("Finished simple query");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    public static void queryDataWithJoin(String uos) {
        String enrolmentSQL =
                """
                SELECT first_name, last_name
                FROM students AS s
                INNER JOIN student_units AS su ON s.id = su.student_id
                INNER JOIN units as u ON su.unit_id = u.id
                WHERE u.code = ?
                """;

        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement preparedStatement = conn.prepareStatement(enrolmentSQL)) {
            preparedStatement.setString(1, uos);
            ResultSet results = preparedStatement.executeQuery();

            while (results.next()) {
                System.out.println(
                        results.getString("first_name") + " " +
                                results.getString("last_name"));
            }

            System.out.println("Finished join query");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        removeDB();
        createDB();
        setupDB();
        addStartingData();
        addDataFromQuestionableSource("New", "Student", 110.0);
        queryDataSimple(65.0, 75.0);
        queryDataWithJoin("SOFT3202");
    }
}
