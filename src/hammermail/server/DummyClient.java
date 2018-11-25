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
package hammermail.server;

import hammermail.core.Globals;
import hammermail.net.requests.RequestGetMails;
import hammermail.net.responses.ResponseBase;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.Socket;
import java.time.Instant;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A test class created to test server
 *
 * @author 00mar
 */
public class DummyClient {

    public DummyClient() {
        try {
            Socket socket = new Socket(Inet4Address.getLocalHost().getHostAddress(), Globals.HAMMERMAIL_SERVER_PORT_NUMBER);
            RequestGetMails req = new RequestGetMails(Date.from(Instant.now()));
            
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            System.out.println("SENDING REQUEST");

            out.writeObject(req);
            
            String str;
            
            ResponseBase response = (ResponseBase)in.readObject();
            System.out.println("This is the client. Got a response: " + response.response);
            System.out.println("This is the client. The type is : " + ((Object)response).getClass());

            
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(DummyClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
