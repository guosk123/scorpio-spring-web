import type { IFilter } from '@/components/FieldFilter/typings';
import type { IProperty } from './typing';
import { EMetadataProtocol } from '@/pages/app/appliance/Metadata/typings';
import { tableColumns as httpColumns } from '@/pages/app/appliance/Metadata/HTTP/Specifications';
import { tableColumns as dnsColumns } from '@/pages/app/appliance/Metadata/DNS';
import { tableColumns as ftpColumns } from '@/pages/app/appliance/Metadata/FTP';
import { tableColumns as mailColumns } from '@/pages/app/appliance/Metadata/Mail';
import { tableColumns as MetadataSMTP } from '@/pages/app/appliance/Metadata/Mail';
import { tableColumns as MetadataPOP3 } from '@/pages/app/appliance/Metadata/Mail';
import { tableColumns as MetadataIMAP } from '@/pages/app/appliance/Metadata/Mail';
import { tableColumns as telnetColumns } from '@/pages/app/appliance/Metadata/TELNET';
import { tableColumns as sslColumns } from '@/pages/app/appliance/Metadata/SSL';
import { tableColumns as sshColumns } from '@/pages/app/appliance/Metadata/SSH';
import { tableColumns as mysqlColumns } from '@/pages/app/appliance/Metadata/MYSQL';
import { tableColumns as postgresqlColumns } from '@/pages/app/appliance/Metadata/PostgreSQL';
import { tableColumns as tnsColumns } from '@/pages/app/appliance/Metadata/TNS';
import { tableColumns as icmpColumns } from '@/pages/app/appliance/Metadata/ICMP';
import { tableColumns as icmpv4Columns } from '@/pages/app/appliance/Metadata/ICMPV4';
import { tableColumns as icmpv6Columns } from '@/pages/app/appliance/Metadata/ICMPV6';
import { tableColumns as socks4Columns } from '@/pages/app/appliance/Metadata/SOCKS4';
import { tableColumns as socks5Columns } from '@/pages/app/appliance/Metadata/Socks5';
import { tableColumns as dhcpColumns } from '@/pages/app/appliance/Metadata/DHCP/DHCPSpecifications';
import { tableColumns as dhcpv6Columns } from '@/pages/app/appliance/Metadata/DHCPV6/DHCPV6Specifications';
import { tableColumns as tdsColumns } from '@/pages/app/appliance/Metadata/TDS';
import { tableColumns as arpColumns } from '@/pages/app/appliance/Metadata/ARP';
import { tableColumns as ospfColumns } from '@/pages/app/appliance/Metadata/OSPF';

export const METADATA_FIELD_MAP = {
  [EMetadataProtocol.HTTP]: httpColumns(),
  [EMetadataProtocol.DNS]: dnsColumns,
  [EMetadataProtocol.FTP]: ftpColumns,
  [EMetadataProtocol.MAIL]: mailColumns,
  [EMetadataProtocol.SMTP]: MetadataSMTP,
  [EMetadataProtocol.POP3]: MetadataPOP3,
  [EMetadataProtocol.IMAP]: MetadataIMAP,
  [EMetadataProtocol.TELNET]: telnetColumns,
  [EMetadataProtocol.SSL]: sslColumns,
  [EMetadataProtocol.SSH]: sshColumns,
  [EMetadataProtocol.MYSQL]: mysqlColumns,
  [EMetadataProtocol.POSTGRESQL]: postgresqlColumns,
  [EMetadataProtocol.TNS]: tnsColumns,
  [EMetadataProtocol.ICMP]: [...icmpColumns, ...icmpv4Columns, ...icmpv6Columns],
  [EMetadataProtocol.ICMPV4]: icmpv4Columns,
  [EMetadataProtocol.ICMPV6]: icmpv6Columns,
  [EMetadataProtocol.SOCKS4]: socks4Columns,
  [EMetadataProtocol.SOCKS5]: socks5Columns,
  [EMetadataProtocol.DHCP]: dhcpColumns,
  [EMetadataProtocol.DHCPV6]: dhcpv6Columns,
  [EMetadataProtocol.TDS]: tdsColumns,
  [EMetadataProtocol.ARP]: arpColumns,
  [EMetadataProtocol.OSPF]: ospfColumns,
  'mail-pop3': MetadataPOP3,
  'mail-smtp': MetadataSMTP,
  'mail-imap': MetadataIMAP,
  dhcp_v6: dhcpv6Columns,
};

