import ajax from '@/utils/frame/ajax';
import config from '@/common/applicationConfig';

export async function queryLowerCMSList() {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/central/cms-devices`);
}

export async function queryLoginSensorUrl(params: any) {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/central/fpc-devices/${params}/loginUrl`);
}

export async function queryLoginCmsUrl(params: any) {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/central/cms-devices/${params}/loginUrl`);
}

export async function deleteLowerCMS(params: any) {
  console.log('params', params);
  return ajax(`${config.API_VERSION_PRODUCT_V1}/configuration/equipment/lower-cms/${params.id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

export async function createLowerCMS(params: any) {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/configuration/equipment/lower-cms`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      status: params,
    },
  });
}

export async function querySensorList() {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/central/fpc-devices`);
}

export async function querySensorItem(params: any) {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/central/fpc-devices/${params.id}`);
}

export async function deleteSensorItem(params: any) {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/central/fpc-devices/${params.id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

export async function createSensor(params: any) {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/configuration/equipment/sensor`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      status: params,
    },
  });
}

export async function querySensorUpgradeList() {
  return ajax(
    `${config.API_VERSION_PRODUCT_V1}/configuration/equipment/sensor-upgrade-packet/as-list`,
  );
}

export async function deleteSensorUpgradeItem(params: any) {
  return ajax(
    `${config.API_VERSION_PRODUCT_V1}/configuration/equipment/sensor-upgrade-packet/${params.id}`,
    {
      type: 'POST',
      data: {
        _method: 'DELETE',
      },
    },
  );
}

export async function queryUpperCMSSetting() {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/central/cms-settings`);
}

export async function createUpperCMSSetting(params: any) {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/central/cms-settings`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}

export async function querySyncRemoteServers() {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/central/fpc-devices/sync-remote-servers`, {
    type: 'POST',
  });
}
