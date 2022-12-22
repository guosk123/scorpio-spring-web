import { Alert, Card, Empty, message } from 'antd';
import { connect } from 'dva';
import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router';
import type { Dispatch } from 'redux';
import TransmitTaskForm from '../components/TransmitTaskForm';
import { queryTransmitTasksDetail } from '../service';
import type { ITransmitTask } from '../typings';

interface ICopyTransmitTaskProps {
  dispatch: Dispatch<any>;
  detail: ITransmitTask;
}

const CopyTransmitTask: React.FC<ICopyTransmitTaskProps> = () => {
  const params = useParams() as { taskId: string };

  const [detail, setDetail] = useState<any>();

  const [queryLoading, setQueryLoading] = useState(true);

  useEffect(() => {
    queryTransmitTasksDetail({ id: params.taskId }).then((res) => {
      const { success, result } = res;
      setQueryLoading(false);
      if (success) {
        setDetail(result);
      } else {
        message.error('获取详情失败');
      }
    });
  }, [params.taskId]);

  if (!detail?.id && !queryLoading) {
    return <Empty description="任务不存在或已被删除" />;
  }

  return (
    <Card bordered={false}>
      <Alert message="保存后生成新的查询任务" type="info" showIcon />
      {detail?.id && (
        <TransmitTaskForm
          // 注意清空原有 ID
          detail={{ ...detail, id: '', name: `${detail.name}_copy` }}
        />
      )}
    </Card>
  );
};

export default connect()(CopyTransmitTask);
