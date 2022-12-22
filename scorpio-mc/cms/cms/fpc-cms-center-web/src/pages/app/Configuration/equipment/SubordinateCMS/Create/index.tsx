import { ipV4Regex } from '@/utils/utils';
import { Button, Divider, Form, Input } from 'antd';
import TextArea from 'antd/lib/input/TextArea';
import { useCallback } from 'react';
import { connect } from 'dva';
import type { Dispatch } from 'umi';
import { history } from 'umi';

const FormItem = Form.Item;

const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 5 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 16 },
  },
};

const appKeyAndTokenRegex = /^[a-zA-Z0-9-_@]+[^/s]$/;

function CreateLowerCMS(props: any) {
  const { createLowerCMS } = props;
  const [form] = Form.useForm();

  const onFinish = useCallback(
    (e: any) => {
      createLowerCMS(e);
    },
    [createLowerCMS],
  );

  return (
    <Form form={form} onFinish={onFinish}>
      <FormItem
        {...formItemLayout}
        label="设备名称"
        name="name"
        extra="设备名称主要用于显示，便于管理"
        rules={[
          {
            required: true,
            message: '请填写设备名称',
          },
          {
            pattern: /^[\u4E00-\u9FA5_a-zA-Z0-9-@]+$/,
            message: '只能输入汉字、字母、数字、中划线-、下划线_ 和 @ 组合',
          },
          {
            max: 32,
            message: '最多限制32个字符',
          },
        ]}
      >
        <Input placeholder="设备名称不可重复，允许输入汉字、字母、数字、中划线、下划线和 @ 的组合，不超过32个字符" />
      </FormItem>
      <FormItem
        {...formItemLayout}
        label="设备IP"
        name="ip"
        rules={[
          {
            required: true,
            message: '请填写设备ip',
          },
          {
            pattern: ipV4Regex,
            message: '请填写正确的IP地址',
          },
        ]}
      >
        <Input placeholder="请填写设备ip" />
      </FormItem>
      <FormItem
        {...formItemLayout}
        label="管理用户appKey"
        name="appKey"
        rules={[
          {
            required: true,
            message: '请填写设备管理用户appKey',
          },
          {
            pattern: appKeyAndTokenRegex,
            message: '只允许输入字母、数字、中划线-、下划线_ 和 @ 组合',
          },
          {
            max: 32,
            message: '最多限制32个字符',
          },
        ]}
      >
        <Input placeholder="请填写设备管理用户appKey" />
      </FormItem>
      <FormItem
        {...formItemLayout}
        label="管理用户appToken"
        name="appToken"
        rules={[
          {
            required: true,
            message: '请填写设备管理用户appToken',
          },
          {
            pattern: appKeyAndTokenRegex,
            message: '只允许输入字母、数字、中划线-、下划线_ 和 @ 组合',
          },
          {
            max: 32,
            message: '最多限制32个字符',
          },
        ]}
      >
        <Input.Password placeholder="请填写设备管理用户appToken" />
      </FormItem>
      <FormItem
        {...formItemLayout}
        label="描述信息"
        name="description"
        rules={[
          {
            max: 255,
            message: '最多可输入255个字符',
          },
        ]}
      >
        <TextArea rows={4} placeholder="描述信息（可选），最多可输入255个字符" />
      </FormItem>
      <Divider dashed>集群连接配置</Divider>
      <FormItem
        {...formItemLayout}
        label="CMS Token"
        extra="设备配置CMS集群时需要填写"
        rules={[
          {
            required: true,
          },
        ]}
      >
        <Input.Password
          readOnly
          placeholder="允许输入字母、数字、中划线、下划线和 @ 的组合，不超过32个字符"
        />
        ,
      </FormItem>
      <FormItem wrapperCol={{ span: 12, offset: 4 }} style={{ textAlign: 'center' }}>
        <Button className="mr-10" type="primary" htmlType="submit" loading={false}>
          保存
        </Button>
        <Button
          onClick={() => {
            history.goBack();
          }}
        >
          返回
        </Button>
      </FormItem>
    </Form>
  );
}
export default connect(
  () => {
    return {};
  },
  (dispatch: Dispatch) => {
    return {
      createLowerCMS: (payload: any) => {
        return dispatch({ type: 'ConfigurationModel/createLowerSensor', payload });
      },
    };
  },
)(CreateLowerCMS);
