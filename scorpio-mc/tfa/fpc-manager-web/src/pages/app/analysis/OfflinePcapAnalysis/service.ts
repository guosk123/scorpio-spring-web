import { stringify } from 'qs';
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';

export async function queryPcapList(params: any) {
  const suffix = `${params && `?${stringify(params)}`}`;
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/packet-analysis-subtasks${suffix}`);
}

export async function queryPcapById(id:string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/packet-analysis-subtasks/${id}`);
}


export async function queryPcapTaskLogList(params: any) {
  const suffix = `${params && `?${stringify(params)}`}`;
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/packet-analysis-tasks/logs${suffix}`);
}

export async function queryOfflineSubtaskList(params: any) {
  const suffix = `${params && `?${stringify(params)}`}`;
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/packet-analysis-subtasks${suffix}`);
}

export async function queryPcapTaskList(params?: any) {
  const suffix = `${params ? `?${stringify(params)}` : ''}`;
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/packet-analysis-tasks${suffix}`);
}

export async function deleteOfflineSubTask({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/packet-analysis-subtasks/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}
export async function deletePcapFile({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/packet-analysis-tasks/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

export async function updatePacpTask(params?: any) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/packet-analysis-tasks`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

export async function deletePcapTask({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/packet-analysis-tasks/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

export async function queryPcapTaskInfo(pcapFileId: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/packet-analysis-tasks/${pcapFileId}`);
}

export async function queryOfflineFiles(params: any) {
  const suffix = `${params && `?${stringify(params)}`}`;
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/packet-file-directory${suffix}`);
}

export async function queryPcpInfo(pcapFileId: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/packet-analysis-subtasks/${pcapFileId}`);
}

export async function queryUploadUri(filename: string) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/packet-analysis-tasks/upload-urls?name=${filename}`,
  );
}
