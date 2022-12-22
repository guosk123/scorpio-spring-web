export const BOOL_NO = '0';
export const BOOL_YES = '1';

/**
 * ===============
 * 系统角色
 * ===============
 */

/**
 * 系统管理员
 */
export const PERM_SYS_USER = 'PERM_SYS_USER';

/**
 * 审计管理员
 */
export const PERM_AUDIT_USER = 'PERM_AUDIT_USER';

/**
 * 业务用户（对配置只有查看权限）
 */
export const PERM_USER = 'PERM_USER';

/**
 * 业务管理员（可以对配置进行管理）
 */
export const PERM_SERVICE_USER = 'PERM_SERVICE_USER';

/**
 * RESTAPI调用用户
 */
export const PERM_RESTAPI_USER = 'PERM_RESTAPI_USER';

/**
 * ===============
 * 告警
 * ===============
 */

/**
 * 告警级别
 */
export const SYSTEM_ALARM_LEVEL = [
  {
    key: '0',
    label: '提示',
    // 颜色配置详情看 https://ant.design/components/badge-cn/
    status_color: '#d9d9d9',
  },
  {
    key: '1',
    label: '一般',
    status_color: 'blue',
  },
  {
    key: '2',
    label: '重要',
    status_color: 'yellow',
  },
  {
    key: '3',
    label: '紧急',
    status_color: 'red',
  },
];

/**
 * 系统组件
 */
export const SYSTEM_COMPONENT = [
  {
    key: '000000',
    label: 'Common',
  },
  {
    key: '001001',
    label: 'FPCManager',
  },
  {
    key: '001002',
    label: 'FPCEngine',
  },
  {
    key: '001003',
    label: 'FPCCenter',
  },
  {
    key: '001004',
    label: 'SYSMonitor',
  },
];

/**
 * ===============
 * 日志
 * ===============
 */

/**
 * 日志级别
 */
export const SYSTEM_LOG_LEVEL = [
  {
    key: '0',
    label: '调试',
  },
  {
    key: '1',
    label: '通知',
  },
  {
    key: '2',
    label: '告警',
  },
  {
    key: '3',
    label: '错误',
  },
  {
    key: '4',
    label: '致命',
  },
];

/**
 * 审计日志
 */
export const SYSTEM_LOG_CATEGORY_AUDIT = '101';

/**
 * 日志告警归档
 */
export const SYSTEM_LOG_CATEGORY_LOG_ARCHIVE = '102';

/**
 * 系统备份日志
 */
export const SYSTEM_LOG_CATEGORY_SYSTEM_BACKUP = '103';

/**
 * 日志类型
 */
export const SYSTEM_LOG_CATEGORY = [
  {
    key: '1',
    label: '运行',
  },
  {
    key: SYSTEM_LOG_CATEGORY_AUDIT,
    label: '审计',
  },
  {
    key: SYSTEM_LOG_CATEGORY_LOG_ARCHIVE,
    label: '日志告警归档',
  },
  {
    key: SYSTEM_LOG_CATEGORY_SYSTEM_BACKUP,
    label: '系统备份',
  },
];

/**
 * ===============
 * RAID
 * ===============
 */

/**
 * RAID 状态：正常
 */
export const DEVICE_RAID_STATE_NORMAL = '0';
/**
 * RAID 状态：部分降级
 */
export const DEVICE_RAID_STATE_PARTIALLY_DEGRADE = '1';
/**
 * RAID 状态：降级
 */
export const DEVICE_RAID_STATE_DEGRADE = '2';
/**
 * RAID 状态：离线
 */
export const DEVICE_RAID_STATE_OFFLINE = '3';
/**
 * RAID 状态：故障
 */
export const DEVICE_RAID_STATE_ERROR = '4';

/**
 * 所有的RAID状态
 */
export const DEVICE_RAID_STATE_LIST = [
  {
    key: DEVICE_RAID_STATE_NORMAL,
    label: '正常',
    color: '#52c41a',
  },
  {
    key: DEVICE_RAID_STATE_PARTIALLY_DEGRADE,
    label: '部分降级',
    color: '#13c2c2',
  },
  {
    key: DEVICE_RAID_STATE_DEGRADE,
    label: '降级',
    color: '#1890ff',
  },
  {
    key: DEVICE_RAID_STATE_OFFLINE,
    label: '离线',
    color: '#faad14',
  },
  {
    key: DEVICE_RAID_STATE_ERROR,
    label: '故障',
    color: '#f5222d',
  },
];

/**
 * ===============
 * 硬盘
 * ===============
 */

/**
 * 磁盘状态：在线
 */
