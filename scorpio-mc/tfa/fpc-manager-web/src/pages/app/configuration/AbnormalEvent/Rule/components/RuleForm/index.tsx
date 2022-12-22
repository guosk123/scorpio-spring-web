// import type { ConnectState } from '@/models/connect';
// import { createConfirmModal, hostRegex, ipV4Regex, updateConfirmModal } from '@/utils/utils';
// import { Button, Card, Form, Input, Select, Switch } from 'antd';
// import type { Rule } from 'antd/lib/form';
// import React, { useState } from 'react';
// import type { Dispatch } from 'umi';
// import { connect, history } from 'umi';
// import type { AbnormalEventType, IAbnormalEventRule } from '../../../typings';
// import {
//   ABNORMAL_EVENT_TYPE_ENUM,
//   EAbnormalEventRuleStatus,
//   EABNORMAL_EVENT,
// } from '../../../typings';

// /** 私有规则最小编号 */
export const PRIVATE_RULE_NUMBER_MIN = 200;

// const FormItem = Form.Item;
// const { TextArea } = Input;

// const formItemLayout = {
//   labelCol: {
//     xs: { span: 24 },
//     sm: { span: 3 },
//   },
//   wrapperCol: {
//     xs: { span: 24 },
//     sm: { span: 20 },
//     md: { span: 18 },
//   },
// };

// const submitFormLayout = {
//   wrapperCol: {
//     xs: { span: 24, offset: 0 },
//     sm: { span: 12, offset: 3 },
//   },
// };

// const getContentPlaceholder = (type: AbnormalEventType) => {
//   return ABNORMAL_EVENT_TYPE_ENUM[+type]?.placeholder || '请填写事件内容';
// };

// interface AbnormalEventRuleFormProps {
//   submitting: boolean | undefined;
//   dispatch: Dispatch;
//   detail?: IAbnormalEventRule;
// }
// const AbnormalEventRuleForm: React.FC<AbnormalEventRuleFormProps> = (props) => {
//   const { detail = {} as IAbnormalEventRule, submitting, dispatch } = props;
//   const [form] = Form.useForm();
//   const [contentPlaceholder, setContentPlaceholder] = useState(() =>
//     getContentPlaceholder(detail.type),
//   );

//   const handleGoBack = () => {
//     history.goBack();
//   };

//   const handleReset = () => {
//     form.resetFields();
//   };

//   const handleCreate = (values: IAbnormalEventRule) => {
//     createConfirmModal({
//       dispatchType: 'abnormalEventModel/createAbnormalEventRule',
//       values,
//       onOk: handleGoBack,
//       onCancel: handleReset,
//       dispatch,
//     });
//   };

//   const handleUpdate = (values: IAbnormalEventRule) => {
//     updateConfirmModal({
//       dispatchType: 'abnormalEventModel/updateAbnormalEventRule',
//       values,
//       onOk: handleGoBack,
//       dispatch,
//       onCancel: () => {},
//     });
//   };

//   const handleFinish = (values: Record<string, any>) => {
//     const { id, status, ...rest } = values;
//     const data = {
//       id,
//       ...rest,
//       status: status ? EAbnormalEventRuleStatus.Open : EAbnormalEventRuleStatus.Closed,
//     } as IAbnormalEventRule;

//     if (id) {
//       handleUpdate(data);
//     } else {
//       handleCreate(data);
//     }
//   };

//   const getContentRule = () => {
//     const tempType = form.getFieldValue(['type']) || detail.type;
//     const placeholder = getContentPlaceholder(tempType);

//     let contentRule: Rule[] = [
//       {
//         required: true,
//         whitespace: false,
//         message: placeholder,
//       },
//       { max: 255, message: '最多可输入255个字符' },
//     ];

//     if (tempType === EABNORMAL_EVENT.DHCP_FAKE_SERVER) {
//       contentRule = [
//         {
//           whitespace: false,
//         },
//         {
//           validator: async (_rule: any, value: string) => {
//             let isPass = true;
//             if (value) {
//               const splitArr = value.split(',');
//               for (let index = 0; index < splitArr.length; index += 1) {
//                 const element = splitArr[index];
//                 // 检查是否为空
//                 if (!element) {
//                   isPass = false;
//                   throw new Error(`第${index + 1}个不能为空`);
//                   break;
//                 }
//                 // 检查是否为正确的 IPv4
//                 if (!ipV4Regex.test(element)) {
//                   isPass = false;
//                   throw new Error(`${element}不是正确的 IPv4`);
//                   break;
//                 }
//               }
//               // 检查是否为在10个以内
//               if (isPass && splitArr.length > 10) {
//                 isPass = false;
//                 throw new Error(`最多支持10个 IPv4`);
//               }
//             }

