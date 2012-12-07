package com.intel.mountwilson.manifest;

import javax.persistence.EntityManagerFactory;

import com.intel.mtwilson.as.data.TblHosts;

/**
 * XXX this interface must change, it doesn't make sense to make an application
 * interface that depends on a specific database-layer implementation
 * (in this case JPA).
 */
public interface IManifestStrategyFactory {

	IManifestStrategy getManifestStategy(TblHosts tblHosts,EntityManagerFactory entityManagerFactory);
}
