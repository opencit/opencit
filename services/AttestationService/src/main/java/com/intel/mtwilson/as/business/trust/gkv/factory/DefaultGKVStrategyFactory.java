package com.intel.mtwilson.as.business.trust.gkv.factory;

import com.intel.mtwilson.as.business.trust.gkv.IGKVStrategy;
import com.intel.mtwilson.as.business.trust.gkv.IGKVStrategyFactory;
import com.intel.mtwilson.as.business.trust.gkv.strategy.PcrGKVStrategy;
import com.intel.mtwilson.as.data.TblHosts;

public class DefaultGKVStrategyFactory implements IGKVStrategyFactory {
	@Override
	public IGKVStrategy getGkStrategy(TblHosts host) {
		return new PcrGKVStrategy();
	}
}
