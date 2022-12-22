import type { ConnectState } from '@/models/connect';
import { ExclamationCircleOutlined } from '@ant-design/icons';
import { Alert, Button, Card, Form, InputNumber, Select, Space, Modal, Skeleton } from 'antd';
import React, { useCallback, useEffect, useMemo } from 'react';
import type { Dispatch } from 'umi';
import { connect, history, useParams } from 'umi';
import type {
  IPerformanceSettingData,
  IUriParams,
  IResponseTimeSettingParams,
} from '../../typings';
import { EMetricSettingCategory } from '../../typings';
import { ESourceType } from '../../typings';
import styles from './index.less';

const FormItem = Form.Item;
const { Option } = Select;
const { confirm } = Modal;

const layout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 10 },
};

const buttonLayout = {
  wrapperCol: { offset: 4 },
};

const trendLayout = {
  wrapperCol: { span: 12 },
};

const timeoutAlertMsg = (
  <div className={styles.performanceAlertMsg}>
    <p>响应时间单位为毫秒（ms）</p>
    <p>如果服务器响应时间小于等于正常响应时间，将统计到迅速响应时间内；</p>
    <p>大于正常响应时间，小于等于超时响应时间，将统计到正常响应时间内；</p>
    <p>大于超时响应时间，则认为服务器响应超时，将统计到超时响应时间内。</p>
  </div>
);
const timeoutAlert = <Alert message={timeoutAlertMsg} showIcon />;

const trendWeightingModelEnum = {
  MEAN: '均值',
  MIN: '最小值',
  MEDIAN: '中位数',
  MAX: '最大值',
};

const trendWindowingModelEnum = {
  'minute_of_day,five_minute_of_day,hour_of_day': '天同比',
  'minute_of_week,five_minute_of_week,hour_of_week': '周同比',
  'last_n_minutes,last_n_five_minutes,last_n_hours': '环比',
};

export interface ISettingProps {
  dispatch: Dispatch;
  queryLoading: boolean | undefined;
  updateLoading: boolean | undefined;
  performanceSettingData: IPerformanceSettingData;
}

