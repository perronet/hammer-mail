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
package hammermail.net.requests;

import hammermail.core.Mail;

/**
 * The client is sending a new mail
 * @author 00mar
 */
public class RequestSendMail extends RequestBase {
    Mail mail;

    public Mail getMail() {
        return mail;
    }
    
    public RequestSendMail(Mail mail) {
        this.mail = mail;
    }
    
    public boolean IsMailWellFormed(){
        return mail != null &&
                mail.getSender() != null &&
                mail.getReceiver() != null &&
                mail.getTitle() != null &&
                mail.getText() != null &&
                mail.getDate() != null;
    }
}
