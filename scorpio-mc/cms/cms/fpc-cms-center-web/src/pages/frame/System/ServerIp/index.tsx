/* eslint-disable radix */
/* eslint-disable no-bitwise */
import React, { useEffect, Fragment } from 'react';
import type { Dispatch } from 'redux';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { Input, Button, Skeleton, Modal, Card } from 'antd';
import { connect } from 'dva';
import type { FormComponentProps } from '@ant-design/compatible/es/form';
import { ipV4Regex, ipV6Regex, ipv4MaskRegex } from '@/utils/utils';
import ipaddr from 'ipaddr.js';
import type { IModelState } from './model';
import type { IServerIpSettings } from './typings';

const FormItem = Form.Item;

const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 5 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 14 },
  },
};
const tailFormItemLayout = {
  wrapperCol: {
    xs: {
      span: 24,
      offset: 0,
    },
    sm: {
      offset: 5,
      span: 14,
    },
  },
};

interface IServerIpSettingProps extends FormComponentProps {
  dispatch: Dispatch<any>;
  serverIpSettings: IServerIpSettings;
  queryLoading: boolean;
  updateLoading: boolean;
}

const ipv4AddressDemo = '192.168.0.1/255.255.255.0';
const ipv6AddressDemo = '234e:0:4567::3d/64';

