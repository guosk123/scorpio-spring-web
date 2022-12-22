-- ----------------------------
-- Table structure for fpccms_appliance_sa_category
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_sa_category"
(
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "assign_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "category_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_appliance_sa_category_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_sa_category"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_category"."assign_id" IS '分类在上级cms中的ID';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_category"."name" IS '分类名称';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_category"."category_id" IS '分类ID（范围为101-150, 最多允许添加50个）';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_category"."description" IS '备注';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_category"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_category"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_category"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_category"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_category"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_appliance_sa_subcategory
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_sa_subcategory"
(
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "assign_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "sub_category_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "category_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_appliance_sa_subcategory_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_sa_subcategory"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_subcategory"."assign_id" IS '子分类在上级cms中的ID';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_subcategory"."name" IS '子分类名称';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_subcategory"."sub_category_id" IS '子分类ID（范围为101-200，最多允许添加100个）';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_subcategory"."category_id" IS '所属分类ID';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_subcategory"."description" IS '备注';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_subcategory"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_subcategory"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_subcategory"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_subcategory"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_subcategory"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_appliance_sa_application
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_sa_application"
(
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "assign_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "application_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "category_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "sub_category_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "l7_protocol_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "rule" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_appliance_sa_application_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_sa_application"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_application"."assign_id" IS '应用在上级cms中的ID';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_application"."name" IS '应用名称';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_application"."application_id" IS '应用ID（数字大小在5万和6万之间）';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_application"."category_id" IS '所属分类ID';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_application"."sub_category_id" IS '所属子分类ID';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_application"."l7_protocol_id" IS '七层承载协议ID';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_application"."rule" IS '规则（JSON）';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_application"."description" IS '备注';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_application"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_application"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_application"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_application"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_application"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_appliance_sa_hierarchy
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_sa_hierarchy"
(
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "type" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "category_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "sub_category_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "application_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "create_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_appliance_sa_hierarchy_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_sa_hierarchy"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_hierarchy"."type" IS '类型';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_hierarchy"."category_id" IS '分类ID';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_hierarchy"."sub_category_id" IS '子分类ID';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_hierarchy"."application_id" IS '应用ID';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_hierarchy"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_sa_hierarchy"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_appliance_geoip_country
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_geoip_country" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "assign_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "name" varchar(128) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "country_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "longitude" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "latitude" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_appliance_geoip_country_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_geoip_country"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_geoip_country"."assign_id" IS '国家在上级cms中的ID';
COMMENT ON COLUMN "public"."fpccms_appliance_geoip_country"."name" IS '国家名称';
COMMENT ON COLUMN "public"."fpccms_appliance_geoip_country"."country_id" IS '国家地区ID（范围[300-499]）';
COMMENT ON COLUMN "public"."fpccms_appliance_geoip_country"."longitude" IS '经度';
COMMENT ON COLUMN "public"."fpccms_appliance_geoip_country"."latitude" IS '纬度';
COMMENT ON COLUMN "public"."fpccms_appliance_geoip_country"."description" IS '描述';
COMMENT ON COLUMN "public"."fpccms_appliance_geoip_country"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_appliance_geoip_country"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_geoip_country"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_geoip_country"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_geoip_country"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_appliance_geoip_settings
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_geoip_settings" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "assign_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "country_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '0',
  "province_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '0',
  "city_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '0',
  "ip_address" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_appliance_geoip_settings_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_geoip_settings"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_geoip_country"."assign_id" IS 'geoIpSetting在上级cms中的ID';
COMMENT ON COLUMN "public"."fpccms_appliance_geoip_settings"."country_id" IS '国家ID';
COMMENT ON COLUMN "public"."fpccms_appliance_geoip_settings"."province_id" IS '省份ID';
COMMENT ON COLUMN "public"."fpccms_appliance_geoip_settings"."city_id" IS '城市ID';
COMMENT ON COLUMN "public"."fpccms_appliance_geoip_settings"."ip_address" IS '地区所包含IP地址';
COMMENT ON COLUMN "public"."fpccms_appliance_geoip_settings"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_appliance_geoip_settings"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_geoip_settings"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_geoip_settings"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_geoip_settings"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_appliance_host_group
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_host_group" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "assign_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "ip_address" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_appliance_host_group_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_host_group"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_host_group"."assign_id" IS 'IP地址组在上级cms中的ID';
COMMENT ON COLUMN "public"."fpccms_appliance_host_group"."name" IS '名称';
COMMENT ON COLUMN "public"."fpccms_appliance_host_group"."ip_address" IS 'IP地址';
COMMENT ON COLUMN "public"."fpccms_appliance_host_group"."description" IS '备注';
COMMENT ON COLUMN "public"."fpccms_appliance_host_group"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_appliance_host_group"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_host_group"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_host_group"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_host_group"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_appliance_ingest_policy
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_ingest_policy" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "assign_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "default_action" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "except_bpf" varchar(1024) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "except_tuple" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deduplication" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_appliance_ingest_policy_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_ingest_policy"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_ingest_policy"."assign_id" IS '捕获过滤策略在上级cms中的ID';
COMMENT ON COLUMN "public"."fpccms_appliance_ingest_policy"."name" IS '名称';
COMMENT ON COLUMN "public"."fpccms_appliance_ingest_policy"."default_action" IS '默认动作（0：存储；1：不存储）';
COMMENT ON COLUMN "public"."fpccms_appliance_ingest_policy"."except_bpf" IS '例外条件（BPF表达式）';
COMMENT ON COLUMN "public"."fpccms_appliance_ingest_policy"."except_tuple" IS '例外条件（六元组json）';
COMMENT ON COLUMN "public"."fpccms_appliance_ingest_policy"."deduplication" IS '报文去重（0：去重；1：不去重）';
COMMENT ON COLUMN "public"."fpccms_appliance_ingest_policy"."description" IS '备注';
COMMENT ON COLUMN "public"."fpccms_appliance_ingest_policy"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_appliance_ingest_policy"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_ingest_policy"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_ingest_policy"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_ingest_policy"."operator_id" IS '操作人id';

-- ----------------------------
-- 历史版本，已弃用
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_filter_policy" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "assign_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "default_action" int4 NOT NULL,
  "except_application" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "except_flow" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 1,
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_appliance_filter_policy_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_filter_policy"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_filter_policy"."assign_id" IS '存储过滤策略在上级cms中的ID';
COMMENT ON COLUMN "public"."fpccms_appliance_filter_policy"."name" IS '名称';
COMMENT ON COLUMN "public"."fpccms_appliance_filter_policy"."default_action" IS '默认动作（0：存储，1：不存储，N：截断存储字节数[64-1500]）';
COMMENT ON COLUMN "public"."fpccms_appliance_filter_policy"."except_application" IS '例外应用存储方式（json格式：{applicationId：storeAction}，例：{1:123,2:0,3:1}）';
COMMENT ON COLUMN "public"."fpccms_appliance_filter_policy"."except_flow" IS '例外流量是否存储（0：不存储，1：存储）';
COMMENT ON COLUMN "public"."fpccms_appliance_filter_policy"."description" IS '备注';
COMMENT ON COLUMN "public"."fpccms_appliance_filter_policy"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_appliance_filter_policy"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_filter_policy"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_filter_policy"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_filter_policy"."operator_id" IS '操作人id';

------------------------------------------------------------
------ Table structure for fpccms_appliance_storage_filter_rule
------------------------------------------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_storage_filter_rule" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "tuple" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "assign_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
    "state" char COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "priority" integer  NOT NULL,
    "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "delete_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    CONSTRAINT "fpccms_appliance_storage_filter_rule_pkey" PRIMARY KEY ("id")
    )
;
COMMENT ON COLUMN "public"."fpccms_appliance_storage_filter_rule"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_storage_filter_rule"."name" IS '名称';
COMMENT ON COLUMN "public"."fpccms_appliance_storage_filter_rule"."tuple" IS '存储过滤条件';
COMMENT ON COLUMN "public"."fpccms_appliance_storage_filter_rule"."assign_id" IS '存储过滤规则在cms上级中的ID';
COMMENT ON COLUMN "public"."fpccms_appliance_storage_filter_rule"."description" IS '备注';
COMMENT ON COLUMN "public"."fpccms_appliance_storage_filter_rule"."priority" IS '过滤规则优先级';
COMMENT ON COLUMN "public"."fpccms_appliance_storage_filter_rule"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_appliance_storage_filter_rule"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_storage_filter_rule"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_storage_filter_rule"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_storage_filter_rule"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_appliance_sensor_network
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_sensor_network" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "name" text COLLATE "pg_catalog"."default" DEFAULT '',
    "sensor_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "sensor_name" text COLLATE "pg_catalog"."default" NOT NULL,
    "sensor_type" char(1) COLLATE "pg_catalog"."default" NOT NULL,
    "network_in_sensor_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "owner" varchar(16) COLLATE "pg_catalog"."default" NOT NULL,
    "description" varchar(512) COLLATE "pg_catalog"."default" DEFAULT '',
    "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "delete_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    CONSTRAINT "fpccms_appliance_sensor_network_pkey" PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network"."name" IS '网络名称';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network"."sensor_id" IS '探针ID';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network"."sensor_name" IS '探针名';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network"."sensor_type" IS '探针类型';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network"."network_in_sensor_id" IS '探针中的网络ID';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network"."owner" IS '管理CMS';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network"."description" IS '描述';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_appliance_network_policy
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_network_policy" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "assign_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "network_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "policy_type" varchar(16) COLLATE "pg_catalog"."default" NOT NULL,
  "policy_source" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "policy_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "timestamp" timestamptz(6) NOT NULL,
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_appliance_network_policy_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_network_policy"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_network_policy"."assign_id" IS '网络策略在上级cms中的ID';
COMMENT ON COLUMN "public"."fpccms_appliance_network_policy"."network_id" IS '网络id';
COMMENT ON COLUMN "public"."fpccms_appliance_network_policy"."policy_type" IS '策略类型（ingest：捕获策略；filter：过滤策略；send：外发策略）';
COMMENT ON COLUMN "public"."fpccms_appliance_network_policy"."policy_source" IS '策略配置来源（0：单个网络）';
COMMENT ON COLUMN "public"."fpccms_appliance_network_policy"."policy_id" IS '策略id';
COMMENT ON COLUMN "public"."fpccms_appliance_network_policy"."timestamp" IS '时间戳';
COMMENT ON COLUMN "public"."fpccms_appliance_network_policy"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_appliance_sensor_network_group
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_sensor_network_group" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" text COLLATE "pg_catalog"."default" NOT NULL,
  "network_in_sensor_ids" text COLLATE "pg_catalog"."default" NOT NULL,
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_appliance_sensor_network_group_pkey" PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network_group"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network_group"."name" IS '网络组名称';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network_group"."network_in_sensor_ids" IS '探针中的网络ID';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network_group"."description" IS '描述';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network_group"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network_group"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network_group"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network_group"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_appliance_sensor_logical_subnet
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_sensor_logical_subnet" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "assign_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "name" text COLLATE "pg_catalog"."default" NOT NULL,
  "type" varchar(16) COLLATE "pg_catalog"."default" NOT NULL,
  "configuration" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "network_in_sensor_ids" text COLLATE "pg_catalog"."default" NOT NULL,
  "bandwidth" int4 NOT NULL,
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_appliance_sensor_logical_subnet_pkey" PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_logical_subnet"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_logical_subnet"."assign_id" IS '子网在上级cms中的ID';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_logical_subnet"."name" IS '逻辑子网名称';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_logical_subnet"."type" IS '子网类型（ip：IP子网络；mac：MAC子网络；vlan：VLAN子网络；mpls：MPLS子网络；gre：GRE子网络；vxlan：VXLAN子网络）';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_logical_subnet"."configuration" IS '子网配置，子网类型的不同则配置不同';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_logical_subnet"."network_in_sensor_ids" IS '探针中的网络ID';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_logical_subnet"."bandwidth" IS '总带宽（Mbps）';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_logical_subnet"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_logical_subnet"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_logical_subnet"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_logical_subnet"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_appliance_sensor_network_topology
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_sensor_network_topology" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "topology" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "metric" varchar(1024) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "timestamp" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_appliance_sensor_network_topology_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network_topology"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network_topology"."topology" IS '网元链路配置';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network_topology"."metric" IS '指标配置(csv格式)';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network_topology"."timestamp" IS '配置时间';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network_topology"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_appliance_sensor_network_perm
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_sensor_network_perm" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "user_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "network_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "network_group_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  CONSTRAINT "fpccms_appliance_sensor_network_perm_pkey" PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network_perm"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network_perm"."user_id" IS '用户ID';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network_perm"."network_id" IS '网络/子网ID';
COMMENT ON COLUMN "public"."fpccms_appliance_sensor_network_perm"."network_group_id" IS '网络组ID';

-- ----------------------------
-- Table structure for fpccms_appliance_filter_rule_network
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_filter_rule_network" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "filter_rule_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "network_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
    "network_group_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
    CONSTRAINT "fpccms_appliance_filter_rule_network_pkey" PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."fpccms_appliance_filter_rule_network"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_filter_rule_network"."filter_rule_id" IS '存储过滤规则id';
COMMENT ON COLUMN "public"."fpccms_appliance_filter_rule_network"."network_id" IS '网络/子网络id';
COMMENT ON COLUMN "public"."fpccms_appliance_filter_rule_network"."network_group_id" IS '网络组id';

-- ----------------------------
-- Table structure for fpccms_appliance_service
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_service" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "assign_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "application" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "creater_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_appliance_service_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_service"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_service"."assign_id" IS '业务在上级CMS中的ID';
COMMENT ON COLUMN "public"."fpccms_appliance_service"."name" IS '名称';
COMMENT ON COLUMN "public"."fpccms_appliance_service"."application" IS '业务所包含的应用的ID集合(csv格式)';
COMMENT ON COLUMN "public"."fpccms_appliance_service"."description" IS '备注';
COMMENT ON COLUMN "public"."fpccms_appliance_service"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_appliance_service"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_service"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_service"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_service"."creater_id" IS '创建人id';
COMMENT ON COLUMN "public"."fpccms_appliance_service"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_appliance_service_dashboard_settings
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_service_dashboard_settings" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "parameters" text COLLATE "pg_catalog"."default" DEFAULT '',
  "percent_parameter" char(1) NOT NULL DEFAULT 0,
  "time_window_parameter" char(1) NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_appliance_service_dashboard_settings_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_service_dashboard_settings"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_service_dashboard_settings"."parameters" IS '参数数组';
COMMENT ON COLUMN "public"."fpccms_appliance_service_dashboard_settings"."percent_parameter" IS '百分比参数(0:连接成功率 1:客户端重传率 2:服务端重传率)';
COMMENT ON COLUMN "public"."fpccms_appliance_service_dashboard_settings"."time_window_parameter" IS '时间窗口参数(0:流量趋势图 1:告警分布图)';
COMMENT ON COLUMN "public"."fpccms_appliance_service_dashboard_settings"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_service_dashboard_settings"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_service_dashboard_settings"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_appliance_service_network
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_service_network" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "service_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "network_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "network_group_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  CONSTRAINT "fpccms_appliance_service_network_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_service_network"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_service_network"."service_id" IS '业务id';
COMMENT ON COLUMN "public"."fpccms_appliance_service_network"."network_id" IS '网络/子网络id';
COMMENT ON COLUMN "public"."fpccms_appliance_service_network"."network_group_id" IS '网络组id';

-- ----------------------------
-- Table structure for fpccms_appliance_service_link
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_service_link" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "assign_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "service_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "link" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "metric" varchar(1024) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "timestamp" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_appliance_service_link_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_service_link"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_service_link"."assign_id" IS '业务路径在上级CMS中的ID';
COMMENT ON COLUMN "public"."fpccms_appliance_service_link"."service_id" IS '业务ID';
COMMENT ON COLUMN "public"."fpccms_appliance_service_link"."link" IS '网元链路配置';
COMMENT ON COLUMN "public"."fpccms_appliance_service_link"."metric" IS '指标配置(csv格式)';
COMMENT ON COLUMN "public"."fpccms_appliance_service_link"."timestamp" IS '配置时间';
COMMENT ON COLUMN "public"."fpccms_appliance_service_link"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_appliance_user_service_follow
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_user_service_follow" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "user_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "service_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "network_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "network_group_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "follow_time" timestamptz(6),
  CONSTRAINT "fpccms_appliance_user_service_follow_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_user_service_follow"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_user_service_follow"."user_id" IS '用户id';
COMMENT ON COLUMN "public"."fpccms_appliance_user_service_follow"."service_id" IS '业务ID';
COMMENT ON COLUMN "public"."fpccms_appliance_user_service_follow"."network_id" IS '网络/子网络ID';
COMMENT ON COLUMN "public"."fpccms_appliance_user_service_follow"."network_group_id" IS '网络组id';
COMMENT ON COLUMN "public"."fpccms_appliance_user_service_follow"."follow_time" IS '关注时间';

-- ----------------------------
-- Table structure for fpccms_appliance_metric_settings
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_metric_settings" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "assign_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "source_type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "network_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "service_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "packet_file_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "metric" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "value" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "update_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_appliance_metric_settings_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_metric_settings"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_metric_settings"."assign_id" IS '度量指标在上级CMS中的ID';
COMMENT ON COLUMN "public"."fpccms_appliance_metric_settings"."source_type" IS '来源类型（network/service/packetFile）';
COMMENT ON COLUMN "public"."fpccms_appliance_metric_settings"."network_id" IS '网络/子网络ID';
COMMENT ON COLUMN "public"."fpccms_appliance_metric_settings"."service_id" IS '业务ID';
COMMENT ON COLUMN "public"."fpccms_appliance_metric_settings"."packet_file_id" IS '离线数据包文件ID';
COMMENT ON COLUMN "public"."fpccms_appliance_metric_settings"."metric" IS '指标（server_response_normal/server_response_timeout/long_connection）';
COMMENT ON COLUMN "public"."fpccms_appliance_metric_settings"."value" IS '参数值';
COMMENT ON COLUMN "public"."fpccms_appliance_metric_settings"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."fpccms_appliance_metric_settings"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_appliance_baseline_settings
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_baseline_settings" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "source_type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "network_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "network_group_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "service_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "category" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "weighting_model" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "windowing_model" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "windowing_count" int4 NOT NULL,
  "update_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_appliance_baseline_settings_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_baseline_settings"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_baseline_settings"."source_type" IS '来源类型（network/service）';
COMMENT ON COLUMN "public"."fpccms_appliance_baseline_settings"."network_id" IS '网络/子网络ID';
COMMENT ON COLUMN "public"."fpccms_appliance_baseline_settings"."network_group_id" IS '网络组ID';
COMMENT ON COLUMN "public"."fpccms_appliance_baseline_settings"."service_id" IS '业务ID';
COMMENT ON COLUMN "public"."fpccms_appliance_baseline_settings"."category" IS '基线类型（bandwidth/flow/packet/responseLatency）';
COMMENT ON COLUMN "public"."fpccms_appliance_baseline_settings"."weighting_model" IS '权重模型（最小：MIN；最大：MAX；均值：MEAN；中位数：MEDIAN）';
COMMENT ON COLUMN "public"."fpccms_appliance_baseline_settings"."windowing_model" IS '基线窗口（天同比：minute_of_day/five_minute_of_day/hour_of_day；周同比：minute_of_week/five_minute_of_week/hour_of_week；环比：last_n_minutes/last_n_five_minutes/last_n_hours；）';
COMMENT ON COLUMN "public"."fpccms_appliance_baseline_settings"."windowing_count" IS '回顾周期';
COMMENT ON COLUMN "public"."fpccms_appliance_baseline_settings"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."fpccms_appliance_baseline_settings"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_appliance_baseline_value
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_baseline_value" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "source_type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "source_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "alert_network_id" varchar(64) COLLATE "pg_catalog"."default",
  "alert_network_group_id" varchar(64) COLLATE "pg_catalog"."default",
  "alert_service_id" varchar(64) COLLATE "pg_catalog"."default",
  "value" double precision NOT NULL,
  "calculate_time" timestamptz(6),
  "timestamp" timestamptz(6),
  CONSTRAINT "fpccms_appliance_baseline_value_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_baseline_value"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_baseline_value"."source_type" IS '基线定义来源（告警alert、网络性能cms）';
COMMENT ON COLUMN "public"."fpccms_appliance_baseline_value"."source_id" IS '基线定义ID（基线定义表ID或业务/网络告警ID）';
COMMENT ON COLUMN "public"."fpccms_appliance_baseline_value"."alert_network_id" IS '业务告警所属网络/子网ID';
COMMENT ON COLUMN "public"."fpccms_appliance_baseline_value"."alert_network_group_id" IS '业务告警所属网络组ID';
COMMENT ON COLUMN "public"."fpccms_appliance_baseline_value"."alert_service_id" IS '业务告警所属业务ID';
COMMENT ON COLUMN "public"."fpccms_appliance_baseline_value"."value" IS '基线计算结果';
COMMENT ON COLUMN "public"."fpccms_appliance_baseline_value"."calculate_time" IS '计算时间';
COMMENT ON COLUMN "public"."fpccms_appliance_baseline_value"."timestamp" IS '写入时间';

-- ----------------------------
-- Table structure for fpccms_appliance_assignment_task
-- ----------------------------
CREATE TABLE IF NOT EXISTS "fpccms_appliance_assignment_task" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "assign_task_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "assign_task_time" timestamptz(6),
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "source" varchar(128) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "filter_start_time" timestamptz(6) NOT NULL,
  "filter_end_time" timestamptz(6) NOT NULL,
  "filter_network_id" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "filter_condition_type" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "filter_tuple" varchar(1024) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "filter_bpf" varchar(1024) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "filter_raw" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "mode" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "replay_netif" varchar(32) NOT NULL DEFAULT '',
  "replay_rate" int4 NOT NULL DEFAULT 0,
  "replay_rate_unit" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "forward_action" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  CONSTRAINT "fpccms_appliance_assignment_task_pkey" PRIMARY KEY ("id") 
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task"."assign_task_id" IS '上级下发任务id';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task"."assign_task_time" IS '上级下发任务时间';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task"."name" IS '任务名称';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task"."source" IS '任务来源';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task"."filter_start_time" IS '查询起始时间';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task"."filter_end_time" IS '查询结束时间';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task"."filter_network_id" IS '过滤网络';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task"."filter_condition_type" IS '查询过滤类型（0：六元组；1：bpf表达式）';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task"."filter_tuple" IS '查询六元组json';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task"."filter_bpf" IS '查询bpf表达式';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task"."filter_raw" IS '过滤原始内容';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task"."mode" IS '导出方式（0：文件；1：重放）';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task"."replay_netif" IS '重放接口的接口名';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task"."replay_rate" IS '重放速率数值';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task"."replay_rate_unit" IS '重放速率单位（0：Kbps；1：pps）';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task"."forward_action" IS '转发策略（0：先存储，再转发；1：不存储，直接转发）';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task"."description" IS '备注';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_appliance_assignment_task_record
-- ----------------------------
CREATE TABLE IF NOT EXISTS "fpccms_appliance_assignment_task_record" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "task_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "fpc_task_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "fpc_serial_number" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "message_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "assignment_state" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "execution_state" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "execution_trace" text COLLATE "pg_catalog"."default" DEFAULT '',
  "execution_start_time" timestamptz(6),
  "execution_end_time" timestamptz(6),
  "execution_progress" int4 DEFAULT 0,
  "execution_cache_path" varchar(512) COLLATE "pg_catalog"."default" DEFAULT '',
  "pcap_file_url" varchar(1024) COLLATE "pg_catalog"."default" DEFAULT '',
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "assignment_time" timestamptz(6),
  CONSTRAINT "fpccms_appliance_assignment_task_record_pkey" PRIMARY KEY ("id") 
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task_record"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task_record"."task_id" IS '任务id（查询任务id）';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task_record"."fpc_task_id" IS 'fpc任务id（查询任务id）';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task_record"."fpc_serial_number" IS '探针设备序列号';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task_record"."message_id" IS '消息id';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task_record"."assignment_state" IS '下发状态（0：下发成功；1：正在下发；2：下发失败；3：等待下发；4：取消下发；5：停止下发）';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task_record"."execution_state" IS '任务执行状态（0：正常；1：停止；2：完成）';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task_record"."execution_trace" IS '任务执行摘要';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task_record"."execution_start_time" IS '任务执行起始时间';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task_record"."execution_end_time" IS '任务执行结束时间';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task_record"."execution_progress" IS '执行进度（1~100）';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task_record"."execution_cache_path" IS '任务文件在探针上的缓存路径';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task_record"."pcap_file_url" IS '任务文件下载url';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task_record"."operator_id" IS '操作人id';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_task_record"."assignment_time" IS '任务下发时间';

-- ----------------------------
-- Table structure for fpccms_appliance_assignment_action
-- ----------------------------
CREATE TABLE IF NOT EXISTS "fpccms_appliance_assignment_action" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "fpc_serial_number" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "message_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "assignment_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "task_policy_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "type" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "action" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "state" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "assignment_time" timestamptz(6),
  CONSTRAINT "fpccms_appliance_assignment_action_pkey" PRIMARY KEY ("id") 
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_action"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_action"."fpc_serial_number" IS '设备id';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_action"."message_id" IS '消息id';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_action"."assignment_id" IS '查询id，前端根据该id进行页面显示';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_action"."task_policy_id" IS '策略内容id或任务内容id（type字段确认）';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_action"."type" IS '下发类型（任务：0；策略：1）';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_action"."action" IS '下发动作（0：下发任务；1：下发删除；）';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_action"."state" IS '下发状态（0：下发成功；1：正在下发；2：下发失败；3：等待下发；4：取消下发；5：停止下发）';
COMMENT ON COLUMN "public"."fpccms_appliance_assignment_action"."assignment_time" IS '任务下发时间';

-- ----------------------------
-- Table structure for fpccms_appliance_alert_rule
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_alert_rule" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "assign_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "name" varchar(128) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "category" varchar(10) COLLATE "pg_catalog"."default" NOT NULL,
  "level" char(1) COLLATE "pg_catalog"."default" NOT NULL,
  "threshold_settings" text COLLATE "pg_catalog"."default",
  "trend_settings" text COLLATE "pg_catalog"."default",
  "advanced_settings" text COLLATE "pg_catalog"."default",
  "refire" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "status" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 1,
  "description" varchar(512) COLLATE "pg_catalog"."default" DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_appliance_alert_rule_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_alert_rule"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_alert_rule"."assign_id" IS '告警在上级cms中的ID';
COMMENT ON COLUMN "public"."fpccms_appliance_alert_rule"."name" IS '名称';
COMMENT ON COLUMN "public"."fpccms_appliance_alert_rule"."category" IS '分类（threshold：阈值；trend：基线；advanced：组合）';
COMMENT ON COLUMN "public"."fpccms_appliance_alert_rule"."level" IS '级别（0：提示；1：一般；2：重要；3：紧急）';
COMMENT ON COLUMN "public"."fpccms_appliance_alert_rule"."threshold_settings" IS '阈值告警配置（json字符串）';
COMMENT ON COLUMN "public"."fpccms_appliance_alert_rule"."trend_settings" IS '基线告警配置（json字符串）';
COMMENT ON COLUMN "public"."fpccms_appliance_alert_rule"."advanced_settings" IS '组合告警配置（json字符串）';
COMMENT ON COLUMN "public"."fpccms_appliance_alert_rule"."refire" IS '告警触发配置（json字符串）';
COMMENT ON COLUMN "public"."fpccms_appliance_alert_rule"."status" IS '启用状态（0：禁用；1：启用）';
COMMENT ON COLUMN "public"."fpccms_appliance_alert_rule"."description" IS '备注';
COMMENT ON COLUMN "public"."fpccms_appliance_alert_rule"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_appliance_alert_rule"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_alert_rule"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_alert_rule"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_alert_rule"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_appliance_alert_scope
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_alert_scope" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "alert_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "source_type" varchar(12) COLLATE "pg_catalog"."default" NOT NULL,
  "network_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "service_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  CONSTRAINT "fpccms_appliance_alert_scope_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_alert_scope"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_alert_scope"."alert_id" IS '告警ID';
COMMENT ON COLUMN "public"."fpccms_appliance_alert_scope"."source_type" IS 'network：网络；service：业务';
COMMENT ON COLUMN "public"."fpccms_appliance_alert_scope"."network_id" IS '网络/子网ID';
COMMENT ON COLUMN "public"."fpccms_appliance_alert_scope"."service_id" IS '业务ID';

-- ----------------------------
-- Table structure for fpccms_appliance_smtp_configuration
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_smtp_configuration" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "mail_username" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "mail_address" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
    "smtp_server" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
    "server_port" int4 NOT NULL,
    "encrypt" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "login_user" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
    "login_password" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
    "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "delete_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_appliance_smtp_configuration_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_smtp_configuration"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_smtp_configuration"."mail_username" IS '用户名';
COMMENT ON COLUMN "public"."fpccms_appliance_smtp_configuration"."mail_address" IS '邮件地址';
COMMENT ON COLUMN "public"."fpccms_appliance_smtp_configuration"."smtp_server" IS '邮件服务器';
COMMENT ON COLUMN "public"."fpccms_appliance_smtp_configuration"."server_port" IS '服务器端口';
COMMENT ON COLUMN "public"."fpccms_appliance_smtp_configuration"."encrypt" IS '是否加密（0：不加密；1：加密）';
COMMENT ON COLUMN "public"."fpccms_appliance_smtp_configuration"."login_user" IS '登录用户';
COMMENT ON COLUMN "public"."fpccms_appliance_smtp_configuration"."login_password" IS '登录密码';
COMMENT ON COLUMN "public"."fpccms_appliance_smtp_configuration"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_appliance_smtp_configuration"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_smtp_configuration"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_smtp_configuration"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_smtp_configuration"."operator_id" IS '操作人id';

    -- ----------------------------
-- Table structure for fpccms_appliance_domain_whitelist
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_domain_whitelist" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "assign_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
    "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "domain" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "delete_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    CONSTRAINT "fpc_appliance_domain_whitelist_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_domain_whitelist"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_domain_whitelist"."assign_id" IS '域名白名单在cms上级中的ID';
COMMENT ON COLUMN "public"."fpccms_appliance_domain_whitelist"."name" IS '名称';
COMMENT ON COLUMN "public"."fpccms_appliance_domain_whitelist"."domain" IS '域名';
COMMENT ON COLUMN "public"."fpccms_appliance_domain_whitelist"."description" IS '备注';
COMMENT ON COLUMN "public"."fpccms_appliance_domain_whitelist"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_appliance_domain_whitelist"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_domain_whitelist"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_domain_whitelist"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_domain_whitelist"."operator_id" IS '操作人id';

-- ----------------------------
-- 历史版本，已弃用
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_mail_sendup_rule" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "mail_title" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "receiver" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "cc" text COLLATE "pg_catalog"."default" DEFAULT '',
    "interval" int4 NOT NULL,
    "network_alert_content" text COLLATE "pg_catalog"."default" DEFAULT '',
    "service_alert_content" text COLLATE "pg_catalog"."default" DEFAULT '',
    "system_alarm_content" text COLLATE "pg_catalog"."default" DEFAULT '',
    "system_log_content" text COLLATE "pg_catalog"."default" DEFAULT '',
    "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "delete_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_appliance_mail_sendup_rule_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_mail_sendup_rule"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_mail_sendup_rule"."mail_title" IS '邮件主题';
COMMENT ON COLUMN "public"."fpccms_appliance_mail_sendup_rule"."receiver" IS '收件人';
COMMENT ON COLUMN "public"."fpccms_appliance_mail_sendup_rule"."cc" IS '抄送人';
COMMENT ON COLUMN "public"."fpccms_appliance_mail_sendup_rule"."interval" IS '时间间隔';
COMMENT ON COLUMN "public"."fpccms_appliance_mail_sendup_rule"."network_alert_content" IS '网络告警消息内容';
COMMENT ON COLUMN "public"."fpccms_appliance_mail_sendup_rule"."service_alert_content" IS '业务告警消息内容';
COMMENT ON COLUMN "public"."fpccms_appliance_mail_sendup_rule"."system_alarm_content" IS '系统告警消息内容';
COMMENT ON COLUMN "public"."fpccms_appliance_mail_sendup_rule"."system_log_content" IS '系统日志消息内容';
COMMENT ON COLUMN "public"."fpccms_appliance_mail_sendup_rule"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_appliance_mail_sendup_rule"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_mail_sendup_rule"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_mail_sendup_rule"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_mail_sendup_rule"."operator_id" IS '操作人id';

-- ----------------------------
-- 历史版本，已弃用
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_syslog_sendup_rule" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "assign_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
    "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "syslog_server_address" text COLLATE "pg_catalog"."default" NOT NULL,
    "send_type" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "send_time" varchar(32) COLLATE "pg_catalog"."default" DEFAULT '',
    "interval" int4 NOT NULL DEFAULT 0,
    "threshold" int4 NOT NULL DEFAULT 0,
    "severity" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "facility" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "encode_type" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "separator" char(1) COLLATE "pg_catalog"."default" DEFAULT '',
    "network_alert_content" text COLLATE "pg_catalog"."default" DEFAULT '',
    "service_alert_content" text COLLATE "pg_catalog"."default" DEFAULT '',
    "system_alarm_content" text COLLATE "pg_catalog"."default" DEFAULT '',
    "system_log_content" text COLLATE "pg_catalog"."default" DEFAULT '',
	"connect_info" text COLLATE "pg_catalog"."default" DEFAULT '',
    "data_source" text COLLATE "pg_catalog"."default" DEFAULT '',
    "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "delete_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_appliance_syslog_sendup_rule_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_syslog_sendup_rule"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_syslog_sendup_rule"."assign_id" IS 'syslog在上级cms中的id';
COMMENT ON COLUMN "public"."fpccms_appliance_syslog_sendup_rule"."name" IS 'syslog规则名';
COMMENT ON COLUMN "public"."fpccms_appliance_syslog_sendup_rule"."syslog_server_address" IS 'syslog服务器地址';
COMMENT ON COLUMN "public"."fpccms_appliance_syslog_sendup_rule"."send_type" IS '发送方式';（0：即时发送；1：定时发送；2：抑制发送）
COMMENT ON COLUMN "public"."fpccms_appliance_syslog_sendup_rule"."send_time" IS '发送间隔（当发送方式为定时发送时使用）';
COMMENT ON COLUMN "public"."fpccms_appliance_syslog_sendup_rule"."interval" IS '时间间隔';
COMMENT ON COLUMN "public"."fpccms_appliance_syslog_sendup_rule"."threshold" IS '数量阈值';
COMMENT ON COLUMN "public"."fpccms_appliance_syslog_sendup_rule"."severity" IS 'syslog等级';
COMMENT ON COLUMN "public"."fpccms_appliance_syslog_sendup_rule"."facility" IS 'syslog类型';
COMMENT ON COLUMN "public"."fpccms_appliance_syslog_sendup_rule"."encode_type" IS '字符编码方式（0：UTF-8；1：GB2312）';
COMMENT ON COLUMN "public"."fpccms_appliance_syslog_sendup_rule"."separator" IS '字段分隔符';
COMMENT ON COLUMN "public"."fpccms_appliance_syslog_sendup_rule"."network_alert_content" IS '网络告警消息内容';
COMMENT ON COLUMN "public"."fpccms_appliance_syslog_sendup_rule"."service_alert_content" IS '业务告警消息内容';
COMMENT ON COLUMN "public"."fpccms_appliance_syslog_sendup_rule"."system_alarm_content" IS '系统告警消息内容';
COMMENT ON COLUMN "public"."fpccms_appliance_syslog_sendup_rule"."system_log_content" IS '系统日志消息内容';
COMMENT ON COLUMN "public"."fpccms_appliance_syslog_sendup_rule"."connect_info" IS '北向syslog目的地址信息';
COMMENT ON COLUMN "public"."fpccms_appliance_syslog_sendup_rule"."data_source" IS '北向syslog数据源';
COMMENT ON COLUMN "public"."fpccms_appliance_syslog_sendup_rule"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_appliance_syslog_sendup_rule"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_syslog_sendup_rule"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_syslog_sendup_rule"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_syslog_sendup_rule"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_central_fpc
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_central_fpc" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "ip" varchar(20) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "serial_number" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "version" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "type" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
  "app_key" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "app_token" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "cms_token" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "cms_serial_number" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" DEFAULT '',
  "report_state" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
  "report_action" char(1) COLLATE "pg_catalog"."default" DEFAULT 1,
  "deleted" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_central_fpc_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_central_fpc"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_central_fpc"."name" IS '设备名称';
COMMENT ON COLUMN "public"."fpccms_central_fpc"."ip" IS '设备IP';
COMMENT ON COLUMN "public"."fpccms_central_fpc"."serial_number" IS '设备序列号';
COMMENT ON COLUMN "public"."fpccms_central_fpc"."version" IS '设备当前版本';
COMMENT ON COLUMN "public"."fpccms_central_fpc"."type" IS '探针类型（0：普通探针；1：软件探针）';
COMMENT ON COLUMN "public"."fpccms_central_fpc"."app_key" IS '管理用户app_key';
COMMENT ON COLUMN "public"."fpccms_central_fpc"."app_token" IS '管理用户app_token';
COMMENT ON COLUMN "public"."fpccms_central_fpc"."cms_token" IS 'cms分配给设备的token';
COMMENT ON COLUMN "public"."fpccms_central_fpc"."cms_serial_number" IS '管理CMS设备编号';
COMMENT ON COLUMN "public"."fpccms_central_fpc"."description" IS '备注';
COMMENT ON COLUMN "public"."fpccms_central_fpc"."report_state" IS '上报状态（0：未上报；1：已上报）';
COMMENT ON COLUMN "public"."fpccms_central_fpc"."report_action" IS '上报动作（1：新增；2：修改；3：删除）';
COMMENT ON COLUMN "public"."fpccms_central_fpc"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_central_fpc"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_central_fpc"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_central_fpc"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_central_fpc"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_central_fpc_network
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_central_fpc_network" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "fpc_network_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "fpc_network_name" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "bandwidth" int4 NOT NULL DEFAULT 0,
  "fpc_serial_number" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "report_state" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
  "report_action" char(1) COLLATE "pg_catalog"."default" DEFAULT 1,
  "deleted" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_central_fpc_network_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_central_fpc_network"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_central_fpc_network"."fpc_network_id" IS '探针内网络ID';
COMMENT ON COLUMN "public"."fpccms_central_fpc_network"."fpc_network_name" IS '探针内网络名称';
COMMENT ON COLUMN "public"."fpccms_central_fpc_network"."bandwidth" IS '探针内网络总带宽';
COMMENT ON COLUMN "public"."fpccms_central_fpc_network"."fpc_serial_number" IS '探针设备编号';
COMMENT ON COLUMN "public"."fpccms_central_fpc_network"."report_state" IS '上报状态（0：未上报；1：已上报）';
COMMENT ON COLUMN "public"."fpccms_central_fpc_network"."report_action" IS '上报动作（1：新增；2：修改；3：删除）';
COMMENT ON COLUMN "public"."fpccms_central_fpc_network"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_central_fpc_network"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_central_fpc_network"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_central_fpc_network"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_central_fpc_network"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_central_sensor_config_mapping
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_central_sensor_config_mapping" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "config_type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "config_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "sensor_serial_number" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "config_in_sensor_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_central_sensor_config_mapping_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_central_sensor_config_mapping"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_central_sensor_config_mapping"."config_type" IS '配置类型';
COMMENT ON COLUMN "public"."fpccms_central_sensor_config_mapping"."config_id" IS '配置本地ID';
COMMENT ON COLUMN "public"."fpccms_central_sensor_config_mapping"."sensor_serial_number" IS '探针设备序列号';
COMMENT ON COLUMN "public"."fpccms_central_sensor_config_mapping"."config_in_sensor_id" IS '配置在探针上的ID';

-- ----------------------------
-- Table structure for fpccms_central_cms
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_central_cms" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "ip" varchar(20) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "serial_number" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "version" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "app_key" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "app_token" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "cms_token" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "superior_cms_serial_number" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" DEFAULT '',
  "report_state" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
  "report_action" char(1) COLLATE "pg_catalog"."default" DEFAULT 1,
  "deleted" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_central_cms_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_central_cms"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_central_cms"."name" IS '设备名称';
COMMENT ON COLUMN "public"."fpccms_central_cms"."ip" IS '设备IP';
COMMENT ON COLUMN "public"."fpccms_central_cms"."serial_number" IS '设备序列号';
COMMENT ON COLUMN "public"."fpccms_central_cms"."version" IS '设备当前版本';
COMMENT ON COLUMN "public"."fpccms_central_cms"."app_key" IS '管理用户app_key';
COMMENT ON COLUMN "public"."fpccms_central_cms"."app_token" IS '管理用户app_token';
COMMENT ON COLUMN "public"."fpccms_central_cms"."cms_token" IS 'cms分配给设备的token';
COMMENT ON COLUMN "public"."fpccms_central_cms"."superior_cms_serial_number" IS '上级cms设备序列号';
COMMENT ON COLUMN "public"."fpccms_central_cms"."description" IS '备注';
COMMENT ON COLUMN "public"."fpccms_central_cms"."report_state" IS '上报状态（0：未上报；1：已上报）';
COMMENT ON COLUMN "public"."fpccms_central_cms"."report_action" IS '上报动作（1：新增；2：修改；3：删除）';
COMMENT ON COLUMN "public"."fpccms_central_cms"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_central_cms"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_central_cms"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_central_cms"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_central_cms"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_central_device_disk
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_central_device_disk" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "device_type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "device_serial_number" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "physical_location" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "slot_no" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "raid_no" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "raid_level" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "state" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "medium" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "capacity" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "rebuild_progress" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "copyback_progress" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "foreign_state" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpccms_central_device_disk_pkey" PRIMARY KEY ("id") 
)
;
COMMENT ON COLUMN "public"."fpccms_central_device_disk"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_central_device_disk"."device_type" IS '设备类型';
COMMENT ON COLUMN "public"."fpccms_central_device_disk"."device_serial_number" IS '设备编号';
COMMENT ON COLUMN "public"."fpccms_central_device_disk"."physical_location" IS '磁盘物理位置';
COMMENT ON COLUMN "public"."fpccms_central_device_disk"."slot_no" IS '槽位号';
COMMENT ON COLUMN "public"."fpccms_central_device_disk"."raid_no" IS 'RAID组编号';
COMMENT ON COLUMN "public"."fpccms_central_device_disk"."raid_level" IS 'RAID组级别';
COMMENT ON COLUMN "public"."fpccms_central_device_disk"."state" IS '磁盘状态（0：在线；1：热备；2：重建；3：可配置；4：不可配置；5：回拷；6：失败；7：错误）';
COMMENT ON COLUMN "public"."fpccms_central_device_disk"."medium" IS '存储介质（0：HDD；1：SSD；2：未知）';
COMMENT ON COLUMN "public"."fpccms_central_device_disk"."capacity" IS '容量（单位：TB）';
COMMENT ON COLUMN "public"."fpccms_central_device_disk"."rebuild_progress" IS '重建进度（0~100）';
COMMENT ON COLUMN "public"."fpccms_central_device_disk"."copyback_progress" IS '回拷进度（0~100）';
COMMENT ON COLUMN "public"."fpccms_central_device_disk"."foreign_state" IS '外部状态（0：None；1：Foreign）';
COMMENT ON COLUMN "public"."fpccms_central_device_disk"."description" IS '备注';

-- ----------------------------
-- Table structure for fpccms_central_device_raid
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_central_device_raid" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "device_type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "device_serial_number" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "raid_no" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "raid_level" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "state" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  CONSTRAINT "fpccms_central_device_raid_pkey" PRIMARY KEY ("id") 
)
;
COMMENT ON COLUMN "public"."fpccms_central_device_raid"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_central_device_raid"."device_type" IS '设备类型';
COMMENT ON COLUMN "public"."fpccms_central_device_raid"."device_serial_number" IS '设备编号';
COMMENT ON COLUMN "public"."fpccms_central_device_raid"."raid_no" IS 'raid序号';
COMMENT ON COLUMN "public"."fpccms_central_device_raid"."raid_level" IS 'raid级别';
COMMENT ON COLUMN "public"."fpccms_central_device_raid"."state" IS 'raid状态';

-- ----------------------------
-- Table structure for fpccms_central_system_metric
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_central_system_metric" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "device_type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "monitored_serial_number" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "cpu_metric" int4 NOT NULL DEFAULT 0,
  "memory_metric" int4 NOT NULL DEFAULT 0,
  "system_fs_metric" int4 NOT NULL DEFAULT 0,
  "index_fs_metric" int4 NOT NULL DEFAULT 0,
  "metadata_fs_metric" int4 NOT NULL DEFAULT 0,
  "metadata_hot_fs_metric" int4 NOT NULL DEFAULT 0,
  "packet_fs_metric" int4 NOT NULL DEFAULT 0,
  "fs_data_total_byte" int8 NOT NULL DEFAULT 0,
  "fs_data_used_pct" int4 NOT NULL DEFAULT 0,
  "fs_cache_total_byte" int8 NOT NULL DEFAULT 0,
  "fs_cache_used_pct" int4 NOT NULL DEFAULT 0,
  "data_oldest_time" int8 NOT NULL DEFAULT 0,
  "data_last24_total_byte" int8 NOT NULL DEFAULT 0,
  "data_predict_total_day" int8 NOT NULL DEFAULT 0,
  "cache_file_avg_byte" int8 NOT NULL DEFAULT 0,
  "fs_store_total_byte" int8 NOT NULL DEFAULT 0,
  "fs_system_total_byte" int8 NOT NULL DEFAULT 0,
  "fs_index_total_byte" int8 NOT NULL DEFAULT 0,
  "fs_metadata_total_byte" int8 NOT NULL DEFAULT 0,
  "fs_metadata_hot_total_byte" int8 NOT NULL DEFAULT 0,
  "fs_packet_total_byte" int8 NOT NULL DEFAULT 0,
  "metric_time" timestamptz(6) NOT NULL,
  CONSTRAINT "fpccms_central_system_metric_pkey" PRIMARY KEY ("id") 
)
;
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."device_type" IS '设备类型';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."monitored_serial_number" IS '被监控者设备编号';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."cpu_metric" IS 'CPU使用率';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."memory_metric" IS '内存使用率';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."system_fs_metric" IS '系统分区使用率';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."index_fs_metric" IS '索引分区使用率';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."metadata_fs_metric" IS '详单冷分区使用率';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."metadata_hot_fs_metric" IS '详单热分区使用率';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."packet_fs_metric" IS '全包分区使用率';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."fs_data_total_byte" IS '数据总存储空间大小';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."fs_data_used_pct" IS '数据存储空间已使用大小';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."fs_cache_total_byte" IS '缓存总空间大小';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."fs_cache_used_pct" IS '缓存空间已使用大小';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."data_oldest_time" IS '数据最早存储时间';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."data_last24_total_byte" IS '最近24小时存储总字节数';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."data_predict_total_day" IS '剩余空间可存储天数';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."cache_file_avg_byte" IS 'pcap缓存文件的平均大小';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."fs_store_total_byte" IS '全包存储空间总大小';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."fs_system_total_byte" IS '系统空间总大小';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."fs_index_total_byte" IS '索引空间总大小';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."fs_metadata_total_byte" IS '详单冷分区空间总大小';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."fs_metadata_hot_total_byte" IS '详单热分区空间总大小';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."fs_packet_total_byte" IS '数据包空间总大小';
COMMENT ON COLUMN "public"."fpccms_central_system_metric"."metric_time" IS '产生时间';

