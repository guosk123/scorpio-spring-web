package com.machloop.fpc.cms.center.knowledge.dao;

import java.util.Date;
import java.util.List;
import com.machloop.fpc.cms.center.knowledge.data.GeoCustomCountryDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月18日, fpc-cms-center
 */
public interface GeoCustomCountryDao {

  List<GeoCustomCountryDO> queryGeoCustomCountrys(String deleted);

  List<GeoCustomCountryDO> queryGeoCustomCountryIds(boolean onlyLocal);

  GeoCustomCountryDO queryGeoCustomCountry(String id);

  GeoCustomCountryDO queryGeoCustomCountryByName(String name);

  GeoCustomCountryDO queryGeoCustomCountryByCountryId(String countryId);

  GeoCustomCountryDO queryGeoCustomCountryByAssignId(String assignId);
  
  List<GeoCustomCountryDO> queryAssignGeoCustomCountryIds(Date beforetime);
  
  int countGeoCustomCountrys();

  GeoCustomCountryDO saveGeoCustomCountry(GeoCustomCountryDO countryDO);

  List<GeoCustomCountryDO> batchSaveGeoCustomCountrys(List<GeoCustomCountryDO> countrys);

  int updateGeoCustomCountry(GeoCustomCountryDO countryDO);

  int deleteGeoCustomCountry(String id, String operatorId);

}
