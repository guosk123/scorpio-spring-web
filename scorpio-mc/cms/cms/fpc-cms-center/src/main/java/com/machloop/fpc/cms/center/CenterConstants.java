package com.machloop.fpc.cms.center;

public final class CenterConstants {

  public static final String GLOBAL_SETTING_FPC_NEXT_ID = "fpc.device.next.id";
  public static final String GLOBAL_SETTING_FPC_GROUP_NEXT_ID = "fpc.group.next.id";
  public static final String GLOBAL_SETTING_NETIF_ROLLUP_LATEST_5MIN = "report.netif.rollup.latest.5min";
  public static final String GLOBAL_SETTING_SYSTEM_ROLLUP_LATEST_5MIN = "report.system.rollup.latest.5min";
  public static final String GLOBAL_SETTING_DISKIO_ROLLUP_LATEST_5MIN = "report.diskio.rollup.latest.5min";

  public static final String GLOBAL_SETTING_CMS_IP = "cms.ip";
  public static final String GLOBAL_SETTING_CMS_TOKEN = "cms.token";
  public static final String GLOBAL_SETTING_CMS_STATE = "cms.state";
  public static final String GLOBAL_SETTING_LOCAL_LAST_HEARTBEAT = "local.last.heartbeat";

  public static final String GLOBAL_SETTING_DEVICE_SSO_PLATFORM_USER_ID = "device.sso.platform.user.id";
  public static final String GLOBAL_SETTING_DEVICE_SSO_LOGIN_LIST = "device.sso.login.list";

  public static final String GLOBAL_SETTING_GEOIP_CUSTOM_COUNTRY_SEQ_KEY = "geoip.custom.country.next.id";
  public static final String GLOBAL_SETTING_ANALYSIS_STANDARD_PROTOCOL_VERSION = "analysis.standard.protocol.version";

  public static final String GLOBAL_SETTING_SA_CUSTOM_CATEGORY_SEQ_KEY = "sa.custom.category.next.id";
  public static final String GLOBAL_SETTING_SA_CUSTOM_SUBCATEGORY_SEQ_KEY = "sa.custom.subcategory.next.id";
  public static final String GLOBAL_SETTING_SA_CUSTOM_APPLICATION_SEQ_KEY = "sa.custom.application.next.id";

  public static final String MONITOR_METRIC_FS_SYS_USED_PCT = "fs_system_used_pct";

  public static final String CENTRAL_DEVICE_CMS = "0";
  public static final String CENTRAL_DEVICE_FPC = "1";

  public static final int ASSIGNMENT_ACTION_STOP = 1;
  public static final int ASSIGNMENT_ACTION_CONTINUE = 2;
  public static final int ASSIGNMENT_ACTION_CANCEL = 3;

  public static final String APPLIANCE_TRANSMITTASK_STATE_RUN = "0";
  public static final String APPLIANCE_TRANSMITTASK_STATE_STOP = "1";
  public static final String APPLIANCE_TRANSMITTASK_STATE_FINISH = "2";

  public static final String TASK_ASSIGNMENT_STATE_SUCCESS = "0";
  public static final String TASK_ASSIGNMENT_STATE_DOING = "1";
  public static final String TASK_ASSIGNMENT_STATE_FAILED = "2";
  public static final String TASK_ASSIGNMENT_STATE_WAIT = "3";
  public static final String TASK_ASSIGNMENT_STATE_CANCEL = "4";
  public static final String TASK_ASSIGNMENT_STATE_STOP = "5";

  public static final int REQUEST_PARAMETER_IS_EMPTY_CODE = 44001;
  public static final int ILLEGAL_PARAMETER_CODE = 44101;
  public static final int SYSTEM_ERROR_CODE = 90001;

  public static final String TRANSMIT_TASK_FILTER_TYPE_TUPLE = "0";
  public static final String TRANSMIT_TASK_FILTER_TYPE_BPF = "1";
  public static final String TRANSMIT_TASK_FILTER_TYPE_MIX = "2";

  public static final String TRANSMIT_TASK_MODE_FILE_PCAP = "0";
  public static final String TRANSMIT_TASK_MODE_REPLAY = "1";
  public static final String TRANSMIT_TASK_MODE_FILE_PCAPNG = "2";

  public static final String METRIC_DISK_IO_READ = "read";
  public static final String METRIC_DISK_IO_WRITE = "write";

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

  public static final String FPC_DATABASE = "fpc";

  public static final String IPV4_TO_IPV6_PREFIX = "::ffff:";

