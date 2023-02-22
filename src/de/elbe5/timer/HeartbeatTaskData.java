/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.timer;

import de.elbe5.base.DateHelper;
import de.elbe5.base.Log;

import java.time.LocalDateTime;

public class HeartbeatTaskData extends TimerTaskData {

    public HeartbeatTaskData(){
        Log.log("creating heartbeat");
    }

    @Override
    public String getName() {
        return "heartbeat";
    }

    @Override
    public boolean execute(LocalDateTime executionTime, LocalDateTime checkTime) {
        Log.log("Heartbeat at " + DateHelper.toHtmlDateTime(TimerBean.getInstance().getServerTime()));
        return true;
    }
}
