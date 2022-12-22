package com.machloop.fpc.cms.center.metadata.dao;

import java.util.Map;
import java.util.Set;

public interface ProtocolMapKeysDao {

    Map<String, Set<String>> queryProtocolMapKeys(String protocol);
}
