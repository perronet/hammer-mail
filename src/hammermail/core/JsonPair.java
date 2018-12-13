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
package hammermail.core;

import java.sql.Timestamp;
import java.util.List;

public class JsonPair {
    private Timestamp lastReq;
    private List<Mail> mails;
    
    public JsonPair(Timestamp ts, List<Mail> mails){
        this.lastReq = ts;
        this.mails = mails;
    }

    public Timestamp getLastReq() {
        return lastReq;
    }

    public List<Mail> getMails() {
        return mails;
    }

    public void setLastReq(Timestamp lastReq) {
        this.lastReq = lastReq;
    }

    public void setMails(List<Mail> mails) {
        this.mails = mails;
    }
    
    
}
