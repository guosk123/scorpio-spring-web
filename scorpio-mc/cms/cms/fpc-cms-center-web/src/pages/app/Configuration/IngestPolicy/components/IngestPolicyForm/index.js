import {
  createConfirmModal,
  bpfValid,
  ipV4Regex,
  ipV6Regex,
  updateConfirmModal,
} from '@/utils/utils';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { MinusCircleOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, Card, Col, Divider, Input, notification, Radio, Row, Select } from 'antd';
import { connect } from 'dva';
import hash from 'hash.js';
import _ from 'lodash';
import React, { PureComponent } from 'react';
import { history } from 'umi';

const FormItem = Form.Item;
const RadioGroup = Radio.Group;
const { TextArea } = Input;

export const DEFAULT_POLICY_ID = '1';
// 规则抓包配置支持的协议类型
const FILTER_PROTOCOL_TYPE_LIST = [
  {
    key: '',
    label: '不限制',
  },
  {
    key: 'TCP',
    label: 'TCP',
  },
  {
    key: 'UDP',
    label: 'UDP',
  },
];

const DEDUPLICATION_LIST = [
  {
    key: '0',
    label: '去重',
  },
  {
    key: '1',
    label: '不去重',
  },
];

/**
 * 捕获过滤：存储
 */
export const APPLIANCE_INGEST_ACTION_SAVE = '0';
/**
 * 捕获过滤：不存储
 */
export const APPLIANCE_INGEST_ACTION_NO_SAVE = '1';

/**
 * 流量捕获策略
 */
export const APPLIANCE_INGEST_ACTION = [
  {
    key: APPLIANCE_INGEST_ACTION_SAVE,
    label: '捕获',
  },
  {
    key: APPLIANCE_INGEST_ACTION_NO_SAVE,
    label: '不捕获',
  },
];

// VLANID可以配置的范围为：0-4094 （如果不配置，则与0是相同的，相当于查找没有VLAN标签头的流）
export const VLANID_MIN_NUMBER = 0;
export const VLANID_MAX_NUMBER = 4094;

// 源端口、目的端口只支持输出数字，取值范围：1-65535之间
export const PORT_MIN_NUMBER = 1;
export const PORT_MAX_NUMBER = 65535;

// 规则最大的条数限制
export const TUPLE_MAX_COUNT = 10;

export const exceptTupleExtra = (
  <section>
    <ul style={{ listStyle: 'decimal', paddingLeft: 20 }}>
      <li>源IP、目的IP支持 A.B.C.D 格式的单IP或 A.B.C.D/E 格式的网段</li>
      <li>源IP、目的IP支持IPv6网络</li>
      <li>
        源端口、目的端口支持单端口、端口范围，可以配置的范围为：[{PORT_MIN_NUMBER},{' '}
        {PORT_MAX_NUMBER}]
      </li>
      <li>
        VLANID支持单VLAN或VLAN范围，可以配置的范围为：[{VLANID_MIN_NUMBER}, {VLANID_MAX_NUMBER}]
      </li>
      <li>配置多个规则时，规则之间至少需要有1项内容不同，即不允许存在完全相同的两条规则配置</li>
      <li>一组规则中所有的值都为空时，保存时将会被忽略</li>
      <li>最多可以配置{TUPLE_MAX_COUNT}个有效规则</li>
    </ul>
  </section>
);

const value2String = (value) => {
  if (value === 0) {
    return '0';
  }
  if (!value) return '';
  return String(value);
};

// 分隔符
const SEPARATOR = '$_o_$_';
let sixTupleNo = 0;

/**
 * 校验IP、IP段，支持 IP v6
 * @param {*} rule
 * @param {*} value
 * @param {*} callback
 *
 * @example 192.168.1.1
 * @example 192.168.1.2/24
 * @example 2031::130f::09c0:876a:130b
 */
