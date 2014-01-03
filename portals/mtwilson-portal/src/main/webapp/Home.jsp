<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<meta http-equiv="AuthorizationToken" value="<%=request.getAttribute("AuthorizationToken")%>"/>
        <title>Welcome</title>

        <link rel="stylesheet" type="text/css" href="CSS/home.css" />
        <link rel="stylesheet" type="text/css" href="CSS/JQueryHelperCSS/jquery.ui.menubar.css" />
        <link rel="stylesheet" type="text/css" href="CSS/JQueryHelperCSS/style.css" />

        <script type="text/javascript" src="Scripts/JQuery/jquery-1.7.2.js"></script>
        <script type="text/javascript" src="Scripts/JQuery/jquery.json-1.3.min.js"></script>
        <script type="text/javascript" src="Scripts/JQuery/jquery.ui.core.js"></script>
        <script type="text/javascript" src="Scripts/JQuery/jquery.ui.dialog.js"></script>
        <script type="text/javascript" src="Scripts/JQuery/jquery.effects.core.js"></script>
        <script type="text/javascript" src="Scripts/JQuery/jquery.ui.widget.js"></script>
        <script type="text/javascript" src="Scripts/JQuery/jquery.ui.position.js"></script>
        <script type="text/javascript" src="Scripts/JQuery/jquery.ui.menu.js"></script>
        <script type="text/javascript" src="Scripts/JQuery/jquery.ui.menubar.js"></script>
        <script type="text/javascript" src="Scripts/JQuery/jquery.paginate.js"></script>
        <script type="text/javascript" src="Scripts/ajaxfileupload.js"></script>
        <script type="text/javascript" src="Scripts/JQuery/jquery.popupWindow.js"></script>

        <script type="text/javascript" src="Scripts/commonUtils.js"></script>
        <script type="text/javascript" src="Scripts/CommonMessage.js"></script>
        <script type="text/javascript" src="Scripts/home.js"></script>
        <script> var assetTagUrl = "<% out.print(com.intel.mtwilson.My.configuration().getAssetTagServerString()); %>"; </script>


    </head>
    <body>
        <div class="header">
            <div class="title"><h1>Mt. Wilson</h1></div>
            <div class="loginDisplay">
                <span id="loginStatusValue">Welcome <%=session.getAttribute("username")%>  </span>
                <a href="javascript:logoutUser();" id="LogInOut">Logout</a>
            </div>

            <div class="clear hideSkiplink">
                <div id="NavigationMenu" class="menu" style="float: left;">
                    <ul id="menubarItems">

                        <!-- TRUST DASHBOARD --> 

                        <li>
                            <a>Trust</a>
                            <ul>
                                <li><a href="javascript:;" onclick="getDashBoardPage()">Dashboard</a></li> <!-- was "Trust Dashboard" -->
                                <li><a href="javascript:;" onclick="bulktrustUpdate()">Bulk Refresh</a></li> <!-- was "Bulk Trust Refresh" -->
                                <li><a href="javascript:;" onclick="getShowReportPage()">Reports</a></li>
                            </ul>
                        </li>
                        <!-- 
<li ><a href="javascript:;" onclick="getDashBoardPage()">Trust Dashboard</a><ul></ul></li> 
                        <li><a href="javascript:;" onclick="bulktrustUpdate()">Bulk Trust Refresh</a><ul></ul></li>
                        <li><a href="javascript:;" onclick="getShowReportPage()">Reports</a><ul></ul></li>
                        -->
                        <li>
                            <a>Host Management</a>
                            <ul>
                                <li><a href="javascript:getRegisterHostPage();">Import...</a></li> <!-- was "Register Host" under Management Console automation -->
                                <li><a href="javascript:getAddHostPage()">Add Host</a></li>
                                <li><a href="javascript:getEditHostPage()">Edit Host</a></li>
                                <li><a href="javascript:getViewHostPage()">View Host</a></li>
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
                            <a>Whitelist</a>
                            <ul>
                                <li><a href="javascript:getWhiteListConfigurationPage();">Import from Trusted Host</a></li> <!-- was "Whitelist Configuration" under Management Console automation -->
                                <li><a href="javascript:getEditMle();">Edit MLE</a></li>
                                <li><a href="javascript:fnGetEditOS()">Edit OS</a></li>
                                <li><a href="javascript:fnEditOEM()">Edit OEM</a></li>
                            </ul>
                        </li>
                         <li >
                            <a>Asset Tag Management</a>
                            <ul>
                                <li><a href="javascript:getAssetTagPage()">Tag Creation</a></li> <!-- was "Register Host" under Management Console automation -->
                                <li><a href="javascript:getAssetTagPage()">Tag Selection</a></li>
                                <li><a href="javascript:getAssetTagPage()">Certificate Management</a></li>
                                <!-- <li><a href="javascript:getAssetTagPage()">Settings</a></li>
                                <li><a href="javascript:getAssetTagPage()">Log</a></li> -->
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
                            <a>Administration</a>
                            <ul>
                                <li><a href="javascript:getApproveRequestPage();">Pending Requests</a></li> <!-- was "Approve Request" -->
                                <li><a href="javascript:getViewRequest();">List Users</a></li> <!-- was "View Request" -->
                                <li><a href="javascript:getDeletePendingRegistration();">Delete User</a></li> <!-- was "Delete Request" -->
                                <li><a href="javascript:getViewExpiringPage();">Extend User</a></li> <!-- was "Extend Request" -->
                                <li><a href="javascript:viewCert();">View Certificates</a></li>
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
                        <!-- stdalex 1/18 this functionality is now duplicated in Certificates tab
                             TODO-stdalex this will be offically removed in 1.2
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
        </div>
        <div class="main" id="mainContainer">
        </div>
        <div class="footer">
            <p>&copy; 2012-2013 Intel Corporation.<br/><span style="font-size:0.8em"><%@include file="mtwilson-version.txt" %></span></p>
        </div>
    </body>
</html>