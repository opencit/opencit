/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.files;

import com.intel.dcsg.cpg.crypto.PasswordHash;
import com.intel.mtwilson.util.ResourceFinder;
import com.intel.mtwilson.My;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadx
 */
public class ClientFilesServlet extends HttpServlet {
    private Logger log = LoggerFactory.getLogger(getClass());

    private String username = null;
    private PasswordHash password = null;
    public ClientFilesServlet() {
        super();
        try {
            //File configFile = ResourceFinder.getFile("PrivacyCA.properties");
            //FileInputStream in = new FileInputStream(configFile);
            Configuration myConf = My.configuration().getConfiguration();
            //Properties p = new Properties();
            //p.load(in);
            username = myConf.getString("ClientFilesDownloadUsername");
            String passwordHashed = myConf.getString("ClientFilesDownloadPassword");
            password = PasswordHash.valueOf(passwordHashed);
            log.debug("Privacy CA ClientFilesServlet read configuration");
        }
        catch(Exception e) {
            log.error("Privacy Error while loading PrivacyCA.properties: {}", e.toString());
        }
        finally {
            if( username == null || password == null ) {
                log.warn("Download username and password not set; client files download disabled");
            }            
        }
    }
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.debug("Client Files request called.");
        String user = request.getParameter("user");
        String pwd = request.getParameter("password");
        try {
        if (username != null && password != null 
                && user != null && user.equals(username) && pwd != null && ! pwd.isEmpty() && password.isEqualTo(pwd)) {
            

            String setUpFile = ResourceFinder.getFile("privacyca-client.properties").getAbsolutePath();
            String fileLocation = setUpFile.substring(0, setUpFile.indexOf("privacyca-client.properties"));

            System.out.println("Path :" + fileLocation + "clientfiles.zip");
            File clientFiles = new File(fileLocation + "clientfiles.zip");

            if (clientFiles.exists()) {
                OutputStream os = null;
                try {
                    response.setContentType("application/x-zip-compressed");
                    InputStream is = new FileInputStream(ResourceFinder.getFile("clientfiles.zip"));

                    int read = 0;
                    byte[] bytes = new byte[1024];
                    os = response.getOutputStream();
                    while ((read = is.read(bytes)) != -1) {
                        os.write(bytes, 0, read);
                    }
                    log.debug("PrivacyCA provided clientfiles.zip");
                } finally {
                    if (os != null) {
                        os.flush();
                        os.close();
                    }
                }
            } else {
                PrintWriter out = response.getWriter();
                response.setContentType("application/html");

                out.write("File not created. Try again later.");
            }
        }
        }catch(Exception e){
            log.debug("PrivacyCA cannot validate user credentials for clientfiles.zip request");
            PrintWriter out = response.getWriter();
            response.setContentType("application/html");
            out.write("Cannot validate your password");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "File Download Servlet.";
    }// </editor-fold>
}
