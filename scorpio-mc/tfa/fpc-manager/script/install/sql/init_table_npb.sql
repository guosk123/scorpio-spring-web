-- ----------------------------
-- Table structure for fpc_appliance_collect_policy
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_collect_policy" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "order_no" serial8,
  "ip_address" varchar(32) COLLATE "pg_catalog"."default" DEFAULT '',
  "ip_start" int8 DEFAULT 0,
  "ip_end" int8 DEFAULT 0,
  "l7_protocol_id" varchar(256) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "level" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "state" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_collect_policy_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_collect_policy"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_collect_policy"."name" IS '名称';
COMMENT ON COLUMN "public"."fpc_appliance_collect_policy"."order_no" IS '序号';
COMMENT ON COLUMN "public"."fpc_appliance_collect_policy"."ip_address" IS 'IP地址';
COMMENT ON COLUMN "public"."fpc_appliance_collect_policy"."ip_start" IS '起始IP地址';
COMMENT ON COLUMN "public"."fpc_appliance_collect_policy"."ip_end" IS '结束IP地址';
COMMENT ON COLUMN "public"."fpc_appliance_collect_policy"."l7_protocol_id" IS '协议ID集合（SA规则库协议ID集合，csv格式）';
COMMENT ON COLUMN "public"."fpc_appliance_collect_policy"."level" IS '级别（0：低；1：中；2：高）';
COMMENT ON COLUMN "public"."fpc_appliance_collect_policy"."state" IS '是否启用（0：未启用；1：启用）';
COMMENT ON COLUMN "public"."fpc_appliance_collect_policy"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_collect_policy"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_collect_policy"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_collect_policy"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_collect_policy"."operator_id" IS '操作人id';

CREATE SEQUENCE IF NOT EXISTS order_no_seq
START WITH 1
INCREMENT BY 1
NO MINVALUE
NO MAXVALUE
CACHE 1;
alter table "public"."fpc_appliance_collect_policy" alter column order_no set default nextval('order_no_seq');

