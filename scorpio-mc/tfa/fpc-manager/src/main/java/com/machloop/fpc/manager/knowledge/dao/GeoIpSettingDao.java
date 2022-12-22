package com.machloop.fpc.manager.knowledge.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.manager.knowledge.data.GeoIpSettingDO;

/**
 * @author guosk
 *
 * create at 2021年8月25日, fpc-manager
 */
public interface GeoIpSettingDao {

  List<GeoIpSettingDO> queryGeoIpSettings();

  List<GeoIpSettingDO> queryGeoCountryIpSettingIds(boolean onlyLocal);
  
  List<String> queryGeoCountryIpSettingIds(Date beforeTime);

  List<GeoIpSettingDO> queryGeoCountryIpSettings();

  GeoIpSettingDO queryGeoIpSetting(String countryId, String provinceId, String cityId);

  GeoIpSettingDO queryGeoIpSettingByCountryId(String countryId);

  int saveOrUpdateGeoIpSetting(GeoIpSettingDO geoIpSetting);

  int saveGeoIpSetting(GeoIpSettingDO geoIpSetting);

  int updateGeoIpSetting(GeoIpSettingDO geoIpSetting);

  int batchSaveGeoIpSettings(List<GeoIpSettingDO> geoIpSettings);

  int deleteGeoIpSetting(String countryId, String provinceId, String cityId, String operatorId);

  int deleteByCountryIds(List<String> countryIds, String operatorId);

}
