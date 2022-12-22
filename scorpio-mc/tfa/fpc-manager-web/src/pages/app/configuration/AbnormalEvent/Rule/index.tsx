// import { getTablePaginationDefaultSettings, proTableSerchConfig } from '@/common/app';
// import { API_BASE_URL, API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
// import type { IAjaxResponseFactory, IPageFactory, IProTableData } from '@/common/typings';
// import Import from '@/components/Import';
// import type { ConnectState } from '@/models/connect';
// import ajax from '@/utils/frame/ajax';
// import { getLinkUrl, ipV4Regex } from '@/utils/utils';
// import { ExportOutlined, PlusOutlined, UploadOutlined } from '@ant-design/icons';
// import type { ActionType, ProColumns } from '@ant-design/pro-table';
// import ProTable from '@ant-design/pro-table';
// import { Button, Dropdown, Menu, Popconfirm } from 'antd';
// import { stringify } from 'querystring';
// import { useRef } from 'react';
// import type { Dispatch } from 'umi';
// import { connect, history } from 'umi';
// import type { IAbnormalEventRule } from '../typings';
// import { EABNORMAL_EVENT } from '../typings';
// import {
//   ABNORMAL_EVENT_TYPE_ENUM,
//   EAbnormalEventRuleSource,
//   EAbnormalEventRuleStatus,
// } from '../typings';
// import { customEventRuleDesc, intelligenceRuleDesc } from './components/ImportRule';

// interface IAbnormalEventRuleListProps {
//   dispatch: Dispatch;
//   importAbnormalEventLoading: boolean | undefined;
//   importThreatIntelligenceLoading: boolean | undefined;
// }
// const AbnormalEventRuleList = ({
//   dispatch,
//   importAbnormalEventLoading,
//   importThreatIntelligenceLoading,
// }: IAbnormalEventRuleListProps) => {
//   const actionRef = useRef<ActionType>();
//   const handleDelete = (id: string) => {
//     dispatch({
//       type: 'abnormalEventModel/deleteAbnormalEventRule',
//       payload: { id },
//     }).then(() => {
//       actionRef.current?.reload();
//     });
//   };

//   const handleExport = () => {
//     const url = `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/analysis/abnormal-event-rules/as-export`;
//     window.location.href = url;
//   };

//   const handleEnableAbnormalEventRule = (id: string) => {
//     (
//       dispatch({
//         type: 'abnormalEventModel/enableAbnormalEventRule',
//         payload: { id },
//       }) as unknown as Promise<any>
//     ).then((success: boolean) => {
//       if (success) {
//         actionRef.current?.reload();
//       }
//     });
//   };

//   const handleDisableAbnormalEventRule = (id: string) => {
//     (
//       dispatch({
//         type: 'abnormalEventModel/disableAbnormalEventRule',
//         payload: { id },
//       }) as unknown as Promise<any>
//     ).then((success: boolean) => {
//       if (success) {
//         actionRef.current?.reload();
//       }
//     });
//   };

//   const columns: ProColumns<IAbnormalEventRule>[] = [
//     {
//       title: '来源',
//       dataIndex: 'source',
//       align: 'center',
//       width: 180,
//       valueType: 'select',
//       formItemProps: {
//         rules: [],
//       },
//       valueEnum: {
//         [EAbnormalEventRuleSource.Default]: {
//           text: '系统内置',
//         },
//         [EAbnormalEventRuleSource.Custom]: {
//           text: '自定义',
//         },
//       },
//     },
//     {
//       title: '分类',
//       dataIndex: 'type',
//       align: 'center',
//       valueType: 'select',
//       valueEnum: Object.keys(ABNORMAL_EVENT_TYPE_ENUM).reduce((prev, current) => {
//         return {
//           ...prev,
//           [current]: { text: ABNORMAL_EVENT_TYPE_ENUM[current]?.label },
//         };
//       }, {}),
//     },

