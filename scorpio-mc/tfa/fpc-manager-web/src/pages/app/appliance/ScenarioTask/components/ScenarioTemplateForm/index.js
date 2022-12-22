/* eslint-disable no-restricted-globals */
/* eslint-disable no-throw-literal */
import SplConverter, { getFullSpl } from '@/components/Spl2SqlConverter';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { CheckCircleTwoTone } from '@ant-design/icons';
import { Button, Checkbox, Col, Input, InputNumber, Modal, Row, Select } from 'antd';
import { connect } from 'dva';
import PropTypes from 'prop-types';
import React, { Fragment, PureComponent } from 'react';
import FieldFilter, { filterCondition2Spl } from '@/components/FieldFilter';
import { history } from 'umi';
import {
  commonFilterFields,
  networkMetadataFilterField,
  dnsFilterField,
  ftpFilterField,
  mailFilterField,
  telnetFilterField,
  sslFilterField,
  recordFilterField,
} from './context';

const FormItem = Form.Item;

const formLayout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 16 },
};

const subTailLayout = {
  labelCol: { lg: { span: 5 }, xl: { span: 4 } },
  wrapperCol: { span: 16 },
  style: { marginBottom: 0 },
};

const commonFields = [
  { value: 'src_ipv4', label: '源IPv4', type: 'IPv4' },
  { value: 'src_ipv6', label: '源IPv6', type: 'IPv6' },
  { value: 'src_port', label: '源端口' },
  { value: 'dest_ipv4', label: '目的IPv4', type: 'IPv4' },
  { value: 'dest_ipv6', label: '目的IPv6', type: 'IPv6' },
  { value: 'dest_port', label: '目的端口' },
  { value: 'policy_name', label: '策略名称', description: '默认策略请填写空字符串' },
];

export const flowRecordComputableFields = [
  { value: 'upstream_bytes', label: '上行字节数' },
  { value: 'downstream_bytes', label: '下行字节数' },
  { value: 'total_bytes', label: '总字节数' },
  { value: 'upstream_packets', label: '上行包数' },
  { value: 'downstream_packets', label: '下行包数' },
  { value: 'total_packets', label: '总包数' },
  { value: 'upstream_payload_bytes', label: '上行payload' },
  { value: 'downstream_payload_bytes', label: '下行payload' },
  { value: 'total_payload_bytes', label: 'payload总字节数' },
];

const DATA_SOURCE_HTTP = 'http';
const DATA_SOURCE_DNS = 'dns';
/**
 * 分析场景列表
 */