-- ----------------------------
-- Table structure for fpccms_central_system_metric_history
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_central_system_metric_history" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "device_type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "monitored_serial_number" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "cpu_metric" int4 NOT NULL DEFAULT 0,
  "memory_metric" int4 NOT NULL DEFAULT 0,
  "system_fs_metric" int4 NOT NULL DEFAULT 0,
  "metric_time" timestamptz(6) NOT NULL,
  CONSTRAINT "fpccms_central_system_metric_history_pkey" PRIMARY KEY ("id") 
)
;
COMMENT ON COLUMN "public"."fpccms_central_system_metric_history"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_central_system_metric_history"."device_type" IS '设备类型';
COMMENT ON COLUMN "public"."fpccms_central_system_metric_history"."monitored_serial_number" IS '被监控者设备编号';
COMMENT ON COLUMN "public"."fpccms_central_system_metric_history"."cpu_metric" IS 'CPU使用率';
COMMENT ON COLUMN "public"."fpccms_central_system_metric_history"."memory_metric" IS '内存使用率';
COMMENT ON COLUMN "public"."fpccms_central_system_metric_history"."system_fs_metric" IS '系统分区使用率';
COMMENT ON COLUMN "public"."fpccms_central_system_metric_history"."metric_time" IS '产生时间';

