<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
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
	
    <script type="text/javascript" src="Scripts/commonUtils.js"></script>
	<script type="text/javascript" src="Scripts/CommonMessage.js"></script>
	<script type="text/javascript" src="Scripts/home.js"></script>
	
	

</head>
<body>
	<div class="header">
      	<div class="title"><h1>Management Console</h1></div>
        <div class="loginDisplay">
            <span id="loginStatusValue">Welcome <%=session.getAttribute("username") %>  </span>
            <a href="javascript:logoutUser();" id="LogInOut">Logout</a>
        </div>

          <div class="clear hideSkiplink">
             <div id="NavigationMenu" class="menu" style="float: left;">
				<ul id="menubarItems">
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
					<li >
					<!--    We will move this functionality to the MW 1.2 release.
                                                Begin Added by Soni on 18/10/12 for New Screen for CA 
						<a>CA</a>
						<ul>
							<li><a href="javascript:getCAStatus();">Enable/Disable</a></li>
							
						</ul>
					</li> -->
					<!--End Added by Soni on 18/10/12 for New Screen for CA-->
					<li >
					<!--Begin Added by Soni on 18/10/12 for New Screen for SAML -->
						<a>SAML</a>
						<ul>
							<li><a href="javascript:downloadSAML();">Download SAML</a></li>
							
						</ul>
					</li>
					<!--End Added by Soni on 18/10/12 for New Screen for SAML-->
                                        <li >
					<!--Begin Added by stdale on 1/8/13 for New Screen for MC fingerprint -->
						<a>Certificates</a>
						<ul>
							<li><a href="javascript:viewCert();">View Certificates</a></li>
							
						</ul>
					</li>
					<!--End Added by Soni on 18/10/12 for New Screen for SAML-->
				</ul>

			</div>
		<div style="clear: left;"></div><a id="NavigationMenu_SkipLink"></a>
        </div>
	</div>
	   <div class="main" id="mainContainer">
	   </div>
	   <div class="footer">
		   	<h4>@ Intel Corp | 2012</h4>
	   </div>
</body>
</html>