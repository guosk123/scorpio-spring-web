-- ----------------------------
-- Table structure for alpha_system_alarm
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."alpha_system_alarm" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "level" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "category" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "keyword" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "component" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "solver_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "status" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "solve_time" timestamptz(6),
  "arise_time" timestamptz(6) NOT NULL,
  "content" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "reason" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "node_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "create_time" timestamptz(6),
  "order_num" int8 DEFAULT 0,
  CONSTRAINT "alpha_system_alarm_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."alpha_system_alarm"."id" IS '主键';
COMMENT ON COLUMN "public"."alpha_system_alarm"."level" IS '级别（0：提示；1：一般；2：重要；3：紧急）';
COMMENT ON COLUMN "public"."alpha_system_alarm"."category" IS '告警类型';
COMMENT ON COLUMN "public"."alpha_system_alarm"."keyword" IS '关键字';
COMMENT ON COLUMN "public"."alpha_system_alarm"."component" IS '组件编号（统一编号为八位字符，例如：001001）';
COMMENT ON COLUMN "public"."alpha_system_alarm"."solver_id" IS '解决人ID';
COMMENT ON COLUMN "public"."alpha_system_alarm"."status" IS '当前状态（0：未解决；1：解决；）';
COMMENT ON COLUMN "public"."alpha_system_alarm"."solve_time" IS '解决时间';
COMMENT ON COLUMN "public"."alpha_system_alarm"."arise_time" IS '发生时间';
COMMENT ON COLUMN "public"."alpha_system_alarm"."content" IS '告警内容';
COMMENT ON COLUMN "public"."alpha_system_alarm"."reason" IS '解决备注';
COMMENT ON COLUMN "public"."alpha_system_alarm"."node_id" IS '分布式节点id';
COMMENT ON COLUMN "public"."alpha_system_alarm"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."alpha_system_alarm"."order_num" IS '自增数字';

CREATE SEQUENCE IF NOT EXISTS alarm_order_num_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
alter table "public"."alpha_system_alarm" alter column order_num set default nextval('alarm_order_num_seq');

-- ----------------------------
-- Table structure for alpha_system_log
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."alpha_system_log" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "level" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "category" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "component" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "arise_time" timestamptz(6) NOT NULL,
  "content" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "source" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "node_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "create_time" timestamptz(6),
  "order_num" int8 DEFAULT 0,
  CONSTRAINT "alpha_system_log_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."alpha_system_log"."id" IS '主键';
COMMENT ON COLUMN "public"."alpha_system_log"."level" IS '级别（0：调试；1：通知；2：告警；3：错误；4：致命）';
COMMENT ON COLUMN "public"."alpha_system_log"."category" IS '类型（0：审计；1：运行）';
COMMENT ON COLUMN "public"."alpha_system_log"."component" IS '组件编号（统一编号为八位字符，例如：001001）';
COMMENT ON COLUMN "public"."alpha_system_log"."arise_time" IS '发生时间';
COMMENT ON COLUMN "public"."alpha_system_log"."content" IS '日志内容（包括但不限于：操作类型，操作对象）';
COMMENT ON COLUMN "public"."alpha_system_log"."source" IS '日志来源（包括但不限于：操作人名，ID，IP）';
COMMENT ON COLUMN "public"."alpha_system_log"."node_id" IS '分布式节点id';
COMMENT ON COLUMN "public"."alpha_system_log"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."alpha_system_log"."order_num" IS '自增数字';

CREATE SEQUENCE IF NOT EXISTS log_order_num_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
alter table "public"."alpha_system_log" alter column order_num set default nextval('log_order_num_seq');

-- ----------------------------
-- 当前分支版本已弃用
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."alpha_system_syslog" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "state" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "ip_address" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "port" int4 NOT NULL DEFAULT 0,
  "protocol" varchar(8) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "data_source" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "alpha_system_syslog_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."alpha_system_syslog"."id" IS '主键';
