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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author 00mar
 */
public class Utils {

    public static boolean isNullOrWhiteSpace(String s) {
        return s == null || s.isEmpty() || s.trim().isEmpty();
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

}
