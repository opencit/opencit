package com.intel.mtwilson.as.business.trust.gkv;

import com.intel.mtwilson.as.data.TblHosts;

public interface IGKVStrategyFactory {
	
	IGKVStrategy getGkStrategy(TblHosts host);

}