COMMENT ON COLUMN "public"."alpha_system_syslog"."state" IS '状态（0：关闭；1：启用）';
COMMENT ON COLUMN "public"."alpha_system_syslog"."ip_address" IS '日志主机IP地址';
COMMENT ON COLUMN "public"."alpha_system_syslog"."port" IS '端口';
COMMENT ON COLUMN "public"."alpha_system_syslog"."protocol" IS '协议(TCP/UDP)';
COMMENT ON COLUMN "public"."alpha_system_syslog"."data_source" IS '发送数据源(system_log/audit_log/system_alarm)';
COMMENT ON COLUMN "public"."alpha_system_syslog"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."alpha_system_syslog"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."alpha_system_syslog"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."alpha_system_syslog"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."alpha_system_syslog"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for alpha_system_perm
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."alpha_system_perm" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name_en" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "name_zh" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "alpha_system_perm_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."alpha_system_perm"."id" IS '主键';
COMMENT ON COLUMN "public"."alpha_system_perm"."name_en" IS '权限英文名，唯一';
COMMENT ON COLUMN "public"."alpha_system_perm"."name_zh" IS '权限中文名';
COMMENT ON COLUMN "public"."alpha_system_perm"."description" IS '备注';
COMMENT ON COLUMN "public"."alpha_system_perm"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."alpha_system_perm"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."alpha_system_perm"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."alpha_system_perm"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."alpha_system_perm"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for alpha_system_role
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."alpha_system_role" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name_en" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "name_zh" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "alpha_system_role_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."alpha_system_role"."id" IS '主键';
COMMENT ON COLUMN "public"."alpha_system_role"."name_en" IS '角色英文名，唯一';
COMMENT ON COLUMN "public"."alpha_system_role"."name_zh" IS '角色中文名';
COMMENT ON COLUMN "public"."alpha_system_role"."description" IS '备注';
COMMENT ON COLUMN "public"."alpha_system_role"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."alpha_system_role"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."alpha_system_role"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."alpha_system_role"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."alpha_system_role"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for alpha_system_role_perm
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."alpha_system_role_perm" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "role_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "perm_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "alpha_system_role_perm_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."alpha_system_role_perm"."id" IS '主键';
COMMENT ON COLUMN "public"."alpha_system_role_perm"."role_id" IS '角色ID';
COMMENT ON COLUMN "public"."alpha_system_role_perm"."perm_id" IS '权限ID';

-- ----------------------------
-- Table structure for alpha_system_user
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."alpha_system_user" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "fullname" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "email" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "password" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "need_change_password" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "app_key" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "app_token" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "user_type" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "locked" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "password_update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "alpha_system_user_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."alpha_system_user"."id" IS '主键';
COMMENT ON COLUMN "public"."alpha_system_user"."name" IS '登录名';
COMMENT ON COLUMN "public"."alpha_system_user"."fullname" IS '显示名';
COMMENT ON COLUMN "public"."alpha_system_user"."email" IS 'email';
COMMENT ON COLUMN "public"."alpha_system_user"."password" IS '密码';
COMMENT ON COLUMN "public"."alpha_system_user"."need_change_password" IS '是否需要提示修改密码（0：不需要；1：需要）';
COMMENT ON COLUMN "public"."alpha_system_user"."app_key" IS '第三方key';
COMMENT ON COLUMN "public"."alpha_system_user"."app_token" IS '第三方认证token';
COMMENT ON COLUMN "public"."alpha_system_user"."user_type" IS '用户类型（0：普通用户；1：rest内置用户；2：sso内置用户）';
COMMENT ON COLUMN "public"."alpha_system_user"."description" IS '备注';
COMMENT ON COLUMN "public"."alpha_system_user"."locked" IS '锁定标记(0-未锁定 1-锁定)';
COMMENT ON COLUMN "public"."alpha_system_user"."deleted" IS '是否删除（0：未删除；1：已删除）';
COMMENT ON COLUMN "public"."alpha_system_user"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."alpha_system_user"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."alpha_system_user"."password_update_time" IS '密码修改时间';
COMMENT ON COLUMN "public"."alpha_system_user"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."alpha_system_user"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for alpha_system_user_role
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."alpha_system_user_role" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "user_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "role_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "alpha_system_user_role_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."alpha_system_user_role"."id" IS '主键';
COMMENT ON COLUMN "public"."alpha_system_user_role"."user_id" IS '用户ID';
COMMENT ON COLUMN "public"."alpha_system_user_role"."role_id" IS '角色ID';

-- ----------------------------
-- Table structure for alpha_system_user_menu
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."alpha_system_user_menu" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "user_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "resource" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "perm" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "update_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "alpha_system_user_menu_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."alpha_system_user_menu"."id" IS '主键';
COMMENT ON COLUMN "public"."alpha_system_user_menu"."user_id" IS '用户ID';
COMMENT ON COLUMN "public"."alpha_system_user_menu"."resource" IS '资源';
COMMENT ON COLUMN "public"."alpha_system_user_menu"."perm" IS '权限（0：没有权限；1：拥有权限）';
COMMENT ON COLUMN "public"."alpha_system_user_menu"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."alpha_system_user_menu"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for alpha_system_user_password
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."alpha_system_user_password" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "user_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "password" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "create_time" timestamptz(6),
  CONSTRAINT "alpha_system_user_password_pkey" PRIMARY KEY ("id")
    )
;
COMMENT ON COLUMN "public"."alpha_system_user_password"."id" IS '主键';
COMMENT ON COLUMN "public"."alpha_system_user_password"."user_id" IS '用户ID';
COMMENT ON COLUMN "public"."alpha_system_user_password"."password" IS '密码';
COMMENT ON COLUMN "public"."alpha_system_user_password"."create_time" IS '创建时间';