-- ----------------------------
-- Table structure for fpccms_central_system_metric_history_5min
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_central_system_metric_history_5min" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "device_type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "monitored_serial_number" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "cpu_metric" int4 NOT NULL DEFAULT 0,
  "memory_metric" int4 NOT NULL DEFAULT 0,
  "system_fs_metric" int4 NOT NULL DEFAULT 0,
  "metric_time" timestamptz(6) NOT NULL,
  CONSTRAINT "fpccms_central_system_metric_history_5min_pkey" PRIMARY KEY ("id") 
)
;
COMMENT ON COLUMN "public"."fpccms_central_system_metric_history_5min"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_central_system_metric_history_5min"."device_type" IS '设备类型';
COMMENT ON COLUMN "public"."fpccms_central_system_metric_history_5min"."monitored_serial_number" IS '被监控者设备编号';
COMMENT ON COLUMN "public"."fpccms_central_system_metric_history_5min"."cpu_metric" IS 'CPU使用率';
COMMENT ON COLUMN "public"."fpccms_central_system_metric_history_5min"."memory_metric" IS '内存使用率';
COMMENT ON COLUMN "public"."fpccms_central_system_metric_history_5min"."system_fs_metric" IS '系统分区使用率';
COMMENT ON COLUMN "public"."fpccms_central_system_metric_history_5min"."metric_time" IS '产生时间';

