import ajax from '@/utils/frame/ajax';
import config from '@/common/applicationConfig';

/**
 * 升级系统
 */
export async function uploadUpgrade({ formData }: any) {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/system/upgrade`, {
    type: 'POST',
    processData: false, //  告诉jquery不要处理发送的数据
    contentType: false, // 告诉jquery不要设置content-Type请求头
    data: formData,
  });
}

/**
 * 获取更新日志
 */
export async function queryUpgradeLogs({ cursor }: { cursor: number }) {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/system/upgrade/logs?cursor=${cursor}`);
}

/**
 * 获取当前更新包的版本
 */
export async function queryUpgradeInfos() {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/system/upgrade/infos`);
}
