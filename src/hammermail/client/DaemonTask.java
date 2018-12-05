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
        private User user = Model.getModel().getCurrentUser();

        public DaemonTask(){
            if (clientSocket == null)
                clientSocket = new Socket();
        }

        @Override
        public void run() {
            try {
                while (true){
                    System.out.println("Daemon polling: " + viewLog());
                    RequestGetMails requestGetMail = new RequestGetMails(viewLog());
                    requestGetMail.SetAuthentication(user.getUsername(), user.getPassword());

                    Thread.sleep(2000);
                    System.out.println("Daemon polling");
                    ResponseBase response;
                    try {
                        response = sendRequest(requestGetMail);
                        clientServerLog(new Timestamp(System.currentTimeMillis()));
                        List<Mail> received = ((ResponseMails) response).getReceivedMails();
                            List<Mail> sent = ((ResponseMails) response).getSentMails();
                        if (received.size() > 0){
                            Platform.runLater(()->Model.getModel().addMultiple(received));
                            
                            //Popup notification here 
                        }

                            if (sent.size() > 0){
                                Platform.runLater(()->Model.getModel().addMultiple(sent));
                                
//                                for (Mail m : received)
//                                    Model.getModel().storeMail(m);
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

    
//    private Socket clientSocket;
//    private final User user = Model.getModel().getCurrentUser();
//    private final Stage s; //used for notifications
//
//    public DaemonTask(Stage s){
//        this.s = s;
//        if (clientSocket == null)
//            clientSocket = new Socket();
//    }

//    //POPUP NOTIFICATION 
//
//    private void inboxNotify(Mail mail) {
//        final Popup popup = createPopup("You got mail from " + mail.getSender() + " " + mail.getTitle());
//        popup.setOnShown((e) -> { //WindowEvent implementation
//            popup.setX(s.getX() + s.getWidth()/2 - popup.getWidth()/2);
//            popup.setY(s.getY() + s.getHeight()/2 - popup.getHeight()/2);
//            
//            //HAMMER TIME (with multiple mails this will SMASH your ears, i swear i'll move it up to line 70)
//            
//        });        
//        popup.show(s);
//    }
//    
//    private Popup createPopup(String message) {
//        final Popup popup = new Popup();
//        popup.setAutoFix(true);
//        popup.setAutoHide(true);
//        popup.setHideOnEscape(true);
//        Label label = new Label(message);
//        label.setOnMouseReleased((e) -> { popup.hide(); }); //MouseEvent implementation
//        popup.getContent().add(label);
//        return popup;
//    }    