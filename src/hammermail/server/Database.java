/*
 * Copyright (C) 2018 gaet
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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


public class Database {
    
    private static final String dbUrl = "jdbc:sqlite:src/hammermail/server/hammer.db";
    
    public static void createDB() {
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }
        } catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
        }
    }
	
    public static void createTables() {
        String sqlUser = "CREATE TABLE IF NOT EXISTS users (\n"
                        // + "	user_id integer AUTO_INCREMENT PRIMARY KEY,\n"
                         + " username varchar(255) PRIMARY KEY,\n"
                         + " password varchar(255) NOT NULL\n"
                         + ");";

        String sqlEmail = "CREATE TABLE IF NOT EXISTS email (\n"
                        + " from_user varchar(255) NOT NULL,\n"
                        + " to_user varchar(255) NOT NULL,\n"
                        + " time varchar(255) NOT NULL,\n"
                        + " email_text text NOT NULL, \n"
                        + " PRIMARY KEY (from_user, to_user, time), \n"
                        + " FOREIGN KEY (from_user) REFERENCES users(username) ON DELETE CASCADE, \n"
                        + " FOREIGN KEY (to_user) REFERENCES users(username) ON DELETE CASCADE\n"
                        + ");";
        
        try (Connection conn = DriverManager.getConnection(dbUrl);
            Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUser);
            stmt.execute(sqlEmail);
            
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }
    
	//delete?
    protected static void connectDB(String url) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
                ex.printStackTrace(System.out);
            }
        }
    }

    
	//Check the user login credential
    protected static boolean checkPassword(String userN, String passW){
        String dbPsw = "";
        try {
            Connection conn = DriverManager.getConnection(dbUrl);
            String sql = "SELECT * FROM users WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userN);
            ResultSet rs = pstmt.executeQuery();
            dbPsw = rs.getString("password");

            rs.close();
            pstmt.close();
            conn.close();

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            ex.printStackTrace(System.out);
        }
        
        finally{
            //Never get access to user if DB check fail
            return dbPsw.equals(passW);
        }
	}

    protected static void dbAddUser(String userN, String psw) {
        try (Connection conn = DriverManager.getConnection(dbUrl);){

            String sql = "SELECT * FROM users WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userN);
            ResultSet rs = pstmt.executeQuery();

            if (rs.isClosed()){
                sql = "INSERT INTO users VALUES (?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, userN);
                pstmt.setString(2, psw);
                pstmt.executeUpdate();
            } else {
                System.out.println("User already exists");
            }

            rs.close();
            pstmt.close();
            conn.close();

            } catch (SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                ex.printStackTrace(System.out);
            }
    }
    
    protected static void dbRemoveUser(String userN){
        try (Connection conn = DriverManager.getConnection(dbUrl);){
            String sql = "DELETE FROM users WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userN);
            pstmt.executeUpdate();

            pstmt.close();
            conn.close();

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            ex.printStackTrace(System.out);
        }    
    }
 
    protected static void dbAddMail(Mail mail){
        try (Connection conn = DriverManager.getConnection(dbUrl);){
            String sql = "INSERT INTO email VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, mail.getSender());
            pstmt.setString(2, mail.getReceiver());
            pstmt.setString(3, mail.getDate());
            pstmt.setString(4, mail.getText());
            pstmt.executeUpdate();
            
            pstmt.close();
            conn.close();

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            ex.printStackTrace(System.out);
        }
  }

    protected static void dbRemoveMail(Mail mail){
        try (Connection conn = DriverManager.getConnection(dbUrl);){         
            String sql = "DELETE FROM email WHERE from_user = ? AND "
                            + "to_user = ? AND time = ? ";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, mail.getSender());
            pstmt.setString(2, mail.getReceiver());
            pstmt.setString(3, mail.getDate());
            pstmt.executeUpdate();

            pstmt.close();
            conn.close();

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            ex.printStackTrace(System.out);
        }    
    }
	
    protected static ArrayList<Mail> getRiceivedMail(String userN){
        ArrayList<Mail> mailList = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbUrl);) {
            String sql = "SELECT * FROM email WHERE to_user = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userN);
            ResultSet rs = pstmt.executeQuery();
            
            rs.beforeFirst();
            while (rs.next()) {
				//first value only for test, consider meaning of "id" in mail class
				//fourth value only for test, considere how to implement in db
                Mail m = new Mail( 1,
                                /*rs.getInt("user_id"), */
                                rs.getString("from_user"), 
                                rs.getString("to_user"),
                                "title",
                                rs.getString("time"),
                                rs.getString("text"));
                mailList.add(m);
            }
            rs.close();
            pstmt.close();
            conn.close();
        } catch (SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                ex.printStackTrace(System.out);
        }
        return mailList;
    }
	
    protected static ArrayList<Mail> getSentMail(String userN){
        ArrayList<Mail> mailList = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbUrl);) {
            String sql = "SELECT * FROM email WHERE from_user = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userN);
            ResultSet rs = pstmt.executeQuery();
            
            rs.beforeFirst();
            while (rs.next()) {
				//first value only for test, consider meaning of "id" in mail class
				//forth value only for test, considere how to implement in db
                Mail m = new Mail(1,
                                /*rs.getInt("user_id"), */
                                rs.getString("from_user"), 
                                rs.getString("to_user"),
                                "title",
                                rs.getString("time"),
                                rs.getString("text"));
                mailList.add(m);
            }
            rs.close();
            pstmt.close();
            conn.close();
        } catch (SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                ex.printStackTrace(System.out);
        }
        return mailList;
    }


}