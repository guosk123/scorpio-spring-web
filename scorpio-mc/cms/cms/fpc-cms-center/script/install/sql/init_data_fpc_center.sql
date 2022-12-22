-- ----------------------------
-- Table initial data for alpha
-- ----------------------------
INSERT INTO public.alpha_system_perm
(id, name_en, name_zh, description, deleted, create_time, update_time, delete_time, operator_id)
VALUES
('1', 'PERM_USER', '普通用户权限','具有普通用户权限，可以使用业务配置相关功能。', 0, NOW(), NOW(), NULL,''),
('2', 'PERM_SERVICE_USER', '业务管理用户权限','具有业务管理用户权限，可以管理业务配置相关功能。', 0, NOW(), NOW(), NULL,''),
('3', 'PERM_SYS_USER', '系统管理权限','具有系统管理权限，可以使用系统管理相关功能。', 0, NOW(), NOW(), NULL,''),
('4', 'PERM_AUDIT_USER', '审计管理权限','具有审计管理权限，可以使用日志审计管理相关功能。', 0, NOW(), NOW(), NULL,''),
('5', 'PERM_RESTAPI_USER', 'RESTAPI调用权限','具有RESTAPI调用权限，可以使用RESTAPI相关功能。', 0, NOW(), NOW(), NULL,'');

INSERT INTO public.alpha_system_role
(id, name_en, name_zh, description, deleted, create_time, update_time, delete_time, operator_id)
VALUES
('1', 'ROLE_USER', '业务用户角色', '具有普通用户权限的角色。', 0, NOW(),NOW(),NULL,''),
('2', 'ROLE_SERVICE_USER', '业务管理角色', '具有业务管理用户权限的角色。', 0, NOW(),NOW(),NULL,''),
('3', 'ROLE_SYS_USER', '系统管理角色', '具有系统管理权限的角色。', 0, NOW(),NOW(),NULL,''),
('4', 'ROLE_AUDIT_USER', '审计管理角色', '具有审计管理权限的角色。', 0, NOW(),NOW(),NULL,''),
('5', 'ROLE_RESTAPI_USER', 'RESTAPI调用角色', '具有RESTAPI调用权限的角色。', 0, NOW(),NOW(),NULL,'');

INSERT INTO public.alpha_system_user
(id, "name", fullname, email, "password", need_change_password, app_key, app_token, user_type, description, "locked", deleted, create_time, update_time, password_update_time, delete_time, operator_id)
VALUES
('1', 'admin', '系统管理员', 'admin@yourdomain.com', '$2a$10$Llv2l0656TPNg03WDE9Ps.zkTB.KtJfmIw7fE9fm7zN3HgBluqgNa', 1, '', '', 0, '系统管理员', 0, 0, NOW(), NOW(), NOW(), NULL, ''),
('2', 'audit', '审计管理员', 'audit@yourdomain.com', '$2a$10$K0CD/Q87RgOX.jo4wL.8EuJu0DT062qrNNFOgymYDUQ6x7xphGq2i', 1, '', '', 0, '审计管理员', 0, 0, NOW(), NOW(), NOW(), NULL, ''),
('3', 'adm', '配置管理员', 'adm@yourdomain.com', '$2a$10$EAGe.lHsYU8puH526I4gBu.mwqbex4SNhLdL4Xq8yU/kKmPyvAyx6', 1, '', '', 0, '配置管理员', 0, 0, NOW(), NOW(), NOW(), NULL, ''),
('4', 'cms_rest', 'CMSRest用户', 'cms_rest@yourdomain.com', '$2a$10$rET4ycY604c2Hbey9kGiEeVKzkFV7u8CFwTmqvFUJQDUhoKgl32VG', 0, 'td-XPsGtzCZQnkmhNNGn_jsec7cb8e_R', 'kMPTZbKnYgiL9kiHAu8Z5VPUxnjp0852H9zr4gbYpAaA8Ps0Y4Gh/8jvonoQEKhP', 1, 'CMSRest用户', 0, 0, NOW(), NOW(), NOW(), NULL, ''),
('5', 'cms_sso', 'CMS单点登录用户', 'cms_sso@yourdomain.com', '$2a$10$TnPPpRU2BHMVYJLos4US/u.zF8kONmlD3VI9PGYgdkHig1joWZzDW', 0, '4SApSGsEGjHXrBATyFT8fc@DD-fFt-R5', 'bz666ARHnb+RF4rjYRv5tr6EnpYKhTDLFWwx23ZUisEcyn/HdkXePOThzuwAXTPi', 2, 'CMS单点登录用户', 0, 0, NOW(), NOW(), NOW(), NULL, '');

