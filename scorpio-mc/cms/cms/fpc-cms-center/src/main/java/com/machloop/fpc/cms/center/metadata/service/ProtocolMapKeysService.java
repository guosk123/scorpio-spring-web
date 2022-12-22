package com.machloop.fpc.cms.center.metadata.service;

import java.util.Map;
import java.util.Set;

public interface ProtocolMapKeysService {


  Map<String, Set<String>> queryProtocolMapKeys(String protocol);
}
