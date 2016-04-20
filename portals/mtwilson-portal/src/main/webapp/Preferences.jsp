<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title data-i18n="title.preferences">My Preferences</title>
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