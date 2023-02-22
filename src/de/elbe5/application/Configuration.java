/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.application;

import de.elbe5.base.Mailer;

import jakarta.servlet.ServletContext;
import java.util.*;

public class Configuration {

    public static String ENCODING = "UTF-8";

    private static String appTitle = "";
    private static String salt = "";
    private static String smtpHost = null;
    private static int smtpPort = 25;
    private static Mailer.SmtpConnectionType smtpConnectionType = Mailer.SmtpConnectionType.plain;
    private static String smtpUser = "";
    private static String smtpPassword = "";
    private static String mailSender = null;
    private static String mailReceiver = null;
    private static int timerInterval = 30;
    private static Locale locale = Locale.GERMAN;
    private static final Map<String,Locale> locales = new HashMap<>();

    private static boolean logContent = true;

    static{
        locales.put("de",Locale.GERMAN);
        locales.put("en",Locale.ENGLISH);
    }

    // base data

    public static String getAppTitle() {
        return appTitle;
    }

    public static void setAppTitle(String appTitle) {
        Configuration.appTitle = appTitle;
    }

    public static String getSalt() {
        return salt;
    }

    public static void setSalt(String salt) {
        Configuration.salt = salt;
    }

    public static String getSmtpHost() {
        return smtpHost;
    }

    public static void setSmtpHost(String smtpHost) {
        Configuration.smtpHost = smtpHost;
    }

    public static int getSmtpPort() {
        return smtpPort;
    }

    public static void setSmtpPort(int smtpPort) {
        Configuration.smtpPort = smtpPort;
    }

    public static Mailer.SmtpConnectionType getSmtpConnectionType() {
        return smtpConnectionType;
    }

    public static void setSmtpConnectionType(Mailer.SmtpConnectionType smtpConnectionType) {
        Configuration.smtpConnectionType = smtpConnectionType;
    }

    public static String getSmtpUser() {
        return smtpUser;
    }

    public static void setSmtpUser(String smtpUser) {
        Configuration.smtpUser = smtpUser;
    }

    public static String getSmtpPassword() {
        return smtpPassword;
    }

    public static void setSmtpPassword(String smtpPassword) {
        Configuration.smtpPassword = smtpPassword;
    }

    public static String getMailSender() {
        return mailSender;
    }

    public static void setMailSender(String mailSender) {
        Configuration.mailSender = mailSender;
    }

    public static String getMailReceiver() {
        return mailReceiver;
    }

    public static void setMailReceiver(String mailReceiver) {
        Configuration.mailReceiver = mailReceiver;
    }

    public static Locale getLocale() {
        return locale;
    }

    public static void setLocale(Locale locale) {
        if (locale == null || !locales.containsValue(locale))
            return;
        Configuration.locale = locale;
    }

    public static boolean isLogContent() {
        return logContent;
    }

    public static void setLogContent(boolean logContent) {
        Configuration.logContent = logContent;
    }

    public static int getTimerInterval() {
        return timerInterval;
    }

    public static void setTimerInterval(int timerInterval) {
        Configuration.timerInterval = timerInterval;
    }

    // read from config file

    private static String getSafeInitParameter(ServletContext servletContext, String key){
        String s=servletContext.getInitParameter(key);
        return s==null ? "" : s;
    }

    public static void setConfigs(ServletContext servletContext) {
        setSalt(getSafeInitParameter(servletContext,"salt"));
        setSmtpHost(getSafeInitParameter(servletContext,"mailHost"));
        setSmtpPort(Integer.parseInt(getSafeInitParameter(servletContext,"mailPort")));
        setSmtpConnectionType(Mailer.SmtpConnectionType.valueOf(getSafeInitParameter(servletContext,"mailConnectionType")));
        setSmtpUser(getSafeInitParameter(servletContext,"mailUser"));
        setSmtpPassword(getSafeInitParameter(servletContext,"mailPassword"));
        setMailSender(getSafeInitParameter(servletContext,"mailSender"));
        setMailReceiver(getSafeInitParameter(servletContext,"mailReceiver"));
        setTimerInterval(Integer.parseInt(getSafeInitParameter(servletContext,"timerInterval")));
        String language = getSafeInitParameter(servletContext,"defaultLanguage");
        try {
            setLocale(new Locale(language));
        } catch (Exception ignore) {
        }
        System.out.println("language is "+ getLocale().getDisplayName());
    }

}
