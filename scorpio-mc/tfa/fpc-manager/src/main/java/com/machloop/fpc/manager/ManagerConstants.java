package com.machloop.fpc.manager;

public interface ManagerConstants {

  public static final String GLOBAL_SETTING_CMS_IP = "cms.ip";
  public static final String GLOBAL_SETTING_CMS_TOKEN = "cms.token";
  public static final String GLOBAL_SETTING_CMS_STATE = "cms.state";
  public static final String GLOBAL_SETTING_LOCAL_LAST_HEARTBEAT = "local.last.heartbeat";

  /*
   * 
   */
  public static final String RECEIVER_TYPE_KAFKA = "kafka";

  public static final String FPC_DATABASE = "fpc";

  public static final String IPV4_TO_IPV6_PREFIX = "::ffff:";

  public static final String INDEX_PROTOCOL_MAIL_LOG_RECORD = "i_fpc-protocol-mail-log-record-";
  public static final String INDEX_PROTOCOL_HTTP_LOG_RECORD = "i_fpc-protocol-http-log-record-";
  public static final String INDEX_PROTOCOL_DNS_LOG_RECORD = "i_fpc-protocol-dns-log-record-";
  public static final String INDEX_PROTOCOL_FTP_LOG_RECORD = "i_fpc-protocol-ftp-log-record-";
  public static final String INDEX_PROTOCOL_TELNET_LOG_RECORD = "i_fpc-protocol-telnet-log-record-";
  public static final String INDEX_PROTOCOL_SSL_LOG_RECORD = "i_fpc-protocol-ssl-log-record-";

  public static final String ALIAS_PROTOCOL_MAIL_LOG_RECORD = "a_fpc-protocol-mail-log-record";
  public static final String ALIAS_PROTOCOL_HTTP_LOG_RECORD = "a_fpc-protocol-http-log-record";
  public static final String ALIAS_PROTOCOL_DNS_LOG_RECORD = "a_fpc-protocol-dns-log-record";
  public static final String ALIAS_PROTOCOL_FTP_LOG_RECORD = "a_fpc-protocol-ftp-log-record";
  public static final String ALIAS_PROTOCOL_TELNET_LOG_RECORD = "a_fpc-protocol-telnet-log-record";
  public static final String ALIAS_PROTOCOL_SSL_LOG_RECORD = "a_fpc-protocol-ssl-log-record";

  public static final String INDEX_ANALYSIS_BEACON_DETECTION = "i_fpc-analysis-beacon-detection";
  public static final String INDEX_ANALYSIS_DYNAMIC_DOMAIN = "i_fpc-analysis-dynamic-domain";
  public static final String INDEX_ANALYSIS_INTELLIGENCE_IP = "i_fpc-analysis-intelligence-ip";
  public static final String INDEX_ANALYSIS_NONSTANDARD_PROTOCOL = "i_fpc-analysis-nonstandard-protocol";
  public static final String INDEX_ANALYSIS_SUSPICIOUS_HTTPS = "i_fpc-analysis-suspicious-https";
  public static final String INDEX_ANALYSIS_BRUTE_FORCE = "i_fpc-analysis-brute-force";
  public static final String INDEX_ANALYSIS_CUSTOM_TEMPLATE = "i_fpc-analysis-custom-template";
  public static final String INDEX_ANALYSIS_PATTERN = "i_fpc-analysis-*";

  public static final String TABLE_ANALYSIS_BEACON_DETECTION = "t_fpc_analysis_beacon_detection";
  public static final String TABLE_ANALYSIS_DYNAMIC_DOMAIN = "t_fpc_analysis_dynamic_domain";
  public static final String TABLE_ANALYSIS_INTELLIGENCE_IP = "t_fpc_analysis_intelligence_ip";
  public static final String TABLE_ANALYSIS_NONSTANDARD_PROTOCOL = "t_fpc_analysis_nonstandard_protocol";
  public static final String TABLE_ANALYSIS_SUSPICIOUS_HTTPS = "t_fpc_analysis_suspicious_https";
  public static final String TABLE_ANALYSIS_BRUTE_FORCE = "t_fpc_analysis_brute_force";
  public static final String TABLE_ANALYSIS_CUSTOM_TEMPLATE = "t_fpc_analysis_custom_template";

