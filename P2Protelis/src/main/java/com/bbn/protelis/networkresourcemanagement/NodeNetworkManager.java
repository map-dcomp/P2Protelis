package com.bbn.protelis.networkresourcemanagement;

import java.util.HashMap;
import java.util.Map;

import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.NetworkManager;
import org.protelis.vm.util.CodePath;

/**
 * Initial version from Protelis-Demo. This will need to be enhanced to support
 * the network testbed.
 */
public class NodeNetworkManager implements NetworkManager {
	private Map<CodePath, Object> sendCache = null;
	private final Map<DeviceUID, Map<CodePath, Object>> receiveCache = new HashMap<>();

	/** External access to sending cache */
	public Map<CodePath, Object> getSendCache() {
		return sendCache;
	}

	/** External access to put messages into receive cache */
	public void receiveFromNeighbor(final DeviceUID neighbor, final Map<CodePath, Object> message) {
		receiveCache.put(neighbor, message);
	}

	/**
	 * External access to note when a device is no longer a neighbor, wiping
	 * cache
	 */
	public void removeNeighbor(final DeviceUID neighbor) {
		receiveCache.remove(neighbor);
	}

	@Override
	public Map<DeviceUID, Map<CodePath, Object>> getNeighborState() {
		return receiveCache;
	}

	@Override
	public void shareState(final Map<CodePath, Object> toSend) {
		sendCache = toSend;
	}

}
