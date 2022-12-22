package com.machloop.fpc.cms.common;

public final class FpcCmsConstants {


  public static final String DEVICE_NETIF_CATEGORY_MGMT = "0";
  public static final String DEVICE_NETIF_CATEGORY_INGEST = "1";
  public static final String DEVICE_NETIF_CATEGORY_TRANSMIT = "2";

  public static final int RESULT_SUCCESS_CODE = 0;
  public static final String RESULT_SUCCESS_DETAIL = "success";

  public static final String CMS_TOKEN = "fpc.cms.grpc.token";
  public static final String CMS_MAX_SERIES = "fpc.cms.max.series";

  public static final String DEVICE_TYPE_TFA = "tfa";
  public static final String DEVICE_TYPE_CMS = "cms";

  public static final String SENSOR_TYPE_NORMAL = "0";
  public static final String SENSOR_TYPE_SOFTWARE = "1";

  public static final String CONNECT_STATUS_NORMAL = "0";
  public static final String CONNECT_STATUS_ABNORMAL = "1";

  public static final int HEARTBEAT_INACTIVATION_MILLISECOND = 10 * 1000;

  public static final String LICENSE_NORMAL = "0";
  public static final String LICENSE_ABNORMALITY = "1";

  public static final String MESSAGE_TYPE_REGISTER = "0";
  public static final String MESSAGE_TYPE_HEARTBEAT = "1";

  public static final String ASSIGNMENT_TYPE_TASK = "0";

  public static final String SYNC_ACTION_ADD = "1";
  public static final String SYNC_ACTION_MODIFY = "2";
  public static final String SYNC_ACTION_DELETE = "3";
  public static final String SYNC_ACTION_BATCH_MODIFY = "4";
  public static final String SYNC_ACTION_BATCH_DELETE = "5";

  public static final String TASK_SOURCE_ASSIGNMENT = "assignment";

  public static final String TASK_ACTION_TYPE_ASSIGNMENT = "0";
  public static final String TASK_ACTION_TYPE_DELETE = "1";

  public static final String SENDUP_TYPE_SYSTEM_METRIC = "0";
  public static final String SENDUP_TYPE_LOG_ALARM = "1";
  public static final String SENDUP_TYPE_NETWORK = "2";
  public static final String SENDUP_TYPE_SENSOR = "3";
  public static final String SENDUP_TYPE_CMS = "4";
  public static final String RESEND_TYPE_SYSTEM_METRIC = "10";
  public static final String RESEND_TYPE_LOG_ALARM = "11";

  public static final String STATISTIC_TYPE_SYSTEM_METRIC = "0";
  public static final String STATISTIC_TYPE_LOG_ALARM = "1";

  public static final String MQ_TOPIC_CMS_FULL_ASSIGNMENT = "cmsFullAssginTopic";

  public static final String MQ_TOPIC_CMS_ASSIGNMENT = "cmsAssginTopic";
  public static final String MQ_TOPIC_FPC_SENDUP = "fpcSendupTopic";
  public static final String MQ_TOPIC_CMS_SENDUP = "cmsSendupTopic";

  // exception tag
  public static final String MQ_TAG_SIGNATURE = "signature";
  public static final String MQ_TAG_RESETOFFSET = "resetoffset";
  public static final String MQ_TAG_SSO = "sso";

  // configuration tag
  public static final String MQ_TAG_LOGICALSUBNET = "logicalSubnet";
  public static final String MQ_TAG_SERVICE = "service";
  public static final String MQ_TAG_SERVICE_LINK = "serviceLink";
  public static final String MQ_TAG_INGESTPOLICY = "ingestPolicy";
  public static final String MQ_TAG_FILTERPOLICY = "filterPolicy";
  public static final String MQ_TAG_FILTERRULE = "filterRule";
  public static final String MQ_TAG_EXTERNALRECEIVER = "externalReceiver";
  public static final String MQ_TAG_SENDRULE = "sendRule";
  public static final String MQ_TAG_SENDPOLICY = "sendPolicy";
  public static final String MQ_TAG_HOSTGROUP = "hostgroup";

  public static final String MQ_TAG_DOMAIN_WHITE_LIST = "domainWhiteList";
  public static final String MQ_TAG_ALERT = "alert";
  public static final String MQ_TAG_SAKNOWLEDGE = "saKnowledge";
  public static final String MQ_TAG_GEOKNOWLEDGE = "geoKnowledge";
  public static final String MQ_TAG_GEOCUSTOM = "geoCustom";
  public static final String MQ_TAG_GEOIPSETTING = "geoIpSetting";
  public static final String MQ_TAG_CUSTOMCATEGORY = "customCategory";
  public static final String MQ_TAG_CUSTOMSUBCATEGORY = "customSubCategory";
  public static final String MQ_TAG_CUSTOMAPPLICATION = "cumtomApplication";
  public static final String MQ_TAG_NETWORKPOLICY = "networkPolicy";
  public static final String MQ_TAG_METRICSETTING = "metricSetting";
  public static final String MQ_TAG_SURICATA = "suricata";
  public static final String MQ_TAG_SURICATA_RULE_CLASSTYPE = "suricataRuleClasstype";
  public static final String MQ_TAG_CUSTOMTIME = "customTime";