function checkSourceOrDestIp(rule, value, callback) {
  if (!value) {
    callback();
    return;
  }

  // 如果有 - ，直接判错
  if (value.indexOf('-') > -1) {
    callback('请输入正确的IP/IP段');
  } else if (value.indexOf('/') > -1) {
    const ips = value.split('/');
    // 校验第一个 ip
    if (!ipV4Regex.test(ips[0]) && !ipV6Regex.test(ips[0])) {
      callback('请输入正确的IP/IP段。支持 IPv4 和 IPv6');
      return;
    }
    // 校验子网掩码
    // eslint-disable-next-line no-restricted-globals
    if (!ips[1] || isNaN(ips[1])) {
      callback('IP网段请填写子网掩码');
      return;
    }
    // IPv4最高支持32
    if (ipV4Regex.test(ips[0]) && (ips[1] <= 0 || ips[1] > 32)) {
      callback('IPv4子网掩码范围是(0,32]');
      return;
    }
    // IPv6最高支持128
    if (ipV6Regex.test(ips[0]) && (ips[1] <= 0 || ips[1] > 128)) {
      callback('IPv6子网掩码范围是(0,128]');
      return;
    }
  } else if (!ipV4Regex.test(value) && !ipV6Regex.test(value)) {
    callback('请输入正确的IP地址');
    return;
  }

  callback();
}

/**
 * 端口校验
 */
function checkPort(rule, value, callback) {
  if (!value) {
    callback();
    return;
  }

  if (!/^[0-9-]+$/.test(value)) {
    callback('错误的端口范围');
    return;
  }

  // 支持单端口
  const portRange = value.split('-');
  const [portStart, portEnd] = portRange;
  if (portRange.length === 1) {
    if (portStart < PORT_MIN_NUMBER || portStart > PORT_MAX_NUMBER) {
      callback(`端口范围是[${PORT_MIN_NUMBER}, ${PORT_MAX_NUMBER}]`);
      return;
    }
  } else if (portRange.length === 2) {
    if (
      portStart < PORT_MIN_NUMBER ||
      portStart > PORT_MAX_NUMBER ||
      portEnd < PORT_MIN_NUMBER ||
      portEnd > PORT_MAX_NUMBER
    ) {
      callback(`端口范围是[${PORT_MIN_NUMBER}, ${PORT_MAX_NUMBER}]`);
      return;
    }

    if (+portEnd <= +portStart) {
      callback('错误的端口范围');
      return;
    }
  } else {
    callback('请输入正确的端口范围');
    return;
  }

  callback();
}

/**
 * 端口VLANID
 */
function checkVlan(rule, value, callback) {
  if (!value) {
    callback();
    return;
  }

  if (!/^[0-9-]+$/.test(value)) {
    callback('错误的VLANID范围');
    return;
  }

  const vlanRange = value.split('-');
  const [vlanStart, vlanEnd] = vlanRange;
  if (vlanRange.length === 1) {
    if (vlanStart < VLANID_MIN_NUMBER || vlanStart > VLANID_MAX_NUMBER) {
      callback(`VLANID范围是[${VLANID_MIN_NUMBER}, ${VLANID_MAX_NUMBER}]`);
      return;
    }
  } else if (vlanRange.length === 2) {
    if (
      vlanStart < VLANID_MIN_NUMBER ||
      vlanStart > VLANID_MAX_NUMBER ||
      vlanEnd < VLANID_MIN_NUMBER ||
      vlanEnd > VLANID_MAX_NUMBER
    ) {
      callback(`VLANID范围是[${VLANID_MIN_NUMBER}, ${VLANID_MAX_NUMBER}]`);
      return;
    }

    if (+vlanEnd <= +vlanStart) {
      callback('错误的VLANID范围');
      return;
    }
  } else {
    callback('请输入正确的VLANID范围');
    return;
  }

  callback();
}

const tupleTitleStyle = {
  textAlign: 'center',
};

// 规则标题
const tupleTitle = (
  <Row gutter={4}>
    <Col style={{ ...tupleTitleStyle }} span={1} />
    <Col style={{ ...tupleTitleStyle }} span={5}>
      源IP
    </Col>
    <Col style={{ ...tupleTitleStyle }} span={3}>
      源端口
    </Col>
    <Col style={{ ...tupleTitleStyle }} span={5}>
      目的IP
    </Col>
    <Col style={{ ...tupleTitleStyle }} span={3}>
      目的端口
    </Col>
    <Col style={{ ...tupleTitleStyle }} span={3}>
      协议号
    </Col>
    <Col style={{ ...tupleTitleStyle }} span={3}>
      VLANID
    </Col>
    <Col style={{ ...tupleTitleStyle }} span={1} />
  </Row>
);

/**
 * 计算每规则的item的key值
 * 如果不是字符串，转成hash字符串
 * @param {Object} key
 */
const convertTupleItemKey = (key) => {
  if (typeof key === 'string') {
    return key;
  }
  return hash.sha256().update(JSON.stringify(key)).digest('hex');
};

