package db;
import com.amazonaws.services.simpleworkflow.flow.Suspendable;
import javafx.scene.control.Tab;

import javax.swing.plaf.nimbus.State;
import javax.xml.transform.Result;
import java.io.*;
import java.sql.Date;
import java.util.*;
import java.sql.*;
import java.util.concurrent.Exchanger;

/**
 * Created by slgu1 on 10/9/15.
 */
//rds client for tweetmap
public class Rds {
    public void init() throws SQLException, ClassNotFoundException, InterruptedException {
        Class.forName("org.postgresql.Driver");
        Properties props = new Properties();
        props.setProperty("user","");
        props.setProperty("password", "");
        conn = DriverManager.getConnection(JDBC_URL, props);
    }

    public void close() throws SQLException{
        conn.close();
    }
    public void createTable() throws SQLException{
        Statement st = conn.createStatement();
        String createString =
                String.format("CREATE TABLE %s (" +
                        "%s DATE, " +
                        "%s VARCHAR(100) PRIMARY KEY," +
                        "%s VARCHAR(100)," +
                        "%s VARCHAR(100)," +
                        "%s VARCHAR(100)," +
                        "%s VARCHAR(100)," +
                                "%s VARCHAR(100)" +
                        ")", TABLE_NAME, CREATE_DATE_FIELD, ID_FIELD, USER_NAME_FIELD,
                        TEXT_FIELD, LONG_FIELD, LAT_FIELD, CAT_FIELD);
        System.out.println("create table:" + createString);
        st.execute(createString);
    }

    public void insertTweet(Tweet item) throws SQLException{
        String preString =
                String.format("INSERT into %s " +
                                "(%s,%s,%s,%s,%s,%s, %s) " +
                                "VALUES (?,?,?,?,?,?,?)",
                        TABLE_NAME,
                        CREATE_DATE_FIELD,
                        ID_FIELD,
                        USER_NAME_FIELD,
                        LONG_FIELD,
                        LAT_FIELD,
                        TEXT_FIELD,
                        CAT_FIELD);
        System.out.println("insert sql:" + preString);
        PreparedStatement st = conn.prepareStatement(
                preString
        );
        st.setDate(1, new java.sql.Date(item.getCreateTime().getTime()));
        st.setString(2, item.getId());
        st.setString(3, item.getUserName());
        st.setString(4, String.valueOf(item.getLontitude()));
        st.setString(5, String.valueOf(item.getLatitude()));
        st.setString(6, item.getText());
        st.setString(7, item.getCategory());
        st.execute();
    }
    public LinkedList <Tweet> getByCategory(String category) throws SQLException {
        ResultSet set = null;
        if (category.equals("all")) {
            Statement st = conn.createStatement();
            set = st.executeQuery(String.format("select * from %s", TABLE_NAME));
        }
        else {
            PreparedStatement st = conn.prepareStatement(String.format("select * from %s where category = ?", TABLE_NAME));
            st.setString(1, category);
            set = st.executeQuery();
        }
        LinkedList <Tweet> resultList = new LinkedList<Tweet>();
        while(set.next()) {
            Tweet t = new Tweet();
            try {
                t.setCreateTime(set.getDate(1));
                t.setId(set.getString(2));
                t.setUserName(set.getString(3));
                t.setText(set.getString(4));
                t.setLontitude(Double.parseDouble(set.getString(5)));
                t.setLatitude(Double.parseDouble(set.getString(6)));
                t.setCategory(set.getString(7));
            }
            catch (Exception e) {
                continue;
            }
            resultList.add(t);
        }
        return resultList;
    }

    public LinkedList <Tweet> getSample(int number) throws SQLException {
        Statement st = conn.createStatement();
        ResultSet set = st.executeQuery(String.format("select * from %s limit %d", TABLE_NAME, number));
        LinkedList <Tweet> resultList = new LinkedList<Tweet>();
        while(set.next()) {
            Tweet t = new Tweet();
            try {
                t.setCreateTime(set.getDate(1));
                t.setId(set.getString(2));
                t.setUserName(set.getString(3));
                t.setText(set.getString(4));
                t.setLontitude(Double.parseDouble(set.getString(5)));
                t.setLatitude(Double.parseDouble(set.getString(6)));
                t.setCategory(set.getString(7));
            }
            catch (Exception e) {
                continue;
            }
            resultList.add(t);
        }
        return resultList;
    }

