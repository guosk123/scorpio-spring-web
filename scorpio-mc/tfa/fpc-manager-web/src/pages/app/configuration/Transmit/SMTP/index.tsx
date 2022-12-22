import { Form, Input, InputNumber, Select, Button, Col, Modal } from 'antd';
import { useEffect, useRef } from 'react';
import { validateMail, validateSmtpServer } from '../utils/validators';
import type { ITransmitSmtp } from '../typings';
import type { Dispatch } from 'umi';
import { connect } from 'umi';
import type { ConnectState } from '@/models/connect';
import { SettingOutlined } from '@ant-design/icons';

const { Option } = Select;
const Index: React.FC<{
  dispatch: Dispatch;
  transmitSmtp: ITransmitSmtp;
}> = ({ dispatch, transmitSmtp }) => {
  const form = useRef<any>();
  const handleSubmit = (params: ITransmitSmtp) => {
    Modal.confirm({
      width: 500,
      title: '确定保存吗?',
      icon: <SettingOutlined />,
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        dispatch({
          type: 'transmitModel/createSmtpConfiguration',
          payload: params,
        });
      },
    });
  };

  useEffect(() => {
    dispatch({
      type: 'transmitModel/querySmtpConfiguration',
    });
  }, []);
  useEffect(() => {
    const { setFieldsValue } = form.current;
    setFieldsValue(transmitSmtp);
  }, [transmitSmtp]);
  return (
    <>
      <Form
        name="transmit-smtp-form"
        labelCol={{ span: 5 }}
        wrapperCol={{ span: 16 }}
        ref={form}
        onFinish={handleSubmit}
        style={{ marginTop: 20 }}
      >
        <Col offset={5}>
          <div style={{ fontSize: '20px', paddingBottom: '20px' }}>用户信息</div>
        </Col>
        <Form.Item
          label="名称"
          name="mailUsername"
          rules={[{ required: true, message: '必须输入名称' }]}
        >
          <Input style={{ width: '100%' }} allowClear placeholder="请输入名称" />
        </Form.Item>
        <Form.Item
          label="邮件地址"
          name="mailAddress"
          rules={[{ required: true, message: '必须邮箱地址' }, { validator: validateMail }]}
        >
          <Input style={{ width: '100%' }} allowClear placeholder="请输入邮箱地址" />
        </Form.Item>
        <Col offset={5}>
          <div style={{ fontSize: '20px', paddingBottom: '20px' }}>服务器信息</div>
        </Col>
        <Form.Item
          label="邮件服务器"
          name="smtpServer"
          rules={[
            { required: true, message: '必须输入邮件服务器地址' },
          ]}
        >
          <Input style={{ width: '100%' }} allowClear placeholder="请输入邮件服务器" />
        </Form.Item>
        <Form.Item
          label="是否加密"
          name="encrypt"
          rules={[{ required: true, message: '必须选择是否加密' }]}
        >
          <Select style={{ width: '100%' }} placeholder="请选择是否加密">
            <Option key="1" value="1">
              是
            </Option>
            <Option key="0" value="0">
              否
            </Option>
          </Select>
        </Form.Item>
        <Form.Item
          label="服务端口"
          name="serverPort"
          rules={[{ required: true, message: '必须输入服务端口' }]}
        >
          <InputNumber min={0} max={65535} style={{ width: '100%' }} placeholder="请输入服务端口" />
        </Form.Item>
        <Col offset={5}>
          <div style={{ fontSize: '20px', paddingBottom: '20px' }}>登录信息</div>
        </Col>
        <Form.Item
          label="登录用户名"
          name="loginUser"
          rules={[{ required: true, message: '必须输入登录用户名' }]}
        >
          <Input style={{ width: '100%' }} allowClear placeholder="请输入用户名" />
        </Form.Item>
        <Form.Item
          label="登录密码"
          name="loginPassword"
          rules={[{ required: true, message: '必须登录密码' }]}
        >
          <Input.Password style={{ width: '100%' }} allowClear placeholder="请输入邮箱密码" />
        </Form.Item>
        <Form.Item wrapperCol={{ offset: 5, span: 16 }}>
          <div style={{ display: 'flex', position: 'absolute', left: 0, marginTop: '20px' }}>
            <Button type="primary" htmlType="submit" style={{ marginRight: '10px' }}>
              保存
            </Button>
            <Button
              onClick={() => {
                const { getFieldsValue } = form.current;
                const { loginUser, loginPassword, smtpServer } = getFieldsValue();
                dispatch({
                  type: 'transmitModel/testSmtpConnection',
                  payload: {
                    loginUser,
                    loginPassword,
                    smtpServer,
                  },
                });
              }}
            >
              测试
            </Button>
          </div>
        </Form.Item>
      </Form>
    </>
  );
};

export default connect(({ transmitModel: { transmitSmtp } }: ConnectState) => ({
  transmitSmtp,
}))(Index as any);
