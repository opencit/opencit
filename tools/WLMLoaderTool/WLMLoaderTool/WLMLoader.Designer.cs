namespace WLMLoaderTool
{
    partial class WLMLoader
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(WLMLoader));
            this.label1 = new System.Windows.Forms.Label();
            this.serverNameLbl = new System.Windows.Forms.Label();
            this.vCenterConnStringLbl = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.serverName = new System.Windows.Forms.TextBox();
            this.vCenterConnStr = new System.Windows.Forms.TextBox();
            this.uploadBtn = new System.Windows.Forms.Button();
            this.clearBtn = new System.Windows.Forms.Button();
            this.storeReportCBox = new System.Windows.Forms.CheckBox();
            this.errorTBox = new System.Windows.Forms.TextBox();
            this.mleDetails = new System.Windows.Forms.ComboBox();
            this.label2 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.biosMLEs = new System.Windows.Forms.ComboBox();
            this.txtHostLoc = new System.Windows.Forms.TextBox();
            this.lblLocation2 = new System.Windows.Forms.Label();
            this.lblLocation1 = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Font = new System.Drawing.Font("Calibri", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label1.Location = new System.Drawing.Point(23, 23);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(370, 19);
            this.label1.TabIndex = 0;
            this.label1.Text = "Reference  Whitelist Enrollment Environment Details";
            // 
            // serverNameLbl
            // 
            this.serverNameLbl.AutoSize = true;
            this.serverNameLbl.Font = new System.Drawing.Font("Calibri", 9.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.serverNameLbl.Location = new System.Drawing.Point(24, 147);
            this.serverNameLbl.Name = "serverNameLbl";
            this.serverNameLbl.Size = new System.Drawing.Size(174, 15);
            this.serverNameLbl.TabIndex = 12;
            this.serverNameLbl.Text = "VMware ESXi 5.1 Server Name";
            // 
            // vCenterConnStringLbl
            // 
            this.vCenterConnStringLbl.AutoSize = true;
            this.vCenterConnStringLbl.Font = new System.Drawing.Font("Calibri", 9.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.vCenterConnStringLbl.Location = new System.Drawing.Point(24, 183);
            this.vCenterConnStringLbl.Name = "vCenterConnStringLbl";
            this.vCenterConnStringLbl.Size = new System.Drawing.Size(201, 15);
            this.vCenterConnStringLbl.TabIndex = 13;
            this.vCenterConnStringLbl.Text = "VMware vCenter Connection String";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Font = new System.Drawing.Font("Calibri", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label3.Location = new System.Drawing.Point(24, 198);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(201, 13);
            this.label3.TabIndex = 14;
            this.label3.Text = "(https://IP Addr:Port/sdk;Login;Password)";
            // 
            // serverName
            // 
            this.serverName.Font = new System.Drawing.Font("Calibri", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.serverName.Location = new System.Drawing.Point(253, 147);
            this.serverName.Name = "serverName";
            this.serverName.Size = new System.Drawing.Size(399, 23);
            this.serverName.TabIndex = 3;
            // 
            // vCenterConnStr
            // 
            this.vCenterConnStr.BackColor = System.Drawing.Color.White;
            this.vCenterConnStr.Font = new System.Drawing.Font("Calibri", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.vCenterConnStr.Location = new System.Drawing.Point(253, 183);
            this.vCenterConnStr.Name = "vCenterConnStr";
            this.vCenterConnStr.Size = new System.Drawing.Size(399, 23);
            this.vCenterConnStr.TabIndex = 4;
            // 
            // uploadBtn
            // 
            this.uploadBtn.Font = new System.Drawing.Font("Calibri", 9.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.uploadBtn.Location = new System.Drawing.Point(253, 283);
            this.uploadBtn.Name = "uploadBtn";
            this.uploadBtn.Size = new System.Drawing.Size(209, 23);
            this.uploadBtn.TabIndex = 7;
            this.uploadBtn.Text = "Upload Good Known Module Values";
            this.uploadBtn.UseVisualStyleBackColor = true;
            this.uploadBtn.Click += new System.EventHandler(this.uploadBtn_Click);
            // 
            // clearBtn
            // 
            this.clearBtn.Font = new System.Drawing.Font("Calibri", 9.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.clearBtn.Location = new System.Drawing.Point(479, 283);
            this.clearBtn.Name = "clearBtn";
            this.clearBtn.Size = new System.Drawing.Size(75, 23);
            this.clearBtn.TabIndex = 8;
            this.clearBtn.Text = "Clear";
            this.clearBtn.UseVisualStyleBackColor = true;
            this.clearBtn.Click += new System.EventHandler(this.clearBtn_Click);
            // 
            // storeReportCBox
            // 
            this.storeReportCBox.AutoSize = true;
            this.storeReportCBox.Location = new System.Drawing.Point(254, 254);
            this.storeReportCBox.Name = "storeReportCBox";
            this.storeReportCBox.Size = new System.Drawing.Size(268, 17);
            this.storeReportCBox.TabIndex = 6;
            this.storeReportCBox.Text = "Export Good Known Module Values Report as XML";
            this.storeReportCBox.UseVisualStyleBackColor = true;
            // 
            // errorTBox
            // 
            this.errorTBox.BackColor = System.Drawing.Color.White;
            this.errorTBox.Font = new System.Drawing.Font("Calibri", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.errorTBox.ForeColor = System.Drawing.Color.Black;
            this.errorTBox.Location = new System.Drawing.Point(253, 321);
            this.errorTBox.Multiline = true;
            this.errorTBox.Name = "errorTBox";
            this.errorTBox.ReadOnly = true;
            this.errorTBox.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.errorTBox.Size = new System.Drawing.Size(399, 77);
            this.errorTBox.TabIndex = 9;
            this.errorTBox.Visible = false;
            // 
            // mleDetails
            // 
            this.mleDetails.Font = new System.Drawing.Font("Calibri", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.mleDetails.FormattingEnabled = true;
            this.mleDetails.Location = new System.Drawing.Point(254, 109);
            this.mleDetails.Name = "mleDetails";
            this.mleDetails.Size = new System.Drawing.Size(398, 23);
            this.mleDetails.TabIndex = 2;
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Font = new System.Drawing.Font("Calibri", 9.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label2.Location = new System.Drawing.Point(27, 109);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(162, 15);
            this.label2.TabIndex = 11;
            this.label2.Text = "VMM MLEs Configured in DB";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Font = new System.Drawing.Font("Calibri", 9.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label4.Location = new System.Drawing.Point(27, 77);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(157, 15);
            this.label4.TabIndex = 10;
            this.label4.Text = "BIOS MLEs Configured in DB";
            // 
            // biosMLEs
            // 
            this.biosMLEs.Font = new System.Drawing.Font("Calibri", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.biosMLEs.FormattingEnabled = true;
            this.biosMLEs.Location = new System.Drawing.Point(254, 73);
            this.biosMLEs.Name = "biosMLEs";
            this.biosMLEs.Size = new System.Drawing.Size(398, 23);
            this.biosMLEs.TabIndex = 1;
            // 
            // txtHostLoc
            // 
            this.txtHostLoc.BackColor = System.Drawing.Color.White;
            this.txtHostLoc.Font = new System.Drawing.Font("Calibri", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.txtHostLoc.Location = new System.Drawing.Point(254, 219);
            this.txtHostLoc.Name = "txtHostLoc";
            this.txtHostLoc.Size = new System.Drawing.Size(398, 23);
            this.txtHostLoc.TabIndex = 5;
            // 
            // lblLocation2
            // 
            this.lblLocation2.AutoSize = true;
            this.lblLocation2.Font = new System.Drawing.Font("Calibri", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblLocation2.Location = new System.Drawing.Point(24, 236);
            this.lblLocation2.Name = "lblLocation2";
            this.lblLocation2.Size = new System.Drawing.Size(104, 13);
            this.lblLocation2.TabIndex = 16;
            this.lblLocation2.Text = "(If configured in TPM)";
            // 
            // lblLocation1
            // 
            this.lblLocation1.AutoSize = true;
            this.lblLocation1.Font = new System.Drawing.Font("Calibri", 9.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblLocation1.Location = new System.Drawing.Point(24, 221);
            this.lblLocation1.Name = "lblLocation1";
            this.lblLocation1.Size = new System.Drawing.Size(128, 15);
            this.lblLocation1.TabIndex = 15;
            this.lblLocation1.Text = "Map Location Value To";
            // 
            // WLMLoader
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.BackColor = System.Drawing.SystemColors.Control;
            this.ClientSize = new System.Drawing.Size(814, 415);
            this.Controls.Add(this.lblLocation2);
            this.Controls.Add(this.lblLocation1);
            this.Controls.Add(this.txtHostLoc);
            this.Controls.Add(this.biosMLEs);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.mleDetails);
            this.Controls.Add(this.errorTBox);
            this.Controls.Add(this.storeReportCBox);
            this.Controls.Add(this.clearBtn);
            this.Controls.Add(this.uploadBtn);
            this.Controls.Add(this.vCenterConnStr);
            this.Controls.Add(this.serverName);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.vCenterConnStringLbl);
            this.Controls.Add(this.serverNameLbl);
            this.Controls.Add(this.label1);
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.Name = "WLMLoader";
            this.Text = "White List Manifest Loader Tool";
            this.FormClosed += new System.Windows.Forms.FormClosedEventHandler(this.WLMLoader_FormClosed);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label serverNameLbl;
        private System.Windows.Forms.Label vCenterConnStringLbl;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.TextBox serverName;
        private System.Windows.Forms.TextBox vCenterConnStr;
        private System.Windows.Forms.Button uploadBtn;
        private System.Windows.Forms.Button clearBtn;
        private System.Windows.Forms.CheckBox storeReportCBox;
        private System.Windows.Forms.TextBox errorTBox;
        private System.Windows.Forms.ComboBox mleDetails;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.ComboBox biosMLEs;
        private System.Windows.Forms.TextBox txtHostLoc;
        private System.Windows.Forms.Label lblLocation2;
        private System.Windows.Forms.Label lblLocation1;

    }
}