@Form.create()
@connect((state) => {
  const { loading } = state;
  const { effects } = loading;
  return {
    submitting:
      effects['ingestPolicyModel/createIngestPolicy'] ||
      effects['ingestPolicyModel/updateIngestPolicy'],
  };
})
class IngestPolicy extends PureComponent {
  constructor(props) {
    super(props);
    this.createConfirmModal = createConfirmModal.bind(this);
    this.updateConfirmModal = updateConfirmModal.bind(this);
  }

  componentDidMount() {
    this.debouncedBpfValid = _.debounce(bpfValid, 500);
  }

  addTupleItem = () => {
    const { form } = this.props;
    const exceptTupleKeys = form.getFieldValue('exceptTupleKeys');
    sixTupleNo += 1;
    const nextKeys = exceptTupleKeys.concat(`${SEPARATOR}${sixTupleNo}`);
    form.setFieldsValue({
      exceptTupleKeys: nextKeys,
    });
  };

  removeTupleItem = (k) => {
    const { form } = this.props;
    const exceptTupleKeys = form.getFieldValue('exceptTupleKeys');
    form.setFieldsValue({
      exceptTupleKeys: exceptTupleKeys.filter((key) => convertTupleItemKey(key) !== k),
    });
  };

  handleSubmit = (e) => {
    e.preventDefault();
    const { form } = this.props;

    form.validateFieldsAndScroll((err, data) => {
      if (err) {
        return;
      }
      const fieldsValue = { ...data };
      const {
        exceptTupleKeys,
        exceptTuple_sourceIp,
        exceptTuple_sourcePort,
        exceptTuple_protocol,
        exceptTuple_destIp,
        exceptTuple_destPort,
        exceptTuple_vlanId,
      } = fieldsValue;

      const exceptTupleList = [];
      const exceptTupleItemHash = [];
      // 遍历数据组装
      exceptTupleKeys.forEach((key) => {
        const kString = convertTupleItemKey(key);

        const sourcePort = exceptTuple_sourcePort[kString];
        const destPort = exceptTuple_destPort[kString];
        const vlanId = exceptTuple_vlanId[kString];

        const itemObj = {
          sourceIp: exceptTuple_sourceIp[kString],
          sourcePort: value2String(sourcePort),
          destIp: exceptTuple_destIp[kString],
          destPort: value2String(destPort),
          protocol: exceptTuple_protocol[kString],
          vlanId: value2String(vlanId),
        };

        const values = Object.values(itemObj);
        // 如果所有的值都是空，忽略过去
        const isEmpty = values.filter((value) => value || value === 0).length === 0;
        if (!isEmpty) {
          exceptTupleItemHash.push(hash.sha256().update(values.toString()).digest('hex'));
          exceptTupleList.push(itemObj);
        }
      });

      // 比较有效的六元组，是否完全不同
      const uniqResult = _.uniq(exceptTupleItemHash);
      if (uniqResult.length !== exceptTupleItemHash.length) {
        notification.warning({
          message: '无法保存',
          description: '存在两条存在完全一样的流过滤条件。请修改后再次保存。',
        });
        return;
      }

      // 如果没有有效的六元组，直接将 json 字段设置为空
      if (exceptTupleList.length === 0) {
        fieldsValue.exceptTuple = '';
      } else {
        fieldsValue.exceptTuple = JSON.stringify(exceptTupleList);
      }

      fieldsValue.name = fieldsValue.name.trim();

      // 删除无效的字段
      delete fieldsValue.exceptTupleKeys;
      delete fieldsValue.exceptTuple_sourceIp;
      delete fieldsValue.exceptTuple_sourcePort;
      delete fieldsValue.exceptTuple_destIp;
      delete fieldsValue.exceptTuple_destPort;
      delete fieldsValue.exceptTuple_protocol;
      delete fieldsValue.exceptTuple_vlanId;

      if (fieldsValue.id) {
        this.handleUpdate(fieldsValue);
      } else {
        this.handleCreate(fieldsValue);
      }
    });
  };

  handleCreate = (values) => {
    this.createConfirmModal({
      dispatchType: 'ingestPolicyModel/createIngestPolicy',
      values,
      onOk: () => {
        this.handleGoBack();
      },
      onCancel: () => {
        this.handleReset();
      },
    });
  };

  handleUpdate = (values) => {
    this.updateConfirmModal({
      dispatchType: 'ingestPolicyModel/updateIngestPolicy',
      values,
      onOk: () => {
        this.handleGoBack();
      },
      onCancel: () => {},
    });
  };

