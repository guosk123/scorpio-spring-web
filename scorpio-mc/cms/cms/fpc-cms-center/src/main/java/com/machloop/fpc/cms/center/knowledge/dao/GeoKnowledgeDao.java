package com.machloop.fpc.cms.center.knowledge.dao;

import java.util.List;
import com.machloop.fpc.cms.center.knowledge.data.GeoCityDO;
import com.machloop.fpc.cms.center.knowledge.data.GeoCountryDO;
import com.machloop.fpc.cms.center.knowledge.data.GeoKnowledgeInfoDO;
import com.machloop.fpc.cms.center.knowledge.data.GeoProvinceDO;
import reactor.util.function.Tuple3;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月18日, fpc-cms-center
 */
public interface GeoKnowledgeDao {

  GeoKnowledgeInfoDO queryGeoKnowledgeInfos(String filePath);

  Tuple3<List<GeoCountryDO>, List<GeoProvinceDO>, List<GeoCityDO>> queryGeolocations(
      String filePath);

}
