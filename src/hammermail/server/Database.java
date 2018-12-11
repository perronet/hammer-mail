package hammermail.server;

import hammermail.core.Mail;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Driver;
import java.sql.SQLTimeoutException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sqlite.JDBC;
import org.sqlite.*;
import org.sqlite.SQLiteConfig.TransactionMode;


public class Database {
    private static final String URL = "src/hammermail/server/hammer.db";
    private static final String DB_URL = "jdbc:sqlite:" + URL;
    private Connection conn = null;
    private boolean exist;


    public Database(boolean dropOld) {
        if (!Files.exists(Paths.get(URL))) 
            exist = false;
        else {
            exist = true;
                if (dropOld) {
                    dropDB();
                    System.out.println("Delete " + URL + " database");
                    exist = false;
                }
            }
        
        configConnection();
        
        if (!exist)
            createTables();
            
    }


    private void createTables() {
        String sqlUser = "CREATE TABLE IF NOT EXISTS users (\n"
                //+ " user_id INTEGER AUTO_INCREMENT PRIMARY KEY,\n"
                + " username VARCHAR(255) PRIMARY KEY,\n"
                + " password VARCHAR(255) NOT NULL\n"
                + ");";

        String sqlEmail = "CREATE TABLE IF NOT EXISTS email (\n"
                
                + " email_id INTEGER PRIMARY KEY , "
                + " sender VARCHAR(255) NOT NULL,\n"
                + " receiver VARCHAR(1023) NOT NULL,\n"
                + " title VARCHAR(255),\n"
                + " email_text TEXT NOT NULL, \n"
                + " time TIMETSAMP NOT NULL,\n"
                + " deleted VARCHAR(1023) DEFAULT ';', \n"
                + " FOREIGN KEY (sender) REFERENCES users(username) "
                + " ON UPDATE CASCADE \n"
                + " ON DELETE CASCADE \n"
                + ");";
//        Connection conn = null;
        Statement stmt = null;
        try {
//            conn = DriverManager.getConnection(DB_URL);
            stmt = conn.createStatement();
            stmt.execute(sqlUser);
            stmt.execute(sqlEmail);
            
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);

        } finally {
            try {
                if (stmt != null) stmt.close();
//                conn.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    protected boolean checkPassword(String userN, String passW) throws SQLException {
        
        if (!isUser(userN)){
            return false;
        }
        
        String dbPsw = "";
//        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
//            conn = DriverManager.getConnection(DB_URL);
            String sql = "SELECT * FROM users WHERE username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userN);
            rs = pstmt.executeQuery();
            dbPsw = rs.getString("password");

        } catch (SQLException ex) {
            if (((SQLiteException)ex).getResultCode().equals(SQLiteErrorCode.SQLITE_BUSY))
                throw new SQLiteException("retrieve", SQLiteErrorCode.SQLITE_BUSY);

            System.out.println("SQLException: " + ex.getMessage());
            ex.printStackTrace(System.out);

        } finally {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                return dbPsw.equals(passW);
        }
    }

    protected boolean isUser(String userN) throws SQLException{
//        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean isUser = false;
        try {
//            conn = DriverManager.getConnection(DB_URL);
            String sql = "SELECT * FROM users WHERE username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userN);
            rs = pstmt.executeQuery();
            if (rs.next())
               isUser = true;

        } catch (SQLException ex) {
            if (((SQLiteException)ex).getResultCode().equals(SQLiteErrorCode.SQLITE_BUSY))
                throw new SQLiteException("retrieve", SQLiteErrorCode.SQLITE_BUSY);

            System.out.println("SQLException: " + ex.getMessage());
            ex.printStackTrace(System.out);

        } finally {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                return isUser;
        }
    }

