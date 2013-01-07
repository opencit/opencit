package com.intel.mountwilson.manifest;

import javax.persistence.EntityManagerFactory;

import com.intel.mtwilson.as.data.TblHosts;

/**
 * XXX TODO this interface must change, it doesn't make sense to make an application
 * interface that depends on a specific database-layer implementation
 * (in this case JPA). In both implementations of this factory the TblHosts parameter
 * is ignored because it is passed again later to the IManifestStrategy object
 * anyway.
 * XXX for now the HostAgentFactory will take on this role of returning IManifestStrategy
 * objects based on the host record, and later we can get rid of this interface
 * completely.
 */
public interface IManifestStrategyFactory {

	IManifestStrategy getManifestStategy(TblHosts tblHosts,EntityManagerFactory entityManagerFactory);
}
