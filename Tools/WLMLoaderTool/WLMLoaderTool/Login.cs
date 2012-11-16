using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using MySql.Data.MySqlClient;

namespace WLMLoaderTool
{
    public partial class loginForm : Form
    {
        public loginForm()
        {
            InitializeComponent();
            String dbConnString = Properties.Settings.Default.DB_Connection_String;
            dbMsgTbox.Text = dbConnString.Split(';')[0];
            dbMsgTbox.Visible = true;
        }


        /// <summary>
        /// Connects to the WLM DB to do the authentication of the user. If successful, shows
        /// the WLM Loader form.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void loginBtn_Click(object sender, EventArgs e)
        {
            try
            {
                this.errorTxt.ForeColor = Color.Black;
                this.Cursor = Cursors.WaitCursor;
                this.errorTxt.Visible = false;         

                // Ensure that the user has entered the right credentials
                if (String.IsNullOrEmpty(loginID.Text) || String.IsNullOrEmpty(password.Text))
                    throw new Exception("Please specify the login credentials to continue.");

                using (MySqlConnection mySQLConnection = new MySqlConnection(Properties.Settings.Default.DB_Connection_String))
                {
                    uint rowCount = 0;
                    MySqlCommand sqlCommand = mySQLConnection.CreateCommand();
                    try
                    {
                        sqlCommand.CommandText = "select count(`client_id`) from tbl_api_client where `client_id`= '" + loginID.Text + "' and `secret_key` = '" + password.Text + "';";
                        mySQLConnection.Open();
                        MySqlDataReader dataReader = sqlCommand.ExecuteReader();
                        while (dataReader.Read())
                            rowCount = dataReader.GetUInt32(0);
                        dataReader.Close();

                        if (rowCount == 0)
                            throw new Exception("Invalid login or password specified.");
                    }
                    catch (Exception sqlEx)
                    {
                        throw sqlEx;
                    }
                    mySQLConnection.Close();
                }

                // If the authentication is successful, then we will load the WLM Loader form.
                this.Cursor = Cursors.Default;
                new WLMLoader().Visible = true;
                this.Visible = false;
            }
            catch (Exception ex)
            {
                this.errorTxt.ForeColor = Color.Red;
                this.errorTxt.Text = ex.Message;
                this.errorTxt.Visible = true;
                this.Cursor = Cursors.Default;
            }
        }

        private void cancelBtn_Click(object sender, EventArgs e)
        {
            Application.Exit();
        }
    }
}
