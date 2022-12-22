import { useEffect, useState } from 'react';
import '@ant-design/compatible/assets/index.css';
import { Input, Button, Spin, Modal, Badge, Switch, Form } from 'antd';
import { ipV4Regex, ipV6Regex, isCidr } from '@/utils/utils';
import styles from './index.less';
import { queryDeviceInfo } from '@/pages/frame/sso/DeviceInfo/service';
import { history } from 'umi';
import { queryCmsSettings, updateCmsSettings } from './service';
import { BOOL_NO, BOOL_YES } from '@/common/dict';

const CONNECT_STATUS_OK = '0';
const CONNECT_STATUS_OK_COLOR = '#52c41a';

const CONNECT_STATUS_FAILED = '1';
const CONNECT_STATUS_FAILED_COLOR = '#f5222d';

const formItemLayout = {
  labelCol: { span: 6 },
  wrapperCol: { span: 14 },
};

export default function CMSSetting() {
  const [haveDeviceInfo, sethaveDeviceInfo] = useState(true);
  const [queryLoading, setQueryLoading] = useState(true);
  const [form] = Form.useForm();

  const checkDeviceInfo = () => {
    queryDeviceInfo().then((res) => {
      const { success, result } = res;
      if (success && !result.deviceName) {
        sethaveDeviceInfo(false);
        Modal.confirm({
          title: '提示',
          content: (
            <div>
              <span>{'设备信息配置不完整，请前往：'}</span>
              <br />
              <span>{'单点登录>设备信息，完善信息'}</span>
            </div>
          ),
          okText: '前往',
          onOk: () => {
            history.push('/system/cluster-setting/device-info');
          },
        });
      }
    });
  };

  const [settings, setSettings] = useState<any>({});

  useEffect(() => {
    setQueryLoading(true);
    checkDeviceInfo();
    queryCmsSettings().then((res) => {
      const { success, result } = res;
      if (success) {
        setSettings(result);
      }
      setQueryLoading(false);
    });
  }, []);

  const handleSubmit = (data: any) => {
    if (!haveDeviceInfo) {
      return;
    }
    Modal.confirm({
      title: '确定保存吗？',
      maskClosable: false,
      keyboard: false,
      autoFocusButton: 'ok',
      onOk: () => {
        updateCmsSettings({ ...data, state: data.state ? BOOL_YES : BOOL_NO }).then(() => {
          setQueryLoading(true);
          queryCmsSettings().then((res) => {
            const { success, result } = res;
            if (success) {
              setSettings(result);
            }
            setQueryLoading(false);
          });
        });
      },
    });
  };
  console.log('settings', settings, queryLoading);

  return (
    <>
      {queryLoading ? (
        <Spin />
      ) : (
        <Form
          {...formItemLayout}
          onFinish={handleSubmit}
          form={form}
          initialValues={{ state: settings.state === BOOL_YES, cmsIp: settings.cmsIp }}
        >
          <Form.Item
            label="集群管理CMS开启状态"
            name="state"
            rules={[{ required: true }]}
            valuePropName="checked"
          >
            <Switch checkedChildren="开启" unCheckedChildren="关闭" />
          </Form.Item>
          <Form.Item
            label="集群管理CMS IP"
            name="cmsIp"
            rules={[
              {
                required: true,
                message: '集群管理CMS IP地址',
              },
              {
                validator: async (rule, value) => {
                  if (!value) {
                    throw new Error('请输入正确的IPv4或IPv6地址');
                  }
                  if (
                    !ipV6Regex.test(value) &&
                    !isCidr(value, 'IPv6') &&
                    !ipV4Regex.test(value) &&
                    !isCidr(value, 'IPv4')
                  ) {
                    throw new Error('请输入正确的IPv4或IPv6地址');
                  }
                },
              },
            ]}
          >
            <Input placeholder="点分十进制IP地址" />
          </Form.Item>

          <Form.Item
            label="管理CMS节点连接状态"
            className={styles.badge}
            style={{ display: settings.state === BOOL_YES ? 'flex' : 'none' }}
          >
            {!settings.connectStatus && <Badge status="default" text="尚未连接" />}
            {settings.connectStatus === CONNECT_STATUS_OK && (
              <Badge
                status="success"
                text={<span style={{ color: CONNECT_STATUS_OK_COLOR }}>连接成功</span>}
              />
            )}
            {settings.connectStatus === CONNECT_STATUS_FAILED && (
              <Badge
                status="error"
                text={<span style={{ color: CONNECT_STATUS_FAILED_COLOR }}>连接异常</span>}
              />
            )}
          </Form.Item>
          <Form.Item wrapperCol={{ span: 12, offset: 6 }}>
            <Button type="primary" htmlType="submit" style={{ marginRight: 10 }}>
              保存
            </Button>
          </Form.Item>
        </Form>
      )}
    </>
  );
}
