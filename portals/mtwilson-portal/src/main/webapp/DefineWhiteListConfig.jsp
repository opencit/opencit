<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
	<div id="mainDivForConfig">
		<div class="singleDivConfig">
			<div class="labelDivConfig"><span>Configure White List For : </span><input type="image" onclick="showDialogConfigureWhiteHelp()" src="images/helpicon.png" class="helperImageClass"></div>
			<div class="valueDivConfig">
				<input type="checkbox" id="Oem_Bios_Checkbox">
				<span>OEM BIOS</span>
			</div>
			<div class="valueDivConfig">
				<input type="checkbox" id="Hypervisor_Checkbox">
				<span>Hypervisor (VMM)</span>
			</div>
		</div>     
		<div class="singleDivConfig" style="height: 66px;">
			<div class="labelDivConfig"><span>White List Applicable For : </span><input type="image" onclick="showDialogWhiteListApplicableHelp()" src="images/helpicon.png" class="helperImageClass"></div>
			<div class="valueDivConfig">
				<select size="3" id="oem_bios_applicable_for" onchange="fnChangeApplicableForBios(this)">
					<option>Host with same BIOS only</option>
					<option goodKnown="true" class="goodKnown">Specified Good Known Host Only</option>
				</select>
			</div>
			<div class="valueDivConfig">
				<select size="3" id="Hypervisor_bios_applicable_for" onchange="fnChangeApplicableForHyper(this)">
					<option>Host with same OS/VMM Builds</option>
					<option>OEM Specific Hosts with same OS/VMM Builds</option>
					<option goodKnown="true" class="goodKnown">Specified Good Known Host Only</option>
				</select>
			</div>
		</div>     
		<div class="singleDivConfig">
			<div class="labelDivConfig"><span>Required PCRs :</span><input type="image" onclick="showDialogRequiredPCRValues()" src="images/helpicon.png" class="helperImageClass"></div>
			<div class="valueDivConfig" id="biosPCRsValues">
				<input type="checkbox" id="required_pcrs_0" name="0">
				<span>0</span>
				<input type="checkbox" id="required_pcrs_1" name="1">
				<span>1</span>
				<input type="checkbox" id="required_pcrs_2" name="2">
				<span>2</span>
				<input type="checkbox" id="required_pcrs_3" name="3">
				<span>3</span>
				<input type="checkbox" id="required_pcrs_4" name="4">
				<span>4</span>
				<input type="checkbox" id="required_pcrs_5" name="5">
				<span>5</span>
                                <input type="checkbox" id="required_pcrs_17" name="17">
				<span>17</span>
			</div>
			<div class="valueDivConfig" id="vmmPCRsValues">
				<input type="checkbox" id="required_pcrs_18" name="18"> 
				<span>18</span>
				<input type="checkbox" id="required_pcrs_19" name="19">
				<span>19</span>
				<input type="checkbox" id="required_pcrs_20" name="20">
				<span>20</span>
			</div>
		</div>
		<div class="singleDivConfig">
			<div class="labelDivConfig"><span>Configure Host Location To : </span>
										<input type="image" onclick="showHelpForLocation()" src="images/helpicon.png" class="helperImageClass">
										<span style="float: left;font-size: 10px;">(If configured in TPM PCR 22)</span></div>
			<div class="valueDivConfig">
				<input type="text" id="location_host">
			</div>
			<div class="valueDivConfig">
				
			</div>
		</div>
		<div class="singleDivConfig errorMessage" id="defineErrorMessage"></div>
		<div class="singleDivConfig" style="margin-top: 35px;">
			<div class="labelDivConfig">&nbsp;</div>
			<div class="valueDivConfig">
				<input type="button" id="" value="Save Configuration" onclick="fnSaveSelectedConfiguration()">
				<input type="button" id="" value="Clear" onclick="fnClearAllConfigFiled()" style="">
				<input type="button" id="" value="Cancel" onclick="fnCloseDialogBox()" style="float:right">
			</div>
			<!-- <div class="valueDivConfig">
				<input type="button" id="" value="Clear" onclick="fnClearAllConfigFiled()" style="margin-right: 75px;">
				<input type="button" id="" value="Cancel" onclick="fnCloseDialogBox()">
			</div> -->
		</div>     
	</div>
</body>
</html>