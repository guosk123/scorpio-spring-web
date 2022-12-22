import { Form, Input, InputNumber, Checkbox, Select, Button, Radio, DatePicker, Modal } from 'antd';
import { useEffect, useRef, useState } from 'react';
import { history, connect, Dispatch } from 'umi';
import { validateCompleteAddressList } from '../../../utils/validators';
import {
  ESyslogTransmitType,
  ESyslogTransmitName,
  ESeverity,
  EFacilityType,
  EEncodeType,
  ITransmitSyslog,
} from '../../../typings';
import { upperCase } from 'lodash';
import moment from 'moment';
import { SettingOutlined } from '@ant-design/icons';
import {
  NETWORK_ALARM_DICT,
  SERVICE_ALARM_DICT,
  SYSTEM_ALARM_DICT,
  SYSTEM_LOG_DICT,
} from '../../../dict';

const { Option } = Select;
const { TextArea } = Input;

const Index: React.FC<{
  dispatch: Dispatch;
  details: ITransmitSyslog;
}> = ({ dispatch, details }) => {
  const form = useRef<any>();
  const [transmitType, setTransmitType] = useState<ESyslogTransmitType>(
    ESyslogTransmitType.Instant,
  );
  const [networkAlertRadio, setNetworkAlertRadio] = useState<boolean>(true);
  const [serviceAlertRadio, setServiceAlertRadio] = useState<boolean>(true);
  const [systemAlertRadio, setSystemAlertRadio] = useState<boolean>(true);
  const [systemLogRadio, setSystemLogRadio] = useState<boolean>(true);

  const [showDemo, setShowDemo] = useState<boolean>(false);

  /** 提交表单 */
  const handleSubmit = (params: any) => {
    const {
      sendTime,
      syslogServerAddress,
      networkAlertContent,
      serviceAlertContent,
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
          sendTime: sendTime ? moment(sendTime).format('HH:mm:ss') : '',
          syslogServerAddress: syslogServerAddress.replace(/\n/g, ','),
          networkAlertContent: networkAlertRadio ? networkAlertContent.join(',') : undefined,
          serviceAlertContent: serviceAlertRadio ? serviceAlertContent.join(',') : undefined,
          systemAlarmContent: systemAlarmContent ? systemAlarmContent.join(',') : undefined,
          systemLogContent: systemLogContent ? systemLogContent.join(',') : undefined,
          sendType: transmitType,
        };
        if (details) {
          /** 编辑 */
          dispatch({
            type: 'transmitModel/updateSyslogConfiguration',
            payload: { ...payload, id: details.id },
          });
        } else {
          /** 创建 */
          dispatch({
            type: 'transmitModel/createSyslogConfiguration',
            payload,
          });
        }
      },
    });
  };
  /** 渲染表单内的可选项 */
  const renderTransmitType = () => {
    if (transmitType === ESyslogTransmitType.Timing) {
      return (
        <Form.Item
          label="发送时间"
          name="sendTime"
          rules={[{ required: true, message: '必须选择发送时间范围!' }]}
        >
          <DatePicker placeholder="请选择发送时间" picker={'time'} style={{ width: 200 }} />
        </Form.Item>
      );
    } else if (transmitType === ESyslogTransmitType.Inhibit) {
      return (
        <>
          <Form.Item
            label="间隔时间"
            name="interval"
            rules={[{ required: true, message: '必须选择间隔时间!' }]}
          >
            <InputNumber min={0} style={{ width: '100%' }} placeholder="请输入间隔时间(秒)" />
          </Form.Item>
          <Form.Item
            label="发送数量阀值"
            name="threshold"
            rules={[{ required: true, message: '必须输入发送数量阀值!' }]}
          >
            <InputNumber min={0} style={{ width: '100%' }} placeholder="请输入发送数量阀值" />
          </Form.Item>
        </>
      );
    }
    return '';
  };
  /** 渲染配置后的syslog消息正文示例 */
  const renderSyslogMsgDemo = () => {
    if (form) {
      const { getFieldsValue } = form.current;
      const { facility, severity } = getFieldsValue();
      return (
        <>{`<${
          facility * 8 + severity * 1
        }>2021-11-12 16:17:36 <HOST> FPCCenter[001003]: 配置管理员/3（10.0.0.8）日志内容`}</>
      );
    }
    return '';
  };
  useEffect(() => {
    if (details) {
      const {
        networkAlertContent,
        serviceAlertContent,
        syslogServerAddress,
        sendType,
        sendTime,
        systemAlarmContent,
        systemLogContent,
      } = details;
      const { setFieldsValue } = form.current;
      setTransmitType(sendType);
      setNetworkAlertRadio(networkAlertContent !== null);
      setServiceAlertRadio(serviceAlertContent !== null);
      setFieldsValue({
        ...details,
        sendTime: sendTime ? moment(sendTime, 'HH:mm:ss') : undefined,
        networkAlertContent: networkAlertContent?.split(','),
        serviceAlertContent: serviceAlertContent?.split(','),
        systemAlarmContent: systemAlarmContent?.split(','),
        systemLogContent: systemLogContent?.split(','),
        syslogServerAddress: syslogServerAddress?.replace(/,/g, '\n'),
      });
    }
  }, [details]);
  return (
    <>
      <Form
        name="transmit-syslog-form"
        labelCol={{ span: 5 }}
        wrapperCol={{ span: 16 }}
        ref={form}
        onFinish={handleSubmit}
        style={{ marginTop: 20 }}
      >
        <Form.Item
          label="规则名称"
          name="name"
          rules={[{ required: true, message: '必须输入规则名称' }]}
        >
          <Input style={{ width: '100%' }} allowClear placeholder="请输入规则名称"></Input>
        </Form.Item>
        <Form.Item
          label="Syslog服务器地址"
          name="syslogServerAddress"
          rules={[
            { required: true, message: '必须输入Syslog服务器地址' },
            { validator: validateCompleteAddressList },
          ]}
        >
          <TextArea
            style={{ width: '100%' }}
            allowClear
            rows={5}
            placeholder="请输入IP地址:端口，多个地址之间请换行输入"
          ></TextArea>
        </Form.Item>
        <Form.Item label="发送方式" name="sendType">
          <Radio.Group
            onChange={(e) => {
              setTransmitType(e.target.value);
            }}
            defaultValue={transmitType}
            value={transmitType}
          >
            <Radio value={ESyslogTransmitType.Instant}>
              {ESyslogTransmitName[ESyslogTransmitType.Instant]}
            </Radio>
            <Radio value={ESyslogTransmitType.Timing}>
              {ESyslogTransmitName[ESyslogTransmitType.Timing]}
            </Radio>
            <Radio value={ESyslogTransmitType.Inhibit}>
              {ESyslogTransmitName[ESyslogTransmitType.Inhibit]}
            </Radio>
          </Radio.Group>
        </Form.Item>
        {renderTransmitType()}
        <Form.Item
          label="Syslog等级"
          name="severity"
          rules={[{ required: true, message: '必须选择Syslog等级' }]}
        >
          <Select allowClear placeholder="请选择Syslog等级">
            {(() => {
              const severityList = [];
              for (let key in ESeverity) {
                if (!isNaN(Number(key))) {
                  severityList.push(<Option value={key}>{ESeverity[key]}</Option>);
                }
              }
              return severityList;
            })()}
          </Select>
        </Form.Item>

        <Form.Item
          label="Syslog类型"
          name="facility"
          rules={[{ required: true, message: '必须选择Syslog类型' }]}
        >
          <Select allowClear placeholder="请选择Syslog类型">
            {(() => {
              const facilityTypeList = [];
              for (let key in EFacilityType) {
                if (!isNaN(Number(key))) {
                  facilityTypeList.push(
                    <Option value={key}>{upperCase(EFacilityType[key])}</Option>,
                  );
                }
              }
              return facilityTypeList;
            })()}
          </Select>
        </Form.Item>

        <Form.Item
          label="字符编码"
          name="encodeType"
          rules={[{ required: true, message: '必须选择字符编码' }]}
        >
          <Select allowClear placeholder="请选择字符编码">
            <Option value={EEncodeType['UTF-8']}>UTF-8</Option>
            <Option value={EEncodeType['GB2312']}>GB2312</Option>
          </Select>
        </Form.Item>
        <Form.Item
          label="字段分隔符"
          name="separator"
          rules={[{ max: 1, message: '输入超过限制!' }]}
        >
          <Input placeholder="例如: |+&.~#$等，默认为半角英文逗号"></Input>
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
            查看配置后的Syslog消息正文示例
          </a>
          <div style={{ display: 'flex', position: 'absolute', left: 0 }}>
            <Button style={{ marginRight: '10px' }} type="primary" htmlType="submit">
              确定
            </Button>
            <Button
              onClick={() => {
                history.push('/configuration/transmit/syslog');
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
        width={'40%'}
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
        {showDemo && renderSyslogMsgDemo()}
      </Modal>
    </>
  );
};

export default connect()(Index as any);
