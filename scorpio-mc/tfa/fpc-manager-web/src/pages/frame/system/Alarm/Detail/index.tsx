import type { ConnectState } from '@/models/connect';
import { Badge, Button, Card, Descriptions, Divider, Empty, Result, Skeleton } from 'antd';
import { connect } from 'dva';
import moment from 'moment';
import { Fragment, useCallback, useEffect } from 'react';
import type { Dispatch } from 'umi';
import { history, useLocation } from 'umi';
import AlermSolveModel from '../components/SolveModel';
import type { ISystemAlarm } from '../typings';
import { ESystemAlarmStatus } from '../typings';

interface IProps {
  detail: ISystemAlarm;
  dispatch: Dispatch;
  loading?: boolean;
}
const AlarmDetail = ({ dispatch, detail, loading }: IProps) => {
  const { query } = useLocation() as any as { query: { id: string } };

  const queryDetail = useCallback(() => {
    dispatch({
      type: 'alarmModel/queryAlarmDetail',
      payload: {
        id: query.id,
      },
    });
  }, [dispatch, query.id]);

  useEffect(() => {
    queryDetail();
  }, [queryDetail]);

  const showSolveModal = () => {
    dispatch({
      type: 'alarmModel/showModal',
      payload: {
        currentItem: detail,
      },
    });
  };

  const { id, status } = detail;

  if (loading) {
    return <Skeleton active />;
  }

  if (!id) {
    return <Empty />;
  }

  const isResolved = id && status === ESystemAlarmStatus.Resolved;

  const extra = !isResolved ? (
    <Button onClick={showSolveModal} type="primary">
      解决
    </Button>
  ) : (
    <Button type="primary" onClick={() => history.goBack()}>
      返回列表
    </Button>
  );

  return (
    <Fragment>
      <Card bordered={false}>
        {!id ? (
          <Empty description="没有找到相关告警信息" />
        ) : (
          <Result
            status={isResolved ? 'success' : 'warning'}
            style={{ paddingTop: 0 }}
            title={isResolved ? '已解决' : '未处理'}
            extra={extra}
          >
            <Descriptions style={{ marginBottom: 24 }} column={1}>
              <Descriptions.Item label="告警级别">{detail.level}</Descriptions.Item>
              <Descriptions.Item label="组件">{detail.component}</Descriptions.Item>
              <Descriptions.Item label="告警类型">{detail.category}</Descriptions.Item>
              <Descriptions.Item label="告警关键字">{detail.keyword || '--'}</Descriptions.Item>
              <Descriptions.Item label="告警时间">
                {detail.ariseTime ? moment(detail.ariseTime).format('YYYY-MM-DD HH:mm:ss') : '--'}
              </Descriptions.Item>
              <Descriptions.Item label="当前状态">
                {isResolved ? <Badge status="success" text="已解决" /> : '未处理'}
              </Descriptions.Item>
              <Descriptions.Item label="告警内容">{detail.content}</Descriptions.Item>
              {isResolved && (
                <Fragment>
                  <Divider dashed />
                  <Descriptions.Item label="解决人员">{detail.solver}</Descriptions.Item>
                  <Descriptions.Item label="解决时间">
                    {detail.solveTime
                      ? moment(detail.solveTime).format('YYYY-MM-DD HH:mm:ss')
                      : '--'}
                  </Descriptions.Item>
                  <Descriptions.Item label="解决备注">{detail.reason}</Descriptions.Item>
                </Fragment>
              )}
            </Descriptions>
          </Result>
        )}
      </Card>
      <AlermSolveModel callback={queryDetail} />
    </Fragment>
  );
};

export default connect(
  ({
    loading: { effects },
    alarmModel: { alarmDetail },
  }: ConnectState & {
    alarmModel: {
      alarmDetail: ISystemAlarm;
    };
  }) => ({
    detail: alarmDetail,
    loading: effects['alarmModel/queryAlarmDetail'],
  }),
)(AlarmDetail);
