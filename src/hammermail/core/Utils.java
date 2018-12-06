/*
 * Copyright (C) 2018 00mar
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
package hammermail.core;

import hammermail.net.requests.RequestBase;
import hammermail.net.responses.ResponseBase;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 00mar
 */
public class Utils {

    public static boolean isNullOrWhiteSpace(String s) {
        return s == null || s.isEmpty() || s.trim().isEmpty();
    }
    
    public static boolean containsUser(String container, String sub){
        StringTokenizer st = new StringTokenizer(container, ";");
        Boolean isContained = false;
        String test;
        while(st.hasMoreTokens() && !isContained){
            test = st.nextToken();
            if(test.equals(sub)){
                isContained = true;
            }
        }
        return isContained;
    }

    public static int countLines(String str) {
        if (str == null || str.length() == 0) {
            return 0;
        }
        int lines = 1;
        int len = str.length();
        for (int pos = 0; pos < len; pos++) {
            char c = str.charAt(pos);
            if (c == '\r') {
                lines++;
                if (pos + 1 < len && str.charAt(pos + 1) == '\n') {
                    pos++;
                }
            } else if (c == '\n') {
                lines++;
            }
        }
        return lines;
    }
    
    public static ResponseBase sendRequest(RequestBase request) throws ClassNotFoundException, UnknownHostException,  IOException{
        Socket socket = new Socket(Inet4Address.getLocalHost().getHostAddress(), Globals.HAMMERMAIL_SERVER_PORT_NUMBER);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(request);
        return (ResponseBase)in.readObject();
    }

//    public static void clientServerLog(Timestamp ts) throws IOException{    
//        BufferedWriter out = new BufferedWriter(new FileWriter("log.txt"));
//        out.write(ts.toString());
//        out.flush();
//        out.close();
//    }
//
//    public static Timestamp viewLog() {
//        BufferedReader in = null;
//        try {
//            in = new BufferedReader(new FileReader("log.txt"));
//            String time = in.readLine();            
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//            Date parsedDate = dateFormat.parse(time);
//            Timestamp timestamp = new Timestamp(parsedDate.getTime());
////            Timestamp timestamp = Timestamp.valueOf(time);
//            return timestamp;
//
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (ParseException | IOException ex) {
//            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
//        } 
//        //If error occurs then return the epoch
//        //We have to consider a better solution, we don't want to duplicate mail
//        //so we can for example reset the json file, reset the log, and then 
//        //take all the mail, a sort of "restore"
//        return new Timestamp(0);
//    }
//
    
}
