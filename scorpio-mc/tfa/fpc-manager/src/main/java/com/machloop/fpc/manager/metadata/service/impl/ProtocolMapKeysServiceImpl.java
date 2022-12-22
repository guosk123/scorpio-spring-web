package com.machloop.fpc.manager.metadata.service.impl;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.machloop.fpc.manager.metadata.dao.ProtocolMapKeysDao;
import com.machloop.fpc.manager.metadata.service.ProtocolMapKeysService;

@Service
public class ProtocolMapKeysServiceImpl implements ProtocolMapKeysService {

  @Autowired
  private ProtocolMapKeysDao protocolMapKeysDao;

  @Override
  public Map<String, Set<String>> queryProtocolMapKeys(String protocol) {
    return protocolMapKeysDao.queryProtocolMapKeys(protocol);
  }
}
