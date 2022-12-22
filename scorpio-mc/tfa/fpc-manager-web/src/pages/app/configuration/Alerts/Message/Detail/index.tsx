import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { Card, Divider, Empty, Skeleton, Tag } from 'antd';
import { connect } from 'dva';
import moment from 'moment';
import numeral from 'numeral';
import React, { Fragment, useCallback, useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'redux';
import type { IAlertMessage, IAlertMessageComponent, IAlertRule } from '../../typings';
import { TREND_WEIGHTING_MODEL_ENUM } from '../../typings';
import {
  ALERT_CALCULATION_ENUM,
  ALERT_CATEGORY_ENUM,
  ALERT_LEVEL_ENUM,
  ALERT_METRIC_ENUM,
  EAlertCategory,
} from '../../typings';
import type { AlertModelState } from '../../model';
import Profile from '../../Rule/components/AlertRuleProfile';
import { useParams } from 'react-router';
import { enumValue2Label } from '@/utils/utils';
import DisposeAlert, { EDispose } from '../components/DisposeAlert';
import AlertAnalysis from '../components/AlertAnalysis';

const FormItem = Form.Item;

const formLayout = {
  labelCol: { span: 2 },
  wrapperCol: { span: 19 },
  style: { marginBottom: 0 },
};

interface IAlertMessageProps {
  dispatch: Dispatch<any>;
  detail: IAlertMessage;
  alertRuleDetail: IAlertRule;
  allAlertRule: IAlertRule[];
  queryDetailLoading: boolean;
  location: {
    query: {
      id: string;
    };
  };
}

/**
 * 处理告警结果
 * @param result
 */
const formatterResult = (result: number) => {
  // console.log(result, 'result???');
  let text = result as any;
  if (text) {
    // 判断是否为科学记数法
    if (!isNaN(text) && String(text).indexOf('-') === -1) {
      // 保留2位小数
      text = numeral(text.toFixed(2)).format('0,0');
    }
  }
  return text;
};

const tagProps = {
  color: '#108ee9',
  style: {
    cursor: 'pointer',
  },
};

const Message = (props: IAlertMessageComponent) => {
  const { alertDefine, alertFireContext } = props;
  const { category } = alertDefine || {};

  let message = `在<b>${moment(alertFireContext?.windowStartTime).format(
    'YYYY-MM-DD HH:mm:ss',
  )}</b> ~ <b>${moment(alertFireContext?.windowEndTime).format('YYYY-MM-DD HH:mm:ss')}</b>内，`;

  if (category === EAlertCategory.THRESHOLD) {
    // 指标信息
    const { metrics, source, fireCriteria } = alertDefine.thresholdSettings;
    // 分子的指标
    const numeratorMetricText = enumValue2Label(ALERT_METRIC_ENUM, metrics?.numerator.metric);
    // 分母指标
    const denominatorMetricText = enumValue2Label(ALERT_METRIC_ENUM, metrics?.denominator.metric);
    // 计算方法
    const calculationText = enumValue2Label(ALERT_CALCULATION_ENUM, fireCriteria?.calculation);

    const thresholdResult = alertFireContext?.thresholdResult;
    const thresholdResultText = formatterResult(thresholdResult);

    if (metrics?.isRatio) {
      message += `<b>${numeratorMetricText}</b>指标`;
      message += `与`;
      message += `<b>${denominatorMetricText}</b>指标`;
      message += `<b>${calculationText}</b>`;
      message += `比率为`;
      message += `<b>${thresholdResultText}</b>`;
    } else {
      message += `<b>${numeratorMetricText}</b>`;
      message += `指标数据`;
      message += `<b>${calculationText}</b>`;
      message += `为`;
      message += `<b>${thresholdResultText}</b>`;
    }
    message += '。';

    return <span dangerouslySetInnerHTML={{ __html: message }} />;
  }
  if (category === EAlertCategory.TREND) {
    // 指标信息
    const { metrics, source, fireCriteria, trend } = alertDefine.trendSettings;
    // 分子的指标
    const numeratorMetricText = enumValue2Label(ALERT_METRIC_ENUM, metrics?.numerator.metric);
    // 分母指标
    const denominatorMetricText = enumValue2Label(ALERT_METRIC_ENUM, metrics?.denominator.metric);
    // 计算方法
    const calculationText = enumValue2Label(TREND_WEIGHTING_MODEL_ENUM, trend.weightingModel);
    // 基线值
    const trendBaseline = alertFireContext?.trendBaseline;
    const trendBaselineText = formatterResult(trendBaseline);
    // 实际值
    const trendResult = alertFireContext?.trendResult;
    const trendResultText = formatterResult(trendResult);

    //趋势百分比
    // const trendPercent = alertFireContext?.trendPercent;
    // const trendPercentText = formatterResult(trendPercent);

    const resultText = `基线值为<b>${trendBaselineText}</b>，实际值<b>${enumValue2Label(
      ALERT_CALCULATION_ENUM,
      fireCriteria?.calculation,
    )}</b>为<b>${trendResultText}</b>${
      alertFireContext?.trendPercent
        ? '，趋势百分比为' + '<b>' + `${alertFireContext.trendPercent.toFixed(2)}` + '</b>'
        : ''
    }`;

    if (metrics?.isRatio) {
      message += `<b>${numeratorMetricText}</b>指标`;
      message += `与`;
      message += `<b>${denominatorMetricText}</b>指标`;
      message += `<b>${calculationText}</b>`;
      message += `比率的`;
      message += resultText;
    } else {
      message += `<b>${numeratorMetricText}</b>`;
      message += `指标数据`;
      message += `<b>${calculationText}</b>`;
      message += '的';
      message += resultText;
    }
    message += '。';
    // TODO: 基线告警无法画图，先直接显示内容值
    return <span dangerouslySetInnerHTML={{ __html: message }} />;
  }
  return null;
};

const AlertMessageSlimProfile: React.FC<IAlertMessageComponent> = (props) => {
  const { alertDefine } = props;
  return (
    <Form>
      <FormItem key="alert-item-name" {...formLayout} label="告警名称">
        <span className="ant-form-text">{alertDefine.name}</span>
        <Profile id={alertDefine.id} category={alertDefine.category}>
          <Tag {...tagProps}>查看告警配置</Tag>
        </Profile>
      </FormItem>
      <FormItem key="alert-item-sourceType" {...formLayout} label="告警分类">
        <span className="ant-form-text">
          {enumValue2Label(ALERT_CATEGORY_ENUM, alertDefine.category)}
        </span>
      </FormItem>
      {/* 阈值告警内容 */}
      {alertDefine.category === EAlertCategory.THRESHOLD && (
        <FormItem key="threshole-message" {...formLayout} label="告警详情">
          <span className="ant-form-text">
            <Message {...props} />
          </span>
        </FormItem>
      )}

      {/* 基线告警详情 */}
      {alertDefine.category === EAlertCategory.TREND && (
        <FormItem key="trend-message" {...formLayout} label="告警详情">
          <span className="ant-form-text">
            <Message {...props} />
          </span>
        </FormItem>
      )}
    </Form>
  );
};

const AlertMessageContent: React.FC<IAlertMessageProps> = (props) => {
  const { dispatch, alertRuleDetail, detail = {} as IAlertMessage, queryDetailLoading } = props;

  const { alertId }: { alertId: string } = useParams();
  const [description, setDescription] = useState<string>('');
  useEffect(() => {
    if (dispatch) {
      dispatch({
        type: 'alertModel/queryAlertMessageDetail',
        payload: {
          id: alertId,
        },
      });
    }
  }, [dispatch, alertId]);

  const Refresh = useCallback(() => {
    if (dispatch) {
      dispatch({
        type: 'alertModel/queryAlertMessageDetail',
        payload: {
          id: alertId,
        },
      });
    }
  }, [alertId, dispatch]);

  useEffect(() => {
    if (dispatch && detail.category === EAlertCategory.ADVANCED) {
      dispatch({
        type: 'alertModel/queryAllAlertRules',
        payload: {
          category: [EAlertCategory.THRESHOLD, EAlertCategory.TREND].join(','),
        },
      });
    }
  }, [dispatch, detail.category]);

  useEffect(() => {
    dispatch({
      type: 'alertModel/queryAlertRuleDetail',
      payload: {
        id: detail.alertId,
      },
    });
  }, [detail]);

  useEffect(() => {
    if (alertRuleDetail.description !== undefined) {
      setDescription(alertRuleDetail.description);
    }
  }, [alertRuleDetail]);

  if (queryDetailLoading) {
    return <Skeleton active />;
  }

  if (!detail.id) {
    return <Empty description="告警消息不存在或已被删除" />;
  }

  return (
    <Card bordered={false}>
      <Form>
        <FormItem key="id" {...formLayout} label="id" style={{ display: 'none' }}>
          <span className="ant-form-text">{detail.id}</span>
        </FormItem>
        <FormItem key="name" {...formLayout} label="告警名称">
          <span className="ant-form-text">{detail.name}</span>
          {/* 告警设置详情 */}
          <Profile id={detail.alertId} category={detail.category}>
            <Tag {...tagProps}>查看告警配置</Tag>
          </Profile>
        </FormItem>
        <FormItem key="level" {...formLayout} label="告警级别">
          <span className="ant-form-text">{enumValue2Label(ALERT_LEVEL_ENUM, detail.level)}</span>
        </FormItem>
        <FormItem key="category" {...formLayout} label="告警分类">
          <span className="ant-form-text">
            {enumValue2Label(ALERT_CATEGORY_ENUM, detail.category)}
          </span>
        </FormItem>
        <FormItem key="category" {...formLayout} label="触发时间">
          <span className="ant-form-text">
            {moment(detail.ariseTime).format('YYYY-MM-DD HH:mm:ss')}
          </span>
        </FormItem>

        {/* TODO: 维度没有了。这里应该是网络 ID 或者是业务 ID */}

        {/* 阈值告警详情 */}
        {detail.category === EAlertCategory.THRESHOLD && (
          <FormItem key="threshole-message" {...formLayout} label="告警详情">
            <span className="ant-form-text">
              <Message {...detail.components[0]} />
            </span>
          </FormItem>
        )}

        {/* 基线告警详情 */}
        {detail.category === EAlertCategory.TREND && (
          <FormItem key="trend-message" {...formLayout} label="告警详情">
            <span className="ant-form-text">
              <Message {...detail.components[0]} />
            </span>
          </FormItem>
        )}

        {/* 组合告警 */}
        {detail.category === EAlertCategory.ADVANCED &&
          Array.isArray(detail.components) &&
          detail.components.map((alertItem, index) => (
            <Card
              key={`${alertItem.alertId}_${index + 1}`}
              size="small"
              title={`子告警${index + 1} `}
              style={{ marginBottom: 10 }}
            >
              <AlertMessageSlimProfile {...alertItem} />
            </Card>
          ))}
      </Form>
      {detail.reason && (
        <FormItem key="reason" {...formLayout} label="处理结果">
          <span>{detail.reason}</span>
        </FormItem>
      )}
      {description && (
        <FormItem key="alert-item-sourceType" {...formLayout} label="告警配置描述">
          <span className="ant-form-text">{description}</span>
        </FormItem>
      )}
      {detail.status === EDispose.Untreated && (
        <FormItem key="option" {...formLayout} label="操作">
          <DisposeAlert
            id={detail.id}
            buttonType={'primary'}
            onChange={Refresh}
            // disable={detail.status === EDispose.Processed}
          />
        </FormItem>
      )}
      {detail?.category !== EAlertCategory.ADVANCED &&
        detail?.alertDefine?.thresholdSettings?.metrics?.numerator?.metric !==
          'broadcast_packets' &&
        detail?.alertDefine?.thresholdSettings?.metrics?.denominator?.metric !==
          'broadcast_packets' &&
        detail?.alertDefine?.thresholdSettings?.metrics?.numerator?.metric !== 'long_connections' &&
        detail?.alertDefine?.thresholdSettings?.metrics?.denominator?.metric !==
          'long_connections' && (
          <FormItem key="analysis" {...formLayout} label="分析">
            <AlertAnalysis alertDetail={detail} />
          </FormItem>
        )}
    </Card>
  );
};

export default connect(
  ({
    alertModel: { alertMessageDetail, allAlertRule, alertRuleDetail },
    loading: { effects },
  }: {
    alertModel: AlertModelState;
    loading: { effects: Record<string, boolean> };
  }) => ({
    detail: alertMessageDetail,
    alertRuleDetail,
    allAlertRule,
    queryDetailLoading: effects['alertModel/queryAlertMessageDetail'],
  }),
)(AlertMessageContent);