-- ----------------------------
-- Table structure for fpc_appliance_receiver_setting
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_receiver_setting" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "protocol_topic" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "http_action" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
  "http_action_exculde_uri_suffix" varchar(32) COLLATE "pg_catalog"."default" DEFAULT '',
  "receiver_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "receiver_type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "state" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_receiver_setting_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_receiver_setting"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_setting"."name" IS '名称';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_setting"."protocol_topic" IS 'topic名称';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_setting"."http_action" IS 'http动作（0：发送；1：过滤）';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_setting"."http_action_exculde_uri_suffix" IS 'http详情，URI后缀过滤';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_setting"."receiver_id" IS '接收者ID';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_setting"."receiver_type" IS '接收者类型';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_setting"."state" IS '是否启用（0：未启用；1：启用）';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_setting"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_setting"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_setting"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_setting"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_setting"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_receiver_kafka
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_receiver_kafka" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "receiver_address" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "kerberos_certification" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 1,
  "keytab_file_path" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "key_restore_time" int4 DEFAULT 0,
  "sasl_kerberos_service_name" varchar(32) COLLATE "pg_catalog"."default" DEFAULT '',
  "sasl_kerberos_principal" varchar(32) COLLATE "pg_catalog"."default" DEFAULT '',
  "security_protocol" varchar(32) COLLATE "pg_catalog"."default" DEFAULT '',
  "authentication_mechanism" varchar(32) COLLATE "pg_catalog"."default" DEFAULT '',
  "state" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_receiver_kafka_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_receiver_kafka"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_kafka"."name" IS '名称';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_kafka"."receiver_address" IS '接收地址';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_kafka"."kerberos_certification" IS 'KERBEROS认证是否启用（0：未启用；1：启用）';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_kafka"."keytab_file_path" IS 'keytab文件路径';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_kafka"."key_restore_time" IS 'key尝试恢复时间，单位为ms';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_kafka"."sasl_kerberos_service_name" IS 'sasl.kerberos.service.name';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_kafka"."sasl_kerberos_principal" IS 'sasl.kerberos.principal';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_kafka"."security_protocol" IS '安全协议';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_kafka"."authentication_mechanism" IS '鉴权机制';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_kafka"."state" IS '是否启用（0：未启用；1：启用）';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_kafka"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_kafka"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_kafka"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_kafka"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_receiver_kafka"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_service_decrypt
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_service_decrypt" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "ip_address" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "port" varchar(8) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "protocol" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "cert_content" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "cert_hash" varchar(128) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_decriceypt_serv_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_service_decrypt"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_service_decrypt"."ip_address" IS 'IP地址';
COMMENT ON COLUMN "public"."fpc_appliance_service_decrypt"."port" IS '端口';
COMMENT ON COLUMN "public"."fpc_appliance_service_decrypt"."protocol" IS '协议';
COMMENT ON COLUMN "public"."fpc_appliance_service_decrypt"."cert_content" IS '证书内容';
COMMENT ON COLUMN "public"."fpc_appliance_service_decrypt"."cert_hash" IS '证书哈希值';
COMMENT ON COLUMN "public"."fpc_appliance_service_decrypt"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_service_decrypt"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_service_decrypt"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_service_decrypt"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_service_decrypt"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_analysis_scenario_template
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_analysis_scenario_template" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "data_source" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "filter_dsl" text COLLATE "pg_catalog"."default" DEFAULT '',
  "filter_spl" text COLLATE "pg_catalog"."default" DEFAULT '',
  "function" text COLLATE "pg_catalog"."default" DEFAULT '',
  "avg_time_interval" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "slice_time_interval" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "group_by" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  CONSTRAINT "fpc_analysis_scenario_template_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_analysis_scenario_template"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_template"."name" IS '模板名称';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_template"."filter_dsl" IS '过滤条件表达式';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_template"."filter_spl" IS '过滤条件ES搜索json';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_template"."data_source" IS '数据源';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_template"."function" IS '计算方法（内容为json）';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_template"."avg_time_interval" IS '按时间平均时的时间间隔（秒，0表示不平均）';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_template"."slice_time_interval" IS '按时间切片时的时间间隔（秒，0表示不切片）';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_template"."group_by" IS '分组字段';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_template"."description" IS '备注';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_template"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_template"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_template"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_template"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_template"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_analysis_ti_threatbook
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_analysis_ti_threatbook"(
    "id"    varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "ioc_raw" varchar(128) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "basic_tag" varchar(256) COLLATE"pg_catalog"."default" NOT NULL DEFAULT '',
    "intel_type" varchar(256) COLLATE"pg_catalog"."default" NOT NULL DEFAULT '',
    "source" varchar(128) COLLATE"pg_catalog"."default" NOT NULL DEFAULT '',
    "tag" varchar(256) COLLATE"pg_catalog"."default" NOT NULL DEFAULT '',
    "time" timestamptz(6) ,
    "ioc_type" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    CONSTRAINT "fpc_analysis_ti_threatbook_pkey" PRIMARY KEY ("id")
    );
COMMENT ON COLUMN "public"."fpc_analysis_ti_threatbook"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_analysis_ti_threatbook"."ioc_raw" IS 'IOC内容';
COMMENT ON COLUMN "public"."fpc_analysis_ti_threatbook"."basic_tag" IS '基础标签';
COMMENT ON COLUMN "public"."fpc_analysis_ti_threatbook"."intel_type" IS '情报类型';
COMMENT ON COLUMN "public"."fpc_analysis_ti_threatbook"."source" IS '来源';
COMMENT ON COLUMN "public"."fpc_analysis_ti_threatbook"."tag" IS '标签';
COMMENT ON COLUMN "public"."fpc_analysis_ti_threatbook"."time" IS '时间';
COMMENT ON COLUMN "public"."fpc_analysis_ti_threatbook"."ioc_type" IS 'IOC类型';

-- ----------------------------
-- Table structure for fpc_analysis_threat_intelligence
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_analysis_threat_intelligence" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "content" varchar(1024) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "threat_category" varchar(1024) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "timestamp" timestamptz(6),
  CONSTRAINT "fpc_analysis_threat_intelligence_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_analysis_threat_intelligence"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_analysis_threat_intelligence"."type" IS '情报类型';
COMMENT ON COLUMN "public"."fpc_analysis_threat_intelligence"."content" IS '情报内容';
COMMENT ON COLUMN "public"."fpc_analysis_threat_intelligence"."threat_category" IS '威胁分类';
COMMENT ON COLUMN "public"."fpc_analysis_threat_intelligence"."description" IS '备注';
COMMENT ON COLUMN "public"."fpc_analysis_threat_intelligence"."timestamp" IS '时间戳';

-- ----------------------------
-- Table structure for fpc_analysis_abnormal_event_rule
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_analysis_abnormal_event_rule" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "type" int4 NOT NULL,
  "content" varchar(1024) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "source" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "status" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "timestamp" timestamptz(6),
  CONSTRAINT "fpc_analysis_abnormal_event_rule_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_analysis_abnormal_event_rule"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_analysis_abnormal_event_rule"."type" IS '事件类型';
