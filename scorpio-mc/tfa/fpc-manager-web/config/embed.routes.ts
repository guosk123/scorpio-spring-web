export default [
  {
    path: '/embed',
    component: '../layouts/SecurityLayout',
    hideChildrenInMenu: true,
    routes: [
      {
        path: '/embed',
        component: '../layouts/EmbedLayout',
        routes: [
          {
            path: '/embed/analysis/network/packet',
            name: 'jumpToPacket',
            title: '正在跳转到数据包',
            access: 'hasUserPerm',
            component: './app/appliance/Packet/JumpToPacket',
          },
          {
            path: '/embed/home',
            name: 'home',
            title: '仪表盘',
            icon: 'dashboard',
            access: 'hasUserPerm',
            component: './app/Home',
          },
          {
            path: '/embed/analysis/dashboard',
            name: 'analysis.dashboard',
            title: '仪表盘',
            icon: 'fund',
            access: 'hasUserPerm',
            routes: [
              {
                path: '/embed/analysis/dashboard/custom',
                name: 'bi',
                title: '自定义仪表盘',
                component: './app/Report/Dashboard',
              },
              {
                component: './frame/Exception/404',
              },
            ],
          },
          {
            path: '/embed/analysis/offline',
            name: 'analysis.offline',
            title: '离线文件分析',
            icon: 'filePpt',
            access: 'hasUserPerm',
            hideChildrenInMenu: true,
            routes: [
              {
                path: '/embed/analysis/offline/list',
                name: 'list',
                title: '离线文件列表',
                component: './app/analysis/OfflinePcapAnalysis/OfflineTaskTab',
              },
              {
                path: '/embed/analysis/offline/:pcapFileId',
                name: 'analysis',
                title: '文件分析',
                component: './app/analysis/OfflinePcapAnalysis/Layout',
                hideInMenu: true,
                routes: [
                  {
                    path: '/embed/analysis/offline/:pcapFileId',
                    redirect: '/embed/analysis/offline/:pcapFileId/dashboard',
                  },
                  {
                    path: '/embed/analysis/offline/:pcapFileId/dashboard',
                    name: 'dashboard',
                    title: '概览',
                    component: './app/analysis/OfflinePcapAnalysis/Dashboard',
                  },
                  {
                    path: '/embed/analysis/offline/:pcapFileId/flow',
                    name: 'flow',
                    title: '流量分析',
                    component: './app/analysis/components/PageLayoutWithFilter',
                    routes: [
                      {
                        path: '/embed/analysis/offline/:pcapFileId/flow',
                        redirect: '/embed/analysis/offline/:pcapFileId/flow/location',
                      },
                      {
                        path: '/embed/analysis/offline/:pcapFileId/flow/location',
                        name: 'location',
                        title: '地区',
                        component: './app/analysis/Flow/Location',
                      },
                      {
                        path: '/embed/analysis/offline/:pcapFileId/flow/application',
                        name: 'application',
                        title: '应用',
                        component: './app/analysis/Flow/Application',
                      },
                      {
                        path: '/embed/analysis/offline/:pcapFileId/flow/protocol',
                        name: 'protocol',
                        title: '应用层协议',
                        component: './app/analysis/Flow/Protocol',
                      },
                      {
                        path: '/embed/analysis/offline/:pcapFileId/flow/port',
                        name: 'port',
                        title: '端口',
                        component: './app/analysis/Flow/Port',
                      },
                      {
                        path: '/embed/analysis/offline/:pcapFileId/flow/hostgroup',
                        name: 'hostgroup',
                        title: 'IP地址组',
                        component: './app/analysis/Flow/HostGroup',
                      },
                      {
                        path: '/embed/analysis/offline/:pcapFileId/flow/mac',
                        name: 'mac',
                        title: 'MAC地址',
                        component: './app/analysis/Flow/Mac',
                      },
                      {
                        path: '/embed/analysis/offline/:pcapFileId/flow/ip',
                        name: 'ip',
                        title: 'IP地址',
                        component: './app/analysis/Flow/Ip',
                      },
                      {
                        path: '/embed/analysis/offline/:pcapFileId/flow/ip-conversation',
                        name: 'ip-conversation',
                        title: 'IP会话',
                        component: './app/analysis/Flow/IpConversation',
                      },
                      {
                        component: './frame/Exception/Embed404',
                      },
                    ],
                  },
                  {
                    path: '/embed/analysis/offline/:pcapFileId/payload',
                    name: 'payload',
                    title: '负载量',
                    component: './app/analysis/Payload',
                  },
                  {
                    path: '/embed/analysis/offline/:pcapFileId/performance',
                    name: 'performance',
                    title: '性能',
                    component: './app/analysis/Performance',
                  },
                  {
                    path: '/embed/analysis/offline/:pcapFileId/tcp/stats',
                    name: 'tcp.stats',
                    title: 'TCP指标',
                    component: './app/analysis/TCP_Stats',
                  },
                  {
                    path: '/embed/analysis/offline/:pcapFileId/ip-graph',
                    name: 'ip-graph',
                    title: '访问关系',
                    component: './app/analysis/Network/IpGraph',
                  },
                  {
                    path: '/embed/analysis/offline/:pcapFileId/tcp/retransmission',
                    name: 'tcp.retransmission',
                    title: '重传分析',
                    component: './app/analysis/TCP_Retransmission',
                  },
                  {
                    path: '/embed/analysis/offline/:pcapFileId/tcp/connection',
                    name: 'tcp.connection',
                    title: '建连分析',
                    component: '../layouts/PageLayout',
                    routes: [
                      {
                        path: '/embed/analysis/offline/:pcapFileId/tcp/connection',
                        redirect: '/embed/analysis/offline/:pcapFileId/tcp/connection/error',
                      },
                      {
                        path: '/embed/analysis/offline/:pcapFileId/tcp/connection/error',
                        name: 'error',
                        title: '建连失败查询',
                        component: './app/analysis/TCP_Connection/Error',
                      },
                      {
                        path: '/embed/analysis/offline/:pcapFileId/tcp/connection/long-connection',
                        name: 'long-connection',
                        title: '长连接分析',
                        routes: [
                          {
                            path: '/embed/analysis/offline/:pcapFileId/tcp/connection/long-connection',
                            component: './app/analysis/TCP_Connection/LongConnection',
                          },
                          {
                            path: '/embed/analysis/offline/:pcapFileId/tcp/connection/long-connection/setting',
                            name: 'setting',
                            title: '认定时间配置',
                            component: './app/analysis/TCP_Connection/LongConnectionSetting',
                          },
                          {
                            component: './frame/Exception/Embed404',
                          },
                        ],
                      },
                      {
                        component: './frame/Exception/Embed404',
                      },
                    ],
                  },
                  {
                    path: '/embed/analysis/offline/:pcapFileId/metadata',
                    name: 'metadata',
                    title: '应用层协议分析',
                    component: './app/appliance/Metadata/Analysis',
                  },
                  {
                    path: '/embed/analysis/offline/:pcapFileId/ip-graph',
                    name: 'ip-graph',
                    title: '访问关系',
                    component: './app/analysis/Network/IpGraph',
                  },
                  {
                    path: '/embed/analysis/offline/:pcapFileId/flow-record',
                    name: 'flow-record',
                    title: '会话详单',
                    component: './app/appliance/FlowRecord/Record',
                  },
                  {
                    path: '/embed/analysis/offline/:pcapFileId/packet',
                    name: 'packet',
                    title: '数据包',
                    routes: [
                      {
                        path: '/embed/analysis/offline/:pcapFileId/packet',
                        component: './app/appliance/Packet',
                      },
                      {
                        path: '/embed/analysis/offline/:pcapFileId/packet/analysis',
                        name: 'analysis',
                        title: '在线分析',
                        component: './app/appliance/Packet/Analysis',
                      },
                      {
                        component: './frame/Exception/404',
                      },
                    ],
                  },
                  {
                    component: './frame/Exception/Embed404',
                  },
                ],
              },
              {
                component: './frame/Exception/Embed404',
              },
            ],
          },
          {
            path: '/embed/analysis/performance',
            name: 'analysis.performance',
            title: '性能分析',
            icon: 'ThunderboltOutlined',
            access: 'hasUserPerm',
            routes: [
              {
                path: '/embed/analysis/performance/network/list',
                name: 'network.list',
                exact: true,
                title: '所有网络',
                wrappers: ['./app/analysis/Network/Layout'],
                component: './app/analysis/Network/List',
              },
              {
                path: '/embed/analysis/performance/network',
                name: 'network',
                wrappers: ['./app/analysis/Network/Layout'],
                title: '网络分析',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/embed/analysis/performance/network',
                    redirect: '/embed/analysis/performance/network/redirect',
                  },
                  {
                    path: '/embed/analysis/performance/network/redirect',
                    name: 'redirect',
                    title: '网络分析',
                    component: './app/analysis/Network/Redirect',
                  },
                  {
                    path: '/embed/analysis/performance/network/:networkId',
                    name: 'detail',
                    title: '详情',
                    component: './app/analysis/Network/Layout',
                    hideChildrenInMenu: true,
                    hideInMenu: true,
                    parentKeys: ['redirect'],
                    routes: [
                      {
                        path: '/embed/analysis/performance/network/:networkId',
                        redirect: '/analysis/network/:networkId/dashboard',
                      },
                      {
                        path: '/embed/analysis/performance/network/:networkId/dashboard',
                        name: 'dashboard',
                        title: '概览',
                        component: './app/analysis/Network/Dashboard',
                      },
                      {
                        path: '/embed/analysis/performance/network/:networkId/payload',
                        name: 'payload',
                        title: '负载量',
                        routes: [
                          {
                            path: '/embed/analysis/performance/network/:networkId/payload',
                            component: './app/analysis/Payload',
                          },
                          {
                            path: '/embed/analysis/performance/network/:networkId/payload/baseline',
                            name: 'baseline',
                            title: '负载量',
                            component: './app/analysis/Payload/Baseline',
                          },
                          {
                            component: './frame/Exception/404',
                          },
                        ],
                      },
                      {
                        path: '/embed/analysis/performance/network/:networkId/performance',
                        name: 'performance',
                        title: '性能',
                        routes: [
                          {
                            path: '/embed/analysis/performance/network/:networkId/performance',
                            component: './app/analysis/Performance',
                          },
                          {
                            path: '/embed/analysis/performance/network/:networkId/performance/setting',
                            name: 'setting',
                            title: '性能配置',
                            component: './app/analysis/Performance/Setting',
                          },
                          {
                            component: './frame/Exception/404',
                          },
                        ],
                      },
                      {
                        path: '/embed/analysis/performance/network/:networkId/tcp/stats',
                        name: 'tcp.stats',
                        title: 'TCP指标',
                        component: './app/analysis/TCP_Stats',
                      },
                      {
                        path: '/embed/analysis/performance/network/:networkId/alert',
                        name: 'alert',
                        title: '告警消息',
                        routes: [
                          {
                            path: '/embed/analysis/performance/network/:networkId/alert',
                            component: './app/configuration/Alerts/Message/List',
                          },
                          {
                            path: '/embed/analysis/performance/network/:networkId/alert/:alertId/message',
                            name: 'message',
                            title: '告警详情',
                            component: './app/configuration/Alerts/Message/Detail',
                          },
                          {
                            component: './frame/Exception/404',
                          },
                        ],
                      },
                      {
                        path: '/embed/analysis/performance/network/:networkId/flow',
                        name: 'flow',
                        title: '流量分析',
                        component: './app/analysis/components/PageLayoutWithFilter',
                        routes: [
                          {
                            path: '/embed/analysis/performance/network/:networkId/flow',
                            redirect:
                              '/embed/analysis/performance/network/:networkId/flow/location',
                          },
                          {
                            path: '/embed/analysis/performance/network/:networkId/flow/location',
                            name: 'location',
                            title: '地区',
                            component: './app/analysis/Flow/Location',
                          },
                          {
                            path: '/embed/analysis/performance/network/:networkId/flow/application',
                            name: 'application',
                            title: '应用',
                            component: './app/analysis/Flow/Application',
                          },
                          {
                            path: '/embed/analysis/performance/network/:networkId/flow/protocol',
                            name: 'protocol',
                            title: '应用层协议',
                            component: './app/analysis/Flow/Protocol',
                          },
                          {
                            path: '/embed/analysis/performance/network/:networkId/flow/port',
                            name: 'port',
                            title: '端口',
                            component: './app/analysis/Flow/Port',
                          },
                          {
                            path: '/embed/analysis/performance/network/:networkId/flow/hostgroup',
                            name: 'hostgroup',
                            title: 'IP地址组',
                            component: './app/analysis/Flow/HostGroup',
                          },
                          {
                            path: '/embed/analysis/performance/network/:networkId/flow/mac',
                            name: 'mac',
                            title: 'MAC地址',
                            component: './app/analysis/Flow/Mac',
                          },
                          {
                            path: '/embed/analysis/performance/network/:networkId/flow/ip',
                            name: 'ip',
                            title: 'IP地址',
                            component: './app/analysis/Flow/Ip',
                          },
                          {
                            path: '/embed/analysis/performance/network/:networkId/flow/ip-conversation',
                            name: 'ip-conversation',
                            title: 'IP会话',
                            component: './app/analysis/Flow/IpConversation',
                          },
                          {
                            component: './frame/Exception/404',
                          },
                        ],
                      },
                      {
                        path: '/embed/analysis/performance/network/:networkId/tcp/retransmission',
                        name: 'tcp.retransmission',
                        title: '重传分析',
                        component: '../layouts/PageLayout',
                        routes: [
                          {
                            path: '/embed/analysis/performance/network/:networkId/tcp/retransmission',
                            redirect:
                              '/embed/analysis/performance/network/:networkId/tcp/retransmission/retransmission-analysis',
                          },
                          {
                            path: '/embed/analysis/performance/network/:networkId/tcp/retransmission/retransmission-analysis',
                            name: 'analysis',
                            title: '重传分析',
                            component:
                              './app/analysis/TCP_Retransmission/TCPRetransmissionAnalysis',
                          },
                          {
                            path: '/embed/analysis/performance/network/:networkId/tcp/retransmission/retransmission-detail',
                            name: 'detail',
                            title: '重传分析详单',
                            component: './app/analysis/TCP_Retransmission',
                          },
                        ],
                      },
                      {
                        path: '/embed/analysis/performance/network/:networkId/tcp/connection',
                        name: 'tcp.connection',
                        title: '建连分析',
                        component: '../layouts/PageLayout',
                        routes: [
                          {
                            path: '/embed/analysis/performance/network/:networkId/tcp/connection',
                            redirect:
                              '/embed/analysis/performance/network/:networkId/tcp/connection/error-analysis',
                          },
                          {
                            path: '/embed/analysis/performance/network/:networkId/tcp/connection/error-analysis',
                            name: 'error',
                            title: '建连失败分析',
                            component: './app/analysis/TCP_Connection/ErrorAnalysis',
                          },
                          {
                            path: '/embed/analysis/performance/network/:networkId/tcp/connection/error',
                            name: 'error',
                            title: '建连失败详单',
                            component: './app/analysis/TCP_Connection/Error',
                          },
                          {
                            path: '/embed/analysis/performance/network/:networkId/tcp/connection/long-connection',
                            name: 'long-connection',
                            title: '长连接分析',
                            routes: [
                              {
                                path: '/embed/analysis/performance/network/:networkId/tcp/connection/long-connection',
                                component: './app/analysis/TCP_Connection/LongConnection',
                              },
                              {
                                path: '/embed/analysis/performance/network/:networkId/tcp/connection/long-connection/setting',
                                name: 'setting',
                                title: '认定时间配置',
                                component: './app/analysis/TCP_Connection/LongConnectionSetting',
                              },
                              {
                                component: './frame/Exception/404',
                              },
                            ],
                          },
                        ],
                      },
                    ],
                  },
                ],
              },
              {
                path: '/embed/analysis/performance/service',
                name: 'service',
                title: '业务分析',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/embed/analysis/performance/service',
                    redirect: '/embed/analysis/performance/service/list',
                  },
                  {
                    path: '/embed/analysis/performance/service/list',
                    name: 'list',
                    title: '业务看板',
                    wrappers: ['./app/analysis/Service/Layout'],
                    component: './app/analysis/Service/List',
                  },
                  {
                    path: '/embed/analysis/performance/service/:serviceId/:networkId',
                    name: 'analysis',
                    title: '详情',
                    component: './app/analysis/Service/Layout',
                    hideChildrenInMenu: true,
                    hideInMenu: true,
                    routes: [
                      {
                        path: '/embed/analysis/performance/service/:serviceId/:networkId',
                        redirect:
                          '/embed/analysis/performance/service/:serviceId/:networkId/dashboard',
                      },
                      {
                        path: '/embed/analysis/performance/service/:serviceId/:networkId/dashboard',
                        name: 'dashboard',
                        title: '概览',
                        component: './app/analysis/Service/Dashboard',
                      },
                      {
                        path: '/embed/analysis/performance/service/:serviceId/:networkId/payload',
                        name: 'payload',
                        title: '负载量',
                        routes: [
                          {
                            path: '/embed/analysis/performance/service/:serviceId/:networkId/payload',
                            component: './app/analysis/Payload',
                          },
                          {
                            path: '/embed/analysis/performance/service/:serviceId/:networkId/payload/baseline',
                            name: 'baseline',
                            title: '负载量',
                            component: './app/analysis/Payload/Baseline',
                          },
                          {
                            component: './frame/Exception/404',
                          },
                        ],
                      },
                      {
                        path: '/embed/analysis/performance/service/:serviceId/:networkId/performance',
                        name: 'performance',
                        title: '性能',
                        routes: [
                          {
                            path: '/embed/analysis/performance/service/:serviceId/:networkId/performance',
                            component: './app/analysis/Performance',
                          },
                          {
                            path: '/embed/analysis/performance/service/:serviceId/:networkId/performance/setting',
                            name: 'setting',
                            title: '性能配置',
                            component: './app/analysis/Performance/Setting',
                          },
                          {
                            component: './frame/Exception/404',
                          },
                        ],
                      },
                      {
                        path: '/embed/analysis/performance/service/:serviceId/:networkId/tcp/stats',
                        name: 'tcp.stats',
                        title: 'TCP指标',
                        component: './app/analysis/TCP_Stats',
                      },
                      {
                        path: '/embed/analysis/performance/service/:serviceId/:networkId/alert',
                        name: 'alert',
                        title: '告警消息',
                        routes: [
                          {
                            path: '/embed/analysis/performance/service/:serviceId/:networkId/alert',
                            component: './app/configuration/Alerts/Message/List',
                          },
                          {
                            path: '/embed/analysis/performance/service/:serviceId/:networkId/alert/:alertId/message',
                            name: 'message',
                            title: '告警详情',
                            component: './app/configuration/Alerts/Message/Detail',
                          },
                          {
                            component: './frame/Exception/404',
                          },
                        ],
                      },
                      {
                        path: '/embed/analysis/performance/service/:serviceId/:networkId/flow',
                        name: 'flow',
                        title: '流量分析',
                        component: './app/analysis/components/PageLayoutWithFilter',
                        routes: [
                          {
                            path: '/embed/analysis/performance/service/:serviceId/:networkId/flow',
                            redirect:
                              '/embed/analysis/performance/service/:serviceId/:networkId/flow/location',
                          },
                          {
                            path: '/embed/analysis/performance/service/:serviceId/:networkId/flow/location',
                            name: 'location',
                            title: '地区',
                            component: './app/analysis/Flow/Location',
                          },
                          {
                            path: '/embed/analysis/performance/service/:serviceId/:networkId/flow/application',
                            name: 'application',
                            title: '应用',
                            component: './app/analysis/Flow/Application',
                          },
                          {
                            path: '/embed/analysis/performance/service/:serviceId/:networkId/flow/protocol',
                            name: 'protocol',
                            title: '协议',
                            component: './app/analysis/Flow/Protocol',
                          },
                          {
                            path: '/embed/analysis/performance/service/:serviceId/:networkId/flow/port',
                            name: 'port',
                            title: '端口',
                            component: './app/analysis/Flow/Port',
                          },
                          {
                            path: '/embed/analysis/performance/service/:serviceId/:networkId/flow/hostgroup',
                            name: 'hostgroup',
                            title: 'IP地址组',
                            component: './app/analysis/Flow/HostGroup',
                          },
                          {
                            path: '/embed/analysis/performance/service/:serviceId/:networkId/flow/mac',
                            name: 'mac',
                            title: 'MAC地址',
                            component: './app/analysis/Flow/Mac',
                          },
                          {
                            path: '/embed/analysis/performance/service/:serviceId/:networkId/flow/ip',
                            name: 'ip',
                            title: 'IP地址',
                            component: './app/analysis/Flow/Ip',
                          },
                          {
                            path: '/embed/analysis/performance/service/:serviceId/:networkId/flow/ip-conversation',
                            name: 'ip-conversation',
                            title: 'IP会话',
                            component: './app/analysis/Flow/IpConversation',
                          },
                          {
                            component: './frame/Exception/404',
                          },
                        ],
                      },
                      {
                        path: '/embed/analysis/performance/service/:serviceId/:networkId/tcp/retransmission',
                        name: 'tcp.retransmission',
                        title: '重传分析',
                        component: '../layouts/PageLayout',
                        routes: [
                          {
                            path: '/embed/analysis/performance/service/:serviceId/:networkId/tcp/retransmission',
                            redirect:
                              '/embed/analysis/performance/service/:serviceId/:networkId/tcp/retransmission/retransmission-analysis',
                          },
                          {
                            path: '/embed/analysis/performance/service/:serviceId/:networkId/tcp/retransmission/retransmission-analysis',
                            name: 'analysis',
                            title: '重传分析',
                            component:
                              './app/analysis/TCP_Retransmission/TCPRetransmissionAnalysis',
                          },
                          {
                            path: '/embed/analysis/performance/service/:serviceId/:networkId/tcp/retransmission/retransmission-detail',
                            name: 'detail',
                            title: '重传分析详单',
                            component: './app/analysis/TCP_Retransmission',
                          },
                        ],
                      },
                      {
                        path: '/embed/analysis/performance/service/:serviceId/:networkId/tcp/connection',
                        name: 'tcp.connection',
                        title: '建连分析',
                        component: '../layouts/PageLayout',
                        routes: [
                          {
                            path: '/embed/analysis/performance/service/:serviceId/:networkId/tcp/connection',
                            redirect:
                              '/embed/analysis/performance/service/:serviceId/:networkId/tcp/connection/error-analysis',
                          },
                          {
                            path: '/embed/analysis/performance/service/:serviceId/:networkId/tcp/connection/error-analysis',
                            name: 'errorAnalysis',
                            title: '建连失败分析',
                            component: './app/analysis/TCP_Connection/ErrorAnalysis',
                          },
                          {
                            path: '/embed/analysis/performance/service/:serviceId/:networkId/tcp/connection/error',
                            name: 'error',
                            title: '建连失败详单',
                            component: './app/analysis/TCP_Connection/Error',
                          },
                          {
                            path: '/embed/analysis/performance/service/:serviceId/:networkId/tcp/connection/long-connection',
                            name: 'long-connection',
                            title: '长连接分析',
                            routes: [
                              {
                                path: '/embed/analysis/performance/service/:serviceId/:networkId/tcp/connection/long-connection',
                                component: './app/analysis/TCP_Connection/LongConnection',
                              },
                              {
                                path: '/embed/analysis/performance/service/:serviceId/:networkId/tcp/connection/long-connection/setting',
                                name: 'setting',
                                title: '认定时间配置',
                                component: './app/analysis/TCP_Connection/LongConnectionSetting',
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
                    ],
                  },
                  {
                    component: './frame/Exception/404',
                  },
                ],
              },
            ],
          },
          {
            path: '/embed/report',
            icon: 'FormOutlined',
            name: 'analysis.report',
            title: '报表',
            access: 'hasUserPerm',
            routes: [
              {
                path: '/embed/report',
                redirect: '/embed/report/custom',
              },
              {
                path: '/embed/report/custom',
                name: 'custom',
                title: '自定义报表',
                component: './app/Report/Custom',
              },
              {
                path: '/embed/report/widget',
                name: 'widget',
                title: '图表',
                component: './app/Report/Widget',
              },
              {
                path: '/embed/report/datasource',
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
            path: '/embed/analysis/security',
            name: 'analysis.security',
            title: '分析任务',
            icon: 'history',
            access: 'hasUserPerm',
            routes: [
              {
                path: '/embed/analysis/security',
                redirect: '/embed/analysis/security/scenario-task',
              },
              {
                path: '/embed/analysis/security/scenario-task',
                name: 'scenario-task',
                title: '场景分析',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/embed/analysis/security/scenario-task',
                    component: './app/appliance/ScenarioTask',
                  },
                  {
                    path: '/embed/analysis/security/scenario-task/create',
                    name: 'create',
                    title: '新建分析任务',
                    component: './app/appliance/ScenarioTask/Create',
                  },
                  {
                    path: '/embed/analysis/security/scenario-task/result',
                    name: 'result',
                    title: '分析任务结果',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/embed/analysis/security/scenario-task/result',
                        name: '',
                        title: '',
                        component: './app/appliance/ScenarioTask/Result',
                      },
                      {
                        path: '/embed/analysis/security/scenario-task/result/metadata-dns',
                        name: 'metadata-dns',
                        title: 'DNS详单',
                        component: './app/appliance/ScenarioTask/Result/DNSDetail',
                      },
                      {
                        path: '/embed/analysis/security/scenario-task/result/metadata-http',
                        name: 'metadata-http',
                        title: 'HTTP详单',
                        component: './app/appliance/ScenarioTask/Result/HttpDetail',
                      },
                      {
                        path: '/embed/analysis/security/scenario-task/result/metadata-ssl',
                        name: 'metadata-ssl',
                        title: 'SSL详单',
                        component: './app/appliance/ScenarioTask/Result/SSLDetail',
                      },
                      {
                        path: '/embed/analysis/security/scenario-task/result/metadata-ftp',
                        name: 'metadata-ftp',
                        title: 'FTP详单',
                        component: './app/appliance/ScenarioTask/Result/FTPDetail',
                      },
                      {
                        path: '/embed/analysis/security/scenario-task/result/metadata-mail',
                        name: 'metadata-mail',
                        title: 'MAIL详单',
                        component: './app/appliance/ScenarioTask/Result/MailDetail',
                      },
                      {
                        path: '/embed/analysis/security/scenario-task/result/metadata-telnet',
                        name: 'metadata-telnet',
                        title: 'TELNET详单',
                        component: './app/appliance/ScenarioTask/Result/TelnetDetail',
                      },
                      {
                        path: '/embed/analysis/security/scenario-task/result/flow-record',
                        name: 'flow-record',
                        title: '会话详单',
                        component: './app/appliance/FlowRecord/Slim',
                      },
                      {
                        path: '/embed/analysis/security/scenario-task/result/packet',
                        name: 'packet',
                        title: '数据包',
                        component: './app/appliance/Packet/Slim',
                      },
                      {
                        component: './frame/Exception/Embed404',
                      },
                    ],
                  },
                  {
                    component: './frame/Exception/Embed404',
                  },
                ],
              },
              {
                component: './frame/Exception/Embed404',
              },
            ],
          },
          {
            path: '/embed/analysis/trace/packet/analysis',
            name: 'analysis.trace.packet.analysis',
            title: '在线分析',
            component: './app/appliance/Packet/Analysis',
            hideInMenu: true,
          },
          {
            path: '/embed/analysis/trace',
            name: 'analysis.trace',
            title: '溯源分析',
            icon: 'SearchOutlined',
            access: 'hasUserPerm',
            component: './app/trace/Layout',
            routes: [
              {
                path: '/embed/analysis/trace/transmit-task',
                name: 'transmit-task',
                title: '流量溯源',
                component: './app/appliance/TransmitTask/Layout',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/embed/analysis/trace/transmit-task',
                    component: './app/appliance/TransmitTask',
                  },
                  {
                    path: '/embed/analysis/trace/transmit-task/create',
                    name: 'create',
                    title: '新建查询任务',
                    component: './app/appliance/TransmitTask/Create',
                  },
                  {
                    path: '/embed/analysis/trace/transmit-task/:taskId/update',
                    name: 'update',
                    title: '编辑查询任务',
                    component: './app/appliance/TransmitTask/Update',
                  },
                  {
                    path: '/embed/analysis/trace/transmit-task/:taskId/copy',
                    name: 'copy',
                    title: '复制查询任务',
                    component: './app/appliance/TransmitTask/Copy',
                  },
                  {
                    path: '/embed/analysis/trace/transmit-task/:taskId/analysis',
                    name: 'analysis',
                    title: '在线分析',
                    component: './app/appliance/TransmitTask/Analysis',
                  },
                  {
                    component: './frame/Exception/404',
                  },
                ],
              },

              {
                path: '/embed/analysis/trace/ip-graph',
                name: 'ip-graph',
                title: '访问关系',
                component: './app/analysis/Network/IpGraph',
              },
              {
                path: '/embed/analysis/trace/metadata/analysis',
                name: 'metadata.analysis',
                title: '应用层协议分析',
                component: './app/trace/Metadata/Analysis',
              },
              {
                path: '/embed/analysis/trace/metadata/record',
                name: 'metadata.record',
                title: '应用层协议详单',
                component: './app/trace/Metadata/Record',
              },
              {
                path: '/embed/analysis/trace/flow-record',
                name: 'flow-record',
                title: '会话详单',
                component: './app/security/RecordQuery/FlowRecord',
              },
              {
                path: '/embed/analysis/trace/packet',
                name: 'packet',
                title: '数据包',
                component: './app/appliance/Packet',
              },
            ],
          },
          {
            path: '/embed/configuration',
            name: 'configuration',
            icon: 'tool',
            title: '功能配置',
            access: 'hasUserPerm',
            routes: [
              {
                path: '/embed/configuration/network-netif',
                name: 'network-netif',
                title: '网络接口',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/embed/configuration/network-netif',
                    redirect: '/embed/configuration/network-netif/netif',
                  },
                  {
                    path: '/embed/configuration/network-netif/netif',
                    name: 'netif',
                    title: '接口',
                    component: './app/configuration/DeviceNetif',
                  },
                  {
                    path: '/embed/configuration/network-netif/network',
                    name: 'network',
                    title: '网络',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/embed/configuration/network-netif/network',
                        component: './app/configuration/Network',
                      },
                      {
                        path: '/embed/configuration/network-netif/network/create',
                        name: 'create',
                        title: '新建网络',
                        component: './app/configuration/Network/Create',
                      },
                      {
                        path: '/embed/configuration/network-netif/network/:networkId/update',
                        name: 'update',
                        title: '编辑网络',
                        component: './app/configuration/Network/Update',
                      },
                      {
                        component: './frame/Exception/Embed404',
                      },
                    ],
                  },
                  {
                    path: '/embed/configuration/network-netif/logical-subnet',
                    name: 'logical-subnet',
                    title: '逻辑子网',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/embed/configuration/network-netif/logical-subnet',
                        name: '',
                        title: '',
                        component: './app/configuration/LogicalSubnet',
                      },
                      {
                        path: '/embed/configuration/network-netif/logical-subnet/create',
                        name: 'create',
                        title: '新建逻辑子网',
                        component: './app/configuration/LogicalSubnet/Create',
                      },
                      {
                        path: '/embed/configuration/network-netif/logical-subnet/:subnetId/update',
                        name: 'update',
                        title: '编辑逻辑子网',
                        component: './app/configuration/LogicalSubnet/Update',
                      },
                      {
                        component: './frame/Exception/Embed404',
                      },
                    ],
                  },
                  {
                    component: './frame/Exception/Embed404',
                  },
                ],
              },
              {
                path: '/embed/configuration/service-settings',
                name: 'service-settings',
                title: '业务',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/embed/configuration/service-settings',
                    redirect: '/embed/configuration/service-settings/service',
                  },
                  {
                    path: '/embed/configuration/service-settings/service',
                    name: 'service',
                    title: '业务配置',
                    icon: 'inbox',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/embed/configuration/service-settings/service',
                        component: './app/configuration/Service',
                      },
                      {
                        path: '/embed/configuration/service-settings/service/create',
                        name: 'create',
                        title: '新建业务',
                        component: './app/configuration/Service/Create',
                      },
                      {
                        path: '/embed/configuration/service-settings/service/:serviceId/update',
                        name: 'update',
                        title: '编辑业务',
                        component: './app/configuration/Service/Update',
                      },
                      {
                        path: '/embed/configuration/service-settings/service/:serviceId/link',
                        name: 'link',
                        title: '业务路径编辑',
                        component: './app/configuration/Service/Link',
                      },
                      {
                        component: './frame/Exception/Embed404',
                      },
                    ],
                  },
                  {
                    component: './frame/Exception/Embed404',
                  },
                ],
              },
              {
                path: '/embed/configuration/netflow',
                name: 'netflow',
                title: '流量采集存储',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/embed/configuration/netflow',
                    redirect: '/embed/configuration/netflow/ingest/policy',
                  },
                  {
                    path: '/embed/configuration/netflow/ingest/policy',
                    name: 'ingest-policy',
                    title: '捕获过滤规则',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/embed/configuration/netflow/ingest/policy',
                        component: './app/configuration/IngestPolicy',
                      },
                      {
                        path: '/embed/configuration/netflow/ingest/policy/create',
                        name: 'create',
                        title: '新建捕获过滤规则',
                        component: './app/configuration/IngestPolicy/Create',
                      },
                      {
                        path: '/embed/configuration/netflow/ingest/policy/:policyId/update',
                        name: 'update',
                        title: '编辑捕获过滤规则',
                        component: './app/configuration/IngestPolicy/Update',
                      },
                      {
                        component: './frame/Exception/Embed404',
                      },
                    ],
                  },
                  {
                    path: '/embed/configuration/netflow/ingest/network-ingest-policy',
                    name: 'ingest-policy.network',
                    title: '捕获过滤策略',
                    component: './app/configuration/Network/IngestPolicy',
                  },
                  // {
                  //   path: '/embed/configuration/netflow/application-policy',
                  //   name: 'store-policy.application',
                  //   title: '存储过滤规则',
                  //   hideChildrenInMenu: true,
                  //   routes: [
                  //     {
                  //       path: '/embed/configuration/netflow/application-policy',
                  //       component: './app/configuration/ApplicationPolicy',
                  //     },
                  //     {
                  //       path: '/embed/configuration/netflow/application-policy/create',
                  //       name: 'create',
                  //       title: '新建存储过滤规则',
                  //       component: './app/configuration/ApplicationPolicy/Create',
                  //     },
                  //     {
                  //       path: '/embed/configuration/netflow/application-policy/:policyId/update',
                  //       name: 'update',
                  //       title: '编辑存储过滤规则',
                  //       component: './app/configuration/ApplicationPolicy/Update',
                  //     },
                  //     {
                  //       component: './frame/Exception/Embed404',
                  //     },
                  //   ],
                  // },
                  {
                    path: '/embed/configuration/netflow/forward-rule',
                    name: 'forward.rule',
                    title: '实时转发规则',
                    hideChildrenInMenu: true,
                    access: 'hasUserPerm',
                    routes: [
                      {
                        path: '/embed/configuration/netflow/forward-rule',
                        title: '列表',
                        access: 'hasUserPerm',
                        accessMenuIgnore: true,
                        component: './app/configuration/ForwardNetflow/Rule',
                      },
                      {
                        path: '/embed/configuration/netflow/forward-rule/create',
                        name: 'create',
                        title: '创建实时转发规则',
                        access: 'hasUserPerm',
                        component: './app/configuration/ForwardNetflow/Rule/Create',
                      },
                      {
                        path: '/embed/configuration/netflow/forward-rule/update/:ruleId',
                        name: 'update',
                        title: '编辑实时转发规则',
                        access: 'hasUserPerm',
                        component: './app/configuration/ForwardNetflow/Rule/Update',
                      },
                    ],
                  },
                  {
                    path: '/embed/configuration/netflow/forward-policy',
                    name: 'forward.policy',
                    title: '实时转发策略',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/embed/configuration/netflow/forward-policy',
                        title: '列表',
                        access: 'hasUserPerm',
                        accessMenuIgnore: true,
                        component: './app/configuration/ForwardNetflow/Policy',
                      },
                      {
                        path: '/embed/configuration/netflow/forward-policy/create',
                        name: 'create',
                        title: '创建实时转发策略',
                        access: 'hasUserPerm',
                        component: './app/configuration/ForwardNetflow/Policy/Create',
                      },
                      {
                        path: '/embed/configuration/netflow/forward-policy/update/:id',
                        name: 'update',
                        title: '编辑实时转发策略',
                        access: 'hasUserPerm',
                        component: './app/configuration/ForwardNetflow/Policy/Update',
                      },
                    ],
                  },
                  {
                    path: '/embed/configuration/netflow/network-application-policy',
                    name: 'store-policy.application.network',
                    title: '存储过滤策略',
                    component: './app/configuration/ApplicationPolicy',
                  },
                  {
                    path: '/embed/configuration/netflow/stogage/policy',
                    name: 'store-policy.encrypt-compress',
                    title: '加密压缩',
                    component: './app/configuration/StoragePolicy',
                  },
                  {
                    path: '/embed/configuration/netflow/stogage/space',
                    name: 'store-policy.space',
                    title: '空间管理',
                    component: './app/configuration/StorageSpace',
                  },
                  {
                    path: '/embed/configuration/netflow/metadata/collect-policy',
                    name: 'metadata.collect-policy',
                    title: '应用层协议采集',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/embed/configuration/netflow/metadata/collect-policy',
                        component: './app/configuration/MetadataCollectPolicy',
                      },
                      {
                        path: '/embed/configuration/netflow/metadata/collect-policy/create',
                        name: 'create',
                        title: '新建采集策略',
                        component: './app/configuration/MetadataCollectPolicy/Create',
                      },
                      {
                        path: '/embed/configuration/netflow/metadata/collect-policy/:policyId/update',
                        name: 'update',
                        title: '更新采集策略',
                        component: './app/configuration/MetadataCollectPolicy/Update',
                      },
                      {
                        component: './frame/Exception/Embed404',
                      },
                    ],
                  },
                  {
                    component: './frame/Exception/Embed404',
                  },
                ],
              },
              {
                path: '/embed/configuration/knowledge',
                name: 'knowledge',
                title: '知识库',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/embed/configuration/knowledge',
                    redirect: '/embed/configuration/knowledge/tls-decrypt-setting',
                  },
                  {
                    path: '/embed/configuration/knowledge/tls-decrypt-setting',
                    name: 'tls-decrypt-setting',
                    title: 'TLS私钥',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/embed/configuration/knowledge/tls-decrypt-setting',
                        component: './app/configuration/TlsDecryptSetting',
                      },
                      {
                        path: '/embed/configuration/knowledge/tls-decrypt-setting/create',
                        name: 'create',
                        title: '新建TLS私钥',
                        component: './app/configuration/TlsDecryptSetting/Create',
                      },
                      {
                        path: '/embed/configuration/knowledge/tls-decrypt-setting/:settingId/update',
                        name: 'update',
                        title: '编辑TLS私钥',
                        component: './app/configuration/TlsDecryptSetting/Update',
                      },
                      {
                        component: './frame/Exception/Embed404',
                      },
                    ],
                  },
                  {
                    path: '/embed/configuration/knowledge/sa-knowledge/upload',
                    name: 'sa.upload',
                    title: 'SA规则库',
                    component: './app/configuration/SAKnowledge/Upload',
                  },
                  {
                    path: '/embed/configuration/knowledge/geolocation/upload',
                    name: 'geolocaiton.upload',
                    title: '地区库',
                    component: './app/configuration/Geolocation/Upload',
                  },
                  {
                    component: './frame/Exception/Embed404',
                  },
                ],
              },
              {
                path: '/embed/configuration/objects',
                name: 'objects',
                title: '对象',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/embed/configuration/objects',
                    redirect: '/embed/configuration/objects/sa-knowledge',
                  },
                  {
                    path: '/embed/configuration/objects/sa-knowledge',
                    name: 'sa-knowledge',
                    title: '应用',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/embed/configuration/objects/sa-knowledge',
                        component: './app/configuration/SAKnowledge',
                      },
                      {
                        path: '/embed/configuration/objects/sa-knowledge/application/create',
                        name: 'application.create',
                        title: '新建自定义应用',
                        component: './app/configuration/SAKnowledge/CustomApplication/Create',
                      },
                      {
                        path: '/embed/configuration/objects/sa-knowledge/application/:applicationId/update',
                        name: 'application.update',
                        title: '编辑自定义应用',
                        component: './app/configuration/SAKnowledge/CustomApplication/Update',
                      },
                      {
                        path: '/embed/configuration/objects/sa-knowledge/category/create',
                        name: 'category.create',
                        title: '新建分类',
                        component: './app/configuration/SAKnowledge/CustomCategory/Create',
                      },
                      {
                        path: '/embed/configuration/objects/sa-knowledge/category/:categoryId/update',
                        name: 'category.update',
                        title: '编辑分类',
                        component: './app/configuration/SAKnowledge/CustomCategory/Update',
                      },
                      {
                        path: '/embed/configuration/objects/sa-knowledge/subcategory/create',
                        name: 'subcategory.create',
                        title: '新建子分类',
                        component: './app/configuration/SAKnowledge/CustomSubCategory/Create',
                      },
                      {
                        path: '/embed/configuration/objects/sa-knowledge/subcategory/:subcategoryId/update',
                        name: 'subcategory.update',
                        title: '编辑子分类',
                        component: './app/configuration/SAKnowledge/CustomSubCategory/Update',
                      },
                      {
                        component: './frame/Exception/Embed404',
                      },
                    ],
                  },
                  {
                    path: '/embed/configuration/objects/geolocation',
                    name: 'geolocation',
                    title: '地区',
                    component: './app/configuration/Geolocation',
                  },
                  {
                    path: '/embed/configuration/objects/hostgroup',
                    name: 'hostgroup',
                    title: 'IP地址组',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/embed/configuration/objects/hostgroup',
                        component: './app/configuration/IpAddressGroup',
                      },
                      {
                        path: '/embed/configuration/objects/hostgroup/create',
                        name: 'create',
                        title: '新建IP地址组',
                        component: './app/configuration/IpAddressGroup/Create',
                      },
                      {
                        path: '/embed/configuration/objects/hostgroup/:hostgroupId/update',
                        name: 'update',
                        title: '编辑IP地址组',
                        component: './app/configuration/IpAddressGroup/Update',
                      },
                      {
                        component: './frame/Exception/Embed404',
                      },
                    ],
                  },
                  {
                    path: '/embed/configuration/objects/standard-protocol',
                    name: 'standard-protocol',
                    title: '协议端口',
                    icon: 'control',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/embed/configuration/objects/standard-protocol',
                        component: './app/configuration/StandardProtocol',
                      },
                      {
                        path: '/embed/configuration/objects/standard-protocol/create',
                        name: 'create',
                        title: '新建协议端口',
                        component: './app/configuration/StandardProtocol/Create',
                      },
                      {
                        path: '/embed/configuration/objects/standard-protocol/:protocolId/update',
                        name: 'update',
                        title: '编辑协议端口',
                        component: './app/configuration/StandardProtocol/Update',
                      },
                      {
                        component: './frame/Exception/Embed404',
                      },
                    ],
                  },
                  {
                    path: '/embed/configuration/objects/alerts/rule',
                    name: 'alerts',
                    title: '告警',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/embed/configuration/objects/alerts/rule',
                        component: './app/configuration/Alerts/Rule',
                      },
                      {
                        path: '/embed/configuration/objects/alerts/rule/create',
                        name: 'create',
                        title: '新建告警',
                        component: './app/configuration/Alerts/Rule/Create',
                      },
                      {
                        path: '/embed/configuration/objects/alerts/rule/:ruleId/update',
                        name: 'update',
                        title: '编辑告警',
                        component: './app/configuration/Alerts/Rule/Update',
                      },
                      {
                        path: '/embed/configuration/objects/alerts/rule/:ruleId/copy',
                        name: 'copy',
                        title: '复制告警',
                        component: './app/configuration/Alerts/Rule/Copy',
                      },
                      {
                        component: './frame/Exception/Embed404',
                      },
                    ],
                  },
                  // 标签内嵌路由不体现在内嵌文档中
                  {
                    path: '/embed/configuration/objects/ip-label',
                    name: 'ip-label',
                    title: '标签',
                    hideChildrenInMenu: true,
                    access: 'hasUserPerm',
                    routes: [
                      {
                        path: '/embed/configuration/objects/ip-label',
                        component: './app/configuration/IpLabel',
                      },
                      {
                        path: '/embed/configuration/objects/ip-label/create',
                        name: 'create',
                        component: './app/configuration/IpLabel/Create',
                        access: 'hasUserPerm',
                        title: '创建标签',
                      },
                      {
                        path: '/embed/configuration/objects/ip-label/:labelId/update',
                        name: 'update',
                        component: './app/configuration/IpLabel/Update',
                        access: 'hasUserPerm',
                        title: '更新标签',
                      },
                    ],
                  },
                  {
                    component: './frame/Exception/Embed404',
                  },
                ],
              },
              {
                path: '/embed/configuration/safety-analysis',
                name: 'safety-analysis',
                title: '安全分析',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/embed/configuration/safety-analysis',
                    redirect: '/embed/configuration/safety-analysis/suricata/rule',
                  },
                  // {
                  //   path: '/embed/configuration/safety-analysis/abnormal-event/rule',
                  //   name: 'abnormal-event',
                  //   title: '异常事件',
                  //   hideChildrenInMenu: true,
                  //   routes: [
                  //     {
                  //       path: '/embed/configuration/safety-analysis/abnormal-event/rule',
                  //       component: './app/configuration/AbnormalEvent/Rule',
                  //     },
                  //     {
                  //       path: '/embed/configuration/safety-analysis/abnormal-event/rule/create',
                  //       name: 'create',
                  //       title: '新建异常事件',
                  //       component: './app/configuration/AbnormalEvent/Rule/Create',
                  //     },
                  //     {
                  //       path: '/embed/configuration/safety-analysis/abnormal-event/rule/:eventRuleId/update',
                  //       name: 'update',
                  //       title: '编辑异常事件',
                  //       component: './app/configuration/AbnormalEvent/Rule/Update',
                  //     },
                  //     {
                  //       component: './frame/Exception/Embed404',
                  //     },
                  //   ],
                  // },
                  {
                    path: '/embed/configuration/safety-analysis/scenario-task-template',
                    name: 'scenario-task-template',
                    title: '分析模板',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/embed/configuration/safety-analysis/scenario-task-template',
                        component: './app/appliance/ScenarioTask/Template',
                      },
                      {
                        path: '/embed/configuration/safety-analysis/scenario-task-template/create',
                        name: 'create',
                        title: '新建分析模板',
                        component: './app/appliance/ScenarioTask/Template/Create',
                      },
                      {
                        component: './frame/Exception/Embed404',
                      },
                    ],
                  },
                  {
                    path: '/configuration/safety-analysis/suricata/rule',
                    name: 'suricata.rule',
                    title: '检测规则',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/configuration/safety-analysis/suricata/rule',
                        component: './app/configuration/Suricata/Rule',
                      },
                      {
                        path: '/configuration/safety-analysis/suricata/rule/create',
                        name: 'create',
                        title: '新建规则',
                        component: './app/configuration/Suricata/Rule/Create',
                      },
                      {
                        path: '/configuration/safety-analysis/suricata/rule/:sid/update',
                        name: 'update',
                        title: '编辑规则',
                        component: './app/configuration/Suricata/Rule/Update',
                      },
                      {
                        path: '/configuration/safety-analysis/suricata/rule/classtype/create',
                        name: 'classtype.create',
                        title: '新建分类',
                        component: './app/configuration/Suricata/Classtype/Create',
                      },
                      {
                        path: '/configuration/safety-analysis/suricata/rule/classtype/:id/update',
                        name: 'classtype.update',
                        title: '编辑分类',
                        component: './app/configuration/Suricata/Classtype/Update',
                      },
                    ],
                  },
                  {
                    component: './frame/Exception/Embed404',
                  },
                ],
              },
              {
                path: '/embed/configuration/third-party',
                name: 'third-party',
                title: '第三方对接',
                hideChildrenInMenu: true,
                routes: [
                  {
                    path: '/embed/configuration/third-party',
                    redirect: '/embed/configuration/third-party/external-storage',
                  },
                  {
                    path: '/embed/configuration/third-party/external-storage',
                    name: 'external-storage',
                    title: '存储服务器',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/embed/configuration/third-party/external-storage',
                        component: './app/configuration/ExternalStorage',
                      },
                      {
                        path: '/embed/configuration/third-party/external-storage/create',
                        name: 'create',
                        title: '新建存储服务器',
                        component: './app/configuration/ExternalStorage/Create',
                      },
                      {
                        path: '/embed/configuration/third-party/external-storage/:id/update',
                        name: 'update',
                        title: '编辑存储服务器',
                        component: './app/configuration/ExternalStorage/Update',
                      },
                      {
                        component: './frame/Exception/404',
                      },
                    ],
                  },
                  {
                    path: '/embed/configuration/third-party/metadata/receiver-policy',
                    name: 'metadata.receiver-policy',
                    title: '应用层协议详单外发',
                    component: './app/configuration/MetadataReceiverPolicy',
                  },
                  {
                    path: '/embed/configuration/third-party/alert/syslog',
                    name: 'alert.syslog',
                    title: '告警外发',
                    component: './app/configuration/Alerts/Syslog',
                  },
                  {
                    component: './frame/Exception/Embed404',
                  },
                ],
              },
              {
                component: './frame/Exception/Embed404',
              },
            ],
          },
          {
            path: '/embed/analysis/netflow',
            name: 'analysis.netflow',
            title: 'netflow',
            icon: 'SwapOutlined',
            access: 'hasUserPerm',
            routes: [
              {
                path: '/embed/analysis/netflow/device/list',
                name: 'device',
                title: '所有设备',
                component: './app/netflow/Device',
              },
              {
                path: '/embed/analysis/netflow/device/redirect',
                name: 'analysis',
                title: '设备分析',
                component: './app/netflow/Redirect',
              },
              {
                path: '/embed/analysis/netflow/device/:deviceName/netif/:netifNo',
                name: 'analysis',
                wrappers: ['./app/netflow/layouts/FlowWrapper'],
                component: './app/netflow/layouts/FlowLayout',
                hideChildrenInMenu: true,
                hideInMenu: true,
                routes: [
                  {
                    path: '/embed/analysis/netflow/device/:deviceName/netif/:netifNo',
                    redirect: '/embed/analysis/netflow/device/:deviceName/netif/:netifNo/dashboard',
                  },
                  {
                    path: '/embed/analysis/netflow/device/:deviceName/netif/:netifNo/dashboard',
                    name: 'dashboard',
                    title: '概览',
                    component: './app/netflow/Dashboard',
                  },
                  {
                    path: '/embed/analysis/netflow/device/:deviceName/netif/:netifNo/flow',
                    name: 'flow',
                    title: '流量分析',
                    component: './app/netflow/Flow',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/embed/analysis/netflow/device/:deviceName/netif/:netifNo/flow',
                        redirect:
                          '/embed/analysis/netflow/device/:deviceName/netif/:netifNo/flow/ip',
                      },
                      {
                        path: '/embed/analysis/netflow/device/:deviceName/netif/:netifNo/flow/ip',
                        name: 'ip',
                        title: 'IP',
                        component: './app/netflow/Flow/Ip',
                      },
                      {
                        path: '/embed/analysis/netflow/device/:deviceName/netif/:netifNo/flow/senderip',
                        name: 'senderip',
                        title: '发送IP',
                        component: './app/netflow/Flow/SenderIp',
                      },
                      {
                        path: '/embed/analysis/netflow/device/:deviceName/netif/:netifNo/flow/receiverip',
                        name: 'receiverip',
                        title: '接收IP',
                        component: './app/netflow/Flow/ReceiverIp',
                      },
                      {
                        path: '/embed/analysis/netflow/device/:deviceName/netif/:netifNo/flow/port',
                        name: 'port',
                        title: '协议端口',
                        component: './app/netflow/Flow/Port',
                      },
                      {
                        path: '/embed/analysis/netflow/device/:deviceName/netif/:netifNo/flow/ip-conversation',
                        name: 'ip-conversation',
                        title: '会话',
                        component: './app/netflow/Flow/IpConversation',
                      },
                    ],
                  },
                  {
                    path: '/embed/analysis/netflow/device/:deviceName/netif/:netifNo/flow-record',
                    name: 'flow-record',
                    title: '会话详单',
                    component: './app/netflow/FlowRecord',
                  },
                ],
              },
              {
                path: '/embed/analysis/netflow/device/:deviceName',
                name: 'analysis',
                wrappers: ['./app/netflow/layouts/FlowWrapper'],
                component: './app/netflow/layouts/FlowLayout',
                hideChildrenInMenu: true,
                hideInMenu: true,
                routes: [
                  // 接口情况
                  // 设备情况
                  {
                    path: '/embed/analysis/netflow/device/:deviceName',
                    redirect: '/embed/analysis/netflow/device/:deviceName/dashboard',
                  },
                  {
                    path: '/embed/analysis/netflow/device/:deviceName/dashboard',
                    name: 'dashboard',
                    title: '概览',
                    component: './app/netflow/Dashboard',
                  },
                  {
                    path: '/embed/analysis/netflow/device/:deviceName/flow',
                    name: 'flow',
                    title: '流量分析',
                    component: './app/netflow/Flow',
                    hideChildrenInMenu: true,
                    routes: [
                      {
                        path: '/embed/analysis/netflow/device/:deviceName/flow',
                        redirect: '/embed/analysis/netflow/device/:deviceName/flow/ip',
                      },
                      {
                        path: '/embed/analysis/netflow/device/:deviceName/flow/ip',
                        name: 'ip',
                        title: 'IP',
                        component: './app/netflow/Flow/Ip',
                      },
                      {
                        path: '/embed/analysis/netflow/device/:deviceName/flow/senderip',
                        name: 'senderip',
                        title: '发送IP',
                        component: './app/netflow/Flow/SenderIp',
                      },
                      {
                        path: '/embed/analysis/netflow/device/:deviceName/flow/receiverip',
                        name: 'receiverip',
                        title: '接收IP',
                        component: './app/netflow/Flow/ReceiverIp',
                      },
                      {
                        path: '/embed/analysis/netflow/device/:deviceName/flow/port',
                        name: 'port',
                        title: '协议端口',
                        component: './app/netflow/Flow/Port',
                      },
                      {
                        path: '/embed/analysis/netflow/device/:deviceName/flow/ip-conversation',
                        name: 'ip-conversation',
                        title: '会话',
                        component: './app/netflow/Flow/IpConversation',
                      },
                    ],
                  },
                  {
                    path: '/embed/analysis/netflow/device/:deviceName/flow-record',
                    name: 'flow-record',
                    title: '会话详单',
                    component: './app/netflow/FlowRecord',
                  },
                ],
              },
            ],
          },
          {
            component: './frame/Exception/Embed404',
          },
        ],
      },
    ],
  },
];
