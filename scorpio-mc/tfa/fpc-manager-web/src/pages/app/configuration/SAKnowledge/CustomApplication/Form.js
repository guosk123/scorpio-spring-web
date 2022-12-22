/* eslint-disable @typescript-eslint/naming-convention */
import { createConfirmModal, ipV4Regex, ipV6Regex, updateConfirmModal } from '@/utils/utils';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { MinusCircleOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, Card, Col, Input, InputNumber, notification, Row, Select } from 'antd';
import { connect } from 'dva';
import hash from 'hash.js';
import _ from 'lodash';
import PropTypes from 'prop-types';
import React, { PureComponent } from 'react';
import { history } from 'umi';
import { ECustomSAApiType } from '../typings';

const FormItem = Form.Item;
const { TextArea } = Input;

const formLayout = {
  labelCol: { span: 2 },
  wrapperCol: { span: 22 },
};

/**
 * 16进制
 */
const SIGNATURE_TYPE_HEX = 'hex';
/**
 * ASCII
 */
const SIGNATURE_TYPE_ASCII = 'ascii';
/**
 * 签名类型
 */
export const SIGNATURE_TYPE_LIST = [
  {
    key: SIGNATURE_TYPE_HEX,
    label: '16进制',
  },
  {
    key: SIGNATURE_TYPE_ASCII,
    label: 'ASCII',
  },
];

const PORT_MIN_NUMBER = 1;
const PORT_MAX_NUMBER = 65535;

export const getSignatureTypeName = (signatureType) => {
  const info = SIGNATURE_TYPE_LIST.find((item) => item.key === signatureType);
  return info ? info.label : signatureType;
};

/**
 * 规则中可选择的协议
 */
const PROTOCOL_TYPE_LIST = ['all', 'tcp', 'udp'];

/**
 * 规则最大限制数量
 */
const RULE_MAX_LIMIT = 16;

// 分隔符
const SEPARATOR = '$_o_$_';
let ruleNo = 0;

/**
 * 规则描述信息
 */