-- ----------------------------
-- Table structure for fpccms_central_device_netif
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_central_device_netif" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "device_type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "monitored_serial_number" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "netif_name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "state" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "category" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "specification" int4 NOT NULL DEFAULT 0,
  "rx_bps" int8 NOT NULL DEFAULT 0,
  "tx_bps" int8 NOT NULL DEFAULT 0,
  "rx_pps" int8 NOT NULL DEFAULT 0,
  "tx_pps" int8 NOT NULL DEFAULT 0,
  "metric_time" timestamptz(6) NOT NULL,
  CONSTRAINT "fpccms_central_device_netif_pkey" PRIMARY KEY ("id") 
)
;
COMMENT ON COLUMN "public"."fpccms_central_device_netif"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_central_device_netif"."device_type" IS '设备类型';
COMMENT ON COLUMN "public"."fpccms_central_device_netif"."monitored_serial_number" IS '被监控者设备编号';
COMMENT ON COLUMN "public"."fpccms_central_device_netif"."netif_name" IS '接口名称';
COMMENT ON COLUMN "public"."fpccms_central_device_netif"."state" IS '接口状态（0：UP；1：DOWN）';
COMMENT ON COLUMN "public"."fpccms_central_device_netif"."category" IS '接口用途（0：管理；1：流量接收；2：流量转发）';
COMMENT ON COLUMN "public"."fpccms_central_device_netif"."specification" IS '接口规格';
COMMENT ON COLUMN "public"."fpccms_central_device_netif"."rx_bps" IS '接收流量';
COMMENT ON COLUMN "public"."fpccms_central_device_netif"."tx_bps" IS '转发流量';
COMMENT ON COLUMN "public"."fpccms_central_device_netif"."rx_pps" IS '接收数据包';
COMMENT ON COLUMN "public"."fpccms_central_device_netif"."tx_pps" IS '转发数据包';
COMMENT ON COLUMN "public"."fpccms_central_device_netif"."metric_time" IS '产生时间';

