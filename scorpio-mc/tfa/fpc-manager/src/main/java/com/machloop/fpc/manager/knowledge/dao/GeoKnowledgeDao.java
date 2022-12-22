package com.machloop.fpc.manager.knowledge.dao;

import java.util.List;

import com.machloop.fpc.manager.knowledge.data.GeoCityDO;
import com.machloop.fpc.manager.knowledge.data.GeoCountryDO;
import com.machloop.fpc.manager.knowledge.data.GeoKnowledgeInfoDO;
import com.machloop.fpc.manager.knowledge.data.GeoProvinceDO;

import reactor.util.function.Tuple3;

/**
 * @author guosk
 *
 * create at 2020年12月31日, fpc-manager
 */
public interface GeoKnowledgeDao {

  GeoKnowledgeInfoDO queryGeoKnowledgeInfos(String filePath);

  Tuple3<List<GeoCountryDO>, List<GeoProvinceDO>, List<GeoCityDO>> queryGeolocations(
      String filePath);

}
