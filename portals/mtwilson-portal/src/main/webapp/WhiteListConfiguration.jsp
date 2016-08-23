<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loMLEe.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title data-i18n="title.whitelist_config">White List Configuration</title>
    </head>
    <body>
        <div class="container">
            <div class="nagPanel"><span data-i18n="title.whitelist">Whitelist</span> &gt;</div> <!-- was: "Automation" -->
            <div id="nameOfPage" class="NameHeader" data-i18n="header.whitelist_config">Import Whitelist from Trusted Host</div> <!-- was: "White List Configuration" -->
            <c:set var="Data" value="${result}"></c:set>
            <c:choose>
                <c:when test="${Data == true}">
                    <div id="mainLoadingDiv" class="mainContainer">
                        <div class="singleDiv">
                            <div class="labelDiv" data-i18n="input.host_type">Host Type:</div>
                            <div class="valueDiv">
                                <select class="textBox_Border" id="MainContent_ddlHOSTType" onchange="fnChangehostType(this, true)" >
                                    <c:forEach var="HostData" varStatus="rowCounter"  items="${hostTypeList}">
                                        <option value="${HostData.hostName}" type="${HostData.isVMM}" pcrs="${HostData.pcrs}">${HostData.hostName}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <br>
                        <div id="mainDivForConfig">
                            <div class="singleDiv">
                                <div class="labelDiv"><span data-i18n="input.config_wl_for">Configure White List For:</span> <input type="image" onclick="showDialogConfigureWhiteHelp()" src="images/helpicon.png" class="helperImageClass"></div>
                                <div class="valueDivConfig">
                                    <input type="checkbox" id="Oem_Bios_Checkbox" onclick="fnChangeApplicableFor(checked, 'oem_bios_applicable_for', 'Hypervisor_Checkbox')">
                                    <span data-i18n="label.bios">BIOS</span>
                                </div>
                                <div class="valueDivConfig">
                                    <input type="checkbox" id="Hypervisor_Checkbox" onclick="fnChangeApplicableFor(checked, 'Hypervisor_bios_applicable_for', 'Oem_Bios_Checkbox')">
                                    <span data-i18n="label.hypervisor" name="Hypervisor_Checkbox_lbl" id="Hypervisor_Checkbox_lbl" >Hypervisor (VMM)</span>
                                </div>
                            </div>
                            <div class="singleDiv" style="height: 66px;">
                                <div class="labelDiv"><span data-i18n="input.wl_applicable_for">White List Applicable For:</span> <input type="image" onclick="showDialogWhiteListApplicableHelp()" src="images/helpicon.png" class="helperImageClass"></div>
                                <div class="valueDivConfig">
                                    <select class="whiteListConfigDropDown" size="3" id="oem_bios_applicable_for" onchange="fnSelectWhiteListType(this, 'Oem_Bios_Checkbox')">
                                        <c:forEach var="BIOSWhiteListData" varStatus="rowCounter"  items="${BIOSWhiteList}">
                                            <option value="${BIOSWhiteListData}" data-i18n="[text]select.${fn:toLowerCase(BIOSWhiteListData)}">${BIOSWhiteListData}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="valueDivConfig">
                                    <select class="whiteListConfigDropDown" size="3" id="Hypervisor_bios_applicable_for" onchange="fnSelectWhiteListType(this, 'Hypervisor_Checkbox')">
                                        <c:forEach var="vmmWhiteListData" varStatus="rowCounter"  items="${vmmWhiteList}">
                                            <option value="${vmmWhiteListData}" data-i18n="[text]select.${fn:toLowerCase(vmmWhiteListData)}">${vmmWhiteListData}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>     
                            <div class="singleDiv">
                                <div class="labelDiv"><span data-i18n="input.required_pcrs">Optional PCRs:</span> <input type="image" onclick="showDialogRequiredPCRValues()" src="images/helpicon.png" class="helperImageClass"></div>
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
                                    <input type="checkbox" id="required_pcrs_12" name="12"> 
                                    <span>12</span>
                                    <input type="checkbox" id="required_pcrs_13" name="13">
                                    <span>13</span>
                                    <input type="checkbox" id="required_pcrs_14" name="14">
                                    <span>14</span>
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
                                <div class="labelDiv" data-i18n="input.wl_host">White List Host:</div>
                                <div class="valueDiv">
                                    <input type="text" class="textBox_Border" id="whiteListOpenSource_Host">
                                    <span class="requiredField">*</span>
                                </div>
                            </div>
                            <div class="singleDiv">
                                <div class="labelDiv" data-i18n="input.port_num">Port #:</div>
                                <div class="valueDiv">
                                    <input type="text" class="textBox_Border" id="whiteListOpenSource_portNO">
                                    <span class="requiredField">*</span>
                                </div>
                            </div>
                            <div  name="opensource_credentials" id ="opensource_credentials"><a href="#" onclick="fnShowLoginCredentials()">Show login credentials</a></div>
                            <div class="singleDiv" id="openSourcesHostType_username">
                                <div class="labelDiv" data-i18n="input.username">User Name:</div>
                                <div class="valueDiv">
                                    <input type="text" class="textBox_Border" id="whiteListOpenSource_userName">
                                </div>
                            </div>
                            <div class="singleDiv" id="openSourcesHostType_password">
                                <div class="labelDiv" data-i18n="input.password">Password:</div>
                                <div class="valueDiv">
                                    <input type="password" class="textBox_Border" id="whiteListOpenSource_password">
                                </div>
                            </div>
                        </div>
                        <div id="citrixHostType" style="display: none;">
                            <div class="singleDiv">
                                <div class="labelDiv" data-i18n="input.wl_host">WhiteList Host:</div>
                                <div class="valueDiv">
                                    <input type="text" class="textBox_Border" id="whiteListCitrix_Host">
                                    <span class="requiredField">*</span>
                                </div>
                            </div>
                            <div class="singleDiv">
                                <div class="labelDiv" data-i18n="input.port_num">Port #:</div>
                                <div class="valueDiv">
                                    <input type="text" class="textBox_Border" id="whiteListCitrix_portNO">
                                    <span class="requiredField">*</span>
                                </div>
                            </div>
                            <div class="singleDiv">
                                <div class="labelDiv" data-i18n="input.username">User Name:</div>
                                <div class="valueDiv">
                                    <input type="text" class="textBox_Border" id="whiteListCitrix_userName">
                                    <span class="requiredField">*</span>
                                </div>
                            </div>
                            <div class="singleDiv">
                                <div class="labelDiv" data-i18n="input.password">Password:</div>
                                <div class="valueDiv">
                                    <input type="password" class="textBox_Border" id="whiteListCitrix_password">
                                    <span class="requiredField">*</span>
                                </div>
                            </div>
                        </div>
                        <div id="vmwareHostType" style="display: none;" >
                            <div id="defineVMWareHostType">
                                <div class="singleDiv">
                                    <div class="labelDiv" data-i18n="input.wl_host">WhiteList Host:</div>
                                    <div class="valueDiv">
                                        <input type="text" class="textBox_Border" id="whiteListVMware_Host">
                                        <span class="requiredField">*</span>
                                    </div>
                                </div>                                            
                                <div class="singleDiv">
                                    <div class="labelDiv"><span data-i18n="input.vcenter_server">vCenter Server:</span> <img alt="image" style="cursor:pointer"onclick="showDialogVcenterHelp()"src="images/helpicon.png"     class="helperImageClass"></div>
                                    <div class="valueDiv">
                                        <input type="text" class="textBox_Border" id="whiteListVMWare_vCenterServer">
                                        <span class="requiredField">*</span>
                                    </div>
                                </div>
                                <div class="singleDiv">
                                    <div class="labelDiv" data-i18n="input.login_id">Login ID:</div>
                                    <div class="valueDiv">
                                        <input type="text" class="textBox_Border" id="whiteListVMWare_LoginID">
                                        <span class="requiredField">*</span>
                                    </div>
                                </div>
                                <div class="singleDiv">
                                    <div class="labelDiv" data-i18n="input.password">Password:</div>
                                    <div class="valueDiv">
                                        <input type="password" class="textBox_Border" id="whiteListVMWare_password">
                                        <span class="requiredField">*</span>
                                    </div>
                                </div>
                                <br>
                            </div>
                        </div>
                        <div id="tls_policy_input_div" class="singleDiv">
                            <div class="labelDiv"><span data-i18n="input.tls_policy">TLS Policy</span></div>
                            <div class="valueDivConfig">
                            <select id="tls_policy_select" class="textBoxClass">
                            </select>
                            </div>
                        </div>
                        <div id="tls_policy_data_container" class="tlspolicy-input-container singleDiv" style="display: none; padding-bottom: 10px;">
                            <div class="labelDiv">&nbsp;</div>
                            <div class="valueDivConfig">
                                    <textarea class="tlspolicy-input-certificate" id="tls_policy_data_certificate" placeholder="Hex or Base64-encoded DER-format X.509 public key certificate" data-i18n="[placeholder]tlspolicy.certificate_input_format"></textarea>
                                    <textarea class="tlspolicy-input-certificate-digest" id="tls_policy_data_certificate_digest" placeholder="Hex or Base64-encoded digest of DER-format X.509 public key certificate" data-i18n="[placeholder]tlspolicy.certificate_digest_input_format"></textarea>
                                    <textarea class="tlspolicy-input-public-key" id="tls_policy_data_public_key" placeholder="Hex or Base64-encoded DER-format X.509 public key" data-i18n="[placeholder]tlspolicy.public_key_input_format"></textarea>  
                                    <textarea class="tlspolicy-input-public-key-digest" id="tls_policy_data_public_key_digest" placeholder="Hex or Base64-encoded digest of DER-format X.509 public key" data-i18n="[placeholder]tlspolicy.public_key_digest_input_format"></textarea>                                    
                            </div>
                        </div>
                        <div class="singleDiv" style="clear: both;">
                            <div class="labelDiv"><span data-i18n="input.register_host">Register Host:</span></div>
                            <div class="valueDivConfig">
                                <input type="checkbox" id="RegisterWhiteListHost" checked="yes">
                            </div>
                        </div> 
                        <div class="singleDiv">
                            <div class="labelDiv"><span data-i18n="input.overwrite_wl">Overwrite Whitelist <br> (If exists):</span></div>
                            <div class="valueDivConfig">
                                <input type="checkbox" id="OverwriteWhitelist">
                            </div>
                        </div> 
                        <div class="singleDiv" id="uploadButtonID">
                            <div class="labelDiv">&nbsp;</div>
                            <div class="valueDiv">
                                <input type="button" class="" value="Import White List" onclick="fnUploadWhiteListConfigurationData()" data-i18n="[value]button.import_whitelist"> <!-- was: "Upload White List" -->
                                <input type="button" value="Clear" onclick="fnClearAllFiled()" data-i18n="[value]button.clear">
                            </div>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="errorMessage">
                        <span data-i18n="error.wl_config_error_msg">Error While Getting MLE Data.</span>
                        <c:out value="${message}"></c:out>
                        </div>
                </c:otherwise>
            </c:choose>
            <br>
            <div id="whiteListMessage"></div>
            <span id="alert_valid_hostname_ip" data-i18n="alert.valid_hostname_ip" style="display: none;">Please enter a valid hostname or ip address and try again.</span>
        </div>
	<script type="text/javascript" src="Scripts/tls_policy.js"></script>
        <script type="text/javascript" src="Scripts/WhiteListConfig.js"></script>
    </body>
</html>