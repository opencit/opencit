<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<!--[if lt IE 9]><script src="Scripts/html5.js"></script><![endif]-->
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <meta http-equiv="AuthorizationToken" value="<%=request.getAttribute("AuthorizationToken")%>"/>
    <title data-i18n="title.home">Welcome</title>
	<link rel="stylesheet" type="text/css" href="CSS/home.css" />
	<link rel="stylesheet" type="text/css" href="CSS/JQueryHelperCSS/jquery.ui.menubar.css" />
	<link rel="stylesheet" type="text/css" href="CSS/JQueryHelperCSS/jquery.contextMenu.css" />
	<link rel="stylesheet" type="text/css" href="CSS/JQueryHelperCSS/style.css" />
	<link rel="stylesheet" type="text/css" href="CSS/JQueryHelperCSS/notifications.css" />
        <link rel="stylesheet" type="text/css" href="CSS/bootstrap.css" />
        <link rel="stylesheet" type="text/css" href="CSS/dashboard.css" />
        <link rel="stylesheet" type="text/css" href="CSS/intel.css" />
	
    
    <!--
	<script type="text/javascript" src="Scripts/JQuery/jquery-1.10.2.js"></script>
	<script type="text/javascript" src="Scripts/JQuery/jquery-ui-1.10.4.min.js"></script>
    -->
	<script type="text/javascript" src="Scripts/bootstrap.min.js"></script>
	<script type="text/javascript" src="Scripts/JQuery/jquery-1.7.2.js"></script>
	<script type="text/javascript" src="Scripts/JQuery/jquery.ui.core.js"></script>
	<script type="text/javascript" src="Scripts/JQuery/jquery.ui.dialog.js"></script>
	<script type="text/javascript" src="Scripts/JQuery/jquery.effects.core.js"></script>
	<script type="text/javascript" src="Scripts/JQuery/jquery.ui.position.js"></script>
	<script type="text/javascript" src="Scripts/JQuery/jquery.ui.widget.js"></script>
	<script type="text/javascript" src="Scripts/JQuery/jquery.ui.menu.js"></script>
	<script type="text/javascript" src="Scripts/JQuery/jquery.ui.menubar.js"></script>
	<script type="text/javascript" src="Scripts/JQuery/jquery.json-1.3.min.js"></script>
	<script type="text/javascript" src="Scripts/JQuery/jquery.paginate.js"></script>
	<script type="text/javascript" src="Scripts/JQuery/jquery.contextMenu.js"></script>
	<script type="text/javascript" src="Scripts/token.js"></script>
	<script type="text/javascript" src="Scripts/i18next-1.7.1.min.js"></script>
	<script type="text/javascript" src="Scripts/i18n_util.js"></script>
	<script type="text/javascript" src="Scripts/ajaxfileupload.js"></script>
    <script type="text/javascript" src="Scripts/JQuery/jquery.popupWindow.js"></script>
    <script type="text/javascript" src="Scripts/JQuery/prettify.js"></script>

    <script type="text/javascript" src="Scripts/commonUtils.js"></script>
        <script type="text/javascript" src="Scripts/safe.js"></script>
	<script type="text/javascript" src="Scripts/CommonMessage.js"></script>
	<script type="text/javascript" src="Scripts/home.js"></script>

	<script> var assetTagUrl = "<% out.print(com.intel.mtwilson.My.configuration().getAssetTagServerString()); %>"; </script>

