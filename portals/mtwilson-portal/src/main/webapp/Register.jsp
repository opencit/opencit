<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <meta http-equiv="AuthorizationToken" value="<%=request.getAttribute("AuthorizationToken")%>"/>
    <title data-i18n="app.greeting2">Welcome to Mt.Wilson</title>
	
	<link rel="stylesheet" type="text/css" href="CSS/home.css" />
	<script type="text/javascript" src="Scripts/Register.js"></script>
        
        <!--
	<script type="text/javascript" src="Scripts/JQuery/jquery-1.7.2.js"></script>
        <script type="text/javascript" src="Scripts/token.js"></script>
	<script type="text/javascript" src="Scripts/commonUtils.js"></script>
        <script type="text/javascript" src="Scripts/i18next-1.7.1.min.js"></script>
	<script type="text/javascript" src="Scripts/i18n_util.js"></script>
        -->

</head>
<body>
	<div>
	<div class="header">
       <div class="title"><h1 data-i18n="app.title">Cloud Integrity Technology</h1></div>
            <div class="clear hideSkiplink">
                <span style="float: right;padding-right: 50px;font-size: 13px;font-weight: bold;">
                 <a href="login.htm" id="LogInOut" data-i18n="link.login">Login</a>
                </span>
            </div>
        </div>
        </div>
        <div class="main" id="mainContainer">
	        <div class="container">
                    <h2 data-i18n="header.register">Create New Account</h2>
                    <p data-i18n="label.form_to_create_account">Use the form below to create a new account.</p>        
                    <p data-i18n="label.password_requirement">Passwords are required to be a minimum of 6 characters in length.</p>
                    <form id="loginForm" action="" method="post" style="margin-left: 60px;font-size: 16px;">
                            <table cellpadding="3" cellspacing="5">
                            <tbody>
                                    <tr>
                                        <td ><label data-i18n="input.username">User Name:</label></td>
                                        <td><input type="text" class="textBoxClass" name="userNameTXT" id="userNameValue"></td>
                                        <td><span class="requiredField">*</span></td>
                                    </tr>
                                    <tr>
                                        <td ><label data-i18n="input.locale">Locale:</label></td>
                                        <!--<td><select type="text" class="textBoxClass" name="userNameTXT" id="userNameValue"></td>-->
                                        <td><select class="textBoxClass" id="ddlLocales" > <!-- onchange="fnChangehostType(this, true)" > -->
                                            <c:forEach var="locale" varStatus="rowCounter"  items="${locales}">
                                                <option value="${locale.localeName}">${locale.localeName}</option>
                                            </c:forEach>
                                        </select></td>
                                        <!--<td><span class="requiredField">*</span></td>-->
                                    </tr>
                                    <tr>
                                        <td><label data-i18n="input.password">Password:</label></td>
                                        <td><input type="password" class="textBoxClass" name="passwordTXT" id="passwordValue"></td>
                                        <td><span class="requiredField">*</span></td>
                                     </tr>
                                    <tr>
                                        <td><label data-i18n="input.confirm_password">Confirm Password:</label></td>
                                        <td><input type="password" class="textBoxClass" name="confirmPasswordTXT" id="confirmPasswordValue"></td>
                                        <td><span class="requiredField">*</span></td>
                                     </tr>
                                    <tr>
                                            <td ><label></label></td>
                                            <td><input type="button" class="button" value="Create User" onclick="registerUser()" data-i18n="[value]button.create_user"><input class="button" type="reset" value="Clear" data-i18n="[value]button.clear"></td>
                                    <td></td>
                                </tr>
                            </tbody>
                    </table>
                    </form>
                    <div id="errorMessage"></div>
        	</div>
            <span id="alert_successful_user_registration" data-i18n="alert.successful_user_registration" style="display: none;">User is successfully registered. Contact administrator for access approval before accessing the portal.</span>
        </div>
        <div class="footer">
            <p>&copy; 2012-2013 Intel Corporation.</p>
        </div>
        
        
</body>
</html>