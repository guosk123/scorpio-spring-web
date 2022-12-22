import { ExclamationCircleOutlined } from '@ant-design/icons';
import { Alert, Button, Card, Form, InputNumber, message, Modal, Skeleton } from 'antd';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { history, useParams } from 'umi';
import styles from './index.less';
import { connect } from 'dva';
import { ESourceType, EMetricSettingCategory, IUriParams } from '../../typings';
import { queryMetricSetting, updataMetricSetting } from '../service';
import { ENetowrkType } from '@/pages/app/Network/typing';

/** 长连接认定时间默认秒数：3600s */
export const DEFAULT_LONG_CONNECTION_SECONDS: number = 3600;

const layout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 12 },
};
const tailLayout = {
  wrapperCol: { offset: 4, span: 12 },
};
const alertMessage = (
  <div>
    <p style={{ marginBottom: 0 }}>长连接认定时间单位为秒</p>
    <p style={{ marginBottom: 0 }}>
      系统限定长连接时间不得低于1800秒（半小时），默认为{DEFAULT_LONG_CONNECTION_SECONDS}秒
    </p>
  </div>
);
const timeExtra = <Alert message={alertMessage} type="info" showIcon />;

export interface ILongConnectionSettingProps {
  networkType?: ENetowrkType;
}
const LongConnectionSetting: React.FC<ILongConnectionSettingProps> = ({ networkType }) => {
  const [form] = Form.useForm();
  const urlIds = useParams<IUriParams>();
  const { serviceId, networkId } = useMemo(() => {
    const tmpNetworkId = urlIds.networkId || '';
    if (tmpNetworkId.includes('^')) {
      return {
        serviceId: urlIds.serviceId,
        networkId: tmpNetworkId.split('^')[1],
      };
    }
    return { serviceId: urlIds.serviceId, networkId: urlIds.networkId };
  }, [urlIds.networkId, urlIds.serviceId]);

  const sourceType: ESourceType = useMemo(() => {
    if (serviceId) {
      return ESourceType.SERVICE;
    }
    if (networkId) {
      return ESourceType.NETWORK;
    }
    return ESourceType.OFFLINE;
  }, [serviceId, networkId]);

  const [longConnectionSeconds, setLongConnectionSeconds] = useState<number>(0);
  const [queryLoading, setQueryLoading] = useState(false);
  const queryData = useCallback(() => {
    setQueryLoading(true);
    const queryParams = {
      sourceType,
      serviceId,
    };
    queryParams[networkType === ENetowrkType.NETWORK ? 'networkId' : 'networkGroupId'] = networkId;
    queryMetricSetting(queryParams).then((res) => {
      const { success, result } = res;
      if (success) {
        const target = result.find(
          (row: any) => row.metric === EMetricSettingCategory.LONG_CONNECTION,
        );
        setLongConnectionSeconds(target?.value || DEFAULT_LONG_CONNECTION_SECONDS);
      }
      setQueryLoading(false);
    });
  }, [networkId, serviceId, sourceType]);

  useEffect(() => {
    form.setFieldsValue({
      time: longConnectionSeconds,
    });
  }, [longConnectionSeconds]);

  useEffect(() => {
    queryData();
  }, [queryData]);

  const [updateLoading, setUpdateLoading] = useState(false);

  const onFinish = (values: any) => {
    const metricSettings = JSON.stringify([
      {
        sourceType,
        networkId,
        serviceId,
        metric: EMetricSettingCategory.LONG_CONNECTION,
        value: values.time,
      },
    ]);

    Modal.confirm({
      title: '是否确认保存?',
      icon: <ExclamationCircleOutlined />,
      onOk() {
        setUpdateLoading(true);
        const queryParams = {
          sourceType,
          serviceId,
          metricSettings,
        };
        queryParams[networkType === ENetowrkType.NETWORK ? 'networkId' : 'networkGroupId'] =
          networkId;
        updataMetricSetting(queryParams).then((result) => {
          if (result.success) {
            queryData();
            message.success('保存成功!');
          }
          setUpdateLoading(false);
        });
      },
    });
  };

  const handleGoBack = () => {
    history.goBack();
  };

  return (
    <Card bordered={false}>
      <Skeleton active loading={queryLoading}>
        <Form onFinish={onFinish} form={form} {...layout}>
          <Form.Item
            name="time"
            label="长连接认定时间"
            rules={[{ required: true }]}
            className={styles.formItemWrap}
          >
            <InputNumber
              style={{ width: '100%' }}
              min={1800}
              precision={0}
              placeholder="请输入时间"
            />
          </Form.Item>
          <Form.Item {...tailLayout}>{timeExtra}</Form.Item>
          <Form.Item {...tailLayout}>
            <Button
              style={{ marginRight: 10 }}
              type="primary"
              htmlType="submit"
              loading={updateLoading}
            >
              保存
            </Button>
            {/* <Button onClick={handleGoBack}>返回</Button> */}
          </Form.Item>
        </Form>
      </Skeleton>
    </Card>
  );
};

export default connect()(LongConnectionSetting);
