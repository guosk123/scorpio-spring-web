import ajax from '@/utils/frame/ajax';
import config from '@/common/applicationConfig';
import { stringify } from 'qs';

export async function querySegmentAnalysisShowData(interfaceName: string,params: any) {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/metric/network-segmentation/${interfaceName}?${stringify(params)}`);
}

