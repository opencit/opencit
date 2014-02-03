<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>My Preferences</title>
    </head>
    <body>
        <div class="container">
            <div class="nagPanel">My Preferences</div>
            <div id="nameOfPage" class="NameHeader">My Preferences</div>
            <div id="mainPreferencesDisplayDiv">
                <div class="tableDiv" style="margin-left: 61px; display: none;" id="mainEditTable">
                    <table cellpadding="3" cellspacing="5">
                        <tbody>
                            <tr>
                                <td ><label>Locale : </label></td>
                                <td>
                                    <select class="textBoxClass" id="ddlLocales" onchange="fnChangeLocale(this.selectedLocale)">
                                        <c:forEach var="locale" varStatus="rowCounter"  items="${locales}">
                                            <option value="${locale.localeName}">${locale.localeName}</option>
                                        </c:forEach>
                                    </select>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div id="messageSpace"></div>
                <div id="errorPreferences" class="errorMessage"></div>
            </div>
        </div>
        <script type="text/javascript" src="Scripts/Preferences.js"></script>
    </body>
</html>