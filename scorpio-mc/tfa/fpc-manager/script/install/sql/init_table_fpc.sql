-- ----------------------------
-- Table structure for fpc_appliance_sa_category
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_sa_category" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "category_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "category_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_sa_category_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_sa_category"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_sa_category"."name" IS '分类名称';
COMMENT ON COLUMN "public"."fpc_appliance_sa_category"."category_id" IS '分类ID（范围为101-150, 最多允许添加50个）';
COMMENT ON COLUMN "public"."fpc_appliance_sa_category"."category_in_cms_id" IS '自定义分类在cms中的ID';
COMMENT ON COLUMN "public"."fpc_appliance_sa_category"."description" IS '备注';
COMMENT ON COLUMN "public"."fpc_appliance_sa_category"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_sa_category"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_sa_category"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_sa_category"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_sa_category"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_sa_subcategory
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_sa_subcategory" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "sub_category_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "category_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "sub_category_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_sa_subcategory_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_sa_subcategory"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_sa_subcategory"."name" IS '子分类名称';
COMMENT ON COLUMN "public"."fpc_appliance_sa_subcategory"."sub_category_id" IS '子分类ID（范围为101-200，最多允许添加100个）';
COMMENT ON COLUMN "public"."fpc_appliance_sa_subcategory"."category_id" IS '所属分类ID';
COMMENT ON COLUMN "public"."fpc_appliance_sa_subcategory"."sub_category_in_cms_id" IS '自定义子分类在cms中的ID';
COMMENT ON COLUMN "public"."fpc_appliance_sa_subcategory"."description" IS '备注';
COMMENT ON COLUMN "public"."fpc_appliance_sa_subcategory"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_sa_subcategory"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_sa_subcategory"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_sa_subcategory"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_sa_subcategory"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_sa_application
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_sa_application" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "application_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "category_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "sub_category_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "l7_protocol_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "rule" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "application_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_sa_application_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_sa_application"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_sa_application"."name" IS '应用名称';
COMMENT ON COLUMN "public"."fpc_appliance_sa_application"."application_id" IS '应用ID（数字大小在5万和6万之间）';
COMMENT ON COLUMN "public"."fpc_appliance_sa_application"."category_id" IS '所属分类ID';
COMMENT ON COLUMN "public"."fpc_appliance_sa_application"."sub_category_id" IS '所属子分类ID';
COMMENT ON COLUMN "public"."fpc_appliance_sa_application"."l7_protocol_id" IS '七层承载协议ID';
COMMENT ON COLUMN "public"."fpc_appliance_sa_application"."rule" IS '规则（JSON）';
COMMENT ON COLUMN "public"."fpc_appliance_sa_application"."application_in_cms_id" IS '自定义应用在cms中的ID';
COMMENT ON COLUMN "public"."fpc_appliance_sa_application"."description" IS '备注';
COMMENT ON COLUMN "public"."fpc_appliance_sa_application"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_sa_application"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_sa_application"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_sa_application"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_sa_application"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_sa_hierarchy
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_sa_hierarchy" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "type" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "category_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "sub_category_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "application_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "create_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_sa_hierarchy_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_sa_hierarchy"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_sa_hierarchy"."type" IS '类型';
COMMENT ON COLUMN "public"."fpc_appliance_sa_hierarchy"."category_id" IS '分类ID';
COMMENT ON COLUMN "public"."fpc_appliance_sa_hierarchy"."sub_category_id" IS '子分类ID';
COMMENT ON COLUMN "public"."fpc_appliance_sa_hierarchy"."application_id" IS '应用ID';
COMMENT ON COLUMN "public"."fpc_appliance_sa_hierarchy"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_sa_hierarchy"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_geoip_country
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_geoip_country" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(128) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "country_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "longitude" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "latitude" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "custom_country_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_geoip_country_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_geoip_country"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_geoip_country"."name" IS '应用名称';
COMMENT ON COLUMN "public"."fpc_appliance_geoip_country"."country_id" IS '国家地区ID（范围[300-499]）';
COMMENT ON COLUMN "public"."fpc_appliance_geoip_country"."longitude" IS '经度';
COMMENT ON COLUMN "public"."fpc_appliance_geoip_country"."latitude" IS '纬度';
COMMENT ON COLUMN "public"."fpc_appliance_geoip_country"."custom_country_in_cms_id" IS '自定义地区在cms中的ID';
COMMENT ON COLUMN "public"."fpc_appliance_geoip_country"."description" IS '描述';
COMMENT ON COLUMN "public"."fpc_appliance_geoip_country"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_geoip_country"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_geoip_country"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_geoip_country"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_geoip_country"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_geoip_settings
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_geoip_settings" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "country_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '0',
  "province_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '0',
  "city_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '0',
  "ip_address" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "geoip_setting_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_geoip_settings_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_geoip_settings"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_geoip_settings"."country_id" IS '国家ID';
COMMENT ON COLUMN "public"."fpc_appliance_geoip_settings"."province_id" IS '省份ID';
COMMENT ON COLUMN "public"."fpc_appliance_geoip_settings"."city_id" IS '城市ID';
COMMENT ON COLUMN "public"."fpc_appliance_geoip_settings"."ip_address" IS '地区所包含IP地址';
COMMENT ON COLUMN "public"."fpc_appliance_geoip_settings"."geoip_setting_in_cms_id" IS '地区ip在cms中的ID';
COMMENT ON COLUMN "public"."fpc_appliance_geoip_settings"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_geoip_settings"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_geoip_settings"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_geoip_settings"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_geoip_settings"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_ingest_policy
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_ingest_policy" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "default_action" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "except_bpf" varchar(1024) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "except_tuple" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deduplication" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "ingest_policy_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_ingest_policy_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_ingest_policy"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_ingest_policy"."name" IS '名称';
COMMENT ON COLUMN "public"."fpc_appliance_ingest_policy"."default_action" IS '默认动作（0：存储；1：不存储）';
COMMENT ON COLUMN "public"."fpc_appliance_ingest_policy"."except_bpf" IS '例外条件（BPF表达式）';
COMMENT ON COLUMN "public"."fpc_appliance_ingest_policy"."except_tuple" IS '例外条件（六元组json）';
COMMENT ON COLUMN "public"."fpc_appliance_ingest_policy"."deduplication" IS '报文去重（0：去重；1：不去重）';
COMMENT ON COLUMN "public"."fpc_appliance_ingest_policy"."ingest_policy_in_cms_id" IS '捕获过滤规则在cms中的ID';
COMMENT ON COLUMN "public"."fpc_appliance_ingest_policy"."description" IS '备注';
COMMENT ON COLUMN "public"."fpc_appliance_ingest_policy"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_ingest_policy"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_ingest_policy"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_ingest_policy"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_ingest_policy"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_forward_rule
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_forward_rule" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
    "default_action" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "except_bpf" text COLLATE "pg_catalog"."default" DEFAULT '',
    "except_tuple" text COLLATE "pg_catalog"."default" DEFAULT '',
    "forward_rule_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
    "description" varchar(512) COLLATE "pg_catalog"."default" DEFAULT '',
    "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "deleted_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    CONSTRAINT "fpc_appliance_forward_rule_pkey" PRIMARY KEY ("id")
    )
;
COMMENT ON COLUMN "public"."fpc_appliance_forward_rule"."id" IS '规则主键';
COMMENT ON COLUMN "public"."fpc_appliance_forward_rule"."name" IS '规则名称';
COMMENT ON COLUMN "public"."fpc_appliance_forward_rule"."default_action" IS '默认动作（0：不转发；1：转发）';
COMMENT ON COLUMN "public"."fpc_appliance_forward_rule"."except_bpf" IS 'BPF语法';
COMMENT ON COLUMN "public"."fpc_appliance_forward_rule"."except_tuple" IS '规则过滤';
COMMENT ON COLUMN "public"."fpc_appliance_forward_rule"."forward_rule_in_cms_id" IS '转发过滤规则在CMS中的ID';
COMMENT ON COLUMN "public"."fpc_appliance_forward_rule"."description" IS '备注';
COMMENT ON COLUMN "public"."fpc_appliance_forward_rule"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_forward_rule"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_forward_rule"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."fpc_appliance_forward_rule"."deleted_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_forward_rule"."operator_id" IS '操作人ID';


