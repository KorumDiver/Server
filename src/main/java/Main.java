import java.io.*;
import java.net.ServerSocket;

public class Main {
    final static private String url = "jdbc:mysql://localhost/mydb?serverTimezone=Europe/Moscow&useSSL=false";
    final static private String user = "root";
    final static private String password = "********";
    private static ServerSocket server;
    public static void main(String[] args) {
        DBExchangeRate mydb = new DBExchangeRate(url, user, password);
        mydb.update();

        try {
            server = new ServerSocket(4004);
            while (true) {
                new ClientSocket(server.accept(), mydb);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