-- ----------------------------
-- Table structure for fpccms_central_device_netif_history
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_central_device_netif_history" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "device_type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "monitored_serial_number" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "netif_name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "category" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "rx_bps" int8 NOT NULL DEFAULT 0,
  "tx_bps" int8 NOT NULL DEFAULT 0,
  "rx_pps" int8 NOT NULL DEFAULT 0,
  "tx_pps" int8 NOT NULL DEFAULT 0,
  "metric_time" timestamptz(6) NOT NULL,
  CONSTRAINT "fpccms_central_device_netif_history_pkey" PRIMARY KEY ("id") 
)
;
COMMENT ON COLUMN "public"."fpccms_central_device_netif_history"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_central_device_netif_history"."device_type" IS '设备类型';
COMMENT ON COLUMN "public"."fpccms_central_device_netif_history"."monitored_serial_number" IS '被监控者设备编号';
COMMENT ON COLUMN "public"."fpccms_central_device_netif_history"."netif_name" IS '网卡名称';
COMMENT ON COLUMN "public"."fpccms_central_device_netif_history"."category" IS '接口用途（0：管理；1：流量接收；2：流量转发）';
COMMENT ON COLUMN "public"."fpccms_central_device_netif_history"."rx_bps" IS '接收流量';
COMMENT ON COLUMN "public"."fpccms_central_device_netif_history"."tx_bps" IS '转发流量';
COMMENT ON COLUMN "public"."fpccms_central_device_netif_history"."rx_pps" IS '接收数据包';
COMMENT ON COLUMN "public"."fpccms_central_device_netif_history"."tx_pps" IS '转发数据包';
COMMENT ON COLUMN "public"."fpccms_central_device_netif_history"."metric_time" IS '产生时间';

