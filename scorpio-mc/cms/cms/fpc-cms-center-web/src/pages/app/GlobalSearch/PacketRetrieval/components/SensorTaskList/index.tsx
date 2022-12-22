import { ONE_KILO_1024 } from '@/common/dict';
import { bytesToSize, formatDuration } from '@/utils/utils';
import { PlusSquareOutlined } from '@ant-design/icons';
import { Button, message, Modal, Space, Table } from 'antd';
import type { ColumnsType } from 'antd/lib/table';
import moment from 'moment';
import { useEffect, useState } from 'react';
import { downloadTransmitTaskFile, querySensorTaskList } from '../../service';
import type { ITransmitTask, sensorTaskListItem } from '../../typings';
import SensorTaskDetail from '../SensorTaskDetail';
import {
  CONNECT_STATUS_FAILED_COLOR,
  CONNECT_STATUS_OK,
  CONNECT_STATUS_OK_COLOR,
  TASK_ASSIGNMENT_STATE_SUCCESS,
  TASK_EXECUTION_STATE_FINISHED,
  TASK_EXECUTION_STATE_RUNNING,
  TASK_EXECUTION_STATE_STOPPED,
} from './constant';
import styles from './index.less';

interface Props {
  taskDetail: ITransmitTask;
}

export default function SensorTaskList(props: Props) {
  const { taskDetail } = props;

  /** 下载 */
  const handleDownload = (info: any) => {
    const { taskId, fpcSerialNumber } = info;
    if (!taskId || !fpcSerialNumber) {
      message.error('缺失参数');
      return false;
    }
    return downloadTransmitTaskFile(info).then((response) => {
      const { success, result } = response;
      if (success) {
        const { filePath } = result;
        window.open(filePath);
      }
    });
  };

  const columns: ColumnsType<sensorTaskListItem> = [
    {
      title: '设备名称',
      dataIndex: 'fpcName',
      align: 'center',
      ellipsis: true,
    },
    {
      title: '设备IP',
      dataIndex: 'fpcIp',
      align: 'center',
    },
    {
      title: '连接状态',
      dataIndex: 'connectStatusText',
      align: 'center',
      render: (text, record) => {
        const connectColor =
          record.connectStatus === CONNECT_STATUS_OK
            ? CONNECT_STATUS_OK_COLOR
            : CONNECT_STATUS_FAILED_COLOR;
        return <span style={{ color: connectColor }}>{text}</span>;
      },
    },
    {
      title: '下发状态',
      dataIndex: 'assignmentStateText',
      align: 'center',
    },
    {
      title: '任务开始时间',
      dataIndex: 'executionStartTime',
      align: 'center',
      width: 176,
      render: (text, record) => {
        const { assignmentState } = record;
        // 下发失败时，这里不显示任务开始时间
        if (assignmentState !== TASK_ASSIGNMENT_STATE_SUCCESS) {
          return '--';
        }
        return (text && moment(text).format('YYYY-MM-DD HH:mm:ss')) || '--';
      },
    },
    {
      title: '执行进度',
      dataIndex: 'executionProgress',
      align: 'center',
      render: (text, record) => {
        const { assignmentState, executionState, executionStartTime, executionEndTime } = record;
        // 任务执行状态
        let taskStateText = '--';
        if (assignmentState !== TASK_ASSIGNMENT_STATE_SUCCESS) {
          return taskStateText;
        }

        // 任务开始，有开始时间，没有结束时间：正在执行
        if (
          executionState === TASK_EXECUTION_STATE_RUNNING &&
          executionStartTime &&
          !executionEndTime
        ) {
          taskStateText = '执行中';
        }
        // 任务开始，并且没有开始时间：等待执行
        else if (executionState === TASK_EXECUTION_STATE_RUNNING && !executionStartTime) {
          taskStateText = '等待执行';
        } else if (executionState === TASK_EXECUTION_STATE_STOPPED && executionStartTime) {
          taskStateText = '正在停止';
        } else if (executionState === TASK_EXECUTION_STATE_STOPPED && !executionStartTime) {
          taskStateText = '已停止';
        } else if (executionState === TASK_EXECUTION_STATE_FINISHED) {
          taskStateText = `${text}%`;
        }

        return taskStateText;
      },
    },
    {
      title: '执行时间',
      dataIndex: 'executionTime',
      align: 'center',
      render: (text, record) => {
        const { executionStartTime, executionEndTime, assignmentState } = record;
        if (assignmentState !== TASK_ASSIGNMENT_STATE_SUCCESS) {
          return '--';
        }
        if (executionStartTime && executionEndTime) {
          const startTime = moment(executionStartTime);
          const endTime = moment(executionEndTime);
          // http://momentjs.cn/docs/#/displaying/difference/
          const diff = endTime.diff(startTime, 'ms');

          if (diff === 0) {
            return '小于1s';
          }

          return formatDuration(diff);
        }
        return '--';
      },
    },
    {
      title: '落盘PCAP文件大小',
      dataIndex: 'executionTrace',
      align: 'center',
      render: (executionTrace) => {
        // 任务还没有完成
        if (!executionTrace) {
          return '--';
        }
        let executionTraceJson: any = {};
        try {
          executionTraceJson = JSON.parse(executionTrace);
        } catch (err) {
          executionTraceJson = {};
        }
        return bytesToSize(executionTraceJson.writeBytes || 0, 3, ONE_KILO_1024);
      },
    },
    {
      title: '操作',
      width: 160,
      align: 'center',
      render: (text, record) => {
        const { executionState } = record;
        return (
          <Space>
            <Button
              type={'link'}
              size={'small'}
              onClick={() =>
                handleDownload({
                  taskId: taskDetail.id,
                  fpcSerialNumber: record.fpcSerialNumber,
                })
              }
              disabled={executionState !== TASK_EXECUTION_STATE_FINISHED}
            >
              下载
            </Button>
            <SensorTaskDetail detail={record} taskDetail={taskDetail} />
          </Space>
        );
      },
    },
  ];

  const [isVisibleState, setIsVisibleState] = useState(false);
  const [data, setData] = useState<any>([]);
  const [queryLoading, setQueryLoading] = useState(false);

  const [taskListState, setTaskListState] = useState({
    page: 1,
    pageSize: 10,
  });

  useEffect(() => {
    if (isVisibleState) {
      setQueryLoading(true);
      querySensorTaskList({
        taskId: taskDetail.id,
        page: taskListState.page - 1,
        pageSize: taskListState.pageSize,
      }).then((res) => {
        const { success, result } = res;
        if (success) {
          setData(result.content);
          setQueryLoading(false);
        }
      });
    }
  }, [isVisibleState, taskDetail.id, taskListState]);

  return (
    <div>
      <PlusSquareOutlined
        onClick={() => {
          setIsVisibleState(true);
        }}
      />
      <Modal
        visible={isVisibleState}
        width={'100%'}
        bodyStyle={{ height: '80vh' }}
        footer={false}
        onCancel={() => {
          setIsVisibleState(false);
        }}
      >
        <Table
          className={styles.assignmentTable}
          rowKey="fpcId"
          size="middle"
          loading={queryLoading}
          bordered
          style={{ width: '95%', margin: '0 auto' }}
          columns={columns}
          dataSource={data}
          pagination={{
            onChange: (page) => {
              setTaskListState({
                ...taskListState,
                page,
              });
            },
          }}
        />
      </Modal>
    </div>
  );
}
