// 统计分析中用到的restful Api类别
// example: /api/webapi/fpc-v1/metric/${netif}/as-histogram 图表
// example: /api/webapi/fpc-v1/metric/${netif}             表格
export enum EMetricApiType {
  /**
   * 地区统计
   */
  'location' = 'locations',
  /**
   * 应用统计
   */
  'application' = 'applications',
  /**
   * 协议统计
   */
  'protocol' = 'l7-protocols',
  /**
   * 端口统计
   */
  'port' = 'ports',
  /**
   * IP地址组统计
   */
  'hostGroup' = 'host-groups',
  /**
   * 二层主机（MAC地址）统计
   */
  'macAddress' = 'l2-devices',
  /**
   * 三层主机（IP地址）统计
   */
  'ipAddress' = 'l3-devices',
  /**
   * IP会话统计
   */
  'ipConversation' = 'ip-conversations',
  /** 
   * dhcp分析
   */
  'DHCP' = 'dhcps',
  /**
   * 网络统计
   */
  'network' = 'networks',
  /**
   * 接口统计
   */
  'netif' = 'netifs',
}

// TODO: 每个接口的参数定义需要在这里补充