  public static final String INDEX_METRIC_NETWORK_DATA_RECORD = "i_fpc-metric-network-data-record-";
  public static final String INDEX_METRIC_NETIF_DATA_RECORD = "i_fpc-metric-netif-data-record-";
  public static final String INDEX_METRIC_SERVICE_DATA_RECORD = "i_fpc-metric-service-data-record-";
  public static final String INDEX_METRIC_APP_DATA_RECORD = "i_fpc-metric-application-data-record-";
  public static final String INDEX_METRIC_HOSTGROUP_DATA_RECORD = "i_fpc-metric-hostgroup-data-record-";
  public static final String INDEX_METRIC_LOCATION_DATA_RECORD = "i_fpc-metric-location-data-record-";
  public static final String INDEX_METRIC_L2DEVICE_DATA_RECORD = "i_fpc-metric-l2device-data-record-";
  public static final String INDEX_METRIC_L3DEVICE_DATA_RECORD = "i_fpc-metric-l3device-data-record-";
  public static final String INDEX_METRIC_PORT_DATA_RECORD = "i_fpc-metric-port-data-record-";
  public static final String INDEX_METRIC_L7PROTOCOL_DATA_RECORD = "i_fpc-metric-l7protocol-data-record-";
  public static final String INDEX_METRIC_MONITOR_DATA_RECORD = "i_fpc-metric-monitor-data-record-";
  public static final String INDEX_METRIC_DHCP_DATA_RECORD = "i_fpc-metric-dhcp-data-record-";
  public static final String INDEX_METRIC_IP_CONVERSATION_DATA_RECORD = "i_fpc-metric-ip-conversation-data-record-";
  public static final String INDEX_METRIC_DSCP_DATA_RECORD = "i_fpc-metric-dscp-data-record-";

  public static final String ALIAS_METRIC_NETWORK_DATA_RECORD = "a_fpc-metric-network-data-record";
  public static final String ALIAS_METRIC_NETIF_DATA_RECORD = "a_fpc-metric-netif-data-record";
  public static final String ALIAS_METRIC_SERVICE_DATA_RECORD = "a_fpc-metric-service-data-record";
  public static final String ALIAS_METRIC_APP_DATA_RECORD = "a_fpc-metric-application-data-record";
  public static final String ALIAS_METRIC_HOSTGROUP_DATA_RECORD = "a_fpc-metric-hostgroup-data-record";
  public static final String ALIAS_METRIC_LOCATION_DATA_RECORD = "a_fpc-metric-location-data-record";
  public static final String ALIAS_METRIC_L2DEVICE_DATA_RECORD = "a_fpc-metric-l2device-data-record";
  public static final String ALIAS_METRIC_L3DEVICE_DATA_RECORD = "a_fpc-metric-l3device-data-record";
  public static final String ALIAS_METRIC_PORT_DATA_RECORD = "a_fpc-metric-port-data-record";
  public static final String ALIAS_METRIC_L7PROTOCOL_DATA_RECORD = "a_fpc-metric-l7protocol-data-record";
  public static final String ALIAS_METRIC_MONITOR_DATA_RECORD = "a_fpc-metric-monitor-data-record";
  public static final String ALIAS_METRIC_DHCP_DATA_RECORD = "a_fpc-metric-dhcp-data-record";
  public static final String ALIAS_METRIC_IP_CONVERSATION_DATA_RECORD = "a_fpc-metric-ip-conversation-data-record";
  public static final String ALIAS_METRIC_DSCP_DATA_RECORD = "a_fpc-metric-dscp-data-record";

  public static final String TABLE_METRIC_NETWORK_DATA_RECORD = "t_fpc_metric_network_data_record";
  public static final String TABLE_METRIC_NETIF_DATA_RECORD = "t_fpc_metric_netif_data_record";