    public LinkedList <Tweet> getAll() throws SQLException{
        Statement st = conn.createStatement();
        ResultSet set = st.executeQuery(String.format("select * from %s", TABLE_NAME));
        LinkedList <Tweet> resultList = new LinkedList<Tweet>();
        while(set.next()) {
            Tweet t = new Tweet();
            try {
                t.setCreateTime(set.getDate(1));
                t.setId(set.getString(2));
                t.setUserName(set.getString(3));
                t.setText(set.getString(4));
                t.setLontitude(Double.parseDouble(set.getString(5)));
                t.setLatitude(Double.parseDouble(set.getString(6)));
                t.setCategory(set.getString(7));
            }
            catch (Exception e) {
                continue;
            }
            resultList.add(t);
        }
        return resultList;
    }
    public LinkedList <Tweet> searchByKeyWord(String keyword) throws SQLException{
        Statement st = conn.createStatement();
        String str = String.format(
                "SELECT * FROM %s WHERE %s ILIKE",
                TABLE_NAME, TEXT_FIELD) + " '%" + keyword + "%'";
        System.out.println(str);
        ResultSet set = st.executeQuery(str);
        LinkedList <Tweet> resultList = new LinkedList<Tweet>();
        while(set.next()) {
            Tweet t = new Tweet();
            try {
                t.setCreateTime(set.getDate(1));
                t.setId(set.getString(2));
                t.setUserName(set.getString(3));
                t.setText(set.getString(4));
                t.setLontitude(Double.parseDouble(set.getString(5)));
                t.setLatitude(Double.parseDouble(set.getString(6)));
                t.setCategory(set.getString(7));
            }
            catch (Exception e) {
                continue;
            }
            resultList.add(t);
        }
        return resultList;
    }
    public void clearDb() throws SQLException{
        Statement st = conn.createStatement();
        st.execute(String.format("delete from %s", TABLE_NAME));
    }
    public void loadFromLocal(String filename) throws FileNotFoundException, SQLException, IOException{
        final String beginString = "\n";
        final String splitString = "@x@x@x";
        FileInputStream input = new FileInputStream(filename);
        int num = input.available();
        byte [] buffer = new byte[num];
        input.read(buffer);
        String data = new String(buffer);
        Tweet t = new Tweet();
        for (String tweet: data.split(beginString)) {
            if (tweet.startsWith(beginString))
                continue;
            String[] infoArr = tweet.split(splitString);
            if (infoArr.length == 6) {
                t.setId(infoArr[0]);
                t.setUserName(infoArr[1]);
                t.setCreateTime(new java.util.Date(infoArr[2]));
                t.setLontitude(Double.parseDouble(infoArr[3]));
                t.setLatitude(Double.parseDouble(infoArr[4]));
                t.setText(infoArr[5]);
                System.out.println(t);
                try {
                    insertTweet(t);
                }
                catch (Exception e) {
                    continue;
                }
            }
        }
    }
    public void loadFromLocal(String filename, String category) throws FileNotFoundException, SQLException, IOException{
        final String beginString = "\n";
        final String splitString = "@x@x@x";
        FileInputStream input = new FileInputStream(filename);
        int num = input.available();
        byte [] buffer = new byte[num];
        input.read(buffer);
        String data = new String(buffer);
        Tweet t = new Tweet();
        for (String tweet: data.split(beginString)) {
            if (tweet.startsWith(beginString))
                continue;
            String[] infoArr = tweet.split(splitString);
            if (infoArr.length == 6) {
                t.setId(infoArr[0]);
                t.setUserName(infoArr[1]);
                t.setCreateTime(new java.util.Date(infoArr[2]));
                t.setLontitude(Double.parseDouble(infoArr[3]));
                t.setLatitude(Double.parseDouble(infoArr[4]));
                t.setText(infoArr[5]);
                t.setCategory(category);
                System.out.println(t);
                try {
                    insertTweet(t);
                }
                catch (Exception e) {
                    continue;
                }
            }
        }
    }
    public static void main(String [] args) {
        Rds rds = new Rds();
        try {
            rds.init();
            LinkedList <Tweet> res = rds.getSample(10);
            for (Tweet tweet: res) {
                System.out.println(tweet.getText());
            }
            //rds.clearDb();
            //rds.loadFromLocal("data/food.txt", "food");
            //rds.loadFromLocal("data/sport.txt", "sport");
            //rds.loadFromLocal("data/music.txt", "music");
            //rds.createTable();
            /*
            Tweet t = new Tweet();
            t.setId("142857");
            t.setUserName("slgu");
            t.setLatitude(-73.123);
            t.setLontitude(40);
            t.setText("happy new year");
            t.setCreateTime(new Date());
            rds.insertTweet(t);
            */
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("sql error");
        }
    }
    private static final String CREATE_DATE_FIELD = "createdAt";
    private static final String LONG_FIELD = "long";
    private static final String LAT_FIELD = "lat";
    private static final String USER_NAME_FIELD = "username";
    private static final String ID_FIELD = "tweetid";
    private static final String TEXT_FIELD = "text";
    private static final String TABLE_NAME = "tweetmap";
    private static final String CAT_FIELD = "category";
    private static final String JDBC_URL = "jdbc:postgresql://tweetmap.cepwcaj8gsoq.us-east-1.rds.amazonaws.com/tweetmap";
    private Connection conn;
}