-- ----------------------------
-- Table structure for fpc_appliance_forward_policy
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_forward_policy" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
    "rule_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "netif_name" text COLLATE "pg_catalog"."default" NOT NULL,
    "ip_tunnel" text COLLATE "pg_catalog"."default" NOT NULL,
    "load_balance" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
    "forward_policy_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
    "state" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "description" varchar(512) COLLATE "pg_catalog"."default" DEFAULT '',
    "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "deleted_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    CONSTRAINT "fpc_appliance_forward_policy_pkey" PRIMARY KEY ("id")
    )
;
COMMENT ON COLUMN "public"."fpc_appliance_forward_policy"."id" IS '转发策略主键';
COMMENT ON COLUMN "public"."fpc_appliance_forward_policy"."name" IS '策略名称';
COMMENT ON COLUMN "public"."fpc_appliance_forward_policy"."rule_id" IS '转发策略规则ID';
COMMENT ON COLUMN "public"."fpc_appliance_forward_policy"."netif_name" IS '转发接口数组（["netif_1","netif_2"]）';
COMMENT ON COLUMN "public"."fpc_appliance_forward_policy"."ip_tunnel" IS '隧道封装（{"mode":"","params":[]}）';
COMMENT ON COLUMN "public"."fpc_appliance_forward_policy"."load_balance" IS '负载均衡（null/srcIp/srcIp_destIp/srcIp_destIp_srcPort_destPort）';
COMMENT ON COLUMN "public"."fpc_appliance_forward_policy"."forward_policy_in_cms_id" IS '转发过滤策略在CMS中的ID';
COMMENT ON COLUMN "public"."fpc_appliance_forward_policy"."state" IS '启用状态（0：禁用，1：启用）';
COMMENT ON COLUMN "public"."fpc_appliance_forward_policy"."description" IS '备注';
COMMENT ON COLUMN "public"."fpc_appliance_forward_policy"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_forward_policy"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_forward_policy"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."fpc_appliance_forward_policy"."deleted_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_forward_policy"."operator_id" IS '操作人ID';


-- ----------------------------
-- 历史版本，已弃用
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_filter_policy" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "default_action" int4 NOT NULL,
  "except_application" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "except_flow" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 1,
  "filter_policy_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_filter_policy_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_filter_policy"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_filter_policy"."name" IS '名称';
COMMENT ON COLUMN "public"."fpc_appliance_filter_policy"."default_action" IS '默认动作（0：存储，1：不存储，N：截断存储字节数[64-1500]）';
COMMENT ON COLUMN "public"."fpc_appliance_filter_policy"."except_application" IS '例外应用存储方式（json格式：{applicationId：storeAction}，例：{1:123,2:0,3:1}）';
COMMENT ON COLUMN "public"."fpc_appliance_filter_policy"."except_flow" IS '例外流量是否存储（0：不存储，1：存储）';
COMMENT ON COLUMN "public"."fpc_appliance_filter_policy"."filter_policy_in_cms_id" IS '存储过滤规则在cms中的ID';
COMMENT ON COLUMN "public"."fpc_appliance_filter_policy"."description" IS '备注';
COMMENT ON COLUMN "public"."fpc_appliance_filter_policy"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_filter_policy"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_filter_policy"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_filter_policy"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_filter_policy"."operator_id" IS '操作人id';


-- ----------------------------
-- Table structure for fpc_appliance_storage_filter_rule
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_storage_filter_rule" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "tuple" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "storage_rule_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
    "state" char COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "priority" integer  NOT NULL,
    "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "delete_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    CONSTRAINT "fpc_appliance_storage_filter_rule_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_storage_filter_rule"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_storage_filter_rule"."name" IS '名称';
COMMENT ON COLUMN "public"."fpc_appliance_storage_filter_rule"."tuple" IS '存储过滤条件';
COMMENT ON COLUMN "public"."fpc_appliance_storage_filter_rule"."storage_rule_in_cms_id" IS '存储过滤规则在cms中的ID';
COMMENT ON COLUMN "public"."fpc_appliance_storage_filter_rule"."description" IS '备注';
COMMENT ON COLUMN "public"."fpc_appliance_storage_filter_rule"."priority" IS '过滤规则优先级';
COMMENT ON COLUMN "public"."fpc_appliance_storage_filter_rule"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_storage_filter_rule"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_storage_filter_rule"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_storage_filter_rule"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_storage_filter_rule"."operator_id" IS '操作人id';


-- ----------------------------
-- Table structure for fpc_appliance_storage_policy
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_storage_policy" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "compress_action" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "encrypt_action" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 1,
  "encrypt_algorithm" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_storage_policy_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_storage_policy"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_storage_policy"."compress_action" IS '压缩动作（0：压缩；1：不压缩）';
COMMENT ON COLUMN "public"."fpc_appliance_storage_policy"."encrypt_action" IS '加密动作（0：加密；1：不加密）';
COMMENT ON COLUMN "public"."fpc_appliance_storage_policy"."encrypt_algorithm" IS '加密算法';
COMMENT ON COLUMN "public"."fpc_appliance_storage_policy"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_storage_policy"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_storage_policy"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_storage_policy"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_storage_policy"."operator_id" IS '操作人id';


-- ----------------------------
-- Table structure for fpc_appliance_storage_space
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_storage_space" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "space_type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "capacity" int8 NOT NULL DEFAULT 0,
  "update_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_storage_space_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_storage_space"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_storage_space"."space_type" IS '存储类型（fs_data、offline_pcap、fs_cache、transmit_task_file_limit）';
COMMENT ON COLUMN "public"."fpc_appliance_storage_space"."capacity" IS '容量限制';
COMMENT ON COLUMN "public"."fpc_appliance_storage_space"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_storage_space"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_host_group
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_host_group" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "ip_address" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "host_group_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_host_group_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_host_group"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_host_group"."name" IS '名称';
COMMENT ON COLUMN "public"."fpc_appliance_host_group"."ip_address" IS 'IP地址';
COMMENT ON COLUMN "public"."fpc_appliance_host_group"."host_group_in_cms_id" IS 'IP地址组在cms中的ID';
COMMENT ON COLUMN "public"."fpc_appliance_host_group"."description" IS '备注';
COMMENT ON COLUMN "public"."fpc_appliance_host_group"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_host_group"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_host_group"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_host_group"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_host_group"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_network
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_network" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "netif_type" char(1) COLLATE "pg_catalog"."default" NOT NULL,
  "extra_settings" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "report_state" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
  "report_action" char(1) COLLATE "pg_catalog"."default" DEFAULT 1,
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_network_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_network"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_network"."name" IS '网络名称';
COMMENT ON COLUMN "public"."fpc_appliance_network"."netif_type" IS '接口流量方向（0：单向流量；1：双向流量）';
COMMENT ON COLUMN "public"."fpc_appliance_network"."extra_settings" IS '网络额外配置（{"flowlogDefaultAction": "1","flowlogExceptStatistics": "interframe,preamble","flowlogExceptStatus": "syn_sent","metadataDefaultAction": "1","sessionVlanAction":"1"}）';
COMMENT ON COLUMN "public"."fpc_appliance_network"."report_state" IS '上报状态（0：未上报；1：已上报）';
COMMENT ON COLUMN "public"."fpc_appliance_network"."report_action" IS '上报动作（1：新增；2：修改；3：删除）';
COMMENT ON COLUMN "public"."fpc_appliance_network"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_network"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_network"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_network"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_network"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_network_netif
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_network_netif" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "network_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "netif_name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "specification" int4 NOT NULL,
  "direction" varchar(16) COLLATE "pg_catalog"."default" NOT NULL,
  "timestamp" timestamptz(6) NOT NULL,
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_network_netif_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_network_netif"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_network_netif"."network_id" IS '网络id';
COMMENT ON COLUMN "public"."fpc_appliance_network_netif"."netif_name" IS '接口名称';
COMMENT ON COLUMN "public"."fpc_appliance_network_netif"."specification" IS '配置带宽（Mbps）';
COMMENT ON COLUMN "public"."fpc_appliance_network_netif"."direction" IS '接口方向（upstream | downstream | hybrid）';
COMMENT ON COLUMN "public"."fpc_appliance_network_netif"."timestamp" IS '时间戳';
COMMENT ON COLUMN "public"."fpc_appliance_network_netif"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_network_inside_ip
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_network_inside_ip" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "network_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "ip_address" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "ip_start" int8 NOT NULL DEFAULT 0,
  "ip_end" int8 NOT NULL DEFAULT 0,
  "timestamp" timestamptz(6) NOT NULL,
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_network_inside_ip_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_network_inside_ip"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_network_inside_ip"."network_id" IS '网络id';
COMMENT ON COLUMN "public"."fpc_appliance_network_inside_ip"."ip_address" IS 'IP地址/段';
COMMENT ON COLUMN "public"."fpc_appliance_network_inside_ip"."ip_start" IS '起始IP地址';
COMMENT ON COLUMN "public"."fpc_appliance_network_inside_ip"."ip_end" IS '结束IP地址';
COMMENT ON COLUMN "public"."fpc_appliance_network_inside_ip"."timestamp" IS '时间戳';
COMMENT ON COLUMN "public"."fpc_appliance_network_inside_ip"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_network_policy
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_network_policy" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "network_policy_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
    "network_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "policy_type" varchar(16) COLLATE "pg_catalog"."default" NOT NULL,
    "policy_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "timestamp" timestamptz(6) NOT NULL,
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    CONSTRAINT "fpc_appliance_network_policy_pkey" PRIMARY KEY ("id")
    )
