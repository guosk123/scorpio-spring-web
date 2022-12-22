import { stringify } from 'qs';
import ajax from '@/utils/frame/ajax';
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
export async function querySegmentAnalysisShowData(interfaceName: string, params: any) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/network-segmentation/${interfaceName}?${stringify(params)}`,
  );
}
