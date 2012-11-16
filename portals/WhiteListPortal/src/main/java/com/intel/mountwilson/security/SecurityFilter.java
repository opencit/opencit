package com.intel.mountwilson.security;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
		System.err.println(httpSession +"----"+url);
		if (httpSession != null) {
			Object obj = httpSession.getAttribute("logged-in");
			if ( obj != null) {
				boolean logged =  (Boolean) obj;
				System.err.println(logged);
				if (logged) {
					log.info("User is logged in, forwarding request to "+url);
					chain.doFilter(request, response);
				}else {
					log.info("User is not logged in or session expired, forwarding request to Login page.");
					goToLogin(request,response);
				}
			}else {
				log.info("User is not logged in or session expired, forwarding request to Login page.");
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
			req.setAttribute("message", "Your Login Session is Expired. Please Login again.");
			//dispatcher.forward(req, res);
			//res.setHeader("Refresh", "0;"+req.getContextPath()+"/login.htm");
             /*res.setHeader("requiredLogin", "true");
             res.setHeader("locationUrl", req.getContextPath()+"/login.htm");*/
             res.sendRedirect(req.getContextPath()+"/login.htm");
             
			//res.addHeader("message", "Your Login Session is Expired. Please Login again.");
		} catch (IOException e) {
			log.error("IOException Exception, Error While forwarding request to Login page.");
			e.printStackTrace();
		}//catch (ServletException e) {
//			logger.log(Level.SEVERE,"ServletException Exception, Error While forwarding request to Login page.");
//			e.printStackTrace();
//		}
		
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
