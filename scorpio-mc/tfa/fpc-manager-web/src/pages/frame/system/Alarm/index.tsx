import { getTablePaginationDefaultSettings, proTableSerchConfig } from '@/common/app';
import { SYSTEM_ALARM_LEVEL, SYSTEM_COMPONENT } from '@/common/dict';
import { queryAlarms } from '@/services/frame/alarm';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Button } from 'antd';
import { connect } from 'dva';
import moment from 'moment';
import { useRef } from 'react';
import type { Dispatch } from 'umi';
import { Link } from 'umi';
import AlarmSolveModel from './components/SolveModel';
import type { ISystemAlarm } from './typings';
import { ESystemAlarmStatus } from './typings';

const levelEnumValue = SYSTEM_ALARM_LEVEL.reduce((prev, current) => {
  return {
    ...prev,
    [current.key]: { text: current?.label, status: current?.status_color },
  };
}, {});

const componentEnumValue = SYSTEM_COMPONENT.reduce((prev, current) => {
  return {
    ...prev,
    [current.key]: { text: current?.label },
  };
}, {});

interface IProps {
  dispatch: Dispatch;
}
const SystemAlarm = ({ dispatch }: IProps) => {
  const actionRef = useRef<ActionType>();

  const showSolveModal = (record: ISystemAlarm) => {
    dispatch({
      type: 'alarmModel/showModal',
      payload: {
        currentItem: record,
      },
    });
  };

  const columns: ProColumns<ISystemAlarm>[] = [
    {
      title: '级别',
      dataIndex: 'level',
      align: 'center',
      width: 100,
      valueType: 'select',
      valueEnum: levelEnumValue,
    },
    {
      title: '组件',
      dataIndex: 'component',
      align: 'center',
      width: 120,
      valueType: 'select',
      valueEnum: componentEnumValue,
    },
    {
      title: '告警时间',
      dataIndex: 'ariseTime',
      align: 'center',
      width: 170,
      valueType: 'dateTimeRange',
      search: {
        transform: (value) => ({
          timeBegin: moment(value[0]).format(),
          timeEnd: moment(value[1]).format(),
        }),
      },
      render: (_, { ariseTime }) => ariseTime && moment(ariseTime).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '告警内容',
      dataIndex: 'content',
      search: false,
    },
    {
      title: '当前状态',
      dataIndex: 'status',
      align: 'center',
      width: 100,
      valueType: 'select',
      valueEnum: {
        [ESystemAlarmStatus.UnResolved]: { text: '未处理', status: 'Default' },
        [ESystemAlarmStatus.Resolved]: { text: '已解决', status: 'Success' },
      },
    },
    {
      title: '解决人',
      dataIndex: 'solver',
      width: 200,
      align: 'center',
    },
    {
      title: '解决时间',
      dataIndex: 'solveTime',
      align: 'center',
      width: 170,
      valueType: 'dateTimeRange',
      search: {
        transform: (value) => ({
          solveTimeBegin: moment(value[0]).format(),
          solveTimeEnd: moment(value[1]).format(),
        }),
      },
      render: (_, { solveTime }) => solveTime && moment(solveTime).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '操作',
      dataIndex: 'opreate',
      align: 'center',
      search: false,
      width: 80,
      render: (text, record) => {
        const { status, id } = record;
        if (!status || status === ESystemAlarmStatus.UnResolved) {
          return (
            <Button size="small" type="primary" onClick={() => showSolveModal(record)}>
              解决
            </Button>
          );
        }
        return (
          <Link to={`/system/log-alarm/alarm/detail?id=${id}`}>
            <Button size="small">详情</Button>
          </Link>
        );
      },
    },
  ];

  return (
    <>
      <ProTable<ISystemAlarm>
        rowKey="id"
        bordered
        size="small"
        columns={columns}
        actionRef={actionRef}
        request={async (params = {}) => {
          const { current = 0, pageSize, ...rest } = params;
          const newParams = {
            pageSize,
            page: current && current - 1,
            ...rest,
          };
          const { success, result } = await queryAlarms(newParams);
          return {
            data: result.content,
            page: result.number,
            total: result.totalElements,
            success,
          };
        }}
        search={{
          ...proTableSerchConfig,
        }}
        toolBarRender={false}
        pagination={getTablePaginationDefaultSettings()}
      />
      <AlarmSolveModel callback={() => actionRef?.current?.reload()} />
    </>
  );
};

export default connect()(SystemAlarm);
