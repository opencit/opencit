<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loMLEe.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title data-i18n="title.approve_reject">Api Client Approve/Reject Page</title>
</head>
<body>
	<div id="pendingRequestApprover">
		<div class="singleDiv">
			<div class="labelDiv" data-i18n="input.name">Name:</div>
			<div class="valueDiv">
				<input type="text" class="textBox_Border" id="mainApiClient_Name" disabled="disabled">
			</div>
		</div>
		<div class="singleDiv">
			<div class="labelDiv" data-i18n="input.fingerprint">Fingerprint:</div>
			<div class="valueDiv">
				<input type="text" class="textBox_Border" id="mainApiClient_Finger_Print" disabled="disabled">
			</div>
		</div>
		<div class="singleDiv">
			<div class="labelDiv" data-i18n="input.issuer">Issuer:</div>
			<div class="valueDiv">
				<input type="text" class="textBox_Border" id="mainApiClient_Issuer" disabled="disabled">
			</div>
		</div>
                <div class="singleDiv">
			<div class="labelDiv" data-i18n="input.requested_roles">Requested Roles:</div>
			<div class="valueDiv" id="mainApiClient_Roles" style="overflow-y:hidden;scroll">
				
			</div>
                        
            <div class="valueHintDiv">
            <span style="color: #555555; font-size: 0.9em">Roles requested by the user are highlighted.</span>
            </div>
		</div>
            <br><br>
		<div class="singleDiv">
			<div class="labelDiv" data-i18n="input.expires">Expires:</div>
			<div class="valueDiv">
				<input type="text" class="textBox_Border" id="mainApiClient_Expires" disabled="disabled">
			</div>
		</div>
		<div class="singleDiv">
			<div class="labelDiv" data-i18n="input.comments">Comments:</div>
			<div class="valueDiv">
				<textarea class="textAreaBoxClass" cols="18" rows="3" id="mainApiClient_Comments"></textarea>
			</div>
		</div>
		<br>
		<br>
		<div class="singleDiv">
			<div class="labelDiv">&nbsp;</div>
			<div class="valueDiv">
				<input type="button" value="Approve" onclick="fnApproveSelectedRequest()" data-i18n="[value]button.approve">
				<input type="button" value="Reject" onclick="fnRejectSelectedRequest()" data-i18n="[value]button.reject">
                <br/>
			</div>
            <div class="valueHintDiv" id="approveRejectButtonFeedback"></div>
		</div>
	</div>
</body>
</html>