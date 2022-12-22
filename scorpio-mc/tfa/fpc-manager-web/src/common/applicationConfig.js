/**
 * 产品全局配置
 */

const CONTEXT_PATH = '/manager';
const API_PREFIX = '/webapi';

const application = {
  /**
   * 产品名称
   */
  PRODUCT_NAME: 'TFA',

  /**
   * 生成环境下服务器配置的上下文
   */
  CONTEXT_PATH,

  /**
   * api前缀信息
   */
  API_PREFIX,

  /**
   * 公共的 api 版本前缀
   */
  API_VERSION_V1: '/v1',

  /**
   * 当前产品的 api 版本前缀
   */
  API_VERSION_PRODUCT_V1: '/fpc-v1',

  /**
   * api 完整的前缀路径
   */
  API_BASE_URL: `${CONTEXT_PATH}${API_PREFIX}`,

  /**
   * 页面左上角 logo
   */
  PRODUCT_LOGO: `${CONTEXT_PATH}/web-static/custom-static/current/logo.png?v=${Math.random()}`,
  FAVICON_PATH: `${CONTEXT_PATH}/web-static/custom-static/icon/favicon.ico?v=${Math.random()}`,

  /**
   * 是否使用服务上的路由文件
   */
  USE_SERVER_ROUTER_FILE: true, // 使用服务器上的路由文件

  /**
   * 日志归档路径
   */
  LOG_ARCHIVE_PATH: '/home/fpc/archive/logs',

  /**
   * 告警归档路径
   */
  ALARM_ARCHIVE_PATH: '/home/fpc/archive/alarms',

  /**
   * 系统备份路径
   */
  BACKUP_PATH: '/home/fpc/backup',

  /**
   * localStorage 储存权限的key值
   */
  LOCAL_KEY_AUTHORITY: 'fpc-manager-authority',
  LOCAL_KEY_LEFT_SLIDER_MENU: 'fpc-manager-left-menu-collapsed',
};

// 开发环境时补充前缀,代理进行远程调试
if (process.env.NODE_ENV === 'development') {
  application.API_BASE_URL = `/api${application.API_PREFIX}`;
  application.CONTEXT_PATH = '';
  application.USE_SERVER_ROUTER_FILE = false;

  // logo
  application.PRODUCT_LOGO = `/custom-static/current/logo.png?v=${Math.random()}`;
  application.FAVICON_PATH = `/custom-static/icon/favicon.ico?v=${Math.random()}`;
}

module.exports = application;
