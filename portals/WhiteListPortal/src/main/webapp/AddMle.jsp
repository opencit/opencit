<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Add Mle</title>
</head>
<body>
<div class="container">
		<div class="nagPanel">MLE \> Add MLE</div>
		<div id="nameOfPage" class="NameHeader">New Measured Launch Environment (MLE) Configuration</div>
		<div class="dataTableMle" id="mainDataTableMle">
			<div class="singleDiv">
				<div class="labelDiv">MLE-Type : </div>
				<div class="valueDiv">
					<select class="textBox_Border" id="MainContent_ddlMLEType" onchange="fnChangeleType(this)" >
							<option value="VMM" selected="selected">VMM</option>
							<option value="BIOS">BIOS</option>
						</select>
				</div>
			</div>
					
			<div class="singleDiv">
				<div class="labelDiv" id="mleSubTypeLable">Host OS : </div>
				<div class="valueDiv">
					<select class="textBox_Border" id="MainContent_ddlHostOs" onchange="fnOnChangeVmmName(this)">
					</select>
				</div>
			</div>
					
			<div class="singleDiv">
				<div class="labelDiv" id="mleTypeNameLabel">VMM Name :</div>
				<div class="valueDiv" id="mleTypeNameValue">
					<select class="textBox_Border" id="MainContent_ddlMLEName">
					</select>
				</div>
			</div>
			
			<!-- <div id="mleVmmLableInfo">
				<div class="labelDiv">&nbsp;</div>
				<div class="valueDiv textInfo" >Enable logging=memory</div>
			</div> -->
					
			<div class="singleDiv">
				<div class="labelDiv" id="mleTypeVerLabel">VMM Version :</div>
				<div class="valueDiv">
					<input type="text" class="inputs textBox_Border" id="MainContent_tbVersion" maxlength="200" >
					<span class="requiredField">*</span>
				</div>
			</div>
			
			<div class="singleDiv">
				<div class="labelDiv">Attestation Type :</div>
				<div class="valueDiv">
					<select class="textBox_Border" disabled="disabled" id="MainContent_ddlAttestationType">
					</select>
				</div>
			</div>
			
			<div class="singleDiv">
				<div class="labelDiv">Description :</div>
				<div class="valueDiv">
					<input type="text" class="inputs textBox_Border" id="MainContent_tbDesc" maxlength="200">
				</div>
			</div>
			
			<!-- <div class="singleDiv" id="mainfestGKVSCheck">
				<div class="labelDiv">&nbsp;</div>
				<div class="valueDiv">
					<input type="checkbox" id="" onclick="fnToggelManifestList(checked)"><span>Uplaod GKVs directly from a trusted host</span>
				</div>
			</div> -->
			
			<div class="manifestListClass" id="manifestListDiv" >
				<div class="labelDiv">Manifest List :  
					<input type="image" onclick="showDialogManifestList()" src="images/helpicon.png">
				</div>
				<table>
					<tr>
						<td><span>17</span></td>
						<td><input type="checkbox" onclick="fnToggelRegisterValue(checked,'MainContent_tb17')" id="MainContent_check17"/></td>
						<td><input type="text" class="textBox_Border" disabled="disabled" title="Please enter the good known manifest value in HEX format. Ex:BFC3FFD7940E9281A3EBFDFA4E0412869A3F55D8" id="MainContent_tb17"></td>
						<td>
							<form class="uploadForm textBox_Border" method="post" enctype="multipart/form-data">
							<input id="fileToUpload" class="uploadButton" type="file" name="file" size="50" />
							<input type="button" class="uploadButton" value="Upload" onclick="fnUploadManifestFile()">
							<input style="float: right;" type="image" onclick="showDialogUploadFile();return false;" src="images/helpicon.png">
							</form></td>
					</tr>
					<tr>
						<td><span>18</span></td>
						<td><input type="checkbox" onclick="fnToggelRegisterValue(checked,'MainContent_tb18')" id="MainContent_check18"/></td>
						<td><input type="text" class="textBox_Border" disabled="disabled" title="Please enter the good known manifest value in HEX format. Ex:BFC3FFD7940E9281A3EBFDFA4E0412869A3F55D8" id="MainContent_tb18"></td>
						<td><div id="successMessage"></div></td>
					</tr>
					<tr>
						<td><span>19</span></td>
						<td><input type="checkbox" onclick="fnToggelRegisterValue(checked,'MainContent_tb19')" id="MainContent_check19"/></td>
						<td><input type="text" class="textBox_Border" disabled="disabled" title="Please enter the good known manifest value in HEX format. Ex:BFC3FFD7940E9281A3EBFDFA4E0412869A3F55D8" id="MainContent_tb19"></td>
						<td></td>
					</tr>
					<tr>
						<td><span>20</span></td>
						<td><input type="checkbox" onclick="fnToggelRegisterValue(checked,'MainContent_tb20')" id="MainContent_check20"/></td>
						<td><input type="text" class="textBox_Border" disabled="disabled" title="Please enter the good known manifest value in HEX format. Ex:BFC3FFD7940E9281A3EBFDFA4E0412869A3F55D8" id="MainContent_tb20"></td>
						<td></td>
					</tr>
				</table>
				
			</div>
			<div id="uploadGkvs" class="manifestListClass" style="display: none;">
				<div class="singleDivGkv">
					<div class="labelDivgkv">Required Manifests :</div>
					<div class="valueDiv" id="gkvs_register_checkbox">
						<input type="checkbox" id="MainContent_check_gkvs17" name="17"><span>17</span>
						<input type="checkbox" id="MainContent_check_gkvs18" name="18"><span>18</span>
						<input type="checkbox" id="MainContent_check_gkvs19" name="19"><span>19</span>
						<input type="checkbox" id="MainContent_check_gkvs20" name="20"><span>20</span>
					</div>
				</div>
				
				<!-- <div class="singleDiv">
					<div class="labelDivgkv">Trusted Host for Retrieving GKVs :</div>
					<div class="valueDiv">
						<input type="text" class="inputs" id="trustedHost_gkv" maxlength="200" />
					</div>
				</div>
				
				<div class="singleDiv">
					<div class="labelDivgkv" >Host Connection String :</div>
					<div class="valueDiv">
						
					</div>
					<div style="font-weight: bold;font-size: small;">(port # or vCenter Connection String)</div>
				</div>
				<div class="subSingleDiv">
					<div class="subLabelDivgkv labelDivgkv" >vCenter IP :</div>
					<div class="valueDiv">
						<input type="text" class="inputs" id="trustedHost_gkv" maxlength="200" />
					</div>
				</div>
				<div class="subSingleDiv">
					<div class="subLabelDivgkv labelDivgkv" >Login ID :</div>
					<div class="valueDiv">
						<input type="text" class="inputs" id="trustedHost_gkv" maxlength="200" />
					</div>
				</div>
				<div class="subSingleDiv">
					<div class="subLabelDivgkv labelDivgkv" >Password :</div>
					<div class="valueDiv">
						<input type="password" class="inputs" id="trustedHost_gkv" maxlength="200" />
					</div>
				</div> -->
			</div>
                        <div id="moduleTypeManifestList">
			</div> 
			<div id="moduleTypeAttestationMez">
			</div> 
			<div class="singleDiv">
					<div class="labelDivgkv" >&nbsp;</div>
					<div class="valueDiv">
						<table>
							<tr>
								<td><input type="button" class="button" value="Add Mle" id="addMleButton" onclick="addNewMle()"/></td>
								<td><input type="button" value="Clear" class="button" onclick="clearAllFiled('mainDataTableMle')"/></td>
							</tr>
						</table>
					</div>
				</div>
			
		</div>
		<div id="mleMessage"></div>
	</div>


<script type="text/javascript" src="Scripts/ajaxfileupload.js"></script>
<script type="text/javascript" src="Scripts/AddMle.js"></script>
</body>
</html>