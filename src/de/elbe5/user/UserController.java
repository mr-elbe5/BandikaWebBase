/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.user;

import de.elbe5.application.Configuration;
import de.elbe5.application.MailHelper;
import de.elbe5.base.BaseData;
import de.elbe5.base.BinaryFile;
import de.elbe5.base.LocalizedStrings;
import de.elbe5.base.Log;
import de.elbe5.request.*;
import de.elbe5.rights.SystemZone;
import de.elbe5.servlet.Controller;
import de.elbe5.servlet.ControllerCache;
import de.elbe5.response.*;

import jakarta.servlet.http.HttpServletResponse;

public class UserController extends Controller {

    public static final String KEY = "user";

    private static UserController instance = null;

    public static void setInstance(UserController instance) {
        UserController.instance = instance;
    }

    public static UserController getInstance() {
        return instance;
    }

    public static void register(UserController controller){
        setInstance(controller);
        ControllerCache.addController(controller.getKey(),getInstance());
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public IResponse openLogin(RequestData rdata) {
        return showLogin();
    }

    public IResponse login(RequestData rdata) {
        checkRights(rdata.isPostback());
        String login = rdata.getAttributes().getString("login");
        String pwd = rdata.getAttributes().getString("password");
        if (login.length() == 0 || pwd.length() == 0) {
            rdata.setMessage(LocalizedStrings.string("_notComplete"), RequestKeys.MESSAGE_TYPE_ERROR);
            return openLogin(rdata);
        }
        UserData data = UserBean.getInstance().loginUser(login, pwd);
        if (data == null) {
            Log.info("bad login of "+login);
            rdata.setMessage(LocalizedStrings.string("_badLogin"), RequestKeys.MESSAGE_TYPE_ERROR);
            return openLogin(rdata);
        }
        rdata.setSessionUser(data);
        String next = rdata.getAttributes().getString("next");
        if (!next.isEmpty())
                return new ForwardResponse(next);
        return showHome();
    }

    public IResponse showCaptcha(RequestData rdata) {
        String captcha = UserSecurity.generateCaptchaString();
        rdata.setSessionObject(RequestKeys.KEY_CAPTCHA, captcha);
        BinaryFile data = UserSecurity.getCaptcha(captcha);
        if (data==null){
            return new StatusResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return new MemoryFileResponse(data);
    }

    public IResponse logout(RequestData rdata) {
        rdata.setSessionUser(null);
        rdata.resetSession();
        rdata.setMessage(LocalizedStrings.string("_loggedOut"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        String next = rdata.getAttributes().getString("next");
        if (!next.isEmpty())
            return new ForwardResponse(next);
        return showHome();
    }

    public IResponse openEditUser(RequestData rdata) {
        checkRights(rdata.hasSystemRight(SystemZone.USER));
        int userId = rdata.getId();
        UserData data = UserBean.getInstance().getUser(userId);
        rdata.setSessionObject("userData", data);
        return showEditUser();
    }

    public IResponse openCreateUser(RequestData rdata) {
        checkRights(rdata.hasSystemRight(SystemZone.USER));
        UserData data = new UserData();
        data.setNew(true);
        data.setId(UserBean.getInstance().getNextId());
        rdata.setSessionObject("userData", data);
        return showEditUser();
    }

    public IResponse saveUser(RequestData rdata) {
        checkRights(rdata.hasSystemRight(SystemZone.USER));
        UserData data = (UserData) rdata.getSessionObject("userData");
        data.readSettingsRequestData(rdata);
        if (!rdata.checkFormErrors()) {
            return showEditUser();
        }
        UserBean.getInstance().saveUser(data);
        UserCache.setDirty();
        if (rdata.getUserId() == data.getId()) {
            rdata.setSessionUser(data);
        }
        rdata.setMessage(LocalizedStrings.string("_userSaved"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return new CloseDialogResponse("/ctrl/admin/openPersonAdministration?userId=" + data.getId());
    }

    public IResponse deleteUser(RequestData rdata) {
        checkRights(rdata.hasSystemRight(SystemZone.USER));
        int id = rdata.getId();
        if (id < BaseData.ID_MIN) {
            rdata.setMessage(LocalizedStrings.string("_notDeletable"), RequestKeys.MESSAGE_TYPE_ERROR);
            return new ForwardResponse("/ctrl/admin/openPersonAdministration");
        }
        UserBean.getInstance().deleteUser(id);
        UserCache.setDirty();
        rdata.setMessage(LocalizedStrings.string("_userDeleted"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return new ForwardResponse("/ctrl/admin/openPersonAdministration");
    }

    public IResponse showPortrait(RequestData rdata) {
        int userId = rdata.getId();
        BinaryFile file = UserBean.getInstance().getBinaryPortraitData(userId);
        if (file==null){
            return new StatusResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return new MemoryFileResponse(file);
    }

    public IResponse openProfile(RequestData rdata) {
        checkRights(rdata.isLoggedIn());
        return showProfile();
    }

    public IResponse openChangePassword(RequestData rdata) {
        checkRights(rdata.isLoggedIn());
        return showChangePassword();
    }

    public IResponse changePassword(RequestData rdata) {
        checkRights(rdata.isLoggedIn() && rdata.getUserId() == rdata.getId());
        UserData user = UserBean.getInstance().getUser(rdata.getLoginUser().getId());
        if (user==null){
            return new StatusResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        String oldPassword = rdata.getAttributes().getString("oldPassword");
        String newPassword = rdata.getAttributes().getString("newPassword1");
        String newPassword2 = rdata.getAttributes().getString("newPassword2");
        if (newPassword.length() < UserData.MIN_PASSWORD_LENGTH) {
            rdata.addFormField("newPassword1");
            rdata.addFormError(LocalizedStrings.string("_passwordLengthError"));
            return showChangePassword();
        }
        if (!newPassword.equals(newPassword2)) {
            rdata.addFormField("newPassword1");
            rdata.addFormField("newPassword2");
            rdata.addFormError(LocalizedStrings.string("_passwordsDontMatch"));
            return showChangePassword();
        }
        UserData data = UserBean.getInstance().loginUser(user.getLogin(), oldPassword);
        if (data == null) {
            rdata.addFormField("newPassword1");
            rdata.addFormError(LocalizedStrings.string("_badLogin"));
            return showChangePassword();
        }
        data.setPassword(newPassword);
        UserBean.getInstance().saveUserPassword(data);
        rdata.setMessage(LocalizedStrings.string("_passwordChanged"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return new CloseDialogResponse("/ctrl/user/openProfile");
    }

    public IResponse openChangeProfile(RequestData rdata) {
        checkRights(rdata.isLoggedIn());
        return showChangeProfile();
    }

    public IResponse changeProfile(RequestData rdata) {
        int userId = rdata.getId();
        checkRights(rdata.isLoggedIn() && rdata.getUserId() == userId);
        UserData data = UserBean.getInstance().getUser(userId);
        data.readProfileRequestData(rdata);
        if (!rdata.checkFormErrors()) {
            return showChangeProfile();
        }
        UserBean.getInstance().saveUserProfile(data);
        rdata.setSessionUser(data);
        UserCache.setDirty();
        rdata.setMessage(LocalizedStrings.string("_userSaved"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return new CloseDialogResponse("/ctrl/user/openProfile");
    }

    public IResponse openRegistration(RequestData rdata) {
        rdata.getAttributes().put("userData", new UserData());
        rdata.setSessionObject(RequestKeys.KEY_CAPTCHA, UserSecurity.generateCaptchaString());
        return showRegistration();
    }

    public IResponse register(RequestData rdata) {
        UserData user = new UserData();
        rdata.getAttributes().put("userData", user);
        user.readRegistrationRequestData(rdata);
        if (!rdata.checkFormErrors()) {
            return showRegistration();
        }
        if (UserBean.getInstance().doesLoginExist(user.getLogin())) {
            rdata.addFormField("login");
            rdata.addFormError(LocalizedStrings.string("_loginExistsError"));
        }
        if (UserBean.getInstance().doesEmailExist(user.getEmail())) {
            rdata.addFormField("email");
            rdata.addFormError(LocalizedStrings.string("_emailInUseError"));
        }
        String captchaString = rdata.getAttributes().getString("captcha");
        if (!captchaString.equals(rdata.getSessionObject(RequestKeys.KEY_CAPTCHA))) {
            rdata.addFormField("captcha");
            rdata.addFormError(LocalizedStrings.string("_captchaError"));
        }
        if (!rdata.hasFormError()) {
            return showRegistration();
        }
        user.setApproved(false);
        user.setApprovalCode(UserSecurity.getApprovalString());
        user.setId(UserBean.getInstance().getNextId());
        if (!UserBean.getInstance().saveUser(user)) {
            setSaveError(rdata);
            return showRegistration();
        }
        String mailText = LocalizedStrings.string("_registrationVerifyMail") + " " + rdata.getSessionHost() + "/ctrl/user/verifyEmail/" + user.getId() + "?approvalCode=" + user.getApprovalCode();
        if (!MailHelper.sendPlainMail(user.getEmail(), LocalizedStrings.string("_registrationRequest"), mailText)) {
            rdata.setMessage(LocalizedStrings.string("_emailError"), RequestKeys.MESSAGE_TYPE_ERROR);
            return showRegistration();
        }
        return showRegistrationDone();
    }

    public IResponse verifyEmail(RequestData rdata) {
        int userId = rdata.getId();
        String approvalCode = rdata.getAttributes().getString("approvalCode");
        UserData data = UserBean.getInstance().getUser(userId);
        if (approvalCode.isEmpty() || !approvalCode.equals(data.getApprovalCode())) {
            rdata.setMessage(LocalizedStrings.string("_emailVerificationFailed"), RequestKeys.MESSAGE_TYPE_ERROR);
            return showHome();
        }
        UserBean.getInstance().saveUserVerifyEmail(data);
        String mailText = LocalizedStrings.string("_registrationRequestMail") + " " + data.getName() + "(" + data.getId() + ")";
        if (!MailHelper.sendPlainMail(Configuration.getMailReceiver(), LocalizedStrings.string("_registrationRequest"), mailText)) {
            rdata.setMessage(LocalizedStrings.string("_emailError"), RequestKeys.MESSAGE_TYPE_ERROR);
            return showRegistration();
        }
        return showEmailVerification();
    }

    protected IResponse showProfile() {
        JspInclude jsp = new JspInclude("/WEB-INF/_jsp/user/profile.jsp");
        return new MasterResponse(MasterResponse.DEFAULT_MASTER, jsp);
    }

    protected IResponse showChangePassword() {
        return new ForwardResponse("/WEB-INF/_jsp/user/changePassword.ajax.jsp");
    }

    protected IResponse showChangeProfile() {
        return new ForwardResponse("/WEB-INF/_jsp/user/changeProfile.ajax.jsp");
    }

    protected IResponse showRegistration() {
        JspInclude jsp = new JspInclude("/WEB-INF/_jsp/user/registration.jsp");
        return new MasterResponse(MasterResponse.DEFAULT_MASTER, jsp);
    }

    protected IResponse showRegistrationDone() {
        JspInclude jsp = new JspInclude("/WEB-INF/_jsp/user/registrationDone.jsp");
        return new MasterResponse(MasterResponse.DEFAULT_MASTER, jsp);
    }

    protected IResponse showEmailVerification() {
        JspInclude jsp = new JspInclude("/WEB-INF/_jsp/user/verifyRegistrationEmail.jsp");
        return new MasterResponse(MasterResponse.DEFAULT_MASTER, jsp);
    }

    protected IResponse showLogin() {
        return new ForwardResponse("/WEB-INF/_jsp/user/login.jsp");
    }

    protected IResponse showEditGroup() {
        return new ForwardResponse("/WEB-INF/_jsp/user/editGroup.ajax.jsp");
    }

    protected IResponse showEditUser() {
        return new ForwardResponse("/WEB-INF/_jsp/user/editUser.ajax.jsp");
    }

}
