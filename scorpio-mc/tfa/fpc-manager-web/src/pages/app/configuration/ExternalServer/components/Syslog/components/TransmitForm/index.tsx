import { Form, Input, Button, Modal, message, Select, InputNumber } from 'antd';
import { useEffect } from 'react';
import { history } from 'umi';
import { SettingOutlined } from '@ant-design/icons';
import { validateIp } from '@/pages/app/configuration/Transmit/utils/validators';
import { EReceiverType } from '../../../../typings';
import { useForm } from 'antd/lib/form/Form';
import { EFacilityType, ESeverity } from '../../typing';
import { upperCase } from 'lodash';
import { EEncodeType } from '@/pages/app/configuration/Transmit/typings';
import { createTransmitSyslog, queryTransmitSyslogById, updateTransmitSyslog } from './service';

const { Option } = Select;

export default function TransmitForm({
  syslogId,
  embed = false,
  onSubmit,
  onCancel,
}: {
  syslogId?: string;
  embed?: boolean;
  onSubmit?: (success: boolean) => void;
  onCancel?: () => void;
}) {
  const [form] = useForm<any>();

  useEffect(() => {
    (async () => {
      if (syslogId) {
        const { success, result } = await queryTransmitSyslogById(syslogId);
        if (success) {
          form.setFieldsValue({
            ...result,
          });
        }
      }
    })();
  }, []);

  const handleSubmit = (params: any) => {
    Modal.confirm({
      width: 500,
      title: '确定保存吗?',
      icon: <SettingOutlined />,
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        const payload = {
          id: syslogId,
          name: params?.name,
          receiverContent: JSON.stringify({
            syslogServerIpAddress: params?.syslogServerIpAddress,
            syslogServerPort: params?.syslogServerPort,
            protocol: params?.protocol,
            severity: params?.severity,
            facility: params?.facility,
            encodeType: params?.encodeType,
            separator: params?.separator,
          }),
          receiverType: EReceiverType.SYSLOG,
        };
        if (syslogId) {
          const { success } = await updateTransmitSyslog(payload);
          if (success) {
            message.success('编辑成功!');
            if (!embed) {
              history.push('/configuration/third-party/external-server/syslog/list');
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
          const { success } = await createTransmitSyslog(payload);
          if (success) {
            message.success('创建成功！');
            if (!embed) {
              history.push('/configuration/third-party/external-server/syslog/list');
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
        }
      },
    });
  };

  return (
    <>
      <Form
        name="transmit-mail-form"
        labelCol={{ span: 5 }}
        wrapperCol={{ span: 16 }}
        form={form}
        onFinish={handleSubmit}
        style={{ marginTop: 20 }}
      >
        <Form.Item label="名称" name="name" rules={[{ required: true, message: '必须输入名称' }]}>
          <Input
            disabled={syslogId !== undefined}
            style={{ width: '100%' }}
            allowClear
            placeholder="请输入Syslog名称"
          />
        </Form.Item>

        <Form.Item
          label="协议"
          name="protocol"
          rules={[{ required: true, message: '必须选择协议' }]}
        >
          <Select placeholder="请选择协议">
            <Option value={'TCP'}>TCP</Option>
            <Option value={'UDP'}>UDP</Option>
          </Select>
        </Form.Item>

        <Form.Item
          label="IP地址"
          name="syslogServerIpAddress"
          rules={[{ required: true, message: '必须输入IP地址' }, { validator: validateIp }]}
        >
          <Input style={{ width: '100%' }} allowClear placeholder="请输入IP地址" />
        </Form.Item>

        <Form.Item
          label="端口"
          name="syslogServerPort"
          rules={[
            {
              required: true,
              message: '请输入端口',
            },
          ]}
        >
          <InputNumber placeholder="请输入端口" precision={0} min={0} max={65535} style={{ width: '100%' }} />
        </Form.Item>

        <Form.Item
          label="Syslog等级"
          name="severity"
          rules={[{ required: true, message: '必须选择Syslog等级' }]}
        >
          <Select allowClear placeholder="请选择Syslog等级">
            {(() => {
              const severityList = [];
              for (const key in ESeverity) {
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
              for (const key in EFacilityType) {
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
            <Option value={EEncodeType.GB2312}>GB2312</Option>
          </Select>
        </Form.Item>
        <Form.Item
          label="字段分隔符"
          name="separator"
          rules={[{ max: 1, message: '输入超过限制!' }]}
        >
          <Input placeholder="例如: |+&.~#$等，默认为半角英文逗号" />
        </Form.Item>

        <Form.Item wrapperCol={{ span: 16, offset: 5 }}>
          <div style={{ display: 'flex', position: 'absolute', left: 0 }}>
            <Button style={{ marginRight: '10px' }} type="primary" htmlType="submit">
              确定
            </Button>
            <Button
              onClick={() => {
                if (!embed) {
                  history.push('/configuration/third-party/external-server/syslog/list');
                }
                if (onCancel) {
                  onCancel();
                }
              }}
            >
              取消
            </Button>
          </div>
        </Form.Item>
      </Form>
    </>
  );
}
