import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class RequestToTheServer {
    private ArrayList<String> datesArrayList = new ArrayList<>();
    private boolean arrayListEmpty=true;

    public ArrayList<Double> request(String date1, String date2, String currency) {
        String httpUrl = "http://www.cbr.ru/scripts/XML_dynamic.asp?date_req1=" + date1 + "&date_req2=" + date2 + "&VAL_NM_RQ=" + currency;
        System.out.println(httpUrl);

        HttpURLConnection connection = null;
        ArrayList<Double> priceArrayList = new ArrayList<>();
        try {
            connection = (HttpURLConnection) new URL(httpUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.connect();

            StringBuilder sb = new StringBuilder();
            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    //System.out.println(line);
                    sb.append(line);
                }
            }


            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(sb.toString())));
            NodeList record = document.getElementsByTagName("Record");
            for (int i = 0; i < record.getLength(); i++) {
                Node date = record.item(i);
                if (arrayListEmpty){
                    datesArrayList.add(date.getAttributes().getNamedItem("Date").getNodeValue());
                }
                priceArrayList.add(Double.parseDouble(date.getChildNodes().item(1).getFirstChild().getNodeValue().replace(",",".")));
            }

            if (arrayListEmpty){
                arrayListEmpty=false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return priceArrayList;
    }

    public ArrayList<String> getDatesArrayList() {
        return datesArrayList;
    }
}
