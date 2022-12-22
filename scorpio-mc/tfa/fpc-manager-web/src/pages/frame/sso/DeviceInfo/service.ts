import { API_VERSION_V1 } from "@/common/applicationConfig";
import ajax from "@/utils/frame/ajax";

export async function queryDeviceInfo() {
  return ajax(`${API_VERSION_V1}/system/custom-infos`);
}

export async function updateDeviceInfo(params: any) {
  return ajax(`${API_VERSION_V1}/system/custom-infos`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}