namespace WLMLoaderTool
{
    partial class loginForm
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
            this.components = new System.ComponentModel.Container();
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(loginForm));
            this.loginIdLbl = new System.Windows.Forms.Label();
            this.passwdLbl = new System.Windows.Forms.Label();
            this.loginID = new System.Windows.Forms.TextBox();
            this.password = new System.Windows.Forms.TextBox();
            this.loginBtn = new System.Windows.Forms.Button();
            this.cancelBtn = new System.Windows.Forms.Button();
            this.errorTxt = new System.Windows.Forms.TextBox();
            this.toolTip1 = new System.Windows.Forms.ToolTip(this.components);
            this.dbMsgTbox = new System.Windows.Forms.TextBox();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // loginIdLbl
            // 
            this.loginIdLbl.AutoSize = true;
            this.loginIdLbl.Font = new System.Drawing.Font("Calibri", 9.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.loginIdLbl.Location = new System.Drawing.Point(24, 59);
            this.loginIdLbl.Name = "loginIdLbl";
            this.loginIdLbl.Size = new System.Drawing.Size(50, 15);
            this.loginIdLbl.TabIndex = 0;
            this.loginIdLbl.Text = "Login ID";
            // 
            // passwdLbl
            // 
            this.passwdLbl.AutoSize = true;
            this.passwdLbl.Font = new System.Drawing.Font("Calibri", 9.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.passwdLbl.Location = new System.Drawing.Point(24, 94);
            this.passwdLbl.Name = "passwdLbl";
            this.passwdLbl.Size = new System.Drawing.Size(59, 15);
            this.passwdLbl.TabIndex = 1;
            this.passwdLbl.Text = "Password";
            // 
            // loginID
            // 
            this.loginID.Font = new System.Drawing.Font("Calibri", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.loginID.Location = new System.Drawing.Point(110, 56);
            this.loginID.Name = "loginID";
            this.loginID.Size = new System.Drawing.Size(297, 23);
            this.loginID.TabIndex = 2;
            this.toolTip1.SetToolTip(this.loginID, "Please use the WLM Portal credentials to log in.");
            // 
            // password
            // 
            this.password.Font = new System.Drawing.Font("Calibri", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.password.Location = new System.Drawing.Point(110, 94);
            this.password.Name = "password";
            this.password.Size = new System.Drawing.Size(297, 23);
            this.password.TabIndex = 3;
            this.password.UseSystemPasswordChar = true;
            // 
            // loginBtn
            // 
            this.loginBtn.Font = new System.Drawing.Font("Calibri", 9.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.loginBtn.Location = new System.Drawing.Point(147, 173);
            this.loginBtn.Name = "loginBtn";
            this.loginBtn.Size = new System.Drawing.Size(99, 25);
            this.loginBtn.TabIndex = 4;
            this.loginBtn.Text = "Authenticate";
            this.loginBtn.UseVisualStyleBackColor = true;
            this.loginBtn.Click += new System.EventHandler(this.loginBtn_Click);
            // 
            // cancelBtn
            // 
            this.cancelBtn.Font = new System.Drawing.Font("Calibri", 9.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.cancelBtn.Location = new System.Drawing.Point(264, 173);
            this.cancelBtn.Name = "cancelBtn";
            this.cancelBtn.Size = new System.Drawing.Size(79, 25);
            this.cancelBtn.TabIndex = 5;
            this.cancelBtn.Text = "Cancel";
            this.cancelBtn.UseVisualStyleBackColor = true;
            this.cancelBtn.Click += new System.EventHandler(this.cancelBtn_Click);
            // 
            // errorTxt
            // 
            this.errorTxt.BackColor = System.Drawing.Color.White;
            this.errorTxt.Font = new System.Drawing.Font("Calibri", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.errorTxt.ForeColor = System.Drawing.Color.Black;
            this.errorTxt.Location = new System.Drawing.Point(105, 212);
            this.errorTxt.Multiline = true;
            this.errorTxt.Name = "errorTxt";
            this.errorTxt.ReadOnly = true;
            this.errorTxt.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.errorTxt.Size = new System.Drawing.Size(391, 63);
            this.errorTxt.TabIndex = 6;
            this.errorTxt.Visible = false;
            // 
            // dbMsgTbox
            // 
            this.dbMsgTbox.BackColor = System.Drawing.Color.White;
            this.dbMsgTbox.Font = new System.Drawing.Font("Calibri", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.dbMsgTbox.ForeColor = System.Drawing.Color.Black;
            this.dbMsgTbox.Location = new System.Drawing.Point(110, 129);
            this.dbMsgTbox.Multiline = true;
            this.dbMsgTbox.Name = "dbMsgTbox";
            this.dbMsgTbox.ReadOnly = true;
            this.dbMsgTbox.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.dbMsgTbox.Size = new System.Drawing.Size(297, 31);
            this.dbMsgTbox.TabIndex = 8;
            this.dbMsgTbox.Visible = false;
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Font = new System.Drawing.Font("Calibri", 9.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label1.ForeColor = System.Drawing.Color.Black;
            this.label1.Location = new System.Drawing.Point(24, 132);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(57, 15);
            this.label1.TabIndex = 9;
            this.label1.Text = "Database";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Font = new System.Drawing.Font("Calibri", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label2.Location = new System.Drawing.Point(23, 16);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(372, 19);
            this.label2.TabIndex = 10;
            this.label2.Text = "Please provide White List Manager Portal Credentials";
            // 
            // loginForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 14F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.BackColor = System.Drawing.SystemColors.Control;
            this.ClientSize = new System.Drawing.Size(524, 287);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.dbMsgTbox);
            this.Controls.Add(this.errorTxt);
            this.Controls.Add(this.cancelBtn);
            this.Controls.Add(this.loginBtn);
            this.Controls.Add(this.password);
            this.Controls.Add(this.loginID);
            this.Controls.Add(this.passwdLbl);
            this.Controls.Add(this.loginIdLbl);
            this.Font = new System.Drawing.Font("Calibri", 9F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.Name = "loginForm";
            this.Text = "White List Manifest Loader Tool";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label loginIdLbl;
        private System.Windows.Forms.Label passwdLbl;
        private System.Windows.Forms.TextBox loginID;
        private System.Windows.Forms.TextBox password;
        private System.Windows.Forms.Button loginBtn;
        private System.Windows.Forms.Button cancelBtn;
        private System.Windows.Forms.TextBox errorTxt;
        private System.Windows.Forms.ToolTip toolTip1;
        private System.Windows.Forms.TextBox dbMsgTbox;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
    }
}