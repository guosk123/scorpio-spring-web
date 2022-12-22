package com.machloop.fpc.cms.baseline.setting.service;

import reactor.util.function.Tuple4;

/**
 * @author guosk
 *
 * create at 2021年5月6日, fpc-manager
 */
public interface BaselineSettingSyncService {

  Tuple4<String, Integer, Integer, Integer> sync();

}
