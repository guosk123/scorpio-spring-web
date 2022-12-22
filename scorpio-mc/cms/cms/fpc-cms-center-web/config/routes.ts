export default [
  {
    path: '/login',
    name: 'login',
    title: '用户登录',
    hideInMenu: true,
    component: '../layouts/UserLayout',
    routes: [
      { path: '/login', component: './frame/Login' },
      {
        component: './frame/Exception/404',
      },
    ],
  },
  {
    path: '/',
    component: '../layouts/SecurityLayout',
    routes: [
      {
        path: '/',
        component: '../layouts/BasicLayout',
        routes: [
          { path: '/', redirect: '/redirect' },
          {
            path: '/redirect',
            name: 'home',
            hideInMenu: true,
            component: './Redirect',
          },
          {
            path: '/dashboard',
            icon: 'DashboardOutlined',
            name: 'dashboard',
            title: '仪表盘',
            access: 'hasUserPerm',
            routes: [
              {
                path: '/dashboard',
                redirect: '/dashboard/system',
              },
              {
                path: '/dashboard/system',
                name: 'system',
                title: '系统仪表盘',
                access: 'hasUserPerm',
                component: './app/Dashboard/components/DashboardLayout',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/dashboard/system',
                    redirect: '/dashboard/system/device',
                  },
                  {
                    path: '/dashboard/system/device',
                    name: 'device',
                    title: '设备状态',
                    access: 'hasUserPerm',
                    component: './app/Dashboard/Device',
                  },
                  {
                    path: '/dashboard/system/network',
                    name: 'network',
                    title: '网络流量',
                    access: 'hasUserPerm',
                    component: './app/Dashboard/Network',
                  },
                ],
              },
              {
                path: '/dashboard/bi',
                title: '自定义仪表盘',
                name: 'bi',
                access: 'hasUserPerm',
                component: './app/Bi/Dashboard',
              },
            ],
          },
          {
            path: '/report',
            icon: 'FormOutlined',
            name: 'report',
            title: '报表',
            access: 'hasUserPerm',
            routes: [
              {
                path: '/report',
                redirect: '/report/custom',
              },
              {
                path: '/report/custom',
                name: 'custom',
                title: '自定义报表',
                component: './app/Report/Custom',
              },
              {
                path: '/report/widget',
                name: 'widget',
                title: '图表',
                component: './app/Report/Widget',
              },
              {
                path: '/report/datasource',
                name: 'datasource',
                title: '数据源',
                component: './app/Report/Datasource',
              },
              {
                component: './frame/Exception/404',
              },
            ],
          },
          {
            path: '/performance',
            icon: 'global',
            name: 'performance',
            title: '性能分析',
            access: 'hasUserPerm',
            // component: './app/Network',
            component: './app/Network/NetworkLayout',
            routes: [
              {
                path: '/performance',
                redirect: '/performance/list',
              },
              {
                path: '/performance/network/list',
                name: 'network.list',
                title: '所有网络',
                access: 'hasUserPerm',
                hideChildrenInMenu: true,
                component: './app/Network/List',
              },
              {
                path: '/performance/network/topology',
                name: 'network.topology',
                title: '网络拓扑',
                component: './app/Network/Topology',
              },
              {
                path: '/performance/network/:networkId/analysis',
                name: 'network.analysis',
                title: '网络分析',
                access: 'hasUserPerm',
                hideChildrenInMenu: true,
                component: './app/Network/Analysis',
              },
              {
                path: '/performance/dimensions-search',
                name: 'dimensionsSearch',
                title: '多维检索',
                access: 'hasUserPerm',
                hideChildrenInMenu: true,
                component: './app/GlobalSearch/DimensionsSearch/layout/DimensionsLayout',
                routes: [
                  {
                    path: '/performance/dimensions-search',
                    redirect: '/performance/dimensions-search/search',
                  },
                  {
                    path: '/performance/dimensions-search/search',
                    name: 'search',
                    title: '检索',
                    access: 'hasUserPerm',
                    component: './app/GlobalSearch/DimensionsSearch',
                  },
                ],
              },
              {
                path: '/performance/service',
                name: 'server',
                title: '业务分析',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/performance/service',
                    redirect: '/performance/service/list',
                  },
                  {
                    path: '/performance/service/list',
                    name: 'list',
                    title: '业务看板',
                    hideChildrenInMenu: true,
                    component: './app/analysis/Service/List',
                  },
                  {
                    path: '/performance/service/:serviceId/:networkId',
                    name: 'detail',
                    title: '详情',
                    hideChildrenInMenu: true,
                    component: './app/analysis/Service/index',
                  },
                ],
              },
              {
                path: '/performance/network/segmentAnalysis',
                name: 'segmentAnalysis',
                title: '分段分析',
                access: 'hasUserPerm',
                component: './app/analysis/SegmentAnalysis',
              },
            ],
          },
          // {
          //   path: '/analysis/service',
          //   name: 'analysis.service',
          //   title: '业务性能分析',
          //   icon: 'appstore',
          //   access: 'hasUserPerm',
          //   hideChildrenInMenu: true,
          //   routes: [
          //     {
          //       path: '/analysis/service',
          //       redirect: '/analysis/service/list',
          //     },
          //     {
          //       path: '/analysis/service/:serviceId/:networkId',
          //       name: 'analysis',
          //       title: '详情',
          //       hideChildrenInMenu: true,
          //       component: './app/analysis/Service/index',
          //     },
          //     {
          //       path: '/analysis/service/list',
          //       name: 'list',
          //       title: '业务看板',
          //       component: './app/analysis/Service/List',
          //     },
          //     {
          //       component: './frame/Exception/404',
          //     },
          //   ],
          // },
          // 溯源拓线
          {
            path: '/flow-trace',
            icon: 'FileSearchOutlined',
            name: 'flow.analysis',
            title: '溯源分析',
            access: 'hasUserPerm',
            component: './app/FlowTrace/components/FlowTraceLayout',
            routes: [
              {
                path: '/flow-trace',
                redirect: '/flow-trace/packet-retrieval/task-list',
              },
              {
                path: '/flow-trace/ip-image',
                name: 'ip-image',
                title: 'IP画像',
                access: 'hasUserPerm',
                component: './app/FlowTrace/IPImage',
              },
              {
                path: '/flow-trace/packet-retrieval',
                name: 'flow.trace',
                title: '流量溯源',
                access: 'hasUserPerm',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/flow-trace/packet-retrieval',
                    redirect: '/flow-trace/packet-retrieval/task-list',
                  },
                  {
                    path: '/flow-trace/packet-retrieval/task-list',
                    name: 'list',
                    title: '任务列表',
                    access: 'hasUserPerm',
                    component: './app/GlobalSearch/PacketRetrieval/List',
                  },
                  {
                    path: '/flow-trace/packet-retrieval/create-task',
                    name: 'create-task',
                    title: '新建任务',
                    access: 'hasUserPerm',
                    component: './app/GlobalSearch/PacketRetrieval/Create',
                  },
                  {
                    path: '/flow-trace/packet-retrieval/:taskId/copy',
                    name: 'copy-task',
                    title: '复制查询任务',
                    access: 'hasUserPerm',
                    component: './app/GlobalSearch/PacketRetrieval/Copy',
                  },
                ],
              },
              {
                path: '/flow-trace/trace/ip-graph',
                name: 'ip-graph',
                title: '访问关系',
                access: 'hasUserPerm',
                component: './app/Network/IpGraph',
              },
              {
                path: '/flow-trace/mata-data-analysis',
                name: 'meta-data.analysis',
                title: '应用层协议分析',
                access: 'hasUserPerm',
                component: './app/appliance/Metadata',
              },
              {
                path: '/flow-trace/mata-data-detail',
                name: 'meta-data.detail',
                title: '应用层协议详单',
                access: 'hasUserPerm',
                component: './app/appliance/Metadata',
              },
              {
                path: '/flow-trace/flow-record',
                name: 'flow-record',
                title: '会话详单',
                access: 'hasUserPerm',
                component: './app/appliance/FlowRecords/Record',
              },
              {
                path: '/flow-trace/packet',
                name: 'packet',
                title: '数据包',
                hideChildrenInMenu: true,
                access: 'hasUserPerm',
                routes: [
                  {
                    path: '/flow-trace/packet',
                    redirect: '/flow-trace/packet/statistics',
                  },
                  {
                    path: '/flow-trace/packet/statistics',
                    name: 'statistics',
                    title: '详情',
                    access: 'hasUserPerm',
                    component: './app/appliance/Packet',
                  },
                  {
                    path: '/flow-trace/packet/analysis',
                    name: 'analysis',
                    title: '在线分析',
                    access: 'hasUserPerm',
                    component: './app/appliance/Packet',
                  },
                ],
              },
              {
                path: '/flow-trace/ioc',
                name: 'ioc',
                title: 'IOC溯源',
                access: 'hasUserPerm',
                component: './app/FlowTrace/IOC',
              },
            ],
          },
          {
            path: '/security',
            icon: 'SecurityScanOutlined',
            title: '安全分析',
            name: 'security',
            component: './app/security/Layout',
            access: 'hasUserPerm',
            routes: [
              {
                path: '/security/situation',
                name: 'situation',
                title: '安全态势',
                access: 'hasUserPerm',
                component: './app/security/Situation',
              },
              {
                path: '/security/dashboard',
                name: 'dashboard',
                title: '概览',
                access: 'hasUserPerm',
                component: './app/security/Dashboard',
              },
              {
                path: '/security/mining',
                name: 'mining',
                title: '挖矿',
                access: 'hasUserPerm',
                component: './app/security/MiningAnalysis',
              },
              {
                path: '/security/alert',
                name: 'alert',
                title: '告警',
                access: 'hasUserPerm',
                component: './app/security/SuricataAlert',
              },
            ],
          },
          {
            path: '/analysis/video',
            name: 'analysis.video',
            title: '音视频性能分析',
            icon: 'VideoCameraOutlined',
            access: 'hasUserPerm',
            component: './app/analysis/Video/components/NetworkTimeLayout',
            routes: [
              {
                path: '/analysis/video',
                redirect: '/analysis/video/devices',
                accessMenuIgnore: true,
              },
              {
                path: '/analysis/video/devices',
                name: 'devices',
                title: 'IP设备分析',
                access: 'hasUserPerm',
                component: './app/analysis/Video/components/VideoEditTabs',
                hideChildrenInMenu: true,
              },
              {
                path: '/analysis/video/rtp',
                name: 'rtp',
                title: 'RTP流分析',
                access: 'hasUserPerm',
                component: './app/analysis/Video/RTP',
              },
            ],
          },
          // {
          // path: '/configuration',
          // icon: 'history',
          // name: 'configuration',
          // title: '功能配置',
          // access: 'hasUserPerm',
          // component: './app/Configuration',
          // },
          {
            path: '/system',
            icon: 'setting',
            name: 'system',
            title: 'CMS系统管理',
            access: 'hasAdminOrAuditPerm',
            routes: [
              {
                path: '/system',
                component: './frame/System',
              },
              {
                path: '/system/monitor',
                name: 'monitor',
                title: '系统状态',
                access: 'hasAdminPerm',
                component: '../layouts/PageLayout',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/system/monitor',
                    redirect: '/system/monitor/running',
                  },
                  {
                    path: '/system/monitor/running',
                    name: 'running',
                    title: '运行状态',
                    component: './frame/System/Status',
                  },
                  {
                    component: './frame/Exception/404',
                  },
                ],
              },
              {
                path: '/system/setting',
                name: 'setting',
                title: '系统配置',
                access: 'hasAdminPerm',
                component: '../layouts/PageLayout',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/system/setting',
                    redirect: '/system/setting/server-ip',
                  },
                  {
                    path: '/system/setting/license',
                    name: 'license',
                    title: 'License',
                    hideInMenu: true,
                    icon: 'safety-certificate',
                    component: './frame/System/License',
                  },
                  {
                    path: '/system/setting/server-ip',
                    name: 'server-ip',
                    title: '管理口配置',
                    component: './frame/System/ServerIp',
                  },
                  {
                    path: '/system/setting/device-ntp',
                    name: 'device-ntp',
                    title: '系统时间',
                    component: './frame/System/DeviceNTP',
                  },
                  {
                    path: '/system/setting/snmp',
                    name: 'snmp',
                    title: 'SNMP配置',
                    component: './frame/System/SNMP',
                  },
                  {
                    path: '/system/setting/power',
                    name: 'power',
                    title: '电源管理',
                    icon: 'bulb',
                    component: './frame/System/Power',
                  },
                  {
                    path: '/system/setting/backup',
                    name: 'backup',
                    title: '系统备份',
                    icon: 'project',
                    component: './frame/System/Backup',
                  },
                  {
                    path: '/system/setting/security',
                    name: 'security',
                    title: '安全配置',
                    icon: 'insurance',
                    component: './frame/System/Security',
                  },
                  {
                    path: '/system/setting/alert',
                    name: 'alert',
                    title: '系统告警',
                    icon: 'rule',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/system/setting/alert',
                        component: './frame/System/Alert',
                      },
                      {
                        path: '/system/setting/alert/:id',
                        name: 'detail',
                        title: '系统告警详情',
                        component: './frame/System/Alert/Rule',
                      },
                      {
                        component: './frame/Exception/404',
                      },
                    ],
                  },
                  {
                    path: '/system/setting/device-info',
                    name: 'device-setting',
                    title: '设备信息',
                    component: './frame/System/SSO/DeviceInfo',
                  },
                  {
                    path: '/system/setting/upgrade',
                    name: 'upgrade',
                    title: 'cms升级',
                    component: './frame/System/Upgrade',
                  },
                  {
                    component: './frame/Exception/404',
                  },
                ],
              },
              {
                path: '/system/log-alarm',
                name: 'log-alerm',
                title: '日志告警',
                icon: 'file',
                component: '../layouts/PageLayout',
                access: 'hasAdminPerm',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/system/log-alarm',
                    redirect: '/system/log-alarm/log',
                  },
                  {
                    path: '/system/log-alarm/log',
                    name: 'log',
                    title: '日志',
                    access: 'hasAdminOrAuditPerm',
                    component: './frame/System/LogAlerm/Log',
                  },
                  {
                    path: '/system/log-alarm/alarm',
                    name: 'alarm',
                    title: '告警',
                    access: 'hasAdminPerm',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/system/log-alarm/alarm',
                        name: 'list',
                        title: '告警',
                        access: 'hasAdminPerm',
                        component: './frame/System/LogAlerm/Alerm',
                      },
                      {
                        path: '/system/log-alarm/alarm/detail',
                        name: 'detail',
                        title: '告警详情',
                        access: 'hasAdminPerm',
                        component: './frame/System/LogAlerm/Alerm/Detail',
                      },
                      {
                        component: './frame/Exception/404',
                      },
                    ],
                  },
                  // {
                  //   path: '/system/log-alarm/syslog',
                  //   name: 'syslog',
                  //   title: '外发',
                  //   access: 'hasAdminPerm',
                  //   component: './frame/System/LogAlerm/SendOut',
                  // },
                  {
                    path: '/system/log-alarm/archive',
                    name: 'archive',
                    title: '归档',
                    access: 'hasAdminPerm',
                    component: './frame/System/LogAlerm/Archive',
                  },
                  {
                    component: './frame/Exception/404',
                  },
                ],
              },
              {
                path: '/system/log-system',
                name: 'log-system',
                title: '审计日志',
                icon: 'file',
                access: 'hasAuditPerm',
                component: '../layouts/PageLayout',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/system/log-system',
                    redirect: '/system/log-system/log',
                  },
                  {
                    path: '/system/log-system/log',
                    name: 'log',
                    title: '日志',
                    access: 'hasAuditPerm',
                    component: './frame/System/LogAlerm/Log',
                  },
                  {
                    component: './frame/Exception/404',
                  },
                ],
              },
              {
                path: '/system/user',
                name: 'user',
                title: '用户管理',
                icon: 'team',
                access: 'hasAdminPerm',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/system/user',
                    name: 'list',
                    title: '用户管理',
                    access: 'hasAdminPerm',
                    component: './frame/System/User',
                  },
                  {
                    path: '/system/user/create',
                    name: 'create',
                    title: '新建用户',
                    access: 'hasAdminPerm',
                    component: './frame/System/User/Create',
                  },
                  {
                    path: '/system/user/update',
                    name: 'update',
                    title: '编辑用户',
                    access: 'hasAdminPerm',
                    component: './frame/System/User/Update',
                  },
                  {
                    component: './frame/Exception/404',
                  },
                ],
              },
              {
                path: '/system/sso',
                name: 'sso',
                title: '单点登录',
                icon: 'user',
                access: 'hasAdminPerm',
                component: '../layouts/PageLayout',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/system/sso',
                    redirect: '/system/sso/user',
                  },
                  {
                    path: '/system/sso/user',
                    name: 'user',
                    title: '用户关联',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/system/sso/user',
                        component: './frame/System/SSO/User',
                      },
                      {
                        path: '/system/sso/user/create',
                        name: 'create',
                        title: '新建用户关联',
                        component: './frame/System/SSO/User/Create',
                      },
                      {
                        path: '/system/sso/user/update',
                        name: 'update',
                        title: '编辑用户关联',
                        component: './frame/System/SSO/User/Update',
                      },
                      {
                        component: './frame/Exception/404',
                      },
                    ],
                  },
                  {
                    path: '/system/sso/platform',
                    name: 'platform',
                    title: '外部系统',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/system/sso/platform',
                        component: './frame/System/SSO/Platform',
                      },
                      {
                        path: '/system/sso/platform/create',
                        name: 'create',
                        title: '新建外部系统',
                        component: './frame/System/SSO/Platform/Create',
                      },
                      {
                        path: '/system/sso/platform/update',
                        name: 'update',
                        title: '编辑外部系统',
                        component: './frame/System/SSO/Platform/Update',
                      },
                      {
                        component: './frame/Exception/404',
                      },
                    ],
                  },
                  {
                    path: '/system/sso/fpc-single-sign-settings',
                    name: 'fpc-single-sign-settings',
                    title: '可登录设备配置',
                    component: './frame/System/SSO/SingleSignSetting',
                  },
                  {
                    component: './frame/Exception/404',
                  },
                ],
              },
              // {
              //   path: '/system/webssh',
              //   name: 'webssh',
              //   title: 'web终端',
              // access: 'hasAdminPerm',
              //   component: './frame/System/WebShell',
              // },
              {
                path: '/system/product',
                name: 'product',
                title: '产品信息',
                access: 'hasAdminPerm',
                component: './frame/System/Product',
              },
              {
                component: './frame/Exception/404',
              },
            ],
          },
          //日志告警
          {
            path: '/logAlarm',
            access: 'hasUserPerm',
            name: 'logAlarm',
            title: '日志告警',
            icon: 'AlertOutlined',
            // component: '../layouts/PageLayout',
            routes: [
              {
                path: '/logAlarm',
                redirect: '/logAlarm/log',
              },
              {
                path: '/logAlarm/log',
                name: 'log',
                title: '日志',
                access: 'hasUserPerm',
                component: './frame/System/LogAlerm/Log',
              },
              {
                path: '/logAlarm/alarm',
                name: 'alarm',
                title: '告警',
                access: 'hasUserPerm',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/logAlarm/alarm',
                    name: 'list',
                    title: '告警',
                    component: './frame/System/LogAlerm/Alerm',
                  },
                  {
                    path: '/logAlarm/alarm/detail',
                    name: 'detail',
                    title: '告警详情',
                    component: './frame/System/LogAlerm/Alerm/Detail',
                  },
                  {
                    component: './frame/Exception/404',
                  },
                ],
              },
              {
                component: './frame/Exception/404',
              },
            ],
          },
          // 功能配置
          {
            path: '/configuration',
            access: 'hasUserPerm',
            title: '功能配置',
            name: 'configuration',
            icon: 'ToolOutlined',
            routes: [
              {
                path: '/configuration',
                redirect: '/configuration/network',
              },
              {
                path: '/configuration/network',
                access: 'hasUserPerm',
                title: '网络配置',
                name: 'network',
                component: '../layouts/PageLayout',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/configuration/network',
                    redirect: '/configuration/network/sensor',
                  },
                  {
                    path: '/configuration/network/sensor',
                    access: 'hasUserPerm',
                    title: '探针网络',
                    name: 'sensor',
                    routes: [
                      {
                        path: '/configuration/network/sensor',
                        component: './app/Configuration/Network/Sensor',
                      },
                      {
                        path: '/configuration/network/sensor/create',
                        access: 'hasServiceUserPerm',
                        title: '添加探针网络',
                        name: 'create',
                        component: './app/Configuration/Network/Sensor/Create',
                      },
                      {
                        path: '/configuration/network/sensor/:id/update',
                        access: 'hasServiceUserPerm',
                        title: '编辑探针网络',
                        name: 'update',
                        component: './app/Configuration/Network/Sensor/Create',
                      },
                    ],
                  },
                  {
                    path: '/configuration/network/group',
                    access: 'hasUserPerm',
                    title: '网络组',
                    name: 'group',
                    routes: [
                      {
                        path: '/configuration/network/group',
                        component: './app/Configuration/Network/Group',
                      },
                      {
                        path: '/configuration/network/group/create',
                        access: 'hasServiceUserPerm',
                        title: '添加网络组',
                        name: 'create',
                        component: './app/Configuration/Network/Group/Create',
                      },
                      {
                        path: '/configuration/network/group/:id/update',
                        access: 'hasServiceUserPerm',
                        title: '编辑网络组',
                        name: 'update',
                        component: './app/Configuration/Network/Group/Create',
                      },
                    ],
                  },
                  {
                    path: '/configuration/network/logical-subnet',
                    access: 'hasUserPerm',
                    title: '逻辑子网',
                    name: 'logical-subnet',
                    routes: [
                      {
                        path: '/configuration/network/logical-subnet',
                        component: './app/Configuration/LogicalSubnet',
                      },
                      {
                        path: '/configuration/network/logical-subnet/create',
                        access: 'hasServiceUserPerm',
                        title: '添加逻辑子网',
                        name: 'create',
                        component: './app/Configuration/LogicalSubnet/Create',
                      },
                      {
                        path: '/configuration/network/logical-subnet/:id/update',
                        access: 'hasServiceUserPerm',
                        title: '编辑逻辑子网',
                        name: 'update',
                        component: './app/Configuration/LogicalSubnet/Update',
                      },
                    ],
                  },
                  {
                    path: '/configuration/network/perm',
                    access: 'hasServiceUserPerm',
                    title: '网络权限',
                    name: 'perm',
                    component: './app/Configuration/Network/Perm',
                  },
                ],
              },
              {
                path: '/configuration/service-settings',
                name: 'service-settings',
                title: '业务',
                component: '../layouts/PageLayout',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/configuration/service-settings',
                    redirect: '/configuration/service-settings/service',
                  },
                  {
                    path: '/configuration/service-settings/service',
                    name: 'service',
                    title: '业务配置',
                    icon: 'inbox',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/configuration/service-settings/service',
                        component: './app/Configuration/Service',
                      },
                      {
                        path: '/configuration/service-settings/service/create',
                        name: 'create',
                        title: '新建业务',
                        component: './app/Configuration/Service/Create',
                      },
                      {
                        path: '/configuration/service-settings/service/:serviceId/update',
                        name: 'update',
                        title: '编辑业务',
                        component: './app/Configuration/Service/Update',
                      },
                      {
                        path: '/configuration/service-settings/service/:serviceId/link',
                        name: 'link',
                        title: '业务路径编辑',
                        component: './app/Configuration/Service/Link',
                      },
                      {
                        component: './frame/Exception/404',
                      },
                    ],
                  },
                  {
                    component: './frame/Exception/404',
                  },
                ],
              },
              {
                path: '/configuration/equipment',
                name: 'equipment',
                title: '设备管理',
                access: 'hasUserPerm',
                component: '../layouts/PageLayout',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/configuration/equipment',
                    redirect: '/configuration/equipment/sensor',
                  },
                  {
                    path: '/configuration/equipment/sensor',
                    name: 'sensor',
                    title: '探针设备',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/configuration/equipment/sensor',
                        component: './app/Configuration/equipment/Sensor/List',
                      },
                      {
                        path: '/configuration/equipment/sensor/create',
                        name: 'create',
                        title: '新建探针',
                        component: './app/Configuration/equipment/Sensor/Create',
                      },
                    ],
                  },
                  {
                    path: '/configuration/equipment/subordinate-cms',
                    name: 'subordinateCMS',
                    title: '下级CMS',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/configuration/equipment/subordinate-cms',
                        component: './app/Configuration/equipment/SubordinateCMS/List',
                      },
                      {
                        path: '/configuration/equipment/subordinate-cms/create',
                        name: 'create',
                        title: '新建下级CMS',
                        component: './app/Configuration/equipment/SubordinateCMS/Create',
                      },
                    ],
                  },
                  {
                    path: '/configuration/equipment/superior-cms-setting',
                    name: 'superior-cms-setting',
                    title: '上级CMS',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/configuration/equipment/superior-cms-setting',
                        component: './app/Configuration/equipment/SuperiorCMSSetting',
                      },
                    ],
                  },
                  // {
                  //   path: '/configuration/equipment/sensor-upgrade',
                  //   name: 'sensorUpgrade',
                  //   title: '探针升级管理',
                  //   hideChildrenInMenu: true,
                  //   routes: [
                  //     {
                  //       path: '/configuration/equipment/sensor-upgrade',
                  //       component: './app/Configuration/equipment/SensorUpgradeList',
                  //     },
                  //   ],
                  // },
                  {
                    component: './frame/Exception/404',
                  },
                ],
              },
              {
                path: '/configuration/knowledge',
                name: 'knowledge',
                title: '知识库',
                component: '../layouts/PageLayout',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/configuration/knowledge',
                    redirect: '/configuration/knowledge/sa-knowledge/upload',
                  },
                  {
                    path: '/configuration/knowledge/sa-knowledge/upload',
                    name: 'sa.upload',
                    title: 'SA规则库',
                    component: './app/Configuration/SAKnowledge/Upload',
                  },
                  {
                    path: '/configuration/knowledge/geolocation/upload',
                    name: 'geolocaiton.upload',
                    title: '地区库',
                    component: './app/Configuration/Geolocation/Upload',
                  },
                  {
                    component: './frame/Exception/404',
                  },
                ],
              },
              {
                path: '/configuration/objects',
                name: 'objects',
                title: '对象',
                component: '../layouts/PageLayout',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/configuration/objects',
                    redirect: '/configuration/objects/sa-knowledge',
                  },
                  {
                    path: '/configuration/objects/sa-knowledge',
                    name: 'sa-knowledge',
                    title: '应用',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/configuration/objects/sa-knowledge',
                        component: './app/Configuration/SAKnowledge',
                      },
                      {
                        path: '/configuration/objects/sa-knowledge/application/create',
                        name: 'application.create',
                        title: '新建自定义应用',
                        component: './app/Configuration/SAKnowledge/CustomApplication/Create',
                      },
                      {
                        path: '/configuration/objects/sa-knowledge/application/:applicationId/update',
                        name: 'application.update',
                        title: '编辑自定义应用',
                        component: './app/Configuration/SAKnowledge/CustomApplication/Update',
                      },
                      {
                        path: '/configuration/objects/sa-knowledge/category/create',
                        name: 'category.create',
                        title: '新建分类',
                        component: './app/Configuration/SAKnowledge/CustomCategory/Create',
                      },
                      {
                        path: '/configuration/objects/sa-knowledge/category/:categoryId/update',
                        name: 'category.update',
                        title: '编辑分类',
                        component: './app/Configuration/SAKnowledge/CustomCategory/Update',
                      },
                      {
                        path: '/configuration/objects/sa-knowledge/subcategory/create',
                        name: 'subcategory.create',
                        title: '新建子分类',
                        component: './app/Configuration/SAKnowledge/CustomSubCategory/Create',
                      },
                      {
                        path: '/configuration/objects/sa-knowledge/subcategory/:subcategoryId/update',
                        name: 'subcategory.update',
                        title: '编辑子分类',
                        component: './app/Configuration/SAKnowledge/CustomSubCategory/Update',
                      },
                      {
                        component: './frame/Exception/404',
                      },
                    ],
                  },
                  {
                    path: '/configuration/objects/protocol',
                    name: 'protocols',
                    title: '应用层协议',
                    hideChildInMenu: true,
                    access: 'hasUserPerm',
                    routes: [
                      {
                        path: '/configuration/objects/protocol',
                        title: '列表',
                        accessMenuIgnore: true,
                        access: 'hasUserPerm',
                        component: './app/Configuration/Protocol/List',
                      },
                      {
                        component: './frame/Exception/404',
                        accessMenuIgnore: true,
                      },
                    ],
                  },
                  {
                    path: '/configuration/objects/geolocation',
                    name: 'geolocation',
                    title: '地区',
                    hideChildInMenu: true,
                    routes: [
                      {
                        path: '/configuration/objects/geolocation',
                        component: './app/Configuration/Geolocation',
                      },
                      {
                        path: '/configuration/objects/geolocation/update',
                        name: 'update',
                        title: '编辑',
                        component: './app/Configuration/Geolocation/Create',
                      },
                      {
                        path: '/configuration/objects/geolocation/create',
                        name: 'create',
                        title: '新建',
                        component: './app/Configuration/Geolocation/Create',
                      },
                      {
                        component: './frame/Exception/404',
                      },
                    ],
                  },
                  {
                    path: '/configuration/objects/hostgroup',
                    name: 'hostgroup',
                    title: 'IP地址组',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/configuration/objects/hostgroup',
                        component: './app/Configuration/IpAddressGroup',
                      },
                      {
                        path: '/configuration/objects/hostgroup/create',
                        name: 'create',
                        title: '新建IP地址组',
                        component: './app/Configuration/IpAddressGroup/Create',
                      },
                      {
                        path: '/configuration/objects/hostgroup/:hostgroupId/update',
                        name: 'update',
                        title: '编辑IP地址组',
                        component: './app/Configuration/IpAddressGroup/Update',
                      },
                      {
                        component: './frame/Exception/404',
                      },
                    ],
                  },
                  {
                    path: '/configuration/objects/alerts/rule',
                    name: 'alerts',
                    title: '告警',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/configuration/objects/alerts/rule',
                        component: './app/Configuration/Alerts/Rule',
                      },
                      {
                        path: '/configuration/objects/alerts/rule/create',
                        name: 'create',
                        title: '新建告警',
                        component: './app/Configuration/Alerts/Rule/Create',
                      },
                      {
                        path: '/configuration/objects/alerts/rule/:ruleId/update',
                        name: 'update',
                        title: '编辑告警',
                        component: './app/Configuration/Alerts/Rule/Update',
                      },
                      {
                        path: '/configuration/objects/alerts/rule/:ruleId/copy',
                        name: 'copy',
                        title: '复制告警',
                        component: './app/Configuration/Alerts/Rule/Copy',
                      },
                      {
                        component: './frame/Exception/404',
                      },
                    ],
                  },
                  {
                    path: '/configuration/objects/selfDefinedTime',
                    name: 'selfDefinedTime',
                    title: '自定义时间',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/configuration/objects/selfDefinedTime',
                        component: './app/Configuration/SelfDefinedTime',
                      },
                      {
                        path: '/configuration/objects/selfDefinedTime/create',
                        name: 'create',
                        title: '新建自定义时间',
                        component: './app/Configuration/SelfDefinedTime/Create',
                      },
                      {
                        path: '/configuration/objects/selfDefinedTime/:taskId/update',
                        name: 'update',
                        title: '编辑自定义时间',
                        component: './app/Configuration/SelfDefinedTime/Update',
                      },
                      {
                        component: './frame/Exception/404',
                      },
                    ],
                  },
                  {
                    path: '/configuration/objects/domain-allow-list',
                    name: 'domain-allow-list',
                    title: '域名白名单',
                    hideChildrenInMenu: true,
                    access: 'hasUserPerm',
                    routes: [
                      {
                        path: '/configuration/objects/domain-allow-list',
                        component: './app/Configuration/DomainAllowList'
                      },
                      {
                        path: '/configuration/objects/domain-allow-list/create',
                        name: 'create',
                        component: './app/Configuration/DomainAllowList/Create',
                        access: 'hasUserPerm',
                        title: '创建域名白名单',
                      },
                      {
                        path: '/configuration/objects/domain-allow-list/:id/update',
                        name: 'update',
                        component: './app/Configuration/DomainAllowList/Update',
                        access: 'hasUserPerm',
                        title: '更新白名单',
                      },
                    ]
                  },
                  {
                    component: './frame/Exception/404',
                  },
                ],
              },
              {
                path: '/configuration/netflow',
                name: 'netflow',
                title: '流量采集存储',
                component: '../layouts/PageLayout',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/configuration/netflow',
                    redirect: '/configuration/netflow/ingest/policy',
                  },
                  {
                    path: '/configuration/netflow/ingest/policy',
                    name: 'ingest-policy',
                    title: '捕获过滤规则',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/configuration/netflow/ingest/policy',
                        component: './app/Configuration/IngestPolicy',
                      },
                      {
                        path: '/configuration/netflow/ingest/policy/create',
                        name: 'create',
                        title: '新建捕获过滤规则',
                        component: './app/Configuration/IngestPolicy/Create',
                      },
                      {
                        path: '/configuration/netflow/ingest/policy/:policyId/update',
                        name: 'update',
                        title: '编辑捕获过滤规则',
                        component: './app/Configuration/IngestPolicy/Update',
                      },
                      {
                        component: './frame/Exception/404',
                      },
                    ],
                  },
                  {
                    path: '/configuration/netflow/ingest/network-ingest-policy',
                    name: 'ingest-policy.network',
                    title: '捕获过滤策略',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/configuration/netflow/ingest/network-ingest-policy',
                        component: './app/Configuration/Network/IngestPolicy',
                      },
                      {
                        path: '/configuration/netflow/ingest/network-ingest-policy/create',
                        name: 'create',
                        title: '新建捕获过滤策略',
                        component: './app/Configuration/Network/IngestPolicy/Create',
                      },
                      {
                        component: './frame/Exception/404',
                      },
                    ],
                  },
                  {
                    path: '/configuration/netflow/application-policy',
                    name: 'store-policy.application',
                    title: '存储过滤策略',
                    hideChildrenInMenu: true,
                    component: './app/Configuration/ApplicationPolicy',
                  },
                  // {
                  //   path: '/configuration/netflow/network-application-policy',
                  //   name: 'store-policy.application.network',
                  //   title: '存储过滤策略',
                  //   hideChildrenInMenu: true,
                  //   routes: [
                  //     {
                  //       path: '/configuration/netflow/network-application-policy',
                  //       component: './app/Configuration/Network/ApplicationPolicy',
                  //     },
                  //     {
                  //       path: '/configuration/netflow/network-application-policy/create',
                  //       name: 'create',
                  //       title: '新建存储过滤策略',
                  //       component: './app/Configuration/Network/ApplicationPolicy/Create',
                  //     },
                  //     {
                  //       component: './frame/Exception/404',
                  //     },
                  //   ],
                  // },
                  {
                    component: './frame/Exception/404',
                  },
                ],
              },
              {
                path: '/configuration/safety-analysis',
                name: 'safety-analysis',
                title: '安全分析',
                component: '../layouts/PageLayout',
                hideChildrenInMenu: true,
                access: 'hasUserPerm',
                routes: [
                  {
                    path: '/configuration/safety-analysis',
                    redirect: '/configuration/safety-analysis/suricata/rule',
                    accessMenuIgnore: true,
                  },
                  {
                    path: '/configuration/safety-analysis/suricata/rule',
                    name: 'suricata.rule',
                    title: '检测规则',
                    hideChildrenInMenu: true,
                    access: 'hasUserPerm',
                    routes: [
                      {
                        path: '/configuration/safety-analysis/suricata/rule',
                        title: '列表',
                        accessMenuIgnore: true,
                        access: 'hasUserPerm',
                        component: './app/Configuration/Suricata/Rule',
                      },
                      {
                        path: '/configuration/safety-analysis/suricata/rule/create',
                        name: 'create',
                        title: '新建规则',
                        access: 'hasUserPerm',
                        component: './app/Configuration/Suricata/Rule/Create',
                      },
                      {
                        path: '/configuration/safety-analysis/suricata/rule/:sid/update',
                        name: 'update',
                        title: '编辑规则',
                        access: 'hasUserPerm',
                        component: './app/Configuration/Suricata/Rule/Update',
                      },
                      {
                        path: '/configuration/safety-analysis/suricata/rule/classtype/create',
                        name: 'classtype.create',
                        title: '新建分类',
                        access: 'hasUserPerm',
                        component: './app/Configuration/Suricata/Classtype/Create',
                      },
                      {
                        path: '/configuration/safety-analysis/suricata/rule/classtype/:id/update',
                        name: 'classtype.update',
                        title: '编辑分类',
                        access: 'hasUserPerm',
                        component: './app/Configuration/Suricata/Classtype/Update',
                      },
                    ],
                  },
                  {
                    path: '/configuration/safety-analysis/threat-intelligence',
                    name: 'threat-intelligence',
                    title: '威胁情报',
                    hideChildrenInMenu: true,
                    component: './app/Configuration/ThreatInformation',
                  },
                  {
                    component: './frame/Exception/404',
                    accessMenuIgnore: true,
                  },
                ],
              },
              {
                path: '/configuration/transmit',
                name: 'transmit',
                title: '外发配置',
                component: '../layouts/PageLayout',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/configuration/transmit',
                    redirect: '/configuration/transmit/external-server',
                  },
                  // {
                  //   path: '/configuration/transmit/mail',
                  //   name: 'mail',
                  //   title: '邮件外发配置',
                  //   hideChildrenInMenu: true,
                  //   routes: [
                  //     {
                  //       path: '/configuration/transmit/mail',
                  //       component: './app/Configuration/Transmit/Mail',
                  //     },
                  //     {
                  //       path: '/configuration/transmit/mail/create',
                  //       name: 'create',
                  //       title: '新建邮件外发配置',
                  //       component: './app/Configuration/Transmit/Mail/Create',
                  //     },
                  //     {
                  //       path: '/configuration/transmit/mail/:id/update',
                  //       name: 'update',
                  //       title: '编辑邮件外发配置',
                  //       component: './app/Configuration/Transmit/Mail/Update',
                  //     },
                  //     {
                  //       component: './frame/Exception/404',
                  //     },
                  //   ],
                  // },
                  // {
                  //   path: '/configuration/transmit/syslog',
                  //   name: 'syslog',
                  //   title: 'Syslog外发配置',
                  //   hideChildrenInMenu: true,
                  //   routes: [
                  //     {
                  //       path: '/configuration/transmit/syslog',
                  //       component: './app/Configuration/Transmit/Syslog',
                  //     },
                  //     {
                  //       path: '/configuration/transmit/syslog/create',
                  //       name: 'create',
                  //       title: '新建Syslog外发配置',
                  //       component: './app/Configuration/Transmit/Syslog/Create',
                  //     },
                  //     {
                  //       path: '/configuration/transmit/syslog/:id/update',
                  //       name: 'update',
                  //       title: '编辑Syslog外发配置',
                  //       component: './app/Configuration/Transmit/Syslog/Update',
                  //     },
                  //     {
                  //       component: './frame/Exception/404',
                  //     },
                  //   ],
                  // },
                  {
                    path: '/configuration/transmit/external-server',
                    name: 'external-server',
                    title: '外发服务器',
                    hideChildrenInMenu: true,
                    component: '../layouts/PageLayout',
                    routes: [
                      {
                        path: '/configuration/transmit/external-server',
                        redirect: '/Configuration/transmit/external-server/mail',
                      },
                      {
                        path: '/configuration/transmit/external-server/mail',
                        name: 'mail',
                        title: '邮件外发',
                        routes: [
                          {
                            path: '/configuration/transmit/external-server/mail',
                            redirect: '/configuration/transmit/external-server/mail/list',
                          },
                          {
                            path: '/configuration/transmit/external-server/mail/list',
                            title: '列表',
                            component: './app/Configuration/ExternalServer/components/Mail',
                          },
                          {
                            path: '/configuration/transmit/external-server/mail/create',
                            title: '创建邮件外发',

                            component: './app/Configuration/ExternalServer/components/Mail/Create',
                          },
                          {
                            path: '/configuration/transmit/external-server/mail/:id/update',
                            title: '编辑邮件外发',
                            component: './app/Configuration/ExternalServer/components/Mail/Update',
                          },
                        ],
                      },
                      {
                        path: '/configuration/transmit/external-server/syslog',
                        name: 'syslog',
                        title: 'SYSLOG服务器',
                        routes: [
                          {
                            path: '/configuration/transmit/external-server/syslog',
                            redirect: '/configuration/transmit/external-server/syslog/list',
                          },
                          {
                            path: '/configuration/transmit/external-server/syslog/list',
                            title: '列表',
                            component: './app/Configuration/ExternalServer/components/Syslog',
                          },
                          {
                            path: '/configuration/transmit/external-server/syslog/create',
                            title: '创建SYSLOG外发',
                            component:
                              './app/Configuration/ExternalServer/components/Syslog/Create',
                          },
                          {
                            path: '/configuration/transmit/external-server/syslog/:id/update',
                            title: '编辑SYSLOG外发',
                            component:
                              './app/Configuration/ExternalServer/components/Syslog/Update',
                          },
                        ],
                      },
                      {
                        path: '/configuration/transmit/external-server/kafka',
                        name: 'kafka',
                        title: 'KAFKA服务器',
                        routes: [
                          {
                            path: '/configuration/transmit/external-server/kafka',
                            redirect: '/configuration/transmit/external-server/kafka/list',
                          },
                          {
                            path: '/configuration/transmit/external-server/kafka/list',
                            title: '列表',
                            component: './app/Configuration/ExternalServer/components/Kafka',
                          },
                          {
                            path: '/configuration/transmit/external-server/kafka/create',
                            title: '创建KAFKA服务器',
                            component: './app/Configuration/ExternalServer/components/Kafka/Create',
                          },
                          {
                            path: '/configuration/transmit/external-server/kafka/:id/update',
                            title: '编辑KAFKA服务器',
                            component: './app/Configuration/ExternalServer/components/Kafka/Update',
                          },
                        ],
                      },
                      {
                        path: '/configuration/transmit/external-server/zmq',
                        name: 'zmq',
                        title: 'ZMQ服务器',
                        routes: [
                          {
                            path: '/configuration/transmit/external-server/zmq',
                            redirect: '/configuration/transmit/external-server/zmq/list',
                          },
                          {
                            path: '/configuration/transmit/external-server/zmq/list',
                            title: '列表',
                            component: './app/Configuration/ExternalServer/components/Zmq',
                          },
                          {
                            path: '/configuration/transmit/external-server/zmq/create',
                            title: '创建ZMQ服务器',
                            component: './app/Configuration/ExternalServer/components/Zmq/Create',
                          },
                          {
                            path: '/configuration/transmit/external-server/zmq/:id/update',
                            title: '编辑ZMQ服务器',
                            component: './app/Configuration/ExternalServer/components/Zmq/Update',
                          },
                        ],
                      },
                      {
                        component: './frame/Exception/404',
                      },
                    ],
                  },
                  {
                    path: '/configuration/transmit/sendup-rules',
                    name: 'sendup-rules',
                    title: '外发规则',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/configuration/transmit/sendup-rules',
                        redirect: '/configuration/transmit/sendup-rules/list',
                      },
                      {
                        path: '/configuration/transmit/sendup-rules/list',
                        title: '列表',
                        accessMenuIgnore: true,
                        component: './app/Configuration/SendUpRules',
                      },
                      {
                        path: '/configuration/transmit/sendup-rules/create',
                        title: '创建外发规则',
                        name: 'create',
                        accessMenuIgnore: true,
                        component: './app/Configuration/SendUpRules/Create',
                      },
                      {
                        path: '/configuration/transmit/sendup-rules/:id/update',
                        title: '编辑外发规则',
                        name: 'update',
                        accessMenuIgnore: true,
                        component: './app/Configuration/SendUpRules/Update',
                      },
                      {
                        component: './frame/Exception/404',
                      },
                    ],
                  },
                  {
                    path: '/configuration/transmit/send-policy',
                    name: 'send-policy',
                    title: '外发策略',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/configuration/transmit/send-policy',
                        redirect: '/configuration/transmit/send-policy/list',
                      },
                      {
                        path: '/configuration/transmit/send-policy/list',
                        title: '列表',
                        accessMenuIgnore: true,
                        component: './app/Configuration/SendPolicy',
                      },
                      {
                        path: '/configuration/transmit/send-policy/create',
                        title: '创建外发规则',
                        name: 'create',
                        accessMenuIgnore: true,
                        component: './app/Configuration/SendPolicy/Create',
                      },
                      {
                        path: '/configuration/transmit/send-policy/:id/update',
                        title: '编辑外发规则',
                        name: 'update',
                        accessMenuIgnore: true,
                        component: './app/Configuration/SendPolicy/Update',
                      },
                      {
                        component: './frame/Exception/404',
                      },
                    ],
                  },
                  {
                    path: '/configuration/transmit/smtp',
                    name: 'smtp',
                    title: 'SMTP配置',
                    hideChildrenInMenu: true,
                    component: './app/Configuration/Transmit/SMTP',
                  },
                ],
              },
              // {
              //   path: '/configuration/bi',
              //   title: '自定义仪表盘',
              //   name: 'bi',
              //   access: 'hasUserPerm',
              //   component: './app/Bi/Config',
              // },
            ],
          },
        ],
      },

      {
        component: './frame/Exception/404',
      },
    ],
  },
];
