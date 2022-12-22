/**
 * ===========
 *  TLS协议密钥配置
 * ===========
 */
import ajax from '@/utils/frame/ajax';
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';

export async function queryAllTlsDecryptSettings() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/decrypt-settings`);
}

export async function queryTlsDecryptSettingsDetail({ id }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/decrypt-settings/${id}`);
}

export async function createTlsDecryptSetting({ formData }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/decrypt-settings`, {
    type: 'POST',
    processData: false, // 告诉jquery不要处理发送的数据
    contentType: false, // 告诉jquery不要设置content-Type请求头
    data: formData,
  });
}

export async function updateTlsDecryptSetting({ id, formData }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/decrypt-settings/${id}`, {
    type: 'POST',
    processData: false, // 告诉jquery不要处理发送的数据
    contentType: false, // 告诉jquery不要设置content-Type请求头
    data: formData,
  });
}

export async function deleteTlsDecryptSetting({ id }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/decrypt-settings/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}
