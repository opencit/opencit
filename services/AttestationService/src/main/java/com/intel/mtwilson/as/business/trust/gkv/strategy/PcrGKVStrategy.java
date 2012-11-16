package com.intel.mtwilson.as.business.trust.gkv.strategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.intel.mtwilson.as.business.trust.gkv.IGKVStrategy;
import com.intel.mtwilson.as.controller.TblMleJpaController;
import com.intel.mtwilson.as.controller.TblModuleManifestJpaController;
import com.intel.mtwilson.as.controller.TblPcrManifestJpaController;
import com.intel.mtwilson.as.data.TblHostSpecificManifest;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mtwilson.as.data.TblModuleManifest;
import com.intel.mtwilson.as.data.TblPcrManifest;
import com.intel.mtwilson.as.helper.BaseBO;
import com.intel.mountwilson.manifest.data.IManifest;
import com.intel.mountwilson.manifest.data.ModuleManifest;
import com.intel.mountwilson.manifest.data.PcrManifest;
import com.intel.mountwilson.manifest.data.PcrModuleManifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PcrGKVStrategy extends BaseBO implements IGKVStrategy {
        private Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public HashMap<String, ? extends IManifest> getBiosGoodKnownManifest(
			String mleName, String mleVersion, String oemName) {
		// Call query method to avoid the objects from the cache
		TblMle biosMle = new TblMleJpaController(getEntityManagerFactory())
				.findBiosMle(mleName, mleVersion, oemName);
		HashMap<String, ? extends IManifest> pcrManifestMap = getPcrManifestMap(biosMle, false);
		return pcrManifestMap;
	}

	@Override
	public HashMap<String, ? extends IManifest> getVmmGoodKnownManifest(
			String mleName, String mleVersion, String osName, String osVersion, Integer hostId) {
		HashMap<String, ? extends IManifest> pcrManifestMap;
		// Call query method to avoid the objects from the cache
		TblMle vmmMle = new TblMleJpaController(getEntityManagerFactory())
				.findVmmMle(mleName, mleVersion, osName, osVersion);

		if (vmmMle.getName().contains("ESX")
				&& vmmMle.getVersion().contains("5.1")) {
			pcrManifestMap = getPcrManifestMap(vmmMle, true);
			addModuleManifest(vmmMle, pcrManifestMap,hostId);
		} else {
			pcrManifestMap = getPcrManifestMap(vmmMle, false);
		}

		return pcrManifestMap;
	}

	private void addModuleManifest(TblMle vmmMle,
			HashMap<String, ? extends IManifest> pcrManifestMap, Integer hostId) {

		// For now only VMM mle can have module manifest

		List<TblModuleManifest> moduleManifests = new TblModuleManifestJpaController(
				getEntityManagerFactory()).findByMleId(vmmMle.getId());

		for (TblModuleManifest manifest : moduleManifests) {
			/*
			 * Not considering event type id 2 - Vim25Api.HostTpmOptionEventDetails as it needs to be 
			 * host specific in next release.
			 * */
			
			log.info("Skipping Vim25Api.HostTpmOptionEventDetails in GKV.");
			if (pcrManifestMap.containsKey(manifest.getExtendedToPCR()) && manifest.getEventID().getId() != 2) {

				PcrModuleManifest pcrModuleManifest = (PcrModuleManifest) pcrManifestMap
						.get(manifest.getExtendedToPCR());

				ModuleManifest moduleManifest = new ModuleManifest();
				moduleManifest.setEventName(manifest.getEventID().getName());
				moduleManifest.setComponentName(manifest.getComponentName());
				if (manifest.getUseHostSpecificDigestValue()) {
					moduleManifest.setDigestValue(getHostDigestValue(hostId, manifest
							.getTblHostSpecificManifestCollection()));
				} else {
					moduleManifest.setDigestValue(manifest.getDigestValue());

				}
				moduleManifest.setPackageName(manifest.getPackageName());
				moduleManifest.setPackageVendor(manifest.getPackageVendor());
				moduleManifest.setPackageVersion(manifest.getPackageVersion());

				pcrModuleManifest.getModuleManifests().put(
						moduleManifest.getMFKey(), moduleManifest);
			}
		}

	}

	private String getHostDigestValue(
			Integer hostId, Collection<TblHostSpecificManifest> tblHostSpecificManifestCollection) {
		for (TblHostSpecificManifest hostSpecificManifest : tblHostSpecificManifestCollection) {
			if(hostSpecificManifest.getHostID() == hostId)
				return hostSpecificManifest.getDigestValue();
		}

		return null;
	}

	private HashMap<String, ? extends IManifest> getPcrManifestMap(TblMle mle,
			boolean moduleManifest) {
		HashMap<String, IManifest> pcrManifests = new HashMap<String, IManifest>();

		for (TblPcrManifest pcrMf : mle.getTblPcrManifestCollection()) {
			// Call query method to avoid the objects from the cache
			pcrMf = new TblPcrManifestJpaController(getEntityManagerFactory())
					.findPcrManifestById(pcrMf.getId());

			 if(moduleManifest){
			pcrManifests.put(pcrMf.getName().trim(), new PcrModuleManifest(
					Integer.valueOf(pcrMf.getName()), pcrMf.getValue().trim()));
			 }else{
			 pcrManifests.put(pcrMf.getName().trim(), new
			 PcrManifest(Integer.valueOf(pcrMf.getName()),
			 pcrMf.getValue().trim()));
			
			 }

			log.info( "{} - {}", new Object[] { pcrMf.getName(),
					pcrMf.getValue() });
		}

		return pcrManifests;
	}

}
