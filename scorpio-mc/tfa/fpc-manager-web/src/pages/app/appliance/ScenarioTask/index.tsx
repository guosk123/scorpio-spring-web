import { getTablePaginationDefaultSettings } from '@/common/app';
import { formatDuration, getLinkUrl } from '@/utils/utils';
import { PlusOutlined, QuestionCircleOutlined, WarningTwoTone } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Button, Divider, Popconfirm, Tooltip } from 'antd';
import moment from 'moment';
import { useRef } from 'react';
import type { Dispatch } from 'umi';
import { connect, history, Link } from 'umi';
import { queryScenarioTasks } from './service';
import type { IScenarioTask } from './typings';

/**
 * 进行中
 */
export const TASK_STATE_START = '0';
/**
 * 任务被停止
 */
export const TASK_STATE_STOP = '1';
/**
 * 正常已完成
 */
export const TASK_STATE_FINISHED = '2';
/**
 * 异常已完成
 */
export const TASK_STATE_FAIL_FINISHED = '3';

export const getTaskState = ({ state, executionStartTime, executionEndTime }: any) => {
  // 任务开始，有开始时间，没有结束时间：正在执行
  if (state === TASK_STATE_START && executionStartTime && !executionEndTime) {
    return '执行中';
  }
  // 任务开始，并且没有开始时间：等待执行
  if (state === TASK_STATE_START && !executionStartTime) {
    return '等待执行';
  }
  if (state === TASK_STATE_FINISHED || state === TASK_STATE_FAIL_FINISHED) {
    return '已完成';
  }
  return '--';
};

export const getTaskDurationTime = ({ executionStartTime, executionEndTime }: any) => {
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
};

interface IProps {
  dispatch: Dispatch;
}
const ScenarioTask = ({ dispatch }: IProps) => {
  const actionRef = useRef<ActionType>();

  // 删除
  const handleDelete = ({ id }: IScenarioTask) => {
    dispatch({
      type: 'scenarioTaskModel/deleteScenarioTask',
      payload: { id },
    }).then(() => {
      actionRef.current?.reload();
    });
  };

  const tableColumns: ProColumns<IScenarioTask>[] = [
    {
      title: '名称',
      dataIndex: 'name',
      align: 'center',
      search: false,
    },
    {
      title: '分析数据时间范围',
      dataIndex: 'startTimeAndEndTime',
      align: 'center',
      width: 180,
      search: false,
      renderText: (text, record) => {
        const { analysisStartTime, analysisEndTime } = record;
        return (
          <>
            <div>{moment(analysisStartTime).format('YYYY-MM-DD HH:mm:ss')}</div>
            <div>{moment(analysisEndTime).format('YYYY-MM-DD HH:mm:ss')}</div>
          </>
        );
      },
    },
    {
      title: '分析场景',
      dataIndex: 'typeText',
      align: 'center',
      search: false,
    },
    {
      title: '任务开始时间',
      dataIndex: 'executionStartTime',
      align: 'center',
      search: false,
      width: 180,
      valueType: 'dateTime',
    },
    {
      title: '任务状态',
      dataIndex: 'state',
      align: 'center',
      search: false,
      renderText: (state, record) => {
        const { executionStartTime, executionEndTime, executionTrace } = record;
        const stateText = getTaskState({ state, executionStartTime, executionEndTime });
        return (
          <span>
            {stateText}
            {executionTrace && (
              <Tooltip title={executionTrace}>
                <WarningTwoTone twoToneColor="#faad14" style={{ marginLeft: 4 }} />
              </Tooltip>
            )}
          </span>
        );
      },
    },
    // {
    //   title: '执行进度',
    //   dataIndex: 'executionProgress',
    //   align: 'center',
    //   width: 75,
    //   render: text => (text ? `${text}%` : '--'),
    // },
    {
      title: '执行时间',
      dataIndex: 'executionTime',
      align: 'center',
      search: false,
      renderText: (text, record) => getTaskDurationTime(record),
    },
    {
      title: '操作',
      width: 140,
      dataIndex: 'action',
      align: 'center',
      valueType: 'option',
      search: false,
      render: (text, record) => {
        const { id, executionProgress, state } = record;
        return (
          <>
            {state === TASK_STATE_FINISHED && executionProgress === 100 && (
              <>
                <Link to={getLinkUrl(`/analysis/security/scenario-task/result?id=${id}`)}>结果</Link>
                <Divider type="vertical" />
              </>
            )}
            <Popconfirm
              title="确定删除吗？"
              onConfirm={() => handleDelete(record)}
              icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
            >
              <a>删除</a>
            </Popconfirm>
          </>
        );
      },
    },
  ];

  return (
    <ProTable<IScenarioTask>
      bordered
      rowKey="id"
      size="small"
      actionRef={actionRef}
      columns={tableColumns}
      polling={3 * 1000}
      request={async (params = {}) => {
        const { current, pageSize, ...rest } = params;
        const newParams = {
          pageSize,
          page: current! - 1,
          ...rest,
        } as any;
        const { success, result } = await queryScenarioTasks(newParams);

        if (!success) {
          return {
            data: [],
            success,
          };
        }

        return {
          data: result.content,
          success,
          page: result.number,
          total: result.totalElements,
        };
      }}
      search={{
        optionRender: () => [
          <Button
            key="create"
            icon={<PlusOutlined />}
            type="primary"
            onClick={() => history.push(getLinkUrl('/analysis/security/scenario-task/create'))}
          >
            新建
          </Button>,
        ],
      }}
      toolBarRender={false}
      pagination={getTablePaginationDefaultSettings()}
    />
  );
};

export default connect()(ScenarioTask);
