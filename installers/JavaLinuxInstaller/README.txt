The installer is responsible for creating the following structure:

/opt/intel/cloudsecurity/management-service/
  functions
  version
  ManagementService.war
  management-service.properties
  management-service.env
  management-service.install

/etc/intel/cloudsecurity/
  management-service.properties -> links to /opt/intel/cloudsecurity/management-service/management-service.properties

Also, the installer is responsible for installing bundled Java and Glassfish
if they are not already installed.

If a previous version of the web service is already installed, the
installer must undeploy it from Glassfish, install the new files (but installing
the management-service.properties as management-service.properties.new to avoid
clobbering existing configuration), and deploy the new version to Glassfish.