<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loMLEe.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Api Client Approve/Reject Page</title>
</head>
<body>
	<div id="pendingRequestApprover">
		<div class="singleDiv">
			<div class="labelDiv">Name : </div>
			<div class="valueDiv">
				<input type="text" class="textBox_Border" id="mainApiClient_Name" disabled="disabled">
			</div>
		</div>
		<div class="singleDiv">
			<div class="labelDiv">Finger Print : </div>
			<div class="valueDiv">
				<input type="text" class="textBox_Border" id="mainApiClient_Finger_Print" disabled="disabled">
			</div>
		</div>
		<div class="singleDiv">
			<div class="labelDiv">Issuer : </div>
			<div class="valueDiv">
				<input type="text" class="textBox_Border" id="mainApiClient_Issuer" disabled="disabled">
			</div>
		</div>
                <div class="singleDiv">
			<div class="labelDiv">Requested Roles: </div>
			<div class="valueDiv" id="mainApiClient_Roles">
				
			</div>
		</div>
		<div class="singleDiv">
			<div class="labelDiv">Expires: </div>
			<div class="valueDiv">
				<input type="text" class="textBox_Border" id="mainApiClient_Expires" disabled="disabled">
			</div>
		</div>
		<div class="singleDiv">
			<div class="labelDiv">Comments: </div>
			<div class="valueDiv">
				<textarea class="textAreaBoxClass" cols="18" rows="3" id="mainApiClient_Comments"></textarea>
			</div>
		</div>
		<br>
		<br>
		<div class="singleDiv">
			<div class="labelDiv">&nbsp;</div>
			<div class="valueDiv">
				<input type="button" value="Approve" onclick="fnApproveSelectedRequest()">
				<input type="button" value="Reject" onclick="fnRejectSelectedRequest()">
			</div>
		</div>
	</div>
</body>
</html>