;
COMMENT ON COLUMN "public"."fpc_appliance_network_policy"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_network_policy"."network_policy_in_cms_id" IS 'CMS中id';
COMMENT ON COLUMN "public"."fpc_appliance_network_policy"."network_id" IS '网络id';
COMMENT ON COLUMN "public"."fpc_appliance_network_policy"."policy_type" IS '策略类型（ingest：捕获策略；filter：过滤策略；forward：转发策略；send：外发策略）';
COMMENT ON COLUMN "public"."fpc_appliance_network_policy"."policy_id" IS '策略id';
COMMENT ON COLUMN "public"."fpc_appliance_network_policy"."timestamp" IS '时间戳';
COMMENT ON COLUMN "public"."fpc_appliance_network_policy"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_network_topology
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_network_topology" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "topology" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "metric" varchar(1024) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "timestamp" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_network_topology_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_network_topology"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_network_topology"."topology" IS '网元链路配置';
COMMENT ON COLUMN "public"."fpc_appliance_network_topology"."metric" IS '指标配置(csv格式)';
COMMENT ON COLUMN "public"."fpc_appliance_network_topology"."timestamp" IS '配置时间';
COMMENT ON COLUMN "public"."fpc_appliance_network_topology"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_logical_subnet
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_logical_subnet" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "network_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "bandwidth" int4 NOT NULL,
  "type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "configuration" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "subnet_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_logical_subnet_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_logical_subnet"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_logical_subnet"."name" IS '名称';
COMMENT ON COLUMN "public"."fpc_appliance_logical_subnet"."network_id" IS '所属网络ID';
COMMENT ON COLUMN "public"."fpc_appliance_logical_subnet"."bandwidth" IS '总带宽（Mbps）';
COMMENT ON COLUMN "public"."fpc_appliance_logical_subnet"."type" IS '子网类型（ip：IP子网络；mac：MAC子网络；vlan：VLAN子网络；mpls：MPLS子网络；gre：GRE子网络；vxlan：VXLAN子网络）';
COMMENT ON COLUMN "public"."fpc_appliance_logical_subnet"."configuration" IS '子网配置，子网类型的不同则配置不同';
COMMENT ON COLUMN "public"."fpc_appliance_logical_subnet"."subnet_in_cms_id" IS '子网在cms中的ID';
COMMENT ON COLUMN "public"."fpc_appliance_logical_subnet"."description" IS '备注';
COMMENT ON COLUMN "public"."fpc_appliance_logical_subnet"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_logical_subnet"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_logical_subnet"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_logical_subnet"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_logical_subnet"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_service
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_service" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "application" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "service_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_service_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_service"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_service"."name" IS '名称';
COMMENT ON COLUMN "public"."fpc_appliance_service"."application" IS '业务所包含的应用的ID集合(csv格式)';
COMMENT ON COLUMN "public"."fpc_appliance_service"."service_in_cms_id" IS '业务在cms中的ID';
COMMENT ON COLUMN "public"."fpc_appliance_service"."description" IS '备注';
COMMENT ON COLUMN "public"."fpc_appliance_service"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_service"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_service"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_service"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_service"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_service_dashboard_settings
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_service_dashboard_settings" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "parameters" text COLLATE "pg_catalog"."default" DEFAULT '',
  "percent_parameter" char(1) NOT NULL DEFAULT 0,
  "time_window_parameter" char(1) NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_service_dashboard_settings_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_service_dashboard_settings"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_service_dashboard_settings"."parameters" IS '参数数组';
COMMENT ON COLUMN "public"."fpc_appliance_service_dashboard_settings"."percent_parameter" IS '百分比参数(0:连接成功率 1:客户端重传率 2:服务端重传率)';
COMMENT ON COLUMN "public"."fpc_appliance_service_dashboard_settings"."time_window_parameter" IS '时间窗口参数(0:流量趋势图 1:告警分布图)';
COMMENT ON COLUMN "public"."fpc_appliance_service_dashboard_settings"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_service_dashboard_settings"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_service_dashboard_settings"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_service_network
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_service_network" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "service_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "network_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  CONSTRAINT "fpc_appliance_service_network_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_service_network"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_service_network"."service_id" IS '业务id';
COMMENT ON COLUMN "public"."fpc_appliance_service_network"."network_id" IS '网络/子网络id';

-- ----------------------------
-- Table structure for fpc_appliance_service_link
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_service_link" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "service_link_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "service_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "link" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "metric" varchar(1024) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "timestamp" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_service_link_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_service_link"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_service_link"."service_link_in_cms_id" IS '业务路径在cms中的ID';
COMMENT ON COLUMN "public"."fpc_appliance_service_link"."service_id" IS '业务ID';
COMMENT ON COLUMN "public"."fpc_appliance_service_link"."link" IS '网元链路配置';
COMMENT ON COLUMN "public"."fpc_appliance_service_link"."metric" IS '指标配置(csv格式)';
COMMENT ON COLUMN "public"."fpc_appliance_service_link"."timestamp" IS '配置时间';
COMMENT ON COLUMN "public"."fpc_appliance_service_link"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_user_service_follow
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_user_service_follow" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "user_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "service_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "network_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "follow_time" timestamptz(6),
  CONSTRAINT "fpc_appliance_user_service_follow_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_user_service_follow"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_user_service_follow"."user_id" IS '用户id';
COMMENT ON COLUMN "public"."fpc_appliance_user_service_follow"."service_id" IS '业务ID';
COMMENT ON COLUMN "public"."fpc_appliance_user_service_follow"."network_id" IS '网络/子网络ID';
COMMENT ON COLUMN "public"."fpc_appliance_user_service_follow"."follow_time" IS '关注时间';

-- ----------------------------
-- Table structure for fpc_appliance_metric_settings
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_metric_settings" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "source_type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "network_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "service_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "packet_file_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "metric" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "value" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "metric_setting_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "update_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_metric_settings_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_metric_settings"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_metric_settings"."source_type" IS '来源类型（network/service/packetFile）';
COMMENT ON COLUMN "public"."fpc_appliance_metric_settings"."network_id" IS '网络/子网络ID';
COMMENT ON COLUMN "public"."fpc_appliance_metric_settings"."service_id" IS '业务ID';
COMMENT ON COLUMN "public"."fpc_appliance_metric_settings"."packet_file_id" IS '离线数据包文件ID';
COMMENT ON COLUMN "public"."fpc_appliance_metric_settings"."metric" IS '指标（server_response_normal/server_response_timeout/long_connection）';
COMMENT ON COLUMN "public"."fpc_appliance_metric_settings"."value" IS '参数值';
COMMENT ON COLUMN "public"."fpc_appliance_metric_settings"."metric_setting_in_cms_id" IS 'metricSetting在cms中的ID';
COMMENT ON COLUMN "public"."fpc_appliance_metric_settings"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."fpc_appliance_metric_settings"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_baseline_settings
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_baseline_settings" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "source_type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "network_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "service_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "category" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "weighting_model" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "windowing_model" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "windowing_count" int4 NOT NULL,
  "update_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_baseline_settings_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_baseline_settings"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_baseline_settings"."source_type" IS '来源类型（network/service）';
