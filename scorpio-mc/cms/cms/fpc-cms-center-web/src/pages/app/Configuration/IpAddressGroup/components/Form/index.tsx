import React, { useEffect } from 'react';
import type { ConnectState } from '@/models/connect';
import { connect } from 'dva';
import type { Dispatch } from 'umi';
import { Button, Form, Input } from 'antd';
import { history } from 'umi';
import {
  createConfirmModal,
  ip2number,
  ipV4Regex,
  ipV6Regex,
  updateConfirmModal,
} from '@/utils/utils';
import type { IpAddressGroup } from '../../typings';

interface IpAddressGroupFormProps {
  dispatch: Dispatch;
  operateType: string;
  detail?: IpAddressGroup;
  submitLoading: boolean | undefined;
}
const formLayout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 16 },
};
const IP_MAX_NUMBER = 50;

const IpAddressGroupForm: React.FC<IpAddressGroupFormProps> = ({
  dispatch,
  operateType,
  detail,
  submitLoading,
}) => {
  const [form] = Form.useForm();

  useEffect(() => {
    if (detail) {
      console.log(detail, 'detail!!!');
      form.setFieldsValue({
        ...detail,
        ipAddress: detail.ipAddress && detail.ipAddress.replace(/,/g, '\n'),
      });
    }
  }, [form, detail]);
  /**
   * 检测每行输入的ip地址
   * 保证不重复即可
   * 不用管地址段之间是否有重叠
   */
  const checkTextAreaIp = (_: any, value: any) => {
    // return Promise.reject(new Error(`错误`));
    if (value) {
      const passIpArr: any[] = []; // 已经检查通过的IP
      const valueArr = value.split('\n');
      if (Array.isArray(valueArr)) {
        if (valueArr.length > IP_MAX_NUMBER) {
          return Promise.reject(new Error(`最多支持${IP_MAX_NUMBER}个`));
        }

        let errorText = '';
        valueArr.forEach((item, index) => {
          const lineText = `第${index + 1}行[${item}]: `;
          if (!item) {
            errorText = `${lineText}不能为空`;
          }
          // IP网段
          if (item.indexOf('/') > -1) {
            const [ip, mask] = item.split('/');
            if (!ipV4Regex.test(ip) && !ipV6Regex.test(ip)) {
              errorText = `${lineText}请输入正确的IP/IP段`;

            }
            if (ipV4Regex.test(ip) && (!mask || isNaN(mask) || mask <= 0 || mask > 32)) {
              errorText = errorText
                ? errorText
                : `${lineText}请输入正确的IP v4网段。例，192.168.1.2/24`;

            }
            if (ipV6Regex.test(ip) && (!mask || isNaN(mask) || mask <= 0 || mask > 128)) {
              errorText = errorText
                ? errorText
                : `${lineText}请输入正确的IP v6网段。例，2001:250:6EFA::/48`;
            }
          }
          // IP组
          else if (item.indexOf('-') > -1) {
            const ips = item.split('-');
            if (ips.length !== 2) {
              errorText = errorText
                ? errorText
                : `${lineText}请输入正确的IP地址段。例，192.168.1.1-192.168.1.50`;
            }
            const [ip1, ip2] = ips;
            // 2个ipV4
            if (!ipV4Regex.test(ip1) && !ipV4Regex.test(ip2)) {
              errorText = errorText
                ? errorText
                : `${lineText}请输入正确的IP地址段。例，192.168.1.1-192.168.1.50`;
            }
            // 2个都是ipV4的校验下大小关系
            if (ipV4Regex.test(ip1) && ipV4Regex.test(ip2)) {
              // 校验前后2个ip的大小关系
              const ip1Number = ip2number(ip1);
              const ip2Number = ip2number(ip2);
              // 起止地址是否符合大小要求
              if (ip1Number >= ip2Number) {
                errorText = errorText ? errorText : `${lineText}截止IP必须大于开始IP`;
              }
            } else if (!ipV6Regex.test(ip1) && !ipV6Regex.test(ip2)) {
              // ip v6
              errorText = errorText
                ? errorText
                : `${lineText}请输入正确的IP地址段。例，192.168.1.1-192.168.1.50`;
            }
          } else if (!ipV4Regex.test(item) && !ipV6Regex.test(item)) {
            errorText = errorText ? errorText : `${lineText}请输入正确的IP/IP段`;
          }
          // 是否重复了
          if (passIpArr.indexOf(item) !== -1) {
            errorText = errorText ? errorText : `${lineText}已重复`;
          }
          passIpArr.push(item);
        });
        return errorText ? Promise.reject(new Error(errorText)) : Promise.resolve();
      }
      return Promise.resolve();
    }
    return Promise.resolve();
  };

  const handleGoListPage = () => {
    history.goBack();
  };
  const handleReset = () => {
    form.resetFields();
  };
  const handleCreate = (values: any) => {
    createConfirmModal({
      dispatchType: 'ipAddressGroupModel/createIpAddressGroup',
      values,
      onOk: handleGoListPage,
      onCancel: handleReset,
      dispatch,
    });
  };

  const handleUpdate = (values: any) => {
    updateConfirmModal({
      dispatchType: 'ipAddressGroupModel/updateIpAddressGroup',
      values,
      onOk: handleGoListPage,
      dispatch,
      onCancel: () => {},
    });
  };

  const handleSubmit = ({ ipAddress = '', name = '', description = '' }) => {
    const submittedData = { name, ipAddress: ipAddress.split('\n').join(','), description };
    if (operateType === 'CREATE') {
      handleCreate(submittedData);
    } else {
      handleUpdate({ ...submittedData, id: detail?.id });
    }
  };

  return (
    <Form
      {...formLayout}
      name="nest-messages"
      form={form}
      onFinish={handleSubmit}
      onFinishFailed={() => {
        console.log('error');
      }}
      initialValues={{ ...detail } || {}}
      autoComplete="off"
    >
      <Form.Item
        name="name"
        label="IP地址组名称"
        rules={[
          { required: true, message: '请输入IP地址组名称' },
          {
            max: 30,
            message: '最多可输入30个字符',
          },
        ]}
        {...formLayout}
      >
        <Input placeholder="请输入IP地址组名称" />
      </Form.Item>
      <Form.Item
        name="ipAddress"
        label="IP/IP网段"
        validateFirst
        rules={[
          { required: true, message: '请输入IP/IP网段' },
          {
            validator: checkTextAreaIp,
          },
        ]}
        {...formLayout}
        extra={
          <ul style={{ paddingLeft: 20, listStyle: 'circle' }}>
            <li>每行输入一种IP地址，最多支持{IP_MAX_NUMBER}个；</li>
            <li>可以输入【A.B.C.D】格式的IP地址；</li>
            <li>或输入【A.B.C.D/掩码长度】格式的IP网段；</li>
            <li>或输入【A.B.C.D-E.F.G.H】格式的IP组，请确保 E.F.G.H &gt;= A.B.C.D。</li>
          </ul>
        }
      >
        <Input.TextArea rows={5} placeholder="请输入IP/IP网段" />
      </Form.Item>
      <Form.Item
        name="description"
        label="描述信息"
        rules={[
          { required: false, message: '请输入描述信息' },
          { max: 255, message: '最多可输入255个字符' },
        ]}
      >
        <Input.TextArea rows={5} placeholder="请输入描述信息" />
      </Form.Item>
      <Form.Item wrapperCol={{ span: 12, offset: 4 }}>
        <Button
          style={{ marginRight: 10 }}
          type="primary"
          htmlType="submit"
          loading={submitLoading}
        >
          保存
        </Button>
        <Button onClick={handleGoListPage}>返回</Button>
      </Form.Item>
    </Form>
  );
};

export default connect(({ loading: { effects } }: ConnectState) => ({
  submitLoading:
    effects['ipAddressGroupModel/updateIpAddressGroup'] ||
    effects['ipAddressGroupModel/createIpAddressGroup'],
}))(IpAddressGroupForm);
