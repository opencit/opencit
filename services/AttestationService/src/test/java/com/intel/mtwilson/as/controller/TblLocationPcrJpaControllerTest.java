package com.intel.mtwilson.as.controller;

import static org.junit.Assert.*;

import org.junit.Test;

import com.intel.mtwilson.as.helper.BaseBO;

public class TblLocationPcrJpaControllerTest {

	@Test
	public void test() {
		BaseBO config = new BaseBO();
		String location = new TblLocationPcrJpaController(config.getEntityManagerFactory()).findTblLocationPcrByPcrValue("Hello");
		System.out.println("Location is " + location);
	}

}
