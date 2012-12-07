package com.intel.mountwilson.manifest.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import javax.persistence.EntityManagerFactory;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mountwilson.manifest.IManifestStrategy;
import com.intel.mountwilson.manifest.IManifestStrategyFactory;
import com.intel.mountwilson.manifest.strategy.VMWareManifestStrategy;
import com.intel.mountwilson.util.vmware.VMwareClient;
import com.intel.mtwilson.datatypes.ErrorCode;


/**
 * XXX the interface needs to change, see comments on IManifestStrategyFactory
 */
public class VMWareManifestStategyFactory extends VMwareClient implements IManifestStrategyFactory {
	Logger log = Logger.getLogger(getClass().getName());

	@Override
	public IManifestStrategy getManifestStategy(TblHosts tblHosts, EntityManagerFactory entityManagerFactory) {
            return new VMWareManifestStrategy(entityManagerFactory);
	}


}
