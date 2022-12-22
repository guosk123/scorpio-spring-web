import type { ConnectState } from '@/models/connect';
import { Alert, Card, Empty, Skeleton } from 'antd';
import { connect } from 'dva';
import React, { useEffect } from 'react';
import { useParams } from 'react-router';
import type { Dispatch } from 'redux';
import TransmitTaskForm, { EPageMode } from '../components/TransmitTaskForm';
import type { ITransmitTask } from '../typings';

interface ICopyTransmitTaskProps {
  dispatch: Dispatch<any>;
  detail: ITransmitTask;
  queryLoading: boolean | undefined;
}

const CopyTransmitTask: React.FC<ICopyTransmitTaskProps> = ({
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

  return (
    <Card bordered={false}>
      <Alert message="保存后生成新的查询任务" type="info" showIcon />
      <TransmitTaskForm
        // 注意清空原有 ID
        detail={{ ...detail, id: '', name: `${detail.name}_copy` }}
        pageMode={EPageMode.Copy}
      />
    </Card>
  );
};

export default connect(({ transmitTaskModel: { detail }, loading }: ConnectState) => ({
  detail,
  queryLoading: loading.effects['transmitTaskModel/queryTransmitTasksDetail'],
}))(CopyTransmitTask);
