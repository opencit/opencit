<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loMLEe.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <meta http-equiv="AuthorizationToken" value="<%=request.getAttribute("AuthorizationToken")%>"/>
        <title data-i18n="title.register_host">Host Registration Page</title>
    </head>
    <body>
        <div class="container" id="registerHostMainContainer">
            <div class="nagPanel"><span data-i18n="title.host_management">Host Management</span> &gt;</div> <!-- was: "Automation" -->
            <div id="nameOfPage" class="NameHeader" data-i18n="header.register_host">Host Registration</div> <!-- was: "Register Host" -->
            <div id="mainLoadingDiv" class="mainContainer">
                <div class="singleDiv">
                    <div class="labelDiv" data-i18n="input.host_import_source">Import hosts from:</div> <!-- was: "Host Provided By" -->
                    <div class="valueDiv">
                        <select class="ddlBox_Border" id="MainContent_ddlHOSTProvider" onchange="fnChangehostType(this, false)" >
                            <option value="Flat File" type="false" data-i18n="select.flat_file">Flat File</option>
                            <option value="VMware Cluster" type="true" data-i18n="select.vmware_cluster">VMware Cluster</option>
                        </select>
                    </div>
                </div>
                <br/>
                
                <div id="openSourcesHostType" style="display: none;">
                    <div class="singleDiv">
                        <div class="labelDiv" data-i18n="input.host_import_file">Host(s) File:</div>
                        <div class="valueDiv">
                                <input type="hidden" name="AuthorizationToken" id="AuthorizationToken" />

					<!-- removed attribute data-i18n="[value]button.choose_file"  from input element below because javascript is not allowed to set the value of a file upload element -->
                                <input id="fileToUpload" class="uploadButton" type="file" name="file" size="50"  style="height: 24px"/>
                        </div>
                    </div>
                    <div id="tls_policy_input_div_flatfile" class="singleDiv">
                        <div class="labelDiv" data-i18n="input.tls_policy">TLS Policy:</div>
                        <div class="valueDiv">
                            <select id="tls_policy_select_flatfile" class="textBoxClass">
                            </select>
                        </div>
                    </div>
                    <div class="singleDiv">
                        <div class="labelDiv"></div>
                        <div class="valueDiv">
                            <form class="uploadForm" action="UploadServlet" method="post" enctype="multipart/form-data">
				<input type="hidden" name="tlsPolicy"/>
                                <input type="button" class="uploadButton" value="Retrieve Hosts" onclick="fnUploadFlatFile()" data-i18n="[value]button.retrieve_hosts"/>
                            </form>
                            <input type="image" onclick="showDialogUpFlatFileHelp()" src="images/helpicon.png" class="helperImageClass">
                        </div>
                    </div>
                </div>

                <div id="vmwareHostType" style="display: none;">
                    <div class="singleDiv">
                        <div class="labelDiv"><span data-i18n="input.vcenter_server">vCenter Server:</span> <input type="image" onclick="showHelpForVCenterServer()" src="images/helpicon.png" class="helperImageClass"></div>
                        <div class="valueDiv">
                            <input type="text" class="textBox_Border" id="mainRegisterHost_vCenterServer">
                            <span class="requiredField">*</span>
                        </div>
                    </div>
                    <div id="tls_policy_input_div_vmware" class="singleDiv">
                        <div class="labelDiv" data-i18n="input.tls_policy">TLS Policy:</div>
                        <div class="valueDiv">
                            <select id="tls_policy_select_vmware" class="textBoxClass">
                            </select>
                        </div>
                    </div>
                        <div id="tls_policy_data_container_vmware" class="tlspolicy-input-container singleDiv" style="display: none; padding-bottom: 15px;">
                            <div class="labelDiv">&nbsp;</div>
                            <div class="valueDiv">
                                    <textarea class="tlspolicy-input-certificate" id="tls_policy_data_certificate" placeholder="Hex or Base64-encoded DER-format X.509 public key certificate" data-i18n="[placeholder]tlspolicy.certificate_input_format"></textarea>
                                    <textarea class="tlspolicy-input-certificate-digest" id="tls_policy_data_certificate_digest" placeholder="Hex or Base64-encoded digest of DER-format X.509 public key certificate" data-i18n="[placeholder]tlspolicy.certificate_digest_input_format"></textarea>
                                    <textarea class="tlspolicy-input-public-key" id="tls_policy_data_public_key" placeholder="Hex or Base64-encoded DER-format X.509 public key" data-i18n="[placeholder]tlspolicy.public_key_input_format"></textarea>  
                                    <textarea class="tlspolicy-input-public-key-digest" id="tls_policy_data_public_key_digest" placeholder="Hex or Base64-encoded digest of DER-format X.509 public key" data-i18n="[placeholder]tlspolicy.public_key_digest_input_format"></textarea>                                    
                            </div>
                        </div>
                    <div class="singleDiv">
                        <div class="labelDiv" data-i18n="input.login_id">Login ID:</div>
                        <div class="valueDiv">
                            <input type="text" class="textBox_Border" id="mainRegisterHost_loginID">
                            <span class="requiredField">*</span>
                        </div>
                    </div>
                    <div class="singleDiv">
                        <div class="labelDiv" data-i18n="input.password">Password:</div>
                        <div class="valueDiv">
                            <input type="password" class="textBox_Border" id="mainRegisterHost_password">
                            <span class="requiredField">*</span>
                            <input type="button" class="" id="retrieveDatacentersButton" value="Retrieve Clusters" onclick="fnRetrieveClusters()" data-i18n="[value]button.retrieve_clusters">
                        </div>
                    </div>
                    
                    <!--
                    <div class="singleDiv">
                        <div class="labelDiv">VMware Datacenter : </div>
                        <div class="valueDiv">
                            <select class="textBox_Border" id="MainContent_ddlDatacenterName" onchange="fnRetrieveClusters()" disabled>
                            </select>
                        </div>
                    </div>
                    -->
                    <div class="singleDiv">
                        <div class="labelDiv" data-i18n="input.vmware_cluster">VMware Cluster:</div>
                        <div class="valueDiv">
                            <select class="ddlBox_Border" id="MainContent_ddlClusterName" disabled>
                            </select>
                            <span class="requiredField">&nbsp;</span>
                            <input type="button" class="" id="retriveHostButton" value="Retrieve Hosts" onclick="fnRetriveHostFromCluster()" disabled data-i18n="[value]button.retrieve_hosts">
                        </div>
                    </div>
                    <br>
                </div>

                <div id="registerHostTable" class="registerHostTable" style="display: none;">
                    <table cellpadding="0" cellspacing="0" width="100%" class="tableHeader">
                        <tr>
                            <th class="registerHostRow1" data-i18n="table.host_name">Host Name</td>
                            <th class="registerHostRow2" data-i18n="table.port_num">Port #</td>
                            <th class="registerHostRow3" data-i18n="table.add_on_conn_string">Add On Connection String</td>
                            <th class="registerHostRow4"><input type="checkbox" checked="checked" onclick="fnSelectAllCheckBox(checked)"><span>&nbsp;</span><span data-i18n="table.register">Register</span></td>
                            <th class="registerHostRow5" colspan="2"><span data-i18n="table.configuration">Configuration</span></td>
                            <th class="registerHostRow6Header" data-i18n="table.status">Status</td>
                        </tr>
                        <tr>
                            <th class="registerHostRow1">&nbsp;</td>
                            <th class="registerHostRow2">&nbsp;</td>
                            <th class="registerHostRow3">&nbsp;</td>
                            <th class="registerHostRow4">&nbsp;</td>
                            <th class="registerHostRow5Sub" data-i18n="table.bios">BIOS</td>
                            <th class="registerHostRow5Sub" data-i18n="table.vmm">VMM</td>
                            <!-- <th class="registerHostRow5Sub">Asset Tag Selection</td> -->
                            <th class="registerHostRow6Header">&nbsp;</td>
                        </tr>
                    </table>
                    <div class="registerHostTableContent">
                        <table width="100%" cellpadding="0" cellspacing="0" id="registerHostTableContent">

                        </table>
                    </div>

                    <br>
                    <div class="singleDiv" id="registerHostButton" >
                        <div class="valueDiv">
                            <input type="button" class="" value="Register Host" onclick="fnRegisterMultipleHost()" data-i18n="[value]button.register_host">
                            <input type="button" value="Cancel" onclick="getRegisterHostPage()" data-i18n="[value]button.cancel">
                        </div>
                    </div>
                </div>
                <div id="successMessage"></div>
            </div>
            <span id="alert_valid_hostname_ip" data-i18n="alert.valid_hostname_ip" style="display: none;">Please enter a valid hostname or ip address and try again.</span>
        </div>
	<script type="text/javascript" src="Scripts/tls_policy.js"></script>
        <script type="text/javascript" src="Scripts/RegisterHost.js"></script>
    </body>
</html>