-- ----------------------------
-- Table structure for fpccms_central_device_netif_history_5min
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_central_device_netif_history_5min" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "device_type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "monitored_serial_number" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "netif_name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "category" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "rx_bps" int8 NOT NULL DEFAULT 0,
  "tx_bps" int8 NOT NULL DEFAULT 0,
  "rx_pps" int8 NOT NULL DEFAULT 0,
  "tx_pps" int8 NOT NULL DEFAULT 0,
  "metric_time" timestamptz(6) NOT NULL,
  CONSTRAINT "fpccms_central_device_netif_history_5min_pkey" PRIMARY KEY ("id") 
)
;
COMMENT ON COLUMN "public"."fpccms_central_device_netif_history_5min"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_central_device_netif_history_5min"."device_type" IS '设备类型';
COMMENT ON COLUMN "public"."fpccms_central_device_netif_history_5min"."monitored_serial_number" IS '被监控者设备编号';
COMMENT ON COLUMN "public"."fpccms_central_device_netif_history_5min"."netif_name" IS '网卡名称';
COMMENT ON COLUMN "public"."fpccms_central_device_netif_history_5min"."category" IS '接口用途';
COMMENT ON COLUMN "public"."fpccms_central_device_netif_history_5min"."rx_bps" IS '接收流量';
COMMENT ON COLUMN "public"."fpccms_central_device_netif_history_5min"."tx_bps" IS '转发流量';
COMMENT ON COLUMN "public"."fpccms_central_device_netif_history_5min"."rx_pps" IS '接收数据包';
COMMENT ON COLUMN "public"."fpccms_central_device_netif_history_5min"."tx_pps" IS '转发数据包';
COMMENT ON COLUMN "public"."fpccms_central_device_netif_history_5min"."metric_time" IS '产生时间';

