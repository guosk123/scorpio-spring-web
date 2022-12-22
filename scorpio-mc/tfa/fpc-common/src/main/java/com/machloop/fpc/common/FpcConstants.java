package com.machloop.fpc.common;

/**
 * 
 * @author liumeng
 *
 * create at 2018年12月11日, fpc-common
 */
public final class FpcConstants {

  public static final String DEVICE_NETIF_CATEGORY_MGMT = "0";
  public static final String DEVICE_NETIF_CATEGORY_INGEST = "1";
  public static final String DEVICE_NETIF_CATEGORY_TRANSMIT = "2";
  public static final String DEVICE_NETIF_CATEGORY_NETFLOW = "3";
  public static final String DEVICE_NETIF_CATEGORY_DEFAULT = "";

  public static final String DEVICE_TYPE_NETIF = "1";
  public static final String DEVICE_TYPE_DEVICE = "0";

  public static final String TRANSMIT_TASK_FILTER_TYPE_TUPLE = "0";
  public static final String TRANSMIT_TASK_FILTER_TYPE_BPF = "1";
  public static final String TRANSMIT_TASK_FILTER_TYPE_MIX = "2";
  
  public static final String TRANSMIT_TASK_MODE_FILE_PCAP = "0";
  public static final String TRANSMIT_TASK_MODE_REPLAY = "1";
  public static final String TRANSMIT_TASK_MODE_FILE_PCAPNG = "2";
  public static final String TRANSMIT_TASK_MODE_FILE_EXTERNAL_STORAGE = "3";

  public static final String TRANSMIT_TASK_IPTUNNEL_MODE_GRE = "gre";
  public static final String TRANSMIT_TASK_IPTUNNEL_MODE_VXLAN = "vxlan";

  public static final String PACKET_FILE_TYPE_PCAP = "pcap";
  public static final String PACKET_FILE_TYPE_PCAPNG = "pcapng";

  public static final String APPLIANCE_TRANSMITTASK_STATE_RUN = "0";
  public static final String APPLIANCE_TRANSMITTASK_STATE_STOP = "1";
  public static final String APPLIANCE_TRANSMITTASK_STATE_FINISH = "2";

  public static final String APPLIANCE_FILTER_POLICY_CONDITION_TYPE = "sa-application-id";

  public static final int APPLIANCE_FILTER_POLICY_ACTION_STORE = 0;
  public static final int APPLIANCE_FILTER_POLICY_ACTION_NOT_STORE = 1;

  public static final String APPLIANCE_NETWORK_UNIDIRECTION_FLOW = "0";
  public static final String APPLIANCE_NETWORK_BIDIRECTION_FLOW = "1";

  public static final String APPLIANCE_NETWORK_NETIF_DIRECTION_UPSTREAM = "upstream";
  public static final String APPLIANCE_NETWORK_NETIF_DIRECTION_DOWNSTREAM = "downstream";
  public static final String APPLIANCE_NETWORK_NETIF_DIRECTION_HYBRID = "hybrid";

  public static final String APPLIANCE_NETWORK_POLICY_INGEST = "ingest";
  public static final String APPLIANCE_NETWORK_POLICY_FILTER = "filter";
  public static final String APPLIANCE_NETWORK_POLICY_FORWARD = "forward";
  public static final String APPLIANCE_NETWORK_POLICY_SEND = "send";
  public static final String APPLIANCE_NETWORK_POLICY_STORAGE = "storage";

  public static final String APPLIANCE_PACKET_ANALYSIS_TASK_POLICY_INGEST = "ingest";
  public static final String APPLIANCE_PACKET_ANALYSIS_TASK_POLICY_FILTER = "filter";
  public static final String APPLIANCE_PACKET_ANALYSIS_TASK_POLICY_FORWARD = "forward";
  public static final String APPLIANCE_PACKET_ANALYSIS_TASK_POLICY_SEND = "send";
  public static final String APPLIANCE_SUBNET_TYPE_IP = "ip";
  public static final String APPLIANCE_SUBNET_TYPE_MAC = "mac";
  public static final String APPLIANCE_SUBNET_TYPE_VLAN = "vlan";
  public static final String APPLIANCE_SUBNET_TYPE_MPLS = "mpls";
  public static final String APPLIANCE_SUBNET_TYPE_GRE = "gre";
  public static final String APPLIANCE_SUBNET_TYPE_VXLAN = "vxlan";

  public static final String APPLIANCE_EXTERNAL_RECEIVER_TYPE_MAIL = "0";
  public static final String APPLIANCE_EXTERNAL_RECEIVER_TYPE_SYSLOG = "1";
  public static final String APPLIANCE_EXTERNAL_RECEIVER_TYPE_KAFKA = "2";
  public static final String APPLIANCE_EXTERNAL_RECEIVER_TYPE_ZMQ = "3";


