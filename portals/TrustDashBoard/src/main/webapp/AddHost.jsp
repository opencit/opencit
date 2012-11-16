<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Add Host</title>
</head>
<body>
<div class="container">
		<div class="nagPanel" id="mainHeader">New host configuration</div>
		<div class="dataTableMle" id="mainAddHostContainer">
			<div class="singleDiv">
				<div class="labelDiv">Host Name :</div>
				<div class="valueDiv">
					<input type="text" id="MainContent_tbHostName" class="textBoxClass">
				</div>
			</div>
					
			<div class="singleDiv">
				<div class="labelDiv">Host IP Address : </div>
				<div class="valueDiv">
					<input type="text" class="textBoxClass" id="MainContent_tbHostIP">
				</div>
			</div>
					
			<div class="singleDiv" id="hostPortDisplayDiv">
				<div class="labelDiv">Host Port :</div>
				<div class="valueDiv" >
					<input type="text" class="textBoxClass" id="MainContent_tbHostPort" value="9999" >
				</div>
			</div>
			
			<div class="singleDiv">
				<div class="labelDiv">Description :</div>
				<div class="valueDiv">
					<textarea class="textAreaBoxClass" id="MainContent_tbDesc" cols="20" rows="2"></textarea>
				</div>
			</div>
			
			<div class="singleDiv">
				<div class="labelDiv">OEM Vendor :</div>
				<div class="valueDiv">
					<select class="textBoxClass" id="MainContent_ddlOEM" onchange="fnChangeOEMVender()">
					</select>
				</div>
			</div>
			
			<div class="singleDiv">
				<div class="labelDiv">BIOS Info :</div>
				<div class="valueDiv">
					<select id="MainContent_LstBIOS" class="textBoxClass">
					</select>
				</div>
			</div>
			
			<div class="singleDiv">
				<div class="labelDiv">OS-VMM Info :</div>
				<div class="valueDiv">
					<select onchange="javascript:SetRequired(this);" id="MainContent_LstVmm" size="4" class="textBoxClass" style="height: auto;">
					</select>
				</div>
			</div>
			<div id="vcenterStringElement">
				<div class="singleDiv">
					<div class="labelDiv">vCenter Details :</div>
					<div class="valueDiv">&nbsp;
						<!-- <textarea class="textAreaBoxClass" id="MainContent_tbVCenterDetails" cols="20" rows="2"></textarea> -->
					</div>
				</div>
			
				<div class="subSingleDiv">
						<div class="subLabelDivgkv labelDiv">vCenter IP :</div>
						<div class="valueDiv">
							<input type="text" maxlength="200" id="MainContent_tbVCenterAddress" class="textBoxClass">
						</div>
				</div>
				<div class="subSingleDiv">
						<div class="subLabelDivgkv labelDiv">Login ID :</div>
						<div class="valueDiv">
							<input type="text" maxlength="200" id="MainContent_tbVCenterLoginId" class="textBoxClass">
						</div>
				</div>
				<div class="subSingleDiv">
						<div class="subLabelDivgkv labelDiv">Password :</div>
						<div class="valueDiv">
							<input type="password" maxlength="200" id="MainContent_tbVCenterPass" class="textBoxClass">
						</div>
				</div>
			</div>
			<div class="singleDiv">
				<div class="labelDiv">E-mail Address :</div>
				<div class="valueDiv">
					<input type="text" maxlength="200" id="MainContent_tbEmailAddress" class="textBoxClass">
				</div>
			</div>
			
			<div class="singleDiv">
				<div class="labelDivgkv" >&nbsp;</div>
				<div class="valueDiv">
					<table style="padding-top:20px;padding-bottom: 10px;">
						<tr>
							<td><input type="button" class="button" value="Add Host" id="addHostButton" onclick="addNewHost()"/></td>
							<td><input type="button" class="button" value="Cancel" onclick="getDashBoardPage()"/></td>
							<td><input type="button" value="Clear" class="button" onclick="clearAllFiled('mainAddHostContainer')"/></td>
						</tr>
					</table>
				</div>
			</div>
			
		</div>
		<div id="mleMessage" class="errorDiv"></div>
	</div>
<script type="text/javascript" src="Scripts/addHost.js"></script>
</body>
</html>