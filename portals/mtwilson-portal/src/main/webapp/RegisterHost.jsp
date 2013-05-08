<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loMLEe.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Host Registration Page</title>
</head>
<body>
<div class="container" id="registerHostMainContainer">
		<div class="nagPanel">Automation ></div>
		<div id="nameOfPage" class="NameHeader">Register Host</div>
		<div id="mainLoadingDiv" class="mainContainer">
			<div class="singleDiv">
				<div class="labelDiv">Host Provided By : </div>
				<div class="valueDiv">
					<select class="textBox_Border" id="MainContent_ddlHOSTProvider" onchange="fnChangehostType(this,false)" >
						<option type="false" >Flat File</option>
						<option type="true" >VMware Cluster</option>
					</select>
				</div>
			</div>
			<br>
			<div id="openSourcesHostType" style="display: none;">
				<div class="singleDiv">
					<div class="labelDiv">Host(s) File : </div>
					<div class="valueDiv">
						<form class="uploadForm" action="UploadServlet" method="post" enctype="multipart/form-data">
							<input id="fileToUpload" class="uploadButton" type="file" name="file" size="50" />
							<input type="button" class="uploadButton" value="Retrieve Hosts" onclick="fnUploadFlatFile()">
						</form>
						<input type="image" onclick="showDialogUpFlatFileHelp()" src="images/helpicon.png">
						<span id="messageForFileUpload"></span>
					</div>
				</div>
			</div>
			<div id="vmwareHostType" style="display: none;">
				<div class="singleDiv">
					<div class="labelDiv">VMWare Cluster : </div>
					<div class="valueDiv">
						<input type="text" class="textBox_Border" id="mainRegisterHost_ClusterName">
						<span class="requiredField">*</span>
					</div>
				</div>
				<div class="singleDiv">
					<div class="labelDiv"><span>vCenter Server : </span><input type="image" onclick="showHelpForVCenterServer()" src="images/helpicon.png" class="helperImageClass"></div>
					<div class="valueDiv">
						<input type="text" class="textBox_Border" id="mainRegisterHost_vCenterServer">
						<span class="requiredField">*</span>
					</div>
				</div>
				<div class="singleDiv">
					<div class="labelDiv">Login ID: </div>
					<div class="valueDiv">
						<input type="text" class="textBox_Border" id="mainRegisterHost_loginID">
						<span class="requiredField">*</span>
					</div>
				</div>
				<div class="singleDiv">
					<div class="labelDiv">Password: </div>
					<div class="valueDiv">
						<input type="password" class="textBox_Border" id="mainRegisterHost_password">
						<span class="requiredField">*</span>
					</div>
					<div id="retriveHostButton">
						<input type="button" class="" value="Retrieve Hosts" onclick="fnRetriveHostFromCluster()">
					</div>
				</div>
				<br>
			</div>
			<div id="registerHostTable" class="registerHostTable" style="display: none;">
				<table cellpadding="0" cellspacing="0" width="100%" class="tableHeader">
					<tr>
						<td class="registerHostRow1">Host Name</td>
						<td class="registerHostRow2">Port #</td>
						<td class="registerHostRow3">Add On Connection String</td>
                        <td class="registerHostRow4"><input type="checkbox" checked="checked" onclick="fnSelectAllCheckBox(checked)"><span>&nbsp;Register</span></td>
                        <td class="registerHostRow5" colspan="2"><span>Configuration</span></td>
						<td class="registerHostRow6Header">Status</td>
					</tr>
					<tr>
						<td class="registerHostRow1">&nbsp;</td>
						<td class="registerHostRow2">&nbsp;</td>
						<td class="registerHostRow3">&nbsp;</td>
                        <td class="registerHostRow4">&nbsp;</td>
                        <td class="registerHostRow5Sub">BIOS</td>
                        <td class="registerHostRow5Sub">VMM</td>
						<td class="registerHostRow6Header">&nbsp;</td>
					</tr>
				</table>
				<div class="registerHostTableContent">
					<table width="100%" cellpadding="0" cellspacing="0" id="registerHostTableContent">
					
					</table>
				</div>
				
				<br>
				<div class="singleDiv" id="registerHostButton" >
					<div class="valueDiv">
						<input type="button" class="" value="Register Host" onclick="fnRegisterMultipleHost()">
                                                <input type="button" value="Cancel" onclick="getRegisterHostPage()">
					</div>
				</div>
			</div>
			<div id="successMessage"></div>
		</div>
	</div>
	<script type="text/javascript" src="Scripts/RegisterHost.js"></script>
</body>
</html>