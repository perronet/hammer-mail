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
package hammermail.net.responses;

import hammermail.core.Mail;
import java.util.List;

/**
 * Sent by the user when sending mails
 * @author 00mar
 */
public class ResponseMails extends ResponseBase {
    private List<Mail> receivedMails;
    private List<Mail> sentMails;

    public ResponseMails(List<Mail> rec, List<Mail> sent) {
        this.receivedMails = rec;
        this.sentMails = sent;
    }

    public void setReceivedMails(List<Mail> rec) {
        this.receivedMails = rec;
    }
     
    public List<Mail> getReceivedMails() {
        return receivedMails;
    }

    public void setSentMails(List<Mail> sentMails) {
        this.sentMails = sentMails;
    }
    
    public List<Mail> getSentMails() {
        return sentMails;
    }
}
