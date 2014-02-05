using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml.Linq;
using MySql.Data.MySqlClient;
using System.Data;

namespace Intel.DCSG.IASI.MtWilsonHelperLib
{
    public class MtWilsonHelper
    {
        /// <summary>
        /// Stores the Attestation Report in the backend DB
        /// </summary>
        /// <param name="xeAttReport"></param>
        /// <param name="DBConnectionString"></param>
        /// <param name="mleID"></param>
        /// <returns></returns>
        public static Boolean StoreAttestationReportUsingSP(XElement xeAttReport, String DBConnectionString, int mleID)
        {
            // Return value
            Boolean storeARResult = false;
            String esxHostName = "", vCenterVersion = "", esxHostVersion = "";

            try
            {
                // First let us retrieve the vCenter and ESX host versions. Accordingly we will update
                // the database
                foreach (XAttribute rootAttib in xeAttReport.Attributes())
                {
                    if (rootAttib.Name == "Name")
                        esxHostName = rootAttib.Value;
                    else if (rootAttib.Name == "vCenterVersion")
                        vCenterVersion = rootAttib.Value;
                    else if (rootAttib.Name == "ESXHostVersion")
                        esxHostVersion = rootAttib.Value;
                }

                // If both the vCenter is 5.1 and ESX host is also 5.1, then we need to store the event
                // log.
                if ((vCenterVersion.Contains("5.1")) && (esxHostVersion.Contains("5.1")))
                {
                    #region Store_Event_Log_To_Database

                    //Console.WriteLine("HostName:" + xeAttReport.FirstAttribute.Value);
                    // We will first process all the static modules and copy it into the database
                    IEnumerable<XNode> eventNodes = from eNodes in xeAttReport.DescendantNodes()
                                                    where eNodes.ToString().ToLower().Contains("eventdetails")
                                                    select eNodes;

                    foreach (XElement eventNode in eventNodes)
                    {
                        // For each node we need to call into the stored procedure to store the data
                        // We will initialize all the required variables here, which would be populated
                        // as we traverse through the attributes of the event.
                        String eventName = "", componentName = "", digestValue = "", extendedToPCR = "",
                            packageName = "", packageVendor = "", packageVersion = "", hostName = "",
                            description = "";
                        Boolean useHostDigestValue = false;

                        foreach (XAttribute eNodeAttrib in eventNode.Attributes())
                        {
                            switch (eNodeAttrib.Name.ToString())
                            {
                                case ("EventName"):
                                    eventName = eNodeAttrib.Value;
                                    break;
                                case ("ComponentName"):
                                    componentName = eNodeAttrib.Value;
                                    break;
                                case ("DigestValue"):
                                    digestValue = eNodeAttrib.Value;
                                    break;
                                case ("ExtendedToPCR"):
                                    extendedToPCR = eNodeAttrib.Value;
                                    break;
                                case ("PackageName"):
                                    packageName = eNodeAttrib.Value;
                                    break;
                                case ("PackageVendor"):
                                    packageVendor = eNodeAttrib.Value;
                                    break;
                                case ("PackageVersion"):
                                    packageVersion = eNodeAttrib.Value;
                                    break;
                                case ("UseHostSpecificDigest"):
                                    useHostDigestValue = Convert.ToBoolean(eNodeAttrib.Value);
                                    break;
                                case ("HostName"):
                                    hostName = eNodeAttrib.Value;
                                    break;
                                default:
                                    break;
                            }
                        }

                        // Check if the package is a dynamic package. If it is, then we should not be 
                        // storing it in the database
                        if (packageName == "" && eventName == "Vim25Api.HostTpmSoftwareComponentEventDetails")
                            continue;

                        // Now connect to the database and call into the stored procedure.
                        // String connStr = "SERVER=localhost;PORT=3306;DATABASE=ta_db;UID=root;PWD=password";
                        MySqlCommand cmd = new MySqlCommand("Insert_GKV_Record", new MySqlConnection(DBConnectionString));

                        try
                        {
                            cmd.CommandType = CommandType.StoredProcedure;
                            cmd.Parameters.AddWithValue("mleID", mleID).Direction = ParameterDirection.Input;
                            cmd.Parameters.AddWithValue("eventName", eventName).Direction = ParameterDirection.Input;
                            cmd.Parameters.AddWithValue("componentName", componentName).Direction = ParameterDirection.Input;
                            cmd.Parameters.AddWithValue("digestValue", digestValue).Direction = ParameterDirection.Input;
                            cmd.Parameters.AddWithValue("extendedToPCR", extendedToPCR).Direction = ParameterDirection.Input;
                            cmd.Parameters.AddWithValue("packageName", packageName).Direction = ParameterDirection.Input;
                            cmd.Parameters.AddWithValue("packageVendor", packageVendor).Direction = ParameterDirection.Input;
                            cmd.Parameters.AddWithValue("packageVersion", packageVersion).Direction = ParameterDirection.Input;
                            //cmd.Parameters.AddWithValue("hostName", hostName).Direction = ParameterDirection.Input;
                            cmd.Parameters.AddWithValue("useHostDigestValue", Convert.ToInt16(useHostDigestValue)).Direction = ParameterDirection.Input;
                            cmd.Parameters.AddWithValue("description", description).Direction = ParameterDirection.Input;
                            cmd.Parameters.Add("insertStatus", MySqlDbType.Int16).Direction = ParameterDirection.Output;
                            cmd.Parameters.Add("errorDetails", MySqlDbType.VarChar).Direction = ParameterDirection.Output;
                            cmd.Connection.Open();
                            cmd.ExecuteNonQuery();
                        }
                        catch (Exception ex)
                        {
                            throw new Exception("Exception during Insert_GKV_Record stored procedure execution. "
                                + ex.Message);
                        }
                        finally
                        {
                            cmd.Connection.Close();
                        }

                        // If there are no errors, then the addStatus would be set to 0, which is success in MySQL
                        if (cmd.Parameters["insertStatus"].Value.ToString() == "0")
                            storeARResult = true;
                        else
                        {
                            throw new Exception("Error during Insert_GKV_Record stored procedure execution."
                                + cmd.Parameters["errorDetails"].Value.ToString());
                        }

                    }

                    #endregion
                }

                #region Store_TPMPCRValues_To_Database

                // We will process all the nodes that have the tpmPCRValues if any present
                IEnumerable<XNode> pcrNodes = from pNodes in xeAttReport.DescendantNodes()
                                              where pNodes.ToString().ToLower().Contains("pcrinfo")
                                              select pNodes;

                foreach (XElement pcrNode in pcrNodes)
                {
                    String componentName = "", digestValue = "", description = "";

                    foreach (XAttribute pcrNodeAttrib in pcrNode.Attributes())
                    {
                        switch (pcrNodeAttrib.Name.ToString())
                        {
                            case ("ComponentName"):
                                componentName = pcrNodeAttrib.Value;
                                break;
                            case ("DigestValue"):
                                digestValue = pcrNodeAttrib.Value;
                                break;
                            default:
                                break;
                        }
                    }

                    // We should not store the PCR 0 value here since it is not VMware specific. If would
                    // be stored when the corresponding BIOS MLE is created.
                    // Even we should not store PCR 22 since it is location information.
                    if ((componentName == "0") || (componentName == "22"))
                        continue;

                    if ((vCenterVersion.Contains("5.1")) && (esxHostVersion.Contains("5.1"))
                        && (componentName == "19"))
                    {
                        // Null out the PCR 19 value
                        digestValue = " ";
                    }

                    // Now connect to the database and call into the stored procedure.
                    // String connStr = "SERVER=localhost;PORT=3306;DATABASE=ta_db;UID=root;PWD=password";
                    MySqlCommand cmd = new MySqlCommand("Insert_GKV_Record", new MySqlConnection(DBConnectionString));

                    try
                    {
                        cmd.CommandType = CommandType.StoredProcedure;
                        cmd.Parameters.AddWithValue("mleID", mleID).Direction = ParameterDirection.Input;
                        cmd.Parameters.AddWithValue("eventName", "").Direction = ParameterDirection.Input;
                        cmd.Parameters.AddWithValue("componentName", componentName).Direction = ParameterDirection.Input;
                        cmd.Parameters.AddWithValue("digestValue", digestValue).Direction = ParameterDirection.Input;
                        cmd.Parameters.AddWithValue("extendedToPCR", "").Direction = ParameterDirection.Input;
                        cmd.Parameters.AddWithValue("packageName", "").Direction = ParameterDirection.Input;
                        cmd.Parameters.AddWithValue("packageVendor", "").Direction = ParameterDirection.Input;
                        cmd.Parameters.AddWithValue("packageVersion", "").Direction = ParameterDirection.Input;
                        //cmd.Parameters.AddWithValue("hostName", hostName).Direction = ParameterDirection.Input;
                        cmd.Parameters.AddWithValue("useHostDigestValue", "0").Direction = ParameterDirection.Input;
                        cmd.Parameters.AddWithValue("description", description).Direction = ParameterDirection.Input;
                        cmd.Parameters.Add("insertStatus", MySqlDbType.Int16).Direction = ParameterDirection.Output;
                        cmd.Parameters.Add("errorDetails", MySqlDbType.VarChar).Direction = ParameterDirection.Output;
                        cmd.Connection.Open();
                        cmd.ExecuteNonQuery();
                    }
                    catch (Exception ex)
                    {
                        throw new Exception("Exception during Insert_GKV_Record stored procedure execution. "
                            + ex.Message);
                    }
                    finally
                    {
                        cmd.Connection.Close();
                    }

                    // If there are no errors, then the addStatus would be set to 0, which is success in MySQL
                    if (cmd.Parameters["insertStatus"].Value.ToString() == "0")
                        storeARResult = true;
                    else
                    {
                        throw new Exception("Error during Insert_GKV_Record stored procedure execution."
                            + cmd.Parameters["errorDetails"].Value.ToString());
                    }

                }
                #endregion
            }
            catch (Exception)
            {

                throw;
            }

            return storeARResult;
        }


