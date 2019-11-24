import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class DBExchangeRate {
    private Connection connection;
    private String table = "ExchangeRate";

    private String dollar = "R01235";
    private String euro = "R01239";
    private String pound = "R01035";

    public DBExchangeRate(String url, String user, String password) {
        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connection successful!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select id, date from " + table);

            //Выборка последнего элемента из таблице
            int maxID = 0;
            while (resultSet.next()) {
                if (resultSet.getInt(1) > maxID) {
                    maxID = resultSet.getInt(1);
                }
            }

            resultSet = statement.executeQuery("SELECT date FROM " + table + " where id=" + maxID + ";");
            String date1 = null;
            if (resultSet.next()) {
                date1 = formatDate(resultSet.getString(1), "-", "/");
            }
            String date2 = formatDate(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()), "-", "/");

            RequestToTheServer rs = new RequestToTheServer();
            ArrayList<Double> dollars = rs.request(date1, date2, dollar);
            ArrayList<Double> euros = rs.request(date1, date2, euro);
            ArrayList<Double> pounds = rs.request(date1, date2, pound);
            ArrayList<String> dates = rs.getDatesArrayList();

            if (dollars.size() == euros.size() && euros.size() == pounds.size() && pounds.size() == dates.size()) {
                PreparedStatement prepared = connection.prepareStatement(" insert into ExchangeRate (date, Dollar, Euro, Pound) value (?,?,?,?);");
                for (int i = 1; i < dates.size(); i++) {
                    prepared.setString(1, formatDate(dates.get(i), "\\.", ""));
                    prepared.setDouble(2, dollars.get(i));
                    prepared.setDouble(3, euros.get(i));
                    prepared.setDouble(4, pounds.get(i));
                    prepared.execute();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String formatDate(String str, String replaceable, String replacement) {
        String[] strings = str.split(replaceable);
        return strings[2] + replacement + strings[1] + replacement + strings[0];
    }

    public String verification(int command, String login, String password) {
        StringBuilder out = new StringBuilder();
        //TODO Написать проверку для базы
        try {
            PreparedStatement prepared = connection.prepareStatement("select idUsers from Users where login=(?)&&password=(?);");
            prepared.setString(1, login);
            prepared.setString(2, password);
            ResultSet resultSet = prepared.executeQuery();
            if (resultSet.next()) {
                out.append(command);
                out.append(";");
                out.append(resultSet.getInt("idUsers"));
                out.append(";");
                out.append("true");
                out.append("\n");
            } else {
                out.append(command);
                out.append(";");
                out.append(0);
                out.append(";");
                out.append("false");
                out.append("\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return out.toString();
    }

    public String addingUser(int command, String login, String password, String mail, String country, String phone) {
        //Данный метод записи в базу мне вообще не нравится
        //Три запроса в базу ради одной записи
        //Реализация через скрипт внутри БД не отличается
        //Просто логику перекидываем на сервер самой БД
        //Что может повлечь за собой ошибки на сервере БД
        StringBuilder out = new StringBuilder();
        try {
            PreparedStatement request = connection.prepareStatement("select * from Users where login=?");
            request.setString(1, login);
            ResultSet res = request.executeQuery();
            if (res.next()) {
                return 200 + ";false\n";
            }

            PreparedStatement prepared = connection.prepareStatement(" insert into Users (login, password) value (?,?)");
            prepared.setString(1, login);
            prepared.setString(2, password);
            prepared.execute();
            out.append(command + ";");

            prepared = connection.prepareStatement("select idUsers from Users where login=?");
            prepared.setString(1, login);

            int ID;
            ResultSet resultSet = prepared.executeQuery();
            if (resultSet.next()) {
                ID = resultSet.getInt("idUsers");
                prepared = connection.prepareStatement("insert into UsersData (email, phone, country, Users_idUsers) value (?,?,?,?)");
                prepared.setString(1, mail);
                prepared.setString(2, phone);
                prepared.setString(3, country);
                prepared.setInt(4, ID);
                if (!prepared.execute()) {
                    out.append("true\n");
                } else {
                    System.out.println("Error!!!");
                }
            } else {
                out.append("false\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out.toString();
    }

    public String getData(String firstDate, String secondDate, String currency) {
        StringBuilder out = new StringBuilder();
        //System.out.println(currency);
        out.append(300 + ";");

        try {
            Statement prepared = connection.createStatement();
            ResultSet resultSet = prepared.executeQuery("select " + currency + ", DATE from ExchangeRate where DATE >=" + firstDate.replace("-", "") + " and DATE<=" + secondDate.replace("-", "") + ";");
            resultSet.toString();
            while (resultSet.next()) {
                out.append(resultSet.getDate(2).toString() + ";" + resultSet.getDouble(1) + ";");
            }
            out.append("\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out.toString();
    }
}
