package com.intel.mountwilson.manifest;

import javax.persistence.EntityManagerFactory;

import com.intel.mtwilson.as.data.TblHosts;

public interface IManifestStrategyFactory {

	IManifestStrategy getManifestStategy(TblHosts tblHosts,EntityManagerFactory entityManagerFactory);
}
