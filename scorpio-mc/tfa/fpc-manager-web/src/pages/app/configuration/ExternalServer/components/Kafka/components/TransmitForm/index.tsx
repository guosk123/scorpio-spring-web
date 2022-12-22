import { Form, Input, Button, Modal, message, Select, Upload, InputNumber } from 'antd';
import { useEffect, useState } from 'react';
import { history } from 'umi';
import { SettingOutlined } from '@ant-design/icons';
import { validateCompleteAddressList } from '@/pages/app/configuration/Transmit/utils/validators';
import { useForm } from 'antd/lib/form/Form';
import { API_BASE_URL, API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import { getCookie } from '@/utils/frame/cookie';
import { queryTransmitkafkaById, updateTransmitkafka } from './service';
import { ECertification } from '../../typing';
import { createTransmitkafka } from './service';
import { EReceiverType } from '../../../../typings';
import { Icon as LegacyIcon } from '@ant-design/compatible';

const { TextArea } = Input;
const { Option } = Select;

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

export default function TransmitForm({
  kafkaId,
  embed = false,
  onSubmit,
  onCancel,
}: {
  kafkaId?: string;
  embed?: boolean;
  onSubmit?: (success: boolean) => void;
  onCancel?: () => void;
}) {
  const [form] = useForm<any>();
  const [certification, setCertification] = useState<ECertification>(ECertification.NULL);
  const [keytabFile, setKeytabFile] = useState<any[]>([]);

  useEffect(() => {
    (async () => {
      if (kafkaId) {
        const { success, result } = await queryTransmitkafkaById(kafkaId);
        if (success) {
          const path = [
            {
              name: result?.keytabFilePath,
              uid: result?.keytabFilePath,
              url: result?.keytabFilePath,
            },
          ];
          form.setFieldsValue({
            kafkaId,
            name: result?.name,
            ...result,
            keytabFilePath: path,
            kafkaServerAddress: result?.kafkaServerAddress?.replace(/,/g,'\n'),
          });
          setKeytabFile(path);
          setCertification(result?.kerberosCertification);
        }
      }
    })();
  }, []);

  const [uploadLoading, setUploadLoading] = useState<boolean>(false);

  const handleFileChange = (info: any) => {
    const { status } = info.file;
    setKeytabFile([
      {
        name: info.file.response,
        uid: info.file.response,
        url: info.file.response,
      },
    ]);
    if (status === 'done' || status === 'removed' || status === 'error') {
      setUploadLoading(false);
    } else {
      setUploadLoading(true);
    }

    if (status === 'error') {
      message.error('上传失败');
      return [];
    }
    if (Array.isArray(info)) {
      return info;
    }
    if (info && info.fileList) {
      const result = info.fileList.slice(-1).map((item: any) => ({
        uid: item.uid,
        url: item.response,
        name: item.name,
      }));
      return result;
    }

    return [];
  };

  const handleSubmit = (params: any) => {
    Modal.confirm({
      width: 500,
      title: '确定保存吗?',
      icon: <SettingOutlined />,
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        const payload = {
          id: kafkaId,
          name: params?.name,
          receiverContent: JSON.stringify({
            kafkaServerAddress: params?.kafkaServerAddress?.replace(/\n/g,','),
            kafkaServerTopic: params?.kafkaServerTopic,
            kerberosCertification: params?.kerberosCertification,
            keytabFilePath: keytabFile[0]?.url,
            keyRestoreTime: params?.keyRestoreTime,
            saslKerberosServiceName: params?.saslKerberosServiceName,
            saslKerberosPrincipal: params?.saslKerberosPrincipal,
            securityProtocol: params?.securityProtocol,
            authenticationMechanism: params?.authenticationMechanism,
          }),
          receiverType: EReceiverType.KAFKA,
        };
        if (kafkaId) {
          const { success } = await updateTransmitkafka(payload);
          if (success) {
            message.success('编辑成功!');
            if (!embed) {
              history.push('/configuration/third-party/external-server/kafka/list');
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
          const { success } = await createTransmitkafka(payload);
          if (success) {
            message.success('创建成功！');
            if (!embed) {
              history.push('/configuration/third-party/external-server/kafka/list');
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
            disabled={kafkaId !== undefined}
            style={{ width: '100%' }}
            allowClear
            placeholder="请输入Kafka名称"
          />
        </Form.Item>
        <Form.Item
          label="节点地址"
          name="kafkaServerAddress"
          rules={[
            {
              required: true,
              message: '请填写节点地址',
            },
            {
              validator: validateCompleteAddressList,
            },
          ]}
          extra={'仅限输入IP加端口的地址，例如：A.B.C.D:PORT。多个地址请用换行符分割'}
        >
          <TextArea rows={4} placeholder="请填写节点地址" />
        </Form.Item>
        <Form.Item
          label="topic"
          name="kafkaServerTopic"
          rules={[
            {
              required: true,
              message: '请填写 Kafka Topic',
            },
          ]}
        >
          <Input placeholder="请填写 Kafka Topic" />
        </Form.Item>
        <Form.Item
          label="认证方式"
          name="kerberosCertification"
          initialValue={ECertification.NULL}
          rules={[
            {
              required: true,
              message: '请选择认证方式',
            },
          ]}
        >
          <Select
            onChange={(e) => {
              setCertification(e);
            }}
          >
            <Option value={ECertification.NULL}>无</Option>
            <Option value={ECertification.KERBEROS}>KERBEROS</Option>
          </Select>
        </Form.Item>
        {certification === '1' ? (
          <>
            <Form.Item
              label="keytab文件"
              name="keytabFilePath"
              initialValue={keytabFile || []}
              valuePropName={'fileList'}
              getValueFromEvent={handleFileChange}
              rules={[
                {
                  required: true,
                  message: '请导入keytab文件',
                },
              ]}
            >
              {/* @ts-ignore */}
              <Upload {...UploadProps}>
                <Button>
                  <LegacyIcon type={uploadLoading ? 'loading' : 'upload'} />
                  {uploadLoading ? '上传中' : '导入文件'}
                </Button>
              </Upload>
            </Form.Item>

            <Form.Item
              label="key尝试恢复时间"
              name="keyRestoreTime"
              rules={[
                {
                  required: true,
                  message: '请填写key尝试恢复时间',
                },
              ]}
            >
              <InputNumber style={{ minWidth: 200 }} precision={0} min={1} addonAfter="ms" />
            </Form.Item>

            <Form.Item
              label="sasl.kerberos.service.name"
              name="saslKerberosServiceName"
              rules={[
                {
                  required: true,
                  whitespace: true,
                  message: '请填写sasl.kerberos.service.name',
                },
              ]}
            >
              <Input placeholder="请填写sasl.kerberos.service.name" />
            </Form.Item>

            <Form.Item
              label="sasl.kerberos.principal"
              name="saslKerberosPrincipal"
              rules={[
                {
                  required: true,
                  whitespace: true,
                  message: '请填写sasl.kerberos.principal',
                },
              ]}
            >
              <Input placeholder="请填写sasl.kerberos.principal" />
            </Form.Item>
            <Form.Item
              label="安全协议"
              name="securityProtocol"
              rules={[
                {
                  required: true,
                  whitespace: true,
                  message: '请填写安全协议',
                },
              ]}
              initialValue="sasl_plaintext"
            >
              <Input disabled placeholder="请填写安全协议" />
            </Form.Item>
            <Form.Item
              label="鉴权机制"
              name="authenticationMechanism"
              initialValue={'GSSAPI'}
              rules={[
                {
                  required: true,
                  whitespace: true,
                  message: '请填写鉴权机制',
                },
              ]}
            >
              <Input disabled placeholder="请填写鉴权机制" />
            </Form.Item>
          </>
        ) : (
          ''
        )}

        <Form.Item wrapperCol={{ span: 16, offset: 5 }}>
          <div style={{ display: 'flex', position: 'absolute', left: 0 }}>
            <Button style={{ marginRight: '10px' }} type="primary" htmlType="submit">
              确定
            </Button>
            <Button
              onClick={() => {
                if (!embed) {
                  history.push('/configuration/third-party/external-server/kafka/list');
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
