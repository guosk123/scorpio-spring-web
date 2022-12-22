import type { ConnectState } from '@/models/connect';
import { Card, Empty, Result, Skeleton } from 'antd';
import { connect } from 'dva';
import React, { useEffect } from 'react';
import { useParams } from 'react-router';
import type { Dispatch } from 'redux';
import TransmitTaskForm, { EPageMode } from '../components/TransmitTaskForm';
import type { ITransmitTask } from '../typings';
import { ETransmitTaskState } from '../typings';

interface IUpdateTransmitTaskProps {
  dispatch: Dispatch<any>;
  detail: ITransmitTask;
  queryLoading: boolean | undefined;
}

const UpdateTransmitTask: React.FC<IUpdateTransmitTaskProps> = ({
  dispatch,
  queryLoading = true,
  detail,
}) => {
  const params = useParams() as { taskId: string };
  useEffect(() => {
    // 清除上次的任务详情
    dispatch({ type: 'transmitTaskModel/clearTransmitTasksDetail' });

    dispatch({
      type: 'transmitTaskModel/queryTransmitTasksDetail',
      payload: {
        id: params.taskId,
      },
    });
  }, [params.taskId, dispatch]);

  if (queryLoading) {
    return <Skeleton active />;
  }

  if (!detail.id) {
    return <Empty description="任务不存在或已被删除" />;
  }

  const { state, executionStartTime } = detail;
  // 进行中
  const isGoing = state === ETransmitTaskState.START;
  // 正在停止
  const isStopping = state === ETransmitTaskState.STOPPED && executionStartTime;
  if (isGoing || isStopping) {
    return <Result status="warning" title="任务当前正在执行中，无法修改" />;
  }

  return (
    <Card bordered={false}>
      <TransmitTaskForm detail={detail} pageMode={EPageMode.Update} />
    </Card>
  );
};

export default connect(({ transmitTaskModel: { detail }, loading }: ConnectState) => ({
  detail,
  queryLoading: loading.effects['transmitTaskModel/queryTransmitTasksDetail'],
}))(UpdateTransmitTask);
