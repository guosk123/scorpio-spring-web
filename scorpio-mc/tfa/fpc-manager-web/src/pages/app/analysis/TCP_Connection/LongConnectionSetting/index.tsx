import type { ConnectState } from '@/models/connect';
import { ExclamationCircleOutlined } from '@ant-design/icons';
import { Alert, Button, Card, Form, InputNumber, Modal, Skeleton } from 'antd';
import React, { useCallback, useEffect, useMemo } from 'react';
import type { Dispatch } from 'umi';
import { history, useParams } from 'umi';
import type { IUriParams } from '../../typings';
import { EMetricSettingCategory, ESourceType } from '../../typings';
import styles from './index.less';
import { connect } from 'dva';

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
  dispatch: Dispatch;
  longConnectionSeconds: number;

  queryLoading: boolean | undefined;
  updateLoading: boolean | undefined;
}
const LongConnectionSetting: React.FC<ILongConnectionSettingProps> = ({
  dispatch,
  longConnectionSeconds,
  queryLoading,
  updateLoading,
}) => {
  const [form] = Form.useForm();
  const { networkId, serviceId, pcapFileId }: IUriParams = useParams();

  const sourceType: ESourceType = useMemo(() => {
    if (serviceId) {
      return ESourceType.SERVICE;
    }
    if (networkId) {
      return ESourceType.NETWORK;
    }
    return ESourceType.OFFLINE;
  }, [serviceId, networkId]);

  const queryData = useCallback(() => {
    dispatch({
      type: 'npmdModel/queryLongConnectionSetting',
      payload: {
        sourceType,
        networkId,
        serviceId,
        packetFileId: pcapFileId,
      },
    });
  }, [dispatch, sourceType, networkId, serviceId, pcapFileId]);

  useEffect(() => {
    queryData();
  }, [queryData]);

  const onFinish = (values: any) => {
    const metricSettings = JSON.stringify([
      {
        sourceType,
        networkId,
        serviceId,
        packetFileId: pcapFileId,
        metric: EMetricSettingCategory.LONG_CONNECTION,
        value: values.time,
      },
    ]);

    Modal.confirm({
      title: '是否确认保存?',
      icon: <ExclamationCircleOutlined />,
      onOk() {
        dispatch({
          type: 'npmdModel/updateLongConnectionSetting',
          payload: { sourceType, networkId, serviceId, metricSettings },
        }).then(() => {
          queryData();
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
        <Form
          onFinish={onFinish}
          form={form}
          {...layout}
          initialValues={{
            time: longConnectionSeconds,
          }}
        >
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
            <Button onClick={handleGoBack}>返回</Button>
          </Form.Item>
        </Form>
      </Skeleton>
    </Card>
  );
};

export default connect(
  ({ loading: { effects }, npmdModel: { longConnectionSeconds } }: ConnectState) => ({
    longConnectionSeconds,
    queryLoading: effects['npmdModel/queryLongConnectionSetting'],
    updateLoading: effects['npmdModel/updateLongConnectionSetting'],
  }),
)(LongConnectionSetting);
