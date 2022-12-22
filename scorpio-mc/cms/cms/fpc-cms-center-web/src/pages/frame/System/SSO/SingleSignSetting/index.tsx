import { Select, Button, Form, Badge, Checkbox, message, TreeSelect } from 'antd';
import type { Dispatch } from 'dva';
import { connect } from 'dva';
import { Fragment, useCallback, useEffect, useState } from 'react';
import { queryDeviceList, queryFpcLoginSetting, updateFpcLoginSetting } from '../service';
import type { ISystemUser } from '../typings';
import { EUSERTYPE } from '../typings';
import { USER_LOCKED_FALSE, USER_LOCKED_TRUE } from '../User/Create';

const FormItem = Form.Item;

const { Option } = Select;

const formItemLayout = {
  labelCol: { span: 6 },
  wrapperCol: { span: 14 },
};

interface ILoginSetting {
  deviceId?: string;
  deviceName?: string;
  userId?: string;
  fpcId?: any;
}

interface Props {
  allSystemUsers: ISystemUser[];
  dispatch: Dispatch;
}

export interface serialItem {
  deviceSerialNumber?: string;
  deviceName?: string;
  deviceType?: any; // 0:CMS；1：TFA
  owner?: null;
  sensorType?: null;
  child?: serialItem[];
}

export const serialListToTree = (list: serialItem) => {
  const tmpChild: any = [];
  if (list?.child?.length) {
    list.child.forEach((item) => {
      tmpChild.push(serialListToTree(item));
    });
  }
  if (tmpChild.length) {
    return {
      title: list.deviceName,
      value: list.deviceSerialNumber,
      key: list.deviceSerialNumber,
      children: tmpChild,
    };
  }
  return {
    title: list?.deviceName,
    value: list?.deviceSerialNumber,
    key: list?.deviceSerialNumber,
  };
};

function SingleSignSetting(props: Props) {
  const { allSystemUsers, dispatch } = props;
  const [serialList, setSerialList] = useState<any>([]);

  useEffect(() => {
    queryDeviceList().then((res) => {
      const { success, result } = res;
      if (success) {
        const treeData: any = serialListToTree(result);
        if (treeData) {
          treeData.disabled = true;
        }
        setSerialList(treeData);
      }
    });
    dispatch({
      type: 'ssoModel/queryAllSystemUsers',
    });
  }, [dispatch]);

  const [settings, setSettings] = useState<ILoginSetting>();
  const [isAllFpc, setIsAllFpc] = useState(false);

  useEffect(() => {
    queryFpcLoginSetting().then((res) => {
      const { success, result } = res;
      if (success) {
        if (result.deviceSerialNumbers === 'ALL') {
          setIsAllFpc(true);
          setSettings({
            ...result,
            deviceSerialNumbers: undefined,
            isAllFpc: true,
          });
        } else {
          setSettings({
            ...result,
            deviceSerialNumbers: result.deviceSerialNumbers?.length
              ? result.deviceSerialNumbers.split(',')
              : undefined,
          });
        }
      }
    });
  }, []);

  const [form] = Form.useForm();

  const onFinish = useCallback((e) => {
    const payload = {
      deviceId: e.deviceId,
      deviceName: e.deviceName,
      userId: e.userId,
      deviceSerialNumbers: e.isAllFpc ? 'ALL' : e.deviceSerialNumbers.join(','),
    };
    updateFpcLoginSetting(payload).then((res) => {
      const { success } = res;
      if (success) {
        message.info('保存成功');
      } else {
        message.info('保存失败');
      }
    });
  }, []);

  return (
    <Fragment>
      {settings && (
        <Form
          style={{ marginTop: 40 }}
          {...formItemLayout}
          initialValues={settings}
          name={'SingleSignSetting'}
          form={form}
          onFinish={onFinish}
        >
          <FormItem
            label="可登录用户"
            name="userId"
            rules={[
              {
                required: true,
                message: '请选择用户',
              },
            ]}
          >
            <Select>
              {allSystemUsers
                .filter((item) => item.userType === EUSERTYPE.SIMPLEUSER)
                .map((user) => (
                  <Option value={user.id} disabled={user.locked === USER_LOCKED_TRUE}>
                    <Fragment>
                      <Badge status={user.locked === USER_LOCKED_FALSE ? 'success' : 'error'} />
                      {user.fullname}
                    </Fragment>
                  </Option>
                ))}
            </Select>
          </FormItem>
          <FormItem
            label="作用于全部设备"
            name="isAllFpc"
            valuePropName={'checked'}
            rules={[{ required: isAllFpc }]}
          >
            <Checkbox
              onChange={(e) => {
                const { target } = e;
                setIsAllFpc(target.checked);
              }}
            />
          </FormItem>
          <FormItem
            label="可登录设备"
            name="deviceSerialNumbers"
            rules={[
              {
                required: !isAllFpc,
                message: '请选择设备',
              },
            ]}
          >
            <TreeSelect
              treeData={[serialList]}
              treeDefaultExpandAll
              multiple
              showSearch
              disabled={isAllFpc}
            />
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
export default connect((state: any) => {
  const {
    ssoModel: { allSystemUsers },
  } = state;
  return { allSystemUsers };
})(SingleSignSetting);
