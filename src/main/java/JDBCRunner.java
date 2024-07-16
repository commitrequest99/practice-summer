import java.math.BigDecimal;
import java.sql.*;

public class JDBCRunner {

    private static final String PROTOCOL = "jdbc:postgresql://";
    private static final String DRIVER = "org.postgresql.Driver";
    private static final String URL_LOCALE_NAME = "localhost/";

    private static final String DATABASE_NAME = "CoinCatalog";

    public static final String DATABASE_URL = PROTOCOL + URL_LOCALE_NAME + DATABASE_NAME;
    public static final String USER_NAME = "postgres";
    public static final String DATABASE_PASS = "postgres";

    public static void main(String[] args) {

        checkDriver();
        checkDB();
        System.out.println("Подключение к базе данных | " + DATABASE_URL + "\n");

        try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER_NAME, DATABASE_PASS)) {
            getCoins(connection); System.out.println();
            addRuler(connection, "Александр 2", "1855-1881"); System.out.println();
            removeRuler(connection, "Александр 2"); System.out.println();
            correctEdgeDescription(connection, "Гладкий", "Самый простой вариант гурта..."); System.out.println();
            getRulersWhoseCoinsOnPortal(connection); System.out.println();
            getTableWithDenominationAndYear(connection); System.out.println();
            getTableWithFullInformation(connection); System.out.println();
            getListCoinsWithBetween(connection); System.out.println();
            ExampleWithFirstJoin(connection); System.out.println();
            ExampleWithSecondJoin(connection); System.out.println();


        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")) {
                System.out.println("Произошло дублирование данных");
            } else throw new RuntimeException(e);
        }
    }

    public static void checkDriver() {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("Нет JDBC-драйвера! Подключите JDBC-драйвер к проекту согласно инструкции.");
            throw new RuntimeException(e);
        }
    }

    public static void checkDB() {
        try {
            Connection connection = DriverManager.getConnection(DATABASE_URL, USER_NAME, DATABASE_PASS);
        } catch (SQLException e) {
            System.out.println("Нет базы данных! Проверьте имя базы, путь к базе или разверните локально резервную копию согласно инструкции");
            throw new RuntimeException(e);
        }
    }

    private static void getRulers(Connection connection) throws SQLException {
        String columnName0 = "id", columnName1 = "name", columnName2 = "years_of_reign";
        int param0 = -1;
        String param1 = null, param2 = null;

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM rulers;");

        while (rs.next()) {
            param2 = rs.getString(columnName2);
            param1 = rs.getString(columnName1);
            param0 = rs.getInt(columnName0);
            System.out.println(param0 + " | " + param1 + " | " + param2);
        }
    }

    static void getCoinEdge(Connection connection) throws SQLException {
        String columnName0 = "id", columnName1 = "name", columnName2 = "description";
        int param0 = -1;
        String param1 = null, param2 = null;

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM coin_edge;");

        while (rs.next()) {
            param0 = rs.getInt(columnName0);
            param1 = rs.getString(columnName1);
            param2 = rs.getString(columnName2);
            System.out.println(param0 + " | " + param1 + " | " + param2);
        }
    }

    private static void getCoins(Connection connection) throws SQLException {
        String columnName0 = "id", columnName1 = "denomination", columnName2 = "year", columnName3 = "id_ruler", columnName4 = "material", columnName5 = "weight", columnName6 = "id_edge", columnName7 = "id_variety";
        int param0 = -1, param2 = -1, param3 = -1, param6 = -1, param7 = -1;
        String param1 = null, param4 = null;
        BigDecimal param5 = null;

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM coins;");

        while (rs.next()) {
            param7 = rs.getInt(columnName7);
            param6 = rs.getInt(columnName6);
            param5 = rs.getBigDecimal(columnName5);
            param4 = rs.getString(columnName4);
            param3 = rs.getInt(columnName3);
            param2 = rs.getInt(columnName2);
            param1 = rs.getString(columnName1);
            param0 = rs.getInt(columnName0);
            System.out.println(param0 + " | " + param1 + " | " + param2 + " | " + param3 + " | " + param4 + " | " + param5 + " | " + param6 + " | " + param7);
        }
    }

    private static void addRuler(Connection connection, String name, String years_of_reign) throws SQLException {
        if (name == null || name.isBlank()) return;

        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO rulers(name, years_of_reign) VALUES (?, ?) returning id;", Statement.RETURN_GENERATED_KEYS);    // создаем оператор шаблонного-запроса с "включаемыми" параметрами - ?
        statement.setString(1, name);
        statement.setString(2, years_of_reign);

        int count = statement.executeUpdate();
        ResultSet rs = statement.getGeneratedKeys();
        if (rs.next()) {
            System.out.println("Идентификатор правителя: " + rs.getInt(1));
        }
        System.out.println("Добавлен "+count+" правитель");
        getRulers(connection);
    }

    private static void removeRuler(Connection connection, String name) throws SQLException {
        if (name == null || name.isBlank()) return;

        PreparedStatement statement = connection.prepareStatement("DELETE from rulers WHERE name=?;");
        statement.setString(1, name);

        int count = statement.executeUpdate();
        System.out.println("Удален "+count+" правитель");
        getRulers(connection);
    }

    private static void correctEdgeDescription(Connection connection, String name, String newDescription) throws SQLException {
        if (name == null || name.isBlank() || newDescription == null || newDescription.isBlank()) return;

        PreparedStatement statement = connection.prepareStatement("UPDATE coin_edge SET description=? WHERE name=?;");
        statement.setString(1, newDescription);
        statement.setString(2, name);


        int count = statement.executeUpdate();
        System.out.println("Обновлено "+count+" описание соответствующего гурта");
        getCoinEdge(connection);
    }

    private static void getRulersWhoseCoinsOnPortal(Connection connection) throws SQLException {
        String columnName0 = "id", columnName1 = "name";
        int param0 = -1;
        String param1 = null;

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT coins.id,rulers.name FROM coins, rulers WHERE coins.id_ruler = rulers.id;");

        while (rs.next()) {
            param1 = rs.getString(columnName1);
            param0 = rs.getInt(columnName0);
            System.out.println(param0 + " | " + param1);
        }
    }

    private static void getTableWithDenominationAndYear(Connection connection) throws SQLException {
        String columnName0 = "id", columnName1 = "denomination_with_year";
        int param0 = -1;
        String param1 = null;

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT id, denomination || ' ' || year || ' г.' AS denomination_with_year FROM coins;");

        while (rs.next()) {
            param1 = rs.getString(columnName1);
            param0 = rs.getInt(columnName0);
            System.out.println(param0 + " | " + param1);
        }
    }

    private static void getTableWithFullInformation(Connection connection) throws SQLException {
        String columnName0 = "id", columnName1 = "full_information";
        int param0 = -1;
        String param1 = null;

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT coins.id, denomination || ', ' || name || ' (' || years_of_reign || '), вес - ' || weight AS full_information FROM coins,rulers WHERE coins.id_ruler=rulers.id ORDER BY weight;");

        while (rs.next()) {
            param1 = rs.getString(columnName1);
            param0 = rs.getInt(columnName0);
            System.out.println(param0 + " | " + param1);
        }
    }

    private static void getListCoinsWithBetween(Connection connection) throws SQLException {
        String columnName0 = "id", columnName1 = "denomination", columnName2 = "year", columnName3 = "id_ruler", columnName4 = "material", columnName5 = "weight", columnName6 = "id_edge", columnName7 = "id_variety";
        int param0 = -1, param2 = -1, param3 = -1, param6 = -1, param7 = -1;
        String param1 = null, param4 = null;
        BigDecimal param5 = null;

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM coins WHERE year BETWEEN 1700 AND 1850 AND weight > 10;");

        while (rs.next()) {
            param7 = rs.getInt(columnName7);
            param6 = rs.getInt(columnName6);
            param5 = rs.getBigDecimal(columnName5);
            param4 = rs.getString(columnName4);
            param3 = rs.getInt(columnName3);
            param2 = rs.getInt(columnName2);
            param1 = rs.getString(columnName1);
            param0 = rs.getInt(columnName0);
            System.out.println(param0 + " | " + param1 + " | " + param2 + " | " + param3 + " | " + param4 + " | " + param5 + " | " + param6 + " | " + param7);
        }
    }

    private static void ExampleWithFirstJoin(Connection connection) throws SQLException {
        String columnName0 = "id", columnName1 = "denomination", columnName2 = "year", columnName3 = "varieties";
        int param0 = -1, param2 = -1;
        String param1 = null, param3 = null;

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT coins.id,coins.denomination,coins.year,varieties.name AS varieties FROM coins JOIN varieties ON coins.id_variety = varieties.id;");

        while (rs.next()) {
            param3 = rs.getString(columnName3);
            param2 = rs.getInt(columnName2);
            param1 = rs.getString(columnName1);
            param0 = rs.getInt(columnName0);
            System.out.println(param0 + " | " + param1 + " | " + param2 + " | " + param3);
        }
    }

    private static void ExampleWithSecondJoin(Connection connection) throws SQLException {
        String columnName0 = "id", columnName1 = "coin", columnName2 = "name_of_variety_edge";
        int param0 = -1;
        String param1 = null, param2 = null;

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT coins.id,coins.denomination || ' ' || coins.year AS coin, coin_edge.name AS name_of_variety_edge FROM coins RIGHT JOIN coin_edge ON coins.id_edge=coin_edge.id;");

        while (rs.next()) {
            param2 = rs.getString(columnName2);
            param1 = rs.getString(columnName1);
            param0 = rs.getInt(columnName0);
            System.out.println(param0 + " | " + param1 + " | " + param2);
        }
    }
}
