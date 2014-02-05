using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;
using System.Configuration;
using Vim25Api;
using System.Security.Cryptography.X509Certificates;
using System.Net.Security;
using System.IO;
using System.Xml;
using System.Xml.Linq;

namespace Intel.DCSG.IASI.VMWareHelperLib
{
    public class VMwareHelper
    {
        #region Root Initialization variables
        private static ManagedObjectReference SIMO_REF = new ManagedObjectReference();
        private static VimService VIM_SERVICE;
        private static ServiceContent SERVICE_CONTENT;
        private static String STR_SERVICE_INSTANCE = "ServiceInstance";
        private static String VIM_HOST;
        private static String USER_NAME;
        private static String PASSWORD;
        #endregion

        #region Initialization variable for Search
        private static ManagedObjectReference PROP_COLLECTOR;
        private static ManagedObjectReference ROOT_FOLDER;
        private static ManagedObjectReference SEARCH_INDEX;
        #endregion

        #region Initialization for Performance collection
        private static ManagedObjectReference PERF_MGR;
        private static System.Collections.Hashtable counterInfoMap = new System.Collections.Hashtable();
        private static System.Collections.Hashtable systemPerfCounters = new System.Collections.Hashtable();
        private static System.Collections.Hashtable entityPerfCounters = new System.Collections.Hashtable();
        // New hashtable to store the custom attribute values
        private static System.Collections.Hashtable customAttributes = new System.Collections.Hashtable();
        #endregion


        /// <summary>
        /// Initializes the VMware webservice
        /// </summary>
        private static void InitializeVMwareService(String hostName, String userName, String password)
        {
            try
            {
                // Retrieve the configuration of the VMware SDK from the web.config file and initialize
                VIM_HOST = hostName;
                USER_NAME = userName;
                PASSWORD = password;

                initAll();
            }
            catch (Exception ex)
            {
                throw new Exception("Initialize VMware Connection:" + ex.Message);
            }
        } //InitializeVMwareService


        /// <summary>
        /// Disconnects from the VMware web service and does any cleanup required.
        /// </summary>
        private static void CleanupVMwareService()
        {
            try
            {
                VIM_SERVICE.Logout(SERVICE_CONTENT.sessionManager);
            }
            catch (Exception)
            {
                // cannot do much here since we are already closing the connection.. so ignore
            }
        } // CleanupVMwareService


        /// <summary>
        /// Completes the basic initializations required for connecting to the webservice
        /// </summary>
        /// <param name="sdkURL">URL of the VC or ESX SDK Ex: https://ssbangal-mobl3.gar.corp.intel.com:444/sdk</param>
        private static void RootInitialization(string sdkURL)
        {
            try
            {
                /// Initializes the SIMO_Ref variable. This variable is a Managed Obejct Reference (MORef)
                /// of type "ServiceInstance" with a value of "ServiceInstance". Because the ServiceInstance is the object 
                /// used to map all other MORefs to objects, its MORef is special.
                SIMO_REF.type = STR_SERVICE_INSTANCE;
                SIMO_REF.Value = STR_SERVICE_INSTANCE;

                /// initVimPort takes the URL of a VC or ESX sdk service as an argument, connects to the http port, 
                /// and returns the port to use for future transactions. This does not login to the web service. 
                /// There is a very important distinction here: at this point the program is connected to an http 
                /// session, but has not yet connected to a webservice session.
                VIM_SERVICE = new VimService();

                VIM_SERVICE.Url = sdkURL;
                VIM_SERVICE.CookieContainer = new System.Net.CookieContainer();

                /// initServiceContent retrives the service content across the http connection (again, note this is 
                /// not yet a webservice connection). The service content is essentially a directory of what services
                /// are available for connecting to the web service. Retrieving the service content is the only method
                /// that can be called without authenticating. 
                if (SERVICE_CONTENT == null)
                    SERVICE_CONTENT = VIM_SERVICE.RetrieveServiceContent(SIMO_REF);
            }
            catch (UriFormatException ufe)
            {
                throw ufe;
            }
            catch (Exception ex)
            {
                throw ex;
            }
        } // End of Root Initilization


        /// <summary>
        /// Takes care of all the initializations that are required for search.
        /// </summary>
        private static void InitializeForSearch()
        {
            try
            {
                if (PROP_COLLECTOR == null)
                    PROP_COLLECTOR = SERVICE_CONTENT.propertyCollector;
                if (ROOT_FOLDER == null)
                    ROOT_FOLDER = SERVICE_CONTENT.rootFolder;
                if (SEARCH_INDEX == null)
                    SEARCH_INDEX = SERVICE_CONTENT.searchIndex;
            }
            catch (Exception ex)
            {
                throw ex;
            }

        } // Initialize for Search


        /// <summary>
        /// Initializes all the variables required for performance query operations.
        /// </summary>
        private static void InitializeForPerformanceCollection()
        {
            try
            {
                if (PERF_MGR == null)
                    PERF_MGR = SERVICE_CONTENT.perfManager;
            }
            catch (Exception ex)
            {
                throw ex;
            }
        }//end of InitializeForPerformanceCollection


        /// <summary>
        /// This method authenticates to the web service and connects to the web service.
        /// </summary>
        /// <param name="url">URL for the SDK</param>
        /// <param name="uname">User Name</param>
        /// <param name="pword">Password</param>
        private static void Connect(String url, String uname, String pword)
        {
            try
            {
                VIM_SERVICE.Login(SERVICE_CONTENT.sessionManager, uname, pword, null);
                VIM_SERVICE.Timeout = 360000;
            }
            catch (Exception ex)
            {
                throw ex;
            }
        }// Connect


        /// <summary>
        /// Parent function for all the initializations
        /// </summary>
        private static void initAll()
        {
            try
            {
                //These following methods have to be called in this order.
                RootInitialization(VIM_HOST);

                // Now connect to the web service
                Connect(VIM_HOST, USER_NAME, PASSWORD);

                // Now do the required initializations for searching
                InitializeForSearch();

                // Do the initialization for performance statistics collection
                InitializeForPerformanceCollection();
            }
            catch (Exception ex)
            {
                throw ex;
            }
        }// InitAll

        /// <summary>
        /// Gets the system time by calling into the VMware SDK.
        /// </summary>
        /// <returns>Time</returns>
        private static string GetSystemTime()
        {
            return VIM_SERVICE.CurrentTime(SIMO_REF).ToString();
        }//GetSystemTime

