import type { IFilter } from '@/components/FieldFilter/typings';
import {
  Button,
  Checkbox,
  Col,
  Form,
  Input,
  InputNumber,
  message,
  Modal,
  Radio,
  Row,
  Tree,
} from 'antd';
import { useForm } from 'antd/lib/form/Form';
import { useEffect, useMemo, useState } from 'react';
import type { ILogTypeInfoType } from './dict';
import { NETWORK_ALERT_PROPERTIES, SERVICE_ALERT_PROPERTIES, SYSTEM_ALERT_PROPERTIES, SYSTEM_LOG_PROPERTIES } from './dict';
import { FLOW_LOG_TABLE_MAP } from './dict';
import {
  EDataTypeFlag,
  EMsgHead,
  ESendingMethod,
  EWithNodeIP,
  METADATA_PLACEHOLDER,
  METADATA_TABLE_MAP,
  NETWORK_ALERT_KEY,
  SERVICE_ALERT_KEY,
  STATISTICS_PLACEHOLDER,
  TABLE_MAP,
  treeData,
} from './dict';
import {
  createSendUpRules,
  queryDatasources,
  querySendUpRuleById,
  updateSendUpRules,
} from './service';
import { connect } from 'umi';
import type { IProperty, IDataSourcesMap, IRuleFormProps } from './typing';
import { history } from 'umi';
import LogTypeSelect from './components/LogTypeSelect';
import { formatTitle } from './utils';

const layout = {
  labelCol: { span: 2 },
  wrapperCol: { span: 8 },
};

