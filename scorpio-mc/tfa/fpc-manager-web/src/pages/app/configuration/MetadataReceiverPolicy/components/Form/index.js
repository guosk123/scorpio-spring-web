/* eslint-disable no-throw-literal */
import { API_BASE_URL, API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import { BOOL_NO, BOOL_YES } from '@/common/dict';
import { getCookie } from '@/utils/frame/cookie';
import { ipV4Regex } from '@/utils/utils';
import { Form, Icon as LegacyIcon } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import {
  Button,
  Checkbox,
  Col,
  Input,
  InputNumber,
  message,
  Modal,
  Radio,
  Row,
  Switch,
  Upload,
} from 'antd';
import { connect } from 'dva';
import React, { Fragment, PureComponent } from 'react';
import styles from './index.less';

const FormItem = Form.Item;
const CheckboxGroup = Checkbox.Group;

/**
 * 默认配置：发送
 */
export const HTTP_ACTION_POLICY_SEND = '0';

/**
 * 发送策略
 */
export const HTTP_ACTION_LIST = [
  {
    key: HTTP_ACTION_POLICY_SEND,
    label: '发送',
  },
  {
    key: '1',
    label: '过滤',
  },
];

/**
 * 前缀
 */
const PREFIX = 'id_';

const UploadProps = {
  listType: 'text',
  accept: '.keytab',
  name: 'file',
  withCredentials: true,
  headers: {
    'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
  },
  action: `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/metadata/receiver-settings/keytab`,
};

@Form.create()
@connect(({ metadataModel: { metadataProtocolsList }, loading }) => ({
  metadataProtocolsList,
  queryAllProtocolsLoading: loading.effects['metadataModel/queryAllProtocols'],
}))
class MetadataReceiverPolicyForm extends PureComponent {
  constructor(props) {
    super(props);
    const { keytabFilePath } = props.values;
    this.state = {
      keytabFile: keytabFilePath
        ? [{ uid: keytabFilePath, url: keytabFilePath, name: keytabFilePath }]
        : [],
      showHttpDetail: false,
      uploadLoading: false,
    };
  }
  /** 增加流日志外发配置 */
  componentDidMount() {
    if (this.props.metadataProtocolsList.find((item) => item.protocolId === 'flow_log')) {
      return;
    }
    this.props.metadataProtocolsList.push({
      protocolId: 'flow_log',
      nameText: '会话详单',
      name: '会话详单',
      description: '',
      descriptionText: '',
    });
  }

  handleFileChange = (info) => {
    const { status } = info.file;
    if (status === 'done' || status === 'removed' || status === 'error') {
      this.setState({ uploadLoading: false });
    } else {
      this.setState({ uploadLoading: true });
    }

    if (status === 'error') {
      message.error('上传失败');
      return [];
    }
    if (Array.isArray(info)) {
      return info;
    }
    if (info && info.fileList) {
      const result = info.fileList.slice(-1).map((item) => ({
        uid: item.uid,
        url: item.response,
        name: item.name,
      }));
      return result;
    }

    return [];
  };

  handleToogleDetail = () => {
    this.setState((state) => ({
      showHttpDetail: !state.showHttpDetail,
    }));
  };

  onCheckAllChange = (e) => {
    const { form, metadataProtocolsList } = this.props;
    form.setFieldsValue({
      protocol: e.target.checked ? metadataProtocolsList.map((item) => item.protocolId) : [],
    });
  };
  
  checkClusterNodeAddress = (rule, value, callback) => {
    if (!value) {
      callback('请填写节点地址');
      return;
    }
    const passIpArr = []; // 已经检查通过的IP
    const valueArr = value.split('\n');

    try {
      if (Array.isArray(valueArr)) {
        valueArr.forEach((item, index) => {
          const lineText = `第${index + 1}行[${item}]: `;
          if (!item) {
            throw new Error(`${lineText}不能为空`);
          }
          // 单 IP 校验
          if (item.indexOf(':') > -1) {
            const itemSplit = item.split(':');
            if (itemSplit.length !== 2) {
              throw new Error(
                `${lineText}请输入正确的地址。仅限输入IP加端口的地址，例如：A.B.C.D:PORT`,
              );
            }
            const [ip, port] = itemSplit;
            // 检查 ip 是否正确
            if (!ipV4Regex.test(ip)) {
              throw new Error(`${lineText}输入的IP地址不正确`);
            }
            // 检查端口范围是否正确
            // eslint-disable-next-line no-restricted-globals
            if (isNaN(port) || port < 1 || port > 65535) {
              throw new Error(`${lineText}端口范围不正确`);
            }
            // 是否重复了
            if (passIpArr.indexOf(item) !== -1) {
              throw new Error(`${lineText}已重复`);
            }
            passIpArr.push(item);
          } else {
            throw new Error(`${lineText}请输入符合要求的地址`);
          }
        });
      }
    } catch (e) {
      callback(e);
    } finally {
      callback();
    }
  };

  render() {
    const {
      form,
      form: { getFieldDecorator, getFieldValue },
      values = {}, // 初始值
      loading,
      metadataProtocolsList,
    } = this.props;

    let protocolTopicParse = {};
    if (values.protocolTopic) {
      try {
        protocolTopicParse = JSON.parse(values.protocolTopic);
      } catch (error) {
        protocolTopicParse = {};
      }
    }
    if (Object.keys(protocolTopicParse).length > 0) {
      const newProtocolTopic = {};
      Object.keys(protocolTopicParse).forEach((key) => {
        newProtocolTopic[key] = protocolTopicParse[key];
      });
      values.protocolTopicJson = newProtocolTopic;
    } else {
      values.protocolTopicJson = {};
    }

    const { showHttpDetail, keytabFile, uploadLoading } = this.state;

    const okHandle = (e) => {
      const { dispatch } = this.props;
      e.preventDefault();
      form.validateFieldsAndScroll((err, fieldsValue) => {
        if (err) return;

        const { state, protocolTopic, receiverAddress, kerberosCertification, keytabFilePath } =
          fieldsValue;

        const newProtocolTopic = {};
        Object.keys(protocolTopic).forEach((key) => {
          newProtocolTopic[key.replace(PREFIX, '')] = protocolTopic[key];
        });

        const submitData = {
          ...fieldsValue,
          state: state ? BOOL_YES : BOOL_NO,
          protocolTopic: JSON.stringify(newProtocolTopic),
          receiverAddress: receiverAddress.split('\n').join(','),
          // KERBEROS认证
          kerberosCertification: kerberosCertification ? BOOL_YES : BOOL_NO,
          keytabFilePath: kerberosCertification ? keytabFilePath[0].url : '',
        };

        delete submitData.protocol;

        Modal.confirm({
          title: '确定保存吗?',
          cancelText: '取消',
          okText: '确定',
          onOk: () => {
            dispatch({
              type: 'receiverSettingsModel/update',
              payload: submitData,
            });
          },
        });
      });
    };

    let checked = false;
    const protocolFormValue = getFieldValue('protocol');
    // 先以form表单的值为准
    if (protocolFormValue) {
      checked = protocolFormValue.length === metadataProtocolsList.length;
    } else {
      checked = Object.keys(values.protocolTopicJson).length === metadataProtocolsList.length;
    }

    return (
      <Form
        onSubmit={okHandle}
        labelCol={{ xs: { span: 24 }, sm: { span: 5 } }}
        wrapperCol={{
          xs: { span: 24 },
          sm: { span: 16 },
        }}
      >
        <FormItem label="id" style={{ display: 'none' }}>
          {getFieldDecorator('id', {
            initialValue: values.id,
          })(<Input />)}
        </FormItem>
        <FormItem label="名称">
          {getFieldDecorator('name', {
            initialValue: values.name,
            validateFirst: true,
            rules: [
              {
                required: true,
                message: '请填写发送配置名称',
              },
              {
                max: 32,
                message: '最多限制32个字符',
              },
            ],
          })(<Input placeholder="请填写发送配置名称" />)}
        </FormItem>
        <FormItem label="状态" required>
          {getFieldDecorator('state', {
            valuePropName: 'checked',
            initialValue: values.state === BOOL_YES || false,
          })(<Switch checkedChildren="开启" unCheckedChildren="关闭" />)}
        </FormItem>
        <FormItem label="日志类型" required style={{ marginBottom: 0 }}>
          <Fragment>
            <Checkbox indeterminate={checked} onChange={this.onCheckAllChange} checked={checked}>
              全选
            </Checkbox>
            <Row>
              <Col xl={6} md={6} className={styles.protocolWrap}>
                <FormItem>
                  {getFieldDecorator('protocol', {
                    initialValue: values.protocolTopicJson
                      ? Object.keys(values.protocolTopicJson)
                      : [],
                    rules: [
                      {
                        required: true,
                        message: '请至少勾选一个',
                      },
                    ],
                  })(
                    <CheckboxGroup>
                      <Row>
                        {metadataProtocolsList.map((item) => (
                          <Col className={styles.groupItem} span={24}>
                            <Checkbox value={item.protocolId}>{item.nameText}</Checkbox>
                            {item.nameText.toLocaleUpperCase() === 'HTTP' && (
                              <Button
                                type="primary"
                                size="small"
                                icon={<LegacyIcon type={showHttpDetail ? 'up' : 'down'} />}
                                onClick={this.handleToogleDetail}
                              >
                                详情
                              </Button>
                            )}
                          </Col>
                        ))}
                      </Row>
                    </CheckboxGroup>,
                  )}
                </FormItem>
              </Col>
              <Col xl={18} md={18} className={styles.topicWrap}>
                {metadataProtocolsList.map((item) => (
                  <div className={styles.topicItem}>
                    {getFieldValue('protocol').indexOf(item.protocolId) > -1 && (
                      <FormItem>
                        {/* 往后端传递的协议的ID */}
                        {getFieldDecorator(`protocolTopic[${PREFIX}${item.protocolId}]`, {
                          initialValue:
                            values.protocolTopicJson && values.protocolTopicJson[item.protocolId],
                          rules: [
                            {
                              required: getFieldValue('protocol').indexOf(item.protocolId) > -1,
                              whitespace: true,
                              message: '请填写 Kafka Topic',
                            },
                          ],
                        })(<Input placeholder="请填写 Kafka Topic" />)}
                      </FormItem>
                    )}
                  </div>
                ))}
              </Col>
            </Row>
          </Fragment>
        </FormItem>
        <FormItem
          label="HTTP详情"
          required
          style={{
            display: showHttpDetail ? 'flex' : 'none',
            marginBottom: 0,
          }}
        >
          <Row>
            <Col span={3}>
              <span className="ant-form-text">默认配置</span>
            </Col>
            <Col span={21}>
              {getFieldDecorator('httpAction', {
                initialValue: values.httpAction || HTTP_ACTION_POLICY_SEND,
                rules: [
                  {
                    required: true,
                    message: '请选择默认配置',
                  },
                ],
              })(
                <Radio.Group>
                  {HTTP_ACTION_LIST.map((item) => (
                    <Radio value={item.key}>{item.label}</Radio>
                  ))}
                </Radio.Group>,
              )}
            </Col>

            <Col span={3}>
              <span className="ant-form-text">额外配置</span>
            </Col>
            <Col span={21}>
              <FormItem
                extra={`符合填写的 URI 后缀的 HTTP日志将被${
                  getFieldValue('httpAction') === HTTP_ACTION_POLICY_SEND ? '过滤' : '发送'
                }`}
              >
                {getFieldDecorator('httpActionExculdeUriSuffix', {
                  initialValue: values.httpActionExculdeUriSuffix,
                  rules: [
                    {
                      required: false,
                    },
                  ],
                })(<Input.TextArea rows={4} placeholder="js,css,jpg" />)}
              </FormItem>
            </Col>
          </Row>
        </FormItem>
        <FormItem
          label="节点地址"
          extra="仅限输入IP加端口的地址，例如：A.B.C.D:PORT。多个地址请用换行符分割"
        >
          {getFieldDecorator('receiverAddress', {
            initialValue: values.receiverAddress && values.receiverAddress.replace(/,/g, '\n'),
            rules: [
              {
                required: true,
                message: '请填写 Kafka 集群节点地址',
              },
              {
                validator: this.checkClusterNodeAddress,
              },
            ],
          })(<Input.TextArea rows={4} placeholder="请填写 Kafka 集群节点地址" />)}
        </FormItem>
        <FormItem label="KERBEROS认证" required>
          {getFieldDecorator('kerberosCertification', {
            valuePropName: 'checked',
            initialValue: values.kerberosCertification === BOOL_YES || false,
          })(<Switch checkedChildren="开" unCheckedChildren="关" />)}
        </FormItem>
        {getFieldValue('kerberosCertification') && (
          <Fragment>
            <FormItem label="keytab文件">
              {getFieldDecorator('keytabFilePath', {
                initialValue: keytabFile,
                valuePropName: 'fileList',
                getValueFromEvent: this.handleFileChange,
                rules: [
                  {
                    required: true,
                    message: '请导入keytab文件',
                  },
                ],
              })(
                <Upload {...UploadProps}>
                  <Button>
                    <LegacyIcon type={uploadLoading ? 'loading' : 'upload'} />
                    {uploadLoading ? '上传中' : '导入文件'}
                  </Button>
                </Upload>,
              )}
            </FormItem>
            <FormItem label="key尝试恢复时间">
              {getFieldDecorator('keyRestoreTime', {
                initialValue: values.keyRestoreTime,
                rules: [
                  {
                    required: true,
                    message: '请填写key尝试恢复时间',
                  },
                ],
              })(<InputNumber style={{ minWidth: 200 }} precision={0} min={1} />)}
              <span className="ant-form-text"> ms</span>
            </FormItem>
            <FormItem label="sasl.kerberos.service.name">
              {getFieldDecorator('saslKerberosServiceName', {
                initialValue: values.saslKerberosServiceName,
                rules: [
                  {
                    required: true,
                    whitespace: true,
                    message: '请填写sasl.kerberos.service.name',
                  },
                ],
              })(<Input placeholder="请填写sasl.kerberos.service.name" />)}
            </FormItem>
            <FormItem label="sasl.kerberos.principal">
              {getFieldDecorator('saslKerberosPrincipal', {
                initialValue: values.saslKerberosPrincipal,
                rules: [
                  {
                    required: true,
                    whitespace: true,
                    message: '请填写sasl.kerberos.principal',
                  },
                ],
              })(<Input placeholder="请填写sasl.kerberos.principal" />)}
            </FormItem>
            <FormItem label="安全协议">
              {getFieldDecorator('securityProtocol', {
                initialValue: 'sasl_plaintext',
                rules: [
                  {
                    required: true,
                    whitespace: true,
                    message: '请填写安全协议',
                  },
                ],
              })(<Input disabled placeholder="请填写安全协议" />)}
            </FormItem>
            <FormItem label="鉴权机制">
              {getFieldDecorator('authenticationMechanism', {
                initialValue: 'GSSAPI',
                rules: [
                  {
                    required: true,
                    whitespace: true,
                    message: '请填写鉴权机制',
                  },
                ],
              })(<Input disabled placeholder="请填写鉴权机制" />)}
            </FormItem>
          </Fragment>
        )}
        <FormItem wrapperCol={{ offset: 5, span: 10 }}>
          <Button className="mr-10" type="primary" htmlType="submit" loading={loading}>
            保存
          </Button>
        </FormItem>
      </Form>
    );
  }
}

export default MetadataReceiverPolicyForm;
