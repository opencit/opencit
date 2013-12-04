package com.intel.mountwilson.security;

import java.io.IOException;
import java.net.URL;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityFilter implements Filter {
        Logger log = LoggerFactory.getLogger(getClass().getName());
	
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		log.info("SecurityFilter >>");
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		String url = request.getServletPath();
		
		HttpSession httpSession = request.getSession(false);
		System.out.println(httpSession +"----"+url);
		if (httpSession != null && !httpSession.isNew()) {
			Object obj = httpSession.getAttribute("logged-in");
			if ( obj != null) {
				boolean logged =  (Boolean) obj;
				System.err.println(logged);
				if (logged) {
                    // fix for issue #1038 cross site request forgery (CSRF) is to ensure that logged-in users are in control of requests
                    // so first we deny any request coming from a source other than a link or form in the portal by ensuring the referer 
                    // header contains our own address;  there are situations where attackers can circumvent this  
                    // but it's an easy first line of defense before we spend any resources checking something more intensive
                    log.debug("CSRF: referer = {}", request.getHeader("Referer")); // example CSRF: referer = http://localhost:8080/mtwilson-portal/home.html
                    log.debug("CSRF: request = {}", request.getRequestURL().toString()); // example CSRF: request = http://localhost:8080/mtwilson-portal/getData/getDashBoardData.html
                    try {
                        URL refererUrl = new URL(request.getHeader("Referer"));
                        URL requestUrl = new URL(request.getRequestURL().toString());
                        boolean refererOk = requestUrl.getProtocol().equals(refererUrl.getProtocol()) 
                                && requestUrl.getHost().equals(refererUrl.getHost()) 
                                && requestUrl.getPort() == refererUrl.getPort() ;
                        if( refererOk ) {
                            log.debug("User is logged in, forwarding request to "+url);
                            chain.doFilter(request, response);
                        }
                        else {
                            log.debug("Invalid request, possible CSRF because referer does not match request URL; forwarding request to Login page.");
                            goToLogin(request,response);
                        }
                    }
                    catch(Exception e) {
                        log.debug("Invalid request, forwarding request to Login page.", e);
                        goToLogin(request,response);
                    }
				}else {
					log.debug("User is not logged in or session expired, forwarding request to Login page.");
					goToLogin(request,response);
				}
			}else {
				log.debug("User is not logged in or session expired, forwarding request to Login page.");
				goToLogin(request,response);
			}
			
			
		}else {
			log.info("User's Session expired, forwarding request to Login page.");
			redirectToLogin(request,response);
		}
				
	}

	private void goToLogin(HttpServletRequest req,HttpServletResponse res) {
		try {
			//RequestDispatcher dispatcher = req.getRequestDispatcher("/login.htm");
			//req.setAttribute("message", "Your Login Session is Expired. Please Login again.");
			//dispatcher.forward(req, res);
			//res.setHeader("Refresh", "0;"+req.getContextPath()+"/login.htm");
			//res.sendRedirect(req.getContextPath()+"/login.htm");
			res.addHeader("message", "Your Login Session is Expired. Please Login again.");
             res.addHeader("requiredLogin", "true");
             res.sendRedirect(req.getContextPath()+"/login.htm");
			//res.addHeader("Location", req.getContextPath()+"/login.htm");
			//res.addHeader("message", "Your Login Session is Expired. Please Login again.");
		} catch (IOException e) {
			log.error("IOException Exception, Error While forwarding request to Login page.");
			e.printStackTrace();
		}
		
	}
        
        private void redirectToLogin(HttpServletRequest req,HttpServletResponse res) {
        	try {
    			RequestDispatcher dispatcher = req.getRequestDispatcher("/login.htm");
    			req.setAttribute("message", "Your Login Session is Expired. Please Login again.");
    			dispatcher.forward(req, res);
    		} catch (Exception e) {
    			log.error("IOException Exception, Error While forwarding request to Login page.");
    			e.printStackTrace();
    		} 
		
	}

	@Override
	public void destroy() {
				
	}

}