-- ----------------------------
-- Table structure for fpccms_central_sendup_message
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_central_sendup_message" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "message_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "device_type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "device_serial_number" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "start_time" timestamptz(6),
  "end_time" timestamptz(6),
  "type" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "content" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "result" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  CONSTRAINT "fpccms_central_sendup_message_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_central_sendup_message"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_central_sendup_message"."message_id" IS '消息id';
COMMENT ON COLUMN "public"."fpccms_central_sendup_message"."device_type" IS '消息所属设备类型';
COMMENT ON COLUMN "public"."fpccms_central_sendup_message"."device_serial_number" IS '消息所属设备序列号';
COMMENT ON COLUMN "public"."fpccms_central_sendup_message"."start_time" IS '统计时间范围（开始时间）';
COMMENT ON COLUMN "public"."fpccms_central_sendup_message"."end_time" IS '统计时间范围（结束时间）';
COMMENT ON COLUMN "public"."fpccms_central_sendup_message"."type" IS '上报的消息类型（0：系统状态；1：日志告警；）';
COMMENT ON COLUMN "public"."fpccms_central_sendup_message"."content" IS '上报的内容';
COMMENT ON COLUMN "public"."fpccms_central_sendup_message"."result" IS '上报的结果（0：上报成功；1：上报失败；）';
COMMENT ON COLUMN "public"."fpccms_central_sendup_message"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_central_sendup_message"."update_time" IS '更新时间';

