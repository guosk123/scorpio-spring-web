import ajax from '@/utils/frame/ajax';
import { API_BASE_URL, API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import { stringify } from 'qs';

// 设备列表查询参数
interface IPaginationParams {
  page: number;
  pageSize: number;
}

/** 获取插件列表 */
export async function queryPktAnalysisPlugins(params: IPaginationParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/pkt-analysis?${stringify(params)}`);
}

/** 删除插件列表 */
export async function deletePktAnalysisPlugin(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/pkt-analysis/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

/**
 * 新建插件
 */
export async function createPktAnalysisPlugin(formData: any) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/pkt-analysis `, {
    type: 'POST',
    processData: false, //  告诉jquery不要处理发送的数据
    contentType: false, // 告诉jquery不要设置content-Type请求头
    data: formData,
  });
}

/** 下载插件 */
export async function downloadPlugin(id: string) {
  window.open(`${API_BASE_URL}${API_VERSION_PRODUCT_V1}/appliance/pkt-analysis/${id}/files`);
}

/** 预览插件 */
export async function previewPlugin(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/pkt-analysis/${id}`);
}
