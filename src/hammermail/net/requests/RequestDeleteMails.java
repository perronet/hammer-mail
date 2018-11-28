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
import java.util.ArrayList;
import java.util.List;

/**
 * The client wants to delete some mails
 *
 * @author 00mar
 */
public class RequestDeleteMails extends RequestBase {

    List<Integer> mailsIDsToDelete;//A mail is identified by the sender and the timestamps. The sender is inside the authentication.

    public RequestDeleteMails(List<Mail> mailsToDelete) {
        mailsIDsToDelete = new ArrayList<>(mailsToDelete.size());
        for (Mail m : mailsToDelete) {
            mailsIDsToDelete.add(m.getId());
        }
    }

    public List<Integer> getMailsIDsToDelete() {
        return mailsIDsToDelete;
    }
}