-- ----------------------------
-- Table structure for fpccms_broker_collect_metric
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_broker_collect_metric" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "device_type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "device_serial_number" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "start_time" timestamptz(6),
  "end_time" timestamptz(6),
  "type" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "collect_amount" int4 NOT NULL DEFAULT 0,
  "entity_amount" int4 NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  CONSTRAINT "fpccms_broker_collect_metric_pkey" PRIMARY KEY ("id") 
)
;
COMMENT ON COLUMN "public"."fpccms_broker_collect_metric"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_broker_collect_metric"."device_type" IS '设备类型（tfa/cms）';
COMMENT ON COLUMN "public"."fpccms_broker_collect_metric"."device_serial_number" IS '设备序列号';
COMMENT ON COLUMN "public"."fpccms_broker_collect_metric"."start_time" IS '统计时间范围（开始时间）';
COMMENT ON COLUMN "public"."fpccms_broker_collect_metric"."end_time" IS '统计时间范围（结束时间）';
COMMENT ON COLUMN "public"."fpccms_broker_collect_metric"."type" IS '上报的消息类型（0：系统状态；1：日志告警；）';
COMMENT ON COLUMN "public"."fpccms_broker_collect_metric"."collect_amount" IS '统计时间范围内收到的上报数量';
COMMENT ON COLUMN "public"."fpccms_broker_collect_metric"."entity_amount" IS '统计时间范围内收到的内容数量';
COMMENT ON COLUMN "public"."fpccms_broker_collect_metric"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_broker_collect_metric"."update_time" IS '更新时间';

-- ----------------------------
-- Table structure for fpccms_appliance_custom_time
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_custom_time" (
	"id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "assign_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
	"name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
	"type" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
	"period" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
	"custom_time_setting" text COLLATE "pg_catalog"."default" DEFAULT '',
	"deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
	"create_time" timestamptz(6),
	"update_time" timestamptz(6),
	"delete_time" timestamptz(6),
	"operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
	CONSTRAINT "fpccms_appliance_custom_time_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpccms_appliance_custom_time"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_custom_time"."assign_id" IS '自定义时间在上级cms中的id';
COMMENT ON COLUMN "public"."fpccms_appliance_custom_time"."name" IS '自定义时间名称';
COMMENT ON COLUMN "public"."fpccms_appliance_custom_time"."type" IS '类型（0：周期性时间；1：一次性时间）';
COMMENT ON COLUMN "public"."fpccms_appliance_custom_time"."period" IS '星期';
COMMENT ON COLUMN "public"."fpccms_appliance_custom_time"."custom_time_setting" IS '自定义时间';
COMMENT ON COLUMN "public"."fpccms_appliance_custom_time"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_appliance_custom_time"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_custom_time"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_custom_time"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_custom_time"."operator_id" IS '操作人id';
    
-- ----------------------------
-- Table structure for fpccms_appliance_external_receiver
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_external_receiver" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "receiver_type" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "receiver_content" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "assign_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
    "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "delete_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    CONSTRAINT "fpccms_appliance_external_receiver_pkey" PRIMARY KEY ("id")
    )
;
COMMENT ON COLUMN "public"."fpccms_appliance_external_receiver"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_external_receiver"."name" IS '名称';
COMMENT ON COLUMN "public"."fpccms_appliance_external_receiver"."receiver_type" IS '外发服务器类型（0：mail；1：syslog；2：kafka；3：zmq）';
COMMENT ON COLUMN "public"."fpccms_appliance_external_receiver"."receiver_content" IS '外发服务器内容';
COMMENT ON COLUMN "public"."fpccms_appliance_external_receiver"."assign_id" IS '外发服务器在上级CMS中的ID';
COMMENT ON COLUMN "public"."fpccms_appliance_external_receiver"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_appliance_external_receiver"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_external_receiver"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_external_receiver"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_external_receiver"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_appliance_send_rule
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_send_rule" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "send_rule_content" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "assign_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
    "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "delete_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    CONSTRAINT "fpccms_appliance_send_rule_pkey" PRIMARY KEY ("id")
    )
;
COMMENT ON COLUMN "public"."fpccms_appliance_send_rule"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_send_rule"."name" IS '名称';
COMMENT ON COLUMN "public"."fpccms_appliance_send_rule"."send_rule_content" IS '外发规则内容';
COMMENT ON COLUMN "public"."fpccms_appliance_send_rule"."assign_id" IS '外发规则在上级CMS中的ID';
COMMENT ON COLUMN "public"."fpccms_appliance_send_rule"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_appliance_send_rule"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_send_rule"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_send_rule"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_send_rule"."operator_id" IS '操作人id';


-- ----------------------------
-- Table structure for fpccms_appliance_send_policy
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_send_policy" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "external_receiver_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "send_rule_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "assign_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
    "state" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "delete_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    CONSTRAINT "fpccms_appliance_send_policy_pkey" PRIMARY KEY ("id")
    )
;
COMMENT ON COLUMN "public"."fpccms_appliance_send_policy"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_send_policy"."name" IS '名称';
COMMENT ON COLUMN "public"."fpccms_appliance_send_policy"."external_receiver_id" IS '外发服务器ID';
COMMENT ON COLUMN "public"."fpccms_appliance_send_policy"."send_rule_id" IS '外发规则ID';
COMMENT ON COLUMN "public"."fpccms_appliance_send_policy"."assign_id" IS '外发策略在上级CMS中的ID';
COMMENT ON COLUMN "public"."fpccms_appliance_send_policy"."state" IS '启用状态（0：禁用，1：启用）';
COMMENT ON COLUMN "public"."fpccms_appliance_send_policy"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_appliance_send_policy"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_send_policy"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_send_policy"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_send_policy"."operator_id" IS '操作人id';
    
-- ----------------------------
-- Table structure for fpccms_appliance_ip_conversations_history
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_ip_conversations_history" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "data" text COLLATE "pg_catalog"."default" DEFAULT '',
    "deleted" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "delete_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
    CONSTRAINT "fpccms_appliance_ip_conversations_history_pkey" PRIMARY KEY ("id")
    )
;
COMMENT ON COLUMN "public"."fpccms_appliance_ip_conversations_history"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_ip_conversations_history"."name" IS '历史画布名称';
COMMENT ON COLUMN "public"."fpccms_appliance_ip_conversations_history"."data" IS '历史画布数据';
COMMENT ON COLUMN "public"."fpccms_appliance_ip_conversations_history"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpccms_appliance_ip_conversations_history"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_ip_conversations_history"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_ip_conversations_history"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpccms_appliance_ip_conversations_history"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpccms_appliance_ip_detections_layouts
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpccms_appliance_ip_detections_layouts" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "layouts" text COLLATE "pg_catalog"."default" DEFAULT '',
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    CONSTRAINT "fpccms_appliance_ip_detections_layouts_pkey" PRIMARY KEY ("id")
    )
;
COMMENT ON COLUMN "public"."fpccms_appliance_ip_detections_layouts"."id" IS '主键';
COMMENT ON COLUMN "public"."fpccms_appliance_ip_detections_layouts"."layouts" IS 'IP画像页面布局参数';
COMMENT ON COLUMN "public"."fpccms_appliance_ip_detections_layouts"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpccms_appliance_ip_detections_layouts"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpccms_appliance_ip_detections_layouts"."operator_id" IS '操作人id';