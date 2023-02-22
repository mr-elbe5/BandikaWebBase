/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.request;

import de.elbe5.application.Configuration;
import de.elbe5.base.*;
import de.elbe5.rights.SystemZone;
import de.elbe5.user.UserData;

import jakarta.servlet.http.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class RequestData {

    public static RequestData getRequestData(HttpServletRequest request) {
        return (RequestData) request.getAttribute(RequestKeys.KEY_REQUESTDATA);
    }

    private final KeyValueMap attributes = new KeyValueMap();

    private final StringMap pageAttributes = new StringMap();

    private final Map<String, Cookie> cookies = new HashMap<>();

    private final HttpServletRequest request;

    private int id = 0;

    private final String method;

    private final RequestType type;

    private FormError formError = null;

    public RequestData(String method, RequestType type, HttpServletRequest request) {
        this.request = request;
        this.type = type;
        this.method = method;
    }

    public void init(){
        request.setAttribute(RequestKeys.KEY_REQUESTDATA, this);
        readRequestParams();
        initSession();
    }

    public KeyValueMap getAttributes() {
        return attributes;
    }

    public StringMap getPageAttributes() {
        return pageAttributes;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public boolean isPostback() {
        return method.equals("POST");
    }

    public RequestType getType() {
        return type;
    }

    /*********** message *********/

    public boolean hasMessage() {
        return getAttributes().containsKey(RequestKeys.KEY_MESSAGE);
    }

    public void setMessage(String msg, String type) {
        getAttributes().put(RequestKeys.KEY_MESSAGE, msg);
        getAttributes().put(RequestKeys.KEY_MESSAGETYPE, type);
    }

    /************ user ****************/

    public UserData getLoginUser() {
        return getSessionUser();
    }

    public int getUserId() {
        UserData user = getLoginUser();
        return user == null ? 0 : user.getId();
    }

    public boolean isLoggedIn() {
        UserData user = getLoginUser();
        return user != null;
    }

    public boolean hasAnySystemRight() {
        UserData data = getLoginUser();
        return data != null && (data.hasAnySystemRight());
    }

    public boolean hasAnyElevatedSystemRight() {
        UserData data = getLoginUser();
        return data != null && (data.hasAnyElevatedSystemRight());
    }

    public boolean hasAnyContentRight() {
        UserData data = getLoginUser();
        return data != null && (data.isRoot() || data.hasAnyContentRight());
    }

    public boolean hasSystemRight(SystemZone zone) {
        UserData data = getLoginUser();
        return data != null && (data.isRoot() || data.hasSystemRight(zone));
    }

    /************ form error *************/

    public FormError getFormError(boolean create) {
        if (formError == null && create)
            formError = new FormError();
        return formError;
    }

    public void addFormError(String s) {
        getFormError(true).addFormError(s);
    }

    public void addFormField(String field) {
        getFormError(true).addFormField(field);
    }

    public void addIncompleteField(String field) {
        getFormError(true).addFormField(field);
        getFormError(false).setFormIncomplete();
    }

    public boolean hasFormError() {
        return formError != null && !formError.isEmpty();
    }

    public boolean hasFormErrorField(String name) {
        if (formError == null)
            return false;
        return formError.hasFormErrorField(name);
    }

    public boolean checkFormErrors() {
        if (formError == null)
            return true;
        if (formError.isFormIncomplete())
            formError.addFormError(LocalizedStrings.string("_notComplete"));
        return formError.isEmpty();
    }

    /************** request attributes *****************/

    public void readRequestParams() {
        if (isPostback()) {
            String type = request.getContentType();
            if (type != null && type.toLowerCase().startsWith("multipart/form-data")) {
                getMultiPartParams();
            } else if (type != null && type.equalsIgnoreCase("application/octet-stream")) {
                getSinglePartParams();
                getByteStream();
            }
        }
        getSinglePartParams();
    }

    private void getByteStream(){
        try {
            InputStream in = request.getInputStream();
            BinaryFile file=new BinaryFile();
            file.setBytesFromStream(in);
            file.setFileSize(file.getBytes().length);
            file.setFileName(request.getHeader("fileName"));
            file.setContentType(request.getHeader("contentType"));
            getAttributes().put("file", file);
        }
        catch (IOException ioe){
            Log.error("input stream error", ioe);
        }
    }

    private void getSinglePartParams() {
        Enumeration<?> enm = request.getParameterNames();
        while (enm.hasMoreElements()) {
            String key = (String) enm.nextElement();
            String[] strings = request.getParameterValues(key);
            getAttributes().put(key, strings);
        }
    }

    private void getMultiPartParams() {
        Map<String, List<String>> params = new HashMap<>();
        Map<String, List<BinaryFile>> fileParams = new HashMap<>();
        try {
            Collection<Part> parts = request.getParts();
            for (Part part : parts) {
                String name = part.getName();
                String fileName = getFileName(part);
                if (fileName != null) {
                    if (fileName.isEmpty())
                        continue;
                    BinaryFile file = getMultiPartFile(part, fileName);
                    if (file != null) {
                        List<BinaryFile> values;
                        if (fileParams.containsKey(name))
                            values = fileParams.get(name);
                        else {
                            values = new ArrayList<>();
                            fileParams.put(name, values);
                        }
                        values.add(file);
                    }
                } else {
                    String param = getMultiPartParameter(part);
                    if (param != null) {
                        List<String> values;
                        if (params.containsKey(name))
                            values = params.get(name);
                        else {
                            values = new ArrayList<>();
                            params.put(name, values);
                        }
                        values.add(param);
                    }
                }
            }
        } catch (Exception e) {
            Log.error("error while parsing multipart params", e);
        }
        for (String key : params.keySet()) {
            List<String> list = params.get(key);
            if (list.size() == 1) {
                getAttributes().put(key, list.get(0));
            } else {
                String[] strings = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    strings[i] = list.get(i);
                }
                getAttributes().put(key, strings);
            }
        }
        for (String key : fileParams.keySet()) {
            List<BinaryFile> list = fileParams.get(key);
            if (list.size() == 1) {
                getAttributes().put(key, list.get(0));
            } else {
                BinaryFile[] files = new BinaryFile[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    files[i] = list.get(i);
                }
                getAttributes().put(key, files);
            }
        }
    }

    private String getMultiPartParameter(Part part) {
        try {
            byte[] bytes = new byte[(int) part.getSize()];
            int read = part.getInputStream().read(bytes);
            if (read > 0) {
                return new String(bytes, Configuration.ENCODING);
            }
        } catch (Exception e) {
            Log.error("could not extract parameter from multipart", e);
        }
        return null;
    }

    private BinaryFile getMultiPartFile(Part part, String fileName) {
        try {
            BinaryFile file = new BinaryFile();
            file.setFileName(fileName);
            file.setContentType(part.getContentType());
            file.setFileSize((int) part.getSize());
            InputStream in = part.getInputStream();
            if (in == null) {
                return null;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream(file.getFileSize());
            byte[] buffer = new byte[8096];
            int len;
            while ((len = in.read(buffer, 0, 8096)) != -1) {
                out.write(buffer, 0, len);
            }
            file.setBytes(out.toByteArray());
            return file;
        } catch (Exception e) {
            Log.error("could not extract file from multipart", e);
            return null;
        }
    }

    private String getFileName(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    /************** request attributes ***************/

    public void setRequestObject(String key, Object obj){
        request.setAttribute(key, obj);
    }

    public Object getRequestObject(String key){
        return request.getAttribute(key);
    }

    public <T> T getRequestObject(String key, Class<T> cls) {
        try {
            return cls.cast(request.getAttribute(key));
        }
        catch (NullPointerException | ClassCastException e){
            return null;
        }
    }

    public void removeRequestObject(String key){
        request.removeAttribute(key);
    }

    /************** session attributes ***************/

    public void initSession() {
        HttpSession session = request.getSession(true);
        if (session.isNew()) {
            StringBuffer url = request.getRequestURL();
            String uri = request.getRequestURI();
            String host = url.substring(0, url.indexOf(uri));
            setSessionHost(host);
        }
    }

    public void setSessionObject(String key, Object obj) {
        HttpSession session = request.getSession();
        if (session == null) {
            return;
        }
        session.setAttribute(key, obj);
    }

    public Object getSessionObject(String key) {
        HttpSession session = request.getSession();
        if (session == null) {
            return null;
        }
        return session.getAttribute(key);
    }

    private void removeAllSessionObjects(){
        HttpSession session = request.getSession();
        if (session == null) {
            return;
        }
        Enumeration<String>  keys = session.getAttributeNames();
        while (keys.hasMoreElements()){
            String key=keys.nextElement();
            session.removeAttribute(key);
        }
    }

    public <T> T getSessionObject(String key, Class<T> cls) {
        HttpSession session = request.getSession();
        if (session == null) {
            return null;
        }
        try {
            return cls.cast(request.getSession().getAttribute(key));
        }
        catch (NullPointerException | ClassCastException e){
            return null;
        }
    }

    public void removeSessionObject(String key) {
        HttpSession session = request.getSession();
        if (session == null) {
            return;
        }
        session.removeAttribute(key);
    }

    public ClipboardData getClipboard() {
        ClipboardData data = getSessionObject(RequestKeys.KEY_CLIPBOARD,ClipboardData.class);
        if (data==null){
            data=new ClipboardData();
            setSessionObject(RequestKeys.KEY_CLIPBOARD,data);
        }
        return data;
    }

    public <T extends BaseData> T getCurrentDataInRequestOrSession(String key, Class<T> cls) {
        try {
            Object obj=getRequestObject(key);
            if (obj==null)
                obj=getSessionObject(key);
            if (obj==null){
                return null;
            }
            //Log.log("current request data is: " + obj.getClass().getSimpleName());
            return cls.cast(obj);
        }
        catch (ClassCastException e){
            return null;
        }
    }

    public void setClipboardData(String key, BaseData data){
        getClipboard().putData(key,data);
    }

    public boolean hasClipboardData(String key){
        return getClipboard().hasData(key);
    }

    public <T> T getClipboardData(String key,Class<T> cls) {
        try {
            return cls.cast(getClipboard().getData(key));
        }
        catch(NullPointerException | ClassCastException e){
            return null;
        }
    }

    public void clearClipboardData(String key){
        getClipboard().clearData(key);
    }

    public void clearAllClipboardData(){
        getClipboard().clear();
    }

    public void setSessionUser(UserData data) {
        setSessionObject(RequestKeys.KEY_LOGIN, data);
    }

    public UserData getSessionUser() {
        return (UserData) getSessionObject(RequestKeys.KEY_LOGIN);
    }

    public void setSessionHost(String host) {
        setSessionObject(RequestKeys.KEY_HOST, host);
    }

    public String getSessionHost() {
        return getSessionObject(RequestKeys.KEY_HOST,String.class);
    }

    public void resetSession() {
        removeAllSessionObjects();
        request.getSession(true);
    }

    /*************** cookie methods ***************/

    public void addLoginCookie(String name, String value, int expirationDays){
        Cookie cookie=new Cookie("elbe5cms_"+name,value);
        cookie.setPath("/ctrl/user/login");
        cookie.setMaxAge(expirationDays*24*60*60);
        cookies.put(cookie.getName(),cookie);
    }

    public boolean hasCookies(){
        return !(cookies.isEmpty());
    }

    public void setCookies(HttpServletResponse response){
        for (Cookie cookie : cookies.values()){
            response.addCookie(cookie);
        }
    }

    public Map<String,String> readLoginCookies(){
        Map<String, String> map=new HashMap<>();
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().startsWith("elbe5cms_")) {
                map.put(cookie.getName().substring(9), cookie.getValue());
            }
        }
        return map;
    }

}








