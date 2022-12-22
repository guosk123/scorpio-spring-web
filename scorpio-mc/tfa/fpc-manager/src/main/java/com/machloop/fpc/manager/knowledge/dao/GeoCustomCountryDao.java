package com.machloop.fpc.manager.knowledge.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.manager.knowledge.data.GeoCustomCountryDO;

/**
 * @author guosk
 *
 * create at 2021年8月25日, fpc-manager
 */
public interface GeoCustomCountryDao {

  List<GeoCustomCountryDO> queryGeoCustomCountrys(String deleted);

  List<String> queryAssignGeoCustomCountryIds(Date beforeTime);

  List<String> queryGeoCustomCountryIds(boolean onlyLocal);

  GeoCustomCountryDO queryGeoCustomCountry(String id);

  GeoCustomCountryDO queryGeoCustomCountryByName(String name);

  GeoCustomCountryDO queryGeoCustomCountryByCountryId(String countryId);

  GeoCustomCountryDO queryGeoCustomCountryByCmsCustomCountryId(String cmsCustomCountryId);

  int countGeoCustomCountrys();

  GeoCustomCountryDO saveOrRecoverGeoCustomCountry(GeoCustomCountryDO countryDO);

  int batchSaveGeoCustomCountrys(List<GeoCustomCountryDO> countrys);

  int updateGeoCustomCountry(GeoCustomCountryDO countryDO);

  int deleteGeoCustomCountry(List<String> ids, String operatorId);

}
