/**
 * This class is used while creating ModelAndView object in controllers. To crate a view for JSON Data type.
 * If you want to send any data in JSON format to client, create a object of this class and pass to ModelAndView constructor while making ModelAndView Object. 
 */
package com.intel.mountwilson.util;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.View;

public class JSONView implements View {
	private boolean isKeyRequired;
	
	public JSONView() {
		this(true);
	}
	
	//Pass false if you dont want the keys to be part of json
	public JSONView(boolean isKeyRequired) {
		this.isKeyRequired = isKeyRequired;
	}
	
	//Method to set Content type for response to JSON Type.
	@Override
	public String getContentType() {
		return "application/json";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void render(Map jsonDetailsMap, HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType(getContentType());
		if(isKeyRequired){
			response.getWriter().write(new Gson().toJson(jsonDetailsMap));
		}else{
			if(jsonDetailsMap.size() == 1){
				Iterator iterator = jsonDetailsMap.values().iterator();
				if (iterator.hasNext()) {
					response.getWriter().write(new Gson().toJson(iterator.next()));
				}
			}else{
				response.getWriter().write(new Gson().toJson(jsonDetailsMap.values()));
			}
			
		}
		
	}
}
