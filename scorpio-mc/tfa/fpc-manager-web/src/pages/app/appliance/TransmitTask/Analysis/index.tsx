import type { ConnectState } from '@/models/connect';
import type { IPktAnalysisSharedProps } from '@/pages/app/PktAnalysis';
import PktAnalysis from '@/pages/app/PktAnalysis';
import { EPktAnalysisDataSource } from '@/pages/app/PktAnalysis/typings';
import { Card, Result, Spin } from 'antd';
import React, { useEffect, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, useParams } from 'umi';
import type { ITransmitTask } from '../typings';
import { ETransmitTaskState } from '../typings';

interface ITransmitTaskAnalysisProps {
  dispatch: Dispatch;
  taskDetail: ITransmitTask;
  queryTaskDetailLoading: boolean | undefined;
}

const TransmitTaskAnalysis: React.FC<ITransmitTaskAnalysisProps> = ({
  dispatch,
  taskDetail,
  queryTaskDetailLoading,
}) => {
  const [isReady, setIsReady] = useState(false);
  const { taskId }: { taskId: string } = useParams();

  useEffect(() => {
    if (!taskId) {
      return;
    }
    dispatch({
      type: 'transmitTaskModel/queryTransmitTasksDetail',
      payload: { id: taskId },
    });
  }, [taskId, dispatch]);

  useEffect(() => {
    setIsReady(true);
  }, []);

  if (!isReady || queryTaskDetailLoading) {
    return (
      <Card bordered={false} bodyStyle={{ textAlign: 'center' }}>
        <Spin />
      </Card>
    );
  }

  if (!taskId || !taskDetail.id) {
    return (
      <Card bordered={false}>
        <Result status="warning" title="没有找到相关查询任务" />
      </Card>
    );
  }

  // 如果任务没有结束，给出提示
  if (taskDetail.state !== ETransmitTaskState.FINISHED) {
    return (
      <Card bordered={false}>
        <Result
          status="warning"
          title="查询任务尚未执行完成，请稍候再试"
          subTitle={`当前任务执行进度：${taskDetail.executionProgress}%`}
        />
      </Card>
    );
  }

  const pktAnalysisProps: IPktAnalysisSharedProps = {
    sourceType: EPktAnalysisDataSource['transmit-task'],
    taskId: taskDetail.id,
  };

  return <PktAnalysis {...pktAnalysisProps} />;
};

export default connect(
  ({ loading: { effects }, transmitTaskModel: { detail: taskDetail } }: ConnectState) => ({
    taskDetail,
    queryTaskDetailLoading: effects['transmitTaskModel/queryTransmitTasksDetail'],
  }),
)(TransmitTaskAnalysis);
