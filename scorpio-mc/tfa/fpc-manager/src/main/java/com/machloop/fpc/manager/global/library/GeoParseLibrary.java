package com.machloop.fpc.manager.global.library;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/**
 * @author guosk
 *
 * create at 2020年12月31日, fpc-manager
 */
public interface GeoParseLibrary extends Library {

  static final int LIBPARSE_NAME_LEN = 64;
  static final int LIBPARSE_DESCRIPTION_LEN = 64;
  static final int GEOIP_CC_LEN = 8;

  GeoParseLibrary INSTANCE = Native.load("geoip", GeoParseLibrary.class);

  // GEOIP初始化,前端传入NULL即可
  // int geoip_init(void *compile_call_back);
  int geoip_init(Pointer compile_call_back);

  // GEOIP去始化
  // void geoip_parse_deinit();
  void geoip_parse_deinit();

  // 解析GEOIP规则文件
  // int geoip_parse_file(char *file_path);
  int geoip_parse_file(String file_path);

  // 获取版本号
  // int geoip_parse_get_version(unsigned char **version);
  int geoip_parse_get_version(PointerByReference version);

  // 获取时间戳
  // int geoip_parse_get_time(unsigned int *timestamp);
  int geoip_parse_get_time(IntByReference timestamp);

  // 获取国家字典
  // int geoip_parse_get_country_dict_all(GEOIP_COUNTRY_DICT **country_dict, int32_t *country_num);
  int geoip_parse_get_country_dict_all(PointerByReference country_dict, IntByReference country_num);

  // 获取省份字典
  // int geoip_parse_get_province_dict_all(GEOIP_PROVINCE_DICT **province_dict, int32_t
  // *province_num);
  int geoip_parse_get_province_dict_all(PointerByReference province_dict,
      IntByReference province_num);

  // 获取城市字典
  // int geoip_parse_get_city_dict_all(GEOIP_CITY_DICT **city_dict, int32_t *city_num);
  int geoip_parse_get_city_dict_all(PointerByReference city_dict, IntByReference city_num);

  // 根据省ID获取国家地区ID
  // int geoip_get_country_id_by_province_id(int16_t province_id, int16_t *country_id);
  int geoip_get_country_id_by_province_id(ShortByReference province_id,
      ShortByReference country_id);

  // 根据IP地址获取三ID
  // int geoip_get_all_id_by_addr(uint32 ipaddr, int16_t *country_id, int16_t *province_id, int16_t
  // *city_id);
  int geoip_get_all_id_by_addr(LongByReference ipaddr, ShortByReference country_id,
      ShortByReference province_id, ShortByReference city_id);

  // 释放获取的结果集
  // void geoip_free_result(void **result);
  void geoip_free_result(PointerByReference result);

  public class GeoCountryStructure extends Structure {

    public static class ByReference extends GeoCountryStructure implements Structure.ByReference {
    }

    public static class ByValue extends GeoCountryStructure implements Structure.ByValue {
    }

    public int country_id;
    public byte[] name_cn;
    public byte[] name_en;
    public byte[] desc_cn;
    public byte[] desc_en;
    public byte[] country_code;
    public float longtitude;
    public float latitude;

    public GeoCountryStructure() {
      super(Structure.ALIGN_GNUC);
      this.name_cn = new byte[LIBPARSE_NAME_LEN];
      this.name_en = new byte[LIBPARSE_NAME_LEN];
      this.desc_cn = new byte[LIBPARSE_DESCRIPTION_LEN];
      this.desc_en = new byte[LIBPARSE_DESCRIPTION_LEN];
      this.country_code = new byte[GEOIP_CC_LEN];
    }

    @Override
    public String toString() {
      return "GeoCountryStructure [country_id=" + country_id + ", name_cn="
          + Arrays.toString(name_cn) + ", name_en=" + Arrays.toString(name_en) + ", desc_cn="
          + Arrays.toString(desc_cn) + ", desc_en=" + Arrays.toString(desc_en) + ", country_code="
          + Arrays.toString(country_code) + ", longtitude=" + longtitude + ", latitude=" + latitude
          + "]";
    }

    @Override
    protected List<String> getFieldOrder() {
      return Arrays.asList("country_id", "name_cn", "name_en", "desc_cn", "desc_en", "country_code",
          "longtitude", "latitude");
    }
  }

  public class GeoProvinceStructure extends Structure {

    public int province_id;
    public int country_id;
    public byte[] name_cn;
    public byte[] name_en;
    public byte[] desc_cn;
    public byte[] desc_en;
    public float longtitude;
    public float latitude;

    public GeoProvinceStructure() {
      super(Structure.ALIGN_GNUC);
      this.name_cn = new byte[LIBPARSE_NAME_LEN];
      this.name_en = new byte[LIBPARSE_NAME_LEN];
      this.desc_cn = new byte[LIBPARSE_DESCRIPTION_LEN];
      this.desc_en = new byte[LIBPARSE_DESCRIPTION_LEN];
    }

    @Override
    public String toString() {
      return "GeoProvinceStructure [province_id=" + province_id + ", country_id=" + country_id
          + ", name_cn=" + Arrays.toString(name_cn) + ", name_en=" + Arrays.toString(name_en)
          + ", desc_cn=" + Arrays.toString(desc_cn) + ", desc_en=" + Arrays.toString(desc_en)
          + ", longtitude=" + longtitude + ", latitude=" + latitude + "]";
    }

    @Override
    protected List<String> getFieldOrder() {
      return Arrays.asList("province_id", "country_id", "name_cn", "name_en", "desc_cn", "desc_en",
          "longtitude", "latitude");
    }
  }

  public class GeoCityStructure extends Structure {

    public int city_id;
    public int province_id;
    public int country_id;
    public byte[] name_cn;
    public byte[] name_en;
    public byte[] desc_cn;
    public byte[] desc_en;
    public float longtitude;
    public float latitude;

    public GeoCityStructure() {
      super(Structure.ALIGN_GNUC);
      this.name_cn = new byte[LIBPARSE_NAME_LEN];
      this.name_en = new byte[LIBPARSE_NAME_LEN];
      this.desc_cn = new byte[LIBPARSE_DESCRIPTION_LEN];
      this.desc_en = new byte[LIBPARSE_DESCRIPTION_LEN];
    }

    @Override
    public String toString() {
      return "GeoCityStructure [city_id=" + city_id + ", province_id=" + province_id
          + ", country_id=" + country_id + ", name_cn=" + Arrays.toString(name_cn) + ", name_en="
          + Arrays.toString(name_en) + ", desc_cn=" + Arrays.toString(desc_cn) + ", desc_en="
          + Arrays.toString(desc_en) + ", longtitude=" + longtitude + ", latitude=" + latitude
          + "]";
    }

    @Override
    protected List<String> getFieldOrder() {
      return Arrays.asList("city_id", "province_id", "country_id", "name_cn", "name_en", "desc_cn",
          "desc_en", "longtitude", "latitude");
    }
  }

}