-- ----------------------------
-- Table structure for alpha_system_security_settings
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."alpha_system_security_settings" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "forbidden_max_failed" int4 NOT NULL DEFAULT 5,
  "password_min_length" int4 NOT NULL DEFAULT 6,
  "password_max_length" int4 NOT NULL DEFAULT 25,
  "password_complexity" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'case_sensitivity_letter_number_char',
  "history_password_save_times" int4 NOT NULL DEFAULT 3,
  "forbidden_duration_second" int4 NOT NULL DEFAULT 1800,
  "session_expired_second" int4 NOT NULL DEFAULT 600,
  "password_max_day" int4 NOT NULL DEFAULT 0,
  "permit_multi_session" char(1) NOT NULL DEFAULT '0',
  "whitelist_ip_address_state" char(1) NOT NULL DEFAULT '0',
  "whitelist_referer" text COLLATE "pg_catalog"."default",
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "alpha_system_security_settings_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."alpha_system_security_settings"."id" IS '主键';
COMMENT ON COLUMN "public"."alpha_system_security_settings"."forbidden_max_failed" IS '登陆错误锁定阈值数';
COMMENT ON COLUMN "public"."alpha_system_security_settings"."password_min_length" IS '密码长度最小值';
COMMENT ON COLUMN "public"."alpha_system_security_settings"."password_max_length" IS '密码长度最大值';
COMMENT ON COLUMN "public"."alpha_system_security_settings"."password_complexity" IS '密码复杂度';
COMMENT ON COLUMN "public"."alpha_system_security_settings"."history_password_save_times" IS '历史密码保存次数';
COMMENT ON COLUMN "public"."alpha_system_security_settings"."forbidden_duration_second" IS '账户锁定时长';
COMMENT ON COLUMN "public"."alpha_system_security_settings"."session_expired_second" IS '登录超时时间';
COMMENT ON COLUMN "public"."alpha_system_security_settings"."password_max_day" IS '密码有效期（0代表不限制有效期）';
COMMENT ON COLUMN "public"."alpha_system_security_settings"."permit_multi_session" IS '允许同一账号多人同时登陆(0：false;1：true)';
COMMENT ON COLUMN "public"."alpha_system_security_settings"."whitelist_ip_address_state" IS '是否启用白名单(0：false;1：true)';
COMMENT ON COLUMN "public"."alpha_system_security_settings"."whitelist_referer" IS 'referer白名单';
COMMENT ON COLUMN "public"."alpha_system_security_settings"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."alpha_system_security_settings"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."alpha_system_security_settings"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for alpha_system_security_whitelist
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."alpha_system_security_whitelist" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "ip_address" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "ip_start" int8 NOT NULL DEFAULT 0,
  "ip_end" int8 NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "alpha_system_security_whitelist_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."alpha_system_security_whitelist"."id" IS '主键';
COMMENT ON COLUMN "public"."alpha_system_security_whitelist"."ip_address" IS '地址/段';
COMMENT ON COLUMN "public"."alpha_system_security_whitelist"."ip_start" IS '起始IP地址';
COMMENT ON COLUMN "public"."alpha_system_security_whitelist"."ip_end" IS '结束IP地址';
COMMENT ON COLUMN "public"."alpha_system_security_whitelist"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."alpha_system_security_whitelist"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for alpha_system_global_settings
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."alpha_system_global_settings" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "setting_key" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "setting_value" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "update_time" timestamptz(6),
  CONSTRAINT "alpha_system_global_settings_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."alpha_system_global_settings"."id" IS '主键';
COMMENT ON COLUMN "public"."alpha_system_global_settings"."setting_key" IS '参数名';
COMMENT ON COLUMN "public"."alpha_system_global_settings"."setting_value" IS '参数值';
COMMENT ON COLUMN "public"."alpha_system_global_settings"."update_time" IS '修改时间';

-- ----------------------------
-- Table structure for alpha_system_alarm_settings
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."alpha_system_alarm_settings" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "level" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "source_type" varchar(12) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "fire_criteria" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "refire_seconds" int4 NOT NULL DEFAULT 0,
  "state" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "update_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "alpha_system_alarm_settings_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."alpha_system_alarm_settings"."id" IS '主键';
