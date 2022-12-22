import { BOOL_YES } from '@/common/dict';
import { queryDeviceInfo } from '@/pages/frame/System/SSO/service';
import { ipV4Regex } from '@/utils/utils';
import { Badge, Button, Form, Input, Modal, Switch } from 'antd';
import { connect } from 'dva';
import { Fragment, useCallback, useEffect, useState } from 'react';
import type { Dispatch } from 'umi';

const FormItem = Form.Item;

const CONNECT_STATUS_OK = '0';
const CONNECT_STATUS_OK_COLOR = '#52c41a';

const CONNECT_STATUS_FAILED = '1';
const CONNECT_STATUS_FAILED_COLOR = '#f5222d';

const formItemLayout = {
  labelCol: { span: 6 },
  wrapperCol: { span: 14 },
};

interface ICMSSetting {
  state?: any;
  cmsIp?: string;
  cmsToken?: string;
  connectStatus?: any;
}

interface Props {
  queryUpperCMSSetting: any;
  createUpperCMSSetting: any;
}

function SuperiorCMSSetting(props: Props) {
  const { queryUpperCMSSetting, createUpperCMSSetting } = props;

  const [settings, setSettings] = useState<ICMSSetting>();

  const [form] = Form.useForm();

  const [deviceInfoFlag, setDeviceInfoFlag] = useState(false);

  const checkDeviceInfo = useCallback(() => {
    queryDeviceInfo().then((res) => {
      const { success, result } = res;
      if (success && !result.deviceName) {
        Modal.warning({
          title: '提示',
          content: (
            <div>
              <span>{'设备信息配置不完整，请联系系统管理员前往：'}</span>
              <br />
              <span>{'系统配置>设备信息，完善信息'}</span>
            </div>
          ),
          okText: '确定',
        });
        setDeviceInfoFlag(false);
      } else {
        setDeviceInfoFlag(true);
      }
    });
  }, []);

  useEffect(() => {
    checkDeviceInfo();
  }, [checkDeviceInfo]);

  useEffect(() => {
    queryUpperCMSSetting().then((result: ICMSSetting) => {
      const tmpSetting = result;
      if (tmpSetting.state) {
        tmpSetting.state = tmpSetting.state === BOOL_YES;
      }
      setSettings(tmpSetting);
    });
  }, [queryUpperCMSSetting]);

  const onFinish = useCallback(
    (e) => {
      checkDeviceInfo();
      if (deviceInfoFlag) {
        createUpperCMSSetting(e);
      }
    },
    [checkDeviceInfo, createUpperCMSSetting, deviceInfoFlag],
  );

  return (
    <Fragment>
      {settings && (
        <Form
          {...formItemLayout}
          initialValues={settings}
          name={'SuperiorCMSSetting'}
          form={form}
          onFinish={onFinish}
        >
          <FormItem
            label="集群管理CMS开启状态"
            name="state"
            valuePropName="checked"
            rules={[
              {
                required: true,
              },
            ]}
          >
            <Switch checkedChildren="开启" unCheckedChildren="关闭" />
          </FormItem>
          <FormItem
            label="集群管理CMS IP"
            name="cmsIp"
            rules={[
              {
                required: true,
                message: '集群管理CMS IP地址',
              },
              {
                pattern: ipV4Regex,
                message: '请填写正确的IP地址',
              },
            ]}
          >
            <Input placeholder="点分十进制IP地址" />
          </FormItem>
          <FormItem
            label="管理CMS节点连接状态"
            // style={{ display: settings.state === BOOL_YES ? 'block' : 'none' }}
          >
            {!settings?.state && <Badge status="default" text="尚未连接" />}
            {settings?.state && settings?.connectStatus === CONNECT_STATUS_OK && (
              <Badge
                status="success"
                text={<span style={{ color: CONNECT_STATUS_OK_COLOR }}>连接成功</span>}
              />
            )}
            {settings?.state && settings?.connectStatus === CONNECT_STATUS_FAILED && (
              <Badge
                status="error"
                text={<span style={{ color: CONNECT_STATUS_FAILED_COLOR }}>连接异常</span>}
              />
            )}
          </FormItem>
          <FormItem wrapperCol={{ span: 12, offset: 6 }}>
            <Button type="primary" htmlType="submit" style={{ marginRight: 10 }}>
              保存
            </Button>
          </FormItem>
        </Form>
      )}
    </Fragment>
  );
}
export default connect(
  () => {
    return {};
  },
  (dispatch: Dispatch) => {
    return {
      queryUpperCMSSetting: () => {
        return dispatch({
          type: 'ConfigurationModel/queryUpperCMSSetting',
        });
      },
      createUpperCMSSetting: (payload: any) => {
        return dispatch({
          type: 'ConfigurationModel/createUpperCMSSetting',
          payload,
        });
      },
    };
  },
)(SuperiorCMSSetting);
