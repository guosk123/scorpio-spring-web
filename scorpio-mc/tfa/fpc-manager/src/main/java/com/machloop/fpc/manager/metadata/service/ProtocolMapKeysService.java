package com.machloop.fpc.manager.metadata.service;

import java.util.Map;
import java.util.Set;

public interface ProtocolMapKeysService {


  Map<String, Set<String>> queryProtocolMapKeys(String protocol);
}