COMMENT ON COLUMN "public"."fpc_appliance_baseline_settings"."network_id" IS '网络/子网络ID';
COMMENT ON COLUMN "public"."fpc_appliance_baseline_settings"."service_id" IS '业务ID';
COMMENT ON COLUMN "public"."fpc_appliance_baseline_settings"."category" IS '基线类型（bandwidth/flow/packet/responseLatency）';
COMMENT ON COLUMN "public"."fpc_appliance_baseline_settings"."weighting_model" IS '权重模型（最小：MIN；最大：MAX；均值：MEAN；中位数：MEDIAN）';
COMMENT ON COLUMN "public"."fpc_appliance_baseline_settings"."windowing_model" IS '基线窗口（天同比：minute_of_day/five_minute_of_day/hour_of_day；周同比：minute_of_week/five_minute_of_week/hour_of_week；环比：last_n_minutes/last_n_five_minutes/last_n_hours；）';
COMMENT ON COLUMN "public"."fpc_appliance_baseline_settings"."windowing_count" IS '回顾周期';
COMMENT ON COLUMN "public"."fpc_appliance_baseline_settings"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."fpc_appliance_baseline_settings"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_baseline_value
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_baseline_value" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "source_type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "source_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "alert_network_id" varchar(64) COLLATE "pg_catalog"."default",
  "alert_service_id" varchar(64) COLLATE "pg_catalog"."default",
  "value" double precision NOT NULL,
  "calculate_time" timestamptz(6),
  "timestamp" timestamptz(6),
  CONSTRAINT "fpc_appliance_baseline_value_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_baseline_value"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_baseline_value"."source_type" IS '基线定义来源（告警alert、网络性能npm）';
COMMENT ON COLUMN "public"."fpc_appliance_baseline_value"."source_id" IS '基线定义ID（基线定义表ID或业务/网络告警ID）';
COMMENT ON COLUMN "public"."fpc_appliance_baseline_value"."alert_network_id" IS '业务告警所属网络/子网ID';
COMMENT ON COLUMN "public"."fpc_appliance_baseline_value"."alert_service_id" IS '业务告警所属业务ID';
COMMENT ON COLUMN "public"."fpc_appliance_baseline_value"."value" IS '基线计算结果';
COMMENT ON COLUMN "public"."fpc_appliance_baseline_value"."calculate_time" IS '计算时间';
COMMENT ON COLUMN "public"."fpc_appliance_baseline_value"."timestamp" IS '写入时间';

-- ----------------------------
-- Table structure for fpc_appliance_alert_rule
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_alert_rule" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(128) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "category" varchar(10) COLLATE "pg_catalog"."default" NOT NULL,
  "level" char(1) COLLATE "pg_catalog"."default" NOT NULL,
  "threshold_settings" text COLLATE "pg_catalog"."default",
  "trend_settings" text COLLATE "pg_catalog"."default",
  "advanced_settings" text COLLATE "pg_catalog"."default",
  "refire" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "status" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 1,
  "alert_rule_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_alert_rule_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_alert_rule"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_alert_rule"."name" IS '名称';
COMMENT ON COLUMN "public"."fpc_appliance_alert_rule"."category" IS '分类（threshold：阈值；trend：基线；advanced：组合）';
COMMENT ON COLUMN "public"."fpc_appliance_alert_rule"."level" IS '级别（0：提示；1：一般；2：重要；3：紧急）';
COMMENT ON COLUMN "public"."fpc_appliance_alert_rule"."threshold_settings" IS '阈值告警配置（json字符串）';
COMMENT ON COLUMN "public"."fpc_appliance_alert_rule"."trend_settings" IS '基线告警配置（json字符串）';
COMMENT ON COLUMN "public"."fpc_appliance_alert_rule"."advanced_settings" IS '组合告警配置（json字符串）';
COMMENT ON COLUMN "public"."fpc_appliance_alert_rule"."refire" IS '告警触发配置（json字符串）';
COMMENT ON COLUMN "public"."fpc_appliance_alert_rule"."status" IS '启用状态（0：禁用；1：启用）';
COMMENT ON COLUMN "public"."fpc_appliance_alert_rule"."alert_rule_in_cms_id" IS '告警规则在cms中的ID';
COMMENT ON COLUMN "public"."fpc_appliance_alert_rule"."description" IS '备注';
COMMENT ON COLUMN "public"."fpc_appliance_alert_rule"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_alert_rule"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_alert_rule"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_alert_rule"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_alert_rule"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_alert_scope
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_alert_scope" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "alert_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "source_type" varchar(12) COLLATE "pg_catalog"."default" NOT NULL,
  "network_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "service_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  CONSTRAINT "fpc_appliance_alert_scope_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_alert_scope"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_alert_scope"."alert_id" IS '告警ID';
COMMENT ON COLUMN "public"."fpc_appliance_alert_scope"."source_type" IS 'network：网络；service：业务';
COMMENT ON COLUMN "public"."fpc_appliance_alert_scope"."network_id" IS '网络/子网ID';
COMMENT ON COLUMN "public"."fpc_appliance_alert_scope"."service_id" IS '业务ID';

-- ----------------------------
-- Table structure for fpc_appliance_alert_message
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_alert_message" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "alert_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "name" varchar(128) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "category" varchar(10) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "level" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "source_type" varchar(12) COLLATE "pg_catalog"."default",
  "alert_define" text COLLATE "pg_catalog"."default",
  "components" text COLLATE "pg_catalog"."default",
  "arise_time" timestamptz(6),
  CONSTRAINT "fpc_appliance_alert_message_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_alert_message"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_alert_message"."alert_id" IS '告警ID';
COMMENT ON COLUMN "public"."fpc_appliance_alert_message"."name" IS '告警名称';
COMMENT ON COLUMN "public"."fpc_appliance_alert_message"."category" IS '告警分类';
COMMENT ON COLUMN "public"."fpc_appliance_alert_message"."level" IS '告警级别';
COMMENT ON COLUMN "public"."fpc_appliance_alert_message"."source_type" IS '纬度（network：网络；service：业务；组合类型不存在纬度）';
COMMENT ON COLUMN "public"."fpc_appliance_alert_message"."alert_define" IS '完整的告警定义(json 字符串)';
COMMENT ON COLUMN "public"."fpc_appliance_alert_message"."components" IS '告警子组件(json 字符串)';
COMMENT ON COLUMN "public"."fpc_appliance_alert_message"."arise_time" IS '发生时间';

-- ----------------------------
-- 历史版本，已弃用
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_alert_syslog" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "state" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "ip_address" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "port" int4 NOT NULL,
  "protocol" varchar(8) COLLATE "pg_catalog"."default" NOT NULL,
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_alert_syslog_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_alert_syslog"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_alert_syslog"."state" IS '状态（0：关闭；1：启用）';
COMMENT ON COLUMN "public"."fpc_appliance_alert_syslog"."ip_address" IS '日志主机IP地址';
COMMENT ON COLUMN "public"."fpc_appliance_alert_syslog"."port" IS '端口';
COMMENT ON COLUMN "public"."fpc_appliance_alert_syslog"."protocol" IS '协议';
COMMENT ON COLUMN "public"."fpc_appliance_alert_syslog"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_alert_syslog"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_alert_syslog"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_alert_syslog"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_alert_syslog"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_external_storage
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_external_storage" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "state" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "usage" varchar(16) COLLATE "pg_catalog"."default" NOT NULL,
  "type" varchar(16) COLLATE "pg_catalog"."default" NOT NULL,
  "ip_address" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "port" int4 NOT NULL,
  "username" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "password" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "directory" varchar(256) COLLATE "pg_catalog"."default" NOT NULL,
  "capacity" int8 NOT NULL DEFAULT 0,
  "description" varchar(512) COLLATE "pg_catalog"."default" DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_external_storage_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_external_storage"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_external_storage"."name" IS '名称';
