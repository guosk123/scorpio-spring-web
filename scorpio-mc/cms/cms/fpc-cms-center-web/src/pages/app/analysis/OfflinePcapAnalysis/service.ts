import { stringify } from 'qs';
import config from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';

const { API_VERSION_PRODUCT_V1 } = config;

export async function queryPcapList(params: any) {
  const suffix = `${params && `?${stringify(params)}`}`;
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/offline-analysis-tasks${suffix}`);
}

export async function deletePcapFile({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/offline-analysis-tasks/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

export async function queryPcpInfo(pcapFileId: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/offline-analysis-tasks/${pcapFileId}`);
}

export async function queryUploadUri(filename: string) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/offline-analysis-tasks/upload-urls?name=${filename}`,
  );
}