function RuleForm({ id, embed = false, onSubmit, onCancel }: IRuleFormProps) {
  const [form] = useForm();
  const [sendingMethod, setSendingMethod] = useState<ESendingMethod>(ESendingMethod.REALTIME);
  const [regName, setRegName] = useState<boolean>(false);
  const [logType, setLogType] = useState<boolean>(false);
  const [dataTypeFlag, setDataTypeFlag] = useState<EDataTypeFlag>(EDataTypeFlag.UNCHECKED);
  const [withNodeIp, setWithNodeIp] = useState<EWithNodeIP>(EWithNodeIP.UNCHECKED);
  const [selectedKeys, setSelectedKeys] = useState<string[]>([]);
  const [logTypeInfoMap, setLogTypeInfoMap] = useState<ILogTypeInfoType>({});
  /** 加载标记 */
  const [loading, setLoading] = useState<boolean>(false);
  /** 所有数据 */
  const [dataSources, setDatasources] = useState<IDataSourcesMap>({});

  /** 获取数据源数据 */
  const fetchDataSrouce = async () => {
    setLoading(true);
    const { success, result } = await queryDatasources();
    if (success) {
      setDatasources({
        ...result,
        service_alert_key: Object.keys(SERVICE_ALERT_PROPERTIES).map(key => SERVICE_ALERT_PROPERTIES[key]),
        network_alert_key: Object.keys(NETWORK_ALERT_PROPERTIES).map(key => NETWORK_ALERT_PROPERTIES[key]),
        systemAlert: Object.keys(SYSTEM_ALERT_PROPERTIES).map(key => SYSTEM_ALERT_PROPERTIES[key]),
        systemLog: Object.keys(SYSTEM_LOG_PROPERTIES).map(key => SYSTEM_LOG_PROPERTIES[key]),
      });
    }
    setLoading(false);
  };

  /** 处理表单提交 */
  const onFinish = ({ name, interval }: any) => {

    const params = {
      name,
      sendRuleContent: JSON.stringify(
        selectedKeys
          .filter((f) => !f.includes('placeholder'))
          .map((key) => {
            const isAlert = key === SERVICE_ALERT_KEY || key === NETWORK_ALERT_KEY;
            const isDhcp = key.includes('dhcp');
            const isDhcpV6 = key === 'dhcp_v6';
            const isStatisticsDhcpV6 = key === 'statistics_dhcp_v6';
            const isStatisticsDhcp = key === 'statistics_dhcp';
            const isMail = key === 'mail-pop3' || key === 'mail-smtp' || key === 'mail-imap';

            const dsl = logTypeInfoMap[key]?.dsl || '';

            return {
              index: (() => {
                if (isAlert) {
                  return 'alert';
                }
                if (isDhcpV6) {
                  return 'dhcp';
                }
                if (isStatisticsDhcpV6) {
                  return 'statistics_dhcp_v6';
                }
                if (isMail) {
                  return 'mail';
                }
                return key;
              })(),
              output_detail_info: {
                sending_method: sendingMethod === ESendingMethod.REALTIME ? 'now' : interval,
              },
              data_type_flag: logType ? '1' : '0',
              with_node_ip: withNodeIp,
              filter_info: (() => {
                if (isAlert) {
                  return `${key === SERVICE_ALERT_KEY ? `service_id !=''` : `network_id !=''`}${dsl ? ` AND ${dsl}` : ''
                    }`;
                }

                if (isDhcp) {
                  if (isDhcpV6) {
                    return `(version = '1')${dsl ? ` AND ${dsl}` : ''}`;
                  } else if (isStatisticsDhcpV6) {
                    return `(dhcp_version = '1')${dsl ? ` AND ${dsl}` : ''}`;
                  } else if (isStatisticsDhcp) {
                    return `(dhcp_version = '0')${dsl ? ` AND ${dsl}` : ''}`;
                  } else {
                    return `(version = '0')${dsl ? ` AND ${dsl}` : ''}`;
                  }
                }

                if (isMail) {
                  if (key === 'mail-pop3') {
                    return `(protocol = 'pop3')${dsl ? ` AND ${dsl}` : ''}`;
                  } else if (key === 'mail-smtp') {
                    return `(protocol = 'smtp')${dsl ? ` AND ${dsl}` : ''}`;
                  } else if (key === 'mail-imap') {
                    return `(protocol = 'imap')${dsl ? ` AND ${dsl}` : ''}`;
                  }
                }

                return dsl;
              })(),
              originConditions: logTypeInfoMap[key]?.conditions || [],
              originProperties: logTypeInfoMap[key]?.properties || [],
              originDsl: logTypeInfoMap[key]?.dsl || '',
              originIndex: key,
              msg_name: regName ? name : '',
              properties: (() => {
                if (logTypeInfoMap[key]?.properties?.length > 0) {
                  return (
                    logTypeInfoMap[key]?.properties?.map((p) => {
                      return {
                        field_name: p.name,
                        field_type: p.type || '',
                        output_name: p.name,
                      };
                    }) || []
                  );
                } else {
                  return dataSources[TABLE_MAP[key]]?.map((p) => {
                    return {
                      field_name: p.name,
                      field_type: p.type || '',
                      output_name: p.name,
                    };
                  });
                }
              })(),
            };
          }),
      ),
    };

    Modal.confirm({
      title: '确定提交吗?',
      cancelText: '取消',
      okText: '确定',
      onOk: async () => {
        if (id) {
          const { success } = await updateSendUpRules({ id, ...params });
          if (success) {
            message.success('编辑成功!');
            if (!embed) {
              history.push('/configuration/third-party/sendup-rules');
            }
            if (onSubmit) {
              onSubmit(true);
            }
            return;
          }
          if (onSubmit) {
            onSubmit(false);
          }
          message.error('编辑失败!');
        } else {
          const { success } = await createSendUpRules(params);
          if (success) {
            message.success('创建成功!');
            if (!embed) {
              history.push('/configuration/third-party/sendup-rules');
            }
            if (onSubmit) {
              onSubmit(true);
            }
            return;
          }
          if (onSubmit) {
            onSubmit(false);
          }
          message.error('创建失败!');
        }
      },
    });
  };

  /** 更新logtypeinfo */
  const updateLogTypeInfo = (
    index: string,
    properties?: IProperty[],
    conditions?: IFilter[],
    dsl?: string,
  ) => {
    let info = logTypeInfoMap[index];
    if (info) {
      /** 已经存在index */
      if (properties) {
        info.properties = properties || [];
      }
      if (conditions) {
        info.conditions = conditions || [];
      }
      if (dsl !== undefined) {
        info.dsl = dsl;
      }
      setLogTypeInfoMap({
        ...logTypeInfoMap,
        [index]: info,
      });
    } else {
      /** 不存在index */
      info = {
        properties: [],
        conditions: [],
      };
      if (properties) {
        info.properties = properties || [];
      }
      if (conditions) {
        info.conditions = conditions || [];
      }
      if (dsl !== undefined) {
        info.dsl = dsl;
      }
      setLogTypeInfoMap({
        ...logTypeInfoMap,
        [index]: info,
      });
    }
  };

  /** 获得属性 */
  const getLogTypeProperties = (index: string) => {
    return logTypeInfoMap[index]?.properties || [];
  };

  /** 获取过滤条件 */
  const getLogTypeConditions = (index: string) => {
    return logTypeInfoMap[index]?.conditions || [];
  };

  /** 是否可以定时发送 */
  const canRegularSend = useMemo(() => {
    let includeMetadata = false;
    let includeLog = false;
    for (const i of selectedKeys) {
      if (Object.keys(METADATA_TABLE_MAP).includes(i)) {
        includeMetadata = true;
      }
      if (Object.keys(FLOW_LOG_TABLE_MAP).includes(i)) {
        includeLog = true;
      }
      if (includeMetadata || includeLog) {
        setSendingMethod(ESendingMethod.REALTIME);
        break;
      }
    }
    return !includeMetadata && !includeLog;
  }, [selectedKeys]);

  /** 初始化拿到数据 */
  useEffect(() => {
    fetchDataSrouce();
    (async () => {
      if (id) {
        const { success, result } = await querySendUpRuleById(id);
        if (success) {
          const sendRuleContent = JSON.parse(result?.sendRuleContent || '[]');
          if (sendRuleContent.length === 0) {
            return;
          }
          const isRealTime = sendRuleContent[0]?.output_detail_info?.sending_method === 'now';
          setSendingMethod(isRealTime ? ESendingMethod.REALTIME : ESendingMethod.REGULAR);
          setRegName(!!sendRuleContent[0]?.msg_name);
          setLogType(sendRuleContent[0]?.data_type_flag === '1');
          setDataTypeFlag(
            sendRuleContent[0]?.data_type_flag === '1'
              ? EDataTypeFlag.CHECKED
              : EDataTypeFlag.UNCHECKED,
          );
          setWithNodeIp(
            sendRuleContent[0]?.with_node_ip === '1' ? EWithNodeIP.CHECKED : EWithNodeIP.UNCHECKED,
          );
          setSelectedKeys(sendRuleContent.map((c: any) => c?.originIndex || c?.index));
          /** 设置condition和properties */
          const logInfomap = {};
          sendRuleContent.forEach((content: any) => {
            logInfomap[content?.originIndex || content?.index] = {
              properties: content?.originProperties || [],
              conditions: content?.originConditions || [],
              dsl: content?.originDsl || '',
            };
          });
          setLogTypeInfoMap(logInfomap);
          form.setFieldsValue({
            name: result?.name || '',
            interval: !isRealTime ? sendRuleContent[0]?.output_detail_info?.sending_method : '',
          });
        }
      }
    })();
  }, []);

  return (
    <div style={{ marginTop: '30px' }}>
      <Form {...layout} onFinish={onFinish} form={form}>
        <Form.Item
          name={'name'}
          label={'名称'}
          rules={[
            {
              required: true,
              message: '请输入外发规则名称',
            },
          ]}
        >
          <Input placeholder="请输入外发规则名称" />
        </Form.Item>
        <Form.Item label={'发送方式'}>
          <Radio.Group
            value={sendingMethod}
            onChange={(e) => {
              setSendingMethod(e.target.value);
            }}
          >
            <Radio value={ESendingMethod.REALTIME}>实时</Radio>
            <Radio value={ESendingMethod.REGULAR} disabled={!canRegularSend}>
              定时
            </Radio>
          </Radio.Group>
        </Form.Item>
        {sendingMethod === ESendingMethod.REGULAR ? (
          <Form.Item
            label="间隔时间"
            name="interval"
            rules={[{ required: true, message: '必须输入间隔时间' }]}
          >
            <InputNumber
              min={1}
              max={1440}
              style={{ width: '100%' }}
              placeholder="范围1-1440,单位秒"
            />
          </Form.Item>
        ) : (
          ''
        )}
        <Form.Item label={'消息头'}>
          <Checkbox.Group
            value={(() => {
              const res = [];
              if (regName) {
                res.push(EMsgHead.REG_NAME);
              }
              if (logType) {
                res.push(EMsgHead.LOG_TYPE);
              }
              return res;
            })()}
            options={[
              { label: '规则名称', value: EMsgHead.REG_NAME },
              { label: '日志类型', value: EMsgHead.LOG_TYPE },
            ]}
            onChange={(e) => {
              if (e.findIndex((m) => m === EMsgHead.REG_NAME) >= 0) {
                setRegName(true);
              } else {
                setRegName(false);
              }

              if (e.findIndex((m) => m === EMsgHead.LOG_TYPE) >= 0) {
                setLogType(true);
              } else {
                setLogType(false);
              }
            }}
          />
        </Form.Item>
        <Form.Item style={{ width: '100%' }}>
          <Row>
            <Col offset={6}>
              <Checkbox
                checked={withNodeIp === EWithNodeIP.CHECKED}
                onChange={(e) =>
                  setWithNodeIp(e.target.checked ? EWithNodeIP.CHECKED : EWithNodeIP.UNCHECKED)
                }
              >
                外发时带本节点ip信息
              </Checkbox>
            </Col>
          </Row>
        </Form.Item>
        <Form.Item
          label="日志类型"
          name="logType"
          required
          style={{ marginBottom: 0 }}
          {...{ labelCol: { span: 2 }, wrapperCol: { span: 24 } }}
          rules={[
            {
              validator: (rule, value, callback) => {
                if (selectedKeys.length === 0) {
                  callback('至少勾选一个日志类型!');
                }
                callback();
              },
            },
          ]}
        >
          <Tree
            style={{ width: '100%' }}
            checkable
            blockNode={true}
            onCheck={(keys) => {
              setSelectedKeys(keys as string[]);
            }}
            checkedKeys={selectedKeys}
            titleRender={(nodeData) => {
              const { title, key } = nodeData;
              const fomattedTitle = formatTitle(title);
              if (key !== METADATA_PLACEHOLDER && key !== STATISTICS_PLACEHOLDER) {
                const index = selectedKeys?.findIndex((k) => k === key);
                return index >= 0 ? (
                  <LogTypeSelect
                    title={fomattedTitle}
                    index={key}
                    loading={loading}
                    getLogTypeProperties={getLogTypeProperties}
                    updateLogTypeInfo={updateLogTypeInfo}
                    getLogTypeConditions={getLogTypeConditions}
                    dataSource={dataSources[TABLE_MAP[key]]}
                  />
                ) : (
                  fomattedTitle
                );
              }
              return (
                <span
                  onClick={(e) => {
                    e.stopPropagation();
                  }}
                >
                  {fomattedTitle}
                </span>
              );
            }}
            selectable={false}
            treeData={treeData}
          />
        </Form.Item>
        <Row style={{ marginTop: '20px', paddingBottom: '20px' }}>
          <Col offset={2}>
            <Button style={{ marginRight: '10px' }} type="primary" htmlType="submit">
              确定
            </Button>
          </Col>
          <Col>
            <Button
              onClick={() => {
                if (!embed) {
                  history.push('/configuration/third-party/sendup-rules');
                }
                if (onCancel) {
                  onCancel();
                }
              }}
            >
              取消
            </Button>
          </Col>
        </Row>
      </Form>
    </div>
  );
}

export default connect()(RuleForm);
