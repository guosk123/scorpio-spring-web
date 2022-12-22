import {
  Form,
  Input,
  InputNumber,
  Checkbox,
  Select,
  Button,
  Modal,
  Col,
  Row,
  Divider,
  Tag,
  Descriptions,
  Skeleton,
} from 'antd';
import { useEffect, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import { history, connect } from 'umi';
import { validateMailList } from '../../../utils/validators';
import type { ITransmitMail } from '../../../typings';
import { v1 as uuidv1 } from 'uuid';
import { SettingOutlined } from '@ant-design/icons';
import {
  NETWORK_ALARM_DICT,
  SERVICE_ALARM_DICT,
  SYSTEM_ALARM_DICT,
  SYSTEM_LOG_DICT,
} from '../../../dict';

const { TextArea } = Input;
const { Option } = Select;

const Index: React.FC<{
  dispatch: Dispatch;
  details?: ITransmitMail;
}> = ({ dispatch, details }) => {
  const form = useRef<any>();
  const [networkAlertRadio, setNetworkAlertRadio] = useState<boolean>(true);
  const [serviceAlertRadio, setServiceAlertRadio] = useState<boolean>(true);
  const [systemAlertRadio, setSystemAlertRadio] = useState<boolean>(true);
  const [systemLogRadio, setSystemLogRadio] = useState<boolean>(true);
  const [showDemo, setShowDemo] = useState<boolean>(false);

  const handleSubmit = (params: any) => {
    const {
      networkAlertContent,
      serviceAlertContent,
      receiver,
      cc,
      systemAlarmContent,
      systemLogContent,
    } = params;
    Modal.confirm({
      width: 500,
      title: '确定保存吗?',
      icon: <SettingOutlined />,
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        const payload = {
          ...params,
          receiver: receiver
            .replace(/\n/g, '')
            .split(';')
            .filter((value: string) => value !== '')
            .join(','),
          cc: (cc || '')
            .replace(/\n/g, '')
            .split(';')
            .filter((value: string) => value !== '')
            .join(','),
          networkAlertContent: networkAlertRadio ? networkAlertContent.join(',') : undefined,
          serviceAlertContent: serviceAlertRadio ? serviceAlertContent.join(',') : undefined,
          systemAlarmContent: systemAlarmContent ? systemAlarmContent.join(',') : undefined,
          systemLogContent: systemLogContent ? systemLogContent.join(',') : undefined,
        };
        if (details) {
          /** 编辑 */
          dispatch({
            type: 'transmitModel/updateTransmitMail',
            payload: { ...payload, id: details.id },
          });
        } else {
          /** 创建 */
          dispatch({
            type: 'transmitModel/createTransmitMail',
            payload,
          });
        }
      },
    });
  };
  /** 用于渲染邮件正文示例 */
  const renderMailDemo = () => {
    if (form.current && showDemo) {
      const { getFieldsValue } = form.current;
      const {
        mailTitle,
        cc,
        receiver,
        networkAlertContent,
        serviceAlertContent,
        systemAlarmContent,
        systemLogContent,
      } = getFieldsValue();
      const ccList = cc
        ?.replace(/\n/g, '')
        .split(',')
        .filter((cc: string) => cc !== '');
      const recvList = receiver
        ?.replace(/\n/g, '')
        .split(',')
        .filter((recv: string) => recv !== '');
      return (
        <div style={{ fontSize: '14px' }}>
          <Row>
            <Col style={{ display: 'flex' }}>
              <div style={{ whiteSpace: 'nowrap', paddingRight: '10px' }}>收件人: </div>
              <div>
                {recvList.map((cc: string) => {
                  return <Tag key={uuidv1()}>{cc}</Tag>;
                })}
              </div>
            </Col>
          </Row>
          <Divider style={{ margin: '10px auto' }} />
          <Row>
            <Col style={{ display: 'flex' }}>
              <div style={{ whiteSpace: 'nowrap', paddingRight: '10px' }}>抄送人: </div>
              <div>
                {(ccList || []).map((cc: string) => {
                  return <Tag key={uuidv1()}>{cc}</Tag>;
                })}
              </div>
            </Col>
          </Row>
          <Divider style={{ margin: '10px auto' }} />
          <Row>
            <Col style={{ display: 'flex' }}>
              <div style={{ whiteSpace: 'nowrap', paddingRight: '25px' }}>主题:</div>
              <div>{mailTitle}</div>
            </Col>
          </Row>
          <Divider style={{ margin: '10px auto' }} />
          <Row>
            <Col>
              {networkAlertRadio && (
                <Descriptions title="网络告警信息">
                  {Object.keys(NETWORK_ALARM_DICT).map((key) => {
                    return (
                      <Descriptions.Item
                        label={
                          <span style={{ lineHeight: '45px' }}>{NETWORK_ALARM_DICT[key]}</span>
                        }
                      >
                        <Skeleton paragraph={{ rows: 0 }} />
                      </Descriptions.Item>
                    );
                  })}
                </Descriptions>
              )}
              {serviceAlertRadio && (
                <>
                  <Divider style={{ margin: '10px auto' }} />
                  <Descriptions title="业务告警信息">
                    {Object.keys(SERVICE_ALARM_DICT).map((key) => {
                      return (
                        <Descriptions.Item
                          label={
                            <span style={{ lineHeight: '45px' }}>{SERVICE_ALARM_DICT[key]}</span>
                          }
                        >
                          <Skeleton paragraph={{ rows: 0 }} />
                        </Descriptions.Item>
                      );
                    })}
                  </Descriptions>
                </>
              )}
              {systemAlarmContent && (
                <>
                  <Divider style={{ margin: '10px auto' }} />
                  <Descriptions title="系统告警内容">
                    {Object.keys(SYSTEM_ALARM_DICT).map((key) => {
                      return (
                        <Descriptions.Item
                          label={
                            <span style={{ lineHeight: '45px' }}>{SYSTEM_ALARM_DICT[key]}</span>
                          }
                        >
                          <Skeleton paragraph={{ rows: 0 }} />
                        </Descriptions.Item>
                      );
                    })}
                  </Descriptions>
                </>
              )}
              {systemLogContent && (
                <>
                  <Divider style={{ margin: '10px auto' }} />
                  <Descriptions title="系统日志内容">
                    {Object.keys(SYSTEM_LOG_DICT).map((key) => {
                      return (
                        <Descriptions.Item
                          label={<span style={{ lineHeight: '45px' }}>{SYSTEM_LOG_DICT[key]}</span>}
                        >
                          <Skeleton paragraph={{ rows: 0 }} />
                        </Descriptions.Item>
                      );
                    })}
                  </Descriptions>
                </>
              )}
            </Col>
          </Row>
        </div>
      );
    }
    return '';
  };
  useEffect(() => {
    /** 初始化表单数据 */
    if (details) {
      const { setFieldsValue } = form.current;
      const {
        networkAlertContent,
        serviceAlertContent,
        cc,
        receiver,
        systemAlarmContent,
        systemLogContent,
      } = details;
      setNetworkAlertRadio(networkAlertContent !== null);
      setServiceAlertRadio(serviceAlertContent !== null);
      setFieldsValue({
        ...details,
        cc: cc?.replace(/,/g, ';\n'),
        receiver: receiver?.replace(/,/g, ';\n'),
        networkAlertContent: networkAlertContent?.split(','),
        serviceAlertContent: serviceAlertContent?.split(','),
        systemAlarmContent: systemAlarmContent?.split(','),
        systemLogContent: systemLogContent?.split(','),
      });
    }
  }, []);
  return (
    <>
      <Form
        name="transmit-mail-form"
        labelCol={{ span: 5 }}
        wrapperCol={{ span: 16 }}
        ref={form}
        onFinish={handleSubmit}
        style={{ marginTop: 20 }}
      >
        <Form.Item
          label="邮件主题"
          name="mailTitle"
          rules={[{ required: true, message: '必须输入邮件主题' }]}
        >
          <Input style={{ width: '100%' }} allowClear placeholder="请输入邮件主题" />
        </Form.Item>
        <Form.Item
          label="收件人"
          name="receiver"
          rules={[{ required: true, message: '必须输入收件人' }, { validator: validateMailList }]}
        >
          <TextArea
            style={{ width: '100%' }}
            allowClear
            rows={3}
            placeholder="请输入收件人邮箱地址,多个收件人之间使用,分割"
          />
        </Form.Item>
        <Form.Item label="抄送人" name="cc" rules={[{ validator: validateMailList }]}>
          <TextArea
            style={{ width: '100%' }}
            allowClear
            rows={3}
            placeholder="请输入抄送人邮箱地址,多个收件人之间使用,分割"
          />
        </Form.Item>
        <Form.Item
          label="间隔时间"
          name="interval"
          rules={[{ required: true, message: '必须输入间隔时间' }]}
        >
          <InputNumber
            min={1}
            max={1440}
            style={{ width: '100%' }}
            placeholder="范围1-1440,单位分钟"
          />
        </Form.Item>
        <Form.Item
          style={{ position: 'relative', marginBottom: 0 }}
          labelCol={{ span: 0 }}
          wrapperCol={{ span: 24 }}
        >
          <Form.Item
            label="网络告警消息内容"
            name="networkAlertContent"
            rules={[
              {
                required: networkAlertRadio,
                message: '必须选择网络告警消息内容',
              },
            ]}
            labelCol={{ span: 5 }}
            wrapperCol={{ span: 16 }}
          >
            <Select
              mode="multiple"
              allowClear
              style={{ width: '100%' }}
              placeholder="请选择网络告警消息外发内容字段"
              disabled={!networkAlertRadio}
            >
              {Object.keys(NETWORK_ALARM_DICT).map((key) => (
                <Option value={key}>{NETWORK_ALARM_DICT[key]}</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item style={{ position: 'absolute', right: '8vw', top: 0 }}>
            <Checkbox
              checked={networkAlertRadio}
              onChange={(e) => {
                setNetworkAlertRadio(e.target.checked);
              }}
            />
          </Form.Item>
        </Form.Item>
        <Form.Item
          style={{ position: 'relative', marginBottom: 0 }}
          labelCol={{ span: 0 }}
          wrapperCol={{ span: 24 }}
        >
          <Form.Item
            label="业务告警消息内容"
            name="serviceAlertContent"
            rules={[
              {
                required: serviceAlertRadio,
                message: '必须选择业务告警消息内容',
              },
            ]}
            labelCol={{ span: 5 }}
            wrapperCol={{ span: 16 }}
          >
            <Select
              mode="multiple"
              allowClear
              style={{ width: '100%' }}
              placeholder="请选择业务告警消息外发内容字段"
              disabled={!serviceAlertRadio}
            >
              {Object.keys(SERVICE_ALARM_DICT).map((key) => (
                <Option value={key}>{SERVICE_ALARM_DICT[key]}</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item style={{ position: 'absolute', right: '8vw', top: 0 }}>
            <Checkbox
              checked={serviceAlertRadio}
              onChange={(e) => {
                setServiceAlertRadio(e.target.checked);
              }}
            />
          </Form.Item>
        </Form.Item>

        <Form.Item
          style={{ position: 'relative', marginBottom: 0 }}
          labelCol={{ span: 0 }}
          wrapperCol={{ span: 24 }}
        >
          <Form.Item
            label="系统告警内容"
            name="systemAlarmContent"
            rules={[
              {
                required: systemAlertRadio,
                message: '必须选择系统告警内容',
              },
            ]}
            labelCol={{ span: 5 }}
            wrapperCol={{ span: 16 }}
          >
            <Select
              mode="multiple"
              allowClear
              style={{ width: '100%' }}
              placeholder="请选择系统告警外发内容字段"
              disabled={!systemAlertRadio}
            >
              {Object.keys(SYSTEM_ALARM_DICT).map((key) => (
                <Option value={key}>{SYSTEM_ALARM_DICT[key]}</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item style={{ position: 'absolute', right: '8vw', top: 0 }}>
            <Checkbox
              checked={systemAlertRadio}
              onChange={(e) => {
                setSystemAlertRadio(e.target.checked);
              }}
            />
          </Form.Item>
        </Form.Item>

        <Form.Item
          style={{ position: 'relative', marginBottom: 0 }}
          labelCol={{ span: 0 }}
          wrapperCol={{ span: 24 }}
        >
          <Form.Item
            label="系统日志内容"
            name="systemLogContent"
            rules={[
              {
                required: systemLogRadio,
                message: '必须选择系统日志外发内容字段',
              },
            ]}
            labelCol={{ span: 5 }}
            wrapperCol={{ span: 16 }}
          >
            <Select
              mode="multiple"
              allowClear
              style={{ width: '100%' }}
              placeholder="请选择系统日志内容字段"
              disabled={!systemLogRadio}
            >
              {Object.keys(SYSTEM_LOG_DICT).map((key) => (
                <Option value={key}>{SYSTEM_LOG_DICT[key]}</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item style={{ position: 'absolute', right: '8vw', top: 0 }}>
            <Checkbox
              checked={systemLogRadio}
              onChange={(e) => {
                setSystemLogRadio(e.target.checked);
              }}
            />
          </Form.Item>
        </Form.Item>
        <Form.Item wrapperCol={{ span: 16, offset: 5 }}>
          <a
            style={{ color: '#1890ff', lineHeight: '45px' }}
            onClick={() => {
              const { validateFields } = form.current;
              validateFields().then(() => {
                setShowDemo(true);
              });
            }}
          >
            查看配置后邮件正文示例
          </a>

          <div style={{ display: 'flex', position: 'absolute', left: 0 }}>
            <Button style={{ marginRight: '10px' }} type="primary" htmlType="submit">
              确定
            </Button>
            <Button
              onClick={() => {
                history.push('/configuration/third-party/mail/');
              }}
            >
              取消
            </Button>
          </div>
        </Form.Item>
      </Form>
      <Modal
        title="邮件正文示例"
        visible={showDemo}
        closable={false}
        maskClosable={false}
        onCancel={() => {
          setShowDemo(false);
        }}
        width={'80%'}
        footer={[
          <Button
            onClick={() => {
              setShowDemo(false);
            }}
          >
            返回
          </Button>,
        ]}
      >
        {renderMailDemo()}
      </Modal>
    </>
  );
};

export default connect()(Index as any);