INSERT INTO public.fpccms_appliance_storage_filter_rule
(id, name, tuple, assign_id, state, priority, description, deleted, create_time, update_time, delete_time, operator_id)
VALUES
('1', '缺省策略', '[{"action":"store"}]', '', 1, 0,  '缺省策略,应用于所有网络',  0, NOW(), NOW(), NULL,'');

INSERT INTO public.alpha_system_role_perm
(id, role_id, perm_id)
VALUES
('1', '1', '1'),
('2', '2', '2'),
('3', '3', '3'),
('4', '4', '4'),
('5', '5', '5');

INSERT INTO public.alpha_system_user_role
(id, user_id, role_id)
VALUES
('1', '1', '3'),
('2', '2', '4'),
('3', '3', '1'),
('4', '3', '2'),
('5', '4', '5'),
('6', '5', '1'),
('7', '5', '2'),
('8', '5', '5');

INSERT INTO public.alpha_system_user_menu
(id, user_id, resource, perm, update_time, operator_id)
VALUES
('1', '3', 'all', 1, NOW(), '1'),
('2', '5', 'all', 1, NOW(), '1');

INSERT INTO public.alpha_system_user_password
(id, user_id, "password", create_time )
VALUES
('1', '1', '$2a$10$Llv2l0656TPNg03WDE9Ps.zkTB.KtJfmIw7fE9fm7zN3HgBluqgNa', NOW()),
('2', '2', '$2a$10$K0CD/Q87RgOX.jo4wL.8EuJu0DT062qrNNFOgymYDUQ6x7xphGq2i', NOW()),
('3', '3', '$2a$10$EAGe.lHsYU8puH526I4gBu.mwqbex4SNhLdL4Xq8yU/kKmPyvAyx6', NOW()),
('4', '4', '$2a$10$rET4ycY604c2Hbey9kGiEeVKzkFV7u8CFwTmqvFUJQDUhoKgl32VG', NOW()),
('5', '5', '$2a$10$TnPPpRU2BHMVYJLos4US/u.zF8kONmlD3VI9PGYgdkHig1joWZzDW', NOW());

INSERT INTO public.alpha_system_sso_platform
(id, "name", platform_id, app_token, description, deleted, create_time, update_time, delete_time, operator_id)
VALUES
('1', 'ssoplatform', 'sso_platform_id', 'dzeuZmdHp8rB+2KF24YfirRk5wfda9zcnjX5JE/jtwnpaCLcl8aydLCurkh8lpsj', 'K825jN6@SFnFXYH6srYf3YAheBnnwjf6', 0, NOW(), NOW(), NULL, '');

INSERT INTO public.alpha_system_sso_platform_user
(id, sso_platform_id, platform_user_id, system_user_id, description, deleted, create_time, update_time, delete_time, operator_id)
VALUES
('1', '1', 'platform_user_id', '3', '默认配置adm用户', 0, NOW(), NOW(), NULL, '');