COMMENT ON COLUMN "public"."alpha_system_alarm_settings"."name" IS '告警名称';
COMMENT ON COLUMN "public"."alpha_system_alarm_settings"."level" IS '告警级别（0：提示；1：一般；2：重要；3：紧急）';
COMMENT ON COLUMN "public"."alpha_system_alarm_settings"."source_type" IS '告警来源（CPU、MEMORY、DISK）';
COMMENT ON COLUMN "public"."alpha_system_alarm_settings"."fire_criteria" IS '触发条件';
COMMENT ON COLUMN "public"."alpha_system_alarm_settings"."refire_seconds" IS '告警周期（5m（300s）、15m（900s）、30m（1800s）、60m（3600s））';
COMMENT ON COLUMN "public"."alpha_system_alarm_settings"."state" IS '状态（0：禁用；1：启用）';
COMMENT ON COLUMN "public"."alpha_system_alarm_settings"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."alpha_system_alarm_settings"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for alpha_system_license
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."alpha_system_license" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "content" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "version" int4 NOT NULL DEFAULT 0,
  "file_name" varchar(512) NOT NULL DEFAULT '',
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "alpha_system_license_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."alpha_system_license"."id" IS '主键';
COMMENT ON COLUMN "public"."alpha_system_license"."content" IS 'license内容';
COMMENT ON COLUMN "public"."alpha_system_license"."version" IS 'license版本';
COMMENT ON COLUMN "public"."alpha_system_license"."file_name" IS 'license文件名称';
COMMENT ON COLUMN "public"."alpha_system_license"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."alpha_system_license"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."alpha_system_license"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for alpha_system_product_info
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."alpha_system_product_info" (
  "name" varchar(128) COLLATE "pg_catalog"."default" DEFAULT '',
  "version" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "series" varchar(64) COLLATE "pg_catalog"."default" DEFAULT '',
  "corporation" varchar(128) COLLATE "pg_catalog"."default" DEFAULT '',
  "description" varchar(256) COLLATE "pg_catalog"."default" DEFAULT '',
  "logo_base64" text COLLATE "pg_catalog"."default" DEFAULT '',
  "update_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''
)
;
COMMENT ON COLUMN "public"."alpha_system_product_info"."name" IS '产品名称';
COMMENT ON COLUMN "public"."alpha_system_product_info"."version" IS '产品版本';
COMMENT ON COLUMN "public"."alpha_system_product_info"."series" IS '产品型号';
COMMENT ON COLUMN "public"."alpha_system_product_info"."corporation" IS '版权所属';
COMMENT ON COLUMN "public"."alpha_system_product_info"."description" IS '产品功能描述';
COMMENT ON COLUMN "public"."alpha_system_product_info"."logo_base64" IS '产品logo base64';
COMMENT ON COLUMN "public"."alpha_system_product_info"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."alpha_system_product_info"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for alpha_system_sso_platform
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."alpha_system_sso_platform" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "platform_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "app_token" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "alpha_system_sso_platform_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."alpha_system_sso_platform"."id" IS '主键';
COMMENT ON COLUMN "public"."alpha_system_sso_platform"."name" IS '外部系统名称';
COMMENT ON COLUMN "public"."alpha_system_sso_platform"."platform_id" IS '外部系统ID';
COMMENT ON COLUMN "public"."alpha_system_sso_platform"."app_token" IS '外部系统密钥';
COMMENT ON COLUMN "public"."alpha_system_sso_platform"."description" IS '备注';
COMMENT ON COLUMN "public"."alpha_system_sso_platform"."deleted" IS '是否删除（0未删除，1已删除）';
COMMENT ON COLUMN "public"."alpha_system_sso_platform"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."alpha_system_sso_platform"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."alpha_system_sso_platform"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."alpha_system_sso_platform"."operator_id" IS '操作人id';

-- ----------------------------
-- Table structure for alpha_system_sso_platform_user
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."alpha_system_sso_platform_user" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "sso_platform_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "platform_user_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "system_user_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "description" varchar(512) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  "deleted" char(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamptz(6),
  "update_time" timestamptz(6),
  "delete_time" timestamptz(6),
  "operator_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
  CONSTRAINT "alpha_system_sso_platform_user_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON COLUMN "public"."alpha_system_sso_platform_user"."id" IS '主键';
COMMENT ON COLUMN "public"."alpha_system_sso_platform_user"."sso_platform_id" IS '外部系统表主键';
COMMENT ON COLUMN "public"."alpha_system_sso_platform_user"."platform_user_id" IS '外部系统用户ID';
COMMENT ON COLUMN "public"."alpha_system_sso_platform_user"."system_user_id" IS '内部系统用户id';
COMMENT ON COLUMN "public"."alpha_system_sso_platform_user"."description" IS '备注';
COMMENT ON COLUMN "public"."alpha_system_sso_platform_user"."deleted" IS '是否删除（0未删除，1已删除）';
COMMENT ON COLUMN "public"."alpha_system_sso_platform_user"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."alpha_system_sso_platform_user"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."alpha_system_sso_platform_user"."delete_time" IS '删除时间';
COMMENT ON COLUMN "public"."alpha_system_sso_platform_user"."operator_id" IS '操作人id';