        /// <summary>
        /// Retrieves the details of the Host
        /// </summary>
        /// <param name="hostObj">Reference to the Host Object.</param>
        private static String GetHostInfo(ManagedObjectReference hostObj)
        {
            String hostName = "";

            try
            {
                // Create Property Spec
                PropertySpec propertySpec = new PropertySpec();
                propertySpec.all = false;
                // We need to retrieve both the name of the host and as well as all the
                // VMs associated with the host.
                propertySpec.pathSet = new String[] { "name" };
                // Specify the entity that will have both of the above parameters.
                propertySpec.type = "HostSystem";
                PropertySpec[] propertySpecs = new PropertySpec[] { propertySpec };

                // Now create Object Spec
                ObjectSpec objectSpec = new ObjectSpec();
                objectSpec.obj = hostObj;
                ObjectSpec[] objectSpecs = new ObjectSpec[] { objectSpec };

                // Create PropertyFilterSpec using the PropertySpec and ObjectPec
                // created above.
                PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
                propertyFilterSpec.propSet = propertySpecs;
                propertyFilterSpec.objectSet = objectSpecs;

                PropertyFilterSpec[] propertyFilterSpecs = new PropertyFilterSpec[] { propertyFilterSpec };

                ObjectContent[] oContent = VIM_SERVICE.RetrieveProperties(PROP_COLLECTOR, propertyFilterSpecs);
                if (oContent != null)
                {
                    for (int i = 0; i < oContent.Length; i++)
                    {
                        DynamicProperty[] dps = oContent[i].propSet;
                        if (dps != null)
                        {
                            for (int j = 0; j < dps.Length; j++)
                            {
                                if (dps[j].name.Equals("name"))
                                {
                                    hostName = dps[j].val.ToString();
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                throw new Exception("GetHostInfo :" + ex.Message);
            }
            return hostName;
        }// GetHostInfo


        /// <summary>
        /// Based on the Entity type this function searches through all the objects and returns
        /// the matching objects.
        /// For getting just clusters call it with "ClusterComputeResource"
        /// For getting just Datacenters call it with "Datacenter"
        /// For getting just hosts call it with "ComputeResource"
        /// </summary>
        /// <param name="entityType">Type of the Entity that should be returned back</param>
        /// <returns>Array of Management Oject References to all the objects of the type specified.</returns>
        private static ManagedObjectReference[] GetEntitiesByType(string entityType)
        {
            ManagedObjectReference[] resultEntities = null;
            ManagedObjectReference[] finalEntityList = null;
            int entityIndex = 0;

            try
            {
                // Since each TraversalSpec does one level of traversal, we need to define
                // multiple ones if we need to traverse recursively.

                // Traversal through Cluster branch
                TraversalSpec crToH = new TraversalSpec();
                crToH.name = "crToH";
                crToH.type = "ComputeResource";
                crToH.path = "host";
                crToH.skip = false;

                // Traversal through the DataCenter branch
                TraversalSpec dcToHf = new TraversalSpec();
                dcToHf.name = "dcToHf";
                dcToHf.type = "Datacenter";
                dcToHf.path = "hostFolder";
                dcToHf.skip = false;
                dcToHf.selectSet = new SelectionSpec[] { new SelectionSpec() };
                dcToHf.selectSet[0].name = "visitFolders";

                // Recurse through the folders
                TraversalSpec tSpec = new TraversalSpec();
                tSpec.name = "visitFolders";
                tSpec.type = "Folder";
                tSpec.path = "childEntity";
                tSpec.skip = false;
                tSpec.selectSet = new SelectionSpec[] { new SelectionSpec(), new SelectionSpec(), new SelectionSpec() };
                tSpec.selectSet[0].name = "visitFolders";
                tSpec.selectSet[1].name = "dcToHf";
                tSpec.selectSet[2].name = "crToH";

                // Create Property Spec
                PropertySpec propertySpec = new PropertySpec();
                propertySpec.all = false;
                propertySpec.pathSet = new String[] { "name" }; // add all the properties that needs to be retrieved
                propertySpec.type = "ManagedEntity"; // Having ManagedEntity refers to all managed objects
                PropertySpec[] propertySpecs = new PropertySpec[] { propertySpec };

                // Now create Object Spec
                ObjectSpec objectSpec = new ObjectSpec();
                objectSpec.obj = ROOT_FOLDER;
                objectSpec.skip = true;
                objectSpec.selectSet = new SelectionSpec[] { tSpec, dcToHf, crToH };
                ObjectSpec[] objectSpecs = new ObjectSpec[] { objectSpec };

                // Create PropertyFilterSpec using the PropertySpec and ObjectSpec created above.
                PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
                propertyFilterSpec.propSet = propertySpecs;
                propertyFilterSpec.objectSet = objectSpecs;
                PropertyFilterSpec[] propertyFilterSpecs = new PropertyFilterSpec[] { propertyFilterSpec };

                ObjectContent[] oContent = VIM_SERVICE.RetrieveProperties(PROP_COLLECTOR, propertyFilterSpecs);
                if (oContent != null)
                {
                    // allocate memory for all the MOR references found.
                    resultEntities = new ManagedObjectReference[oContent.Length];
                    for (int i = 0; i < oContent.Length; i++)
                    {
                        ManagedObjectReference mor = oContent[i].obj;
                        DynamicProperty[] dProps = oContent[i].propSet;
                        if (mor.type.Equals(entityType))
                        {
                            resultEntities.SetValue(mor, entityIndex++);
                        }
                    }
                }

                // Since we allocated the MOR array for all the objects before filtering, we need
                // re-size the array for the actual entity objects.
                int entitySize = 0;
                while (resultEntities[entitySize] != null)
                    entitySize++;

                finalEntityList = new ManagedObjectReference[entitySize];
                for (int k = 0; k < entitySize; k++)
                {
                    finalEntityList[k] = resultEntities[k];
                }
            }
            catch (Exception ex)
            {
                throw new Exception("GetEntitiesByType : " + ex.Message);
            }

            // return back the final entity array
            return finalEntityList;

        } // end of GetEntitiesByType()


        /// <summary>
        /// Retrieves the specified property for the specified entity.
        /// </summary>
        /// <param name="moRef">Entity for which the property should be retrieved</param>
        /// <param name="propertyName">Name of the property that should be retrieved</param>
        /// <returns>Value of the property</returns>
        private static Object GetMORProperty(ManagedObjectReference moRef, String propertyName)
        {
            try
            {
                return GetMORProperties(moRef, new String[] { propertyName })[0];
            }
            catch (Exception ex)
            {
                throw new Exception("GetMORProperty:" + ex.Message);
            }
        }


        /// <summary>
        /// Retrieves the list of properties for the speicified entity.
        /// </summary>
        /// <param name="moRef">Entity for which the properties should be retrieved</param>
        /// <param name="properties">Array of properties that should be retrieved</param>
        /// <returns>Array of values corresponding to the properties.</returns>
        private static Object[] GetMORProperties(ManagedObjectReference moRef, String[] properties)
        {
            // Return object array
            Object[] ret;

            try
            {
                // PropertySpec specifies what properties to
                // retrieve and from type of Managed Object
                PropertySpec pSpec = new PropertySpec();
                pSpec.type = moRef.type;
                pSpec.pathSet = properties;

                // ObjectSpec specifies the starting object and
                // any TraversalSpecs used to specify other objects 
                // for consideration
                ObjectSpec oSpec = new ObjectSpec();
                oSpec.obj = moRef;

                // PropertyFilterSpec is used to hold the ObjectSpec and 
                // PropertySpec for the call
                PropertyFilterSpec pfSpec = new PropertyFilterSpec();
                pfSpec.propSet = new PropertySpec[] { pSpec };
                pfSpec.objectSet = new ObjectSpec[] { oSpec };

                // retrieveProperties() returns the properties
                // selected from the PropertyFilterSpec


                ObjectContent[] ocs = new ObjectContent[20];
                ocs = VIM_SERVICE.RetrieveProperties(PROP_COLLECTOR, new PropertyFilterSpec[] { pfSpec });

                // Return value, one object for each property specified
                ret = new Object[properties.Length];

                if (ocs != null)
                {
                    for (int i = 0; i < ocs.Length; ++i)
                    {
                        ObjectContent oc = ocs[i];
                        DynamicProperty[] dps = oc.propSet;
                        if (dps != null)
                        {
                            for (int j = 0; j < dps.Length; ++j)
                            {
                                DynamicProperty dp = dps[j];
                                // find property path index
                                for (int p = 0; p < ret.Length; ++p)
                                {
                                    if (properties[p].Equals(dp.name))
                                    {
                                        ret[p] = dp.val;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                throw new Exception("GetMORProperties:" + ex.Message);
            }
            return ret;
        }


        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="cert"></param>
        /// <param name="chain"></param>
        /// <param name="errors"></param>
        /// <returns></returns>
        private static bool TrustAllCertificateCallback(object sender, X509Certificate cert, X509Chain chain, SslPolicyErrors errors)
        {
            return true;
        }

        /// <summary>
        /// Converts the array of signed bytes to a hex string.
        /// </summary>
        /// <param name="digestValue"></param>
        /// <returns></returns>
        private static String ConvertToHexString(sbyte[] digestValue)
        {
            StringBuilder digestHexValue = new StringBuilder(digestValue.Length * 2);
            if (digestValue.Length != 0)
            {             
                foreach (sbyte b in digestValue)
                {
                    digestHexValue.AppendFormat("{0:x2}", b);
                }
            }
            return digestHexValue.ToString().ToUpper();
        }

        /// <summary>
        /// Retrieves the PCR values from the vCenter for the host specified
        /// </summary>
        /// <param name="hostName">Name of the host as configured in the vCenter server for
        /// which we need to get the PCR values</param>
        /// <param name="pcrList">List of PCR's for which we need to get the value from the
        /// host via vCenter</param>
        /// <returns>Success/Failure</returns>
        public static String GetQuoteInformationForHost(String hostName, String pcrList, String vCenterConnectionString)
        {
            String pcrKeyValuePair = "";
            // Append a comma at the end of the PCRlist for comparison help 
            pcrList = "," + pcrList + ",";

            //This is to accept all SSL certifcates by default.
            System.Net.ServicePointManager.ServerCertificateValidationCallback = TrustAllCertificateCallback;

            try
            {
                // Initialize all the required variables and connect to the
                String[] vcenterConn = vCenterConnectionString.Split(';');
                if (vcenterConn.Length != 3)
                {
                    throw new Exception("The vCenter connection information is not valid for host:" + hostName);
                }
                // Connect to the vCenter server with the passed in parameters
                VMwareHelper.InitializeVMwareService(vcenterConn[0], vcenterConn[1], vcenterConn[2]);

                ManagedObjectReference[] hostObjects;
                hostObjects = VMwareHelper.GetEntitiesByType("HostSystem");

                foreach (ManagedObjectReference hostObj in hostObjects)
                {
                    String hostNameFromVC = VMwareHelper.GetHostInfo(hostObj);

                    // Skip the remaining processing if the hostName does not match the value we are
                    // looking for.
                    if (hostNameFromVC != hostName)
                        continue;

                    Object objFlag = VMwareHelper.GetMORProperty(hostObj, "capability.tpmSupported");
                    if ((Boolean)objFlag == false)
                    {
                        throw new Exception("Host does not support TPM");
                    }
                    else
                    {
                        // Refresh the runtime information
                        HostRuntimeInfo runtimeInfo = (HostRuntimeInfo)VMwareHelper.GetMORProperty(hostObj, "runtime");

                        // Now process the digest information
                        HostTpmDigestInfo[] htdi = runtimeInfo.tpmPcrValues;
                        if (htdi != null)
                        {
                            for (int k = 0; k < htdi.Length; k++)
                            {
                                if (pcrList.Contains("," + htdi[k].pcrNumber.ToString() + ","))
                                {
                                    sbyte[] digestValue = htdi[k].digestValue;
                                    byte[] byteDigestValue = (byte[])(Array)digestValue;
                                    String digest = Convert.ToBase64String(byteDigestValue);
                                    pcrKeyValuePair += htdi[k].pcrNumber.ToString() + " " + digest + "\n";
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                throw new Exception("Get Quote Information:" + ex.Message);
            }
            finally
            {
                VMwareHelper.CleanupVMwareService();
            }
            return pcrKeyValuePair;
        }//GetQuoteInformationForHost

        /// <summary>
        /// Retrieves the list of all the VMs and their current power status for the host specified
        /// </summary>
        /// <param name="hostObj">Reference to the Host Object.</param>
        private static string GetVMInfo(ManagedObjectReference hostObj)
        {
            String vmInfoForHost = "";

            try
            {
                // Create Property Spec
                PropertySpec propertySpec = new PropertySpec();
                propertySpec.all = false;
                // We need to retrieve both the name of the host and as well as all the
                // VMs associated with the host.
                propertySpec.pathSet = new String[] { "vm" };
                // Specify the entity that will have both of the above parameters.
                propertySpec.type = "HostSystem";
                PropertySpec[] propertySpecs = new PropertySpec[] { propertySpec };

                // Now create Object Spec
                ObjectSpec objectSpec = new ObjectSpec();
                objectSpec.obj = hostObj;
                ObjectSpec[] objectSpecs = new ObjectSpec[] { objectSpec };

                // Create PropertyFilterSpec using the PropertySpec and ObjectPec
                // created above.
                PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
                propertyFilterSpec.propSet = propertySpecs;
                propertyFilterSpec.objectSet = objectSpecs;

                PropertyFilterSpec[] propertyFilterSpecs = new PropertyFilterSpec[] { propertyFilterSpec };

                ObjectContent[] oContent = VIM_SERVICE.RetrieveProperties(PROP_COLLECTOR, propertyFilterSpecs);
                if (oContent != null)
                {
                    for (int i = 0; i < oContent.Length; i++)
                    {
                        DynamicProperty[] dps = oContent[i].propSet;
                        if (dps != null)
                        {
                            for (int j = 0; j < dps.Length; j++)
                            {
                                if (dps[j].name.Equals("vm"))
                                {
                                    ManagedObjectReference[] vmObj = (ManagedObjectReference[])dps[j].val;
                                    for (int k = 0; k < vmObj.Length; k++)
                                    {
                                        String vmName = (String) VMwareHelper.GetMORProperty((ManagedObjectReference)vmObj[k], "name");
                                        String vmState = VMwareHelper.GetMORProperty((ManagedObjectReference)vmObj[k], "runtime.powerState").ToString();
                                        vmInfoForHost += vmName + "::" + vmState + "\n";
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                throw new Exception("GetVMInfoForHost :" + ex.Message);
            }
            return vmInfoForHost;
        }// GetVMInfo

        /// <summary>
        /// Retrieves the list of all the VMs and their current power status for the host specified
        /// </summary>
        /// <param name="hostName">Name of the host</param>
        /// <param name="vCenterConnectionString">Connection string to the vCenter server</param>
        /// <returns>List of VMs and their power status (running or stopped)</returns>
        public static String GetVMDetailsForHost(String hostName, String vCenterConnectionString)
        {
            String vmInfoForHost = "";

            //This is to accept all SSL certifcates by default.
            System.Net.ServicePointManager.ServerCertificateValidationCallback = TrustAllCertificateCallback;

            try
            {
                // Initialize all the required variables and connect to the
                String[] vcenterConn = vCenterConnectionString.Split(';');
                if (vcenterConn.Length != 3)
                {
                    throw new Exception("The vCenter connection information is not valid for host:" + hostName);
                }
                // Connect to the vCenter server with the passed in parameters
                VMwareHelper.InitializeVMwareService(vcenterConn[0], vcenterConn[1], vcenterConn[2]);

                ManagedObjectReference[] hostObjects;
                hostObjects = VMwareHelper.GetEntitiesByType("HostSystem");

                foreach (ManagedObjectReference hostObj in hostObjects)
                {
                    String hostNameFromVC = VMwareHelper.GetHostInfo(hostObj);

                    // Skip the remaining processing if the hostName does not match the value we are
                    // looking for.
                    if (hostNameFromVC != hostName)
                        continue;

                    vmInfoForHost = GetVMInfo(hostObj);
                    break;
                }
            }
            catch (Exception ex)
            {
                throw new Exception("Get Quote Information:" + ex.Message);
            }
            finally
            {
                VMwareHelper.CleanupVMwareService();
            }
            return vmInfoForHost;
        }//GetVMDetailsForHost

        /// <summary>
        /// Gets the ManagedObjectReference for the Virtual Machine using its name.
        /// </summary>
        /// <param name="vmName">Name of the VM</param>
        /// <returns>MOR of VM</returns>
        private static ManagedObjectReference GetVMRefByName(string vmName)
        {
            ManagedObjectReference vmMORef = null;

            try
            {
                // Create a traversal spec that starts from the 'root' objects 
                // and traverses the inventory tree to get to the VirtualMachines.
                // Build the traversal specs bottoms up

                // Traversal to get to the vmFolder from DataCenter
                TraversalSpec dcToVMF = new TraversalSpec();
                dcToVMF.name = "dcToVMF";
                dcToVMF.type = "Datacenter";
                dcToVMF.path = "vmFolder";
                dcToVMF.skip = false;
                dcToVMF.selectSet = new SelectionSpec[] { new SelectionSpec() };
                dcToVMF.selectSet[0].name = "visitFolders";

                //TraversalSpec to get to the DataCenter from rootFolder
                TraversalSpec tSpec = new TraversalSpec();
                tSpec.name = "visitFolders";
                tSpec.type = "Folder";
                tSpec.path = "childEntity";
                tSpec.skip = false;
                tSpec.selectSet = new SelectionSpec[] { new SelectionSpec(), new SelectionSpec() };
                tSpec.selectSet[0].name = "visitFolders";
                tSpec.selectSet[1].name = "dcToVMF";

                // Create Property Spec
                PropertySpec propertySpec = new PropertySpec();
                propertySpec.all = false;
                propertySpec.pathSet = new String[] { "name" }; // add all the properties that needs to be retrieved
                propertySpec.type = "VirtualMachine"; 
                PropertySpec[] propertySpecs = new PropertySpec[] { propertySpec };

                // Now create Object Spec
                ObjectSpec objectSpec = new ObjectSpec();
                objectSpec.obj = ROOT_FOLDER;
                objectSpec.skip = true;
                objectSpec.selectSet = new SelectionSpec[] { tSpec, dcToVMF };
                ObjectSpec[] objectSpecs = new ObjectSpec[] { objectSpec };

                // Create PropertyFilterSpec using the PropertySpec and ObjectSpec created above.
                PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
                propertyFilterSpec.propSet = propertySpecs;
                propertyFilterSpec.objectSet = objectSpecs;
                PropertyFilterSpec[] propertyFilterSpecs = new PropertyFilterSpec[] { propertyFilterSpec };

                ObjectContent[] oContent = VIM_SERVICE.RetrieveProperties(PROP_COLLECTOR, propertyFilterSpecs);
                if (oContent != null)
                {
                    for (int i = 0; i < oContent.Length; i++)
                    {
                        ManagedObjectReference tempMOR = oContent[i].obj;
                        String tempVMName = (String) VMwareHelper.GetMORProperty(tempMOR, "name");
                        if (tempVMName == vmName)
                        {
                            vmMORef = tempMOR;
                            break;
                        }
                    }
                }

            }
            catch (Exception ex)
            {
                throw new Exception("GetVMRefByName : " + ex.Message);
            }
            return vmMORef;
        } // end of GetVMRefByName()

        /// <summary>
        /// Gets the ManagedObjectReference for the host using its name.
        /// </summary>
        /// <param name="hostName">Name of the host</param>
        /// <returns>MOR of host</returns>
        private static ManagedObjectReference GetHostRefByName(string hostName)
        {
            ManagedObjectReference hostMORef = null;

            try
            {
                // Create a traversal spec that starts from the 'root' objects 
                // and traverses the inventory tree to get to the hosts.
                // Build the traversal specs bottoms up

                // Traversal through Cluster branch
                TraversalSpec crToH = new TraversalSpec();
                crToH.name = "crToH";
                crToH.type = "ComputeResource";
                crToH.path = "host";
                crToH.skip = false;

                // Traversal through the DataCenter branch
                TraversalSpec dcToHf = new TraversalSpec();
                dcToHf.name = "dcToHf";
                dcToHf.type = "Datacenter";
                dcToHf.path = "hostFolder";
                dcToHf.skip = false;
                dcToHf.selectSet = new SelectionSpec[] { new SelectionSpec() };
                dcToHf.selectSet[0].name = "visitFolders";

                // Recurse through the folders
                TraversalSpec tSpec = new TraversalSpec();
                tSpec.name = "visitFolders";
                tSpec.type = "Folder";
                tSpec.path = "childEntity";
                tSpec.skip = false;
                tSpec.selectSet = new SelectionSpec[] { new SelectionSpec(), new SelectionSpec(), new SelectionSpec() };
                tSpec.selectSet[0].name = "visitFolders";
                tSpec.selectSet[1].name = "dcToHf";
                tSpec.selectSet[2].name = "crToH";


                // Create Property Spec
                PropertySpec propertySpec = new PropertySpec();
                propertySpec.all = false;
                propertySpec.pathSet = new String[] { "name" }; // add all the properties that needs to be retrieved
                propertySpec.type = "HostSystem";
                PropertySpec[] propertySpecs = new PropertySpec[] { propertySpec };

                // Now create Object Spec
                ObjectSpec objectSpec = new ObjectSpec();
                objectSpec.obj = ROOT_FOLDER;
                objectSpec.skip = true;
                objectSpec.selectSet = new SelectionSpec[] { tSpec, dcToHf, crToH };
                ObjectSpec[] objectSpecs = new ObjectSpec[] { objectSpec };

                // Create PropertyFilterSpec using the PropertySpec and ObjectSpec created above.
                PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
                propertyFilterSpec.propSet = propertySpecs;
                propertyFilterSpec.objectSet = objectSpecs;
                PropertyFilterSpec[] propertyFilterSpecs = new PropertyFilterSpec[] { propertyFilterSpec };

                ObjectContent[] oContent = VIM_SERVICE.RetrieveProperties(PROP_COLLECTOR, propertyFilterSpecs);
                if (oContent != null)
                {
                    for (int i = 0; i < oContent.Length; i++)
                    {
                        ManagedObjectReference tempMOR = oContent[i].obj;
                        String tempHostName = (String)VMwareHelper.GetMORProperty(tempMOR, "name");
                        if (tempHostName == hostName)
                        {
                            hostMORef = tempMOR;
                            break;
                        }
                    }
                }

            }
            catch (Exception ex)
            {
                throw new Exception("GetVMRefByName : " + ex.Message);
            }
            return hostMORef;
        } // end of GetHostRefByName()

        /// <summary>
        /// Retrieves the vCenter version information.
        /// </summary>
        /// <returns>Version details.</returns>
        private static String GetVCenterVersion()
        {
            String vCenterVersion = SERVICE_CONTENT.about.version;
            return vCenterVersion;
        }

        /// <summary>
        /// Retrieves the ESX version of the host
        /// </summary>
        /// <param name="hostName">Name of the host for which we need the version</param>
        /// <returns>Version information</returns>
        public static String GetHostESXVersion(String hostName, String vCenterConnectionString)
        {
            ManagedObjectReference hostMORef = null;
            String esxHostVersion = String.Empty;
            try
            {
                // Initialize all the required variables and connect to the
                String[] vcenterConn = vCenterConnectionString.Split(';');
                if (vcenterConn.Length != 3)
                {
                    throw new Exception("The vCenter connection information is not valid.");
                }

                // Connect to the vCenter server with the passed in parameters
                VMwareHelper.InitializeVMwareService(vcenterConn[0], vcenterConn[1], vcenterConn[2]);

                // We need the Managed Object Reference for the host.
                hostMORef = VMwareHelper.GetHostRefByName(hostName);

                if (hostMORef == null)
                    throw new Exception("Invalid host specified.");

                // Retrieve the Host version.
                esxHostVersion = GetHostVersion(hostMORef);
            }
            catch (Exception esxExp)
            {
                throw new Exception("Error during retrival of ESX version of the host. " + esxExp.Message);
            }
            return esxHostVersion;
        }

        /// <summary>
        /// Retrieves the host version information
        /// </summary>
        /// <param name="hostObj">MOR of the host</param>
        /// <returns>Version details</returns>
        private static String GetHostVersion(ManagedObjectReference hostObj)
        {
            String hostVersion = "";
            hostVersion = (String) GetMORProperty(hostObj, "config.product.version");
            return hostVersion;
        }

        /// <summary>
        /// Gets the attestation report for the specified host
        /// </summary>
        /// <param name="hostName">Name of the host for which the attestation is required</param>
        /// <param name="vCenterConnectionString">Connection string to the vCenter server</param>
        /// <returns>String representation of all the PCR and Module hashes.</returns>
        public static XElement GetAttestationReport(String hostName, String vCenterConnectionString, String pcrList="0,17,18,19,20")
        {
            // We will use the XMLTextWriter to format the report.
            MemoryStream memReportWriter = new MemoryStream();
            XmlTextWriter xmlReportWriter = new XmlTextWriter(memReportWriter, Encoding.UTF8);
            String vCenterVersion = "", esxHostVersion = "";
            Boolean isvCenterOf51Ver = true;

            // We need the Managed Object Reference for the host
            ManagedObjectReference hostMORef = null;
            XElement xeReport;

            //This is to accept all SSL certifcates by default.
            System.Net.ServicePointManager.ServerCertificateValidationCallback = TrustAllCertificateCallback;

            try
            {
                // Initialize all the required variables and connect to the
                String[] vcenterConn = vCenterConnectionString.Split(';');
                if (vcenterConn.Length != 3)
                {
                    throw new Exception("The vCenter connection information is not valid.");
                }

                // Connect to the vCenter server with the passed in parameters
                VMwareHelper.InitializeVMwareService(vcenterConn[0], vcenterConn[1], vcenterConn[2]);

                // Retrieve the version of the vCenter we are talking to.
                vCenterVersion = GetVCenterVersion();
                if (!vCenterVersion.ToLower().Contains("5.1"))
                {
                    isvCenterOf51Ver = false;
                    // throw new Exception("The Attestation report should be run against only vCenter 5.1 or higer version");
                }

                // We need the Managed Object Reference for the host.
                hostMORef = VMwareHelper.GetHostRefByName(hostName);

                if (hostMORef == null)
                    throw new Exception("Invalid host specified for retrieving attestation report.");

                // Retrieve the Host version.
                esxHostVersion = GetHostVersion(hostMORef);

                // Create the XML document
                xmlReportWriter.Flush();
                xmlReportWriter.Formatting = Formatting.Indented;
                xmlReportWriter.WriteStartDocument();
                xmlReportWriter.WriteStartElement("Host_Attestation_Report"); // write the root tag
                xmlReportWriter.WriteAttributeString("Name", hostName);
                xmlReportWriter.WriteAttributeString("vCenterVersion", vCenterVersion);
                xmlReportWriter.WriteAttributeString("ESXHostVersion", esxHostVersion);

                // Depending on the vCenter version we need to process the TPM PCR information
                if (isvCenterOf51Ver == false)
                {
                    #region Process vCenter 5.0 or earlier

                    if (pcrList.Trim() == String.Empty)
                        pcrList = ",0,17,18,19,20,";
                    else
                        pcrList = "," + pcrList + ",";

                    // In this case, the TPM PCR information will be stored at the host attribute
                    // in the vCenter DB. We will retrieve the same and store it in the XML file.
                    Object objFlag = VMwareHelper.GetMORProperty(hostMORef, "capability.tpmSupported");
                    if ((Boolean)objFlag == false)
                    {
                        xmlReportWriter.WriteStartElement("PCRInfo");
                        xmlReportWriter.WriteAttributeString("Error", "Host does not support TPM");
                        xmlReportWriter.WriteEndElement();                       
                    }
                    else
                    {
                        // Refresh the runtime information
                        HostRuntimeInfo runtimeInfo = (HostRuntimeInfo)VMwareHelper.GetMORProperty(hostMORef, "runtime");

                        // Now process the digest information
                        HostTpmDigestInfo[] htdi = runtimeInfo.tpmPcrValues;
                        if (htdi != null)
                        {
                            for (int k = 0; k < htdi.Length; k++)
                            {
                                if (pcrList.Contains("," + htdi[k].pcrNumber.ToString() + ","))
                                {
                                    sbyte[] digestValue = htdi[k].digestValue;
                                    //byte[] byteDigestValue = (byte[])(Array)digestValue;
                                    String digest = ConvertToHexString(digestValue);
                                    xmlReportWriter.WriteStartElement("PCRInfo");
                                    xmlReportWriter.WriteAttributeString("ComponentName", htdi[k].pcrNumber.ToString());
                                    xmlReportWriter.WriteAttributeString("DigestValue", digest);
                                    xmlReportWriter.WriteEndElement();
                                }
                            }
                        }
                    }

                    xmlReportWriter.WriteEndElement(); // this will close "Host_Attestation_Report" tag
                    xmlReportWriter.WriteEndDocument();
                    xmlReportWriter.Flush();
                    
                    memReportWriter.Position = 0;
                    xeReport = XElement.Load(memReportWriter);

                    return xeReport;

                    #endregion

                } // End of procesing of vCenter not being 5.1

                HostTpmAttestationReport hostTrustReport = VIM_SERVICE.QueryTpmAttestationReport(hostMORef);
                if (hostTrustReport != null)
                {
                    // Before we process the event log we need to check the ESX host version
                    // If the ESX Host is 5.0 or earlier, it will not contain the event log and
                    // we need to process only the tpm pcr values.
                    if (esxHostVersion.ToLower().Contains("5.1"))
                    {
                        // Get the details of the tpm events
                        int numEvents = hostTrustReport.tpmEvents.Length;

                        #region Capture GKVs from EventLog

                        for (int i = 0; i < numEvents; i++)
                        {
                            HostTpmEventLogEntry logDetails = hostTrustReport.tpmEvents[i];
                            switch (logDetails.pcrIndex)
                            {
                                // We will process only the components that gets extended into PCR 19. We
                                // will ignore the rest of the event entries.
                                case 0:
                                    break;
                                case 17:
                                    break;
                                case 18:
                                    break;
                                case 20:
                                    break;
                                // All the static components hash values are in this index. So, we will process
                                // all the entries and store them into the database.
                                case 19:
                                    switch (logDetails.eventDetails.ToString())
                                    {
                                        case "Vim25Api.HostTpmSoftwareComponentEventDetails":
                                            HostTpmSoftwareComponentEventDetails swEventLog = (HostTpmSoftwareComponentEventDetails)logDetails.eventDetails;
                                            //if (String.IsNullOrEmpty(swEventLog.vibName))
                                                // Console.WriteLine("Ignore ComponentDetails: " + swEventLog.componentName + " : " + swEventLog.vibName + " : " + swEventLog.vibVendor);
                                            //else
                                            {
                                                //Console.WriteLine("ComponentDetails: " + swEventLog.componentName + " : " + swEventLog.vibName + " : " + swEventLog.vibVendor);
                                                xmlReportWriter.WriteStartElement("EventDetails");
                                                xmlReportWriter.WriteAttributeString("EventName", "Vim25Api.HostTpmSoftwareComponentEventDetails");
                                                xmlReportWriter.WriteAttributeString("ComponentName", swEventLog.componentName);
                                                xmlReportWriter.WriteAttributeString("DigestValue", ConvertToHexString(swEventLog.dataHash));
                                                xmlReportWriter.WriteAttributeString("ExtendedToPCR", logDetails.pcrIndex.ToString());
                                                xmlReportWriter.WriteAttributeString("PackageName", swEventLog.vibName);
                                                xmlReportWriter.WriteAttributeString("PackageVendor", swEventLog.vibVendor);
                                                xmlReportWriter.WriteAttributeString("PackageVersion", swEventLog.vibVersion);
                                                xmlReportWriter.WriteAttributeString("UseHostSpecificDigest", "False");
                                                xmlReportWriter.WriteEndElement();
                                            }
                                            break;
                                        case "Vim25Api.HostTpmOptionEventDetails":
                                            HostTpmOptionEventDetails optEventLog = (HostTpmOptionEventDetails)logDetails.eventDetails;
                                            // Console.WriteLine("BootOptions: " + optEventLog.bootOptions + " : " + optEventLog.optionsFileName);
                                            xmlReportWriter.WriteStartElement("EventDetails");
                                            xmlReportWriter.WriteAttributeString("EventName", "Vim25Api.HostTpmOptionEventDetails");
                                            xmlReportWriter.WriteAttributeString("ComponentName", optEventLog.optionsFileName);
                                            xmlReportWriter.WriteAttributeString("DigestValue", ConvertToHexString(optEventLog.dataHash));
                                            xmlReportWriter.WriteAttributeString("ExtendedToPCR", logDetails.pcrIndex.ToString());
                                            xmlReportWriter.WriteAttributeString("PackageName", "");
                                            xmlReportWriter.WriteAttributeString("PackageVendor", "");
                                            xmlReportWriter.WriteAttributeString("PackageVersion", "");
                                            xmlReportWriter.WriteAttributeString("UseHostSpecificDigest", "False");
                                            xmlReportWriter.WriteEndElement();
                                            break;
                                        case "Vim25Api.HostTpmBootSecurityOptionEventDetails":
                                            HostTpmBootSecurityOptionEventDetails bootEventLog = (HostTpmBootSecurityOptionEventDetails)logDetails.eventDetails;
                                            // Console.WriteLine("BootSecurity: " + bootEventLog.bootSecurityOption);
                                            xmlReportWriter.WriteStartElement("EventDetails");
                                            xmlReportWriter.WriteAttributeString("EventName", "Vim25Api.HostTpmBootSecurityOptionEventDetails");
                                            xmlReportWriter.WriteAttributeString("ComponentName", bootEventLog.bootSecurityOption);
                                            xmlReportWriter.WriteAttributeString("DigestValue", ConvertToHexString(bootEventLog.dataHash));
                                            xmlReportWriter.WriteAttributeString("ExtendedToPCR", logDetails.pcrIndex.ToString());
                                            xmlReportWriter.WriteAttributeString("PackageName", "");
                                            xmlReportWriter.WriteAttributeString("PackageVendor", "");
                                            xmlReportWriter.WriteAttributeString("PackageVersion", "");
                                            xmlReportWriter.WriteAttributeString("UseHostSpecificDigest", "False");
                                            xmlReportWriter.WriteEndElement();
                                            break;
                                        case "Vim25Api.HostTpmCommandEventDetails":
                                            HostTpmCommandEventDetails cmdEventLog = (HostTpmCommandEventDetails)logDetails.eventDetails;
                                            //Console.WriteLine("CommandEvent: " + cmdEventLog.commandLine);
                                            xmlReportWriter.WriteStartElement("EventDetails");
                                            xmlReportWriter.WriteAttributeString("EventName", "Vim25Api.HostTpmCommandEventDetails");
                                            // We should not store the actual command line data here since it is host specific.
                                            xmlReportWriter.WriteAttributeString("ComponentName", ""); //cmdEventLog.commandLine);
                                            xmlReportWriter.WriteAttributeString("DigestValue", ConvertToHexString(cmdEventLog.dataHash));
                                            xmlReportWriter.WriteAttributeString("ExtendedToPCR", logDetails.pcrIndex.ToString());
                                            xmlReportWriter.WriteAttributeString("PackageName", "");
                                            xmlReportWriter.WriteAttributeString("PackageVendor", "");
                                            xmlReportWriter.WriteAttributeString("PackageVersion", "");
                                            xmlReportWriter.WriteAttributeString("UseHostSpecificDigest", "True");
                                            xmlReportWriter.WriteAttributeString("HostName", hostName);
                                            xmlReportWriter.WriteEndElement();
                                            break;
                                        default:
                                            break;
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    } // End of processing the event log

                    #endregion
                    
                    #region Capture main PCR digest values

                    // Convert the pcr list string into an List of integers.
                    List<int> intPCRList = new List<int>(Array.ConvertAll(pcrList.Split(','), Convert.ToInt32));
                    
                    for (int k = 0; k < hostTrustReport.tpmPcrValues.Length; k++)
			        {
                        HostTpmDigestInfo pcrInfo = hostTrustReport.tpmPcrValues[k];
                        if (intPCRList.Contains(pcrInfo.pcrNumber))
                        {
                            xmlReportWriter.WriteStartElement("PCRInfo");
                            xmlReportWriter.WriteAttributeString("ComponentName", pcrInfo.pcrNumber.ToString());
                            xmlReportWriter.WriteAttributeString("DigestValue", ConvertToHexString(pcrInfo.digestValue));
                            xmlReportWriter.WriteEndElement();
                        }
                        #region Old SwitchStatment.. Not needed.

                        /*switch (pcrInfo.pcrNumber)
                        {
                            case 0:
                                // Console.WriteLine("PCR Details:" + pcrInfo.pcrNumber + " : " + Convert.ToBase64String((byte[])(Array)pcrInfo.digestValue)); 
                                xmlReportWriter.WriteStartElement("PCRInfo");
                                xmlReportWriter.WriteAttributeString("ComponentName", pcrInfo.pcrNumber.ToString());
                                xmlReportWriter.WriteAttributeString("DigestValue", ConvertToHexString(pcrInfo.digestValue));
                                xmlReportWriter.WriteEndElement();
                                break;
                            case 17:
                                xmlReportWriter.WriteStartElement("PCRInfo");
                                xmlReportWriter.WriteAttributeString("ComponentName", pcrInfo.pcrNumber.ToString());
                                xmlReportWriter.WriteAttributeString("DigestValue", ConvertToHexString(pcrInfo.digestValue));
                                xmlReportWriter.WriteEndElement();
                                break;
                            case 18:
                                xmlReportWriter.WriteStartElement("PCRInfo");
                                xmlReportWriter.WriteAttributeString("ComponentName", pcrInfo.pcrNumber.ToString());
                                xmlReportWriter.WriteAttributeString("DigestValue", ConvertToHexString(pcrInfo.digestValue));
                                xmlReportWriter.WriteEndElement();
                                break;
                            case 19:
                                xmlReportWriter.WriteStartElement("PCRInfo");
                                xmlReportWriter.WriteAttributeString("ComponentName", pcrInfo.pcrNumber.ToString());
                                xmlReportWriter.WriteAttributeString("DigestValue", ConvertToHexString(pcrInfo.digestValue));
                                xmlReportWriter.WriteEndElement();
                                break;
                            case 20:
                                xmlReportWriter.WriteStartElement("PCRInfo");
                                xmlReportWriter.WriteAttributeString("ComponentName", pcrInfo.pcrNumber.ToString());
                                xmlReportWriter.WriteAttributeString("DigestValue", ConvertToHexString(pcrInfo.digestValue));
                                xmlReportWriter.WriteEndElement();
                                break;
                            case 22:
                                xmlReportWriter.WriteStartElement("PCRInfo");
                                xmlReportWriter.WriteAttributeString("ComponentName", pcrInfo.pcrNumber.ToString());
                                xmlReportWriter.WriteAttributeString("DigestValue", ConvertToHexString(pcrInfo.digestValue));
                                xmlReportWriter.WriteEndElement();
                                break;
                            default:
                                break;
                        }
                        */
                        #endregion
                    }

                    #endregion

                }

                xmlReportWriter.WriteEndElement(); // this will close "Host_Attestation_Report" tag
                xmlReportWriter.WriteEndDocument();
                xmlReportWriter.Flush();
                // Let us convert the memory string into a string and return back the same
                memReportWriter.Position = 0;

                xeReport = XElement.Load(memReportWriter);
                // xeReport.Save(@"c:\temp\"+hostName+DateTime.Now+".xml");

                // Read MemoryStream into the string using the StreamReader so that we can convert it to a 
                // string to be returned back to the caller.
                // StreamReader sReader = new StreamReader(memReportWriter);
                // xmlReport = sReader.ReadToEnd();
            }
            catch (Exception ex)
            {
                throw new Exception("Query Attestation Report:" + ex.Message);
            }
            finally
            {
                VMwareHelper.CleanupVMwareService();
            }
            return xeReport;
        } // end of GetAttestationReport

        /// <summary>
        /// Starts the VM if the current state of the VM is poweredOff.
        /// </summary>
        /// <param name="vmName">Name of the VM to start</param>
        /// <param name="vCenterConnectionString">Connection string to the vCenter that is managing
        /// the host</param>
        /// <returns>Success or Failure</returns>
        public static String PowerOnVM(String hostName, String vmName, String vCenterConnectionString)
        {
            // We need the Managed Object Reference for both the host and VM.
            ManagedObjectReference vmMORef = null;
            ManagedObjectReference hostMORef = null;
            String powerOnStatus = String.Empty;

            //This is to accept all SSL certifcates by default.
            System.Net.ServicePointManager.ServerCertificateValidationCallback = TrustAllCertificateCallback;

            try
            {
                // Initialize all the required variables and connect to the
                String[] vcenterConn = vCenterConnectionString.Split(';');
                if (vcenterConn.Length != 3)
                {
                    throw new Exception("The vCenter connection information is not valid.");
                }

                // Connect to the vCenter server with the passed in parameters
                VMwareHelper.InitializeVMwareService(vcenterConn[0], vcenterConn[1], vcenterConn[2]);

                // We need the Managed Object Reference for both the host and VM.
                vmMORef = VMwareHelper.GetVMRefByName(vmName);
                hostMORef = VMwareHelper.GetHostRefByName(hostName);

                if (vmMORef == null || hostMORef == null)
                    throw new Exception("Invalid host or virtual machine specified for power on operation.");

                ManagedObjectReference taskObjRef = VIM_SERVICE.PowerOnVM_Task(vmMORef, hostMORef);
                String result = WaitForTask(taskObjRef);
                if (result.Equals("success"))
                    powerOnStatus = "success";
                else
                {
                    if (result.Contains(".") || result.Contains("\n"))
                    {
                        char[] delimiters = new char[] { '.', '\n' };
                        powerOnStatus = result.Split(delimiters)[0];
                    }
                    else
                        powerOnStatus = result;
                }
            }
            catch (Exception ex)
            {
                powerOnStatus = "Power On VM:" + ex.Message;
            }
            finally
            {
                VMwareHelper.CleanupVMwareService();
            }
            return powerOnStatus;
        } // end of PowerOnVM

        /// <summary>
        /// Shuts down the VM if the current state of the VM is poweredOn.
        /// </summary>
        /// <param name="vmName">Name of the VM to shut down</param>
        /// <param name="vCenterConnectionString">Connection string to the vCenter that is managing
        /// the host</param>
        /// <returns>Success or Failure</returns>
        public static String PowerOffVM(String vmName, String vCenterConnectionString)
        {
            // We need the Managed Object Reference for VM.
            ManagedObjectReference vmMORef = null;
            String powerOffStatus = String.Empty;

            //This is to accept all SSL certifcates by default.
            System.Net.ServicePointManager.ServerCertificateValidationCallback = TrustAllCertificateCallback;

            try
            {
                // Initialize all the required variables and connect to the
                String[] vcenterConn = vCenterConnectionString.Split(';');
                if (vcenterConn.Length != 3)
                {
                    throw new Exception("The vCenter connection information is not valid.");
                }

                // Connect to the vCenter server with the passed in parameters
                VMwareHelper.InitializeVMwareService(vcenterConn[0], vcenterConn[1], vcenterConn[2]);

                ManagedObjectReference[] hostObjects;
                hostObjects = VMwareHelper.GetEntitiesByType("HostSystem");

                vmMORef = VMwareHelper.GetVMRefByName(vmName);

                if (vmMORef == null)
                    throw new Exception("Invalid virtual machine specified for power on operation.");

                ManagedObjectReference taskObjRef = VIM_SERVICE.PowerOffVM_Task(vmMORef);
                String result = WaitForTask(taskObjRef);
                if (result.Equals("success"))
                    powerOffStatus = "success";
                else
                {
                    if (result.Contains(".") || result.Contains("\n"))
                    {
                        char[] delimiters = new char[] { '.', '\n' };
                        powerOffStatus = result.Split(delimiters)[0];
                    }
                    else
                        powerOffStatus = result;
                }

            }
            catch (Exception ex)
            {
                powerOffStatus = "Power On VM:" + ex.Message;
            }
            finally
            {
                VMwareHelper.CleanupVMwareService();
            }

            return powerOffStatus;
        } // end of PowerOffVM

        /// <summary>
        /// Migrates/moves the VM from source host to the destination host chosen.
        /// </summary>
        /// <param name="vmName">Name of the VM to migrate</param>
        /// <param name="destHostName">Destination host to which the VM should be migrated to</param>
        /// <param name="vCenterConnectionString">Connection string to the vCenter that is managing
        /// the host</param>
        /// <returns>Success or Failure</returns>
        public static String MigrateVM(String vmName, String destHostName, String vCenterConnectionString)
        {
            // ManagedObjectReference sourceHostMORef = null;
            ManagedObjectReference destHostMORef = null;
            ManagedObjectReference vmMORef = null;

            String migrationStatus = String.Empty;

            //This is to accept all SSL certifcates by default.
            System.Net.ServicePointManager.ServerCertificateValidationCallback = TrustAllCertificateCallback;

            try
            {
                // Initialize all the required variables and connect to the
                String[] vcenterConn = vCenterConnectionString.Split(';');
                if (vcenterConn.Length != 3)
                {
                    throw new Exception("The vCenter connection information is not valid.");
                }

                // Connect to the vCenter server with the passed in parameters
                VMwareHelper.InitializeVMwareService(vcenterConn[0], vcenterConn[1], vcenterConn[2]);

                // We need the Managed Object Reference for both the host and VM.
                // sourceHostMORef = VMwareHelper.GetHostRefByName(sourceHostName);
                destHostMORef = VMwareHelper.GetHostRefByName(destHostName);
                vmMORef = VMwareHelper.GetVMRefByName(vmName);

                if (destHostMORef == null || vmMORef == null)
                    throw new Exception("Invalid host or virtual machine specified for Migration.");

                // Get the current state of the VM and accordingly do the migration
                String vmPowerState = GetMORProperty(vmMORef, "runtime.powerState").ToString();

                ManagedObjectReference taskObjRef = null;
                if (vmPowerState == "poweredOn")
                {
                    taskObjRef = VIM_SERVICE.MigrateVM_Task(vmMORef, null,
                        destHostMORef, VirtualMachineMovePriority.highPriority,
                        VirtualMachinePowerState.poweredOn, true);
                }
                else
                {
                    taskObjRef = VIM_SERVICE.MigrateVM_Task(vmMORef, null,
                        destHostMORef, VirtualMachineMovePriority.highPriority,
                        VirtualMachinePowerState.poweredOff, true);
                }

                String result = WaitForTask(taskObjRef);
                if (result.Equals("success"))
                    migrationStatus = "success";
                else
                {
                    if (result.Contains(".") || result.Contains("\n"))
                    {
                        char[] delimiters = new char[] { '.', '\n' };
                        migrationStatus = result.Split(delimiters)[0];
                    }
                    else
                        migrationStatus = result;
                }
            }
            catch (Exception ex)
            {
                migrationStatus = "Migrate VM:" + ex.Message;
            }
            finally
            {
                VMwareHelper.CleanupVMwareService();
            }
            return migrationStatus;
        } // end of MigrateVM

        
        #region CODE FOR VMOTION COPIED FROM SAMPLES


        /// <summary>
        /// 
        /// </summary>
        /// <param name="taskmor"></param>
        /// <returns></returns>
        private static String WaitForTask(ManagedObjectReference taskmor)
        {
            Object[] result = WaitForValues(
                              taskmor, new String[] { "info.state", "info.error" },
                              new String[] { "state" },
                              new Object[][] { new Object[] { TaskInfoState.success, TaskInfoState.error } });
            if (result[0].Equals(TaskInfoState.success))
            {
                return "success";
            }
            else
            {
                TaskInfo tinfo = (TaskInfo)GetDynamicProperty(taskmor, "info");
                LocalizedMethodFault fault = tinfo.error;
                String error = "Error Occured";
                if (fault != null)
                {
                    error = fault.localizedMessage + "Fault : " + fault.fault.ToString();
                }
                return error;
            }
        }


        /// <summary>
        /// 
        /// </summary>
        /// <param name="mor"></param>
        /// <param name="propertyName"></param>
        /// <returns></returns>
        private static Object GetDynamicProperty(ManagedObjectReference mor, String propertyName)
        {
            ObjectContent[] objContent = GetObjectProperties(null, mor,
                  new String[] { propertyName });

            Object propertyValue = null;
            if (objContent != null)
            {
                DynamicProperty[] dynamicProperty = objContent[0].propSet;
                if (dynamicProperty != null)
                {
                    Object dynamicPropertyVal = dynamicProperty[0].val;
                    String dynamicPropertyName = dynamicPropertyVal.GetType().FullName;
                    propertyValue = dynamicPropertyVal;

                }
            }
            return propertyValue;
        }

        /// <summary>
        /// Retrieve contents for a single object based on the property collector
        /// registered with the service. 
        /// </summary>
        /// <param name="collector">Property collector registered with service</param>
        /// <param name="mobj">Managed Object Reference to get contents for</param>
        /// <param name="properties">names of properties of object to retrieve</param>
        /// <returns>retrieved object contents</returns>
        private static ObjectContent[] GetObjectProperties(ManagedObjectReference collector, ManagedObjectReference mobj,
            string[] properties)
        {
            if (mobj == null)
            {
                return null;
            }

            ManagedObjectReference usecoll = collector;
            if (usecoll == null)
            {
                usecoll = SERVICE_CONTENT.propertyCollector;
            }

            PropertyFilterSpec spec = new PropertyFilterSpec();
            spec.propSet = new PropertySpec[] { new PropertySpec() };
            spec.propSet[0].all = properties == null || properties.Length == 0;
            spec.propSet[0].allSpecified = spec.propSet[0].all;
            spec.propSet[0].type = mobj.type;
            spec.propSet[0].pathSet = properties;

            spec.objectSet = new ObjectSpec[] { new ObjectSpec() };
            spec.objectSet[0].obj = mobj;
            spec.objectSet[0].skip = false;

            return VIM_SERVICE.RetrieveProperties(usecoll, new PropertyFilterSpec[] { spec });
        }

        /// <summary>
        /// Handle Updates for a single object. 
        /// waits till expected values of properties to check are reached
        /// Destroys the ObjectFilter when done.
        /// </summary>
        /// <param name="objmor">MOR of the Object to wait for</param>
        /// <param name="filterProps">Properties list to filter</param>
        /// <param name="endWaitProps">
        ///   Properties list to check for expected values
        ///   these be properties of a property in the filter properties list
        /// </param>
        /// <param name="expectedVals">values for properties to end the wait</param>
        /// <returns>true indicating expected values were met, and false otherwise</returns>
        private static object[] WaitForValues(ManagedObjectReference objmor, string[] filterProps,
           string[] endWaitProps, object[][] expectedVals)
        {
            // version string is initially null
            string version = "";
            object[] endVals = new object[endWaitProps.Length];
            object[] filterVals = new object[filterProps.Length];

            PropertyFilterSpec spec = new PropertyFilterSpec();
            spec.objectSet = new ObjectSpec[] { new ObjectSpec() };
            spec.objectSet[0].obj = objmor;

            spec.propSet = new PropertySpec[] { new PropertySpec() };
            spec.propSet[0].pathSet = filterProps;
            spec.propSet[0].type = objmor.type;

            spec.objectSet[0].selectSet = null;
            spec.objectSet[0].skip = false;
            spec.objectSet[0].skipSpecified = true;

            ManagedObjectReference filterSpecRef = VIM_SERVICE.CreateFilter(SERVICE_CONTENT.propertyCollector, spec, true);

            bool reached = false;

            UpdateSet updateset = null;
            PropertyFilterUpdate[] filtupary = null;
            PropertyFilterUpdate filtup = null;
            ObjectUpdate[] objupary = null;
            ObjectUpdate objup = null;
            PropertyChange[] propchgary = null;
            PropertyChange propchg = null;
            while (!reached)
            {
                updateset = VIM_SERVICE.WaitForUpdates(SERVICE_CONTENT.propertyCollector, version);

                version = updateset.version;

                if (updateset == null || updateset.filterSet == null)
                {
                    continue;
                }

                // Make this code more general purpose when PropCol changes later.
                filtupary = updateset.filterSet;
                filtup = null;
                for (int fi = 0; fi < filtupary.Length; fi++)
                {
                    filtup = filtupary[fi];
                    objupary = filtup.objectSet;
                    objup = null;
                    propchgary = null;
                    for (int oi = 0; oi < objupary.Length; oi++)
                    {
                        objup = objupary[oi];

                        // TODO: Handle all "kind"s of updates.
                        if (objup.kind == ObjectUpdateKind.modify ||
                           objup.kind == ObjectUpdateKind.enter ||
                           objup.kind == ObjectUpdateKind.leave
                           )
                        {
                            propchgary = objup.changeSet;
                            for (int ci = 0; ci < propchgary.Length; ci++)
                            {
                                propchg = propchgary[ci];
                                UpdateValues(endWaitProps, endVals, propchg);
                                UpdateValues(filterProps, filterVals, propchg);
                            }
                        }
                    }
                }

                object expctdval = null;
                // Check if the expected values have been reached and exit the loop if done.
                // Also exit the WaitForUpdates loop if this is the case.
                for (int chgi = 0; chgi < endVals.Length && !reached; chgi++)
                {
                    for (int vali = 0; vali < expectedVals[chgi].Length && !reached; vali++)
                    {
                        expctdval = expectedVals[chgi][vali];

                        reached = expctdval.Equals(endVals[chgi]) || reached;
                    }
                }
            }

            // Destroy the filter when we are done.
            VIM_SERVICE.DestroyPropertyFilter(filterSpecRef);

            return filterVals;
        }

        /// <summary>
        /// set values into the return array
        /// </summary>
        /// <param name="props">property names</param>
        /// <param name="vals">return array</param>
        /// <param name="propchg">Change received</param>
        private static void UpdateValues(string[] props, object[] vals, PropertyChange propchg)
        {
            for (int findi = 0; findi < props.Length; findi++)
            {
                if (propchg.name.LastIndexOf(props[findi]) >= 0)
                {
                    if (propchg.op == PropertyChangeOp.remove)
                    {
                        vals[findi] = "";
                    }
                    else
                    {
                        vals[findi] = propchg.val;
                    }
                }
            }
        }

        #endregion

    }
}
