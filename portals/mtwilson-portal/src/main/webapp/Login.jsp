<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <meta http-equiv="AuthorizationToken" value="<%=request.getAttribute("AuthorizationToken")%>"/>
    <title data-i18n="app.greeting2">Welcome to Mt.Wilson</title>
	<link rel="stylesheet" type="text/css" href="CSS/home.css" />
	<script type="text/javascript" src="Scripts/JQuery/jquery-1.7.2.js"></script>
        <script type="text/javascript" src="Scripts/token.js"></script>
	<script type="text/javascript" src="Scripts/commonUtils.js"></script>
        <script type="text/javascript" src="Scripts/login.js"></script>
        <script type="text/javascript" src="Scripts/i18next-1.7.1.min.js"></script>
	<script type="text/javascript" src="Scripts/i18n_util.js"></script>
</head>
<body>
	<div class="page">
	<div class="header">
       <div class="title"><h1 data-i18n="title.login">Cloud Integrity Technology</h1></div>
            <div class="clear hideSkiplink">
				<div style="clear: left;"></div><a id="NavigationMenu_SkipLink"></a>
            </div>
        </div>
        </div>
        <div class="main" id="mainContainer">
	        <div class="container">
				<div class="nagPanel"></div>
				<div id="nameOfPage" class="NameHeader" data-i18n="header.login">Login</div>
                                <div class="registerUser"><span data-i18n="label.enter_credentials">Please enter your username and password.</span> <a href="javascript:;" onclick="getRegisterUserPage()" data-i18n="link.register">Register</a> <span data-i18n="label.no_account">if you don't have an account.</span></div>
				<form id="loginForm" action="checkLogin.htm" method="post" style="margin-left: 60px;font-size: 16px;">
					<table cellpadding="3" cellspacing="5">
                 	<tbody>
                 		<tr>
                 			<td ><label data-i18n="input.username">User Name:</label></td>
	                    	<td><input type="text" class="textBox_Border" name="userNameTXT" id="userNameValue"></td>
	                     	<td><span class="requiredField">*</span></td>
	                    </tr>
	                	<tr>
                 			<td ><label data-i18n="input.password">Password:</label></td>
	                    	<td><input type="password" class="textBox_Border" name="passwordTXT" id="passwordValue"></td>
	                     	<td><span class="requiredField">*</span></td>
	                    </tr>
	                	<tr>
                 			<td ><label></label></td>
	                    	<td><input type="submit" class="button" value="Login" data-i18n="[value]button.login"><input class="button" type="reset" value="Clear" data-i18n="[value]button.clear"></td>
	                    	<td></td>
	                    </tr>
	                </tbody>
                </table>
				</form>
				<div class="errorMessage" data-i18n="${message}">${message}</div>
        	</div>
        </div>
        <div class="footer">
            <p>&copy; 2012-2014 Intel Corporation.</p>
        </div>
        
        
</body>
</html>