COMMENT ON COLUMN "public"."fpc_appliance_external_storage"."state" IS '状态（0：关闭；1：启用）';
COMMENT ON COLUMN "public"."fpc_appliance_external_storage"."usage" IS '用途（transmit_task：全量查询任务文件存储；packet_file_task：离线文件分析任务）';
COMMENT ON COLUMN "public"."fpc_appliance_external_storage"."type" IS '服务器类型（FTP、SMB、HDFS、SFTP、TFTP、NAS）';
COMMENT ON COLUMN "public"."fpc_appliance_external_storage"."ip_address" IS '服务器IP';
COMMENT ON COLUMN "public"."fpc_appliance_external_storage"."port" IS '端口（FTP默认端口21、SMB默认端口445）';
COMMENT ON COLUMN "public"."fpc_appliance_external_storage"."username" IS '用户名';
COMMENT ON COLUMN "public"."fpc_appliance_external_storage"."password" IS '密码';
COMMENT ON COLUMN "public"."fpc_appliance_external_storage"."directory" IS '存储目录';
COMMENT ON COLUMN "public"."fpc_appliance_external_storage"."capacity" IS '存储容量';
COMMENT ON COLUMN "public"."fpc_appliance_external_storage"."description" IS '描述';
COMMENT ON COLUMN "public"."fpc_appliance_external_storage"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_external_storage"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_external_storage"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_external_storage"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_external_storage"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_transmit_task
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_transmit_task" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "assign_task_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "assign_task_time" timestamptz(6),
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "source" varchar(128) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "filter_start_time" timestamptz(6) NOT NULL,
  "filter_end_time" timestamptz(6) NOT NULL,
  "filter_network_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "filter_packet_file_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "filter_condition_type" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "filter_tuple" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "filter_bpf" varchar(1024) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "filter_raw" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "mode" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "replay_netif" varchar(32) NOT NULL DEFAULT '',
  "replay_rate" int4 NOT NULL DEFAULT 0,
  "replay_rate_unit" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "replay_rule" text COLLATE "pg_catalog"."default" DEFAULT '',
  "forward_action" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "ip_tunnel" text COLLATE "pg_catalog"."default" DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "execution_start_time" timestamptz(6),
  "execution_end_time" timestamptz(6),
  "execution_progress" int4 DEFAULT 0,
  "execution_cache_path" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "execution_download_url" varchar(1024) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "execution_trace" text COLLATE "pg_catalog"."default" DEFAULT '',
  "transfer_time" timestamptz(6),
  "state" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "deleted" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  CONSTRAINT "fpc_appliance_transmit_task_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."assign_task_id" IS '分配任务id';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."assign_task_time" IS '分配任务时间';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."name" IS '任务名称';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."source" IS '任务来源';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."filter_start_time" IS '查询起始时间';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."filter_end_time" IS '查询结束时间';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."filter_network_id" IS '查询网络ID';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."filter_packet_file_id" IS '查询离线任务ID';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."filter_condition_type" IS '查询过滤类型（0：六元组；1：bpf表达式）';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."filter_tuple" IS '查询六元组json';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."filter_bpf" IS '查询bpf表达式';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."filter_raw" IS '过滤原始内容';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."mode" IS '导出方式（0：文件；1：重放）';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."replay_netif" IS '重放接口的接口名';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."replay_rate" IS '重放速率数值';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."replay_rate_unit" IS '重放速率单位（0：Kbps；1：pps）';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."replay_rule" IS '重放规则';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."forward_action" IS '转发策略（0：先存储，再转发；1：不存储，直接转发）';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."ip_tunnel" IS 'ip隧道';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."description" IS '备注';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."execution_start_time" IS '任务执行起始时间';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."execution_end_time" IS '任务执行结束时间';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."execution_progress" IS '执行进度（1~100）';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."execution_cache_path" IS '文件缓存路径';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."execution_download_url" IS '文件下载url';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."execution_trace" IS '任务执行摘要';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."transfer_time" IS '结果传输操作时间（点击回放）';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."state" IS '任务状态（0：正常；1：停止；2：完成）';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_transmit_task"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_packet_analysis_task 
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_packet_analysis_task" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" text COLLATE "pg_catalog"."default" NOT NULL,
  "mode" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "source" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "file_path" text COLLATE "pg_catalog"."default" DEFAULT '',
  "configuration" text COLLATE "pg_catalog"."default" DEFAULT '',
  "execution_trace" text COLLATE "pg_catalog"."default" DEFAULT '',
  "status" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "deleted" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
  "create_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_packet_analysis_task_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task"."name" IS '数据包文件全称或主任务名';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task"."mode" IS '当前的分析模式(MULTIPLE_FILES_TO_SINGLE_TASK,MULTIPLE_FILES_TO_MULTIPLE_TASK,SINGLE_DIRECTORY_TO_SINGLE_TASK,SINGLE_DIRECTORY_TO_MULTIPLE_TASK)';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task"."source" IS '当前的任务文件来源（UPLOAD、EXTERNAL_STORAGE）';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task"."file_path" IS '所需要分析的目录或文件路径';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task"."configuration" IS '任务配置json';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task"."execution_trace" IS '分析过程';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task"."status" IS '分析状态（0：等待分析；1：正在分析；2：分析异常；3：分析完成；4：已删除；5：持续分析）';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task"."create_time" IS '数据包文件上传成功时间';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task"."delete_time" IS '数据包文件删除标记时间';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task"."operator_id" IS '操作人ID';

-- ----------------------------
-- Table structure for fpc_appliance_packet_analysis_sub_task
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_packet_analysis_sub_task" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" text COLLATE "pg_catalog"."default" NOT NULL,
  "task_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "packet_start_time" timestamptz(6),
  "packet_end_time" timestamptz(6),
  "size" int8 DEFAULT 0,
  "file_path" text COLLATE "pg_catalog"."default" DEFAULT '',
  "status" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "execution_trace" text COLLATE "pg_catalog"."default" DEFAULT '',
  "execution_progress" int4 DEFAULT 0,
  "execution_result" text COLLATE "pg_catalog"."default" DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
  "create_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_packet_analysis_sub_task_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_sub_task"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_sub_task"."name" IS '数据包文件全称或主任务名';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_sub_task"."task_id" IS '关联的主任务ID';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_sub_task"."packet_start_time" IS '数据包文件内记录数据的开始时间';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_sub_task"."packet_end_time" IS '数据包文件内记录数据的结束时间';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_sub_task"."size" IS '数据包文件大小（单位：byte）';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_sub_task"."file_path" IS '数据包文件在服务器上的绝对路径';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_sub_task"."status" IS '分析状态（0：等待分析；1：正在分析；2：分析异常；3：分析完成；4：已删除；5：持续分析）';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_sub_task"."execution_trace" IS '分析过程';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_sub_task"."execution_progress" IS '分析进度（0~100）';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_sub_task"."execution_result" IS '分析结果（各项指标的汇总结果）';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_sub_task"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_sub_task"."create_time" IS '数据包文件上传成功时间';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_sub_task"."delete_time" IS '数据包文件删除标记时间';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_sub_task"."operator_id" IS '操作人ID';

-- ----------------------------
-- Table structure for fpc_appliance_packet_analysis_task_log
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_packet_analysis_task_log" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "task_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "sub_task_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "status" varchar(16) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "content" text COLLATE "pg_catalog"."default" DEFAULT '',
  "arise_time" timestamptz(6),
  CONSTRAINT "fpc_appliance_packet_analysis_task_log_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task_log"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task_log"."task_id" IS '日志所属主任务ID';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task_log"."sub_task_id" IS '日志所属子主任务ID';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task_log"."status" IS '执行状态（SUCCESS，FAIL）';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task_log"."content" IS '日志内容';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task_log"."arise_time" IS '日志写入时间';

-- ----------------------------
-- Table structure for fpc_appliance_sendup_message
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_sendup_message" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "message_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "start_time" timestamptz(6),
  "end_time" timestamptz(6),
  "type" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "content" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "result" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  CONSTRAINT "fpc_appliance_sendup_message_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_sendup_message"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_sendup_message"."message_id" IS '消息id';
COMMENT ON COLUMN "public"."fpc_appliance_sendup_message"."start_time" IS '统计时间范围（开始时间）';
COMMENT ON COLUMN "public"."fpc_appliance_sendup_message"."end_time" IS '统计时间范围（结束时间）';
COMMENT ON COLUMN "public"."fpc_appliance_sendup_message"."type" IS '上报的消息类型（0：系统状态；1：日志告警；）';
COMMENT ON COLUMN "public"."fpc_appliance_sendup_message"."content" IS '上报的内容';
COMMENT ON COLUMN "public"."fpc_appliance_sendup_message"."result" IS '上报的结果（0：上报成功；1：上报失败；）';
COMMENT ON COLUMN "public"."fpc_appliance_sendup_message"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_sendup_message"."update_time" IS '更新时间';