  public static final String TABLE_METRIC_FORWARD_DATA_RECORD = "t_fpc_metric_forward_data_record";
  public static final String TABLE_METRIC_SERVICE_DATA_RECORD = "t_fpc_metric_service_data_record";
  public static final String TABLE_METRIC_APP_DATA_RECORD = "t_fpc_metric_application_data_record";
  public static final String TABLE_METRIC_HOSTGROUP_DATA_RECORD = "t_fpc_metric_hostgroup_data_record";
  public static final String TABLE_METRIC_LOCATION_DATA_RECORD = "t_fpc_metric_location_data_record";
  public static final String TABLE_METRIC_L2DEVICE_DATA_RECORD = "t_fpc_metric_l2device_data_record";
  public static final String TABLE_METRIC_L3DEVICE_DATA_RECORD = "t_fpc_metric_l3device_data_record";
  public static final String TABLE_METRIC_PORT_DATA_RECORD = "t_fpc_metric_port_data_record";
  public static final String TABLE_METRIC_L7PROTOCOL_DATA_RECORD = "t_fpc_metric_l7protocol_data_record";
  public static final String TABLE_METRIC_MONITOR_DATA_RECORD = "t_fpc_metric_monitor_data_record";
  public static final String TABLE_METRIC_DHCP_DATA_RECORD = "t_fpc_metric_dhcp_data_record";
  public static final String TABLE_METRIC_IP_CONVERSATION_DATA_RECORD = "t_fpc_metric_ip_conversation_data_record";
  public static final String TABLE_METRIC_DSCP_DATA_RECORD = "t_fpc_metric_dscp_data_record";
  public static final String TABLE_METRIC_DISK_IO_DATA_RECORD = "t_fpc_metric_disk_io_data_record";
  public static final String TABLE_METRIC_RESTAPI_DATA_RECORD = "t_fpc_metric_restapi_data_record";

  public static final String METRIC_NPM_FLOW = "flow";
  public static final String METRIC_NPM_FRAME_LENGTH = "frame_length";
  public static final String METRIC_NPM_SESSION = "session";
  public static final String METRIC_NPM_ETHERNET_TYPE = "ethernet_type";
  public static final String METRIC_NPM_IP_PROTOCOL = "ip_protocol";
  public static final String METRIC_NPM_FRAGMENT = "fragment";
  public static final String METRIC_NPM_TCP = "tcp";
  public static final String METRIC_NPM_PACKET_TYPE = "packet_type";
  public static final String METRIC_NPM_PERFORMANCE = "performance";
  public static final String METRIC_NPM_UNIQUE_IP_COUNTS = "unique_ip_counts";

  public static final String METRIC_NPM_ALL_AGGSFILED = "all";

  public static final String SCENARIO_CUSTOM_TEMPLATE_PREFIX = "custom_";

  public static final String INDEX_PATTERNS_FPC = "i_fpc-*";

  public static final String GLOBAL_SETTING_SA_CUSTOM_CATEGORY_SEQ_KEY = "sa.custom.category.next.id";
  public static final String GLOBAL_SETTING_SA_CUSTOM_SUBCATEGORY_SEQ_KEY = "sa.custom.subcategory.next.id";
  public static final String GLOBAL_SETTING_SA_CUSTOM_APPLICATION_SEQ_KEY = "sa.custom.application.next.id";

  public static final String GLOBAL_SETTING_GEOIP_CUSTOM_COUNTRY_SEQ_KEY = "geoip.custom.country.next.id";

  public static final String GLOBAL_SETTING_DR_ROLLUP_LATEST_5MIN = "data-record.rollup.latest.5min";
  public static final String GLOBAL_SETTING_DR_ROLLUP_LATEST_1HOUR = "data-record.rollup.latest.1hour";
  public static final String GLOBAL_SETTING_DR_CLEAN_LATEST_TIME = "data-record.clean.latest.time";

  public static final String GLOBAL_SETTING_ANALYSIS_INTELLIGENCES_VERSION = "analysis.intelligences.version";
  public static final String GLOBAL_SETTING_ANALYSIS_STANDARD_PROTOCOL_VERSION = "analysis.standard.protocol.version";