INSERT INTO public.alpha_system_security_settings
(id, forbidden_max_failed, password_min_length, password_max_length, password_complexity, history_password_save_times, forbidden_duration_second, session_expired_second, password_max_day, permit_multi_session, whitelist_ip_address_state, whitelist_referer, create_time, update_time)
VALUES
('1', 5, 6, 25, 'case_sensitivity_letter_number_char', 3, 1800, 600, 0, '0', '0', '', NOW(), NOW());

INSERT INTO public.alpha_system_global_settings
(id, setting_key, setting_value, update_time)
VALUES
('1', 'system.archive.setting.state', '0',NOW()),
('2', 'system.archive.setting.day', '90',NOW()),
('3', 'system.archive.time', '',NOW()),
('4', 'system.backup.setting.state', '0',NOW()),
('5', 'system.backup.setting.filenum', '5',NOW()),
('6', 'fpc.device.next.id', '1',NOW()),
('7', 'fpc.group.next.id', '1',NOW()),
('8', 'report.netif.rollup.latest.5min', '',NOW()),
('9', 'report.system.rollup.latest.5min', '',NOW()),
('10', 'report.diskio.rollup.latest.5min', '',NOW()),
('11', 'local.device.id', '',NOW()),
('12', 'local.device.name', '',NOW()),
('13', 'local.last.heartbeat', '', NOW()),
('14', 'cms.ip', '',NOW()),
('15', 'cms.token', '',NOW()),
('16', 'cms.state', '0',NOW()),
('17', 'device.sso.platform.user.id', '',NOW()),
('18', 'device.sso.login.list', '',NOW()),
('19', 'sa.custom.category.next.id', '101', NOW()),
('20', 'sa.custom.subcategory.next.id', '101', NOW()),
('21', 'sa.custom.application.next.id', '50001', NOW()),
('22', 'geoip.custom.country.next.id', '300', NOW()),
('23', 'metric.setting.server.response.normal', '100', NOW()),
('24', 'metric.setting.server.response.timeout', '400', NOW()),
('25', 'metric.setting.long.connection', '3600', NOW());

INSERT INTO public.alpha_system_alarm_settings
(id, name, level, source_type, fire_criteria, refire_seconds, state, update_time, operator_id)
VALUES 
('1', 'CPU使用率', '2', 'CPU', '[{"metric":"cpu_usage","operator":">","operand":90,"operand_unit":"percent"}]', 300, '0', NOW(), '1'),
('2', '内存使用率', '2', 'MEMORY', '[{"metric":"memory_usage","operator":">","operand":90,"operand_unit":"percent"}]', 300, '0', NOW(), '1'),
('3', '硬盘剩余空间', '2', 'DISK', '[{"metric":"fs_system_free","operator":"<","operand":10000,"operand_unit":"mb"}]', 300, '0', NOW(), '1');

-- -------------------------------
-- Table initial data for fpc cms
-- -------------------------------

INSERT INTO public.fpccms_central_device_netif
(id, device_type, monitored_serial_number, netif_name, state, category, specification, metric_time)
VALUES
('1', 'cms', '', 'eth0', 0, 0, 0, NOW());

INSERT INTO public.fpccms_analysis_suricata_rule_classtype
(id, name, deleted, create_time, update_time, delete_time, operator_id)
VALUES
    ('0', '其他', 0, NOW(), NOW(), NULL, ''),
    ('1', '挖矿行为', 0, NOW(), NOW(), NULL, ''),
    ('2', '挖矿域名', 0, NOW(), NOW(), NULL, ''),
    ('3', '命令控制', 0, NOW(), NOW(), NULL, ''),
    ('4', '隐蔽通道', 0, NOW(), NOW(), NULL, ''),
    ('5', '漏洞利用', 0, NOW(), NOW(), NULL, ''),
    ('6', '木马', 0, NOW(), NOW(), NULL, '');