COMMENT ON COLUMN "public"."fpc_analysis_abnormal_event_rule"."content" IS '事件内容';
COMMENT ON COLUMN "public"."fpc_analysis_abnormal_event_rule"."source" IS '事件来源（0：预置；1：自定义）';
COMMENT ON COLUMN "public"."fpc_analysis_abnormal_event_rule"."status" IS '启用状态（0：停用，1：启用）';
COMMENT ON COLUMN "public"."fpc_analysis_abnormal_event_rule"."description" IS '备注';
COMMENT ON COLUMN "public"."fpc_analysis_abnormal_event_rule"."operator_id" IS '操作人id';
COMMENT ON COLUMN "public"."fpc_analysis_abnormal_event_rule"."timestamp" IS '时间戳';

-- ----------------------------
-- Table structure for fpc_analysis_standard_protocol
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_analysis_standard_protocol" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "l7_protocol_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "ip_protocol" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "port" varchar(8) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "source" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_analysis_standard_protocol_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_analysis_standard_protocol"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_analysis_standard_protocol"."l7_protocol_id" IS '应用层协议ID';
COMMENT ON COLUMN "public"."fpc_analysis_standard_protocol"."ip_protocol" IS '传输层协议';
COMMENT ON COLUMN "public"."fpc_analysis_standard_protocol"."port" IS '端口';
COMMENT ON COLUMN "public"."fpc_analysis_standard_protocol"."source" IS '配置来源（0：预置；1：自定义）';
COMMENT ON COLUMN "public"."fpc_analysis_standard_protocol"."description" IS '备注';
COMMENT ON COLUMN "public"."fpc_analysis_standard_protocol"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_analysis_standard_protocol"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_analysis_standard_protocol"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_analysis_standard_protocol"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_analysis_standard_protocol"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_analysis_scenario_task
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_analysis_scenario_task" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "analysis_start_time" timestamptz(6) NOT NULL,
  "analysis_end_time" timestamptz(6) NOT NULL,
  "type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "execution_start_time" timestamptz(6),
  "execution_end_time" timestamptz(6),
  "execution_progress" int4 DEFAULT 0,
  "execution_trace" text COLLATE "pg_catalog"."default" DEFAULT '',
  "state" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "deleted" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  CONSTRAINT "fpc_analysis_scenario_task_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_analysis_scenario_task"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_task"."name" IS '任务名称';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_task"."analysis_start_time" IS '分析起始时间';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_task"."analysis_end_time" IS '分析结束时间';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_task"."type" IS '分析任务场景类型';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_task"."description" IS '备注';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_task"."execution_start_time" IS '任务执行起始时间';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_task"."execution_end_time" IS '任务执行结束时间';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_task"."execution_progress" IS '执行进度（1~100）';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_task"."execution_trace" IS '任务执行摘要';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_task"."state" IS '任务状态（0：正常；1：停止；2：完成）';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_task"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_task"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_task"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_task"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_analysis_scenario_task"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_analysis_suricata_rule_classtype
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_analysis_suricata_rule_classtype" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "classtype_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "name" varchar(256) COLLATE "pg_catalog"."default" NOT NULL,
  "deleted" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_analysis_suricata_rule_classtype_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule_classtype"."id" IS 'ID';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule_classtype"."classtype_in_cms_id" IS '规则分类在CMS中的ID';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule_classtype"."name" IS '分类名称';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule_classtype"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule_classtype"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule_classtype"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule_classtype"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule_classtype"."operator_id" IS '操作者ID';

-- ----------------------------
-- Table structure for fpc_analysis_mitre_attack
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_analysis_mitre_attack" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(256) COLLATE "pg_catalog"."default" NOT NULL,
  "parent_id" varchar(256) COLLATE "pg_catalog"."default" DEFAULT '',
  CONSTRAINT "fpc_analysis_mitre_attack_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_analysis_mitre_attack"."id" IS 'ID';
COMMENT ON COLUMN "public"."fpc_analysis_mitre_attack"."name" IS '中文名称';
COMMENT ON COLUMN "public"."fpc_analysis_mitre_attack"."parent_id" IS '父节点ID，填空为父节点(可拥有多个父节点，csv格式)';