export const DEVICE_DISK_STATUS_ONLINE = '0';
/**
 * 磁盘状态：热备
 */
export const DEVICE_DISK_STATUS_HOTSPARE = '1';
/**
 * 磁盘状态：重建
 */
export const DEVICE_DISK_STATUS_REBUILD = '2';
/**
 * 磁盘状态：可配置
 */
export const DEVICE_DISK_STATUS_UNCONFIGURED_GOOD = '3';
/**
 * 磁盘状态：不可配置
 */
export const DEVICE_DISK_STATUS_UNCONFIGURED_BAD = '4';
/**
 * 磁盘状态：回拷
 */
export const DEVICE_DISK_STATUS_COPY_BACK = '5';
/**
 * 磁盘状态：失败
 */
export const DEVICE_DISK_STATUS_FAILED = '6';
/**
 * 磁盘状态：错误
 */
export const DEVICE_DISK_STATUS_ERROR = '7';

/**
 * 硬盘状态
 */
export const DEVICE_DISK_STATUS = [
  {
    key: DEVICE_DISK_STATUS_ONLINE,
    label: '在线',
    // 颜色配置详情看 https://ant.design/components/badge-cn/
    color: '#52c41a',
    status_color: 'green',
  },
  {
    key: DEVICE_DISK_STATUS_HOTSPARE,
    label: '热备',
    status_color: 'blue',
    color: '#1890ff',
  },
  {
    key: DEVICE_DISK_STATUS_REBUILD,
    label: '重建',
    status_color: 'cyan',
    color: '#13c2c2',
  },
  {
    key: DEVICE_DISK_STATUS_UNCONFIGURED_GOOD,
    label: '可配置',
    status_color: '#d9d9d9',
    color: '#d9d9d9',
  },
  {
    key: DEVICE_DISK_STATUS_UNCONFIGURED_BAD,
    label: '不可配置',
    status_color: '#d9d9d9',
    color: '#d9d9d9',
  },
  {
    key: DEVICE_DISK_STATUS_COPY_BACK,
    label: '回拷',
    color: '#d3adf7',
    status_color: 'purple',
  },
  {
    key: DEVICE_DISK_STATUS_FAILED,
    label: '失败',
    color: '#f5222d',
    status_color: 'red',
  },
  {
    key: DEVICE_DISK_STATUS_ERROR,
    label: '错误',
    color: '#f5222d',
    status_color: 'red',
  },
];

/**
 * 磁盘介质
 */
export const DEVICE_DISK_MEDIUM = [
  {
    key: '0',
    label: 'HDD',
  },
  {
    key: '1',
    label: 'SSD',
  },
  {
    key: '2',
    label: '未知',
  },
];

/**
 * ===============
 * 设备接口
 * ===============
 */

/**
 * 网卡状态：up
 */
export const DEVICE_NETIF_STATE_UP = '0';
/**
 * 网卡状态：down
 */
export const DEVICE_NETIF_STATE_DOWN = '1';

/**
 * 磁盘物理位置
 */
export const DEVICE_DISK_ENCLOSURE = [
  {
    key: '8',
    label: '前面板',
  },
  {
    key: '9',
    label: '后面板',
  },
];

/**
 * 统计默认的时间间隔，30秒
 */
export const DEFAULT_INTERVAL = 30;

/**
 * 统计时可以选择的时间
 */
export const STATS_TIME_RANGE = [
  {
    key: 'now-30m',
    name: '最近30分钟',
  },
  {
    key: 'now-1h',
    name: '最近1小时',
  },
  {
    key: 'now-12h',
    name: '最近12小时',
  },
  {
    key: 'now-1d',
    name: '最近1天',
  },
];

/**
 * 转换单位
 */
export const ONE_KILO_1000 = 1000;
export const ONE_KILO_1024 = 1024;

/**
 * 业务接口类型：管理口
 */
export const DEVICE_NETIF_CATEGORY_MANAGER = '0';
/**
 * 业务接口类型：接收口
 */
export const DEVICE_NETIF_CATEGORY_RECEIVE = '1';
/**
 * 业务接口类型：重放口
 */
export const DEVICE_NETIF_CATEGORY_REPLAY = '2';

export const DEVICE_NETIF_CATEGORY_LIST = [
  {
    key: DEVICE_NETIF_CATEGORY_MANAGER,
    label: '流量管理',
  },
  {
    key: DEVICE_NETIF_CATEGORY_RECEIVE,
    label: '流量接收',
  },
  {
    key: DEVICE_NETIF_CATEGORY_REPLAY,
    label: '流量重放',
  },
];
