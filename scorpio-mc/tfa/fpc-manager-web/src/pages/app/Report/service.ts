import config from '@/common/applicationConfig';
import type { IAjaxResponseFactory } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
const { API_VERSION_PRODUCT_V1 } = config;

/**
 * 获取 BI 配置
 */
export async function queryBISettings(): Promise<
  IAjaxResponseFactory<{
    entry: string;
    token: string;
  }>
> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/bi-infos`);
}