-- ----------------------------
-- Table structure for fpc_appliance_ip_conversations_history
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_ip_conversations_history" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "data" text COLLATE "pg_catalog"."default" DEFAULT '',
    "deleted" char(1) COLLATE "pg_catalog"."default" DEFAULT 0,
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "delete_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
    CONSTRAINT "fpc_appliance_ip_conversations_history_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_ip_conversations_history"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_ip_conversations_history"."name" IS '历史画布名称';
COMMENT ON COLUMN "public"."fpc_appliance_ip_conversations_history"."data" IS '历史画布数据';
COMMENT ON COLUMN "public"."fpc_appliance_ip_conversations_history"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_ip_conversations_history"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_ip_conversations_history"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_ip_conversations_history"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_ip_conversations_history"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_ip_label
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_ip_label" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "ip_address" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "category" char(1) COLLATE "pg_catalog"."default" NOT NULL,
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_ip_label_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_ip_label"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_ip_label"."name" IS '名称';
COMMENT ON COLUMN "public"."fpc_appliance_ip_label"."ip_address" IS 'IP地址';
COMMENT ON COLUMN "public"."fpc_appliance_ip_label"."category" IS '分类（1：行业标签；2：单位标签；3：地区标签；4：专题标签）';
COMMENT ON COLUMN "public"."fpc_appliance_ip_label"."description" IS '备注';
COMMENT ON COLUMN "public"."fpc_appliance_ip_label"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_ip_label"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_ip_label"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_ip_label"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_ip_label"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_domain_whitelist
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_domain_whitelist" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "domain_white_list_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
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
COMMENT ON COLUMN "public"."fpc_appliance_domain_whitelist"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_domain_whitelist"."domain_white_list_in_cms_id" IS '域名白名单在cms中的ID';
COMMENT ON COLUMN "public"."fpc_appliance_domain_whitelist"."name" IS '名称';
COMMENT ON COLUMN "public"."fpc_appliance_domain_whitelist"."domain" IS '域名';
COMMENT ON COLUMN "public"."fpc_appliance_domain_whitelist"."description" IS '备注';
COMMENT ON COLUMN "public"."fpc_appliance_domain_whitelist"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_domain_whitelist"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_domain_whitelist"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_domain_whitelist"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_domain_whitelist"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_system_device_disk
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_system_device_disk" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "device_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
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
  CONSTRAINT "fpc_system_device_disk_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_system_device_disk"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_system_device_disk"."device_id" IS '机箱设备ID';
COMMENT ON COLUMN "public"."fpc_system_device_disk"."physical_location" IS '物理位置';
COMMENT ON COLUMN "public"."fpc_system_device_disk"."slot_no" IS '槽位号';
COMMENT ON COLUMN "public"."fpc_system_device_disk"."raid_no" IS 'RAID组编号';
COMMENT ON COLUMN "public"."fpc_system_device_disk"."raid_level" IS 'RAID组级别';
COMMENT ON COLUMN "public"."fpc_system_device_disk"."state" IS '磁盘状态（0：在线；1：热备；2：重建；3：可配置；4：不可配置；5：回拷；6：失败；7：错误）';
COMMENT ON COLUMN "public"."fpc_system_device_disk"."medium" IS '存储介质（0：HDD；1：SSD；2：未知）';
COMMENT ON COLUMN "public"."fpc_system_device_disk"."capacity" IS '容量（单位：TB）';
COMMENT ON COLUMN "public"."fpc_system_device_disk"."rebuild_progress" IS '重建进度（0~100）';
COMMENT ON COLUMN "public"."fpc_system_device_disk"."copyback_progress" IS '回拷进度（0~100）';
COMMENT ON COLUMN "public"."fpc_system_device_disk"."foreign_state" IS '外部状态（0：None；1：Foreign）';
COMMENT ON COLUMN "public"."fpc_system_device_disk"."description" IS '备注';

-- ----------------------------
-- Table structure for fpc_system_device_netif
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_system_device_netif" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "state" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "category" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "specification" int4 NOT NULL DEFAULT 0,
  "type" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "ipv4_address" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "ipv4_gateway" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "ipv6_address" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "ipv6_gateway" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_system_device_netif_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_system_device_netif"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_system_device_netif"."name" IS '接口名称';
COMMENT ON COLUMN "public"."fpc_system_device_netif"."state" IS '接口状态（0：UP；1：DOWN）';
COMMENT ON COLUMN "public"."fpc_system_device_netif"."category" IS '接口用途（0：管理；1：流量接收；2：流量转发；）';
COMMENT ON COLUMN "public"."fpc_system_device_netif"."specification" IS '接口规格（单位：Mbps）';
COMMENT ON COLUMN "public"."fpc_system_device_netif"."type" IS '类型（type=1：DPDK接口；type=2：普通接口）';
COMMENT ON COLUMN "public"."fpc_system_device_netif"."ipv4_address" IS 'ipv4地址/掩码';
COMMENT ON COLUMN "public"."fpc_system_device_netif"."ipv4_gateway" IS 'ipv4网关';
COMMENT ON COLUMN "public"."fpc_system_device_netif"."ipv6_address" IS 'ipv6地址/掩码';
COMMENT ON COLUMN "public"."fpc_system_device_netif"."ipv6_gateway" IS 'ipv6网关';
COMMENT ON COLUMN "public"."fpc_system_device_netif"."description" IS '备注';
COMMENT ON COLUMN "public"."fpc_system_device_netif"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_system_device_netif"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_system_device_netif"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_system_device_netif"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_system_monitor_metric
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_system_monitor_metric" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "metric_name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "metric_value" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "metric_time" timestamptz(6) NOT NULL,
  CONSTRAINT "fpc_system_monitor_metric_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_system_monitor_metric"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_system_monitor_metric"."metric_name" IS '统计项（
cpu_used_pct,
memory_used_pct,
fs_system_used_pct,
fs_index_used_pct,
fs_data_used_pct,
fs_data_used_byte,
fs_data_total_byte,
fs_cache_used_pct,
fs_cache_used_byte,
fs_cache_total_byte,
data_oldest_time,
data_last24_total_byte,
data_predict_total_day,
cache_file_avg_byte）';
COMMENT ON COLUMN "public"."fpc_system_monitor_metric"."metric_value" IS '统计值';
COMMENT ON COLUMN "public"."fpc_system_monitor_metric"."metric_time" IS '统计时间';

-- ----------------------------
-- Table structure for fpc_statistics_rrd
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_statistics_rrd" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "last_time" timestamptz(6) NOT NULL,
  "last_position" int4 NOT NULL DEFAULT 0,
  CONSTRAINT "fpc_statistics_rrd_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_statistics_rrd"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_statistics_rrd"."name" IS 'rrd数据名称';
COMMENT ON COLUMN "public"."fpc_statistics_rrd"."last_time" IS '最新时间';
COMMENT ON COLUMN "public"."fpc_statistics_rrd"."last_position" IS '最新位置';

-- ----------------------------
-- Table structure for fpc_statistics_timeseries
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_statistics_timeseries" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rrd_name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "cell_number" int4 NOT NULL DEFAULT 0,
  "data_point" DOUBLE PRECISION[] NOT NULL DEFAULT '{}',
  CONSTRAINT "fpc_statistics_timeseries_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_statistics_timeseries"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_statistics_timeseries"."rrd_name" IS 'rrd名称';
COMMENT ON COLUMN "public"."fpc_statistics_timeseries"."cell_number" IS '行序号';
COMMENT ON COLUMN "public"."fpc_statistics_timeseries"."data_point" IS '行内时序值';
CREATE INDEX fpc_statistics_timeseries_rrd_name_idx ON public.fpc_statistics_timeseries (rrd_name,cell_number);

-- ----------------------------
-- Table structure for fpc_appliance_netflow_source
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_netflow_source" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "device_name" text COLLATE "pg_catalog"."default" NOT NULL,
    "device_type" char(1) COLLATE "pg_catalog"."default" NOT NULL,
    "netif_no" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "protocol_version" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "alias" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "netif_speed" int4 NOT NULL DEFAULT 0,
    "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "delete_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    CONSTRAINT "fpc_appliance_netflow_source_pkey" PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."fpc_appliance_netflow_source"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_netflow_source"."device_name" IS '设备名称';