</head>
<body>

        <nav class="navbar navbar-inverse">
                                <div class="container-fluid">
                                        <div class="navbar-header">
                                                <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
                                                        <span class="sr-only">Toggle navigation</span>
                                                        <span class="icon-bar"></span>
                                                        <span class="icon-bar"></span>
                                                        <span class="icon-bar"></span>
                                                </button>
                                                <div class="navbar-brand-image"><img src="images/intel-logo-white-transparent-84x60.png"></img></div>
                                                <a class="navbar-brand" href="#"><span title="Intel and the Intel logo are trademarks of Intel Corporation in the U.S. and/or other countries."><!--Intel&reg; -->Cloud Integrity Technology</span></a>
        <!--
                                                <a class="navbar-brand" href="#">Key Server - <span title="Intel and the Intel logo are trademarks of Intel Corporation in the U.S. and/or other countries.">Intel&reg; Cloud Integrity Technology</span></a>
        -->
                                        </div>
                                        <!-- the navbar contents are loaded dynamically and inserted into this div after login -->
                                        <div id="navbar">
						<ul class="nav navbar-nav navbar-right">
				                	<li><a href="javascript:;" onclick="openPreferences();" data-toggle="tab">Preferences</a></li>
                    					<li><a href="javascript:logoutUser();" data-toggle="tab">Logout</a></li>
						</ul>
                                        </div>
                                </div>
        </nav>
        <!--<div class="header">-->
	<!--
            <div class="title"><h1 data-i18n="app.title">Mt. Wilson</h1></div>
            <div class="loginDisplay">
                <table>
                    <tr style="display:none">
                        <td> <!-- id="loginStatusValue"--><!--
                            <div><span data-18n="app.greeting">Welcome</span> <span id="sessionUser"><%=session.getAttribute("username")%></span></div>
                        </td>
                        <td>
                            <a href="javascript:logoutUser();" id="LogInOut" data-i18n="app.logout">Logout</a>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <a href="javascript:;" onclick="openPreferences();" id="OpenPref" data-i18n="app.preferences">Preferences</a>&nbsp;&nbsp;|&nbsp;&nbsp;
                        </td>
                        <td><a href="javascript:logoutUser();" id="LogInOut" data-i18n="app.logout">Logout</a></td>
                    </tr>
                </table>
            </div>
		-->
            <div class="clear hideSkiplink">
                <div id="NavigationMenu" class="menu" style="float: left;">
                    <ul id="menubarItems">

                        <!-- TRUST DASHBOARD --> 

                        <li>
                            <a data-i18n="link.trust">Trust</a>
                            <ul>
                                <li><a href="javascript:;" onclick="getDashBoardPage()" data-i18n="link.dashboard">Dashboard</a></li> <!-- was "Trust Dashboard" -->
                                <!--<li><a href="javascript:;" onclick="bulktrustUpdate()" data-i18n="link.bulk_refresh">Bulk Refresh</a></li>--> <!-- was "Bulk Trust Refresh" -->
                                <li><a href="javascript:;" onclick="getShowReportPage()" data-i18n="link.reports">Reports</a></li>
                            </ul>
                        </li>
                        <!-- 
