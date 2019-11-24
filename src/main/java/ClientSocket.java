import java.io.*;
import java.net.Socket;

public class ClientSocket extends Thread {
    private Socket mySocket;
    private DBExchangeRate mydb;
    private BufferedReader in;
    private BufferedWriter out;

    public ClientSocket(Socket clientSocket, DBExchangeRate mydb) {
        mySocket = clientSocket;
        this.mydb = mydb;
        try {
            in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(mySocket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        run();
    }

    @Override
    public void run() {
        while (true) {
            try {
                String[] request = in.readLine().split(";");
                switch (Integer.parseInt(request[0])){
                    case 100:
                        out.write(mydb.verification(100,request[1],request[2]));
                        out.flush();
                        break;
                    case 200:
                        out.write(mydb.addingUser(200, request[1],request[2],request[3],request[4],request[5]));
                        out.flush();
                        break;
                    case 300:
                        //300;firstDate;secondDate;currency
                        out.write(mydb.getData(request[1],request[2],request[3]));
                        out.flush();
                        break;
                }
            } catch (IOException e) {
                System.out.println("Error!");
                return;
            }
        }
    }
}
