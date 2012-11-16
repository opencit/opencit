using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using Intel.DCSG.IASI.VMWareHelperLib;
using Intel.DCSG.IASI.MtWilsonHelperLib;
using System.Xml.Linq;
using System.IO;
using MySql.Data.MySqlClient;
using System.Security.Cryptography.X509Certificates;
using System.Net.Security;

namespace WLMLoaderTool
{
    public partial class WLMLoader : Form
    {
        /// <summary>
        /// Function that will ensure to accept all the SSL Certifcates by default. Otherwise there will be errors from
        /// certificates
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="cert"></param>
        /// <param name="chain"></param>
        /// <param name="errors"></param>
        /// <returns></returns>
        public static bool TrustAllCertificateCallback(object sender, X509Certificate cert, X509Chain chain, SslPolicyErrors errors)
        {
            return true;
        }


        public WLMLoader()
        {
            String sqlCmd = "";
            InitializeComponent();
            this.Cursor = Cursors.WaitCursor;
            this.errorTBox.ForeColor = Color.Black;
            MySqlDataAdapter mleAdaptor = null;

            //This is to accept all SSL certifcates by default.
            System.Net.ServicePointManager.ServerCertificateValidationCallback = TrustAllCertificateCallback;

            using (MySqlConnection mySQLConnection = new MySqlConnection(Properties.Settings.Default.DB_Connection_String))
            {                
                try
                {
                    // If we need to update even the BIOS MLE then show the user the list of BIOS MLEs 
                    // currently configured.
                    if (Properties.Settings.Default.BIOS_MLE_PCR_List.Trim() != String.Empty)
                    {
                        sqlCmd = "select id, concat(name,' ',version) as bios_mle_info from tbl_mle where oem_id != 0;";
                        mleAdaptor = new MySqlDataAdapter(sqlCmd, mySQLConnection);
                        DataTable biosDT = new DataTable("bios");
                        mleAdaptor.Fill(biosDT);

                        biosMLEs.DataSource = biosDT;
                        biosMLEs.DisplayMember = biosDT.Columns[1].ColumnName;
                        biosMLEs.ValueMember = biosDT.Columns[0].ColumnName;

                        if (biosDT.Rows.Count != 0)
                            biosMLEs.SelectedIndex = 0;
                    }
                    else
                        biosMLEs.Enabled = false; // Disable the dropdown list.

                    // Now get the list of MLEs defined for VMware
                    sqlCmd = "select id, concat(name,' ',version) as mle_info from tbl_mle where name like '%esx%';";
                    mleAdaptor = new MySqlDataAdapter(sqlCmd, mySQLConnection);
                    DataTable mleDT = new DataTable("mle");
                    mleAdaptor.Fill(mleDT);

                    mleDetails.DataSource = mleDT;
                    mleDetails.DisplayMember = mleDT.Columns[1].ColumnName;
                    mleDetails.ValueMember = mleDT.Columns[0].ColumnName;

                    if (mleDT.Rows.Count == 0)
                        throw new Exception ("Currently there are no MLEs for VMware ESXi configured in the system. " +
                            "Please configure the MLE information using the WLM portal first.");
                    
                    // By default select the first item.
                    mleDetails.SelectedIndex = 0;
                }
                catch (Exception sqlEx)
                {
                    this.errorTBox.ForeColor = Color.Red;
                    this.Cursor = Cursors.Default;
                    throw new Exception("Error getting the MLE details from the database. " + sqlEx.Message);
                }
                // Close the connection to the sql server
                mySQLConnection.Close();
            }
            this.Cursor = Cursors.Default;

            // Check if we need to even update the location information. If not disable the controls
            if (!Properties.Settings.Default.UpdateLocationMapping)
            {
                this.txtHostLoc.Visible = false;
                this.txtHostLoc.Enabled = false;
                this.lblLocation1.Visible = false;
                this.lblLocation2.Visible = false;
            }
        }