  handleGoBack = () => {
    history.goBack();
  };

  handleReset = () => {
    const { form } = this.props;
    form.resetFields();
  };

  render() {
    const {
      form: { getFieldDecorator, getFieldValue },
      detail: policy = {},
      submitting,
    } = this.props;
    const formItemLayout = {
      labelCol: {
        xs: { span: 24 },
        sm: { span: 4 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 18 },
      },
    };
    const submitFormLayout = {
      wrapperCol: {
        xs: { span: 24, offset: 0 },
        sm: { span: 12, offset: 4 },
      },
    };

    // 处理流过滤条件
    let exceptTupleList = [];
    // 解析返回值中的 json 字段
    if (policy.exceptTuple) {
      try {
        exceptTupleList = JSON.parse(policy.exceptTuple);
      } catch (err) {
        exceptTupleList = [];
      }
    }

    getFieldDecorator('exceptTupleKeys', { initialValue: exceptTupleList || [] });
    const exceptTupleKeys = getFieldValue('exceptTupleKeys');
    const sixTupleItems = exceptTupleKeys.map((k, index) => {
      const kString = convertTupleItemKey(k);
      return (
        <Row key={kString} data-key={kString} gutter={4}>
          <Col span={1} style={{ textAlign: 'center' }}>
            <Form.Item>{index + 1}</Form.Item>
          </Col>
          <Col span={5}>
            <Form.Item>
              {getFieldDecorator(`exceptTuple_sourceIp[${kString}]`, {
                initialValue: k.sourceIp || '',
                validateFirst: true,
                rules: [
                  {
                    required: false,
                    whitespace: true,
                    message: '源IP不能为空',
                  },
                  { validator: checkSourceOrDestIp },
                ],
              })(<Input size="small" placeholder="" />)}
            </Form.Item>
          </Col>
          <Col span={3}>
            <Form.Item>
              {getFieldDecorator(`exceptTuple_sourcePort[${kString}]`, {
                initialValue: k.sourcePort || '',
                validateFirst: true,
                rules: [
                  {
                    required: false,
                    whitespace: true,
                    message: '',
                  },
                  { validator: checkPort },
                ],
              })(<Input style={{ width: '100%' }} size="small" />)}
            </Form.Item>
          </Col>
          <Col span={5}>
            <Form.Item>
              {getFieldDecorator(`exceptTuple_destIp[${kString}]`, {
                initialValue: k.destIp || '',
                validateFirst: true,
                rules: [
                  {
                    required: false,
                    whitespace: true,
                    message: '目的IP不能为空',
                  },
                  { validator: checkSourceOrDestIp },
                ],
              })(<Input size="small" placeholder="" />)}
            </Form.Item>
          </Col>
          <Col span={3}>
            <Form.Item>
              {getFieldDecorator(`exceptTuple_destPort[${kString}]`, {
                initialValue: k.destPort || '',
                validateFirst: true,
                rules: [
                  {
                    required: false,
                    whitespace: true,
                    message: '',
                  },
                  { validator: checkPort },
                ],
              })(<Input style={{ width: '100%' }} size="small" />)}
            </Form.Item>
          </Col>
          <Col span={3}>
            <Form.Item>
              {getFieldDecorator(`exceptTuple_protocol[${kString}]`, {
                initialValue: k.protocol || '',
                validateFirst: true,
                rules: [
                  {
                    required: false,
                    message: '请选择过滤协议',
                  },
                ],
              })(
                <Select size="small">
                  {FILTER_PROTOCOL_TYPE_LIST.map((item) => (
                    <Select.Option key={item.key} value={item.key}>
                      {item.label}
                    </Select.Option>
                  ))}
                </Select>,
              )}
            </Form.Item>
          </Col>
          <Col span={3}>
            <Form.Item>
              {getFieldDecorator(`exceptTuple_vlanId[${kString}]`, {
                initialValue: k.vlanId || '',
                validateFirst: true,
                rules: [
                  {
                    required: false,
                    whitespace: true,
                    message: '',
                  },
                  { validator: checkVlan },
                ],
              })(<Input style={{ width: '100%' }} size="small" />)}
            </Form.Item>
          </Col>
          <Col span={1}>
            <Form.Item>
              <MinusCircleOutlined onClick={() => this.removeTupleItem(kString)} />
            </Form.Item>
          </Col>
        </Row>
      );
    });

    return (
      <Card bordered={false}>
        <Form onSubmit={this.handleSubmit}>
          <FormItem {...formItemLayout} label="id" style={{ display: 'none' }}>
            {getFieldDecorator('id', {
              initialValue: policy.id,
            })(<Input />)}
          </FormItem>
          <FormItem
            {...formItemLayout}
            label="名称"
            extra={policy.id === DEFAULT_POLICY_ID && '默认规则无法修改名称'}
          >
            {getFieldDecorator('name', {
              initialValue: policy.name,
              rules: [
                {
                  required: true,
                  whitespace: true,
                  message: '请填写捕获规则名称',
                },
                {
                  max: 32,
                  message: '最多可输入32个字符',
                },
              ],
            })(<Input disabled={policy.id === DEFAULT_POLICY_ID} placeholder="捕获规则名称" />)}
          </FormItem>
          <FormItem
            {...formItemLayout}
            label="默认动作"
            extra={
              (getFieldValue('defaultAction') || policy.defaultAction) ===
              APPLIANCE_INGEST_ACTION_SAVE
                ? '默认动作选择捕获时，接口所有收到的报文都会捕获处理，但符合额外配置的包或流将不再捕获'
                : '默认动作选择不捕获时，符合额外配置的包或流都会捕获处理，其它包或流将不再捕获'
            }
          >
            {getFieldDecorator('defaultAction', {
              initialValue: policy.defaultAction || '1',
              rules: [
                {
                  required: true,
                  message: '请选择流量过滤策略',
                },
              ],
            })(
              <RadioGroup>
                {APPLIANCE_INGEST_ACTION.map((item) => (
                  <Radio key={item.key} value={item.key}>
                    {item.label}
                  </Radio>
                ))}
              </RadioGroup>,
            )}
          </FormItem>
          <FormItem {...formItemLayout} label="报文去重">
            {getFieldDecorator('deduplication', {
              initialValue: policy.deduplication || '1',
              rules: [
                {
                  required: true,
                  message: '请选择报文去重策略',
                },
              ],
            })(
              <RadioGroup>
                {DEDUPLICATION_LIST.map((item) => (
                  <Radio key={item.key} value={item.key}>
                    {item.label}
                  </Radio>
                ))}
              </RadioGroup>,
            )}
          </FormItem>
          {/* 额外配置 */}
          <Divider style={{ minWidth: 'initial', margin: '16px auto' }} orientation="center">
            额外配置
          </Divider>
          <FormItem
            {...formItemLayout}
            label="BPF过滤条件"
            extra="标准BPF语法，条件过于复杂，有可能会影响系统整体性能"
          >
            {getFieldDecorator('exceptBpf', {
              initialValue: policy.exceptBpf || '',
              validateFirst: true,
              rules: [
                {
                  required: false,
                  whitespace: true,
                  message: '请填写BPF过滤条件',
                },
                {
                  max: 1024,
                  message: '最多可输入1024个字符',
                },
                {
                  validator: this.debouncedBpfValid,
                },
              ],
            })(<Input.TextArea rows={4} />)}
          </FormItem>
          <FormItem {...formItemLayout} label="流过滤条件" extra={exceptTupleExtra}>
            <Card bordered bodyStyle={{ padding: 4 }}>
              {tupleTitle}
              {sixTupleItems}
              {/* 增加按钮 */}
              <Form.Item style={{ marginBottom: 0, textAlign: 'center' }}>
                {exceptTupleKeys.length < TUPLE_MAX_COUNT ? (
                  <Button type="primary" size="small" onClick={this.addTupleItem}>
                    <PlusOutlined /> 新增
                  </Button>
                ) : (
                  <Button type="primary" size="small" disabled>
                    最多可配置{TUPLE_MAX_COUNT}个
                  </Button>
                )}
              </Form.Item>
            </Card>
          </FormItem>
          <FormItem {...formItemLayout} label="备注">
            {getFieldDecorator('description', {
              initialValue: policy.description || '',
              rules: [
                {
                  required: false,
                  message: '请填写备注',
                },
                {
                  max: 255,
                  message: '最多可输入255个字符',
                },
              ],
            })(<TextArea rows={4} />)}
          </FormItem>
          <FormItem {...submitFormLayout}>
            <Button type="primary" htmlType="submit" loading={submitting}>
              保存
            </Button>
            <Button style={{ marginLeft: 8 }} onClick={this.handleGoBack}>
              取消
            </Button>
          </FormItem>
        </Form>
      </Card>
    );
  }
}

export default IngestPolicy;