  public static final String TABLE_METRIC_NETWORK_DATA_RECORD = "d_fpc_metric_network_data_record";
  public static final String TABLE_METRIC_NETIF_DATA_RECORD = "d_fpc_metric_netif_data_record";
  public static final String TABLE_METRIC_SERVICE_DATA_RECORD = "d_fpc_metric_service_data_record";
  public static final String TABLE_METRIC_APP_DATA_RECORD = "d_fpc_metric_application_data_record";
  public static final String TABLE_METRIC_HOSTGROUP_DATA_RECORD = "d_fpc_metric_hostgroup_data_record";
  public static final String TABLE_METRIC_LOCATION_DATA_RECORD = "d_fpc_metric_location_data_record";
  public static final String TABLE_METRIC_L2DEVICE_DATA_RECORD = "d_fpc_metric_l2device_data_record";
  public static final String TABLE_METRIC_L3DEVICE_DATA_RECORD = "d_fpc_metric_l3device_data_record";
  public static final String TABLE_METRIC_PORT_DATA_RECORD = "d_fpc_metric_port_data_record";
  public static final String TABLE_METRIC_L7PROTOCOL_DATA_RECORD = "d_fpc_metric_l7protocol_data_record";
  public static final String TABLE_METRIC_MONITOR_DATA_RECORD = "d_fpc_metric_monitor_data_record";
  public static final String TABLE_METRIC_DHCP_DATA_RECORD = "d_fpc_metric_dhcp_data_record";
  public static final String TABLE_METRIC_IP_CONVERSATION_DATA_RECORD = "d_fpc_metric_ip_conversation_data_record";
  public static final String TABLE_METRIC_DSCP_DATA_RECORD = "d_fpc_metric_dscp_data_record";
  public static final String TABLE_METRIC_DISK_IO_DATA_RECORD = "d_fpc_metric_disk_io_data_record";

  public static final String GLOBAL_SETTING_DR_ROLLUP_LATEST_5MIN = "data-record.rollup.latest.5min";
  public static final String GLOBAL_SETTING_DR_ROLLUP_LATEST_1HOUR = "data-record.rollup.latest.1hour";
  public static final String GLOBAL_SETTING_DR_CLEAN_LATEST_TIME = "data-record.clean.latest.time";

  public static final String METRIC_NPM_ALL_AGGSFILED = "all";

  // 日志老化时间(流日志, 元数据), 理论上最长1小时, 但有可能不精准, 所以此处设置为2小时
  public static final long ENGINE_LOG_AGINGTIME_MILLS = 2 * 60 * 60 * 1000;

  public static final String METADATA_CONDITION_IP_HOSTGROUP_PREFIX = "host-group-id:";
  public static final String METADATA_CONDITION_ID_ANALYSIS_RESULT_PREFIX = "analysis-result-id:";

  public static final String SCENARIO_CUSTOM_TEMPLATE_PREFIX = "custom_";

  public static final String SENSOR_ALARM_COUNT = "sensor_alarm_count";

  public static final String TCP_ESTABLISH_SUCCESS_RATE = "tcp_establish_success_rate";
  public static final String CONCURRENT_SESSIONS = "concurrent_sessions";
  public static final String ESTABLISHED_SESSIONS = "established_sessions";
  public static final String TOTAL_BYTES = "total_bytes";

  public static final String SYSTEM_FREESPACE_METRIC = "system_fs_metric";
  public static final String INDEX_FREESPACE_METRIC = "index_fs_metric";
  public static final String METADATA_FREESPACE_METRIC = "metadata_fs_metric";
  public static final String METADATA_HOT_FREESPACE_METRIC = "metadata_hot_fs_metric";
  public static final String PACKET_FREESPACE_METRIC = "packet_fs_metric";
  public static final String DISK_IO = "disk_io";

  public static final String GLOBAL_SETTING_SENDUP_ALARM_CURSOR = "sendup.alarm.cursor";
  public static final String GLOBAL_SETTING_SENDUP_LOG_CURSOR = "sendup.log.cursor";
  
  /**
   * sensor engine restapi
   */
  // 数据包分析(列表、统计、数据包文件路径、下载)
  public static final String REST_ENGINE_PACKETS_QUERY = "/fpc-fs/v1/packets/lists";
  public static final String REST_ENGINE_PACKETS_QUERY_STOP = "/fpc-fs/v1/packets/lists/stop";
  public static final String REST_ENGINE_PACKETS_REFINE = "/fpc-fs/v1/packets/refines";
  public static final String REST_ENGINE_PACKETS_REFINE_STOP = "/fpc-fs/v1/packets/refines/stop";
  public static final String REST_ENGINE_PACKETS_PATH = "/fpc-fs/v1/packets/fs-paths";
  public static final String REST_ENGINE_PACKETS_DOWNLOAD = "/fpc-fs/v1/packets/binarys";

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

  private CenterConstants() {
    throw new IllegalStateException("Utility class");
  }

}
