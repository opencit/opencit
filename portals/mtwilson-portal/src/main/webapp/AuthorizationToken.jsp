<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%
    
/* the security filter will add the token to the header */
    /*
response.setContentType("application/octet-stream");
response.setHeader("Content-Disposition","attachment;filename=mtwilson-rootCA");
*/
%>
<html>
<head>
<title data-i18n="title.auth_token">Authorization Token</title>
<meta http-equiv="AuthorizationToken" value="<%=request.getAttribute("AuthorizationToken")%>"/>
     
</head>
<body>
    <p><span data-i18n="input.auth_token">Authorization Token:</span> <%=request.getAttribute("AuthorizationToken")%></p>
</body>
</html>