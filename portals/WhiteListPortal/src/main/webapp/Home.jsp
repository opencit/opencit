<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
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
	
	<script type="text/javascript" src="Scripts/CommonMessage.js"></script>
    <script type="text/javascript" src="Scripts/commonUtils.js"></script>
	<script type="text/javascript" src="Scripts/home.js"></script>
	
	

</head>
<body>
	<div>
	<div class="header">
       <div class="title"><h1>White List Manager Portal</h1></div>
       <div class="loginDisplay">
           <span id="loginStatusValue" style="margin-right:40px">Welcome <%=session.getAttribute("username") %></span>
           <a href="javascript:logoutUser();" id="LogInOut">Logout</a>
       </div>

          <div class="clear hideSkiplink">
            <div id="NavigationMenu" class="menu" style="float: left;">
			
			<!-- Make a structure to crate menu bar using jquery.ui.menubar.js file 
			for more info check http://wiki.jqueryui.com/w/page/38666403/Menubar -->
			<ul id="menubarItems">
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
				<li><a href="javascript:;" onclick="getAboutWlm()">About</a><ul></ul></li>
			</ul>

		</div>
		<div style="clear: left;"></div><a id="NavigationMenu_SkipLink"></a>
        </div>
      </div>
       </div>
       
       <div class="main" id="mainContainer">
       </div>
       
       <div class="footer">
       	<h4>@ Intel Corp | 2012</h4>
       </div>
        
        
</body>
</html>