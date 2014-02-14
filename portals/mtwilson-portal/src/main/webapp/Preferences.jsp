<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title data-i18n="title.preferences">My Preferences</title>
    </head>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <meta http-equiv="AuthorizationToken" value="<%=request.getAttribute("AuthorizationToken")%>"/>
        <title data-i18n="title.preferences">My Preferences</title>

        <link rel="stylesheet" type="text/css" href="CSS/home.css" />

        <script type="text/javascript" src="Scripts/JQuery/jquery-1.7.2.js"></script>
        <script type="text/javascript" src="Scripts/token.js"></script>
        <script type="text/javascript" src="Scripts/commonUtils.js"></script>
        <script type="text/javascript" src="Scripts/Register.js"></script>
        <script type="text/javascript" src="Scripts/i18next-1.7.1.min.js"></script>
	<script type="text/javascript" src="Scripts/i18n_util.js"></script>

    </head>
    <body>
        <div class="container">
            <div class="nagPanel"><span data-i18n="title.preferences">My Preferences</span> &gt;</div>
            <div id="nameOfPage" class="NameHeader"><span id="sessionUser"><%=session.getAttribute("username")%></span> <span data-i18n="header.preferences">Preferences</span></div>
            <div id="mainPreferencesDisplayDiv">
                <div class="tableDivPref" style="margin-left: 61px;" id="mainEditTable">
                    <table cellpadding="3" cellspacing="5">
                        <tbody>
                            <tr>
                                <td><label data-i18n="input.locale">Locale:</label></td>
                                <td>
                                    <select class="textBoxClass" id="ddlLocales">
                                        <c:forEach var="locale" varStatus="rowCounter"  items="${locales}">
                                            <option value="${locale.localeName}" <c:if test='${locale.localeName == selectedLocale}'> selected </c:if>>${locale.localeName}</option>
                                        </c:forEach>
                                    </select>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div>&nbsp;</div>
                <div>&nbsp;</div>
                <div>&nbsp;</div>
                <div>&nbsp;</div>
                <div style="margin-left: 61px;"><input type="button" class="button" value="Save" id="savePrefButton" onclick="fnSavePreferences()" data-i18n="[value]button.save"/></div>
                <div id="messageSpace"></div>
                <div id="errorPreferences" class="errorMessage"></div>
            </div>
        </div>
        <script type="text/javascript" src="Scripts/Preferences.js"></script>
    </body>
</html>