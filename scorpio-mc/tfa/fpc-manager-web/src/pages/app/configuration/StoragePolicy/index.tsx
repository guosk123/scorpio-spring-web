import type { ConnectState } from '@/models/connect';
import { Button, Card, Form, Input, Modal, Radio, Select, Skeleton } from 'antd';
import React, { useEffect } from 'react';
import type { Dispatch, IMonitorMetric } from 'umi';
import { connect } from 'dva';
import type { IStoragePolicy } from './typings';
import { ECompressAction, EEncryptAction } from './typings';

const FormItem = Form.Item;
const RadioGroup = Radio.Group;

/**
 * 加密算法
 */
export const FLOW_STORAGE_ENCRYPTION_ALGORITHM_LIST = [
  {
    key: 'SM4_ECB',
    label: 'SM4_ECB',
  },
  {
    key: 'SM4_CBC',
    label: 'SM4_CBC',
  },
  {
    key: 'AES_128_ECB',
    label: 'AES_128_ECB',
  },
  {
    key: 'AES_128_CBC',
    label: 'AES_128_CBC',
  },
  {
    key: 'AES_192_ECB',
    label: 'AES_192_ECB',
  },
  {
    key: 'AES_192_CBC',
    label: 'AES_192_CBC',
  },
  {
    key: 'AES_256_ECB',
    label: 'AES_256_ECB',
  },
  {
    key: 'AES_256_CBC',
    label: 'AES_256_CBC',
  },
];

/**
 * 压缩策略
 */
export const FLOW_STORAGE_COMPRESS_ACTION_LIST = [
  {
    key: ECompressAction.YES,
    label: '压缩',
  },
  {
    key: ECompressAction.NO,
    label: '不压缩',
  },
];

/**
 * 加密策略
 */
export const FLOW_STORAGE_ENCRYPT_ACTION_LIST = [
  {
    key: EEncryptAction.YES,
    label: '加密',
  },
  {
    key: EEncryptAction.NO,
    label: '不加密',
  },
];

const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 4 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 10 },
  },
};

interface IStoragePolicyProps {
  dispatch: Dispatch;
  policy: IStoragePolicy;
  metrics: IMonitorMetric[];
  queryLoading: boolean | undefined;
  updateLoading: boolean | undefined;
}
const StoragePolicy: React.FC<IStoragePolicyProps> = ({
  dispatch,
  policy,
  queryLoading,
  updateLoading,
}) => {
  useEffect(() => {
    if (dispatch) {
      // 先获取统计信息
      dispatch({
        type: 'moitorModel/queryMetrics',
      });

      dispatch({
        type: 'storagePolicyModel/query',
      });
    }
  }, [dispatch]);

  const handleSubmit = (values: any) => {
    Modal.confirm({
      title: '确定保存吗?',
      cancelText: '取消',
      okText: '保存',
      onOk() {
        const data = {
          ...values,
          encryptAlgorithm: values.encryptAlgorithm || '',
        };

        delete data.transmitTaskFileLimitGigabyte;

        dispatch({
          type: 'storagePolicyModel/update',
          payload: {
            ...data,
          },
        });
      },
      onCancel() {},
    });
  };

  return (
    <Skeleton loading={queryLoading} active>
      <Card bordered={false}>
        <Form
          {...formItemLayout}
          scrollToFirstError
          onFinish={handleSubmit}
          initialValues={{
            ...policy,
            encryptAlgorithm: policy.encryptAlgorithm || undefined,
          }}
        >
          <FormItem label="id" name="id" hidden>
            <Input />
          </FormItem>
          <FormItem
            {...formItemLayout}
            label="压缩策略"
            name="compressAction"
            rules={[
              {
                required: true,
                message: '请选择流量存储压缩策略',
              },
            ]}
          >
            <RadioGroup>
              {FLOW_STORAGE_COMPRESS_ACTION_LIST.map((item) => (
                <Radio key={item.key} value={item.key}>
                  {item.label}
                </Radio>
              ))}
            </RadioGroup>
          </FormItem>
          <FormItem
            label="加密策略"
            name="encryptAction"
            rules={[
              {
                required: true,
                message: '请选择流量存储加密策略',
              },
            ]}
          >
            <RadioGroup>
              {FLOW_STORAGE_ENCRYPT_ACTION_LIST.map((item) => (
                <Radio key={item.key} value={item.key}>
                  {item.label}
                </Radio>
              ))}
            </RadioGroup>
          </FormItem>
          <FormItem
            noStyle
            shouldUpdate={(prevValues, curValues) =>
              prevValues.encryptAction !== curValues.encryptAction
            }
          >
            {({ getFieldValue }) => {
              return getFieldValue('encryptAction') === EEncryptAction.YES ? (
                <FormItem
                  label="加密算法"
                  name="encryptAlgorithm"
                  rules={[
                    {
                      required: true,
                      message: '请选择流量存储加密算法',
                    },
                  ]}
                >
                  <Select placeholder="选择加密算法">
                    {FLOW_STORAGE_ENCRYPTION_ALGORITHM_LIST.map((item) => (
                      <Select.Option key={item.key} value={item.key}>
                        {item.label}
                      </Select.Option>
                    ))}
                  </Select>
                </FormItem>
              ) : null;
            }}
          </FormItem>
          <FormItem wrapperCol={{ span: 12, offset: 4 }}>
            <Button type="primary" htmlType="submit" loading={updateLoading}>
              保存
            </Button>
          </FormItem>
        </Form>
      </Card>
    </Skeleton>
  );
};

export default connect(
  ({
    storagePolicyModel: { policy },
    moitorModel: { metrics },
    loading: { effects },
  }: ConnectState) => ({
    policy,
    metrics,
    queryLoading: effects['storagePolicyModel/query'] || effects['moitorModel/queryMetrics'],
    updateLoading: effects['storagePolicyModel/update'],
  }),
)(StoragePolicy);