  public static final String GLOBAL_SETTING_SENDUP_ALARM_CURSOR = "sendup.alarm.cursor";
  public static final String GLOBAL_SETTING_SENDUP_LOG_CURSOR = "sendup.log.cursor";

  public static final String METADATA_CONDITION_IP_HOSTGROUP_PREFIX = "host-group-id:";
  public static final String METADATA_CONDITION_ID_ANALYSIS_RESULT_PREFIX = "analysis-result-id:";

  /**
   * engine restapi
   */
  // 全包查询任务 数据包下载
  public static final String REST_ENGINE_TASK_PACKET_DOWNLOAD = "/fpc-fs/v1/packets/transmit-tasks/%s/binarys";

  // 单条流数据包所在路径查询和下载
  public static final String REST_ENGINE_FLOW_PACKET_QUERY = "/fpc-fs/v1/packets/flows/%s/fs-paths";
  public static final String REST_ENGINE_FLOW_PACKET_DOWNLOAD = "/fpc-fs/v1/packets/flows/%s/binarys";

  // 流日志数据包批量查询和下载
  public static final String REST_ENGINE_MULTIFLOW_PACKET_QUERY = "/fpc-fs/v1/packets/multiflow/query";
  public static final String REST_ENGINE_MULTIFLOW_PACKET_DOWNLOAD = "/fpc-fs/v1/packets/multiflow/binarys";

  // 流日志数据包批量最大数量
  public static final int REST_ENGINE_DOWNLOAD_MAX_SESSIONS = 10000;

  // 数据包分析(列表、统计、数据包文件路径、下载)
  public static final String REST_ENGINE_PACKETS_QUERY = "/fpc-fs/v1/packets/lists";
  public static final String REST_ENGINE_PACKETS_QUERY_STOP = "/fpc-fs/v1/packets/lists/stop";
  public static final String REST_ENGINE_PACKETS_REFINE = "/fpc-fs/v1/packets/refines";
  public static final String REST_ENGINE_PACKETS_REFINE_STOP = "/fpc-fs/v1/packets/refines/stop";
  public static final String REST_ENGINE_PACKETS_PATH = "/fpc-fs/v1/packets/fs-paths";
  public static final String REST_ENGINE_PACKETS_DOWNLOAD = "/fpc-fs/v1/packets/binarys";

  // 离线数据包文件上传
  public static final String REST_ENGINE_OFFLINE_UPLOAD = "/fpc-fs/v1/packets/offline/upload";

  // 网络秒级统计
  public static final String REST_ENGINE_STATISTICS_NETWORK_DASHBOARD = "/fpc-statistics/v1/metric/networks/%s/dashboard";
  public static final String REST_ENGINE_STATISTICS_NETWORK_PAYLOAD = "/fpc-statistics/v1/metric/networks/%s/payload";
  public static final String REST_ENGINE_STATISTICS_NETWORK_PERFORMANCE = "/fpc-statistics/v1/metric/networks/%s/performance";
  public static final String REST_ENGINE_STATISTICS_NETWORK_TCP = "/fpc-statistics/v1/metric/networks/%s/tcp";

  // 业务秒级统计
  public static final String REST_ENGINE_STATISTICS_SERVICE_DASHBOARD = "/fpc-statistics/v1/metric/services/%s/dashboard";
  public static final String REST_ENGINE_STATISTICS_SERVICE_PAYLOAD = "/fpc-statistics/v1/metric/services/%s/payload";
  public static final String REST_ENGINE_STATISTICS_SERVICE_PERFORMANCE = "/fpc-statistics/v1/metric/services/%s/performance";
  public static final String REST_ENGINE_STATISTICS_SERVICE_TCP = "/fpc-statistics/v1/metric/services/%s/tcp";

  // 日志老化时间(流日志, 元数据), 理论上最长1小时, 但有可能不精准, 所以此处设置为2小时
  public static final long ENGINE_LOG_AGINGTIME_MILLS = 2 * 60 * 60 * 1000;

  // 资产数据老化时间
  public static final String ASSET_USEFUL_LIFE = "asset.useful.life";
}
