<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Welcome to Demo Portal</title>
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
        <script type="text/javascript" src="Scripts/JQuery/jquery.popupWindow.js"></script>
	
    <script type="text/javascript" src="Scripts/commonUtils.js"></script>
	<script type="text/javascript" src="Scripts/home.js"></script>
</head>
<body>
	<div class="header">
            <div class="title"><h1>Reference Cloud Portal</h1></div>
            <div class="loginDisplay">
                <span id="loginStatusValue" style="margin-right:40px">Welcome <%=session.getAttribute("username") %></span>
                <a href="javascript:logoutUser();" id="LogInOut">Logout</a>
            </div>
            
            <div class="clear hideSkiplink">       
               <div class="menu" style="float: left;">
					<ul id="NavigationMenu" >
						<li ><a href="javascript:;" onclick="getDashBoardPage()">Home</a><ul></ul></li>
						<li>
							<a>Host Management</a>
							<ul>
								<li><a href="javascript:;" onclick="getAddHostPage()">Add Host</a></li>
								<li><a href="javascript:;" onclick="getEditHostPage()">Edit Host</a></li>
								<li><a href="javascript:;" onclick="getViewHostPage()">View Host</a></li>
							</ul>
						</li>
						<!-- <li>
							<a>Administration</a>
							<ul>
								<li><a href="javascript:;" onclick="updateDBForMle()">Update DB</a></li>
							</ul>
						</li> -->
						<li><a href="javascript:;" onclick="bulktrustUpdate()">Bulk Trust Refresh</a><ul></ul></li>
						<li><a href="javascript:;" onclick="getShowReportPage()">Reports</a><ul></ul></li>
					</ul>
				</div>
            </div>
        </div>
        <div class="main" id="mainContainer">
        	
        </div>
        <div class="footer">
        	<h4>@ Intel Corp | 2013</h4>
        </div>
</body>
</html>