export const ruleExtra = (
  <section>
    <ul style={{ listStyle: 'decimal', paddingLeft: 20 }}>
      <li>名称、协议字段必填</li>
      <li>IP地址、端口、签名请至少填写1项</li>
      <li>IP地址支持单一IP或者IP/掩码，支持IPv6网络</li>
      <li>协议支持：TCP、UDP或ALL（代表TCP/UDP都可以）</li>
      <li>
        端口可配置范围： 1-65535，允许配置一个端口（例如 80）、多个端口（例如
        80,443）、端口范围（10-20）
      </li>
      <li>签名偏移支持 -1或者一个小于1500的正整数，-1代表任意位置匹配</li>
      <li>
        配置多条规则时，规则和规则之间至少需要有1项内容不同，即不允许存在完全相同的两条规则配置
      </li>
      <li>最多可以配置{RULE_MAX_LIMIT}条有效规则</li>
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

const ruleTitleStyle = {
  textAlign: 'center',
};

const nameSpan = 4;
const ipAddressSpan = 3;
const protocolSpan = 3;
const portSpan = 3;
const signatureTypeSpan = 3;
const signatureOffsetSpan = 3;
const signatureContentSpan = 3;

// 规则标题
const ruleTitle = (
  <Row gutter={4}>
    <Col style={{ ...ruleTitleStyle }} span={1} />
    <Col style={{ ...ruleTitleStyle }} span={nameSpan}>
      名称
    </Col>
    <Col style={{ ...ruleTitleStyle }} span={ipAddressSpan}>
      IP地址
    </Col>
    <Col style={{ ...ruleTitleStyle }} span={protocolSpan}>
      传输层协议
    </Col>
    <Col style={{ ...ruleTitleStyle }} span={portSpan}>
      端口
    </Col>
    <Col style={{ ...ruleTitleStyle }} span={signatureTypeSpan}>
      签名类型
    </Col>
    <Col style={{ ...ruleTitleStyle }} span={signatureOffsetSpan}>
      签名偏移
    </Col>
    <Col style={{ ...ruleTitleStyle }} span={signatureContentSpan}>
      签名内容
    </Col>
    <Col style={{ ...ruleTitleStyle }} span={1} />
  </Row>
);

/**
 * 计算每条规则的item的key值
 * 如果不是字符串，转成hash字符串
 * @param {Object} key
 */
const convertRuleItemKey = (key) => {
  if (typeof key === 'string') {
    return key;
  }
  return hash.sha256().update(JSON.stringify(key)).digest('hex');
};

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
function checkIpAddress(rule, value, callback) {
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

@Form.create()
@connect(
  ({
    SAKnowledgeModel: { allCategoryList, allSubCategoryList, allApplicationList },
    metadataModel: { metadataProtocolsList },
    loading,
  }) => ({
    allCategoryList,
    allSubCategoryList,
    allApplicationList,
    metadataProtocolsList,
    submitLoading:
      loading.effects['customSAModel/createCustomSA'] ||
      loading.effects['customSAModel/updateCustomSA'],
  }),
)
class CustomApplicationForm extends PureComponent {
  static propTypes = {
    detail: PropTypes.object,
    operateType: PropTypes.oneOf(['CREATE', 'UPDATE']).isRequired,
  };

  static defaultProps = {
    detail: {},
  };

  constructor(props) {
    super(props);
    this.createConfirmModal = createConfirmModal.bind(this);
    this.updateConfirmModal = updateConfirmModal.bind(this);

    this.state = {
      selectSubcategoryId: '',
    };
  }

  handleCategoryChange = () => {
    const { form } = this.props;
    form.setFieldsValue({
      subCategoryId: undefined,
    });
  };

  addRuleItem = () => {
    const { form } = this.props;
    const ruleKeys = form.getFieldValue('ruleKeys');
    ruleNo += 1;
    const nextKeys = ruleKeys.concat(`${SEPARATOR}${ruleNo}`);
    form.setFieldsValue({
      ruleKeys: nextKeys,
    });
  };

  removeRuleItem = (k) => {
    const { form } = this.props;
    const ruleKeys = form.getFieldValue('ruleKeys');
    form.setFieldsValue({
      ruleKeys: ruleKeys.filter((key) => convertRuleItemKey(key) !== k),
    });
  };

  checkApplicationName = (rule, value, callback) => {
    const { detail, allApplicationList } = this.props;
    const { id, applicationId } = detail;
    if (!value) {
      callback();
    }
    const appName = value.trim();
    const existsApp = allApplicationList.filter((app) => app.nameText === appName);
    // 如果是新增
    if (!id) {
      if (existsApp.length > 0) {
        callback('应用名称已存在');
        return;
      }
    } else if (
      existsApp.length > 1 ||
      (existsApp.length === 1 && existsApp[0].applicationId !== applicationId)
    ) {
      callback('应用名称已存在');
      return;
    }
    callback();
  };

  /**
   * IP地址、端口、签名请至少填写1项
   */
  checkIpAndPortAndSignature = (rule, value, callback) => {
    const {
      form: { getFieldValue },
    } = this.props;

    const suffix = `[${rule.fullField.split('[')[1]}`;

    const ipAddress = getFieldValue(`rule_ipAddress${suffix}`);
    const port = getFieldValue(`rule_port${suffix}`);
    const signatureType = getFieldValue(`rule_signatureType${suffix}`);
    const signatureOffset = getFieldValue(`rule_signatureOffset${suffix}`);
    const signatureContent = getFieldValue(`rule_signatureContent${suffix}`);

    const signatureFlag = +!!signatureType + +!!signatureOffset + +!!signatureContent;
    if (+!!ipAddress + +!!port + signatureFlag === 0) {
      callback('IP地址、端口、签名请至少填写1项');
      return;
    }

    callback();
  };

  checkPort = (rule, portValue, callback) => {
    if (!portValue) {
      callback();
      return;
    }
    if (portValue.includes('-')) {
      const portArr = portValue.split('-');
      if (portArr.length !== 2) {
        callback('请输入正确的端口范围。例，10-20');
        return;
      }
      const port1Num = +portArr[0];
      const port2Num = +portArr[1];

      if (
        port1Num < PORT_MIN_NUMBER ||
        port1Num > PORT_MAX_NUMBER ||
        port2Num < PORT_MIN_NUMBER ||
        port2Num > PORT_MAX_NUMBER ||
        port1Num >= port2Num
      ) {
        callback('请输入正确的端口范围');
        return;
      }
      callback();
      return;
    }

    const portArr = portValue.split(',');
    for (let index = 0; index < portArr.length; index += 1) {
      const port = +portArr[index];
      if (isNaN(port) || port < PORT_MIN_NUMBER || port > PORT_MAX_NUMBER) {
        callback('请输入正确的端口范围');
        break;
      }
    }
    // 最长限制64位
    if (portValue.length > 64) {
      callback('长度最长64位');
      return;
    }
    callback();
  };

  checkSignatureType = (rule, value, callback) => {
    const {
      form: { getFieldValue },
    } = this.props;

    const suffix = rule.fullField.replace('rule_signatureType', '');

    const signatureOffset = getFieldValue(`rule_signatureOffset${suffix}`);
    const signatureContent = getFieldValue(`rule_signatureContent${suffix}`);

    if (value && ((!signatureOffset && signatureOffset !== 0) || !signatureContent)) {
      callback('签名填写不完整');
      return;
    }

    callback();
  };

  checkSignatureOffset = (rule, value, callback) => {
    const {
      form: { getFieldValue, validateFields },
    } = this.props;

    const suffix = rule.fullField.replace('rule_signatureOffset', '');
    const signatureType = getFieldValue(`rule_signatureType${suffix}`);
    if (!signatureType) {
      callback();
      return;
    }
    if (!value && value !== 0) {
      callback('请填写签名偏移');
      return;
    }
    validateFields([`rule_signatureType${suffix}`, `rule_port${suffix}`], { force: true });
    // TODO: 校验范围
    callback();
  };

  checkSignatureContent = (rule, value, callback) => {
    const {
      form: { getFieldValue, validateFields },
    } = this.props;

    const suffix = rule.fullField.replace('rule_signatureContent', '');
    const signatureType = getFieldValue(`rule_signatureType${suffix}`);

    if (!signatureType) {
      callback();
      return;
    }
    if (!value) {
      callback('请填写签名内容');
      return;
    }
    validateFields([`rule_signatureType${suffix}`, `rule_port${suffix}`], { force: true });
    // 校验16进制
    if (signatureType === SIGNATURE_TYPE_HEX) {
      // 内容
      const regex = /^[0-9a-fA-F]+$/;
      if (!regex.test(value)) {
        callback('只能输入 0-9 或 a-f 或 A-F 的字符');
        return;
      }

      // 长度必须是2的整数倍
      if (value.length % 2 !== 0) {
        callback('长度必须为2的整数倍');
        return;
      }
      // 最长限制64位
      if (value.length > 64) {
        callback('长度最长64位');
        return;
      }
    }
    // 校验 ASCII
    if (signatureType === SIGNATURE_TYPE_ASCII) {
      // 只允许输入可显示字符
      const isPrintableASCII = (string) => /^[\x20-\x7F]*$/.test(string);
      if (!isPrintableASCII(value)) {
        callback('只允许输入可显示ASCII码');
        return;
      }
      // 最长限制32位
      if (value.length > 32) {
        callback('长度最长32位');
        return;
      }
    }
    callback();
  };

  // 提交
  handleSubmit = (e) => {
    const { form, operateType } = this.props;
    e.preventDefault();
    form.validateFieldsAndScroll((err, fieldsValue) => {
      if (err) {
        return;
      }

      const {
        // 规则
        ruleKeys,
        rule_name,
        rule_ipAddress,
        rule_protocol,
        rule_port,
        rule_signatureType,
        rule_signatureOffset,
        rule_signatureContent,
      } = fieldsValue;

      const fields = {
        ...fieldsValue,
      };

      const ruleList = [];
      const ruleItemHash = [];
      // 遍历数据组装
      ruleKeys.forEach((key) => {
        const kString = convertRuleItemKey(key);

        const itemObj = {
          name: rule_name[kString],
          ipAddress: rule_ipAddress[kString],
          protocol: rule_protocol[kString],
          port: value2String(rule_port[kString]),
          signatureType: value2String(rule_signatureType[kString]),
          signatureOffset: value2String(rule_signatureOffset[kString]),
          signatureContent: value2String(rule_signatureContent[kString]),
        };

        const values = Object.values(itemObj);
        // 如果所有的值都是空，忽略过去
        const isEmpty = values.filter((value) => value || value === 0).length === 0;
        if (!isEmpty) {
          ruleItemHash.push(convertRuleItemKey(values.toString()));
          ruleList.push(itemObj);
        }
      });

      // 比较有效的规则，是否完全不同
      const uniqResult = _.uniq(ruleItemHash);
      if (uniqResult.length !== ruleItemHash.length) {
        notification.warning({
          message: '无法保存',
          description: '存在完全一样的规则。请修改后再次保存。',
        });
        return;
      }

      fields.rule = JSON.stringify(ruleList);

      delete fields.ruleKeys;
      delete fields.rule_ipAddress;
      delete fields.rule_name;
      delete fields.rule_port;
      delete fields.rule_protocol;
      delete fields.rule_signatureContent;
      delete fields.rule_signatureOffset;
      delete fields.rule_signatureType;
      if (operateType === 'CREATE') {
        this.handleCreate({ data: fields, type: ECustomSAApiType.APPLICATION });
      } else {
        this.handleUpdate({ data: fields, type: ECustomSAApiType.APPLICATION });
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
    this.createConfirmModal({
      dispatchType: 'customSAModel/createCustomSA',
      values,
      onOk: this.handleGoBack,
      onCancel: this.handleReset,
    });
  };

  handleUpdate = (values) => {
    this.updateConfirmModal({
      dispatchType: 'customSAModel/updateCustomSA',
      values,
      onOk: this.handleGoBack,
    });
  };

  render() {
    const {
      submitLoading,
      form,
      allCategoryList,
      allSubCategoryList,
      metadataProtocolsList,
      detail = {}, // 当前编辑的用户
    } = this.props;

    const { getFieldDecorator, getFieldValue } = form;
    const selectCategoryId = getFieldValue('categoryId')
      ? getFieldValue('categoryId')
      : detail.categoryId;

    let displaySubCategoryList = [];
    if (selectCategoryId) {
      displaySubCategoryList = allSubCategoryList.filter(
        (sub) => sub.categoryId === selectCategoryId,
      );
    }

    let initRuleKeys = [];
    if (detail.rule) {
      try {
        initRuleKeys = JSON.parse(detail.rule);
      } catch (error) {
        initRuleKeys = [];
      }
    }

    getFieldDecorator('ruleKeys', {
      initialValue: initRuleKeys,
    });
    const ruleKeys = getFieldValue('ruleKeys');
    const ruleItems = ruleKeys.map((k, index) => {
      const kString = convertRuleItemKey(k);
      return (
        <Row key={kString} data-key={kString} gutter={4}>
          <Col span={1} style={{ textAlign: 'center' }}>
            <Form.Item>{index + 1}</Form.Item>
          </Col>
          <Col span={nameSpan}>
            <Form.Item>
              {getFieldDecorator(`rule_name[${kString}]`, {
                initialValue: k.name || '',
                validateFirst: true,
                rules: [
                  {
                    required: true,
                    whitespace: true,
                    message: '名称不能为空',
                  },
                ],
              })(<Input size="small" placeholder="名称" />)}
            </Form.Item>
          </Col>
          <Col span={portSpan}>
            <Form.Item>
              {getFieldDecorator(`rule_ipAddress[${kString}]`, {
                initialValue: k.ipAddress || '',
                validateFirst: true,
                rules: [
                  {
                    required: false,
                    whitespace: true,
                    message: 'IP地址不能为空',
                  },
                  { validator: checkIpAddress },
                ],
              })(<Input size="small" placeholder="IP地址" />)}
            </Form.Item>
          </Col>
          <Col span={protocolSpan}>
            <Form.Item>
              {getFieldDecorator(`rule_protocol[${kString}]`, {
                initialValue: k.protocol || undefined,
                validateFirst: true,
                rules: [
                  {
                    required: true,
                    message: '请选择协议',
                  },
                ],
              })(
                <Select size="small" placeholder="选择协议">
                  {PROTOCOL_TYPE_LIST.map((protocol) => (
                    <Select.Option value={protocol}>{protocol.toLocaleUpperCase()}</Select.Option>
                  ))}
                </Select>,
              )}
            </Form.Item>
          </Col>
          <Col span={portSpan}>
            <Form.Item>
              {getFieldDecorator(`rule_port[${kString}]`, {
                initialValue: k.port || undefined,
                validateFirst: true,
                rules: [
                  {
                    required: false,
                    message: '',
                  },
                  { validator: this.checkIpAndPortAndSignature },
                  { validator: this.checkPort },
                ],
              })(<Input placeholder="端口" style={{ width: '100%' }} size="small" />)}
            </Form.Item>
          </Col>
          <Col span={signatureTypeSpan}>
            <Form.Item>
              {getFieldDecorator(`rule_signatureType[${kString}]`, {
                initialValue: k.signatureType || undefined,
                validateFirst: true,
                rules: [
                  {
                    required: false,
                    message: '选择签名类型',
                  },
                  { validator: this.checkSignatureType },
                ],
              })(
                <Select size="small" placeholder="选择签名类型">
                  <Select.Option value="">无</Select.Option>
                  {SIGNATURE_TYPE_LIST.map((item) => (
                    <Select.Option key={item.key} value={item.key}>
                      {item.label}
                    </Select.Option>
                  ))}
                </Select>,
              )}
            </Form.Item>
          </Col>

          <Col span={signatureOffsetSpan}>
            <Form.Item>
              {getFieldDecorator(`rule_signatureOffset[${kString}]`, {
                initialValue: k.signatureOffset || '',
                validateFirst: true,
                rules: [{ validator: this.checkSignatureOffset }],
              })(
                <InputNumber
                  placeholder="签名偏移"
                  style={{ width: '100%' }}
                  disabled={!getFieldValue(`rule_signatureType[${kString}]`)}
                  size="small"
                  precision={0}
                  min={-1}
                  max={1500}
                />,
              )}
            </Form.Item>
          </Col>
          <Col span={signatureContentSpan}>
            <Form.Item>
              {getFieldDecorator(`rule_signatureContent[${kString}]`, {
                initialValue: k.signatureContent || '',
                validateFirst: true,
                rules: [{ validator: this.checkSignatureContent }],
              })(
                <Input
                  placeholder="签名内容"
                  disabled={!getFieldValue(`rule_signatureType[${kString}]`)}
                  style={{ width: '100%' }}
                  size="small"
                />,
              )}
            </Form.Item>
          </Col>
          <Col span={1}>
            <Form.Item>
              <MinusCircleOutlined onClick={() => this.removeRuleItem(kString)} />
            </Form.Item>
          </Col>
        </Row>
      );
    });

    return (
      <Card bordered={false}>
        <Form onSubmit={this.handleSubmit}>
          <FormItem {...formLayout} style={{ display: 'none' }}>
            {form.getFieldDecorator('id', {
              initialValue: detail.id,
            })(<Input placeholder="请输入" />)}
          </FormItem>
          <FormItem {...formLayout} key="name" label="应用名称">
            {form.getFieldDecorator('name', {
              initialValue: detail.name,
              validateFirst: true,
              rules: [
                { required: true, whitespace: true, message: '请输入应用名称' },
                { max: 32, message: '最多可输入32个字符' },
                { validator: this.checkApplicationName },
              ],
            })(<Input placeholder="请输入应用名称" />)}
          </FormItem>
          <FormItem {...formLayout} key="categoryId" label="分类">
            {form.getFieldDecorator('categoryId', {
              initialValue: detail.categoryId,
              validateFirst: true,
              rules: [{ required: true, message: '请选择应用所属分类' }],
            })(
              <Select placeholder="请选择应用所属分类" onChange={this.handleCategoryChange}>
                {allCategoryList.map((category) => (
                  <Select.Option key={category.categoryId} value={category.categoryId}>
                    {category.nameText}
                  </Select.Option>
                ))}
              </Select>,
            )}
          </FormItem>
          <FormItem {...formLayout} key="subCategoryId" label="子分类">
            {form.getFieldDecorator('subCategoryId', {
              initialValue: detail.subCategoryId,
              validateFirst: true,
              rules: [{ required: true, message: '请选择应用所属子分类' }],
            })(
              <Select placeholder="请选择应用所属子分类">
                {displaySubCategoryList.map((sub) => (
                  <Select.Option key={sub.subCategoryId} value={sub.subCategoryId}>
                    {sub.nameText}
                  </Select.Option>
                ))}
              </Select>,
            )}
          </FormItem>
          <FormItem {...formLayout} key="l7ProtocolId" label="应用层协议">
            {form.getFieldDecorator('l7ProtocolId', {
              initialValue: detail.l7ProtocolId,
              validateFirst: true,
              rules: [{ required: true, message: '请选择应用层承载协议' }],
            })(
              <Select placeholder="请选择应用层承载协议">
                <Select.Option key="other" value={'0'}>
                  其他
                </Select.Option>
                {metadataProtocolsList.map((protocol) => (
                  <Select.Option key={protocol.protocolId} value={protocol.protocolId}>
                    {protocol.nameText}
                  </Select.Option>
                ))}
              </Select>,
            )}
          </FormItem>
          <FormItem key="applicationRule" {...formLayout} label="规则" extra={ruleExtra}>
            <Card bordered bodyStyle={{ padding: 4 }}>
              {ruleTitle}
              {ruleItems}
              {/* 增加按钮 */}
              <Form.Item style={{ marginBottom: 0, textAlign: 'center' }}>
                {ruleKeys.length < RULE_MAX_LIMIT ? (
                  <Button type="primary" size="small" onClick={this.addRuleItem}>
                    <PlusOutlined /> 新增
                  </Button>
                ) : (
                  <Button type="primary" size="small" disabled>
                    最多可配置{RULE_MAX_LIMIT}个
                  </Button>
                )}
              </Form.Item>
            </Card>
          </FormItem>

          <FormItem key="description" {...formLayout} label="描述信息">
            {form.getFieldDecorator('description', {
              initialValue: detail.description || '',
              rules: [
                { required: false, message: '请输入描述信息' },
                { max: 255, message: '最多可输入255个字符' },
              ],
            })(<TextArea rows={4} placeholder="请输入描述信息" />)}
          </FormItem>
          <FormItem wrapperCol={{ offset: 2 }}>
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
      </Card>
    );
  }
}

export default CustomApplicationForm;