INSERT INTO public.fpccms_analysis_mitre_attack
(id, name, parent_id)
VALUES
    ('0', '其他', ''),
    ('TA0001', '初始访问', ''),
    ('T1189', '水坑攻击', 'TA0001'),
    ('T1190', '利用面向公众的应用', 'TA0001'),
    ('T1200', '硬件添加', 'TA0001'),
    ('T1091', '通过可移动媒体进行复制', 'TA0001,TA0008'),
    ('T1566', '网络钓鱼', 'TA0001'),
    ('T1195', '供应链攻击', 'TA0001'),
    ('T1199', '可信关系', 'TA0001'),
    ('T1078', '有效帐户', 'TA0001,TA0003,TA0004,TA0005'),
    ('T1133', '外部远程服务', 'TA0001,TA0003'),
    ('TA0002', '执行', ''),
    ('T1059', '命令和脚本', 'TA0002'),
    ('T1609', '容器管理命令', 'TA0002'),
    ('T1610', '部署容器', 'TA0002,TA0005'),
    ('T1203', '利用漏洞进行客户端执行', 'TA0002'),
    ('T1559', '进程间通信', 'TA0002'),
    ('T1106', '利用OS API执行', 'TA0002'),
    ('T1053', '工作/任务调度', 'TA0002,TA0003,TA0004'),
    ('T1129', '共享模块', 'TA0002'),
    ('T1072', '软件部署工具', 'TA0002,TA0008'),
    ('T1569', '系统服务', 'TA0002'),
    ('T1204', '用户执行', 'TA0002'),
    ('T1047', 'windows管理工具', 'TA0002'),
    ('TA0003', '持久化', ''),
    ('T1098', '账户操纵', 'TA0003'),
    ('T1197', 'BITS 工作', 'TA0003,TA0005'),
    ('T1547', '引导或登录自动执行', 'TA0003,TA0004'),
    ('T1037', '引导或登录初始化脚本', 'TA0003,TA0004'),
    ('T1176', '浏览器扩展', 'TA0003'),
    ('T1554', '入侵客户端软件', 'TA0003'),
    ('T1136', '创建账号', 'TA0003'),
    ('T1543', '创建或修改系统进程', 'TA0003,TA0004'),
    ('T1546', '事件触发执行', 'TA0003,TA0004'),
    ('T1574', '劫持执行流', 'TA0003,TA0004,TA0005'),
    ('T1525', '注入内部镜像', 'TA0003'),
    ('T1556', '修改认证过程', 'TA0003,TA0005,TA0006'),
    ('T1137', 'office应用启动', 'TA0003'),
    ('T1542', 'OS引导前利用', 'TA0003,TA0005'),
    ('T1505', 'server应用组件', 'TA0003'),
    ('T1205', '流量信号', 'TA0003,TA0005,TA0011'),
    ('TA0004', '权限提升', ''),
    ('T1548', '滥用提权控制机制', 'TA0004,TA0005'),
    ('T1134', '篡改访问令牌', 'TA0004,TA0005'),
    ('T1484', '域策略修改', 'TA0004,TA0005'),
    ('T1611', '逃逸到宿主机', 'TA0004'),
    ('T1068', '利用漏洞提权', 'TA0004'),
    ('T1055', '进程注入', 'TA0004,TA0005'),
    ('TA0005', '防御绕过', ''),
    ('T1612', '构建自定义镜像', 'TA0005'),
    ('T1140', '反混淆/解码文件或信息', 'TA0005'),
    ('T1006', '直接卷访问', 'TA0005'),
    ('T1480', '执行护栏', 'TA0005'),
    ('T1211', '利用防御漏洞', 'TA0005'),
    ('T1222', '文件和目录权限修改', 'TA0005'),
    ('T1564', '隐藏痕迹', 'TA0005'),
    ('T1562', '削弱防御能力', 'TA0005'),
    ('T1070', '移除痕迹', 'TA0005'),
    ('T1202', '间接命令执行', 'TA0005'),
    ('T1036', '伪装', 'TA0005'),
    ('T1578', '修改云计算基础设施', 'TA0005'),
    ('T1112', '修改注册表', 'TA0005'),
    ('T1601', '修改系统镜像', 'TA0005'),
    ('T1599', '网络边界桥接', 'TA0005'),
    ('T1027', '混淆文件或信息', 'TA0005'),
    ('T1620', '反射式代码加载', 'TA0005'),
    ('T1207', 'DCShadow', 'TA0005'),
    ('T1014', 'Rootkit', 'TA0005'),
    ('T1218', '签名二进制代理执行', 'TA0005'),
    ('T1216', '签名脚本代理执行', 'TA0005'),
    ('T1553', '颠覆信任控制', 'TA0005'),
    ('T1221', '模板注入', 'TA0005'),
    ('T1127', '可信开发工具代理执行', 'TA0005'),
    ('T1535', '未使用/不支持的云区域', 'TA0005'),
    ('T1550', '使用替代认证材料', 'TA0005,TA0008'),
    ('T1497', '虚拟机/沙箱逃避', 'TA0005,TA0007'),
    ('T1600', '削弱加密', 'TA0005'),
    ('T1220', 'XSL脚本处理', 'TA0005'),
    ('TA0006', '凭据访问', ''),
    ('T1557', '中间人攻击', 'TA0006,TA0009'),
    ('T1110', '暴力破解', 'TA0006'),
    ('T1555', '来自密码存储的凭据', 'TA0006'),
    ('T1212', '凭据访问攻击', 'TA0006'),
    ('T1187', '强制认证', 'TA0006'),
    ('T1606', '伪造Web凭证', 'TA0006'),
    ('T1056', '输入捕获', 'TA0006,TA0009'),
    ('T1040', '网络嗅探', 'TA0006,TA0007'),
    ('T1003', 'OS凭据转储', 'TA0006'),
    ('T1528', '窃取应用访问令牌', 'TA0006'),
    ('T1539', '窃取web会话cookie', 'TA0006'),
    ('T1111', '双因子认证拦截', 'TA0006'),
    ('T1552', '不安全凭据', 'TA0006'),
    ('TA0007', '发现', ''),
    ('T1087', '账号发现', 'TA0007'),
    ('T1010', '应用窗口发现', 'TA0007'),
    ('T1217', '浏览器书签发现', 'TA0007'),
    ('T1580', '云设施发现', 'TA0007'),
    ('T1538', '云服务看板', 'TA0007'),
    ('T1526', '云服务发现', 'TA0007'),
    ('T1619', '云存储对象发现', 'TA0007'),
    ('T1613', '容器和资源发现', 'TA0007'),
    ('T1482', '域信任发现', 'TA0007'),
    ('T1083', '文件和目录发现', 'TA0007'),
    ('T1615', '组策略发现', 'TA0007'),
    ('T1046', '网络服务扫描', 'TA0007'),
    ('T1135', '网络共享发现', 'TA0007'),
    ('T1201', '密码策略发现', 'TA0007'),
    ('T1120', '外围设备发现', 'TA0007'),
    ('T1069', '权限组发现', 'TA0007'),
    ('T1057', '进程发现', 'TA0007'),
    ('T1012', '查询注册表', 'TA0007'),
    ('T1018', '远程系统发现', 'TA0007'),
    ('T1518', '软件发现', 'TA0007'),
    ('T1082', '系统信息发现', 'TA0007'),
    ('T1614', '系统位置发现', 'TA0007'),
    ('T1016', '系统网络配置发现', 'TA0007'),
    ('T1049', '系统网络连接发现', 'TA0007'),
    ('T1033', '系统所有者/用户发现', 'TA0007'),
    ('T1007', '系统设备发现', 'TA0007'),
    ('TA0008', '横向移动', ''),
    ('T1210', '利用远程服务', 'TA0008'),
    ('T1534', '内部网络钓鱼', 'TA0008'),
    ('T1570', '横向工具传输', 'TA0008'),
    ('T1563', '远程服务会话劫持', 'TA0008'),
    ('T1021', '远程服务', 'TA0008'),
    ('T1080', '污染共享内容', 'TA0008'),
    ('TA0009', '收集', ''),
    ('T1560', '打包收集到的数据', 'TA0009'),
    ('T1123', '声音捕获', 'TA0009'),
    ('T1119', '自动收集', 'TA0009'),
    ('T1185', '浏览器会话劫持', 'TA0009'),
    ('T1115', '剪贴板数据', 'TA0009'),
    ('T1530', '云存储对象数据', 'TA0009'),
    ('T1602', '配置库数据', 'TA0009'),
    ('T1213', '信息库数据', 'TA0009'),
    ('T1005', '本地系统数据', 'TA0009'),
    ('T1039', '网络共享驱动器数据', 'TA0009'),
    ('T1025', '可移动介质数据', 'TA0009'),
    ('T1074', '数据暂存', 'TA0009'),
    ('T1114', '邮件收集', 'TA0009'),
    ('T1113', '屏幕捕获', 'TA0009'),
    ('T1125', '视频捕获', 'TA0009'),
    ('TA0011', '命令控制', ''),
    ('T1071', '标准应用层协议', 'TA0011'),
    ('T1092', '通过可移动介质通信', 'TA0011'),
    ('T1132', '数据编码', 'TA0011'),
    ('T1001', '数据混淆', 'TA0011'),
    ('T1568', '动态解析', 'TA0011'),
    ('T1573', '加密通道', 'TA0011'),
    ('T1008', '后备通道', 'TA0011'),
    ('T1105', 'Ingress工具传输', 'TA0011'),
    ('T1104', '多级通道', 'TA0011'),
    ('T1095', '非标准应用层协议', 'TA0011'),
    ('T1571', '非标准端口', 'TA0011'),
    ('T1572', '协议隧道', 'TA0011'),
    ('T1090', '代理', 'TA0011'),
    ('T1219', '远程访问软件', 'TA0011'),
    ('T1102', 'Web服务', 'TA0011'),
    ('TA0010', '数据渗出', ''),
    ('T1020', '自动渗出', 'TA0010'),
    ('T1030', '数据传输大小限制', 'TA0010'),
    ('T1048', '通过替代协议渗出', 'TA0010'),
    ('T1041', '通过C2通道渗出', 'TA0010'),
    ('T1011', '通过其他网络介质渗出', 'TA0010'),
    ('T1052', '通过物理介质渗出', 'TA0010'),
    ('T1567', '通过Web服务渗出', 'TA0010'),
    ('T1029', '定时传输', 'TA0010'),
    ('T1537', '传输数据到云账号', 'TA0010'),
    ('TA0040', '影响', ''),
    ('T1531', '删除账号访问权限', 'TA0040'),
    ('T1485', '数据销毁', 'TA0040'),
    ('T1486', '加密数据以产生影响', 'TA0040'),
    ('T1565', '数据操纵', 'TA0040'),
    ('T1491', '污损', 'TA0040'),
    ('T1561', '磁盘擦除', 'TA0040'),
    ('T1499', '终端拒绝服务', 'TA0040'),
    ('T1495', '固件损坏', 'TA0040'),
    ('T1490', '禁止系统恢复', 'TA0040'),
    ('T1498', '网络拒绝服务', 'TA0040'),
    ('T1496', '资源劫持', 'TA0040'),
    ('T1489', '停止服务', 'TA0040'),
    ('T1529', '系统关闭/停止', 'TA0040'),
    ('TA0042', '资源开发', ''),
    ('T1583', '获取基础设施', 'TA0042'),
    ('T1584', '攻陷基础设施', 'TA0042'),
    ('T1586', '盗取账户', 'TA0042'),
    ('T1587', '开发能力', 'TA0042'),
    ('T1585', '建立账户', 'TA0042'),
    ('T1588', '获取能力', 'TA0042'),
    ('T1608', '筹划能力', 'TA0042'),
    ('TA0043','侦查', ''),
    ('T1589', '收集受害者身份信息', 'TA0043'),
    ('T1590', '收集受害者网络信息', 'TA0043'),
    ('T1591', '收集受害者组织信息', 'TA0043'),
    ('T1592', '收集受害者主机信息', 'TA0043'),
    ('T1593', '搜索公开网站/域名', 'TA0043'),
    ('T1594', '搜索受害者拥有的网站', 'TA0043'),
    ('T1595', '主动扫描', 'TA0043'),
    ('T1596', '搜索公开的技术数据库', 'TA0043'),
    ('T1597', '搜索闭源数据', 'TA0043'),
    ('T1598', '钓鱼信息', 'TA0043');

