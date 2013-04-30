<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loMLEe.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>White List Configuration</title>
</head>
<body>
<div class="container">
		<div class="nagPanel">Automation ></div>
		<div id="nameOfPage" class="NameHeader">White List Configuration</div>
		<c:set var="Data" value="${result}"></c:set>
		<c:choose>
			<c:when test="${Data == true}">
				<div id="mainLoadingDiv" class="mainContainer">
					<div class="singleDiv">
						<div class="labelDiv">Host type : </div>
						<div class="valueDiv">
							<select class="textBox_Border" id="MainContent_ddlHOSTType" onchange="fnChangehostType(this,true)" >
								<c:forEach var="HostData" varStatus="rowCounter"  items="${hostTypeList}">
										<option value="${HostData.hostName}" type="${HostData.isVMM}" pcrs="${HostData.pcrs}">${HostData.hostName}</option>
								</c:forEach>
							</select>
						</div>
					</div>
					<br>
					<div id="mainDivForConfig">
						<div class="singleDiv">
							<div class="labelDiv"><span>Configure White List For : </span><input type="image" onclick="showDialogConfigureWhiteHelp()" src="images/helpicon.png" class="helperImageClass"></div>
							<div class="valueDivConfig">
								<input type="checkbox" id="Oem_Bios_Checkbox" onclick="fnChangeApplicableFor(checked,'oem_bios_applicable_for','Hypervisor_Checkbox')">
								<span>BIOS</span>
							</div>
							<div class="valueDivConfig">
								<input type="checkbox" id="Hypervisor_Checkbox" onclick="fnChangeApplicableFor(checked,'Hypervisor_bios_applicable_for','Oem_Bios_Checkbox')">
								<span>Hypervisor (VMM)</span>
							</div>
						</div>     
						<div class="singleDiv" style="height: 66px;">
							<div class="labelDiv"><span>White List Applicable For : </span><input type="image" onclick="showDialogWhiteListApplicableHelp()" src="images/helpicon.png" class="helperImageClass"></div>
							<div class="valueDivConfig">
								<select class="whiteListConfigDropDown" size="3" id="oem_bios_applicable_for" onchange="fnSelectWhiteListType(this,'Oem_Bios_Checkbox')">
									<c:forEach var="BIOSWhiteListData" varStatus="rowCounter"  items="${BIOSWhiteList}">
										<option>${BIOSWhiteListData}</option>
									</c:forEach>
								</select>
							</div>
							<div class="valueDivConfig">
								<select class="whiteListConfigDropDown" size="3" id="Hypervisor_bios_applicable_for" onchange="fnSelectWhiteListType(this,'Hypervisor_Checkbox')">
									<c:forEach var="vmmWhiteListData" varStatus="rowCounter"  items="${vmmWhiteList}">
										<option>${vmmWhiteListData}</option>
									</c:forEach>
								</select>
							</div>
						</div>     
						<div class="singleDiv">
							<div class="labelDiv"><span>Required PCRs :</span><input type="image" onclick="showDialogRequiredPCRValues()" src="images/helpicon.png" class="helperImageClass"></div>
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
							</div>
							<div class="valueDivConfig" id="vmmPCRsValues">
								<input type="checkbox" id="required_pcrs_17" name="17">
								<span>17</span>
								<input type="checkbox" id="required_pcrs_18" name="18"> 
								<span>18</span>
								<input type="checkbox" id="required_pcrs_19" name="19">
								<span>19</span>
								<input type="checkbox" id="required_pcrs_20" name="20">
								<span>20</span>
							</div>
						</div>
						<div class="singleDiv errorMessage" id="defineErrorMessage"></div>
					</div>
					<br>
					<div id="openSourcesHostType" style="display: none;">
						<div class="singleDiv">
							<div class="labelDiv">White List Host : </div>
							<div class="valueDiv">
								<input type="text" class="textBox_Border" id="whiteListOpenSource_Host">
								<span class="requiredField">*</span>
							</div>
						</div>
						<div class="singleDiv">
							<div class="labelDiv">Port #: </div>
							<div class="valueDiv">
								<input type="text" class="textBox_Border" id="whiteListOpenSource_portNO">
								<span class="requiredField">*</span>
							</div>
						</div>
					</div>
					<div id="vmwareHostType" style="display: none;" >
						<div id="defineVMWareHostType">
							<div class="singleDiv">
								<div class="labelDiv">Good Known Host : </div>
								<div class="valueDiv">
									<input type="text" class="textBox_Border" id="whiteListVMware_Host">
									<span class="requiredField">*</span>
								</div>
							</div>                                            
							<div class="singleDiv">
								<div class="labelDiv">vCenter Server : <img alt="image" style="cursor:pointer"onclick="showDialogVcenterHelp()"src="images/helpicon.png"     class="helperImageClass"></div>
								<div class="valueDiv">
									<input type="text" class="textBox_Border" id="whiteListVMWare_vCenterServer">
									<span class="requiredField">*</span>
								</div>
							</div>
							<div class="singleDiv">
								<div class="labelDiv">Login ID: </div>
								<div class="valueDiv">
									<input type="text" class="textBox_Border" id="whiteListVMWare_LoginID">
									<span class="requiredField">*</span>
								</div>
							</div>
							<div class="singleDiv">
								<div class="labelDiv">Password: </div>
								<div class="valueDiv">
									<input type="password" class="textBox_Border" id="whiteListVMWare_password">
									<span class="requiredField">*</span>
								</div>
							</div>
							<br>
						</div>
					</div>
					<div class="singleDiv" id="uploadButtonID">
						<div class="labelDiv">&nbsp;</div>
						<div class="valueDiv">
							<input type="button" class="" value="Upload White List" onclick="fnUploadWhiteListConfigurationData()">
							<input type="button" value="Clear" onclick="fnClearAllFiled()">
						</div>
					</div>
				</div>
			</c:when>
			<c:otherwise>
				<div class="errorMessage">
				<span>Error While Getting MLE Data.</span>
					<c:out value="${message}"></c:out>
				</div>
			</c:otherwise>
		</c:choose>
		<br>
		<div id="whiteListMessage"></div>
	</div>
	<script type="text/javascript" src="Scripts/WhiteListConfig.js"></script>
</body>
</html>