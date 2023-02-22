/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.timer;

import de.elbe5.base.LocalizedStrings;
import de.elbe5.request.RequestKeys;
import de.elbe5.response.StatusResponse;
import de.elbe5.servlet.ControllerCache;
import de.elbe5.response.CloseDialogResponse;
import de.elbe5.request.RequestData;
import de.elbe5.rights.SystemZone;
import de.elbe5.servlet.Controller;
import de.elbe5.response.IResponse;
import de.elbe5.response.ForwardResponse;

import jakarta.servlet.http.HttpServletResponse;

public class TimerController extends Controller {

    public static final String KEY = "timer";

    private static TimerController instance = null;

    public static void setInstance(TimerController instance) {
        TimerController.instance = instance;
    }

    public static TimerController getInstance() {
        return instance;
    }

    public static void register(TimerController controller){
        setInstance(controller);
        ControllerCache.addController(controller.getKey(),getInstance());
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public IResponse openEditTimerTask(RequestData rdata) {
        checkRights(rdata.hasSystemRight(SystemZone.APPLICATION));
        String name = rdata.getAttributes().getString("timerName");
        TimerTaskData task = Timer.getInstance().getTaskCopy(name);
        rdata.setSessionObject("timerTaskData", task);
        return showEditTimerTask();
    }

    public IResponse saveTimerTask(RequestData rdata) {
        checkRights(rdata.hasSystemRight(SystemZone.APPLICATION));
        TimerTaskData data = (TimerTaskData) rdata.getSessionObject("timerTaskData");
        if (data==null){
            return new StatusResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        data.readSettingsRequestData(rdata);
        if (!rdata.checkFormErrors()) {
            return showEditTimerTask();
        }
        TimerBean ts = TimerBean.getInstance();
        ts.updateTaskData(data);
        Timer.getInstance().loadTask(data.getName());
        rdata.setMessage(LocalizedStrings.string("_taskSaved"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return new CloseDialogResponse("/ctrl/admin/openSystemAdministration");
    }

    private IResponse showEditTimerTask() {
        return new ForwardResponse("/WEB-INF/_jsp/timer/editTimerTask.ajax.jsp");
    }

}
