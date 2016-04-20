package com.intel.mtwilson.as.controller;


import com.intel.mtwilson.My;
import java.io.IOException;
import org.junit.Test;


public class TblLocationPcrJpaControllerTest {

	@Test
	public void test() throws IOException {
		String location = My.jpa().mwLocationPcr().findTblLocationPcrByPcrValue("Hello");
		System.out.println("Location is " + location);
	}

}