COMMENT ON COLUMN "public"."fpc_appliance_netflow_source"."device_type" IS '设备类型';
COMMENT ON COLUMN "public"."fpc_appliance_netflow_source"."netif_no" IS '接口编号';
COMMENT ON COLUMN "public"."fpc_appliance_netflow_source"."protocol_version" IS '协议版本';
COMMENT ON COLUMN "public"."fpc_appliance_netflow_source"."alias" IS '别名';
COMMENT ON COLUMN "public"."fpc_appliance_netflow_source"."netif_speed" IS '接口速率';
COMMENT ON COLUMN "public"."fpc_appliance_netflow_source"."description" IS '描述';
COMMENT ON COLUMN "public"."fpc_appliance_netflow_source"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_netflow_source"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_netflow_source"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_netflow_source"."operator_id" IS '操作人id';


-- ----------------------------
-- Table structure for fpc_appliance_custom_time
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_custom_time" (
	"id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
	"custom_time_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
	"name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
	"type" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
	"period" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
	"custom_time_setting" text COLLATE "pg_catalog"."default" DEFAULT '',
	"deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
	"create_time" timestamptz(6),
	"update_time" timestamptz(6),
	"delete_time" timestamptz(6),
	"operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
	CONSTRAINT "fpc_appliance_custom_time_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_custom_time"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_custom_time"."custom_time_in_cms_id" IS '自定义时间在cms中的ID';
COMMENT ON COLUMN "public"."fpc_appliance_custom_time"."name" IS '自定义时间名称';
COMMENT ON COLUMN "public"."fpc_appliance_custom_time"."type" IS '类型（0：周期性时间；1：一次性时间）';
COMMENT ON COLUMN "public"."fpc_appliance_custom_time"."period" IS '星期';
COMMENT ON COLUMN "public"."fpc_appliance_custom_time"."custom_time_setting" IS '自定义时间';
COMMENT ON COLUMN "public"."fpc_appliance_custom_time"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_custom_time"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_custom_time"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_custom_time"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_custom_time"."operator_id" IS '操作人id';

-- ----------------------------
-- 历史版本，已弃用
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_syslog_sendup_rule" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "syslog_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
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
    "data_source" text COLLATE "pg_catalog"."default" DEFAULT '',
    "connect_info" text COLLATE "pg_catalog"."default" DEFAULT '',
    "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "delete_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_syslog_sendup_rule_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_syslog_sendup_rule"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_syslog_sendup_rule"."syslog_in_cms_id" IS 'syslog在cms中的ID';
COMMENT ON COLUMN "public"."fpc_appliance_syslog_sendup_rule"."name" IS 'syslog规则名';
COMMENT ON COLUMN "public"."fpc_appliance_syslog_sendup_rule"."syslog_server_address" IS 'syslog服务器地址';
COMMENT ON COLUMN "public"."fpc_appliance_syslog_sendup_rule"."send_type" IS '发送方式（0：即时发送；1：定时发送；2：抑制发送）';
COMMENT ON COLUMN "public"."fpc_appliance_syslog_sendup_rule"."send_time" IS '发送间隔（当发送方式为定时发送时使用）';
COMMENT ON COLUMN "public"."fpc_appliance_syslog_sendup_rule"."interval" IS '时间间隔';
COMMENT ON COLUMN "public"."fpc_appliance_syslog_sendup_rule"."threshold" IS '数量阈值';
COMMENT ON COLUMN "public"."fpc_appliance_syslog_sendup_rule"."severity" IS 'syslog等级';
COMMENT ON COLUMN "public"."fpc_appliance_syslog_sendup_rule"."facility" IS 'syslog类型';
COMMENT ON COLUMN "public"."fpc_appliance_syslog_sendup_rule"."encode_type" IS '字符编码方式（0：UTF-8；1：GB2312）';
COMMENT ON COLUMN "public"."fpc_appliance_syslog_sendup_rule"."separator" IS '字段分隔符';
COMMENT ON COLUMN "public"."fpc_appliance_syslog_sendup_rule"."network_alert_content" IS '网络告警消息内容';
COMMENT ON COLUMN "public"."fpc_appliance_syslog_sendup_rule"."service_alert_content" IS '业务告警消息内容';
COMMENT ON COLUMN "public"."fpc_appliance_syslog_sendup_rule"."system_alarm_content" IS '系统告警消息内容';
COMMENT ON COLUMN "public"."fpc_appliance_syslog_sendup_rule"."system_log_content" IS '系统日志消息内容';
COMMENT ON COLUMN "public"."fpc_appliance_syslog_sendup_rule"."data_source" IS '北向syslog数据源';
COMMENT ON COLUMN "public"."fpc_appliance_syslog_sendup_rule"."connect_info" IS '北向syslog目的地址信息';
COMMENT ON COLUMN "public"."fpc_appliance_syslog_sendup_rule"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_syslog_sendup_rule"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_syslog_sendup_rule"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_syslog_sendup_rule"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_syslog_sendup_rule"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_smtp_configuration
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_smtp_configuration" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "smtp_configuration_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
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
  CONSTRAINT "fpc_appliance_smtp_configuration_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_smtp_configuration"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_smtp_configuration"."id" IS 'smtp配置在cms中的id';
COMMENT ON COLUMN "public"."fpc_appliance_smtp_configuration"."mail_username" IS '用户名';
COMMENT ON COLUMN "public"."fpc_appliance_smtp_configuration"."mail_address" IS '邮件地址';
COMMENT ON COLUMN "public"."fpc_appliance_smtp_configuration"."smtp_server" IS '邮件服务器';
COMMENT ON COLUMN "public"."fpc_appliance_smtp_configuration"."server_port" IS '服务器端口';
COMMENT ON COLUMN "public"."fpc_appliance_smtp_configuration"."encrypt" IS '是否加密（0：不加密；1：加密）';
COMMENT ON COLUMN "public"."fpc_appliance_smtp_configuration"."login_user" IS '登录用户';
COMMENT ON COLUMN "public"."fpc_appliance_smtp_configuration"."login_password" IS '登录密码';
COMMENT ON COLUMN "public"."fpc_appliance_smtp_configuration"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_smtp_configuration"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_smtp_configuration"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_smtp_configuration"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_smtp_configuration"."operator_id" IS '操作人id';

-- ----------------------------
-- 历史版本，已弃用
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_mail_sendup_rule" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "mail_sendup_rule_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
    "mail_title" text COLLATE "pg_catalog"."default" NOT NULL,
    "receiver" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "cc" text COLLATE "pg_catalog"."default" DEFAULT '',
    "interval" int4 NOT NULL,
    "network_alert_content" text COLLATE "pg_catalog"."default" DEFAULT '',
    "service_alert_content" text COLLATE "pg_catalog"."default" DEFAULT '',
    "system_log_content" text COLLATE "pg_catalog"."default" DEFAULT '',
	"system_alarm_content" text COLLATE "pg_catalog"."default" DEFAULT '',
    "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "delete_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_mail_sendup_rule_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_mail_sendup_rule"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_mail_sendup_rule"."mail_sendup_rule_in_cms_id" IS '邮件外发配置在cms中的id';
COMMENT ON COLUMN "public"."fpc_appliance_mail_sendup_rule"."mail_title" IS '邮件主题';
COMMENT ON COLUMN "public"."fpc_appliance_mail_sendup_rule"."receiver" IS '收件人';
COMMENT ON COLUMN "public"."fpc_appliance_mail_sendup_rule"."cc" IS '抄送人';
COMMENT ON COLUMN "public"."fpc_appliance_mail_sendup_rule"."interval" IS '时间间隔';
COMMENT ON COLUMN "public"."fpc_appliance_mail_sendup_rule"."network_alert_content" IS '网络告警消息内容';
COMMENT ON COLUMN "public"."fpc_appliance_mail_sendup_rule"."service_alert_content" IS '业务告警消息内容';
COMMENT ON COLUMN "public"."fpc_appliance_mail_sendup_rule"."system_log_content" IS '系统日志消息内容';
COMMENT ON COLUMN "public"."fpc_appliance_mail_sendup_rule"."system_alarm_content" IS '系统告警消息内容';
COMMENT ON COLUMN "public"."fpc_appliance_mail_sendup_rule"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_mail_sendup_rule"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_mail_sendup_rule"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_mail_sendup_rule"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_mail_sendup_rule"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_external_receiver
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_external_receiver" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "receiver_type" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "receiver_content" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "external_receiver_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
    "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "delete_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    CONSTRAINT "fpc_appliance_external_receiver_pkey" PRIMARY KEY ("id")
    )
