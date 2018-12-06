  /*
 * Copyright (C) 2018 sai
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
package hammermail.client;

import hammermail.core.Mail;
import hammermail.core.User;
import static hammermail.core.Utils.clientServerLog;
import static hammermail.core.Utils.sendRequest;
import static hammermail.core.Utils.viewLog;
import hammermail.net.requests.RequestGetMails;
import hammermail.net.responses.ResponseBase;
import hammermail.net.responses.ResponseMails;
import java.io.IOException;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

/**
 *
 * @author sai
 */
public class DaemonTask implements Runnable{
        
    private Socket clientSocket;
    private final User user = Model.getModel().getCurrentUser();

    public DaemonTask(){
    if (clientSocket == null)
        clientSocket = new Socket();
    }

    @Override
    public void run() {
        try {
            while (true){
                System.out.println("Daemon polling: " + Model.getModel().getLastMailStored());
                RequestGetMails requestGetMail = new RequestGetMails(Model.getModel().getLastMailStored());
                requestGetMail.SetAuthentication(user.getUsername(), user.getPassword());

                Thread.sleep(2000);
                System.out.println("Daemon polling");
                ResponseBase response;
                try {
                    response = sendRequest(requestGetMail);
                    Model.getModel().setLastMailStored(new Timestamp(System.currentTimeMillis()));
                    //clientServerLog(new Timestamp(System.currentTimeMillis()));
                    List<Mail> received = ((ResponseMails) response).getReceivedMails();
                    List<Mail> sent = ((ResponseMails) response).getSentMails();
                    
                    if (received.size() > 0){
                        //at login, after having received a mail while offline, enters here twice. to fix
                        Platform.runLater(()->Model.getModel().addMultiple(received));
                        Platform.runLater(()->Model.getModel().addNotify(received));
                        System.out.println("You have " + received.size() + " new mails!");
                    }

                    if (sent.size() > 0){
                        Platform.runLater(()->Model.getModel().addMultiple(sent));
                    }

                } catch (IOException ex) {
                    Logger.getLogger(DaemonTask.class.getName()).log(Level.SEVERE, null, ex);
                }


            }

        } catch (InterruptedException ex) {
                //TODO
        } catch (ClassNotFoundException ex) {
             Logger.getLogger(UILoginController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}  