  public static final String ANALYSIS_TASK_STATE_RUN = "0";
  public static final String ANALYSIS_TASK_STATE_STOP = "1";
  public static final String ANALYSIS_TASK_STATE_FINISH = "2";

  public static final String ANALYSIS_CONFIG_PROTOCOL_SOURCE_DEFAULT = "0";
  public static final String ANALYSIS_CONFIG_PROTOCOL_SOURCE_CUSTOM = "1";

  public static final String ANALYSIS_ABNORMAL_EVENT_SOURCE_DEFAULT = "0";
  public static final String ANALYSIS_ABNORMAL_EVENT_SOURCE_CUSTOM = "1";

  public static final String EXTERNAL_STORAGE_USAGE_TRANSMIT = "transmit_task";
  public static final String EXTERNAL_STORAGE_USAGE_PACKET_FILE = "packet_file_task";

  public static final String SOURCE_TYPE_NETWORK = "network";
  public static final String SOURCE_TYPE_SERVICE = "service";
  public static final String SOURCE_TYPE_PACKET_FILE = "packetFile";

  public static final String METRIC_TYPE_DHCP_CLIENT = "client";
  public static final String METRIC_TYPE_DHCP_SERVER = "server";
  public static final String METRIC_TYPE_DHCP_MESSAGE_TYPE = "messageType";

  public static final int METRIC_TYPE_APPLICATION_CATEGORY = 0;
  public static final int METRIC_TYPE_APPLICATION_SUBCATEGORY = 1;
  public static final int METRIC_TYPE_APPLICATION_APP = 2;

  public static final String SA_TYPE_CUSTOM_CATEGORY = "custom-categorys";
  public static final String SA_TYPE_CUSTOM_SUBCATEGORY = "custom-subcategorys";
  public static final String SA_TYPE_CUSTOM_APPLICATION = "custom-applications";

  public static final String LEVEL_LOW = "0";
  public static final String LEVEL_MIDDLE = "1";
  public static final String LEVEL_HIGH = "2";

  public static final String HTTP_ACTION_SEND = "0";
  public static final String HTTP_ACTION_FILTER = "1";

  public static final int HOSTGROUP_MAX_IP_COUNT = 50;

  public static final int MAX_DOMAIN_COUNT = 100;

  public static final String ALERT_CATEGORY_THRESHOLD = "threshold";
  public static final String ALERT_CATEGORY_TREND = "trend";
  public static final String ALERT_CATEGORY_ADVANCED = "advanced";

  public static final String ALERT_SOURCE_TYPE_IP = "ipAddress";
  public static final String ALERT_SOURCE_TYPE_HOSTGROUP = "hostGroup";
  public static final String ALERT_SOURCE_TYPE_APPLICATION = "application";
  public static final String ALERT_SOURCE_TYPE_LOCATION = "location";

  public static final String ALERT_SYSLOG_PROTOCOL = "UDP";

  public static final String OFFLINE_ANALYSIS_TASK_SOURCE_UPLOAD = "UPLOAD";
  public static final String OFFLINE_ANALYSIS_TASK_SOURCE_EXTERNAL_STORAGE = "EXTERNAL_STORAGE";

  public static final String SURICATA_ALERT_STATISTICS_TYPE_TOP_TARGET_HOST = "top_target_host";
  public static final String SURICATA_ALERT_STATISTICS_TYPE_TOP_ORIGIN_IP = "top_origin_ip";
  public static final String SURICATA_ALERT_STATISTICS_TYPE_TOP_ALARM_ID = "top_alarm_id";
  public static final String SURICATA_ALERT_STATISTICS_TYPE_CLASSIFICATION_PROPORTION = "classification_proportion";
  public static final String SURICATA_ALERT_STATISTICS_TYPE_MITRE_TACTIC_PROPORTION = "mitre_tactic_proportion";
  public static final String SURICATA_ALERT_STATISTICS_TYPE_ALARM_TREND = "alarm_trend";
  public static final String SURICATA_ALERT_STATISTICS_TYPE_TOP_MINING_HOST = "top_mining_host";
  public static final String SURICATA_ALERT_STATISTICS_TYPE_TOP_MINING_DOMAIN = "top_mining_domain";
  public static final String SURICATA_ALERT_STATISTICS_TYPE_TOP_MINING_POOL_ADDRESS = "top_mining_pool_address";
  public static final String SURICATA_ALERT_STATISTICS_TYPE_MINING_ALARM_TREND = "mining_alarm_trend";
  public static final String SURICATA_ALERT_STATISTICS_TYPE_SOURCE_ALARM_TREND = "source_alarm_trend";

  /************************************************************
   *
   *************************************************************/

  public static final String STAT_NETIF_RRD_RX_BYTEPS = "_rx_byteps";
  public static final String STAT_NETIF_RRD_RX_PPS = "_rx_pps";
  public static final String STAT_NETIF_RRD_TX_BYTEPS = "_tx_byteps";
  public static final String STAT_NETIF_RRD_TX_PPS = "_tx_pps";

