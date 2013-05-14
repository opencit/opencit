<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%
response.setContentType("application/octet-stream ");
response.setHeader("Content-Disposition",
"attachment;filename=mtwilson-rootCA");
%>
<html>
<head>
<meta http-equiv="Content-Type" content="application/octet-stream; charset=ISO-8859-1">

<title>Download Certificate</title>

</head>
<body onload="fnLookforRootCACertificate()">
<div class="container">
		<div class="nagPanel">Administration &gt; View Certificates</div> <!-- was: "Management Console Certificate &gt; View " -->
		<div id="nameOfPage" class="NameHeader">Certificate Download</div>
		<div id="mainLoadingDiv" class="mainContainer">
                <!-- stdalex 1/18 the following downloads are disabled in 1.1 due to the files they require are
                     not created as part of install.  These will be back in 1.2 when we can do core vs. premium
                     TODO-stdalex -->
			<div  id ="fdownloadRCA"class="registerUser">Click on Download icon to download Root Ca Certificate
			<input type="image" onclick="fnforRootCACertificate()" src="images/download.jpg"></div>
                        <div  id ="fdownloadPCA"class="registerUser">Click on Download icon to download Privacy Ca Certificate
			<input type="image" onclick="fnforPrivacyCACertificate()" src="images/download.jpg"></div>
                        <div  id ="fdownloadTLS"class="registerUser">Click on Download icon to download TLS Certificate
			<input type="image" onclick="fnforTLSCertificate()" src="images/download.jpg"></div>
                
                        <div  id ="fdownloadSAML"class="registerUser">Click on Download icon to download SAML Certificate
			<input type="image" onclick="fnforSAMLCertificate()" src="images/download.jpg"></div>
                        
			<div id="successMessage"></div>
			
		</div>
	</div>
<script type="text/javascript" src="Scripts/CertDownload.js"></script>
</body>
</html>