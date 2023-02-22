<%--
  Bandika CMS - A Java based modular Content Management System
  Copyright (C) 2009-2021 Michael Roennau

  This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
--%>
<%response.setContentType("text/html;charset=UTF-8");%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@include file="/WEB-INF/_jsp/_include/_functions.inc.jsp" %>
<%@ page import="de.elbe5.request.RequestData" %>
<%@ page import="de.elbe5.user.UserData" %>
<%@ page import="java.util.Date" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    UserData user = (UserData) rdata.getAttributes().get("userData");
    String url = "/ctrl/user/register/" + user.getId();
%>
<form:message/>
<section class="contentTop">
    <h1>
        <%=$SH("_register")%>
    </h1>
</section>
<div class="contentSection">
    <form:form url="<%=url%>" name="registerform">
        <div class="paragraph">
            <form:formerror/>
            <form:line padded="true"><%=$SH("_registrationHint")%>&nbsp;<%=$SH("_mandatoryHint")%>
            </form:line>
            <form:text name="login" label="_loginName" required="true" value="<%=$H(user.getLogin())%>"/>
            <form:password name="password1" label="_password" required="true"/>
            <form:password name="password2" label="_retypePassword" required="true"/>
            <form:text name="title" label="_title" value="<%=$H(user.getTitle())%>"/>
            <form:text name="firstName" label="_firstName" value="<%=$H(user.getFirstName())%>"/>
            <form:text name="lastName" label="_lastName" required="true" value="<%=$H(user.getLastName())%>"/>
            <h3><%=$SH("_address")%>
            </h3>
            <form:text name="street" label="_street" value="<%=$H(user.getStreet())%>"/>
            <form:text name="zipCode" label="_zipCode" value="<%=$H(user.getZipCode())%>"/>
            <form:text name="city" label="_city" value="<%=$H(user.getCity())%>"/>
            <form:text name="country" label="_country" value="<%=$H(user.getCountry())%>"/>
            <h3><%=$SH("_contact")%>
            </h3>
            <form:text name="email" label="_email" required="true" value="<%=$H(user.getEmail())%>"/>
            <form:text name="phone" label="_phone" value="<%=$H(user.getPhone())%>"/>
            <form:text name="fax" label="_fax" value="<%=$H(user.getFax())%>"/>
            <form:text name="mobile" label="_mobile" value="<%=$H(user.getMobile())%>"/>
            <hr/>
            <form:line padded="true">
                <div class="imgBox left50">
                    <img id="captchaImg" src="/ctrl/user/showCaptcha?timestamp=<%=new Date().getTime()%>" alt="captcha"/>
                    <%=$SHM("_captchaHint")%>
                    <br/><br/>
                    <a class="link" href="#" onclick="return renewCaptcha();"><%=$SH("_captchaRenew")%>
                    </a>
                </div>
            </form:line>
            <form:text name="captcha" label="_captcha" value=""/>
        </div>
        <div>
            <button type="submit" class="btn btn-primary"><%=$SH("_register")%>
            </button>
        </div>
    </form:form>
    <script type="text/javascript">
        function renewCaptcha() {
            $.ajax({
                url: '/ctrl/user/renewCaptcha',
                type: 'POST',
                data: {},
                cache: false,
                dataType: 'html'
            }).success(function () {
                $('#captchaImg').attr("src", "/ctrl/user/showCaptcha?timestamp=" + new Date().getTime());
            });
            return false;
        }
    </script>
</div>