        /// <summary>
        /// 
        /// </summary>
        /// <param name="xeAttReport"></param>
        /// <param name="DBConnectionString"></param>
        /// <param name="vmmMLEID"></param>
        /// <param name="vmmPCRList"></param>
        /// <param name="biosMLEID"></param>
        /// <param name="biosPCRList"></param>
        /// <returns></returns>
        public static Boolean StoreAttestationReport(XElement xeAttReport, String DBConnectionString, 
            int vmmMLEID, String vmmPCRList, int biosMLEID, String biosPCRList, String locationMapping = "")
        {
            // Return value
            Boolean storeARResult = false;
            String esxHostName = "", vCenterVersion = "", esxHostVersion = "";

            try
            {
                // First let us retrieve the vCenter and ESX host versions. Accordingly we will update
                // the database
                foreach (XAttribute rootAttib in xeAttReport.Attributes())
                {
                    if (rootAttib.Name == "Name")
                        esxHostName = rootAttib.Value;
                    else if (rootAttib.Name == "vCenterVersion")
                        vCenterVersion = rootAttib.Value;
                    else if (rootAttib.Name == "ESXHostVersion")
                        esxHostVersion = rootAttib.Value;
                }
                
                // If both the vCenter is 5.1 and ESX host is also 5.1, then we need to store the event
                // log.
                if ((vCenterVersion.Contains("5.1")) && (esxHostVersion.Contains("5.1")))
                {
                    #region Store_Event_Log_To_Database

                    //Console.WriteLine("HostName:" + xeAttReport.FirstAttribute.Value);
                    // We will first process all the static modules and copy it into the database
                    IEnumerable<XNode> eventNodes = from eNodes in xeAttReport.DescendantNodes()
                                                    where eNodes.ToString().ToLower().Contains("eventdetails")
                                                    select eNodes;

                    foreach (XElement eventNode in eventNodes)
                    {
                        // For each node we need to call into the stored procedure to store the data
                        // We will initialize all the required variables here, which would be populated
                        // as we traverse through the attributes of the event.
                        String eventName = "", componentName = "", digestValue = "", extendedToPCR = "",
                            packageName = "", packageVendor = "", packageVersion = "", hostName = "",
                            description = "", eventFieldName = "";
                        Boolean useHostDigestValue = false;
                        uint eventID = 0, rowCount=0, nameSpaceID = 1;

                        foreach (XAttribute eNodeAttrib in eventNode.Attributes())
                        {
                            switch (eNodeAttrib.Name.ToString())
                            {
                                case ("EventName"):
                                    eventName = eNodeAttrib.Value;
                                    break;
                                case ("ComponentName"):
                                    componentName = eNodeAttrib.Value;
                                    break;
                                case ("DigestValue"):
                                    digestValue = eNodeAttrib.Value;
                                    break;
                                case ("ExtendedToPCR"):
                                    extendedToPCR = eNodeAttrib.Value;
                                    break;
                                case ("PackageName"):
                                    packageName = eNodeAttrib.Value;
                                    break;
                                case ("PackageVendor"):
                                    packageVendor = eNodeAttrib.Value;
                                    break;
                                case ("PackageVersion"):
                                    packageVersion = eNodeAttrib.Value;
                                    break;
                                case ("UseHostSpecificDigest"):
                                    useHostDigestValue = Convert.ToBoolean(eNodeAttrib.Value);
                                    break;
                                case ("HostName"):
                                    hostName = eNodeAttrib.Value;
                                    break;
                                default:
                                    break;
                            }
                        }

                        // Check if the package is a dynamic package. If it is, then we should not be 
                        // storing it in the database
                        if (packageName == "" && eventName == "Vim25Api.HostTpmSoftwareComponentEventDetails")
                            continue;

                        using (MySqlConnection mySQLConnection = new MySqlConnection(DBConnectionString))
                        {
                            MySqlDataReader dataReader = null;
                            MySqlCommand sqlCommand = mySQLConnection.CreateCommand();
                            try
                            {
                                // First get the event details that we need to log into the database.
                                sqlCommand.CommandText = "select id, fieldname from tbl_event_type where name = '"+ eventName +"';";
                                mySQLConnection.Open();
                                dataReader = sqlCommand.ExecuteReader();
                                while (dataReader.Read())
                                {
                                    eventID = dataReader.GetUInt32(0);
                                    eventFieldName = dataReader.GetString(1);
                                }
                                dataReader.Close();

                                if (String.IsNullOrEmpty(eventFieldName))
                                    throw new Exception("Invalid event type present in the attestation report: " + eventName + ".");

                                sqlCommand.CommandText = "select count(`id`) from `tbl_module_manifest` modwlm " +
                                    " where modwlm.`mle_id` = " + vmmMLEID + " and modwlm.`event_id` = " + eventID + 
                                    " and modwlm.`componentname` = '" + eventFieldName + "." + componentName + "';";
                                dataReader = sqlCommand.ExecuteReader();
                                while (dataReader.Read())
                                    rowCount = dataReader.GetUInt32(0);
                                dataReader.Close();

                                // If the useHostSpecificDigest value is true, then we should not be entering
                                // the digest value here. It would be added as part of the registration process
                                if (useHostDigestValue)
                                    digestValue = null;

                                // Check if we need to insert or do an update
                                if (rowCount == 0)
                                {
                                    // Since no record exists lets do an insert
                                    sqlCommand.CommandText = "insert into `tbl_module_manifest` " +
                                        "(`MLE_ID`,`Event_ID`,`NameSpace_ID`,`ComponentName`,`DigestValue`,`ExtendedToPCR`, " +
                                        "`PackageName`,`PackageVendor`,`PackageVersion`,`UseHostSpecificDigestValue`," +
                                        "`Description`,`Created_By`,`Created_On`) values (" +
                                        vmmMLEID + ", " + eventID + ", " + nameSpaceID + ", '" +
                                        eventFieldName + "." + componentName + "', '" + digestValue + "', '" + extendedToPCR + "','" +
                                        packageName + "', '" + packageVendor + "', '" + packageVersion + "', " +
                                        useHostDigestValue + ", '', 1, Now());";
                                }
                                else
                                {
                                    // Since the record already exists, we will just update the digest value.
                                    sqlCommand.CommandText =  "update `tbl_module_manifest` modwlm " + 
                                        "SET `DigestValue` = '" + digestValue + "', `PackageName` = '" + packageName +
                                        "', `PackageVendor` = '" + packageVendor +"', `PackageVersion` = '" + 
                                        packageVersion + "', `Updated_By` = 1, `Updated_On` = Now() where " +                                        
                                        "modwlm.`mle_id` = " + vmmMLEID + " and modwlm.`event_id` = " + eventID + 
                                        " and modwlm.`componentname` = '" + eventFieldName + "." + componentName + "';";  
                                }
                                // Execute the above statement
                                sqlCommand.ExecuteNonQuery();
                            }
                            catch (Exception sqlEx)
                            {
                                throw sqlEx;
                            }
                            mySQLConnection.Close();
                        }
                    }

                #endregion

                }
                
                #region Store_TPMPCRValues_To_Database

                List<int> intVMMPCRList = null;
                List<int> intBIOSPCRList = null;

                // Let us create two lists to hold the pcrs of VMM and BIOS if present. We are
                // creating empty lists so that we do not get any exception.
                if (vmmPCRList != String.Empty)
                    intVMMPCRList = new List<int>(Array.ConvertAll(vmmPCRList.Split(','), Convert.ToInt32));
                else
                    intVMMPCRList = new List<int>();

                if (biosPCRList != String.Empty)
                    intBIOSPCRList = new List<int>(Array.ConvertAll(biosPCRList.Split(','), Convert.ToInt32));
                else
                    intBIOSPCRList = new List<int>();

                #region Updating_Required_Manifest_List

                using (MySqlConnection mySQLConnection = new MySqlConnection(DBConnectionString))
                {
                    MySqlCommand sqlCommand = mySQLConnection.CreateCommand();
                    try
                    {
                        // First update the required manifest list for BIOS MLE
                        sqlCommand.CommandText = "update `tbl_mle` tmle set tmle.`Required_Manifest_List` " +
                        "= '" + biosPCRList.Trim() + "' where tmle.ID = " + biosMLEID + ";";
                        mySQLConnection.Open();
                        sqlCommand.ExecuteNonQuery();

                        // Now update the required manifest list for VMM MLE
                        sqlCommand.CommandText = "update `tbl_mle` tmle set tmle.`Required_Manifest_List` " +
                        "= '" + vmmPCRList.Trim() + "' where tmle.ID = " + vmmMLEID + ";";
                        sqlCommand.ExecuteNonQuery();
                    }
                    catch (Exception sqlEx)
                    {
                        throw new Exception("Error during updation of the required manifest list: " + sqlEx.Message);
                    }
                    mySQLConnection.Close();
                    storeARResult = true;
                }

                #endregion

                // We will process all the nodes that have the tpmPCRValues if any present
                IEnumerable<XNode> pcrNodes = from pNodes in xeAttReport.DescendantNodes()
                                              where pNodes.ToString().ToLower().Contains("pcrinfo")
                                              select pNodes;

                foreach (XElement pcrNode in pcrNodes)
                {
                    String componentName = "", digestValue = "", description = "";
                    uint rowCount = 0;
                    int mleID = 0;

                    foreach (XAttribute pcrNodeAttrib in pcrNode.Attributes())
                    {
                        switch (pcrNodeAttrib.Name.ToString())
                        {
                            case ("ComponentName"):
                                componentName = pcrNodeAttrib.Value;
                                break;
                            case ("DigestValue"):
                                digestValue = pcrNodeAttrib.Value;
                                break;
                            default:
                                break;
                        }
                    }

                    // Store the location information if specified.
                    if ((componentName == "22") && (locationMapping != String.Empty))
                    {
                        #region Adding_Location_Information_To_DB
                        
                        using (MySqlConnection mySQLConnection = new MySqlConnection(DBConnectionString))
                        {
                            MySqlDataReader dataReader = null;
                            MySqlCommand sqlCommand = mySQLConnection.CreateCommand();
                            try
                            {
                                sqlCommand.CommandText = "select count(`id`) from `tbl_location_pcr` pcrloc " +
                                    "where pcrloc.`pcr_value` = '" + digestValue + "';";
                                mySQLConnection.Open();
                                dataReader = sqlCommand.ExecuteReader();
                                while (dataReader.Read())
                                    rowCount = dataReader.GetUInt32(0);
                                dataReader.Close();

                                // Check if we need to insert or do an update
                                if (rowCount == 0)
                                {
                                    // Since no record exists lets do an insert
                                    sqlCommand.CommandText = "insert into `tbl_location_pcr` " +
                                        "(`location`, `pcr_value`) values ('" + locationMapping + "', '" + 
                                        digestValue + "');";
                                }
                                else
                                {
                                    // Since the record already exists, we will just update the digest value.
                                    sqlCommand.CommandText = "update `tbl_location_pcr` pcrloc " +
                                        "set `location` = '" + locationMapping + "' " +
                                        "where pcrloc.`pcr_value` = '" + digestValue + "';";
                                }
                                // Execute the above statement
                                sqlCommand.ExecuteNonQuery();
                            }
                            catch (Exception sqlEx)
                            {
                                throw sqlEx;
                            }
                            mySQLConnection.Close();
                            storeARResult = true;
                        }
                        continue;
                        #endregion
                    }

                    // Choose the mleID based on which list the PCR number is present in.
                    if (intBIOSPCRList.Contains(Convert.ToInt16(componentName)))
                        mleID = biosMLEID;
                    else if (intVMMPCRList.Contains(Convert.ToInt16(componentName)))
                        mleID = vmmMLEID;
                    else
                        continue;


                    if ((vCenterVersion.Contains("5.1")) && (esxHostVersion.Contains("5.1"))
                        && (componentName == "19"))
                    {
                        // Null out the PCR 19 value
                        digestValue = " ";
                    }

                    using (MySqlConnection mySQLConnection = new MySqlConnection(DBConnectionString))
                    {
                        MySqlDataReader dataReader = null;
                        MySqlCommand sqlCommand = mySQLConnection.CreateCommand();
                        try
                        {
                            sqlCommand.CommandText = "select count(`id`) from `tbl_pcr_manifest` pcrwlm " +
                                "where pcrwlm.`MLE_ID` = " + mleID + " and pcrwlm.`Name` = '" + componentName + "';";
                            mySQLConnection.Open();
                            dataReader = sqlCommand.ExecuteReader();
                            while (dataReader.Read())
                                rowCount = dataReader.GetUInt32(0);
                            dataReader.Close();

                            // Check if we need to insert or do an update
                            if (rowCount == 0)
                            {
                                // Since no record exists lets do an insert
                                sqlCommand.CommandText = "insert into `tbl_pcr_manifest` " + 
                                    "(`MLE_ID`, `Name`, `Value`, `Created_By`, `Created_On`, " + 
                                    "`Updated_By`, `Updated_On`, `PCR_Description`) values (" +
                                    mleID + ", '" + componentName + "', '" + digestValue + "', " +
                                    "1, Now(), 1, Now(), '');";
                            }
                            else
                            {
                                // Since the record already exists, we will just update the digest value.
                                sqlCommand.CommandText = "update `tbl_pcr_manifest` pcrwlm " +
                                    "set `Value` = '" + digestValue + "', `Updated_By` = 1, `Updated_On` = Now() " +
                                    "where pcrwlm.`MLE_ID` = " + mleID + " and pcrwlm.`Name` = '" + componentName + "';";
                            }
                            // Execute the above statement
                            sqlCommand.ExecuteNonQuery();
                        }
                        catch (Exception sqlEx)
                        {
                            throw sqlEx;
                        }
                        mySQLConnection.Close();
                        storeARResult = true;
                    }
                }
                #endregion
            }
            catch (Exception ex)
            {

                throw ex;
            }

            return storeARResult;
        }

    }
}
