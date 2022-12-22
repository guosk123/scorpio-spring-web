/**
 * 产品全局配置
 */
const CONTEXT_PATH = '/center';
const API_PREFIX = '/webapi';

const application = {
  /**
   * 产品名称
   */
  PRODUCT_NAME: 'CMS',

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
  API_VERSION_PRODUCT_V1: '/fpc-cms-v1',

  /**
   * api 完整的前缀路径
   */
  API_BASE_URL: `${CONTEXT_PATH}${API_PREFIX}`,

  /**
   * 页面左上角 logo
   */
  PRODUCT_LOGO: `${CONTEXT_PATH}/web-static/custom-static/current/logo.png?v=${Math.random()}`,

  /**
   * 登录页面 logo
   */
  PRODUCT_LOGIN_LOGO: `${CONTEXT_PATH}/web-static/custom-static/current/login-logo.png?v=${Math.random()}`,

  /**
   * 页面底部的版权信息
   */
  PRODUCT_COPYRIGHT: `${CONTEXT_PATH}/web-static/custom-static/current/copyright.png?v=${Math.random()}`,

  /**
   * 日志归档路径
   */
  LOG_ARCHIVE_PATH: '/home/fpc-cms/archive/logs',

  /**
   * 告警归档路径
   */
  ALARM_ARCHIVE_PATH: '/home/fpc-cms/archive/alarms',

  /**
   * 系统备份路径
   */
  BACKUP_PATH: '/home/fpc-cms/backup',

  /**
   * localStorage
   */
  LOCAL_KEY_AUTHORITY: 'fpc-cms-authority',
  LOCAL_KEY_LEFT_SLIDER_MENU: 'fpc-cms-left-menu-collapsed',
};

// 开发环境时补充前缀,代理进行远程调试
if (process.env.NODE_ENV === 'development') {
  application.API_BASE_URL = `/api${application.API_PREFIX}`;
  application.CONTEXT_PATH = '';

  // logo
  application.PRODUCT_LOGO = '/custom-static/current/logo.png';
  // 登录页logo
  application.PRODUCT_LOGIN_LOGO = '/custom-static/current/login-logo.png';
  // 版权
  application.PRODUCT_COPYRIGHT = '/custom-static/current/copyright.png';
}

export default application;
