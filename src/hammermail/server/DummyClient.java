/*
 * Copyright (C) 2018
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
import hammermail.core.Mail;
import hammermail.net.requests.*;
import hammermail.net.responses.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A test class created to test server
 *
 */
public final class DummyClient {

    public DummyClient() throws InterruptedException, IOException, ClassNotFoundException {
        
//          for(int i = 0; i < 50; i++){
//                ArrayList<Mail> mailsToDelete = new ArrayList<>();
//                mailsToDelete.add(new Mail(i, "tano", "marco", "title", "text", new Timestamp(System.currentTimeMillis())));
//                Thread.sleep(30);
//                RequestDeleteMails deleteMailsReq = new RequestDeleteMails(mailsToDelete);
//                deleteMailsReq.SetAuthentication("tano", "psw");
//                testRequest(deleteMailsReq);
//            }
          
        try {
   
            //Testing Signup
            logAction("Testing invalid...");
            RequestSignUp errorReq = new RequestSignUp();
            errorReq.SetAuthentication("ta no", null);//password is not valid
            testRequest(errorReq);

            logAction("Testing signup...");
            RequestSignUp signupReq = new RequestSignUp();
            signupReq.SetAuthentication("tano", "psw");
            testRequest(signupReq);


            logAction("Testing mails...");
            RequestGetMails mailsReq = new RequestGetMails(new Timestamp(0));
            mailsReq.SetAuthentication("tano", "psw");
            testRequest(mailsReq);
            
            
            logAction("Testing new mail sent...");
            Mail newMail = new Mail(Integer.SIZE, "marco", "tano", "is it true?", "Are nails tasty?", new Timestamp(System.currentTimeMillis()));
            RequestSendMail sendMailReq = new RequestSendMail(newMail);
            sendMailReq.SetAuthentication("hello", "world");
            testRequest(sendMailReq);

            logAction("Testing delete mail...");
            ArrayList<Mail> mailsToDelete = new ArrayList<>();
            mailsToDelete.add(newMail);
            RequestDeleteMails deleteMailsReq = new RequestDeleteMails(mailsToDelete);
            deleteMailsReq.SetAuthentication("tano", "psw");
            testRequest(deleteMailsReq);
            deleteMailsReq.SetAuthentication("marco", "psw");
            testRequest(deleteMailsReq);

        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(DummyClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public void testRequest(RequestBase req) throws IOException, ClassNotFoundException {
        Socket socket = new Socket(Inet4Address.getLocalHost().getHostAddress(), Globals.HAMMERMAIL_SERVER_PORT_NUMBER);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        logAction("Socket initialized. Sending Request...");
        out.writeObject(req);
        ResponseBase response = (ResponseBase) in.readObject();
        logAction("Got a response! The type is : " + ((Object) response).getClass());
        
        if (response instanceof ResponseMails){
            System.out.println( "Test mails in the ResponseMail");
            System.out.println( "response received: " + ((ResponseMails)response).getReceivedMails());
            System.out.println("response sent: " + ((ResponseMails)response).getSentMails());
        }
        
    }

    public void logAction(String log) {
        System.out.println("#### CLIENT #### " + log);
    }

}
