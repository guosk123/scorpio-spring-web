package com.machloop.fpc.manager.knowledge.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.machloop.fpc.manager.knowledge.bo.GeoCityBO;
import com.machloop.fpc.manager.knowledge.bo.GeoCountryBO;
import com.machloop.fpc.manager.knowledge.bo.GeoCustomCountryBO;
import com.machloop.fpc.manager.knowledge.bo.GeoIpSettingBO;
import com.machloop.fpc.manager.knowledge.bo.GeoKnowledgeInfoBO;
import com.machloop.fpc.manager.knowledge.bo.GeoProvinceBO;

import reactor.util.function.Tuple3;

/**
 * @author guosk
 *
 * create at 2020年12月31日, fpc-manager
 */
public interface GeoService {

  GeoKnowledgeInfoBO queryGeoKnowledgeInfos();

  Tuple3<List<GeoCountryBO>, List<GeoProvinceBO>, List<GeoCityBO>> queryGeolocations();

  GeoKnowledgeInfoBO importGeoKnowledges(MultipartFile file);

  /*
   * 自定义地区
   */
  List<String> exportCustomCountrys();

  int importCustomCountrys(MultipartFile file, String operatorId);

  List<GeoCustomCountryBO> queryCustomCountrys();

  GeoCustomCountryBO queryCustomCountry(String id);

  GeoCustomCountryBO queryCustomCountryByCmsCustomCountryId(String cmsCustomCountryId);

  GeoCustomCountryBO saveCustomCountry(GeoCustomCountryBO customCountryBO, String operatorId);

  GeoCustomCountryBO updateCustomCountry(String id, GeoCustomCountryBO customCountryBO,
      String operatorId);

  GeoCustomCountryBO deleteCustomCountry(String id, String operatorId, boolean forceDelete);

  int batchDeleteCustomCountry(List<String> ids, String operatorId);

  int updateGeoIpSetting(GeoIpSettingBO geoIpSetting, String operatorId);

  Map<String, String> queryAllLocationIdNameMapping();

}
