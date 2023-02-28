package de.elbe5.servlet;

import de.elbe5.base.LocalizedStrings;
import de.elbe5.request.*;
import de.elbe5.response.IResponse;
import de.elbe5.response.ForwardResponse;

import jakarta.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;

public abstract class Controller {

    public abstract String getKey();

    protected IResponse showHome() {
        return new ForwardResponse("/");
    }

    protected void checkRights(boolean hasRights){
        if (!hasRights)
            throw new ResponseException(HttpServletResponse.SC_UNAUTHORIZED);
    }

    protected void setSaveError(RequestData rdata) {
        rdata.setMessage(LocalizedStrings.string("_saveError"), RequestKeys.MESSAGE_TYPE_ERROR);
    }

    protected IResponse openAdminPage(RequestData rdata, String jsp, String title) {
        rdata.getAttributes().put(RequestKeys.KEY_JSP, jsp);
        rdata.getAttributes().put(RequestKeys.KEY_TITLE, title);
        return new ForwardResponse("/WEB-INF/_jsp/administration/adminMaster.jsp");
    }

    protected IResponse showSystemAdministration(RequestData rdata) {
        return openAdminPage(rdata, "/WEB-INF/_jsp/administration/systemAdministration.jsp", LocalizedStrings.string("_systemAdministration"));
    }

    protected IResponse showPersonAdministration(RequestData rdata) {
        return openAdminPage(rdata, "/WEB-INF/_jsp/administration/personAdministration.jsp", LocalizedStrings.string("_personAdministration"));
    }

    protected void assertSessionCall(RequestData rdata){
        if (rdata.getType()!=RequestType.session && rdata.getType()!=RequestType.content){
            throw new ResponseException(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    protected void assertApiCall(RequestData rdata){
        if (rdata.getType()!=RequestType.api){
            throw new ResponseException(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    @SuppressWarnings("unchecked")
    protected JSONObject getIdJson(int id) {
        JSONObject json = new JSONObject();
        json.put("id",id);
        return json;
    }
}
