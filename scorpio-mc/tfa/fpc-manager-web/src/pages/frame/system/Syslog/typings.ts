export type TDataSource = 'system_log' | 'audit_log' | 'system_alarm';

/**
 * 日志告警外发
 */
export interface IAlarmAndLogSyslog {
  id?: string;
  /**
   * 主机IP地址
   */
  ipAddress: string;
  /**
   * 端口
   */
  port: string;
  /**
   * 协议
   */
  protocol: 'TCP' | 'UDP';
  /**
   * 状态
   *
   * - 0：关闭
   * - 1：开启
   */
  state: '0' | '1';
  dataSource: string;
}