  public static final String STAT_METRIC_CPU_RRD = "cpu";
  public static final String STAT_METRIC_MEMORY_RRD = "memory";

  public static final String MONITOR_METRIC_CPU_USED_PCT = "cpu_used_pct";
  public static final String MONITOR_METRIC_MEM_USED_PCT = "memory_used_pct";
  public static final String MONITOR_METRIC_FS_SYS_USED_PCT = "fs_system_used_pct";
  public static final String MONITOR_METRIC_FS_IDX_USED_PCT = "fs_index_used_pct";
  public static final String MONITOR_METRIC_FS_METADATA_USED_PCT = "fs_metadata_used_pct";
  public static final String MONITOR_METRIC_FS_DATA_USED_PCT = "fs_data_used_pct";
  public static final String MONITOR_METRIC_FS_DATA_USED = "fs_data_used_byte";
  public static final String MONITOR_METRIC_FS_DATA_TOTAL = "fs_data_total_byte";
  public static final String MONITOR_METRIC_FS_CACHE_USED_PCT = "fs_cache_used_pct";
  public static final String MONITOR_METRIC_FS_CACHE_USED = "fs_cache_used_byte";
  public static final String MONITOR_METRIC_FS_CACHE_TOTAL = "fs_cache_total_byte";
  public static final String MONITOR_METRIC_DATA_OLDEST_TIMESTAMP = "data_oldest_time";
  public static final String MONITOR_METRIC_DATA_LAST24_TOTAL_BYTE = "data_last24_total_byte";
  public static final String MONITOR_METRIC_DATA_PREDICT_TOTAL_DAY = "data_predict_total_day";
  public static final String MONITOR_METRIC_CACHE_FILE_AVG_BYTE = "cache_file_avg_byte";
  public static final String MONITOR_METRIC_FS_STORE_TOTAL = "fs_store_total_byte";
  public static final String MONITOR_METRIC_FS_SYSTEM_TOTAL = "fs_system_total_byte";
  public static final String MONITOR_METRIC_FS_INDEX_TOTAL = "fs_index_total_byte";
  public static final String MONITOR_METRIC_FS_METADATA_TOTAL = "fs_metadata_total_byte";
  public static final String MONITOR_METRIC_FS_METADATA_HOT_TOTAL = "fs_metadata_hot_total_byte";


  /************************************************************
   * 
   *************************************************************/

  public static final String BASELINE_SETTING_SOURCE_ALERT = "alert";
  public static final String BASELINE_SETTING_SOURCE_NPM = "npm";

  public static final String BASELINE_CATEGORY_BANDWIDTH = "bandwidth";
  public static final String BASELINE_CATEGORY_FLOW = "flow";
  public static final String BASELINE_CATEGORY_PACKET = "packet";
  public static final String BASELINE_CATEGORY_RESPONSELATENCY = "responseLatency";

  public static final String BASELINE_WINDOWING_MODEL_MINUTE_OF_DAY = "minute_of_day";
  public static final String BASELINE_WINDOWING_MODEL_FIVE_MINUTE_OF_DAY = "five_minute_of_day";
  public static final String BASELINE_WINDOWING_MODEL_HOUR_OF_DAY = "hour_of_day";
  public static final String BASELINE_WINDOWING_MODEL_MINUTE_OF_WEEK = "minute_of_week";
  public static final String BASELINE_WINDOWING_MODEL_FIVE_MINUTE_OF_WEEK = "five_minute_of_week";
  public static final String BASELINE_WINDOWING_MODEL_HOUR_OF_WEEK = "hour_of_week";
  public static final String BASELINE_WINDOWING_MODEL_LAST_N_MINUTES = "last_n_minutes";
  public static final String BASELINE_WINDOWING_MODEL_LAST_N_FIVE_MINUTES = "last_n_five_minutes";
  public static final String BASELINE_WINDOWING_MODEL_LAST_N_HOURS = "last_n_hours";


  public static final String LOG_CATEGORY_LOG_ALARM_ARCHIVE = "2";
  public static final String LOG_CATEGORY_SYSTEM_BACKUP = "3";

  public static final String ALARM_CATEGORY_KNOWLEDGEBASE = "201";
  public static final String ALARM_CATEGORY_CMS = "301";

  public static final int ILLEGAL_PARAMETER_CODE = 44101;
  public static final int OBJECT_NOT_FOUND_CODE = 46001;

  public static final String OPERAND_NOTIN_FIELD = "0";
  public static final String CHECK_SUCCESS = "1";

  public static final String SYNC_ACTION_ADD = "1";
  public static final String SYNC_ACTION_MODIFY = "2";
  public static final String SYNC_ACTION_DELETE = "3";

  private FpcConstants() {
    throw new IllegalStateException("Utility class");
  }
}
