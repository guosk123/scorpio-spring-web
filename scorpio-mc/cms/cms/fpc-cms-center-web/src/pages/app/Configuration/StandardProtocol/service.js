/**
 * ===========
 *  标准协议端口配置
 * ===========
 */
import { stringify } from 'qs';
import ajax from '@/utils/frame/ajax';
import application from '@/common/applicationConfig';

const { API_VERSION_PRODUCT_V1 } = application
export async function queryStandardProtocols(params) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/analysis/standard-protocols${params && `?${stringify(params)}`}`,
  );
}

export async function queryAllStandardProtocols() {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/standard-protocols/as-list`);
}

export async function queryStandardProtocolDetail({ id }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/standard-protocols/${id}`);
}

export async function createStandardProtocol(params) {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/standard-protocols`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

export async function updateStandardProtocol(params) {
  const { id, ...restParams } = params;
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/standard-protocols/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}

export async function deleteStandardProtocol({ id }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/standard-protocols/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}
