/**
 * 地理库文件
 */
export interface IGeolocationKnowledge {
  /**
   * 版本
   */
  version: string;
  /**
   * 上传时间
   */
  uploadDate: string;
  /**
   *发布时间
   */
  releaseDate: string;
}

interface IGeolocationBase {
  /**
   * ID
   * @description 国家ID_省份ID_城市ID
   */
  id: string;
  /**
   * 名称英文
   */
  name: string;
  /**
   * 完整的名字
   * - 省份：国家-省份
   * - 城市：国家-省份-城市
   */
  fullName: string;
  /**
   * 名称中文
   */
  nameText: string;
  /** 维度 */
  latitude: string;
  /** 经度 */
  longitude: string;
  /**
   * 描述英文
   */
  description?: string;
  /**
   * 描述中文
   */
  descriptionText?: string;
  /**
   * ip地址
   */
  ipAddress?: string;
  // 添加 Tree 组件所需的数据格式
  // TODO: 以后可以删除掉
  // 下个版本就不用自定义转换 Tree 的数据格式了。
  // @see: https://github.com/react-component/select/pull/641
  // ----------
  /**
   * 同中文名称 nameText
   */
  title: string;
  /**
   * 同 ID
   * @description 国家ID_省份ID_城市ID
   */
  value: string;
}

/**
 * Geo: 国家
 */
export interface ICountry extends IGeolocationBase {
  countryId: string;
  /** 国家地区 ISO 二位字母代码 */
  countryCode?: string;
  children?: IProvince[];
}

export type ICountryMap = Record<string, ICountry>;

/**
 * Geo: 省份
 */
export interface IProvince extends IGeolocationBase {
  countryId: string;
  provinceId: string;
  children?: ICity[];
}

export type IProvinceMap = Record<string, IProvince>;

/**
 * Geo: 城市
 */
export interface ICity extends IGeolocationBase {
  countryId: string;
  provinceId: string;
  cityId: string;
  children?: any[];
}

export type ICityMap = Record<string, ICity>;

/**
 * Geo: 自定义地区
 */
export interface ICustomCountry extends IGeolocationBase {
  countryId: string;
  children?: any[];
  countryCode?: string;
}

export type ICustomCountryMap = Record<string, ICustomCountry>;

/** 地区信息集合 */
export type IGeo = ICountry | IProvince | ICity | ICustomCountry;