//             if (!isPass) {
//               throw new Error(placeholder);
//             }
//           },
//         },
//       ];
//     } else if (
//       tempType === EABNORMAL_EVENT.VIOLATION_DOMAIN_NAME ||
//       tempType === EABNORMAL_EVENT.SUSPICIOUS_DOMAIN_NAME
//     ) {
//       contentRule.push({
//         validator: async (_rule: any, value: string) => {
//           if (!value || !hostRegex.test(value)) {
//             throw new Error(placeholder);
//           }
//         },
//       });
//     } else if (
//       tempType === EABNORMAL_EVENT.VIOLATION_IP ||
//       tempType === EABNORMAL_EVENT.SUSPICIOUS_IP
//     ) {
//       contentRule.push({
//         validator: async (_rule: any, value: string) => {
//           if (!value || !ipV4Regex.test(value)) {
//             throw new Error(placeholder);
//           }
//         },
//       });
//     }
//     return contentRule;
//   };

//   const handleTypeChange = () => {
//     const tmpType = form.getFieldValue(['type']) || detail.type;
//     // 清空原有内容
//     form.setFieldsValue({
//       content: undefined,
//     });
//     // 更新内容提示
//     setContentPlaceholder(getContentPlaceholder(tmpType));
//   };

//   return (
//     <Card bordered={false}>
//       <Form
//         form={form}
//         initialValues={{
//           ...detail,
//           status: detail.status === EAbnormalEventRuleStatus.Open,
//         }}
//         onFinish={handleFinish}
//         scrollToFirstError
//       >
//         <FormItem {...formItemLayout} label="ID" name="id" hidden>
//           <Input placeholder="事件规则ID" />
//         </FormItem>
//         <FormItem
//           {...formItemLayout}
//           label="分类"
//           name="type"
//           rules={[
//             {
//               required: true,
//               message: '请选择分类',
//             },
//           ]}
//         >
//           <Select
//             placeholder="请选择分类"
//             onChange={handleTypeChange}
//             disabled={detail.type === EABNORMAL_EVENT.DHCP_FAKE_SERVER}
//           >
//             {Object.keys(ABNORMAL_EVENT_TYPE_ENUM).map(
//               (typeKey) =>
//                 (+typeKey < PRIVATE_RULE_NUMBER_MIN ||
//                   (+typeKey === EABNORMAL_EVENT.DHCP_FAKE_SERVER &&
//                     detail.type === EABNORMAL_EVENT.DHCP_FAKE_SERVER)) && (
//                   <Select.Option key={typeKey} value={+typeKey}>
//                     {ABNORMAL_EVENT_TYPE_ENUM[typeKey].label}
//                   </Select.Option>
//                 ),
//             )}
//           </Select>
//         </FormItem>

//         <FormItem
//           {...formItemLayout}
//           label="事件内容"
//           name="content"
//           rules={getContentRule()}
//           extra={contentPlaceholder}
//           validateFirst={true}
//         >
//           <TextArea rows={4} placeholder={contentPlaceholder} />
//         </FormItem>
//         <FormItem {...formItemLayout} label="状态" name="status" valuePropName="checked">
//           <Switch checkedChildren="启用" unCheckedChildren="禁用" />
//         </FormItem>
//         <FormItem {...submitFormLayout} style={{ marginTop: 32 }}>
//           <Button type="primary" htmlType="submit" loading={submitting}>
//             保存
//           </Button>
//           <Button style={{ marginLeft: 8 }} onClick={handleGoBack}>
//             取消
//           </Button>
//         </FormItem>
//       </Form>
//     </Card>
//   );
// };

// export default connect(({ loading: { effects } }: ConnectState) => ({
//   submitting:
//     effects['abnormalEventModel/createAbnormalEventRule'] ||
//     effects['abnormalEventModel/updateAbnormalEventRule'],
// }))(AbnormalEventRuleForm);