-- ----------------------------
-- Table structure for fpc_analysis_suricata_rule
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_analysis_suricata_rule" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "suricata_rule_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "sid" int4 NOT NULL,
  "action" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "protocol" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "src_ip" text COLLATE "pg_catalog"."default" NOT NULL,
  "src_port" text COLLATE "pg_catalog"."default" NOT NULL,
  "direction" varchar(4) COLLATE "pg_catalog"."default" NOT NULL,
  "dest_ip" text COLLATE "pg_catalog"."default" NOT NULL,
  "dest_port" text COLLATE "pg_catalog"."default" NOT NULL,
  "msg" varchar(256) COLLATE "pg_catalog"."default" NOT NULL,
  "rev" int4 NOT NULL DEFAULT 1,
  "content" text COLLATE "pg_catalog"."default" NOT NULL,
  "priority" int4 DEFAULT 3,
  "classtype_id" varchar(64) COLLATE "pg_catalog"."default",
  "mitre_tactic_id" varchar(64) COLLATE "pg_catalog"."default",
  "mitre_technique_id" varchar(64) COLLATE "pg_catalog"."default",
  "cve" varchar(256) COLLATE "pg_catalog"."default",
  "cnnvd" varchar(256) COLLATE "pg_catalog"."default",
  "signature_severity" char(1) COLLATE "pg_catalog"."default" DEFAULT 2,
  "target" varchar(256) COLLATE "pg_catalog"."default",
  "threshold" varchar(256) COLLATE "pg_catalog"."default",
  "rule" text COLLATE "pg_catalog"."default" NOT NULL,
  "parse_state" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "parse_log" varchar(256) COLLATE "pg_catalog"."default",
  "state" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "source" varchar(32) COLLATE "pg_catalog"."default" DEFAULT 0,
  "deleted" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_analysis_suricata_rule_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."id" IS 'ID';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."suricata_rule_in_cms_id" IS '规则在CMS中的ID';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."sid" IS '规则ID';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."action" IS '动作，暂时默认只有alert，后续为可选枚举值';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."protocol" IS '协议，可选枚举值';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."src_ip" IS '源IP信息';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."src_port" IS '源端口信息';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."direction" IS '方向(->,<>)';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."dest_ip" IS '目的IP信息';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."dest_port" IS '目的端口信息';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."msg" IS '描述信息';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."rev" IS '版本号，界面新建时默认为1，导入时按照解析填入';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."content" IS '检测用规则';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."priority" IS '优先级(1-255)，默认3';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."classtype_id" IS '规则分类';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."mitre_tactic_id" IS '战术分类';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."mitre_technique_id" IS '战术分类下对应的技术分类';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."cve" IS 'CVE编号';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."cnnvd" IS 'CNNVD编号';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."signature_severity" IS '严重级别(0-3)';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."target" IS '受害方';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."threshold" IS '告警频率';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."rule" IS '将上述字段重组为完整的suricata规则';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."parse_state" IS '解析状态，刚导入时为0，解析成功为1，解析失败为2';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."parse_log" IS '解析状态为2时此字段有意义，将显示错误信息';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."state" IS '启用状态（0：停用；1启用）';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."source" IS '来源（0：系统内置；1：用户自定义；其他第三方）';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."deleted" IS '规则删除的时候，只需将此标志位置为1，delete操作将由suricata完成';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_analysis_suricata_rule"."operator_id" IS '操作者ID';
CREATE INDEX IF NOT EXISTS sid_index ON fpc_analysis_suricata_rule(sid);

-- ----------------------------
-- Table structure for fpc_analysis_pktanalysis_plugins
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_analysis_pktanalysis_plugins" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "protocol" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "file_name" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "parse_status" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "parse_log" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_analysis_pktanalysis_plugins_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_analysis_pktanalysis_plugins"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_analysis_pktanalysis_plugins"."protocol" IS '协议';
COMMENT ON COLUMN "public"."fpc_analysis_pktanalysis_plugins"."file_name" IS '文件名';
COMMENT ON COLUMN "public"."fpc_analysis_pktanalysis_plugins"."parse_status" IS '解析状态';
COMMENT ON COLUMN "public"."fpc_analysis_pktanalysis_plugins"."parse_log" IS '解析日志';
COMMENT ON COLUMN "public"."fpc_analysis_pktanalysis_plugins"."description" IS '描述';
COMMENT ON COLUMN "public"."fpc_analysis_pktanalysis_plugins"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_analysis_pktanalysis_plugins"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_analysis_pktanalysis_plugins"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_analysis_pktanalysis_plugins"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_analysis_pktanalysis_plugins"."operator_id" IS '操作人id';