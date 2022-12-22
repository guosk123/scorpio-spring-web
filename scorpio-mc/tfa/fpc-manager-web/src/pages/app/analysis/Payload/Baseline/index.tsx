import type { ConnectState } from '@/models/connect';
import { ExclamationCircleOutlined } from '@ant-design/icons';
import { Button, Card, Form, InputNumber, Modal, Select, Skeleton, Space } from 'antd';
import React, { useCallback, useEffect, useMemo } from 'react';
import type { Dispatch } from 'umi';
import { connect, history, useParams } from 'umi';
import type { IBaselineSettingData, IUriParams } from '../../typings';
import { EBaselineCategory } from '../../typings';
import { ESourceType } from '../../typings';

const { confirm } = Modal;
const FormItem = Form.Item;
const { Option } = Select;

const groups = {
  带宽基线定义: EBaselineCategory.BANDWIDTH,
  流量基线定义: EBaselineCategory.FLOW,
  数据包基线定义: EBaselineCategory.PACKET,
};

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

const formLayout = {
  wrapperCol: { span: 12 },
  labelCol: { span: 4 },
};

const trendLayout = {
  wrapperCol: { span: 12 },
};

const buttonLayout = {
  wrapperCol: { offset: 4 },
};

export interface IBaselineProps {
  dispatch: Dispatch;
  queryLoading: boolean | undefined;
  updateLoading: boolean | undefined;
  baseLineSettingData: IBaselineSettingData[];
}

const Baseline: React.FC<IBaselineProps> = ({
  dispatch,
  baseLineSettingData,
  queryLoading,
  updateLoading,
}) => {
  const { networkId, serviceId = '' } = useParams() as IUriParams;
  const [form] = Form.useForm();

  const sourceType = useMemo(() => {
    return serviceId ? ESourceType.SERVICE : ESourceType.NETWORK;
  }, [serviceId]);

  const queryBaselineSetting = useCallback(() => {
    dispatch({
      type: 'npmdModel/queryBaselineSetting',
      payload: {
        sourceType,
        networkId,
        serviceId,
      },
    });
  }, [dispatch, sourceType, networkId, serviceId]);

  useEffect(() => {
    queryBaselineSetting();
  }, [queryBaselineSetting]);

  useEffect(() => {
    const formData = {};
    Object.keys(groups).forEach((key) => {
      formData[groups[key]] = {
        weightingModel: undefined,
        windowingModel: undefined,
        windowingCount: undefined,
      };
    });
    if (baseLineSettingData.length > 0) {
      baseLineSettingData.forEach((item) => {
        formData[item.category] = {
          weightingModel: item.weightingModel,
          windowingModel: item.windowingModel,
          windowingCount: item.windowingCount,
        };
      });
    }
    form.setFieldsValue(formData);
  }, [form, baseLineSettingData]);

  const handleGoBack = () => {
    history.goBack();
  };

  const onFinish = (values: any) => {
    const baselineSettings: IBaselineSettingData[] = [];
    Object.keys(values).forEach((key) => {
      baselineSettings.push({
        sourceType,
        networkId,
        serviceId,
        category: key,
        ...values[key],
      });
    });
    confirm({
      title: '是否确认保存?',
      icon: <ExclamationCircleOutlined />,
      onOk() {
        dispatch({
          type: 'npmdModel/updataBaselineSetting',
          payload: {
            baselineSettings: JSON.stringify(baselineSettings),
          },
        }).then(() => queryBaselineSetting());
      },
    });
  };

  return (
    <Card bordered={false}>
      <Skeleton active loading={queryLoading}>
        <Form form={form} {...formLayout} onFinish={onFinish}>
          {Object.keys(groups).map((groupLabel) => {
            return (
              <FormItem label={groupLabel} key={groupLabel}>
                <Card>
                  <FormItem
                    label="权重模型"
                    rules={[{ required: true, message: '请选择权重模型' }]}
                    name={[groups[groupLabel], 'weightingModel']}
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
                    rules={[{ required: true, message: '请选择基线窗口' }]}
                    name={[groups[groupLabel], 'windowingModel']}
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
                    rules={[{ required: true, message: '请填写回顾周期' }]}
                    name={[groups[groupLabel], 'windowingCount']}
                  >
                    <InputNumber min={1} precision={0} />
                  </FormItem>
                </Card>
              </FormItem>
            );
          })}
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
  npmdModel: { baseLineSettingData },
}: ConnectState) => ({
  queryLoading: effects['npmdModel/queryBaselineSetting'],
  updateLoading: effects['npmdModel/updataBaselineSetting'],
  baseLineSettingData,
});

export default connect(mapStateToProps)(Baseline);
