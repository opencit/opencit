<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title data-i18n="title.add_host">Add Host</title>
</head>
<body>
<div class="container">
    <div class="nagPanel" id="mainHeader"><span data-i18n="title.host_management">Host Management</span> &gt; <span data-i18n="title.add_host">Add Host</span></div>
		<div class="dataTableMle" id="mainAddHostContainer">
			<div class="singleDiv">
				<div class="labelDiv" data-i18n="input.hostname">Host Name:</div>
				<div class="valueDiv">
					<input type="text" id="MainContent_tbHostName" class="textBoxClass">
				</div>
			</div>
            
			<div class="singleDiv">
				<div class="labelDiv" data-i18n="input.oem_vendor">OEM Vendor:</div>
				<div class="valueDiv">
					<select class="textBoxClass" id="MainContent_ddlOEM" onchange="fnChangeOEMVender()">
					</select>
				</div>
			</div>
			
			<div class="singleDiv">
				<div class="labelDiv" data-i18n="input.bios">BIOS Info:</div>
				<div class="valueDiv">
					<select id="MainContent_LstBIOS" class="textBoxClass">
					</select>
				</div>
			</div>
			
			<div class="singleDiv">
				<div class="labelDiv" data-i18n="input.os_vmm">OS-VMM Info:</div>
				<div class="valueDiv">
					<select onchange="javascript:SetRequired(this);" id="MainContent_LstVmm" size="4" class="textBoxClass" style="height: auto; width: auto">
					</select>
				</div>
			</div>
                                    
                                    <div class="singleDiv">
				<div class="labelDivgkv" >&nbsp;</div>
				<div class="labelDivgkv" >&nbsp;</div>
			</div>
            <!--
			<div class="singleDiv">
				<div class="labelDiv">Host IP Address : </div>
				<div class="valueDiv">
					<input type="text" class="textBoxClass" id="MainContent_tbHostIP">
				</div>
			</div>
		    -->
			<div class="singleDiv" id="hostPortDisplayDiv">
				<div class="labelDiv" data-i18n="input.host_port">Host Port:</div>
				<div class="valueDiv" >
                                    <input type="text" class="textBoxClass" id="MainContent_tbHostPort" ><!--value="1443"-->
				</div>
			</div>
						            
                                    <div id="citrixStringElement">
				<div class="singleDiv">
					<div class="labelDiv" data-i18n="input.xen_details">XenServer Details:</div>
					<div class="valueDiv">&nbsp;
						<!-- <textarea class="textAreaBoxClass" id="MainContent_tbVCenterDetails" cols="20" rows="2"></textarea> -->
					</div>
				</div>
				<div class="subSingleDiv">
						<div class="subLabelDivgkv labelDiv" data-i18n="input.login_id">Login ID:</div>
						<div class="valueDiv">
							<input type="text" maxlength="200" id="MainContent_tbVcitrixLoginId" class="textBoxClass">
						</div>
				</div>
				<div class="subSingleDiv">
						<div class="subLabelDivgkv labelDiv" data-i18n="input.password">Password:</div>
						<div class="valueDiv">
							<input type="password" maxlength="200" id="MainContent_tbVcitrixPass" class="textBoxClass">
						</div>
				</div>
			</div>
			<div id="vcenterStringElement">
				<div class="singleDiv">
					<div class="labelDiv" data-i18n="input.vmware_details">vCenter Details:</div>
					<div class="valueDiv">&nbsp;
						<!-- <textarea class="textAreaBoxClass" id="MainContent_tbVCenterDetails" cols="20" rows="2"></textarea> -->
					</div>
				</div>
			
				<div class="subSingleDiv">
						<div class="subLabelDivgkv labelDiv" data-i18n="input.vcenter_server">vCenter IP:</div>
						<div class="valueDiv">
							<input type="text" maxlength="200" id="MainContent_tbVCenterAddress" class="textBoxClass">
						</div>
				</div>
				<div class="subSingleDiv">
						<div class="subLabelDivgkv labelDiv" data-i18n="input.login_id">Login ID:</div>
						<div class="valueDiv">
							<input type="text" maxlength="200" id="MainContent_tbVCenterLoginId" class="textBoxClass">
						</div>
				</div>
				<div class="subSingleDiv">
						<div class="subLabelDivgkv labelDiv" data-i18n="input.password">Password:</div>
						<div class="valueDiv">
							<input type="password" maxlength="200" id="MainContent_tbVCenterPass" class="textBoxClass">
						</div>
				</div>
			</div>
            
                                    <div class="singleDiv">
				<div class="labelDiv" data-i18n="input.description">Description:</div>
				<div class="valueDiv">
					<textarea class="textAreaBoxClass" id="MainContent_tbDesc" cols="20" rows="2"></textarea>
				</div>
			</div>
            
			<div class="singleDiv">
                                                <div class="valueDiv">&nbsp; </div>
				<div class="labelDiv" data-i18n="input.email">Email Address:</div>
				<div class="valueDiv">
					<input type="text" maxlength="200" id="MainContent_tbEmailAddress" class="textBoxClass">
				</div>
			</div>
			
			<div class="singleDiv">
                                                <div class="valueDiv">&nbsp; </div>
				<div class="labelDivgkv" >&nbsp;</div>
				<div class="valueDiv">
					<table style="padding-top:80px;padding-bottom: 10px;padding-right:50px;padding-left:50px;">
						<tr>
							<td><input type="button" class="button" value="Add Host" id="addHostButton" onclick="addNewHost()" data-i18n="[value]button.add_host"/></td>
                                                        <td><input type="button" class="button" value="Update Host" id="updateHostButton" onclick="updateHostInfo()" data-i18n="[value]button.update_host"/></td>
							<td><input type="button" class="button" value="Cancel" onclick="getEditHostPage()" data-i18n="[value]button.cancel"/></td>
							<td><input type="button" value="Clear" class="button" onclick="clearAllFiled('mainAddHostContainer')" data-i18n="[value]button.clear"/></td>
						</tr>
					</table>
				</div>
			</div>
			
		</div>
                        <div class="singleDiv" >&nbsp;</div>
		<div id="mleMessage" class="errorDiv"></div>
	</div>
<script type="text/javascript" src="Scripts/addHost.js"></script>
<script type="text/javascript" src="Scripts/i18next-1.7.1.min.js"></script>
<script type="text/javascript" src="Scripts/i18n_util.js"></script>
</body>
</html>