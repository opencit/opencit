/**
 * This class is used while creating ModelAndView object in controllers. 
 */
package com.intel.mountwilson.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.View;

public class BasicView implements View {
	
	public BasicView() {
	}
	
	//Method to set Content type for response to JSON Type.
	@Override
	public String getContentType() {
		return "text/plain";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws IOException {
		// For the basic implementation, the response only contains the status of the method call. True or False.
		response.setContentType(getContentType());
                if (model.get("result") == null) {
                    throw new IllegalArgumentException("BasicView result cannot be null.");
                }
                response.getWriter().write(model.get("result").toString());
	}
}
