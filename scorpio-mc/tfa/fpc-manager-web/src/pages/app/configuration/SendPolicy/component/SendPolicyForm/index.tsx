import {
  Button,
  Col,
  Form,
  Input,
  Row,
  Select,
  Checkbox,
  Space,
  TreeSelect,
  Modal,
  message,
  Menu,
  Dropdown,
  Alert,
} from 'antd';
import { useForm } from 'antd/lib/form/Form';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { history } from 'umi';
import RuleForm from '../../../SendUpRules/component/RuleForm';
import { queryTransmitRules } from '../../../SendUpRules/service';
import {
  createSendPolicy,
  getSmtpConfiguration,
  queryExternalReceiver,
  querySendPolicyById,
  updateSendPolicy,
} from './service';
import KafkaTransmitForm from '../../../ExternalServer/components/Kafka/components/TransmitForm';
import MailTransmitForm from '../../../ExternalServer/components/Mail/components/TransmitForm';
import SyslogTransmitForm from '../../../ExternalServer/components/Syslog/components/TransmitForm';
import ZmqTransmitForm from '../../../ExternalServer/components/Zmq/components/TransmitForm';
import { EReceiverType } from '../../../ExternalServer/typings';

const { Option } = Select;

const layout = {
  labelCol: { span: 6 },
  wrapperCol: { span: 12 },
};

enum ESmtpStatus {
  'CONFIGURED' = '0',
  'UNCONFIGURED' = '1',
}

