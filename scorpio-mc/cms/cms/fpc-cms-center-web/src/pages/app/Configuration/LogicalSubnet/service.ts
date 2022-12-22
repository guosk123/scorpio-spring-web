import application from '@/common/applicationConfig';
import type { IAjaxResponseFactory } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
import type { ILogicalSubnet } from './typings';
const { API_VERSION_PRODUCT_V1 } = application;
/**
 * 所有的逻辑子网
 */
export async function queryAllLogicalSubnets(): Promise<IAjaxResponseFactory<ILogicalSubnet[]>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/logical-subnets`);
}

/**
 * 获取逻辑子网详情
 */
export async function queryLogicalSubnetDetail({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/logical-subnets/${id}`);
}

/**
 * 新建逻辑子网
 */
export async function createLogicalSubnet(params: ILogicalSubnet) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/logical-subnets`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

/**
 * 更新逻辑子网
 */
export async function updateLogicalSubnet({ id, ...restParams }: ILogicalSubnet) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/logical-subnets/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}

/**
 * 删除逻辑子网
 */
export async function deleteLogicalSubnet(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/logical-subnets/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}
