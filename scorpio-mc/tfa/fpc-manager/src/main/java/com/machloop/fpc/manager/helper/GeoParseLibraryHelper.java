package com.machloop.fpc.manager.helper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.webapp.base.AlarmHelper;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.global.library.GeoParseLibrary;
import com.machloop.fpc.manager.global.library.GeoParseLibrary.GeoCityStructure;
import com.machloop.fpc.manager.global.library.GeoParseLibrary.GeoCountryStructure;
import com.machloop.fpc.manager.global.library.GeoParseLibrary.GeoProvinceStructure;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2020年12月7日, fpc-manager
 */
@Component
public class GeoParseLibraryHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeoParseLibraryHelper.class);

  private long lastLoadFailTime;

  /**
   * 获取Geo地区库文件信息
   * @param filePath
   * @return knowledge info: 
   * {"releaseDate": "发布时间","version": "当前版本","importDate": "导入时间"}
   */
  public synchronized Map<String, Object> queryGeoKnowledgeInfos(String filePath) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    PointerByReference versionReference = new PointerByReference();
    try {
      // 初始化
      init(filePath);

      // 获取地区库生成时间
      IntByReference timestampReference = new IntByReference();
      if (GeoParseLibrary.INSTANCE.geoip_parse_get_time(timestampReference) == 0) {
        int timestamp = timestampReference.getValue();
        result.put("releaseDate", new Date(timestamp * 1000L));
        LOGGER.debug("parse from geo knowledge, timestamp:[{}]", timestamp);
      }

      // 获取版本
      if (GeoParseLibrary.INSTANCE.geoip_parse_get_version(versionReference) == 0
          && versionReference.getValue() != null) {
        String version = versionReference.getValue().getString(0L,
            StandardCharsets.UTF_8.toString());
        result.put("version", version);
        LOGGER.debug("parse from geo knowledge, version:[{}]", version);
      }

      // 导入时间 importDate
      result.put("importDate", new Date(Paths.get(filePath).toFile().lastModified()));

      return result;
    } catch (IOException e) {
      LOGGER.warn("failed to parse geo knowledge file.", e);
      loadFailAlert(lastLoadFailTime);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "GeoIP地区库文件解析失败");
    } finally {
      GeoParseLibrary.INSTANCE.geoip_free_result(versionReference);
      GeoParseLibrary.INSTANCE.geoip_parse_deinit();
    }
  }

  /**
   * 解析Geo地区库
   * @param filePath
   * @return <p>reactor.util.function.Tuple3<国家集合, 省份集合, 城市集合>  </p>
   * <p>国家：{"countryId":"国家ID","name":"名称","nameText":"中文名称","description":"描述","descriptionText":"中文描述","countryCode":"国家编码","longtitude":"经度","latitude":"纬度"}</p>
   * <p>省份：{"provinceId":"省份ID","countryId":"国家ID","name":"名称","nameText":"中文名称","description":"描述","descriptionText":"中文描述","longtitude":"经度","latitude":"纬度"}</p>
   * <p>城市：{"cityId":"城市ID","provinceId":"省份ID","countryId":"国家ID","name":"名称","nameText":"中文名称","description":"描述","descriptionText":"中文描述","longtitude":"经度","latitude":"纬度"}</p>
   */
  public synchronized Tuple3<List<Map<String, String>>, List<Map<String, String>>,
      List<Map<String, String>>> parseGeoKnowledges(String filePath) {

    List<Map<String, String>> countryList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Map<String, String>> provinceList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Map<String, String>> cityList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    Tuple3<List<Map<String, String>>, List<Map<String, String>>,
        List<Map<String, String>>> result = Tuples.of(countryList, provinceList, cityList);
    try {
      // 初始化
      init(filePath);

      // 解析country
      IntByReference countryCountReference = new IntByReference();
      PointerByReference countryReference = new PointerByReference();
      if (GeoParseLibrary.INSTANCE.geoip_parse_get_country_dict_all(countryReference,
          countryCountReference) == 0) {
        try {
          GeoCountryStructure structureResult = Structure.newInstance(GeoCountryStructure.class,
              countryReference.getValue());
          structureResult.read();
          Structure[] structures = structureResult.toArray(countryCountReference.getValue());
          for (Structure structure : structures) {
            Map<String,
                String> country = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
            GeoCountryStructure countryStructure = (GeoCountryStructure) structure;

            country.put("countryId", String.valueOf(countryStructure.country_id));
            country.put("name", convertCTypeString(countryStructure.name_en));
            country.put("nameText", convertCTypeString(countryStructure.name_cn));
            country.put("description", convertCTypeString(countryStructure.desc_en));
            country.put("descriptionText", convertCTypeString(countryStructure.desc_cn));
            country.put("countryCode", convertCTypeString(countryStructure.country_code));
            country.put("longitude", String.valueOf(countryStructure.longtitude));
            country.put("latitude", String.valueOf(countryStructure.latitude));
            countryList.add(country);
          }
        } finally {
          GeoParseLibrary.INSTANCE.geoip_free_result(countryReference);
        }
      }

      // 解析province
      PointerByReference provinceReference = new PointerByReference();
      IntByReference provinceCountReference = new IntByReference();
      if (GeoParseLibrary.INSTANCE.geoip_parse_get_province_dict_all(provinceReference,
          provinceCountReference) == 0) {
        try {
          GeoProvinceStructure structureResult = Structure.newInstance(GeoProvinceStructure.class,
              provinceReference.getValue());
          structureResult.read();
          Structure[] structures = structureResult.toArray(provinceCountReference.getValue());
          for (Structure structure : structures) {
            GeoProvinceStructure provinceStructure = (GeoProvinceStructure) structure;

            Map<String,
                String> province = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
            province.put("provinceId", String.valueOf(provinceStructure.province_id));
            province.put("countryId", String.valueOf(provinceStructure.country_id));
            province.put("name", convertCTypeString(provinceStructure.name_cn));
            province.put("nameText", convertCTypeString(provinceStructure.name_cn));
            province.put("description", convertCTypeString(provinceStructure.desc_en));
            province.put("descriptionText", convertCTypeString(provinceStructure.desc_cn));
            province.put("longitude", String.valueOf(provinceStructure.longtitude));
            province.put("latitude", String.valueOf(provinceStructure.latitude));
            provinceList.add(province);
          }
        } finally {
          GeoParseLibrary.INSTANCE.geoip_free_result(provinceReference);
        }
      }

      // 解析city
      PointerByReference cityReference = new PointerByReference();
      IntByReference cityCountReference = new IntByReference();
      if (GeoParseLibrary.INSTANCE.geoip_parse_get_city_dict_all(cityReference,
          cityCountReference) == 0) {
        try {
          GeoCityStructure structureResult = Structure.newInstance(GeoCityStructure.class,
              cityReference.getValue());
          structureResult.read();
          Structure[] structures = structureResult.toArray(cityCountReference.getValue());
          for (Structure structure : structures) {
            GeoCityStructure cityStructure = (GeoCityStructure) structure;

            Map<String, String> city = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
            city.put("cityId", String.valueOf(cityStructure.city_id));
            city.put("provinceId", String.valueOf(cityStructure.province_id));
            city.put("countryId", String.valueOf(cityStructure.country_id));
            city.put("name", convertCTypeString(cityStructure.name_en));
            city.put("nameText", convertCTypeString(cityStructure.name_cn));
            city.put("description", convertCTypeString(cityStructure.desc_en));
            city.put("descriptionText", convertCTypeString(cityStructure.desc_cn));
            city.put("longitude", String.valueOf(cityStructure.longtitude));
            city.put("latitude", String.valueOf(cityStructure.latitude));
            cityList.add(city);
          }
        } finally {
          GeoParseLibrary.INSTANCE.geoip_free_result(cityReference);
        }
      }
      return result;
    } catch (IOException e) {
      LOGGER.warn("failed to parse geo knowledge file.", e);
      loadFailAlert(lastLoadFailTime);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "GeoIP地区库文件解析失败");
    } finally {
      GeoParseLibrary.INSTANCE.geoip_parse_deinit();
    }
  }

  private void init(String filePath) throws IOException {
    if (GeoParseLibrary.INSTANCE.geoip_init(null) != 0) {
      LOGGER.warn("failed to invoke geoip_init.");
      throw new IOException("failed to init geoip.");
    }
    if (GeoParseLibrary.INSTANCE.geoip_parse_file(filePath) != 0) {
      LOGGER.warn("failed to invoke geoip_parse_file.");
      throw new IOException("failed to parse geoip file.");
    }
  }

  /**
   * @param bytes
   * @return
   */
  private String convertCTypeString(byte[] bytes) {
    if (bytes == null) {
      return "";
    }
    int length = 0;
    for (byte b : bytes) {
      // 找到\0
      if (b == 0) {
        break;
      }
      length += 1;
    }
    if (length <= 0) {
      return "";
    }
    byte[] dest = new byte[length];
    System.arraycopy(bytes, 0, dest, 0, length);
    String result = StringUtils.toEncodedString(dest, StandardCharsets.UTF_8);
    return result;
  }

  /**
   * 告警
   * @param lastLoadFailTime
   */
  private void loadFailAlert(long lastLoadFailTime) {
    long current = System.currentTimeMillis();
    // 第一次产生告警或距离上一次告警30分钟, 生成告警信息
    if (lastLoadFailTime == 0L
        || current - lastLoadFailTime > 1000L * 30 * Constants.ONE_MINUTE_SECONDS) {
      AlarmHelper.alarm(AlarmHelper.LEVEL_IMPORTANT, FpcConstants.ALARM_CATEGORY_KNOWLEDGEBASE,
          "geoip", "文件损坏，加载GeoIP地区库失败.");
    }
  }

}
