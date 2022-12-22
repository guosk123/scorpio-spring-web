-- ----------------------------
-- Table structure for fpccms_analysis_abnormal_event_rule
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_analysis_abnormal_event_rule" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "type" int4 NOT NULL,
  "content" varchar(1024) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "source" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "status" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "timestamp" timestamptz(6),
  CONSTRAINT "fpccms_analysis_abnormal_event_rule_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_analysis_abnormal_event_rule"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_analysis_abnormal_event_rule"."type" IS '事件类型';
COMMENT ON COLUMN "public"."fpccms_analysis_abnormal_event_rule"."content" IS '事件内容';
COMMENT ON COLUMN "public"."fpccms_analysis_abnormal_event_rule"."source" IS '事件来源（0：预置；1：自定义）';
COMMENT ON COLUMN "public"."fpccms_analysis_abnormal_event_rule"."status" IS '启用状态（0：停用，1：启用）';
COMMENT ON COLUMN "public"."fpccms_analysis_abnormal_event_rule"."description" IS '备注';
COMMENT ON COLUMN "public"."fpccms_analysis_abnormal_event_rule"."operator_id" IS '操作人id';
COMMENT ON COLUMN "public"."fpccms_analysis_abnormal_event_rule"."timestamp" IS '时间戳';

-- ----------------------------
-- Table structure for fpccms_analysis_standard_protocol
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_analysis_standard_protocol" (
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
  CONSTRAINT "fpccms_analysis_standard_protocol_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_analysis_standard_protocol"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_analysis_standard_protocol"."l7_protocol_id" IS '应用层协议ID';
COMMENT ON COLUMN "public"."fpccms_analysis_standard_protocol"."ip_protocol" IS '传输层协议';
COMMENT ON COLUMN "public"."fpccms_analysis_standard_protocol"."port" IS '端口';
COMMENT ON COLUMN "public"."fpccms_analysis_standard_protocol"."source" IS '配置来源（0：预置；1：自定义）';
COMMENT ON COLUMN "public"."fpccms_analysis_standard_protocol"."description" IS '备注';
COMMENT ON COLUMN "public"."fpccms_analysis_standard_protocol"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_analysis_standard_protocol"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_analysis_standard_protocol"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_analysis_standard_protocol"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_analysis_standard_protocol"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_analysis_suricata_rule
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_analysis_suricata_rule" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "assign_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
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
    CONSTRAINT "fpccms_analysis_suricata_rule_pkey" PRIMARY KEY ("id")
)
;
CREATE INDEX IF NOT EXISTS sid_index ON fpccms_analysis_suricata_rule(sid);
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."id" IS 'ID';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."assign_id" IS '规则在上级cms中的ID';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."sid" IS '规则ID';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."action" IS '动作，暂时默认只有alert，后续为可选枚举值';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."protocol" IS '协议，可选枚举值';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."src_ip" IS '源IP信息';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."src_port" IS '源端口信息';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."direction" IS '方向(->,<>)';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."dest_ip" IS '目的IP信息';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."dest_port" IS '目的端口信息';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."msg" IS '描述信息';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."rev" IS '版本号，界面新建时默认为1，导入时按照解析填入';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."content" IS '检测用规则';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."priority" IS '优先级(1-255)，默认3';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."classtype_id" IS '规则分类';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."mitre_tactic_id" IS '战术分类';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."mitre_technique_id" IS '战术分类下对应的技术分类';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."cve" IS 'CVE编号';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."cnnvd" IS 'CNNVD编号';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."signature_severity" IS '严重级别(0-3)';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."target" IS '受害方';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."threshold" IS '告警频率';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."rule" IS '将上述字段重组为完整的suricata规则';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."parse_state" IS '解析状态，刚导入时为0，解析成功为1，解析失败为2';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."parse_log" IS '解析状态为2时此字段有意义，将显示错误信息';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."state" IS '启用状态（0：停用；1启用）';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."source" IS '来源（0：系统内置；1：用户自定义；其他第三方）';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."deleted" IS '规则删除的时候，只需将此标志位置为1，delete操作将由suricata完成';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule"."operator_id" IS '操作者ID';
CREATE INDEX IF NOT EXISTS sid_index ON fpccms_analysis_suricata_rule(sid);

-- ----------------------------
-- Table structure for fpc_analysis_ti_threatbook
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_analysis_ti_threatbook"(
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
COMMENT ON COLUMN "public"."fpccms_analysis_ti_threatbook"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_analysis_ti_threatbook"."ioc_raw" IS 'IOC内容';
COMMENT ON COLUMN "public"."fpccms_analysis_ti_threatbook"."basic_tag" IS '基础标签';
COMMENT ON COLUMN "public"."fpccms_analysis_ti_threatbook"."intel_type" IS '情报类型';
COMMENT ON COLUMN "public"."fpccms_analysis_ti_threatbook"."source" IS '来源';
COMMENT ON COLUMN "public"."fpccms_analysis_ti_threatbook"."tag" IS '标签';
COMMENT ON COLUMN "public"."fpccms_analysis_ti_threatbook"."time" IS '时间';
COMMENT ON COLUMN "public"."fpccms_analysis_ti_threatbook"."ioc_type" IS 'IOC类型';

-- ----------------------------
-- Table structure for fpccms_analysis_mitre_attack
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_analysis_mitre_attack" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "name" varchar(256) COLLATE "pg_catalog"."default" NOT NULL,
    "parent_id" varchar(256) COLLATE "pg_catalog"."default" DEFAULT '',
    CONSTRAINT "fpccms_analysis_mitre_attack_pkey" PRIMARY KEY ("id")
    )
;
COMMENT ON COLUMN "public"."fpccms_analysis_mitre_attack"."id" IS 'ID';
COMMENT ON COLUMN "public"."fpccms_analysis_mitre_attack"."name" IS '中文名称';
COMMENT ON COLUMN "public"."fpccms_analysis_mitre_attack"."parent_id" IS '父节点ID，填空为父节点(可拥有多个父节点，csv格式)';
    
-- ----------------------------
-- Table structure for fpccms_analysis_suricata_rule_classtype
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_analysis_suricata_rule_classtype" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "assign_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
    "name" varchar(256) COLLATE "pg_catalog"."default" NOT NULL,
    "deleted" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "delete_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    CONSTRAINT "fpccms_analysis_suricata_rule_classtype_pkey" PRIMARY KEY ("id")
    )
;
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule_classtype"."id" IS 'ID';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule_classtype"."assign_id" IS '规则分类在上级cms中的ID';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule_classtype"."name" IS '分类名称';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule_classtype"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule_classtype"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule_classtype"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule_classtype"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_analysis_suricata_rule_classtype"."operator_id" IS '操作者ID';