INSERT INTO public.fpccms_analysis_suricata_rule
(id, sid, "action", protocol, src_ip, src_port, direction, dest_ip, dest_port, msg, rev, "content", priority, classtype_id, mitre_tactic_id, mitre_technique_id, cve, cnnvd, signature_severity, target, threshold, "rule", parse_state, parse_log, state, "source", deleted, create_time, update_time, delete_time, operator_id)
VALUES
('1', 197001011, 'alert', 'dhcp', 'any', 'any', '->', '![10.0.0.1, 192.168.1.1]', 'any', 'DHCP-假冒服务器', 1, '', 3, '0', '0', '', '', '', 2, 'dest_ip', '', 'alert dhcp any any -> ![10.0.0.1, 192.168.1.1] any (msg:"DHCP"; sid: 197001011; priority: 3; metadata:classtype_id 0, mitre_tactic_id 0, signature_severity 2, source 0;)', 0, '', 0, 0, 0, NOW(), NOW(), NULL, ''),
('2', 197001012, 'alert', 'dhcp', 'any', 'any', '->', 'any', 'any', 'DHCP-DOS攻击', 1, 'flow:to_server;', 3, '0', '0', '', '', '', 2, 'dest_ip', 'type both, track by_dst, count 1000, seconds 5', 'alert dhcp any any -> any any (msg:"DHCP-DOS攻击"; flow:to_server; threshold: type both, track by_dst, count 1000, seconds 5; sid: 197001012; priority: 3; metadata:classtype_id 0, mitre_tactic_id 0, signature_severity 2, source 0;)', 0, '', 0, 0, 0, NOW(), NOW(), NULL, ''),
('3', 197001013, 'alert', 'dns', 'any', 'any', '->', 'any', 'any', 'DNS-Dos攻击', 1, 'flow:to_server;', 3, '0', '0', '', '', '', 2, 'dest_ip', 'type both, track by_dst, count 1000, seconds 5', 'alert dns any any -> any any (msg:"DNS-Dos攻击"; flow:to_server; threshold: type both, track by_dst, count 1000, seconds 5; sid: 197001013; priority: 3; metadata:classtype_id 0, mitre_tactic_id 0, signature_severity 2, source 0;)', 0, '', 0, 0, 0, NOW(), NOW(), NULL, ''),
('4', 197001014, 'alert', 'dns', 'any', 'any', '->', 'any', 'any', 'DNS-回复报文超长', 1, 'dsize:>1024; flow:to_server;', 3, '0', '0', '', '', '', 2, 'dest_ip', 'type both, track by_dst, count 1000, seconds 5', 'alert dns any any -> any any (msg:"DNS-回复报文超长"; dsize:>1024; flow:to_server; threshold: type both, track by_dst, count 1000, seconds 5; sid: 197001014; priority: 3; metadata:classtype_id 0, mitre_tactic_id 0, signature_severity 2, source 0;)', 0, '', 0, 0, 0, NOW(), NOW(), NULL, '');