package au.edu.sydney.soft3202.task1;
import javax.print.attribute.HashPrintJobAttributeSet;
import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Database {
    private static final String DB_NAME = "fruitbasket.db";
    private static String dbURL = "jdbc:sqlite:" + DB_NAME;
    private Connection connection;

    private void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + DB_NAME);
    }
    private void ensureTable() throws SQLException {
        String sql =
                "CREATE TABLE IF NOT EXISTS users (user TEXT PRIMARY KEY NOT NULL)";
        String createShoppingBasketTable = "CREATE TABLE IF NOT EXISTS sb ("
                + "user TEXT NOT NULL,"
                + "item TEXT NOT NULL,"
                + "count INTEGER NOT NULL,"
                + "cost REAL NOT NULL,"
                + "PRIMARY KEY (user, item)"
                + "FOREIGN KEY(user) REFERENCES users(user))";
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            statement.execute(createShoppingBasketTable);
        }
    }

    private void deleteUsersTable() throws SQLException {
        String sql = "DROP TABLE IF EXISTS users";
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    public void addUser(String name) throws SQLException {
        if (getUser(name) == null){
            String sql = "INSERT INTO users (user) VALUES (?)";
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){
                preparedStatement.setString(1, name);
                preparedStatement.executeUpdate();
            }
        }
    }


    public List<String> getUsers() throws SQLException {
        String sql = "SELECT user FROM users";
        List<String> users = new ArrayList<String>();
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String user = resultSet.getString("user");
                if (!user.equals("Admin")){
                    users.add(user);
                }

            }
        }



        return users;
    }

    public String getUser(String name) throws SQLException {
        String sql = "SELECT user FROM users WHERE user = ?";

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String user = resultSet.getString("user");
                return user;
            }
        }

        return null;
    }

    public void addUsertoSB(String name) throws SQLException {
        String addItem = "INSERT INTO sb(user, item, count, cost) VALUES " +
                "('" + name + "', 'apple', 0, 2.5)," +
                "('" + name + "', 'orange', 0, 1.25)," +
                "('" + name + "', 'pear', 0, 3.0)," +
                "('" + name + "', 'banana', 0, 4.95)";


        try(Statement statement = connection.createStatement()){
            statement.execute(addItem);
        }



    }

    public User getUserSB(String userName) throws SQLException {
        String sql = "SELECT * FROM sb WHERE user = ?";

        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, userName);

            ResultSet result = statement.executeQuery();
            User usr = new User(userName);

            //hhw
            while (result.next()) {
             String item = result.getString("item");
             System.out.println("get userdb:"+item);
             int count = result.getInt("count");
             double cost = result.getDouble("cost");
             Item itm = new Item(item, count, cost);
             usr.addItem(itm);

            }
            return usr;
        }
    }

    public void delUserSB(String name) throws SQLException {

        String delUserFromSb = "DELETE FROM sb WHERE user = ?";
        try(PreparedStatement statement = connection.prepareStatement(delUserFromSb)){
            statement.setString(1, name);
            statement.executeUpdate();
        }

    }

    public void delUserUS(String name) throws SQLException {
        String deleteUser = "DELETE FROM users WHERE user = ?";
        try(PreparedStatement statement = connection.prepareStatement(deleteUser)){
            statement.setString(1, name);
            statement.executeUpdate();
        }
    }

    public ArrayList<User> getItems(){
        return null;
    }
    public void updateItemCount(int newCount, String itemName, String userName){
        String sql = "UPDATE sb SET count = ? WHERE user = ? AND item = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, newCount);
            statement.setString(2, userName);
            statement.setString(3, itemName);

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated == 0) {
                // handle case where item was not found for the given user
            }
        } catch (SQLException e) {
            // handle exception
        }
    }

    public void addNewItem(String itemUser, String newItemName, Double newItemCost) throws SQLException {
        String insertItem = "INSERT INTO sb(user, item, count, cost) VALUES (?, ?, 0, ?)";

        try (PreparedStatement statement = connection.prepareStatement(insertItem)) {
            statement.setString(1, itemUser);
            statement.setString(2, newItemName);
            statement.setDouble(3, newItemCost);
            try {
                statement.executeUpdate();
            } catch (SQLException e) {
                System.out.println("The item already exist!");
            }

        }
    }

    public void removeDB() {
        File dbFile = new File(DB_NAME);
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
    public Database() throws SQLException {
        connect();
        ensureTable();
    }

}