export const DATA_SOURCE_LIST = [
  {
    value: DATA_SOURCE_HTTP,
    label: 'HTTP详单',
    fields: [
      ...commonFields,
      { value: 'file_name', label: '传输文件名称' },
      { value: 'file_type', label: '文件传输方式', description: '0-非文件传输，1-上传，2-下载' },
      { value: 'file_flag', label: '传输文件类型' },
      { value: 'method', label: 'method' },
      { value: 'host', label: 'host' },
      { value: 'uri', label: 'url' },
      { value: 'xff', label: 'xff' },
    ],
    computableFields: [],
  },
  {
    value: DATA_SOURCE_DNS,
    label: 'DNS详单',
    fields: [
      ...commonFields,
      { value: 'domain', label: 'Domain', type: 'Array' },
      { value: 'domain_ipv4', label: '域名解析地址IPv4', type: 'Array<IPv4>' },
      { value: 'domain_ipv6', label: '域名解析地址IPv6', type: 'Array<IPv6>' },
      { value: 'dns_rcode', label: 'DNS协议返回码' },
      { value: 'dns_rcode_name', label: 'DNS协议返回码名称' },
    ],
    computableFields: [],
  },
  {
    value: 'ftp',
    label: 'FTP详单',
    fields: [{ value: 'user', label: '用户名' }],
    computableFields: [],
  },
  {
    value: 'mail',
    label: 'MAIL详单',
    fields: [
      ...commonFields,
      { value: 'protocol', label: '协议' },
      { value: 'from', label: '发件人' },
      { value: 'to', label: '收件人' },
      { value: 'subject', label: '邮件主题' },
      { value: 'cc', label: '抄送' },
      { value: 'bcc', label: '密送' },
      { value: 'attachment', label: '附件名称' },
    ],
    computableFields: [],
  },
  {
    value: 'telnet',
    label: 'TELNET详单',
    fields: [
      ...commonFields,
      { value: 'username', label: '登录用户' },
      { value: 'cmd', label: '操作命令' },
    ],
    computableFields: [],
  },
  {
    value: 'ssl',
    label: 'SSL详单',
    fields: [
      ...commonFields,
      { value: 'server_name', label: '服务器名称' },
      { value: 'ja3_client', label: '客户端指纹' },
      { value: 'ja3_server', label: '服务端指纹' },
      { value: 'version', label: 'SSL版本' },
      { value: 'issuer', label: '证书发布者' },
      { value: 'common_name', label: '证书使用者' },
    ],
    computableFields: [],
  },
  {
    value: 'flow-log-record',
    label: '会话详单',
    fields: [
      // TODO: 可能要加网络 ID 和业务 ID
      { value: 'interface', label: '接口名称' },
      { value: 'duration', label: '持续时间(ms)' },
      // { value: 'flow_continued', label: '日志类型', description: '0-不看超长流，1-只看超长流' },
      ...flowRecordComputableFields,
      { value: 'ethernet_initiator', label: '源MAC' },
      { value: 'ethernet_responder', label: '目的MAC' },
      { value: 'ethernet_protocol', label: '网络层协议' },
      { value: 'ipv4_initiator', label: '源IPv4', type: 'IPv4' },
      { value: 'ipv4_responder', label: '目的IPv4', type: 'IPv4' },
      { value: 'ipv6_initiator', label: '源IPv6', type: 'IPv6' },
      { value: 'ipv6_responder', label: '目的IPv6', type: 'IPv6' },
      { value: 'ip_protocol', label: '传输层协议' },
      { value: 'port_initiator', label: '源端口' },
      { value: 'port_responder', label: '目的端口' },
      { value: 'l7_protocol', label: '应用层协议' },
      { value: 'application_name', label: '应用名称' },
      { value: 'country_initiator', label: '源IP国家' },
      { value: 'province_initiator', label: '源IP省份' },
      { value: 'city_initiator', label: '源IP城市' },
      { value: 'country_responder', label: '目的IP国家' },
      { value: 'province_responder', label: '目的IP省份' },
      { value: 'city_responder', label: '目的IP城市' },
    ],
    computableFields: [...flowRecordComputableFields],
  },
];

export const DATA_SOURCE_LIST_FILTER = [
  {
    value: DATA_SOURCE_HTTP,
    label: 'HTTP详单',
    fields: [...commonFilterFields, ...networkMetadataFilterField],
    computableFields: [],
  },
  {
    value: DATA_SOURCE_DNS,
    label: 'DNS详单',
    fields: [...commonFilterFields, ...dnsFilterField],
    computableFields: [],
  },
  {
    value: 'ftp',
    label: 'FTP详单',
    fields: [...ftpFilterField],
    computableFields: [],
  },
  {
    value: 'mail',
    label: 'MAIL详单',
    fields: [...commonFilterFields, ...mailFilterField],
    computableFields: [],
  },
  {
    value: 'telnet',
    label: 'TELNET详单',
    fields: [...commonFilterFields, ...telnetFilterField],
    computableFields: [],
  },
  {
    value: 'ssl',
    label: 'SSL详单',
    fields: [...commonFilterFields, ...sslFilterField],
    computableFields: [],
  },
  {
    value: 'flow-log-record',
    label: '会话详单',
    fields: [...recordFilterField],
    computableFields: [...flowRecordComputableFields],
  },
];

export const GROUP_BY_LIST = [
  { value: 'src_ip', label: '源IP' },
  { value: 'dest_ip', label: '目的IP' },
  { value: 'src_ip&dest_ip', label: '源IP+目的IP' },
  { value: 'src_ip&dest_ip&src_port', label: '源IP+目的IP+源端口' },
  { value: 'src_ip&dest_ip&dest_port', label: '源IP+目的IP+目的端口' },
  { value: 'src_ip&dest_ip&src_port&dest_port', label: '源IP+目的IP+源端口+目的端口' },
];

export const EVAL_FUNCTION_COUNT = 'COUNT';
export const EVAL_FUNCTION_SUM = 'SUM';
export const EVAL_FUNCTION_BEACON = 'BEACON';

/**
 * 计算方法
 */
export const EVAL_FUNCTION_LIST = [
  EVAL_FUNCTION_COUNT,
  EVAL_FUNCTION_SUM,
  EVAL_FUNCTION_BEACON,
].map((func) => ({
  value: func,
  label: func,
}));