export default function SendPolicyForm({ id }: { id?: string }) {
  const [form] = useForm();
  const [state, setState] = useState<boolean>(false);
  const [selectedReceiver, setSelectedReceiver] = useState<string>('');
  const [externalReceiverList, setExternalReceiverList] = useState<Record<string, any[]>>({});
  const [sendUpRules, setSendUpRules] = useState<any[]>([]);
  const [modalType, setModalType] = useState<
    'mail' | 'syslog' | 'kafka' | 'zmq' | 'rule' | undefined
  >(undefined);
  const [smtpStatus, setSmtpStatus] = useState<ESmtpStatus>(ESmtpStatus.CONFIGURED);
  const onFinish = async (e: any) => {
    const params = {
      ...e,
      state: state ? '1' : '0',
    };
    Modal.confirm({
      title: '确定提交吗?',
      cancelText: '取消',
      okText: '确定',
      onOk: async () => {
        if (id) {
          const { success } = await updateSendPolicy({ id, ...params });
          if (success) {
            message.success('编辑成功!');
            history.push('/configuration/third-party/send-policy');
            return;
          }
          message.error('编辑失败!');
        } else {
          const { success } = await createSendPolicy(params);
          if (success) {
            message.success('创建成功!');
            history.push('/configuration/third-party/send-policy');
            return;
          }
          message.error('创建失败!');
        }
      },
    });
  };

  const fetchExternalReceivers = async () => {
    const { success, result } = await queryExternalReceiver();
    if (success) {
      setExternalReceiverList(result);
    }
  };

  const fetchTransmitRules = async () => {
    const { success, result } = await queryTransmitRules();
    if (success) {
      setSendUpRules(result);
    }
  };

  useEffect(() => {
    fetchExternalReceivers();
    fetchTransmitRules();
    /** 初始化 */
    if (id) {
      (async () => {
        const { success, result } = await querySendPolicyById(id);
        if (success) {
          form.setFieldsValue(result);
          setState(result?.state === '1');
        }
      })();
    }
  }, []);

  useEffect(() => {
    (async () => {
      const { success, result } = await getSmtpConfiguration();
      if (success && result.id) {
        setSmtpStatus(ESmtpStatus.CONFIGURED);
      } else {
        setSmtpStatus(ESmtpStatus.UNCONFIGURED);
      }
    })();
  }, []);

  const ruleDisable = useCallback(
    (rule: any) => {
      if (!selectedReceiver) {
        return false;
      }

      const sendRuleContent = JSON.parse(rule?.sendRuleContent || '[]');
      /** 找到父节点 */
      let parentIndex = '';
      for (const key in externalReceiverList) {
        const list = externalReceiverList[key];
        if (list.findIndex((l) => l.id === selectedReceiver) >= 0) {
          parentIndex = key;
          break;
        }
      }
      if (parentIndex === 'mail') {
        /** 如果server是mail 则 规则只能选四个告警 */
        for (const { index } of sendRuleContent) {
          if (index !== 'alert' && index !== 'systemAlert' && index !== 'systemLog') {
            return true;
          }
        }
      }
      if (parentIndex === 'kafka' || parentIndex === 'zmq') {
        /** 如果server是kafka 则 规则不能是 系统告警和系统日志 */
        for (const { index } of sendRuleContent) {
          if (index === 'systemAlert' || index === 'systemLog') {
            return true;
          }
        }
      }
      return false;
    },
    [externalReceiverList, selectedReceiver],
  );

  const showSmtpAlert = useMemo(() => {
    if (!selectedReceiver) {
      return false;
    }
    /** 找到父节点 */
    let parentIndex = '';
    for (const key in externalReceiverList) {
      const list = externalReceiverList[key];
      if (list.findIndex((l) => l.id === selectedReceiver) >= 0) {
        parentIndex = key;
        break;
      }
    }
    if (parentIndex === 'mail') {
      return !!(true && smtpStatus === ESmtpStatus.UNCONFIGURED);
    }
    return false;
  }, [externalReceiverList, selectedReceiver, smtpStatus]);

  return (
    <>
      {showSmtpAlert ? (
        <>
          <Alert
            message="未配置SMTP，可能导致外发出现问题!"
            type="warning"
            action={
              <Space>
                <Button
                  size="small"
                  type="link"
                  onClick={() => {
                    history.push('/configuration/third-party/smtp');
                  }}
                >
                  去配置
                </Button>
              </Space>
            }
          />
        </>
      ) : (
        ''
      )}
      <div style={{ marginTop: '30px' }}>
        <Form {...layout} onFinish={onFinish} form={form}>
          <Form.Item
            name={'name'}
            label={'名称'}
            rules={[
              {
                required: true,
                message: '请输入外发策略名称',
              },
            ]}
          >
            <Input disabled={!!id} placeholder="请输入外发策略名称" style={{ width: '45vw' }} />
          </Form.Item>

          <Form.Item label="外发服务器" required>
            <Space>
              <Form.Item
                name="externalReceiverId"
                noStyle
                rules={[
                  {
                    required: true,
                    message: '请选择外发服务器',
                  },
                ]}
              >
                <TreeSelect
                  // value={value}
                  dropdownStyle={{ maxHeight: 400, overflow: 'auto' }}
                  treeData={Object.keys(externalReceiverList).map((key) => {
                    let title = '';
                    if (key === 'mail') {
                      title = '邮件服务器';
                    } else if (key === 'syslog') {
                      title = 'SYSLOG服务器';
                    } else if (key === 'kafka') {
                      title = 'KAFKA服务器';
                    } else if (key === 'zmq') {
                      title = 'ZMQ服务器';
                    }
                    return {
                      title,
                      value: key,
                      selectable: false,
                      children:
                        externalReceiverList[key]?.map((item) => ({
                          title: item?.name,
                          value: item?.id,
                        })) || [],
                    };
                  })}
                  placeholder="请选择外发服务器"
                  style={{ width: '45vw' }}
                  onChange={(e) => {
                    setSelectedReceiver(e);
                    form.setFieldsValue({
                      sendRuleId: '',
                    });
                  }}
                />
              </Form.Item>
              <Dropdown
                overlay={
                  <Menu
                    onClick={({ key }) => {
                      setModalType(key as any);
                    }}
                  >
                    <Menu.Item key={'mail'}>邮件外发</Menu.Item>
                    <Menu.Item key={'syslog'}>SYSLOG服务器</Menu.Item>
                    <Menu.Item key={'kafka'}>KAFKA服务器</Menu.Item>
                    <Menu.Item key={'zmq'}>ZMQ服务器</Menu.Item>
                  </Menu>
                }
                placement="topCenter"
              >
                <Button type="link">新建外发服务器</Button>
              </Dropdown>
            </Space>
          </Form.Item>

          <Form.Item label="外发规则" required>
            <Space>
              <Form.Item
                name="sendRuleId"
                noStyle
                rules={[
                  {
                    required: true,
                    message: '请选择外发规则',
                  },
                ]}
              >
                <Select placeholder="请选择外发规则" style={{ width: '45vw' }}>
                  {sendUpRules.map((rule) => {
                    const disabled = ruleDisable(rule);
                    return (
                      <Option disabled={disabled} value={rule.id}>
                        {rule.name}
                      </Option>
                    );
                  })}
                </Select>
              </Form.Item>
              <Button
                type="link"
                onClick={() => {
                  setModalType('rule');
                }}
              >
                新建外发规则
              </Button>
            </Space>
          </Form.Item>

          <Form.Item name={'state'} label={'启用'}>
            <Checkbox checked={state} onChange={(e) => setState(e.target.checked)} />
          </Form.Item>

          <Row style={{ marginTop: '20px' }}>
            <Col offset={6}>
              <Button style={{ marginRight: '10px' }} type="primary" htmlType="submit">
                确定
              </Button>
            </Col>
            <Col>
              <Button
                onClick={() => {
                  history.push('/configuration/third-party/send-policy');
                }}
              >
                取消
              </Button>
            </Col>
          </Row>
        </Form>
      </div>
      <Modal
        title={(() => {
          switch (modalType) {
            case 'kafka':
              return '新建KAFKA服务器';
            case 'mail':
              return '新建邮件外发';
            case 'syslog':
              return '新建SYSLOG服务器';
            case 'zmq':
              return '新建ZMQ服务器';
            case 'rule':
              return '新建外发规则';
            case undefined:
              return '';
          }
          return '';
        })()}
        destroyOnClose
        width={'90%'}
        closable={false}
        footer={null}
        visible={!!modalType}
      >
        {(() => {
          switch (modalType) {
            case 'kafka':
              return (
                <KafkaTransmitForm
                  embed
                  onSubmit={(success: boolean) => {
                    if (success) {
                      fetchExternalReceivers();
                      setModalType(undefined);
                    }
                  }}
                  onCancel={() => {
                    setModalType(undefined);
                  }}
                />
              );
            case 'mail':
              return (
                <MailTransmitForm
                  embed
                  onSubmit={(success: boolean) => {
                    if (success) {
                      fetchExternalReceivers();
                      setModalType(undefined);
                    }
                  }}
                  onCancel={() => {
                    setModalType(undefined);
                  }}
                />
              );
            case 'syslog':
              return (
                <SyslogTransmitForm
                  embed
                  onSubmit={(success: boolean) => {
                    if (success) {
                      fetchExternalReceivers();
                      setModalType(undefined);
                    }
                  }}
                  onCancel={() => {
                    setModalType(undefined);
                  }}
                />
              );
            case 'zmq':
              return (
                <ZmqTransmitForm
                  embed
                  onSubmit={(success: boolean) => {
                    if (success) {
                      fetchExternalReceivers();
                      setModalType(undefined);
                    }
                  }}
                  onCancel={() => {
                    setModalType(undefined);
                  }}
                />
              );
            case 'rule':
              return (
                <RuleForm
                  embed
                  onSubmit={(success: boolean) => {
                    if (success) {
                      fetchTransmitRules();
                      setModalType(undefined);
                    }
                  }}
                  onCancel={() => {
                    setModalType(undefined);
                  }}
                />
              );
            case undefined:
              return;
          }
          return '';
        })()}
      </Modal>
    </>
  );
}
