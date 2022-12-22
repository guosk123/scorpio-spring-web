package com.machloop.fpc.cms.center.knowledge.dao;

import java.util.Date;
import java.util.List;
import com.machloop.fpc.cms.center.knowledge.data.GeoIpSettingDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月18日, fpc-cms-center
 */
public interface GeoIpSettingDao {

  List<GeoIpSettingDO> queryGeoIpSettings();

  List<String> queryGeoIpSettingIds(Date beforeTime);

  List<GeoIpSettingDO> queryGeoCountryIpSettings();

  GeoIpSettingDO queryGeoIpSetting(String countryId, String provinceId, String cityId);

  GeoIpSettingDO queryGeoIpSettingByCountryId(String countryId);

  List<GeoIpSettingDO> queryAssignGeoIpSettingIds(Date beforeTime);
  
  GeoIpSettingDO saveOrUpdateGeoIpSetting(GeoIpSettingDO geoIpSetting);

  GeoIpSettingDO saveGeoIpSetting(GeoIpSettingDO geoIpSetting);

  int updateGeoIpSetting(GeoIpSettingDO geoIpSetting);

  List<GeoIpSettingDO> batchSaveGeoIpSettings(List<GeoIpSettingDO> geoIpSettings);

  int deleteGeoIpSetting(String countryId, String provinceId, String cityId, String operatorId);

}
