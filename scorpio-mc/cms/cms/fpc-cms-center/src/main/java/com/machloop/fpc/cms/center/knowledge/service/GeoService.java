package com.machloop.fpc.cms.center.knowledge.service;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import com.machloop.fpc.cms.center.knowledge.bo.GeoCityBO;
import com.machloop.fpc.cms.center.knowledge.bo.GeoCountryBO;
import com.machloop.fpc.cms.center.knowledge.bo.GeoCustomCountryBO;
import com.machloop.fpc.cms.center.knowledge.bo.GeoIpSettingBO;
import com.machloop.fpc.cms.center.knowledge.bo.GeoKnowledgeInfoBO;
import com.machloop.fpc.cms.center.knowledge.bo.GeoProvinceBO;
import reactor.util.function.Tuple3;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月18日, fpc-cms-center
 */
public interface GeoService {

  GeoKnowledgeInfoBO queryGeoKnowledgeInfos();

  Tuple3<List<GeoCountryBO>, List<GeoProvinceBO>, List<GeoCityBO>> queryGeolocations();

  GeoKnowledgeInfoBO importGeoKnowledges(MultipartFile file);

  GeoKnowledgeInfoBO importGeoKnowledges(FileInputStream inputStream);

  /*
   * 自定义地区
   */
  List<String> exportCustomCountrys();

  int importCustomCountrys(MultipartFile file, String operatorId);

  List<GeoCustomCountryBO> queryCustomCountrys();

  GeoCustomCountryBO queryCustomCountry(String id);

  GeoCustomCountryBO saveCustomCountry(GeoCustomCountryBO customCountryBO, String operatorId);

  GeoCustomCountryBO updateCustomCountry(String id, GeoCustomCountryBO customCountryBO,
      String operatorId);

  GeoCustomCountryBO deleteCustomCountry(String id, String operatorId, boolean forceDelete);

  int updateGeoIpSetting(GeoIpSettingBO geoIpSetting, String operatorId);

  Map<String, String> queryAllLocationIdNameMapping();

}
