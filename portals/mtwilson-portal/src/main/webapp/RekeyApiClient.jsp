<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
	<div id="pendingRequestApprover">
		<div class="singleDiv">
			<div class="labelDiv">IP Address : </div>
			<div class="valueDiv">
				<input type="text" class="textBox_Border" id="mainRegisterHost_IP_ADDRESS" disabled="disabled">
			</div>
		</div>
		<div class="singleDiv">
			<div class="labelDiv">Identity : </div>
			<div class="valueDiv">
				<input type="text" class="textBox_Border" id="mainRegisterHost_Identity" disabled="disabled">
			</div>
		</div>
		<div class="singleDiv">
			<div class="labelDiv">Requested Roles: </div>
			<div class="valueDiv" id="mainRegisterHost_Roles">
				
			</div>
		</div>
		<div class="singleDiv">
			<div class="labelDiv">Login: </div>
			<div class="valueDiv">
				<input type="text" class="textBox_Border" id="mainRegisterHost_login" >
				<span class="requiredField">*</span>
			</div>
		</div>
		<div class="singleDiv">
			<div class="labelDiv">Password: </div>
			<div class="valueDiv">
				<input type="password" id="mainRegisterHost_password" class="textBox_Border">
				<span class="requiredField">*</span>
			</div>
		</div>
		<br>
		<br>
		<div class="singleDiv">
			<div class="labelDiv">&nbsp;</div>
			<div class="valueDiv">
				<input type="button" value="Re-Key" onclick="fnReKeySelectedRequest()">
				<input type="button" value="Cancel" onclick="getViewExpiringPage()">
			</div>
		</div>
	</div>
</body>
</html>