import { getTablePaginationDefaultSettings, proTableSerchConfig } from '@/common/app';
import { SYSTEM_ALARM_LEVEL, SYSTEM_COMPONENT } from '@/common/dict';
import { queryAlarms } from '@/services/frame/alarm';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Button, TreeSelect } from 'antd';
import { connect } from 'dva';
import moment from 'moment';
import { useEffect, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import { history } from 'umi';
import { useAccess, useLocation } from 'umi';
import { Link } from 'umi';
import { queryDeviceList } from '../../SSO/service';
import { serialListToTree } from '../../SSO/SingleSignSetting';
import type { optionsType } from '../Log/typings';
import AlarmSolveModel from './components/SolveModel';
import type { ISystemAlarm } from './typings';
import { ESystemAlarmStatus } from './typings';

export interface ICmsSensorTree {
  deviceName?: string;
  deviceSerialNumber?: string;
  child?: ICmsSensorTree[] | null;
}

export const getSensorNameOnSensorTree = (tree: ICmsSensorTree, nodeId: string, res: string[]) => {
  if (tree?.deviceSerialNumber === nodeId) {
    res.push(tree.deviceName || '');
  }
  if (!tree?.child) {
    return null;
  }
  tree?.child.forEach((item) => {
    getSensorNameOnSensorTree(item, nodeId, res);
  });
  return null;
};

interface IProps {
  dispatch: Dispatch;
}
const SystemAlarm = ({ dispatch }: IProps) => {
  const access = useAccess();
  const location = useLocation() as any as {
    query: { startTime: string; endTime: string; components: string };
  };
  const levelEnumValue = SYSTEM_ALARM_LEVEL.reduce((prev, current) => {
    return {
      ...prev,
      [current.key]: { text: current?.label, status: current?.status_color },
    };
  }, {});
  // let canBeSelectedCom = SYSTEM_COMPONENT;
  // if (access.hasUserPerm) {
  //   canBeSelectedCom = canBeSelectedCom.filter((item: optionsType) => item.key === '001001');
  // }
  const componentEnumValue = SYSTEM_COMPONENT.reduce((prev, current) => {
    return {
      ...prev,
      [current.key]: { text: current?.label },
    };
  }, {});

  const actionRef = useRef<ActionType>();
  const [initComVal] = useState(() => {
    // if (access.hasUserPerm) {
    //   return '001001';
    // } else {
    //   return undefined;
    // }
    if (location.query.components) {
      const tmpCom = location.query.components;
      const queryObj = history.location.query || {};
      delete queryObj.components;
      history.replace({ query: queryObj });
      return tmpCom;
    } else {
      return undefined;
    }
  });
  const [initTime] = useState(() => {
    if (location.query.startTime && location.query.endTime) {
      const tmpRes = [
        moment(+location.query.startTime).format('YYYY-MM-DD HH:mm:ss'),
        moment(+location.query.endTime).format('YYYY-MM-DD HH:mm:ss'),
      ];
      const queryObj = history.location.query || {};
      delete queryObj.startTime;
      delete queryObj.endTime;
      history.replace({ query: queryObj });
      return tmpRes;
    } else {
      return [];
    }
  });

  const showSolveModal = (record: ISystemAlarm) => {
    dispatch({
      type: 'alarmModel/showModal',
      payload: {
        currentItem: record,
      },
    });
  };

  const [cmsSensorTree, setCmsSensorTree] = useState<any>();
  useEffect(() => {
    queryDeviceList().then((res) => {
      const { success, result } = res;
      if (success) {
        setCmsSensorTree(result);
      }
    });
  }, []);

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
      title: '设备',
      dataIndex: 'nodeId',
      align: 'center',
      width: 180,
      ellipsis: true,
      valueType: 'select',
      renderFormItem: () => {
        const tmpTree = serialListToTree(cmsSensorTree);
        tmpTree.value = '';
        return <TreeSelect treeData={[tmpTree]} allowClear treeDefaultExpandAll showSearch />;
      },
      render: (text, record) => {
        const res: any = [];
        getSensorNameOnSensorTree(cmsSensorTree || {}, record.nodeId, res);
        return record.nodeId === '' ? '本机' : res[0];
      },
    },
    {
      title: '告警时间',
      dataIndex: 'ariseTime',
      align: 'center',
      width: 170,
      valueType: 'dateTimeRange',
      initialValue: initTime,
      search: {
        transform: (value) => {
          if (value.length === 0) {
            return {};
          }
          return {
            timeBegin: moment(value[0]).format(),
            timeEnd: moment(value[1]).format(),
          };
        },
      },
      render: (_, { ariseTime }) => ariseTime && moment(ariseTime).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '告警内容',
      dataIndex: 'content',
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
        let solveUrl = `/system/log-alarm/alarm/detail?id=${id}`;
        if (access.hasUserPerm) {
          solveUrl = `/logAlarm/alarm/detail?id=${id}`;
        }
        return (
          <Link to={solveUrl}>
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
        onReset={actionRef.current?.reset}
        request={async (params = {}) => {
          const { current = 0, pageSize, ...rest } = params;
          let linkQueryObj = {};
          if (initComVal) {
            linkQueryObj = { component: initComVal };
          }
          const newParams = {
            pageSize,
            page: current && current - 1,
            ...linkQueryObj,
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