<li ><a href="javascript:;" onclick="getDashBoardPage()">Trust Dashboard</a><ul></ul></li> 
                        <li><a href="javascript:;" onclick="bulktrustUpdate()">Bulk Trust Refresh</a><ul></ul></li>
                        <li><a href="javascript:;" onclick="getShowReportPage()">Reports</a><ul></ul></li>
                        -->
                        <li>
                            <a data-i18n="link.host_management">Host Management</a>
                            <ul>
                                <li><a href="javascript:getRegisterHostPage();" data-i18n="link.import">Import...</a></li> <!-- was "Register Host" under Management Console automation -->
                                <li><a href="javascript:getAddHostPage()" data-i18n="link.add_host">Add Host</a></li>
                                <li><a href="javascript:getEditHostPage()" data-i18n="link.edit_host">Edit Host</a></li>
                                <!--<li><a href="javascript:getViewHostPage()" data-i18n="link.view_host">View Host</a></li>-->
                            </ul>
                        </li>
                        <!-- <li>
                                <a>Administration</a>
                                <ul>
                                        <li><a href="javascript:;" onclick="updateDBForMle()">Update DB</a></li>
                                </ul>
                        </li> -->


                        <!-- WHITELIST PORTAL -->
                        <li >
                            <a data-i18n="link.whitelist">Whitelist</a>
                            <ul>
                                <li><a href="javascript:getWhiteListConfigurationPage();" data-i18n="link.import_from_trusted_host">Import from Trusted Host</a></li> <!-- was "Whitelist Configuration" under Management Console automation -->
                                <li><a href="javascript:getEditMle();" data-i18n="link.edit_mle">Edit MLE</a></li>
                                <li><a href="javascript:fnGetEditOS()" data-i18n="link.edit_os">Edit OS</a></li>
                                <li><a href="javascript:fnEditOEM()" data-i18n="link.edit_oem">Edit OEM</a></li>
                            </ul>
                        </li>
                         <li >
                            <a data-i18n="link.asset_tag_management">Asset Tag Management</a>
                            <ul>
                                <li><a href="javascript:getAssetTagPage()" data-i18n="link.create_asset_tag">Tag Creation</a></li> <!-- was "Register Host" under Management Console automation -->
                                <li><a href="javascript:getAssetSelectionPage()" data-i18n="link.select_asset_tag">Tag Selection</a></li>
				<li><a href="javascript:getTagProvisioningPage()" data-i18n="link.tag_provisioning">Provision Tags</a></li>
                                <li><a href="javascript:getAssetCertificatePage()" data-i18n="link.certificate_management">Certificate Management</a></li>
                                <!--li><a href="javascript:getAssetSettingsPage()">Settings</a></li>
                                <li><a href="javascript:getAssetLogPage()">Log</a></li>
                                <li><a href="javascript:getAssetTagPage()">Settings</a></li>
                                <li><a href="javascript:getAssetTagPage()">Log</a></li-->
                            </ul>
                        </li>
                        <!--
                                    <li >
                                            <a>MLE</a>
                                            <ul>
                                                    <li><a href="javascript:getAddMLE();">Add</a></li>
                                                    <li><a href="javascript:getEditMle();">Edit</a></li>
                                                    <li><a href="javascript:getViewMle();">View</a></li>
                                            </ul>
                                    </li>
                                    
                                    <li >
                                            <a tabindex="-1" class="dynamic">OS</a>
                                            <ul>
                                                    <li><a href="javascript:;" onclick="fnGetAddOS()">Add OS</a></li>
                                                    <li><a href="javascript:;" onclick="fnGetEditOS()">Edit OS</a></li>
                                                    <li><a href="javascript:;" onclick="fnViewAllOS()">View OS</a></li>
                                            </ul>
                                    </li>
                                    <li>
                                            <a>OEM</a>
                                            <ul>
                                                    <li><a href="javascript:;" onclick="fnAddOEM()">Add OEM</a></li>
                                                    <li><a href="javascript:;" onclick="fnEditOEM()">Edit OEM</a></li>
                                                    <li><a href="javascript:;" onclick="fnViewAllOEM()">View OEM</a></li>
                                            </ul>
                                    </li>
    
                        -->
                        <!--  MANAGEMENT CONSOLE -->
                        <li >
                            <a data-i18n="link.administration">Administration</a>
                            <ul>
                                <li><a href="javascript:getApproveRequestPage();" data-i18n="link.pending_requests">Pending Requests</a></li> <!-- was "Approve Request" -->
                                <li><a href="javascript:getViewRequest();" data-i18n="link.list_users">List Users</a></li> <!-- was "View Request" -->
                                <!--<li><a href="javascript:getDeletePendingRegistration();" data-i18n="link.delete_user">Delete User</a></li>--> <!-- was "Delete Request" -->
                                <li><a href="javascript:getViewExpiringPage();" data-i18n="link.extend_user">Extend User</a></li> <!-- was "Extend Request" -->
                                <li><a href="javascript:viewCert();" data-i18n="link.view_certificates">View Certificates</a></li>
                                <li><a href="javascript:getTlsPolicyManagementPage()" data-i18n="link.tls_policy_management">TLS Policy Management</a></li>
                                <li><a href="javascript:getAuthenticationPage()" data-i18n="link.alerts">Alerts</a></li>
                            </ul>
                        </li>

                        <!--Begin Added by stdale on 1/8/13 for New Screen for MC fingerprint -->
                        <!--
                        <li >
                                                    <a>Certificates</a>
                                                    <ul>
                                                            <li><a href="javascript:viewCert();">View Certificates</a></li>
                                                            
                                                    </ul>
                                            </li>
                        -->

                        <!--
                                            <li >
                                                    <a>API Client</a>
                                                    <ul>
                                                            <li><a href="javascript:getApproveRequestPage();">Approve Request</a></li>
                                                            <li><a href="javascript:getDeletePendingRegistration();">Delete Request</a></li>
                                                            <li><a href="javascript:getViewExpiringPage();">Extend Request</a></li>
                                                            <li><a href="javascript:getViewRequest();">View Request</a></li>
                                                    </ul>
                                            </li>
                                            
                                            <li >
                                                    <a tabindex="-1" class="dynamic">Automation</a>
                                                    <ul>
                                                            <li><a href="javascript:getWhiteListConfigurationPage();">White List Configuration</a></li>
                                                            <li><a href="javascript:getRegisterHostPage();">Register Host</a></li>
                                                    </ul>
                                            </li>
                        -->
                        <!--    We will move this functionality to the MW 1.2 release.
                                Begin Added by Soni on 18/10/12 for New Screen for CA 
       <li>
                                <a>CA</a>
                                <ul>
                                        <li><a href="javascript:getCAStatus();">Enable/Disable</a></li>
                                        
                                </ul>
                        </li> -->
                        <!--End Added by Soni on 18/10/12 for New Screen for CA-->
                        <!-- 
                        <li >
                        
                                <a>SAML</a>
                                <ul>
                                        <li><a href="javascript:downloadSAML();">Download SAML</a></li>
                                        
                                </ul>
                        </li>
                        -->
                        <!--End Added by Soni on 18/10/12 for New Screen for SAML-->

                    </ul>

                </div>
                <div style="clear: left;"></div><a id="NavigationMenu_SkipLink"></a>
            </div>
        <!--</div>-->
        <div class="main" id="mainContainer">
        </div>
        <div>
            <span id="alert_null_server_response" data-i18n="alert.null_server_response" style="display: none;">Response from server is null.</span>
            <span id="alert_request_error" data-i18n="alert.request_error" style="display: none;">Error while serving request. Please try again later.</span>
            <span id="alert_valid_ip" data-i18n="alert.valid_ip" style="display: none;">Please enter a valid IP address and try again.</span>
            <span id="alert_update_os_info" data-i18n="alert.update_os_info" style="display: none;">Are you sure you want to update OS info?</span>
            <span id="alert_delete_os" data-i18n="alert.delete_os" style="display: none;">Are you sure you want to delete this OS?</span>
            <span id="alert_add_os" data-i18n="alert.add_os" style="display: none;">Are you sure want to add OS?</span>
            <span id="alert_add_oem" data-i18n="alert.add_oem" style="display: none;">Are you sure want to add OEM?</span>
            <span id="alert_update_oem_info" data-i18n="alert.update_oem_info" style="display: none;">Are you sure you want to update OEM info?</span>
            <span id="alert_delete_oem" data-i18n="alert.delete_oem" style="display: none;">Are you sure you want to delete this OEM?</span>
            <span id="alert_delete_host" data-i18n="alert.delete_host" style="display: none;">Are you sure you want to delete this host?</span>
        </div>
        <div class="footer">
            <p>&copy; 2012-2015 Intel Corporation<br/><span style="font-size:0.8em"><%@include file="mtwilson-version.txt" %></span></p>
        </div>
</body>
</html>
