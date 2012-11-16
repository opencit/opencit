package com.intel.mountwilson.manifest.factory;

import javax.persistence.EntityManagerFactory;

import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mountwilson.manifest.IManifestStrategy;
import com.intel.mountwilson.manifest.IManifestStrategyFactory;
import com.intel.mountwilson.manifest.strategy.TrustAgentStrategy;

public class DefaultManifestStrategyFactory implements IManifestStrategyFactory {


	@Override
	public IManifestStrategy getManifestStategy(TblHosts tblHosts, EntityManagerFactory entityManagerFactory) {
               return new TrustAgentStrategy(entityManagerFactory);
	}

}