        /// <summary>
        /// Clear all the controls and its values.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void clearBtn_Click(object sender, EventArgs e)
        {
            this.errorTBox.ForeColor = Color.Black;
            this.serverName.Text = "";
            this.vCenterConnStr.Text = "";
            this.errorTBox.Text = " ";
            this.errorTBox.Visible = false;
            this.storeReportCBox.Checked = false;

            if (Properties.Settings.Default.BIOS_MLE_PCR_List.Trim() != String.Empty)
                this.biosMLEs.SelectedIndex = 0;

            mleDetails.SelectedIndex = 0;

            if (Properties.Settings.Default.UpdateLocationMapping)
            {
                this.txtHostLoc.Text = String.Empty;
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void uploadBtn_Click(object sender, EventArgs e)
        {
            String reportName = "", message = "", hostLocation = "", hostESXVersion = "", chosenMLEVersion ="";
            XElement attestationReport = null;
            Int32 biosMLEID=0, vmmMLEID=0;

            try
            {
                this.errorTBox.ForeColor = Color.Black;
                this.errorTBox.Text = " ";
                this.errorTBox.Visible = false;
                this.Cursor = Cursors.WaitCursor;

                String hostName = this.serverName.Text;
                String vCenterConnString = this.vCenterConnStr.Text;
                if (String.IsNullOrEmpty(hostName) || String.IsNullOrEmpty(vCenterConnString))
                    throw new Exception("Invalid input parameters specified");

                // ((System.Data.DataRowView)(mleDetails.SelectedItem)).Row.ItemArray[1]
                // Check if the version of the MLE chosen is same as the version of the host 
                // chosen from which we need to get the attestation report.
                hostESXVersion = VMwareHelper.GetHostESXVersion(hostName, vCenterConnString);
                chosenMLEVersion = ((System.Data.DataRowView)(mleDetails.SelectedItem)).Row.ItemArray[1].ToString();
                if (!((hostESXVersion.ToLower().Trim().Contains("5.0") &&
                    chosenMLEVersion.ToLower().Trim().Contains("5.0")) ||
                    (hostESXVersion.ToLower().Trim().Contains("5.1") &&
                    chosenMLEVersion.ToLower().Trim().Contains("5.1"))))
                    throw new Exception("There is mismatch in the ESXi version of the host and the associated MLE chosen.");

                try
                {
                    String pcrList = Properties.Settings.Default.BIOS_MLE_PCR_List.Trim() + "," +
                        Properties.Settings.Default.VMM_MLE_PCR_List.Trim();

                    // Since location information is stored in 22, we are hard coding the same.
                    if (Properties.Settings.Default.UpdateLocationMapping)
                        pcrList += ",22";

                    attestationReport = VMwareHelper.GetAttestationReport(hostName, 
                        vCenterConnString, pcrList);
                }
                catch (Exception exp)
                {
                    throw new Exception("Error getting the report from the host specified. " + exp.Message);
                }
                
                // Check if the report needs to be saved
                if (this.storeReportCBox.Checked)
                {
                    // If the report directory configured does not exist, create the same.
                    String reportFolderName = Properties.Settings.Default.ReportFolder;
                    if (!Directory.Exists(reportFolderName))
                        Directory.CreateDirectory(reportFolderName);

                    reportName = reportFolderName + DateTime.Now.Millisecond + ".xml";
                    attestationReport.Save(reportName);
                    message = "GKV report has been saved to " + reportName + ". ";
                }
                // attestationReport = XElement.Load(@"C:\temp\reports\809.xml");
                if (Properties.Settings.Default.UpdateDB)
                {
                    String dbConnString = Properties.Settings.Default.DB_Connection_String;

                    // if the location mapping is requested, get the corresponding location name
                    if ((Properties.Settings.Default.UpdateLocationMapping) &&
                        (this.txtHostLoc.Text.Trim() != String.Empty))
                    {
                        hostLocation = this.txtHostLoc.Text.Trim();
                    }
                    else
                        hostLocation = String.Empty;

                    // Check if we even need to setup the BIOS MLE
                    if (Properties.Settings.Default.BIOS_MLE_PCR_List.Trim() != String.Empty)
                        biosMLEID = Convert.ToInt16(biosMLEs.SelectedValue);

                    vmmMLEID = Convert.ToInt16(mleDetails.SelectedValue);
                    String vmmPCRList = Properties.Settings.Default.VMM_MLE_PCR_List.Trim();
                    String biosPCRList = Properties.Settings.Default.BIOS_MLE_PCR_List.Trim();

                    // Store the attestation report in the database
                    Boolean dbUpdateStatus = MtWilsonHelper.StoreAttestationReport(attestationReport, dbConnString,
                        vmmMLEID, vmmPCRList, biosMLEID, biosPCRList, hostLocation);

                    if (dbUpdateStatus)
                        message += "GKVs have been successfully updated to the database specified in the configuration.";
                    else
                        message += "There was an issue with updating the database with the GKVs.";
                }

                // Show the message.
                this.errorTBox.Text = message;
                this.errorTBox.Visible = true;
                this.Cursor = Cursors.Default;
            }
            catch (Exception ex)
            {
                this.errorTBox.ForeColor = Color.Red;
                this.errorTBox.Text = ex.Message;
                this.errorTBox.Visible = true;
                this.Cursor = Cursors.Default;
            }     
        }

        private void WLMLoader_FormClosed(object sender, FormClosedEventArgs e)
        {
            Application.Exit();
        }

        private void UpdateHostManifest(int oldMMID, int mleID, String mleName)
        {
            int newMMID = 0;

            using (MySqlConnection mySQLConnection = new MySqlConnection(Properties.Settings.Default.DB_Connection_String))
            {
                MySqlCommand sqlCommand = mySQLConnection.CreateCommand();
                try
                {
                    // First get the NEW Module_Manifest_ID corresponding to the Command event type.
                    sqlCommand.CommandText = "select id from tbl_module_manifest where event_id = 4 and mle_id=" + mleID + ";";
                    mySQLConnection.Open();
                    MySqlDataReader dataReader = sqlCommand.ExecuteReader();
                    while (dataReader.Read())
                        newMMID = Convert.ToInt16(dataReader[0]);
                    dataReader.Close();

                    // Now update all the entries in the host specific manifest table
                    sqlCommand.CommandText = "update ta_db.tbl_host_specific_manifest set module_manifest_id =" + newMMID + " where module_manifest_id= 999999;";
                    sqlCommand.ExecuteNonQuery();

                }
                catch (Exception sqlEx)
                {
                    throw new Exception("Error during the deletion of existing GKVs for MLE: " + mleName + "." + sqlEx.Message);
                }
                mySQLConnection.Close();
            }
        }

        private int CleanUpDatabaseManifest(int mleID, String mleName)
        {
            int moduleManifestID = 0;

            using (MySqlConnection mySQLConnection = new MySqlConnection(Properties.Settings.Default.DB_Connection_String))
            {
                MySqlCommand sqlCommand = mySQLConnection.CreateCommand();
                try
                {
                    // First get the Module_Manifest_ID corresponding to the Command event type.
                    // We need to update the host specific table later on using this value.
                    sqlCommand.CommandText = "select id from tbl_module_manifest where event_id = 4 and mle_id=" + mleID + ";";
                    mySQLConnection.Open();
                    MySqlDataReader dataReader = sqlCommand.ExecuteReader();
                    while (dataReader.Read())
                        moduleManifestID = Convert.ToInt16(dataReader[0]);
                    dataReader.Close();

                    // Now update all the entries in the host specific manifest table to a temporary ID
                    sqlCommand.CommandText = "update ta_db.tbl_host_specific_manifest set module_manifest_id = 999999 where module_manifest_id=" + moduleManifestID + ";";
                    sqlCommand.ExecuteNonQuery();

                    // Now delete all the entries in the module manifest table
                    sqlCommand.CommandText = "delete from ta_db.tbl_module_manifest where mle_id=" + mleID + ";";
                    sqlCommand.ExecuteNonQuery();

                    // Now delete all the entries in the PCR manifest table
                    sqlCommand.CommandText = "delete from ta_db.tbl_pcr_manifest where mle_id=" + mleID + ";";
                    sqlCommand.ExecuteNonQuery();
                }
                catch (Exception sqlEx)
                {
                    throw new Exception("Error during the deletion of existing GKVs for MLE: " + mleName + "." + sqlEx.Message);
                }
                mySQLConnection.Close();
            }
            return moduleManifestID;
        }

    }
}
