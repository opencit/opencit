<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Welcome to Mt.Wilson</title>
	
	<link rel="stylesheet" type="text/css" href="CSS/home.css" />
	
	<script type="text/javascript" src="Scripts/JQuery/jquery-1.7.2.js"></script>
        <script type="text/javascript" src="Scripts/token.js"></script>
	<script type="text/javascript" src="Scripts/commonUtils.js"></script>
	<script type="text/javascript" src="Scripts/Register.js"></script>

</head>
<body>
	<div>
	<div class="header">
       <div class="title"><h1>Mt. Wilson</h1></div>
            <div class="clear hideSkiplink">
                <span style="float: right;padding-right: 50px;font-size: 13px;font-weight: bold;">
                 <a href="login.htm" id="LogInOut">Log-in</a>
                </span>
            </div>
        </div>
        </div>
        <div class="main" id="mainContainer">
	        <div class="container">
                    <h2>Create New Account</h2>
                    <p>Use the form below to create a new account.</p>        
                    <p>Passwords are required to be a minimum of 6 characters in length.</p>
                    <form id="loginForm" action="" method="post" style="margin-left: 60px;font-size: 16px;">
                            <table cellpadding="3" cellspacing="5">
                            <tbody>
                                    <tr>
                                        <td ><label>User Name : </label></td>
                                        <td><input type="text" class="textBoxClass" name="userNameTXT" id="userNameValue"></td>
                                        <td><span class="requiredField">*</span></td>
                                    </tr>
                                    <tr>
                                        <td><label>Password : </label></td>
                                        <td><input type="password" class="textBoxClass" name="passwordTXT" id="passwordValue"></td>
                                        <td><span class="requiredField">*</span></td>
                                     </tr>
                                    <tr>
                                        <td><label>Confirm Password : </label></td>
                                        <td><input type="password" class="textBoxClass" name="confirmPasswordTXT" id="confirmPasswordValue"></td>
                                        <td><span class="requiredField">*</span></td>
                                     </tr>
                                    <tr>
                                            <td ><label></label></td>
                                            <td><input type="button" class="button" value="Create User" onclick="registerUser()"><input class="button" type="reset" value="Clear"></td>
                                    <td></td>
                                </tr>
                            </tbody>
                    </table>
                    </form>
                    <div id="errorMessage"></div>
        	</div>
        </div>
        <div class="footer">
            <p>&copy; 2012-2013 Intel Corporation.</p>
        </div>
        
        
</body>
</html>