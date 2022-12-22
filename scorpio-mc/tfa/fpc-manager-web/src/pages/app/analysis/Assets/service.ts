import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';
import type { BaselineItem } from './BaselineList';
import type { IAjaxResponseFactory, IPageFactory } from '@/common/typings';
import type { IassetsSettingParams } from './typing';
import { stringify } from 'qs';
import type { AssetItem } from './AssetsList';

//查询当前有效期
export async function getCurrentExpireDays() {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/asset-information/useful-life`);
}

//有效期配置
export async function settingExpireDays(params: { time: number }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/asset-information/useful-life`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

//资产基线设定
export async function settingsbaseline(params: IassetsSettingParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/asset-baseline`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

// TODO: get => query
//查询基线列表
export async function getbaselineLists(params: {
  ipAddress?: string;
  // type?: string[];
}): Promise<IAjaxResponseFactory<IPageFactory<BaselineItem>>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/asset-baseline?${stringify(params)}`);
}

//删除指定资产基线
export async function deleteBaseline(params: { ipAddress: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/asset-baseline?${stringify(params)}`, {
    type: 'DELETE',
  });
}

//查询设备映射关系接口

export async function getDeviceTypeLists() {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/asset-device`);
}

//查询操作系统关系接口

export async function getOperateSystemTypeLists() {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/asset-os`);
}

//查询资产列表
export async function getAssetsList(params: {
  ipAddress?: string;
}): Promise<IAjaxResponseFactory<IPageFactory<AssetItem>>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/asset-information?${stringify(params)}`);
}

//查询资产总数
export async function getTotalAssetsNumber(params: any) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/asset-information/as-statistics?${stringify(params)}`,
  );
}
