import ajax from '@/utils/frame/ajax';
import application from '@/common/applicationConfig';

const { API_VERSION_PRODUCT_V1 } = application
export interface ICreateCustomCountry {
  name: string,
  longitude: string,
  latitude: string,
  ipAddress: string,
  description: string,
}

export interface IUpdateIpAddress {
  countryId: string,
  provinceId: string,
  cityId: string,
  ipAddress: string,
}

export interface IUpdateCustomCountry {
  id: string,
  name: string,
  longitude: string,
  latitude: string,
  ipAddress: string,
  description: string,
}

/**
 * 获取地理位置规则库
 */
export async function queryGeolocationKnowledge() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/geolocation/knowledge-infos`);
}

/**
 * 获取地理位置
 */
export async function queryGeolocations() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/geolocation/rules`);
}

/**
 * 根据id查询单条自定义地理位置GET 
 */
export async function queryCustomGeoById(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/geolocation/custom-countrys/${id}`)
}

export async function uploadGeolocation() { }


/**
 * 保存自定义地区
 */
export async function createCustomCountry(params: ICreateCustomCountry) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/geolocation/custom-countrys`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

/**
 * 修改自定义地区 
 */
export async function updateCustomCountry(params: IUpdateCustomCountry) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/geolocation/custom-countrys/${params.id}`, {
    type: 'PUT',
    data: {
      ...params,
    },
  });
}



/**
 * 删除自定义地区
 */
export async function deleteCustomCountry(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/geolocation/custom-countrys/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

/**
 * 编辑ip地址 自定义和系统内置都可以修改
 */
export async function updateIpAddress(params: IUpdateIpAddress) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/geolocation/ip-settings`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params
    },
  });
}

/**
 * 导入自定义地区
 */
export async function importCustomGeo(formData: { file: any }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/geolocation/as-import`, {
    type: 'POST',
    processData: false, //  告诉jquery不要处理发送的数据
    contentType: false, // 告诉jquery不要设置content-Type请求头
    data: formData,
  });
}
