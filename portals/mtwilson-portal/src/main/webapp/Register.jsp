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
        <link rel="stylesheet" type="text/css" href="CSS/bootstrap.css" />
        <link rel="stylesheet" type="text/css" href="CSS/dashboard.css" />
        <link rel="stylesheet" type="text/css" href="CSS/intel.css" />
        <script type="text/javascript" src="Scripts/bootstrap.min.js"></script>
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
        <nav class="navbar navbar-inverse">
                                <div class="container-fluid">
                                        <div class="navbar-header">
                                                <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
                                                        <span class="sr-only">Toggle navigation</span>
                                                        <span class="icon-bar"></span>
                                                        <span class="icon-bar"></span>
                                                        <span class="icon-bar"></span>
                                                </button>
                                                <div class="navbar-brand-image"><img src="images/intel-logo-white-transparent-84x60.png"></img></div>
                                                <a class="navbar-brand" href="#"><span title="Intel and the Intel logo are trademarks of Intel Corporation in the U.S. and/or other countries."><!--Intel&reg; -->Cloud Integrity Technology</span></a>
        <!--
                                                <a class="navbar-brand" href="#">Key Server - <span title="Intel and the Intel logo are trademarks of Intel Corporation in the U.S. and/or other countries.">Intel&reg; Cloud Integrity Technology</span></a>
        -->
                                        </div>
                                        <!-- the navbar contents are loaded dynamically and inserted into this div after login -->
                                        <div id="navbar">
                                        </div>
                                </div>
        </nav>
	<div>
	<!--
	<div class="header">
       <div class="title"><h1 data-i18n="app.title">Mt. Wilson</h1></div>
            <div class="clear hideSkiplink">
                <span style="float: right;padding-right: 50px;font-size: 13px;font-weight: bold;">
                 <a href="login.htm" id="LogInOut" data-i18n="link.login">Login</a>
                </span>
            </div>
        </div>
        </div>-->
        <div class="main" id="mainContainer">
	        <div class="container">
                    <h2 data-i18n="header.register">Create New Account</h2>
                    <p data-i18n="label.form_to_create_account">Use the form below to create a new account.</p>
		    <p>If you already have an account then you can <a href="login.htm">Login</a> here.</p>
                    <p data-i18n="label.password_requirement">Passwords are required to be a minimum of 6 characters in length.</p>
                    <form id="loginForm" action="" method="post" style="margin-left: 60px;font-size: 16px;">
                            <table cellpadding="3" cellspacing="5">
                            <tbody>
                                    <tr>
                                        <td style="padding-right: 5px"><label style="font-weight: normal" data-i18n="input.username">User Name:</label></td>
                                        <td><input type="text" class="textBoxClass" name="userNameTXT" id="userNameValue"></td>
                                        <td><span class="requiredField">*</span></td>
                                    </tr>
                                    <tr>
                                        <td style="padding-right: 5px"><label style="font-weight: normal" data-i18n="input.locale">Locale:</label></td>
                                        <!--<td><select type="text" class="textBoxClass" name="userNameTXT" id="userNameValue"></td>-->
                                        <td><select class="textBoxClass" id="ddlLocales" > <!-- onchange="fnChangehostType(this, true)" > -->
                                            <c:forEach var="locale" varStatus="rowCounter"  items="${locales}">
                                                <option value="${locale.localeName}">${locale.localeName}</option>
                                            </c:forEach>
                                        </select></td>
                                        <!--<td><span class="requiredField">*</span></td>-->
                                    </tr>
                                    <tr>
                                        <td style="padding-right: 5px"><label style="font-weight: normal" data-i18n="input.password">Password:</label></td>
                                        <td><input type="password" class="textBoxClass" name="passwordTXT" id="passwordValue"></td>
                                        <td><span class="requiredField">*</span></td>
                                     </tr>
                                    <tr>
                                        <td style="padding-right: 5px"><label style="font-weight: normal" data-i18n="input.confirm_password">Confirm Password:</label></td>
                                        <td><input type="password" class="textBoxClass" name="confirmPasswordTXT" id="confirmPasswordValue"></td>
                                        <td><span class="requiredField">*</span></td>
                                     </tr>
                                    <tr>
                                            <td ><label></label></td>
                                            <td style="padding-top: 5px"><input type="button" class="button" value="Create User" onclick="registerUser()" data-i18n="[value]button.create_user"><input class="button" type="reset" value="Clear" data-i18n="[value]button.clear"></td>
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
