<?xml version="1.0" encoding="utf-8"?>
<!--
This stylesheet can be used to edit the Tomcat 7 server.xml configuration file
to disable SSLv3 support. 
Usage example using xsltproc:
cd /usr/share/apache-tomcat-7.0.34/conf
cp server.xml server.xml.bak
xsltproc -o server.xml tomcat-https.xsl server.xml.bak
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!--
Copy the Connector element, adding a new attribute "sslEnabledProtocols" and
removing the attribute "sslProtocol" if it is present.
-->
<xsl:template match="//Connector[@scheme='https']">
  <xsl:copy>
    <xsl:attribute name="sslEnabledProtocols">TLSv1,TLSv1.1,TLSv1.2</xsl:attribute>
    <xsl:apply-templates select="@*[local-name()!='sslProtocol']|node()"/>
  </xsl:copy>
</xsl:template>

<!--
Copy all other elements and attributes without changes
-->
<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
