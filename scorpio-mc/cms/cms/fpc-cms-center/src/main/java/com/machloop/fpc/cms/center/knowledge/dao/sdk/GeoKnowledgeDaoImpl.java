package com.machloop.fpc.cms.center.knowledge.dao.sdk;

import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.fpc.cms.center.helper.GeoParseLibraryHelper;
import com.machloop.fpc.cms.center.knowledge.dao.GeoKnowledgeDao;
import com.machloop.fpc.cms.center.knowledge.data.GeoCityDO;
import com.machloop.fpc.cms.center.knowledge.data.GeoCountryDO;
import com.machloop.fpc.cms.center.knowledge.data.GeoKnowledgeInfoDO;
import com.machloop.fpc.cms.center.knowledge.data.GeoProvinceDO;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月18日, fpc-cms-center
 */
@Repository
public class GeoKnowledgeDaoImpl implements GeoKnowledgeDao{

  private static final Logger LOGGER = LoggerFactory.getLogger(GeoKnowledgeDaoImpl.class);

  private Tuple3<List<GeoCountryDO>, List<GeoProvinceDO>, List<GeoCityDO>> geolocationsCache;

  private long lastLoadTime;

  @Autowired
  private GeoParseLibraryHelper geoParseLibraryHelper;
  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoKnowledgeDao#queryGeoKnowledgeInfos(java.lang.String)
   */
  @Override
  public GeoKnowledgeInfoDO queryGeoKnowledgeInfos(String filePath) {
    GeoKnowledgeInfoDO geoKnowledgeInfoDO = new GeoKnowledgeInfoDO();
    Map<String, Object> knowledgeInfos = geoParseLibraryHelper.queryGeoKnowledgeInfos(filePath);
    geoKnowledgeInfoDO.setReleaseDate((Date) knowledgeInfos.get("releaseDate"));
    geoKnowledgeInfoDO.setVersion((String) knowledgeInfos.get("version"));
    geoKnowledgeInfoDO.setImportDate((Date) knowledgeInfos.get("importDate"));

    return geoKnowledgeInfoDO;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoKnowledgeDao#queryGeolocations(java.lang.String)
   */
  @Override
  public Tuple3<List<GeoCountryDO>, List<GeoProvinceDO>, List<GeoCityDO>> queryGeolocations(
      String filePath) {
 // 检查当前文件时间与上次加载文件时间是否一致, 不一致则更新Geo cache
    long currentLoadTime = Paths.get(filePath).toFile().lastModified();
    if (geolocationsCache == null || this.lastLoadTime != currentLoadTime) {
      LOGGER.debug("reload geo knowledge file.");
      this.geolocationsCache = parseGeoKnowledges(filePath);
      this.lastLoadTime = currentLoadTime;
    }
    return geolocationsCache;
  }

  private Tuple3<List<GeoCountryDO>, List<GeoProvinceDO>, List<GeoCityDO>> parseGeoKnowledges(
      String filePath) {

    List<GeoCountryDO> countryList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<GeoProvinceDO> provinceList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<GeoCityDO> cityList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    Tuple3<List<GeoCountryDO>, List<GeoProvinceDO>,
        List<GeoCityDO>> result = Tuples.of(countryList, provinceList, cityList);

    Tuple3<List<Map<String, String>>, List<Map<String, String>>, List<
        Map<String, String>>> geoKnowledges = geoParseLibraryHelper.parseGeoKnowledges(filePath);

    geoKnowledges.getT1().forEach(country -> {
      GeoCountryDO geoCountryDO = new GeoCountryDO();
      geoCountryDO.setCountryId(country.get("countryId"));
      geoCountryDO.setName(country.get("name"));
      geoCountryDO.setNameText(country.get("nameText"));
      geoCountryDO.setDescription(country.get("description"));
      geoCountryDO.setDescriptionText(country.get("descriptionText"));
      geoCountryDO.setCountryCode(country.get("countryCode"));
      geoCountryDO.setLongitude(country.get("longitude"));
      geoCountryDO.setLatitude(country.get("latitude"));
      countryList.add(geoCountryDO);
    });
    geoKnowledges.getT2().forEach(province -> {
      GeoProvinceDO geoProvinceDO = new GeoProvinceDO();
      geoProvinceDO.setProvinceId(province.get("provinceId"));
      geoProvinceDO.setCountryId(province.get("countryId"));
      geoProvinceDO.setName(province.get("name"));
      geoProvinceDO.setNameText(province.get("nameText"));
      geoProvinceDO.setDescription(province.get("description"));
      geoProvinceDO.setDescriptionText(province.get("descriptionText"));
      geoProvinceDO.setLongitude(province.get("longitude"));
      geoProvinceDO.setLatitude(province.get("latitude"));
      provinceList.add(geoProvinceDO);
    });
    geoKnowledges.getT3().forEach(city -> {
      GeoCityDO geoCityDO = new GeoCityDO();
      geoCityDO.setCityId(city.get("cityId"));
      geoCityDO.setProvinceId(city.get("provinceId"));
      geoCityDO.setCountryId(city.get("countryId"));
      geoCityDO.setName(city.get("name"));
      geoCityDO.setNameText(city.get("nameText"));
      geoCityDO.setDescription(city.get("description"));
      geoCityDO.setDescriptionText(city.get("descriptionText"));
      geoCityDO.setLongitude(city.get("longitude"));
      geoCityDO.setLatitude(city.get("latitude"));
      cityList.add(geoCityDO);
    });

    return result;
  }
}