@Form.create()
@connect(({ SAKnowledgeModel: { allApplicationList }, loading: { effects } }) => ({
  allApplicationList,
  queryAllApplicationsLoading: effects['SAKnowledgeModel/queryAllApplications'],
  submitLoading: effects['scenarioTaskModel/createScenarioCustomTemplate'],
}))
class ScenarioTaskTemplateForm extends PureComponent {
  static propTypes = {
    submitLoading: PropTypes.bool,
    operateType: PropTypes.oneOf(['CREATE', 'UPDATE']),
  };

  static defaultProps = {
    submitLoading: false,
    operateType: 'CREATE',
  };

  state = {
    filterCondition: [],
    filterField: [],
  };

  componentDidMount() {
    this.queryAllApplications();
  }

  queryAllApplications = () => {
    const { dispatch } = this.props;
    dispatch({
      type: 'SAKnowledgeModel/queryAllApplications',
    });
  };

  handleDataSourceChange = () => {
    const {
      form: { setFieldsValue },
    } = this.props;
    this.setState({
      filterCondition: [],
    });

    // 数据源变化时，清空可选的字段
    setFieldsValue({
      sumField: undefined,
      splConverter: { spl: '', dsl: {} },
    });
  };

  /**
   * 计算方法变化时检查数据源和过滤条件
   */
  handleFuncNameChange = (functionName) => {
    const { form } = this.props;
    // 检查数据源
    const dataSourceValue = form.getFieldValue('dataSource');
    if (
      functionName === EVAL_FUNCTION_BEACON &&
      dataSourceValue !== DATA_SOURCE_DNS &&
      dataSourceValue !== DATA_SOURCE_HTTP
    ) {
      // 清空数据源和过滤条件
      form.setFieldsValue({
        dataSource: undefined,
        splConverter: { spl: '', dsl: {} },
      });
    }
    // 重新触发数据源的校验
    setTimeout(() => {
      form.validateFields(['dataSource'], { force: true });
    }, 0);
  };

  checkDataSource = (rule, value, callback) => {
    const {
      form: { getFieldValue },
    } = this.props;
    const funcName = getFieldValue('functionName');
    if (
      value !== DATA_SOURCE_HTTP &&
      value !== DATA_SOURCE_DNS &&
      funcName === EVAL_FUNCTION_BEACON
    ) {
      callback(`此数据源不支持${EVAL_FUNCTION_BEACON}计算`);
      return;
    }
    callback();
  };

  // 提交
  handleSubmit = (e) => {
    const { form, operateType } = this.props;
    e.preventDefault();
    form.validateFieldsAndScroll((err, fieldsValue) => {
      if (err) return;

      const values = {
        ...fieldsValue,
      };

      // === 过滤条件 ===
      // const { splConverter } = values;
      // const { dsl, spl } = splConverter || {};
      const spl = filterCondition2Spl(values.fieldFilter, this.state.filterField);
      // if (!dsl || !spl) {
      if (!spl) {
        return;
      }

      values.filterSpl = JSON.stringify({
        spl,
        filterCondition: values.fieldFilter,
      });
      values.filterDsl = '';
      // delete values.splConverter;
      delete values.fieldFilter;

      // === 计算方法 ===
      const { functionName, functionParams } = values;
      const evalFunction = {
        name: functionName,
      };
      if (functionName === EVAL_FUNCTION_SUM) {
        evalFunction.params = { field: functionParams.field };
      }
      if (functionName === EVAL_FUNCTION_BEACON) {
        evalFunction.params = { numberThreshold: functionParams.numberThreshold };
      }
      delete values.functionName;
      delete values.functionParams;
      values.function = JSON.stringify(evalFunction);

      // === 按时间平均 ===
      const { timeAvgState, avgTimeInterval } = values;
      values.avgTimeInterval = timeAvgState ? avgTimeInterval : 0;
      delete values.timeAvgState;

      // === 按时间切片 ===
      const { timeSliceState, sliceTimeInterval } = values;
      values.sliceTimeInterval = timeSliceState ? sliceTimeInterval : 0;
      delete values.timeSliceState;

      if (operateType === 'CREATE') {
        this.handleCreate(values);
      }
    });
  };

  handleGoBack = () => {
    history.goBack();
  };

  handleReset = () => {
    const { form } = this.props;
    form.resetFields();
  };

