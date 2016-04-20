<%-- 
    Document   : V2Proxy
    Created on : Mar 23, 2014, 2:01:42 AM
    Author     : jbuhacoff
<%@page session="false"%>
--%><%@page import="com.intel.mountwilson.util.ProxyApiClient,java.net.*,java.io.*,org.apache.commons.io.*" %>
<%
try {
    ProxyApiClient client = (ProxyApiClient)session.getAttribute("api-object");
    client.proxy(request,response);
} catch(Exception e) {
    System.err.println("PROXY ERROR: "+e.getMessage());
    e.printStackTrace();
	response.setStatus(500);
}
%>