//
//
//
//package au.edu.sydney.soft3202.task1;
//
//import java.io.File;
//import java.sql.*;
//import java.util.HashMap;
//
//public class Sql {
//    private static String dbName = "shoppingbasket.db";
//    private static String dbURL = "jdbc:sqlite:" + dbName;
//
//    public Sql(String dbName, String dbURL) {
//        this.dbURL = dbURL;
//        this.dbName = dbName;
//    }
//
//    public static void createDB() {
//        File dbFile = new File(dbName);
//        if (dbFile.exists()) {
//            System.out.println("Database already created");
//            return;
//        }
//        try (Connection ignored = DriverManager.getConnection(dbURL)) {
//            // If we get here that means no exception raised from getConnection - meaning it worked
//            System.out.println("A new database has been created.");
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//            System.exit(-1);
//        }
//    }
//
//    public static void removeDB() {
//        File dbFile = new File(dbName);
//        if (dbFile.exists()) {
//            boolean result = dbFile.delete();
//            if (!result) {
//                System.out.println("Couldn't delete existing db file");
//                System.exit(-1);
//            } else {
//                System.out.println("Removed existing DB file.");
//            }
//        } else {
//            System.out.println("No existing DB file.");
//        }
//    }
//
//    public static void setupDB() {
//        String createUserTable = "CREATE TABLE IF NOT EXISTS users ("
//                + "user TEXT PRIMARY KEY)";
//
//        String createShoppingBasketTable = "CREATE TABLE IF NOT EXISTS sb ("
//                + "user TEXT NOT NULL,"
//                + "item TEXT UNIQUE NOT NULL,"
//                + "count INTEGER NOT NULL,"
//                + "cost REAL NOT NULL,"
//                + "PRIMARY KEY(user, item),"
//                + "FOREIGN KEY(user) REFERENCES users(user))";
//
//        try (Connection conn = DriverManager.getConnection(dbURL);
//             Statement statement = conn.createStatement()) {
//            statement.execute(createUserTable);
//            statement.execute(createShoppingBasketTable);
//
//            System.out.println("Created tables");
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//            System.exit(-1);
//        }
//    }
//
//    public static void addStartingData() {
//
//        String addAdmin = """
//            INSERT INTO user(user) VALUES ('Admin')
//        """;
//        String addItem = """
//            INSERT INTO shoppingcart(user, item, count, cost) VALUES
//                ('Admin', 'apple', 0, 2.5),
//                ('Admin', 'orange', 0, 1.25),
//                ('Admin', 'pear', 0, 3.0),
//                ('Admin', 'banana', 0, 4.95)
//            """;
//
//        try (Connection conn = DriverManager.getConnection(dbURL);
//             Statement statement = conn.createStatement()) {
//            statement.execute(addAdmin);
//            statement.execute(addItem);
//            System.out.println("Added starting data");
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//            System.exit(-1);
//        }
//    }
//
//
//
//    public static boolean userExist(String username){
//        String checkUserExistsSQL = "SELECT COUNT(*) FROM users WHERE user = ?";
//        try (Connection conn = DriverManager.getConnection(dbURL);
//             PreparedStatement pstmt = conn.prepareStatement(checkUserExistsSQL)) {
//            pstmt.setString(1, username);
//            ResultSet results = pstmt.executeQuery();
//            int count = results.getInt(1);
//            if (count == 0) {
//                return false;
//            } else {
//                return true;
//            }
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//            System.exit(-1);
//        }
//        return false;
//    }
//
//    public static void addDataFromQuestionableSource(String firstName, String lastName, double wam) {
//        String addSingleStudentWithParametersSQL =
//                """
//                INSERT INTO students(first_name, last_name, wam) VALUES
//                    (?, ?, ?)
//                """;
//
//        try (Connection conn = DriverManager.getConnection(dbURL);
//             PreparedStatement preparedStatement = conn.prepareStatement(addSingleStudentWithParametersSQL)) {
//            preparedStatement.setString(1, firstName);
//            preparedStatement.setString(2, lastName);
//            preparedStatement.setDouble(3, wam);
//            preparedStatement.executeUpdate();
//
//            System.out.println("Added questionable data");
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//            System.exit(-1);
//        }
//    }
//
//    public static void queryDataSimple(double minWAM, double maxWAM) {
//        String studentRangeSQL =
//                """
//                SELECT first_name, last_name
//                FROM students WHERE wam > ? AND wam < ?
//                """;
//
//        try (Connection conn = DriverManager.getConnection(dbURL);
//             PreparedStatement preparedStatement = conn.prepareStatement(studentRangeSQL)) {
//            preparedStatement.setDouble(1, minWAM);
//            preparedStatement.setDouble(2, maxWAM);
//            ResultSet results = preparedStatement.executeQuery();
//
//            while (results.next()) {
//                System.out.println(
//                        results.getString("first_name") + " " +
//                                results.getString("last_name"));
//            }
//
//            System.out.println("Finished simple query");
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//            System.exit(-1);
//        }
//    }
//
//    public static void queryDataWithJoin(String uos) {
//        String enrolmentSQL =
//                """
//                SELECT first_name, last_name
//                FROM students AS s
//                INNER JOIN student_units AS su ON s.id = su.student_id
//                INNER JOIN units as u ON su.unit_id = u.id
//                WHERE u.code = ?
//                """;
//
//        try (Connection conn = DriverManager.getConnection(dbURL);
//             PreparedStatement preparedStatement = conn.prepareStatement(enrolmentSQL)) {
//            preparedStatement.setString(1, uos);
//            ResultSet results = preparedStatement.executeQuery();
//
//            while (results.next()) {
//                System.out.println(
//                        results.getString("first_name") + " " +
//                                results.getString("last_name"));
//            }
//
//            System.out.println("Finished join query");
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//            System.exit(-1);
//        }
//    }
//
//    public static void main(String[] args) {
//        removeDB();
//        createDB();
//        setupDB();
//        addStartingData();
//        addDataFromQuestionableSource("New", "Student", 110.0);
//        queryDataSimple(65.0, 75.0);
//        queryDataWithJoin("SOFT3202");
//    }
//}