  public static final String ROCKETMQ_PRODUCER_MAX_MESSAGE_SIZE = "rocketmq.producer.max.message.size";
  public static final String ROCKETMQ_BROKER_NAME = "rocketmq.broker.name";

  public static final int METRIC_TYPE_APPLICATION_CATEGORY = 0;
  public static final int METRIC_TYPE_APPLICATION_SUBCATEGORY = 1;
  public static final int METRIC_TYPE_APPLICATION_APP = 2;

  public static final String SA_TYPE_CUSTOM_CATEGORY = "custom-categorys";
  public static final String SA_TYPE_CUSTOM_SUBCATEGORY = "custom-subcategorys";
  public static final String SA_TYPE_CUSTOM_APPLICATION = "custom-applications";

  public static final int HOSTGROUP_MAX_IP_COUNT = 50;
  public static final int MAX_DOMAIN_COUNT = 100;
  public static final String APPLIANCE_NETWORK_POLICY_INGEST = "ingest";
  public static final String APPLIANCE_NETWORK_POLICY_FILTER = "filter";
  public static final String APPLIANCE_NETWORK_POLICY_SEND = "send";
  public static final String APPLIANCE_NETWORK_POLICY_STORAGE = "storage";
  public static final String APPLIANCE_EXTERNAL_RECEIVER_TYPE_MAIL = "0";
  public static final String APPLIANCE_EXTERNAL_RECEIVER_TYPE_SYSLOG = "1";
  public static final String APPLIANCE_EXTERNAL_RECEIVER_TYPE_KAFKA = "2";
  public static final String APPLIANCE_EXTERNAL_RECEIVER_TYPE_ZMQ = "3";

  public static final int APPLIANCE_FILTER_POLICY_ACTION_STORE = 0;
  public static final int APPLIANCE_FILTER_POLICY_ACTION_NOT_STORE = 1;

  public static final String APPLIANCE_NETWORK_UNIDIRECTION_FLOW = "0";
  public static final String APPLIANCE_NETWORK_BIDIRECTION_FLOW = "1";

  /********************************************
   * 安全告警相关
   *******************************************/
  public static final String SURICATA_ALERT_STATISTICS_TYPE_CLASSIFICATION_PROPORTION = "classification_proportion";
  public static final String SURICATA_ALERT_STATISTICS_TYPE_MITRE_TACTIC_PROPORTION = "mitre_tactic_proportion";

  public static final String PACKET_FILE_TYPE_PCAP = "pcap";
  public static final String PACKET_FILE_TYPE_PCAPNG = "pcapng";

  public static final String SOURCE_TYPE_NETWORK = "network";
  public static final String SOURCE_TYPE_SERVICE = "service";
  public static final String SOURCE_TYPE_PACKET_FILE = "packetFile";
  public static final String SOURCE_TYPE_NETWORK_GROUP = "networkGroup";

  public static final int ILLEGAL_PARAMETER_CODE = 44101;
  public static final int OBJECT_NOT_FOUND_CODE = 46001;

  public static final int FPC_NOT_FOUND_CODE = 50002;

  public static final String ANALYSIS_ABNORMAL_EVENT_SOURCE_DEFAULT = "0";
  public static final String ANALYSIS_ABNORMAL_EVENT_SOURCE_CUSTOM = "1";

  public static final String ALARM_CATEGORY_KNOWLEDGEBASE = "201";
  public static final String ALARM_CATEGORY_CMS = "301";

  public static final String ALERT_SYSLOG_PROTOCOL = "UDP";

  public static final String ALERT_SOURCE_TYPE_IP = "ipAddress";
  public static final String ALERT_SOURCE_TYPE_HOSTGROUP = "hostGroup";
  public static final String ALERT_SOURCE_TYPE_APPLICATION = "application";
  public static final String ALERT_SOURCE_TYPE_LOCATION = "location";

  public static final String ALERT_CATEGORY_THRESHOLD = "threshold";
  public static final String ALERT_CATEGORY_TREND = "trend";
  public static final String ALERT_CATEGORY_ADVANCED = "advanced";

  public static final String ANALYSIS_CONFIG_PROTOCOL_SOURCE_DEFAULT = "0";
  public static final String ANALYSIS_CONFIG_PROTOCOL_SOURCE_CUSTOM = "1";

  public static final String METRIC_TYPE_DHCP_CLIENT = "client";
  public static final String METRIC_TYPE_DHCP_SERVER = "server";
  public static final String METRIC_TYPE_DHCP_MESSAGE_TYPE = "messageType";

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

  public static final String APPLIANCE_TRANSMITTASK_STATE_RUN = "0";
  public static final String APPLIANCE_TRANSMITTASK_STATE_STOP = "1";
  public static final String APPLIANCE_TRANSMITTASK_STATE_FINISH = "2";

  public static final String TRANSMIT_TASK_MODE_FILE_PCAP = "0";
  public static final String TRANSMIT_TASK_MODE_REPLAY = "1";
  public static final String TRANSMIT_TASK_MODE_FILE_PCAPNG = "2";
  public static final String TRANSMIT_TASK_MODE_FILE_EXTERNAL_STORAGE = "3";

  public static final String DATABASE_FPC = "fpc";

  private FpcCmsConstants() {
    throw new IllegalStateException("Utility class");
  }
}