;
COMMENT ON COLUMN "public"."fpc_appliance_external_receiver"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_external_receiver"."name" IS '名称';
COMMENT ON COLUMN "public"."fpc_appliance_external_receiver"."receiver_type" IS '外发服务器类型（0：mail；1：syslog；2：kafka；3：zmq）';
COMMENT ON COLUMN "public"."fpc_appliance_external_receiver"."receiver_content" IS '外发服务器内容';
COMMENT ON COLUMN "public"."fpc_appliance_external_receiver"."external_receiver_in_cms_id" IS '外发服务器在CMS中的ID';
COMMENT ON COLUMN "public"."fpc_appliance_external_receiver"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_external_receiver"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_external_receiver"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_external_receiver"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_external_receiver"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_send_rule
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_send_rule" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "send_rule_content" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "send_rule_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
    "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "delete_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    CONSTRAINT "fpc_appliance_send_rule_pkey" PRIMARY KEY ("id")
    )
;
COMMENT ON COLUMN "public"."fpc_appliance_send_rule"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_send_rule"."name" IS '名称';
COMMENT ON COLUMN "public"."fpc_appliance_send_rule"."send_rule_content" IS '外发规则内容';
COMMENT ON COLUMN "public"."fpc_appliance_send_rule"."send_rule_in_cms_id" IS '外发规则在CMS中的ID';
COMMENT ON COLUMN "public"."fpc_appliance_send_rule"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_send_rule"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_send_rule"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_send_rule"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_send_rule"."operator_id" IS '操作人id';


-- ----------------------------
-- Table structure for fpc_appliance_send_policy
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_send_policy" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "external_receiver_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "send_rule_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "send_policy_in_cms_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
    "state" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "delete_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    CONSTRAINT "fpc_appliance_send_policy_pkey" PRIMARY KEY ("id")
    )
;
COMMENT ON COLUMN "public"."fpc_appliance_send_policy"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_send_policy"."name" IS '名称';
COMMENT ON COLUMN "public"."fpc_appliance_send_policy"."external_receiver_id" IS '外发服务器ID';
COMMENT ON COLUMN "public"."fpc_appliance_send_policy"."send_rule_id" IS '外发规则ID';
COMMENT ON COLUMN "public"."fpc_appliance_send_policy"."send_policy_in_cms_id" IS '外发策略在CMS中的ID';
COMMENT ON COLUMN "public"."fpc_appliance_send_policy"."state" IS '启用状态（0：禁用，1：启用）';
COMMENT ON COLUMN "public"."fpc_appliance_send_policy"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_send_policy"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_send_policy"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_send_policy"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_send_policy"."operator_id" IS '操作人id';


-- ----------------------------
-- Table structure for fpc_appliance_packet_analysis_task_policy
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_packet_analysis_task_policy" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "packet_analysis_task_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "policy_type" varchar(16) COLLATE "pg_catalog"."default" NOT NULL,
    "policy_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "timestamp" timestamptz(6) NOT NULL,
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    CONSTRAINT "fpc_appliance_packet_analysis_task_policy_pkey" PRIMARY KEY ("id")
    )
;
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task_policy"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task_policy"."packet_analysis_task_id" IS '离线任务ID';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task_policy"."policy_type" IS '策略类型（ingest：捕获策略；filter：过滤策略；forward：转发策略；send：外发策略）';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task_policy"."policy_id" IS '策略id';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task_policy"."timestamp" IS '时间戳';
COMMENT ON COLUMN "public"."fpc_appliance_packet_analysis_task_policy"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_asset_baseline
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_asset_baseline" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "ip_address" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "baseline" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "description" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_asset_baseline_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_asset_baseline"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_asset_baseline"."ip_address" IS 'IP地址';
COMMENT ON COLUMN "public"."fpc_appliance_asset_baseline"."type" IS '类型';
COMMENT ON COLUMN "public"."fpc_appliance_asset_baseline"."baseline" IS '基线状态';
COMMENT ON COLUMN "public"."fpc_appliance_asset_baseline"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_asset_baseline"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_asset_baseline"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_asset_baseline"."description" IS '描述';
COMMENT ON COLUMN "public"."fpc_appliance_asset_baseline"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_asset_alarm
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_asset_alarm" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "ip_address" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "baseline" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "current" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "alarm_time" timestamptz(6),
  CONSTRAINT "fpc_appliance_asset_alarm_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_asset_alarm"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_asset_alarm"."ip_address" IS 'IP地址';
COMMENT ON COLUMN "public"."fpc_appliance_asset_alarm"."type" IS '类型';
COMMENT ON COLUMN "public"."fpc_appliance_asset_alarm"."baseline" IS '基线状态';
COMMENT ON COLUMN "public"."fpc_appliance_asset_alarm"."current" IS '当前状态';
COMMENT ON COLUMN "public"."fpc_appliance_asset_alarm"."alarm_time" IS '告警时间';

-- ----------------------------
-- Table structure for fpc_appliance_asset_device
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_asset_device" (
  "id" int4 NOT NULL,
  "device_name" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "update_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_asset_device_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_asset_device"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_asset_device"."device_name" IS '设备名称';
COMMENT ON COLUMN "public"."fpc_appliance_asset_device"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_asset_device"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_asset_os
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_asset_os" (
  "id" int4 NOT NULL,
  "os" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "update_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_asset_os_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_asset_os"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_asset_os"."os" IS '设备名称';
COMMENT ON COLUMN "public"."fpc_appliance_asset_os"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_asset_os"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_mail_rule
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_mail_rule" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "mail_address" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
    "country_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '0',
    "province_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '0',
    "city_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '0',
    "start_time" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "end_time" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "action" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "period" varchar(32) COLLATE "pg_catalog"."default" DEFAULT '',
    "state" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "delete_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "fpc_appliance_mail_rule_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."fpc_appliance_mail_rule"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_mail_rule"."mail_address" IS '邮箱';
COMMENT ON COLUMN "public"."fpc_appliance_mail_rule"."country_id" IS '国家ID';
COMMENT ON COLUMN "public"."fpc_appliance_mail_rule"."province_id" IS '省份ID';
COMMENT ON COLUMN "public"."fpc_appliance_mail_rule"."city_id" IS '城市ID';
COMMENT ON COLUMN "public"."fpc_appliance_mail_rule"."start_time" IS '开始时间';
COMMENT ON COLUMN "public"."fpc_appliance_mail_rule"."end_time" IS '结束时间';
COMMENT ON COLUMN "public"."fpc_appliance_mail_rule"."action" IS '动作（0：允许；1：告警）';
COMMENT ON COLUMN "public"."fpc_appliance_mail_rule"."period" IS '每周生效时间';
COMMENT ON COLUMN "public"."fpc_appliance_mail_rule"."state" IS '启用状态（0：禁用，1：启用）';
COMMENT ON COLUMN "public"."fpc_appliance_mail_rule"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."fpc_appliance_mail_rule"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_mail_rule"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_mail_rule"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."fpc_appliance_mail_rule"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for fpc_appliance_nat_config
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_nat_config" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "nat_action" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
    "update_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    CONSTRAINT "fpc_appliance_nat_config_pkey" PRIMARY KEY ("id")
    )
;
COMMENT ON COLUMN "public"."fpc_appliance_nat_config"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_nat_config"."nat_action" IS '是否开启NAT关联状态（0：关闭，1：开启）';
COMMENT ON COLUMN "public"."fpc_appliance_nat_config"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_nat_config"."operator_id" IS '操作人id';


-- ----------------------------
-- Table structure for fpc_appliance_ip_detections_layouts
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."fpc_appliance_ip_detections_layouts" (
    "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "layouts" text COLLATE "pg_catalog"."default" DEFAULT '',
    "create_time" timestamptz(6),
    "update_time" timestamptz(6),
    "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    CONSTRAINT "fpc_appliance_ip_detections_layouts_pkey" PRIMARY KEY ("id")
    )
;
COMMENT ON COLUMN "public"."fpc_appliance_ip_detections_layouts"."id" IS '主键';
COMMENT ON COLUMN "public"."fpc_appliance_ip_detections_layouts"."layouts" IS 'IP画像页面布局参数';
COMMENT ON COLUMN "public"."fpc_appliance_ip_detections_layouts"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."fpc_appliance_ip_detections_layouts"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."fpc_appliance_ip_detections_layouts"."operator_id" IS '操作人id';