import {
  DEVICE_NETIF_CATEGORY_LIST,
  DEVICE_NETIF_CATEGORY_MANAGER,
  DEVICE_NETIF_CATEGORY_NETFLOW,
} from '@/common/dict';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { Input, Modal, Select } from 'antd';
import { connect } from 'dva';
import React, { PureComponent, Fragment } from 'react';
import ipaddr from 'ipaddr.js';
import { ipV4Regex, ipV6Regex, ipv4MaskRegex } from '@/utils/utils';
import { ENetifType, ENetifCategory } from '../../typings';

const { Option } = Select;
const { TextArea } = Input;

// ipdemo
const ipv4AddressDemo = '192.168.0.1/255.255.255.0';
const ipv6AddressDemo = '234e:0:4567::3d/64';

// 跳过管理口
const netifCategoryList = DEVICE_NETIF_CATEGORY_LIST.filter(
  ({ key }) => key !== DEVICE_NETIF_CATEGORY_MANAGER,
);

@Form.create()
@connect((state) => {
  const {
    deviceNetifModel: { modalVisible, currentItem },
    networkModel: { usedNetifs },
    loading,
  } = state;
  const { effects } = loading;
  return {
    modalVisible,
    currentItem,
    usedNetifs,
    queryUsedNetifsLoading: effects['networkModel/queryUsedNetifs'],
    updateLoading: effects['deviceNetifModel/updateDeviceNetifs'],
  };
})
class DeviceNetifUpdate extends PureComponent {
  /** 检验ipv4地址格式 */
  checkIpv4Address = (rule, ipv4Address, callback) => {
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
  /** 检验ipv4网关地址格式 */
  checkIpv4Gateway = (rule, ipv4Gateway, callback) => {
    const ipv4Address = this.props.form.getFieldValue('ipv4Address');
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

    const res0 = parseInt(ipArr[0], 10) && parseInt(maskArr[0], 10);
    const res1 = parseInt(ipArr[1], 10) && parseInt(maskArr[1], 10);
    const res2 = parseInt(ipArr[2], 10) && parseInt(maskArr[2], 10);
    const res3 = parseInt(ipArr[3], 10) && parseInt(maskArr[3], 10);
    const res0_gw = parseInt(gwArr[0], 10) && parseInt(maskArr[0], 10);
    const res1_gw = parseInt(gwArr[1], 10) && parseInt(maskArr[1], 10);
    const res2_gw = parseInt(gwArr[2], 10) && parseInt(maskArr[2], 10);
    const res3_gw = parseInt(gwArr[3], 10) && parseInt(maskArr[3], 10);
    if (res0 === res0_gw && res1 === res1_gw && res2 === res2_gw && res3 === res3_gw) {
      callback();
      return;
    }
    callback('IPv4地址与子网掩码、网关地址不匹配');

    callback();
  };
  /** 检验ipv6地址格式 */
  checkIpv6Address = (rule, ipv6Address, callback) => {
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
  /** 检验ipv6网关地址格式 */
  checkIpv6Gateway = (rule, ipv6Gateway, callback) => {
    const ipv6Address = this.props.form.getFieldValue('ipv6Address');
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
      !gw.match(ipaddr.parseCIDR(ipv6AddressArr.length !== 2 ? `${ipv6Address}/0` : ipv6Address))
    ) {
      callback('IPv6地址与子网掩码、网关地址不匹配');
      return;
    }
    callback();
  };

  componentDidMount() {
    // this.queryUsedNetifs();
  }

  // queryUsedNetifs = () => {
  //   const { dispatch } = this.props;
  //   dispatch({
  //     type: 'networkModel/queryUsedNetifs',
  //   });
  // };

  /** 处理提交信息 */
  handleSubmit = () => {
    const { form, dispatch, onOk } = this.props;
    form.validateFields((err, values) => {
      if (err) {
        return;
      }
      const netifName = this.props.currentItem.name;
      const fieldsArr = [{ ...values, type: this.props.currentItem.type }];
      dispatch({
        type: 'deviceNetifModel/updateDeviceNetifs',
        payload: {
          netifListJson: JSON.stringify(fieldsArr),
          category: values.category,
          netifName,
        },
      }).then((success) => {
        if (success) {
          onOk();
          // 关闭弹出框
          this.handleCancel();
          // 重新查询数据
          dispatch({
            type: 'deviceNetifModel/queryDeviceNetifs',
          });
        }
      });
    });
  };

  /** 处理撤销操作 */
  handleCancel = () => {
    const { dispatch } = this.props;
    dispatch({
      type: 'deviceNetifModel/hideModal',
    });
  };

  render() {
    const { updateLoading, queryUsedNetifsLoading, form, modalVisible, currentItem } = this.props;
    const { getFieldDecorator } = form;
    // 表单布局
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

    const netifCanUpdate = currentItem.useMessage === '';

    // 渲染ip校验
    const renderIpCheck = () => {
      const { getFieldValue, getFieldsValue } = this.props.form;
      const { category } = getFieldsValue();
      if (category === DEVICE_NETIF_CATEGORY_NETFLOW) {
        return (
          <>
            <Form.Item
              {...formItemLayout}
              label="IPv4地址"
              extra={
                <Fragment>
                  <div>格式：{ipv4AddressDemo}</div>
                  <div>IPv4地址和IPv6地址请至少配置一个</div>
                </Fragment>
              }
            >
              {getFieldDecorator('ipv4Address', {
                initialValue: currentItem.ipv4Address,
                validateFirst: true,
                rules: [
                  {
                    required: !getFieldValue('ipv6Address') || getFieldValue('ipv4Gateway'),
                    message: '请输入IPv4地址',
                  },
                  {
                    validator: this.checkIpv4Address,
                  },
                ],
              })(<Input placeholder="请输入IPv4地址" allowClear />)}
            </Form.Item>
            <Form.Item label="IPv4网关地址" {...formItemLayout}>
              {getFieldDecorator('ipv4Gateway', {
                initialValue: currentItem.ipv4Gateway,
                validateFirst: true,
                rules: [
                  { required: getFieldValue('ipv4Address'), message: '请输入IPv4网关地址' },
                  {
                    pattern: ipV4Regex,
                    message: '请输入正确的IPv4地址',
                  },
                  {
                    validator: this.checkIpv4Gateway,
                  },
                ],
              })(<Input placeholder="请输入IPv4网关地址" allowClear />)}
            </Form.Item>
            <Form.Item label="IPv6地址" {...formItemLayout} extra={`格式：${ipv6AddressDemo}`}>
              {getFieldDecorator('ipv6Address', {
                initialValue: currentItem.ipv6Address,
                validateFirst: true,
                rules: [
                  {
                    required: !getFieldValue('ipv4Address') || getFieldValue('ipv6Gateway'),
                    message: '请输入IPv6地址',
                  },
                  {
                    validator: this.checkIpv6Address,
                  },
                ],
              })(<Input placeholder="请输入IPv6地址" allowClear />)}
            </Form.Item>
            <Form.Item label="IPv6网关地址" {...formItemLayout}>
              {getFieldDecorator('ipv6Gateway', {
                initialValue: currentItem.ipv6Gateway,
                validateFirst: true,
                rules: [
                  { required: getFieldValue('ipv6Address'), message: '请输入IPv6网关地址' },
                  {
                    pattern: ipV6Regex,
                    message: '请输入正确的IPv6地址',
                  },
                  {
                    validator: this.checkIpv6Gateway,
                  },
                ],
              })(<Input placeholder="请输入IPv6网关地址" allowClear />)}
            </Form.Item>
          </>
        );
      }
      return undefined;
    };

    return (
      <Modal
        title={`${currentItem.name} 编辑`}
        visible={modalVisible}
        destroyOnClose
        keyboard={false}
        maskClosable={false}
        onOk={this.handleSubmit}
        confirmLoading={updateLoading}
        onCancel={this.handleCancel}
      >
        <Form>
          <Form.Item {...formItemLayout} label="id" style={{ display: 'none' }}>
            {getFieldDecorator('id', {
              initialValue: currentItem.id,
            })(<Input />)}
          </Form.Item>
          <Form.Item
            {...formItemLayout}
            label="接口用途"
            extra={netifCanUpdate ? '' : `接口已经被${currentItem.useMessage}使用，请先解绑后再修改接口用途`}
          >
            {getFieldDecorator('category', {
              initialValue: currentItem.category,
              rules: [
                {
                  required: true,
                  message: '请选择接口用途',
                },
              ],
            })(
              <Select
                style={{ width: '100%' }}
                placeholder="请选择接口用途"
                loading={queryUsedNetifsLoading}
                disabled={!netifCanUpdate}
              >
                {netifCategoryList
                  .filter((item) => {
                    // 如果接口用途是普通，过滤掉流量接收和流量重放
                    if (
                      currentItem.type === ENetifType.COMMON &&
                      (item.key === ENetifCategory.RECEIVE || item.key === ENetifCategory.REPLAY)
                    ) {
                      return false;
                    }
                    if (
                      currentItem.type === ENetifType.DPDK &&
                      item.key === ENetifCategory.NETFLOW
                    ) {
                      return false;
                    }
                    return true;
                  })
                  .map((category) => {
                    return (
                      <Option disabled={category.disabled} key={category.key} value={category.key}>
                        {category.label}
                      </Option>
                    );
                  })}
              </Select>,
            )}
          </Form.Item>
          {renderIpCheck()}
          <Form.Item {...formItemLayout} label="接口描述">
            {getFieldDecorator('description', {
              initialValue: currentItem.description,
              rules: [
                {
                  required: false,
                  message: '请选择接口用途',
                },
                {
                  max: 64,
                  message: '最多可输入64个字符',
                },
              ],
            })(<TextArea placeholder="最多可输入64个字符" rows={4} />)}
          </Form.Item>
        </Form>
      </Modal>
    );
  }
}

export default DeviceNetifUpdate;