/** 元数据和table名称对应的map */
export const METADATA_TABLE_MAP = {
  mysql: 'd_fpc_protocol_mysql_log_record',
  pgsql: 'd_fpc_protocol_postgresql_log_record',
  arp: 'd_fpc_protocol_arp_log_record',
  sip: 'd_fpc_protocol_sip_log_record',
  dns: 'd_fpc_protocol_dns_log_record',
  icmp: 'd_fpc_protocol_icmp_log_record',
  ssh: 'd_fpc_protocol_ssh_log_record',
  dhcp: 'd_fpc_protocol_dhcp_log_record',
  dhcp_v6: 'd_fpc_protocol_dhcp_log_record',
  ssl: 'd_fpc_protocol_ssl_log_record',
  ftp: 'd_fpc_protocol_ftp_log_record',
  telnet: 'd_fpc_protocol_telnet_log_record',
  socks4: 'd_fpc_protocol_socks4_log_record',
  socks5: 'd_fpc_protocol_socks5_log_record',
  'mail-pop3': 'd_fpc_protocol_mail_log_record',
  'mail-smtp': 'd_fpc_protocol_mail_log_record',
  'mail-imap': 'd_fpc_protocol_mail_log_record',
  tds: 'd_fpc_protocol_tds_log_record',
  // rtp: 'd_fpc_protocol_rtp_log_record',
  tns: 'd_fpc_protocol_tns_log_record',
  db2: 'd_fpc_protocol_db2_log_record',
  http: 'd_fpc_protocol_http_log_record',
  ospf: 'd_fpc_protocol_ospf_log_record',
  ldap: 'd_fpc_protocol_ldap_log_record',
};

/** 统计和table名称对应的map */
export const STATISTICS_TABLE_MAP = {
  statistics_forward: 'd_fpc_metric_forward_data_record',
  statistics_dhcp: 'd_fpc_metric_dhcp_data_record',
  statistics_dhcp_v6: 'd_fpc_metric_dhcp_data_record',
  http_request_info: 'd_fpc_metric_http_request_data_record',
  metric_diskio: 'd_fpc_metric_disk_io_data_record',
  statistics_l3device: 'd_fpc_metric_l3device_data_record',
  statistics_port: 'd_fpc_metric_port_data_record',
  statistics_location: 'd_fpc_metric_location_data_record',
  statistics_network: 'd_fpc_metric_network_data_record',
  statistics_l2device: 'd_fpc_metric_l2device_data_record',
  statistics_service: 'd_fpc_metric_service_data_record',
  statistics_dscp: 'd_fpc_metric_dscp_data_record',
  http_terminal_info: 'd_fpc_metric_os_data_record',
  system_monitor: 'd_fpc_metric_monitor_data_record',
  http_status_info: 'd_fpc_metric_http_analysis_data_record',
  statistics_application: 'd_fpc_metric_application_data_record',
  statistics_hostgroup: 'd_fpc_metric_hostgroup_data_record',
  statistics_netif: 'd_fpc_metric_netif_data_record',
  statistics_ip_conversation: 'd_fpc_metric_ip_conversation_data_record',
  statistics_l7protocol: 'd_fpc_metric_l7protocol_data_record',
};

/** table对应的comment */
export const TABLE_COMMENT_MAP = {
  statistics_forward: '实时转发统计表',
  statistics_dhcp: 'DHCP统计表',
  statistics_dhcp_v6: 'DHCPv6统计表',
  http_request_info: 'HTTP请求状态统计表',
  metric_diskio: '磁盘分区IO统计',
  statistics_l3device: '三层主机统计表',
  statistics_port: '端口统计表',
  statistics_location: '地区统计表',
  statistics_network: '网络统计表',
  statistics_l2device: '二层主机统计表',
  statistics_service: '业务统计表',
  statistics_dscp: 'DSCP统计表',
  http_terminal_info: '操作系统分布统计表',
  system_monitor: '系统状态监控表',
  http_status_info: 'HTTP分析统计表',
  statistics_application: '应用统计表',
  statistics_hostgroup: '地址组统计表',
  statistics_netif: '网口统计表',
  statistics_ip_conversation: 'IP通讯对统计表',
  statistics_l7protocol: '七层协议统计表',
};

/** 流日志和table对应map */
export const FLOW_LOG_TABLE_MAP = {
  flowlog: 'd_fpc_flow_log_record',
};

/** 安全告警和table对应map */
export const SURICATA_TABLE_MAP = {
  suricata: 'd_fpc_analysis_suricata_alert_message',
};

/** 跟节点占位符 */
export const METADATA_PLACEHOLDER = 'metadata_placeholder';
export const STATISTICS_PLACEHOLDER = 'statistics_placeholder';
// 这两个index都是alert
export const SERVICE_ALERT_KEY = 'service_alert_key';
export const NETWORK_ALERT_KEY = 'network_alert_key';

export const SURICATA_KEY = 'suricata';
export const FLOW_LOG_KEY = 'flowlog';

export const SYSTEM_ALERT_KEY = 'systemAlert';
export const SYSTEM_LOG_KEY = 'systemLog';

/** 所有的字典 */
export const TABLE_MAP = {
  ...METADATA_TABLE_MAP,
  ...STATISTICS_TABLE_MAP,
  ...FLOW_LOG_TABLE_MAP,
  ...SURICATA_TABLE_MAP,
  // [SERVICE_ALERT_KEY]: 't_fpc_appliance_alert_message',
  // [NETWORK_ALERT_KEY]: 't_fpc_appliance_alert_message',
  service_alert_key: 'service_alert_key',
  network_alert_key: 'network_alert_key',
  systemAlert: 'systemAlert',
  systemLog: 'systemLog',
};