//     {
//       title: '内容',
//       dataIndex: 'content',
//       align: 'center',
//       ellipsis: true,
//       render: (text, record) => {
//         if (
//           record.type === EABNORMAL_EVENT.DHCP_FAKE_SERVER &&
//           ipV4Regex.test(record?.content?.split(',')[0])
//         ) {
//           return `白名单IP: ${record?.content}`;
//         }
//         return record?.typeText;
//       },
//     },
//     {
//       title: '启用状态',
//       dataIndex: 'status',
//       align: 'center',
//       width: 120,
//       valueType: 'select',
//       formItemProps: {
//         rules: [],
//       },
//       valueEnum: {
//         [EAbnormalEventRuleStatus.Open]: {
//           text: '启用',
//           status: 'Success',
//         },
//         [EAbnormalEventRuleStatus.Closed]: {
//           text: '禁用',
//           status: 'Default',
//         },
//       },
//     },
//     {
//       title: '更新时间',
//       key: 'timestamp',
//       dataIndex: 'timestamp',
//       width: 180,
//       align: 'center',
//       search: false,
//       valueType: 'dateTime',
//     },
//     {
//       title: '操作',
//       key: 'option',
//       align: 'center',
//       width: 300,
//       valueType: 'option',
//       render: (_, record) => [
//         record.source === EAbnormalEventRuleSource.Custom ||
//         record.type === EABNORMAL_EVENT.DHCP_FAKE_SERVER ? (
//           // 自定义的可编辑
//           <Button
//             key="edit"
//             type="link"
//             size="small"
//             onClick={() =>
//               history.push(
//                 getLinkUrl(
//                   `/configuration/safety-analysis/abnormal-event/rule/${record.id}/update`,
//                 ),
//               )
//             }
//           >
//             编辑
//           </Button>
//         ) : (
//           <Button disabled key="edit-disabled" type="link" size="small">
//             编辑
//           </Button>
//         ),
//         record.source === EAbnormalEventRuleSource.Custom ? (
//           <Popconfirm key="delete" title="确定删除吗？" onConfirm={() => handleDelete(record.id)}>
//             <Button type="link" size="small">
//               删除
//             </Button>
//           </Popconfirm>
//         ) : (
//           <Button disabled key="del-disabled" type="link" size="small">
//             删除
//           </Button>
//         ),
//         record.status === EAbnormalEventRuleStatus.Open ? (
//           <Popconfirm
//             key="enable"
//             title="确定停用吗？"
//             onConfirm={() => handleDisableAbnormalEventRule(record.id)}
//           >
//             <Button type="link" size="small">
//               停用
//             </Button>
//           </Popconfirm>
//         ) : (
//           <Button disabled key="enable-disabled" type="link" size="small">
//             停用
//           </Button>
//         ),
//         record.status === EAbnormalEventRuleStatus.Closed ? (
//           <Popconfirm
//             key="disable"
//             title="确定启用吗？"
//             onConfirm={() => handleEnableAbnormalEventRule(record.id)}
//           >
//             <Button type="link" size="small">
//               启用
//             </Button>
//           </Popconfirm>
//         ) : (
//           <Button disabled key="disable-disabled" type="link" size="small">
//             启用
//           </Button>
//         ),
//       ],
//     },
//   ];

//   return (
//     <ProTable<IAbnormalEventRule>
//       bordered
//       size="small"
//       columns={columns}
//       request={async (params = {}) => {
//         const { current, pageSize, ...rest } = params;
//         const newParams = { pageSize, page: current! - 1, ...rest };
//         const { success, result } = (await ajax(
//           `${API_VERSION_PRODUCT_V1}/analysis/abnormal-event-rules?${stringify(newParams)}`,
//         )) as IAjaxResponseFactory<IPageFactory<IAbnormalEventRule>>;
//         if (!success) {
//           return {
//             data: [],
//             success,
//           };
//         }

//         return {
//           data: result.content,
//           success,
//           page: result.number,
//           total: result.totalElements,
//         } as IProTableData<IAbnormalEventRule[]>;
//       }}
//       rowKey="id"
//       search={{
//         ...proTableSerchConfig,
//         optionRender: (searchConfig, formProps, dom) => [
//           ...dom.reverse(),
//           <Dropdown
//             key="import"
//             overlay={
//               <Menu>
//                 <Menu.Item>
//                   <Import
//                     loading={importAbnormalEventLoading}
//                     modalTitle="自定义异常事件导入"
//                     buttonText="自定义异常事件"
//                     buttonProps={{ type: 'text', size: 'small', block: true }}
//                     importFunc="abnormalEventModel/importAbnormalEventRule"
//                     tempDownloadUrl="/analysis/abnormal-event-rules/as-template"
//                     importSuccessCallback={() => actionRef.current?.reload()}
//                     description={
//                       <>
//                         <p style={{ marginTop: 10, color: '#ff4d4f' }}>
//                           新导入的规则将会全量替换原有规则
//                         </p>
//                         {customEventRuleDesc}
//                       </>
//                     }
//                   />
//                 </Menu.Item>
//                 <Menu.Item>
//                   <Import
//                     loading={importThreatIntelligenceLoading}
//                     modalTitle="威胁情报导入"
//                     buttonText="威胁情报"
//                     buttonProps={{ type: 'text', size: 'small', block: true }}
//                     importFunc="abnormalEventModel/importThreatIntelligenceRule"
//                     tempDownloadUrl="/analysis/threat-intelligences/as-template"
//                     importSuccessCallback={() => {}}
//                     // 这里导入的是原来的精准情报，所以 custom=false
//                     extraData={{ custom: false }}
//                     description={
//                       <>
//                         <p style={{ marginTop: 10, color: '#ff4d4f' }}>
//                           新导入的规则将会全量替换原有规则
//                         </p>
//                         {intelligenceRuleDesc}
//                       </>
//                     }
//                   />
//                 </Menu.Item>
//               </Menu>
//             }
//           >
//             <Button>
//               <UploadOutlined /> 导入
//             </Button>
//           </Dropdown>,
//           <Button key="export" icon={<ExportOutlined />} onClick={handleExport}>
//             导出
//           </Button>,
//           <Button
//             key="create"
//             icon={<PlusOutlined />}
//             type="primary"
//             onClick={() =>
//               history.push(getLinkUrl('/configuration/safety-analysis/abnormal-event/rule/create'))
//             }
//           >
//             新建
//           </Button>,
//         ],
//       }}
//       form={{
//         ignoreRules: false,
//       }}
//       actionRef={actionRef}
//       dateFormatter="string"
//       toolBarRender={false}
//       pagination={getTablePaginationDefaultSettings()}
//     />
//   );
// };

// export default connect(({ loading: { effects } }: ConnectState) => ({
//   importAbnormalEventLoading: effects['abnormalEventModel/importAbnormalEventRule'],
//   importThreatIntelligenceLoading: effects['abnormalEventModel/importThreatIntelligenceRule'],
// }))(AbnormalEventRuleList);