const ServerIpSetting: React.SFC<IServerIpSettingProps> = ({
  dispatch,
  form,
  serverIpSettings,
  queryLoading,
  updateLoading,
}) => {
  useEffect(() => {
    if (dispatch) {
      dispatch({
        type: 'serverIpSettingModel/querySystemServerIpSettings',
        payload: {},
      });
    }
  }, [dispatch]);

  const checkIpv4Address = (rule: any, ipv4Address: string, callback: any) => {
    if (!ipv4Address) {
      callback();
      return;
    }
    const [ipv4, mask] = ipv4Address.split('/');
    if (!ipv4 || !mask) {
      callback(`格式：${ipv4AddressDemo}`);
      return;
    }
    // 校验ip
    if (!ipV4Regex.test(ipv4)) {
      callback('请输入正确的IPv4地址');
      return;
    }
    if (!ipv4MaskRegex.test(mask)) {
      callback('请输入正确的掩码');
      return;
    }
    if (ipv4 === mask) {
      callback('网络掩码不能IPv4地址相同');
      return;
    }
    callback();
  };

  const checkIpv4Gateway = (rule: any, ipv4Gateway: string, callback: any) => {
    const ipv4Address = form.getFieldValue('ipv4Address');
    if (!ipv4Address) {
      callback();
      return;
    }
    if (!ipv4Gateway) {
      callback('请输入IPv4网关地址');
      return;
    }
    const [ipv4, mask] = ipv4Address.split('/');
    if (!ipv4 || !mask) {
      callback();
      return;
    }

    if (ipv4 === mask || ipv4 === ipv4Gateway || mask === ipv4Gateway) {
      callback('IPv4地址、掩码和网关地址，三者不能相同');
      return;
    }

    const ipArr = ipv4.split('.');
    const maskArr = mask.split('.');
    const gwArr = ipv4Gateway.split('.');

    const res0 = parseInt(ipArr[0]) & parseInt(maskArr[0]);
    const res1 = parseInt(ipArr[1]) & parseInt(maskArr[1]);
    const res2 = parseInt(ipArr[2]) & parseInt(maskArr[2]);
    const res3 = parseInt(ipArr[3]) & parseInt(maskArr[3]);
    const res0_gw = parseInt(gwArr[0]) & parseInt(maskArr[0]);
    const res1_gw = parseInt(gwArr[1]) & parseInt(maskArr[1]);
    const res2_gw = parseInt(gwArr[2]) & parseInt(maskArr[2]);
    const res3_gw = parseInt(gwArr[3]) & parseInt(maskArr[3]);
    if (res0 === res0_gw && res1 === res1_gw && res2 === res2_gw && res3 === res3_gw) {
      callback();
      return;
    }
    callback('IPv4地址与子网掩码、网关地址不匹配');

    callback();
  };

  const checkIpv6Address = (rule: any, ipv6Address: string, callback: any) => {
    if (!ipv6Address) {
      callback();
      return;
    }
    const [ipv6, mask] = ipv6Address.split('/');
    if (!ipv6) {
      callback(`格式：${ipv6AddressDemo}`);
      return;
    }
    // 校验ip
    if (!ipV6Regex.test(ipv6)) {
      callback('请输入正确的IPv6地址');
      return;
    }
    if (+mask <= 0 || +mask > 128) {
      callback('请输入正确的掩码');
      return;
    }
    callback();
  };

  const checkIpv6Gateway = (rule: any, ipv6Gateway: string, callback: any) => {
    const ipv6Address = form.getFieldValue('ipv6Address');
    if (!ipv6Address) {
      callback();
      return;
    }
    if (!ipv6Gateway) {
      callback('请输入IPv6网关地址');
      return;
    }
    const ipv6AddressArr = ipv6Address.split('/');
    const [ipv6] = ipv6AddressArr;
    if (!ipv6) {
      callback();
      return;
    }
    if (ipv6 === ipv6Gateway) {
      callback('IPv6地址和网关地址不能相同');
      return;
    }

    const gw = ipaddr.parse(ipv6Gateway);
    if (
      // 避免不带前缀长度导致的检验错误问题
      gw instanceof ipaddr.IPv6 &&
      !gw.match(
        ipaddr.parseCIDR(ipv6AddressArr.length !== 2 ? `${ipv6Address}/0` : ipv6Address) as [
          ipaddr.IPv6,
          number,
        ],
      )
    ) {
      callback('IPv6地址与子网掩码、网关地址不匹配');
      return;
    }
    callback();
  };

  const checkDns = (rule: any, dnsText: string, callback: any) => {
    const ipv6Address = form.getFieldValue('ipv6Address');
    const ipv4Address = form.getFieldValue('ipv4Address');
    if (!ipv4Address && !ipv6Address) {
      callback('请先输入IPv4管理地址或IPv6管理地址');
      return;
    }
    if (!dnsText) {
      callback();
      return;
    }
    const dnsArr = dnsText.split('\n').filter((dns) => dns);
    if (dnsArr.length === 0) {
      callback('请输入DNS服务器');
      return;
    }

    const passDnsArr: string[] = [];

    for (let index = 0; index < dnsArr.length; index += 1) {
      const dns = dnsArr[index];
      const lineText = `第${index + 1}行[${dns}]: `;
      if (ipv4Address && ipv6Address) {
        if (!ipV4Regex.test(dns) && !ipV6Regex.test(dns)) {
          callback(`${lineText}请输入正确的IPv4或IPv6地址`);
          return;
        }
      }
      // 如果只有IPv6的管理口，那dns只允许配置IPv6地址
      if (!ipv4Address) {
        if (ipV4Regex.test(dns)) {
          callback(`${lineText}未配置IPv4管理地址，所以不允许输入IPv4`);
          return;
        }
        if (!ipV6Regex.test(dns)) {
          callback(`${lineText}请输入正确的IPv6地址`);
          return;
        }
      }
      // 如果只有IPv4的管理口，那dns只允许配置IPv4地址
      if (!ipv6Address) {
        if (ipV6Regex.test(dns)) {
          callback(`${lineText}未配置IPv6管理地址，所以不允许输入IPv6`);
          return;
        }
        if (!ipV4Regex.test(dns)) {
          callback(`${lineText}请输入正确的IPv4地址`);
          return;
        }
      }
      // 是否重复了
      if (passDnsArr.indexOf(dns) !== -1) {
        callback(`${lineText}已重复`);
        return;
      }
      passDnsArr.push(dns);
    }

    callback();
  };

  const handleUpdate = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    form.validateFieldsAndScroll((err, values) => {
      if (err) {
        return;
      }
      Modal.confirm({
        title: '确定保存吗？',
        maskClosable: false,
        onOk: () => {
          const dnsArr = values.dns ? values.dns.split('\n').filter((dns: string) => dns) : [];
          const data = { ...values, dns: dnsArr.join(',') };
          dispatch({
            type: 'serverIpSettingModel/updateSystemServerIpSettings',
            payload: {
              ...data,
            },
          });
        },
      });
    });
  };

  const { getFieldDecorator, getFieldValue } = form;
  return (
    <Card bordered={false}>
      <Skeleton active loading={queryLoading}>
        <Form {...formItemLayout} onSubmit={handleUpdate}>
          <FormItem
            label="IPv4管理地址"
            extra={
              <Fragment>
                <div>格式：{ipv4AddressDemo}</div>
                <div>IPv4管理地址和IPv6管理地址请至少配置一个</div>
              </Fragment>
            }
          >
            {getFieldDecorator('ipv4Address', {
              initialValue: serverIpSettings.ipv4Address || '',
              validateFirst: true,
              rules: [
                {
                  required: !getFieldValue('ipv6Address') || getFieldValue('ipv4Gateway'),
                  message: '请输入IPv4管理地址',
                },
                {
                  validator: checkIpv4Address,
                },
              ],
            })(<Input placeholder="请输入IPv4管理地址" allowClear />)}
          </FormItem>
          <FormItem label="IPv4网关地址">
            {getFieldDecorator('ipv4Gateway', {
              initialValue: serverIpSettings.ipv4Gateway || '',
              validateFirst: true,
              rules: [
                { required: getFieldValue('ipv4Address'), message: '请输入IPv4网关地址' },
                {
                  pattern: ipV4Regex,
                  message: '请输入正确的IPv4地址',
                },
                {
                  validator: checkIpv4Gateway,
                },
              ],
            })(<Input placeholder="请输入IPv4网关地址" allowClear />)}
          </FormItem>
          <FormItem label="IPv6管理地址" extra={`格式：${ipv6AddressDemo}`}>
            {getFieldDecorator('ipv6Address', {
              initialValue: serverIpSettings.ipv6Address || '',
              validateFirst: true,
              rules: [
                {
                  required: !getFieldValue('ipv4Address') || getFieldValue('ipv6Gateway'),
                  message: '请输入IPv6管理地址',
                },
                {
                  validator: checkIpv6Address,
                },
              ],
            })(<Input placeholder="请输入IPv6管理地址" allowClear />)}
          </FormItem>
          <FormItem label="IPv6网关地址">
            {getFieldDecorator('ipv6Gateway', {
              initialValue: serverIpSettings.ipv6Gateway || '',
              validateFirst: true,
              rules: [
                { required: getFieldValue('ipv6Address'), message: '请输入IPv6网关地址' },
                {
                  pattern: ipV6Regex,
                  message: '请输入正确的IPv6地址',
                },
                {
                  validator: checkIpv6Gateway,
                },
              ],
            })(<Input placeholder="请输入IPv6网关地址" allowClear />)}
          </FormItem>
          <FormItem label="DNS服务器" extra="多个DNS用回车换行分隔">
            {getFieldDecorator('dns', {
              initialValue: serverIpSettings.dns ? serverIpSettings.dns.replace(/,/g, '\n') : '',
              validateFirst: true,
              rules: [{ required: false, message: '请输入DNS服务器' }, { validator: checkDns }],
            })(<Input.TextArea rows={4} placeholder="请输入DNS服务器" allowClear />)}
          </FormItem>
          <FormItem {...tailFormItemLayout}>
            <Button loading={updateLoading} type="primary" htmlType="submit">
              保存
            </Button>
          </FormItem>
        </Form>
      </Skeleton>
    </Card>
  );
};

export default connect(
  ({
    serverIpSettingModel: { serverIpSettings },
    loading,
  }: {
    serverIpSettingModel: IModelState;
    loading: { effects: Record<string, boolean> };
  }) => ({
    serverIpSettings,
    queryLoading: loading.effects['serverIpSettingModel/querySystemServerIpSettings'] || false,
    updateLoading: loading.effects['serverIpSettingModel/updateSystemServerIpSettings'] || false,
  }),
)(Form.create<IServerIpSettingProps>()(ServerIpSetting));