    protected void addUser(String userN, String psw) throws SQLException {
//        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
//            conn = DriverManager.getConnection(DB_URL);
            String sql = "SELECT * FROM users WHERE username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userN);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                sql = "INSERT INTO users VALUES (?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, userN);
                pstmt.setString(2, psw);
                pstmt.executeUpdate();
            } else {
                System.out.println("User already exists");
            }

        } catch (SQLException ex) {
            if (((SQLiteException)ex).getResultCode().equals(SQLiteErrorCode.SQLITE_BUSY))
                throw new SQLiteException("retrieve", SQLiteErrorCode.SQLITE_BUSY);
            System.out.println("SQLException: " + ex.getMessage());
            ex.printStackTrace(System.out);

        } finally {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
//                dbStatus();
        }
    }

    protected void removeUser(String userN) {
//        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
//            conn = DriverManager.getConnection(DB_URL);
            String sql = "DELETE FROM users WHERE username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userN);
            pstmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            ex.printStackTrace(System.out);

        } finally {
            try {
                if (pstmt != null) pstmt.close();
//                conn.close();
            } catch (SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                ex.printStackTrace(System.out);
            }
        }

    }

    protected void addMail(Mail mail) throws SQLException {
//        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int mailID = -1;
        
        try {
//            conn = DriverManager.getConnection(DB_URL);
            String sql = "INSERT INTO email(sender, receiver, title, email_text, time) "
                           + "VALUES (?, ?, ?, ?, ?)";
         
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, mail.getSender());
            pstmt.setString(2, mail.getReceiver());
            pstmt.setString(3, mail.getTitle());
            pstmt.setString(4, mail.getText());
            pstmt.setTimestamp(5, mail.getDate());
            pstmt.executeUpdate();
            
//            sql = "SELECT email_id FROM email   WHERE sender = ? "
//                           + "AND receiver = ? AND title = ? AND email_text = ? AND time = ?";
//            pstmt = conn.prepareStatement(sql);
//            pstmt.setString(1, mail.getSender());
//            pstmt.setString(2, mail.getReceiver());
//            pstmt.setString(3, mail.getTitle());
//            pstmt.setString(4, mail.getText());
//            pstmt.setTimestamp(5, mail.getDate());
//            rs = pstmt.executeQuery();
//            
//            if(rs.next())
//                mailID = rs.getInt("email_id");
//            else {
//                System.out.println("Temp print, hope that never print!!!");
//            }
            
        } catch (SQLException ex) {
            if (((SQLiteException)ex).getResultCode().equals(SQLiteErrorCode.SQLITE_BUSY))
                throw new SQLiteException("retrieve", SQLiteErrorCode.SQLITE_BUSY);

            System.out.println("SQLException: " + ex.getMessage());
            ex.printStackTrace(System.out);

        } finally {
                if (pstmt != null) pstmt.close();
                if (rs != null) rs.close();
//                return mailID;
        }
    }

    protected void removeMail(int mailID, String toRemove) throws SQLException {
//        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
//            conn = DriverManager.getConnection(DB_URL);
            String sql = "UPDATE email SET deleted = deleted || ? || ';' WHERE email_id = ?";
            pstmt = conn.prepareStatement(sql);
            String replaceID = Integer.toString(mailID);
            pstmt.setString(1, toRemove);
            pstmt.setString(2, replaceID);
            pstmt.executeUpdate();
            checkRemove(mailID);

        }catch (SQLException ex) {
            if (((SQLiteException)ex).getResultCode().equals(SQLiteErrorCode.SQLITE_BUSY))
                throw new SQLiteException("retrieve", SQLiteErrorCode.SQLITE_BUSY);
            
            System.out.println("SQLException: " + ex.getMessage());

        } finally {
                if (pstmt != null) pstmt.close();
        }
    }
   
    protected ArrayList<Mail> getReceivedMails(String userN, Timestamp time) throws SQLException {
        ArrayList<Mail> mailList = new ArrayList<>();
//        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
//            conn = DriverManager.getConnection(DB_URL);
            String sql = "SELECT * FROM email "
                    + "WHERE time > ?"
                    + "AND (receiver = ? "
                    + "OR receiver LIKE ? "
                    + "OR receiver LIKE ? "
                    + "OR receiver LIKE ? )"
                    + "AND (deleted NOT LIKE ? )"
                    + "ORDER BY time ASC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, time);
            pstmt.setString(2, userN);
            pstmt.setString(3, "%;" + userN + ";%");
            pstmt.setString(4, "%" + userN + ";%");
            pstmt.setString(5, "%;" + userN + "%");
            pstmt.setString(6, "%;" + userN + ";%");
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Mail m = new Mail( 
                        rs.getInt("email_id"),
                        rs.getString("sender"), 
                        rs.getString("receiver"),
                        rs.getString("title"), 
                        rs.getString("email_text"), 
                        rs.getTimestamp("time"));
                                                //ENSURE THAT FIX
                if (!(rs.getString("deleted").contains(";"+userN+";"))){
                    mailList.add(m);
                }
                
            }

        } catch (SQLException ex) {
            if (((SQLiteException)ex).getResultCode().equals(SQLiteErrorCode.SQLITE_BUSY))
                throw new SQLiteException("retrieve", SQLiteErrorCode.SQLITE_BUSY);

            System.out.println("SQLException: " + ex.getMessage());
            ex.printStackTrace(System.out);

        } finally {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                return mailList;
        }
    }

    protected ArrayList<Mail> getSentMails(String userN, Timestamp time) throws SQLException {
        ArrayList<Mail> mailList = new ArrayList<>();
//        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
//            conn = DriverManager.getConnection(DB_URL);
            String sql = "SELECT * FROM email "
                        + "WHERE sender = ? "
                        + "AND (deleted NOT LIKE ? )"
                        + "AND time > ? "
                        + "ORDER BY time ASC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userN);
            pstmt.setString(2, "%;" + userN + ";%");
            pstmt.setTimestamp(3, time);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Mail m = new Mail(1,
                        rs.getString("sender"),
                        rs.getString("receiver"),
                        rs.getString("title"),
                        rs.getString("email_text"),
                        rs.getTimestamp("time"));
                
                if (!(rs.getString("deleted").contains(";"+userN+";"))){
                    mailList.add(m);
                }
            }

        } catch (SQLException ex) {
            if (((SQLiteException)ex).getResultCode().equals(SQLiteErrorCode.SQLITE_BUSY))
                throw new SQLiteException("retrieve", SQLiteErrorCode.SQLITE_BUSY);
            System.out.println("SQLException: " + ex.getMessage());
            ex.printStackTrace(System.out);

        } finally {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                return mailList;
        }
    }    

    protected void release(){
        try {
            conn.close();
//            System.out.println("Releasing connection");
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
  }
  
  
  
    //EXTERNAL UTILS
    private void dropDB() {
        try {
            Files.deleteIfExists(Paths.get(URL));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    private void configConnection(){
        try{
            SQLiteConfig conf = new SQLiteConfig();
            conf.setEncoding(SQLiteConfig.Encoding.UTF8);
            conf.setTransactionMode(SQLiteConfig.TransactionMode.DEFFERED);
            conf.setOpenMode(SQLiteOpenMode.FULLMUTEX);

            Driver driver = new JDBC();
            this.conn = driver.connect(DB_URL, conf.toProperties());
            this.conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            ((SQLiteConnection)this.conn).setBusyTimeout(8000);            
            DatabaseMetaData dbMeta = conn.getMetaData();
            
        } catch (SQLException ex) {
//            if (((SQLiteException)ex).getResultCode().equals(SQLiteErrorCode.SQLITE_BUSY))
//                throw new SQLiteException("retrieve", SQLiteErrorCode.SQLITE_BUSY);
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
  
    private boolean isContained(String [] contained, String[] container){
      return Arrays.asList(container).containsAll(Arrays.asList(contained));
  }
  
    private void checkRemove(int mailID){
//        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
//            conn = DriverManager.getConnection(DB_URL);
            String sql = "SELECT * FROM email WHERE email_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, Integer.toString(mailID));
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String [] sender = rs.getString("sender").split(";");
                String [] receivers = rs.getString("receiver").split(";");
                String [] deleted = rs.getString("deleted").split(";");
                
                if (isContained(sender, deleted) && isContained(receivers, deleted)){
                    System.out.println("DB: definitive deletion");
                    sql = "DELETE FROM email WHERE email_id = ?";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, Integer.toString(mailID));
                    pstmt.executeUpdate();                
                }
            }
      
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            ex.printStackTrace(System.out);
        }
  }
  
    protected void resetTables() {
//        Connection conn = null;
        Statement stmt = null;
        try {
//            conn = DriverManager.getConnection(DB_URL);
            stmt = conn.createStatement();
            stmt.executeUpdate("DELETE FROM users");
            stmt.executeUpdate("DELETE FROM email");

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            ex.printStackTrace(System.out);

        } finally {
            try {
                if (stmt != null) stmt.close();
//                conn.close();
            } catch (SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                ex.printStackTrace(System.out);
            }
        }
    }

//    public void dbStatus(){
//        ArrayList<Mail> mailList = new ArrayList<>();;
////        Connection conn = null;
//        PreparedStatement pstmt = null;
//        ResultSet rs = null;
//
//        try {
////            conn = DriverManager.getConnection(DB_URL);
//            String sql = "SELECT * FROM users";
//            pstmt = conn.prepareStatement(sql);
//            rs = pstmt.executeQuery();
//            
//            System.out.println("Hammer DB - Database status \n *********************************** \n");
//            
//            System.out.println("HammerMail users");
//            System.out.println("Username \t | Password (Very safe with HammerMail!!!)");
//            System.out.println("________________________________________________________");
//            while (rs.next()) {
//                System.out.println(rs.getString("username") + "\t | " + rs.getString("password"));
//                System.out.println("________________________________________________________");
//            }
//            
//            
//            sql = "SELECT * FROM email ORDER BY time DESC";
//            pstmt = conn.prepareStatement(sql);
//            rs = pstmt.executeQuery();
//
//            System.out.println("\n\nHammerMail email");
//            System.out.println("Id\t| From\t| To\t| Titolo\t| Text\t| Time\t\t| Deleted From");
//            System.out.println("_________________________________________________________________________________________________");
//            
//            Timestamp t = null;
//            while (rs.next()) {
//                t = new Timestamp(rs.getDate("time").getTime());
//                System.out.println(rs.getInt("email_id") 
//                        + "\t | " + rs.getString("sender")
//                        + "\t | " + rs.getString("receiver")
//                        + "\t | " + rs.getString("title")
//                        + "\t | " + rs.getString("email_text")
//                        + "\t | " + t
//                        + "\t | " + rs.getString("deleted")
//                );
//                System.out.println("_________________________________________________________________________________________________");
//            }
////
//        } catch (SQLException ex) {
//            System.out.println("SQLException: " + ex.getMessage());
//            ex.printStackTrace(System.out);
//
//        } finally {
//            try {
//                if (rs != null) rs.close();
//                if (pstmt != null) pstmt.close();
////                conn.close();
//            } catch (SQLException ex) {
//                System.out.println("SQLException: " + ex.getMessage());
//                ex.printStackTrace(System.out);
//            }
//        }
//
//    }
}