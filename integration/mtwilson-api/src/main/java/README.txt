How to generate the JAXB classes:

In this directory (src/main/java) type the following command:

xjc -p com.intel.mtwilson.datatypes.xml BulkHostTrustXmlResponse.xsd

This will generate the classes defined in the xsd and the ObjectFactory:

HostTrustXmlListType.java
HostTrustXmlType.java
ObjectFactory.java

The generated classes will be placed in the folder com/intel/mtwilson/datatypes/xml (the -p argument is "package")


References:
http://java.sun.com/webservices/docs/1.4/tutorial/doc/JAXBUsing4.html