const PerformanceSetting: React.FC<ISettingProps> = ({
  dispatch,
  queryLoading,
  updateLoading,
  performanceSettingData,
}) => {
  const [form] = Form.useForm();
  const { networkId, serviceId } = useParams() as IUriParams;
  const sourceType = useMemo(() => {
    return serviceId ? ESourceType.SERVICE : ESourceType.NETWORK;
  }, [serviceId]);

  const queryPerformanceSetting = useCallback(() => {
    dispatch({
      type: 'npmdModel/queryPerformanceSetting',
      payload: {
        sourceType,
        networkId,
        serviceId,
      },
    });
  }, [dispatch, sourceType, networkId, serviceId]);

  useEffect(() => {
    queryPerformanceSetting();
  }, [queryPerformanceSetting]);

  useEffect(() => {
    if (performanceSettingData.responseTime) {
      const { responseTime } = performanceSettingData;
      form.setFieldsValue({
        server_response_normal: responseTime.find(
          (time) => time.metric === EMetricSettingCategory.SERVER_RESPONSE_NORMAL,
        )?.value,
        server_response_timeout: responseTime.find(
          (time) => time.metric === EMetricSettingCategory.SERVER_RESPONSE_TIMEOUT,
        )?.value,
      });
    }
    if (performanceSettingData.baseline) {
      const { baseline } = performanceSettingData;
      form.setFieldsValue({
        responseLatency: {
          weightingModel: baseline.weightingModel,
          windowingModel: baseline.windowingModel,
          windowingCount: baseline.windowingCount,
        },
      });
    } else {
      form.setFieldsValue({
        responseLatency: {
          weightingModel: undefined,
          windowingModel: undefined,
          windowingCount: undefined,
        },
      });
    }
  }, [form, performanceSettingData, networkId, serviceId]);

  const handleGoBack = () => {
    history.goBack();
  };

  const onFinish = (values: any) => {
    const payload: {
      time: IResponseTimeSettingParams[];
      baseline: any[];
    } = {
      time: [],
      baseline: [],
    };
    Object.keys(values).forEach((key) => {
      if (key === 'server_response_normal' || key === 'server_response_timeout') {
        payload.time.push({
          sourceType,
          networkId,
          serviceId,
          metric: key,
          value: values[key].toString(),
        });
      } else {
        payload.baseline.push({
          sourceType,
          networkId,
          serviceId,
          category: key,
          ...values[key],
        });
      }
    });
    confirm({
      title: '是否确认保存?',
      icon: <ExclamationCircleOutlined />,
      onOk: () => {
        dispatch({
          type: 'npmdModel/updatePerformanceSetting',
          payload,
        }).then(() => {
          queryPerformanceSetting();
        });
      },
    });
  };

  return (
    <Card bordered={false}>
      <Skeleton active loading={queryLoading}>
        <Form form={form} {...layout} onFinish={onFinish}>
          <FormItem label="正常响应时间" required>
            <FormItem
              noStyle
              name="server_response_normal"
              rules={[{ required: true, message: '请填写正常响应时间' }]}
            >
              <InputNumber min={1} precision={0} style={{ width: '90%' }} />
            </FormItem>
            <span className="ant-form-text"> ms</span>
          </FormItem>
          <FormItem
            label="超时响应时间"
            required
            extra={timeoutAlert}
            className={styles.formItemWrap}
          >
            <FormItem
              noStyle
              name="server_response_timeout"
              rules={[{ required: true, message: '请填写超时响应时间' }]}
            >
              <InputNumber min={1} precision={0} style={{ width: '90%' }} />
            </FormItem>
            <span className="ant-form-text"> ms</span>
          </FormItem>
          <FormItem label="响应时间基线定义">
            <Card>
              <FormItem
                label="权重模型"
                rules={[
                  {
                    required: true,
                    message: '请选择权重模型',
                  },
                ]}
                name={['responseLatency', 'weightingModel']}
                {...trendLayout}
              >
                <Select>
                  {Object.keys(trendWeightingModelEnum).map((key) => {
                    return (
                      <Option key={key} value={key}>
                        {trendWeightingModelEnum[key]}
                      </Option>
                    );
                  })}
                </Select>
              </FormItem>
              <FormItem
                label="基线窗口"
                rules={[
                  {
                    required: true,
                    message: '请选择基线窗口',
                  },
                ]}
                name={['responseLatency', 'windowingModel']}
                {...trendLayout}
              >
                <Select>
                  {Object.keys(trendWindowingModelEnum).map((key) => {
                    return (
                      <Option key={key} value={key}>
                        {trendWindowingModelEnum[key]}
                      </Option>
                    );
                  })}
                </Select>
              </FormItem>
              <FormItem
                label="回顾周期"
                rules={[
                  {
                    required: true,
                    message: '请填写回顾周期',
                  },
                ]}
                name={['responseLatency', 'windowingCount']}
              >
                <InputNumber min={1} precision={0} />
              </FormItem>
            </Card>
          </FormItem>
          <FormItem {...buttonLayout}>
            <Space>
              <Button type="primary" htmlType="submit" loading={updateLoading}>
                保存
              </Button>
              <Button onClick={handleGoBack}>返回</Button>
            </Space>
          </FormItem>
        </Form>
      </Skeleton>
    </Card>
  );
};

const mapStateToProps = ({
  loading: { effects },
  npmdModel: { performanceSettingData },
}: ConnectState) => ({
  queryLoading: effects['npmdModel/queryPerformanceSetting'],
  updateLoading: effects['npmdModel/updatePerformanceSetting'],
  performanceSettingData,
});

export default connect(mapStateToProps)(PerformanceSetting);