  handleCreate = (values) => {
    const { dispatch } = this.props;
    Modal.confirm({
      title: '确定保存吗？',
      cancelText: '取消',
      okText: '确定',
      onOk: () => {
        dispatch({
          type: 'scenarioTaskModel/createScenarioCustomTemplate',
          payload: values,
        }).then((result) => {
          if (result) {
            Modal.confirm({
              keyboard: false,
              title: '保存成功',
              icon: <CheckCircleTwoTone size={24} twoToneColor="#52c41a" />,
              cancelText: '继续添加',
              okText: '返回列表页',
              onOk: () => {
                this.handleGoBack();
              },
              onCancel: () => {
                this.handleReset();
              },
            });
          }
        });
      },
    });
  };

  handleFilterChange = (newFilter) => {
    this.setState({
      filterCondition: newFilter,
    });
  };

  handleFilterFieldChange = (newFilter) => {
    this.setState({
      filterField: newFilter,
    });
  };

  render() {
    const {
      form: { getFieldDecorator, getFieldValue },
      submitLoading,
    } = this.props;

    const { filterCondition } = this.state;

    const filterField = [];

    getFieldDecorator('sliceTimeInterval');
    getFieldDecorator('avgTimeInterval');
    getFieldDecorator('functionParams[field]');
    getFieldDecorator('functionParams[numberThreshold]');

    // 过滤出当前数据源可 Sum 的字段
    const dataSourceValue = getFieldValue('dataSource');
    const dataSourceInfo = DATA_SOURCE_LIST.find((ds) => ds.value === dataSourceValue);
    const dataSourceInfoFilter = DATA_SOURCE_LIST_FILTER.find((ds) => ds.value === dataSourceValue);

    let computableFields = [];
    let allFields = [];
    if (dataSourceInfo) {
      computableFields = dataSourceInfo.computableFields;
      allFields = dataSourceInfo.fields;
    }
    let allFilterFields = [];
    if (dataSourceInfoFilter) {
      allFilterFields = dataSourceInfoFilter.fields;
      this.handleFilterFieldChange(allFilterFields);
    }

    return (
      <Form onSubmit={this.handleSubmit}>
        <FormItem key="id" {...formLayout} label="id" style={{ display: 'none' }}>
          {getFieldDecorator('id', {})(<Input placeholder="请输入" />)}
        </FormItem>
        <FormItem key="name" {...formLayout} label="模板名称">
          {getFieldDecorator('name', {
            rules: [
              { required: true, whitespace: true, message: '请输入模板名称' },
              {
                max: 30,
                message: '最多可输入30个字符',
              },
            ],
          })(<Input placeholder="请输入任务名称" />)}
        </FormItem>
        <FormItem key="dataSource" {...formLayout} label="数据源">
          {getFieldDecorator('dataSource', {
            validateFirst: true,
            rules: [
              { required: true, message: '请选择数据源' },
              {
                validator: this.checkDataSource,
              },
            ],
          })(
            <Select placeholder="请选择数据源" onChange={this.handleDataSourceChange}>
              {DATA_SOURCE_LIST.map((item) => (
                <Select.Option value={item.value}>{item.label}</Select.Option>
              ))}
            </Select>,
          )}
        </FormItem>
        {/* <Form.Item {...formLayout} label="过滤条件" required>
          {getFieldDecorator('splConverter', {
            initialValue: {},
          })(<SplConverter rows={4} limitTime={false} onlyFilter fields={allFields} />)}
        </Form.Item> */}
        <Form.Item {...formLayout} label="过滤条件" required>
          {getFieldDecorator('fieldFilter', {
            initialValue: {},
            rules: [{ required: true, message: '请添加过滤条件' }],
          })(
            allFilterFields.length ? (
              <FieldFilter
                key="scenario-template-filter"
                fields={allFilterFields}
                onChange={this.handleFilterChange}
                condition={filterCondition}
                // historyStorageKey={filterHistoryKey}
              />
            ) : (
              <span>请先选择数据源</span>
            ),
          )}
        </Form.Item>
        <FormItem
          key="function"
          {...formLayout}
          label="计算方法"
          extra={(() => {
            if (getFieldValue('functionName') === EVAL_FUNCTION_BEACON) {
              return '数据源只支持HTTP和DNS';
            }
            if (getFieldValue('functionName') === EVAL_FUNCTION_SUM) {
              return '数据源只支持会话详单';
            }
            return '';
          })()}
        >
          <Row gutter={10}>
            <Col span={5}>
              {getFieldDecorator('functionName', {
                initialValue: EVAL_FUNCTION_LIST[0].value,
                rules: [{ required: true, message: '请选择计算方法' }],
              })(
                <Select style={{ width: '100%' }} onChange={this.handleFuncNameChange}>
                  {EVAL_FUNCTION_LIST.map((item) => (
                    <Select.Option key={item.value} value={item.value}>
                      {item.label}
                    </Select.Option>
                  ))}
                </Select>,
              )}
            </Col>
            {/* sum 时的 字段名称 */}
            {getFieldValue('functionName') === EVAL_FUNCTION_SUM && (
              <Col span={18}>
                <FormItem {...subTailLayout} required label="字段">
                  {getFieldDecorator('functionParams[field]', {
                    rules: [{ required: true, whitespace: true, message: '请选择所需 Sum 的字段' }],
                  })(
                    <Select
                      style={{ width: '100%' }}
                      placeholder="请选择字段"
                      notFoundContent="没有可用字段"
                    >
                      {computableFields.map((item) => (
                        <Select.Option key={item.value} value={item.value}>
                          {item.label}
                        </Select.Option>
                      ))}
                    </Select>,
                  )}
                </FormItem>
              </Col>
            )}
            {getFieldValue('functionName') === EVAL_FUNCTION_BEACON && (
              <Col span={18}>
                <FormItem {...subTailLayout} required label="数量阈值">
                  {getFieldDecorator('functionParams[numberThreshold]', {
                    rules: [{ required: true, message: '请填写数量阈值' }],
                  })(<InputNumber style={{ width: 100 }} min={1} precision={0} />)}
                </FormItem>
              </Col>
            )}
          </Row>
        </FormItem>
        {getFieldValue('functionName') !== EVAL_FUNCTION_BEACON && (
          <Fragment>
            <FormItem {...formLayout} label="按时间平均">
              <Row>
                <Col span={2}>
                  {getFieldDecorator('timeAvgState', {
                    initialValue: false,
                    valuePropName: 'checked',
                  })(<Checkbox />)}
                </Col>
                {getFieldValue('timeAvgState') && (
                  <Col span={18}>
                    <FormItem {...subTailLayout} required label="时间间隔">
                      {getFieldDecorator('avgTimeInterval', {
                        rules: [{ required: true, message: '请填写时间间隔' }],
                      })(<InputNumber min={1} precision={0} style={{ width: 100 }} />)}
                      <span className="ant-form-text">秒</span>
                    </FormItem>
                  </Col>
                )}
              </Row>
            </FormItem>
            <FormItem {...formLayout} label="按时间切片">
              <Row>
                <Col span={2}>
                  {getFieldDecorator('timeSliceState', {
                    initialValue: false,
                    valuePropName: 'checked',
                  })(<Checkbox />)}
                </Col>
                {getFieldValue('timeSliceState') && (
                  <Col span={18}>
                    <FormItem {...subTailLayout} required label="时间间隔">
                      {getFieldDecorator('sliceTimeInterval', {
                        rules: [{ required: true, message: '请填写时间间隔' }],
                      })(<InputNumber min={1} precision={0} style={{ width: 100 }} />)}
                      <span className="ant-form-text">秒</span>
                    </FormItem>
                  </Col>
                )}
              </Row>
            </FormItem>
          </Fragment>
        )}
        <FormItem {...formLayout} label="分组">
          {getFieldDecorator('groupBy', { initialValue: '' })(
            <Select placeholder="请选择分组">
              <Select.Option value="">不分组</Select.Option>
              {GROUP_BY_LIST.map((item) => (
                <Select.Option key={item.value} value={item.value}>
                  {item.label}
                </Select.Option>
              ))}
            </Select>,
          )}
        </FormItem>
        <FormItem key="description" {...formLayout} label="描述信息">
          {getFieldDecorator('description', {
            initialValue: '',
            rules: [
              { required: false, message: '请输入描述信息' },
              { max: 255, message: '最多可输入255个字符' },
            ],
          })(<Input.TextArea rows={4} placeholder="请输入描述信息" />)}
        </FormItem>
        <FormItem wrapperCol={{ span: 12, offset: 4 }}>
          <Button
            style={{ marginRight: 10 }}
            type="primary"
            htmlType="submit"
            loading={submitLoading}
          >
            保存
          </Button>
          <Button onClick={() => this.handleGoBack()}>返回</Button>
        </FormItem>
      </Form>
    );
  }
}

export default ScenarioTaskTemplateForm;