export const INDEX_LIST = Object.keys(TABLE_MAP);
export const METADATA_NAME_LIST = Object.keys(METADATA_TABLE_MAP);
/** 网络告警字段 */
export const NETWORK_ALERT_PROPERTIES = {
  network_name: { name: 'network_name', type: 'String', comment: '网络名称' },
  name: { name: 'name', type: 'String', comment: '告警名称' },
  category: { name: 'category', type: 'String', comment: '告警分类' },
  level: { name: 'component', type: 'UInt8', comment: '告警级别' },
  alert_content: { name: 'content', type: 'String', comment: '告警详情' },
  arise_time: { name: 'arise_time', type: "DateTime64(3, 'UTC')", comment: '触发时间' },
};

/** 业务告警字段 */
export const SERVICE_ALERT_PROPERTIES = {
  network_name: { name: 'network_name', type: 'String', comment: '网络名称' },
  service_name: { name: 'service_name', type: 'String', comment: '业务名称' },
  name: { name: 'name', type: 'String', comment: '告警名称' },
  category: { name: 'category', type: 'String', comment: '告警分类' },
  level: { name: 'component', type: 'UInt8', comment: '告警级别' },
  alert_content: { name: 'content', type: 'String', comment: '告警详情' },
  arise_time: { name: 'arise_time', type: "DateTime64(3, 'UTC')", comment: '触发时间' },
};

/** 系统告警字段 */
export const SYSTEM_ALERT_PROPERTIES = {
  host_name: { name: 'host_name', comment: '主机名称' },
  ipaddress: { name: 'ipaddress', comment: '设备ip地址' },
  category: { name: 'category', comment: '告警类型' },
  component: { name: 'component', comment: '告警组件' },
  content: { name: 'content', comment: '告警描述' },
  arise_time: { name: 'arise_time', comment: '告警时间' },
};

/** 系统日志告警字段 */
export const SYSTEM_LOG_PROPERTIES = {
  host_name: { name: 'host_name', comment: '主机名称' },
  ipaddress: { name: 'ipaddress', comment: '设备ip地址' },
  category: { name: 'category', comment: '日志类型' },
  component: { name: 'component', comment: '日志组件' },
  content: { name: 'content', comment: '日志描述' },
  arise_time: { name: 'arise_time', comment: '日志时间' },
};

export const OPERATOR_LIST = [
  {
    value: 'EQUALS',
    label: '=',
  },
  {
    value: 'NOT_EQUALS',
    label: '!=',
  },
  {
    value: 'LESS_THAN',
    label: '<',
  },
  {
    value: 'GREATER_THAN',
    label: '>',
  },
  {
    value: 'LESS_THAN_OR_EQUAL',
    label: '<=',
  },
  {
    value: 'GREATER_THAN_OR_EQUAL',
    label: '>=',
  },
  // {
  //   value: 'IN',
  //   label: 'IN',
  // },
  // {
  //   value: 'NOT_IN',
  //   label: 'NOT IN',
  // },
  {
    value: 'LIKE',
    label: 'LIKE',
  },
  {
    value: 'NOT_LIKE',
    label: 'NOT LIKE',
  },
  {
    value: 'EXISTS',
    label: 'EXISTS',
  },
  {
    value: 'NOT_EXISTS',
    label: 'NOT EXISTS',
  },
] as const;

export const operator_ids = OPERATOR_LIST.map((item) => item.value);

export enum ESendingMethod {
  REALTIME = '0',
  REGULAR = '1',
}

export enum EMsgHead {
  REG_NAME = 'reg_name',
  LOG_TYPE = 'log_type',
}

export enum EDataTypeFlag {
  CHECKED = '1',
  UNCHECKED = '0',
}

export enum EWithNodeIP {
  CHECKED = '1',
  UNCHECKED = '0',
}

export const treeData = [
  {
    title: '应用层协议详单',
    key: METADATA_PLACEHOLDER,
    children: Object.keys(METADATA_TABLE_MAP).map((key) => {
      return { title: key, key };
    }),
  },
  {
    title: '统计',
    key: STATISTICS_PLACEHOLDER,
    children: Object.keys(STATISTICS_TABLE_MAP).map((key) => {
      return { title: key, key };
    }),
  },
  {
    title: '会话详单',
    key: FLOW_LOG_KEY,
  },
  {
    title: '安全告警',
    key: SURICATA_KEY,
  },
  {
    title: '业务告警',
    key: SERVICE_ALERT_KEY,
  },
  {
    title: '网络告警',
    key: NETWORK_ALERT_KEY,
  },
  {
    title: '系统告警',
    key: SYSTEM_ALERT_KEY,
  },
  {
    title: '系统日志',
    key: SYSTEM_LOG_KEY,
  },
];

export type ILogTypeInfoType = Record<
  string,
  {
    properties: IProperty[];
    conditions: IFilter[];
    dsl?: string;
  }
>;
