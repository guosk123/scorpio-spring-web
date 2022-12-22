import {
  DEFAULT_PAGE_SIZE_KEY,
  getTablePaginationDefaultSettings,
  proTableSerchConfig,
} from '@/common/app';
import { ONE_KILO_1024 } from '@/common/dict';
import type { ConnectState } from '@/models/connect';
import storage from '@/utils/frame/storage';
import { bytesToSize, formatDuration, getLinkUrl } from '@/utils/utils';
import {
  AreaChartOutlined,
  CopyOutlined,
  DeleteOutlined,
  DownloadOutlined,
  FormOutlined,
  InfoCircleOutlined,
  Loading3QuartersOutlined,
  LoginOutlined,
  PlusOutlined,
  QuestionCircleOutlined,
  RightCircleOutlined,
  StopOutlined,
} from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Button, Drawer, message, Popconfirm, Space, Tooltip, TreeSelect } from 'antd';
import { connect } from 'dva';
import moment from 'moment';
import React, { useEffect, useMemo, useRef, useState } from 'react';
import type { Dispatch, IMonitorMetricMap, INetworkTreeData } from 'umi';
import { history, Link } from 'umi';
import type { ILogicalSubnetMap } from '../../configuration/LogicalSubnet/typings';
import type { INetworkMap } from '../../configuration/Network/typings';
import { ALL_NETWORK_KEY } from './components/TransmitTaskForm';
import TransmitTaskProfile, { computedTaskStateText } from './components/TransmitTaskProfile';
import { batchDelTransmitTasks, queryTransmitTasks } from './service';
import type { ITransmitTask } from './typings';
import {
  ETransmitMode,
  ETransmitTaskState,
  FILTER_CONDITION_TYPE_MAP,
  TASK_MODE_MAP,
  TASK_SOUCE_TYPE_MAP,
} from './typings';

interface ITransmitTaskProps {
  dispatch: Dispatch;

  transmitTaskFileLimitBytes: number;

  metricsMap: IMonitorMetricMap;

  allNetworkMap: INetworkMap;
  networkTree: INetworkTreeData[];
  allLogicalSubnetMap: ILogicalSubnetMap;

  getMetricsLoading: boolean;
  queryTransmitTasksLoading: boolean;
}

const TransmitTask: React.FC<ITransmitTaskProps> = ({
  dispatch,
  transmitTaskFileLimitBytes,
  metricsMap,
  allNetworkMap,
  networkTree,
  allLogicalSubnetMap,
  getMetricsLoading,
  queryTransmitTasksLoading,
}) => {
  const actionRef = useRef<ActionType>();
  // 详情弹出框
  const [drawerVisible, setDrawerVisible] = useState<boolean>(false);

  const [taskList, setTaskList] = useState<ITransmitTask[]>([]);
  // 当前查看详情的任务ID
  const [currentTaskId, setCurrentTaskId] = useState<string>('');

  // 批量删除id
  const [batchDeleteIds, setBatchDeleteIds] = useState<string[]>([]);

  useEffect(() => {
    // 查询存储空间设置
    dispatch({
      type: 'storageSpaceModel/queryStorageSpaceSettings',
    });
    // 查询系统监控
    dispatch({ type: 'moitorModel/queryMetrics' });
  }, [dispatch]);

  /** 删除任务 */
  const handleDelete = ({ id }: ITransmitTask) => {
    dispatch({
      type: 'transmitTaskModel/deleteTransmitTask',
      payload: { id },
    }).then(() => {
      actionRef.current?.reload();
    });
  };

  /** 批量删除任务 */
  const handleBatchDelete = async () => {
    const { success } = await batchDelTransmitTasks({
      delete: batchDeleteIds.join(','),
    });
    if (success) {
      message.success('删除成功');
      setBatchDeleteIds([]);
      actionRef.current?.reload();
    }
  };

  /** 下载 */
  const handleDownload = ({ id }: ITransmitTask) => {
    if (!id) {
      message.error('缺失id');
      return false;
    }
    return (
      dispatch({
        type: 'transmitTaskModel/downloadTransmitTaskFile',
        payload: { id },
      }) as unknown as Promise<any>
    ).then((response) => {
      const { success, result } = response;
      if (success) {
        const { 'result-code': resultCode } = result;

        if (resultCode === 'TASK_EXECUTING') {
          message.warn('任务正在执行中，请稍候再下载');
          actionRef.current?.reload();
          return;
        }

        // 文件被老化，需要重新执行查询任务
        if (resultCode === 'TASK_REDO_EXECUTING') {
          message.warn('文件被老化，需要重新执行查询任务');
          actionRef.current?.reload();
          return;
        }

        // 文件在硬盘中时，开始进行下载
        if (resultCode === 'GO_DOWNLOAD') {
          const { 'download-path': downloadPath } = result;
          window.open(downloadPath);
        }
      }
    });
  };

  // 任务停止
  const handleStop = ({ id }: ITransmitTask) => {
    if (!id) {
      message.error('缺失任务id');
      return false;
    }
    return (
      dispatch({
        type: 'transmitTaskModel/stopTransmitTask',
        payload: { id },
      }) as unknown as Promise<any>
    ).then(() => {
      actionRef.current?.reload();
    });
  };

  // 重新开始任务
  const handleRestart = ({ id }: ITransmitTask) => {
    if (!id) {
      message.error('缺失任务id');
      return false;
    }
    return (
      dispatch({
        type: 'transmitTaskModel/restartTransmitTask',
        payload: { id },
      }) as unknown as Promise<any>
    ).then(() => {
      actionRef.current?.reload();
    });
  };

  // 任务详情抽屉
  const toggleDrawer = (task?: ITransmitTask) => {
    setDrawerVisible((prevVisible) => !prevVisible);
    if (task?.id) {
      setCurrentTaskId(task.id);
    }
  };

  const handleCloseDrawerAfter = (visible: boolean) => {
    if (!visible) {
      setCurrentTaskId('');
    }
  };

  const tableColumns: ProColumns<ITransmitTask>[] = [
    {
      title: '名称',
      dataIndex: 'name',
      align: 'center',
    },
    {
      title: '时间范围',
      align: 'center',
      dataIndex: 'timeRange',
      width: 180,
      search: false,
      renderText: (text, record) => {
        const { filterStartTime, filterEndTime } = record;
        return (
          <div>
            {moment(filterStartTime).format('YYYY-MM-DD HH:mm:ss')}
            <br />
            {moment(filterEndTime).format('YYYY-MM-DD HH:mm:ss')}
          </div>
        );
      },
    },
    {
      title: '条件类型',
      dataIndex: 'filterConditionTypeText',
      align: 'center',
      valueEnum: FILTER_CONDITION_TYPE_MAP,
      search: {
        transform: (value: any) => ({
          filterConditionType: value,
        }),
      },
    },
    {
      title: '过滤网络/离线任务',
      dataIndex: 'filterNetworkId',
      align: 'center',
      search: {
        title: '过滤网络',
      } as any,
      renderFormItem: () => {
        return (
          <TreeSelect
            allowClear
            style={{ width: '100%' }}
            dropdownStyle={{ maxHeight: 400, overflow: 'auto' }}
            treeData={[{ title: '全部网络', value: ALL_NETWORK_KEY }, ...networkTree]}
            placeholder="请选择网络"
            treeDefaultExpandAll
            showSearch
            filterTreeNode={(inputValue, treeNode) => {
              if (!inputValue) {
                return true;
              }
              return (treeNode!.title as string).indexOf(inputValue) > -1;
            }}
          />
        );
      },
      renderText: (_, { filterPacketFileName, filterNetworkId }) => {
        if (filterPacketFileName !== null) {
          return filterPacketFileName;
        }
        if (!filterNetworkId) {
          return;
        }
        if (filterNetworkId === ALL_NETWORK_KEY) {
          return '全部网络';
        }
        const info = { ...allNetworkMap, ...allLogicalSubnetMap }[filterNetworkId];
        return info?.name || `[已删除: ${filterNetworkId}]`;
      },
    },
    {
      title: '导出模式',
      dataIndex: 'modeText',
      align: 'center',
      valueEnum: TASK_MODE_MAP,
      search: {
        transform: (value: any) => ({
          mode: value,
        }),
      },
    },
    {
      title: '任务状态',
      dataIndex: 'state',
      align: 'center',
      renderText: (state, record) => {
        return computedTaskStateText(record);
      },
      valueEnum: {
        [ETransmitTaskState.START]: '进行中',
        [ETransmitTaskState.FINISHED]: '已完成',
        [ETransmitTaskState.STOPPED]: '已停止',
      },
    },
    {
      title: '执行进度',
      dataIndex: 'executionProgress',
      align: 'center',
      search: false,
      renderText: (text) => `${text}%`,
    },
    {
      title: '执行时间',
      dataIndex: 'executionStartTime',
      align: 'center',
      width: 180,
      search: false,
      renderText: (_, record) => {
        const { executionStartTime, executionEndTime } = record;
        if (!executionStartTime) {
          return '';
        }
        let text = moment(executionStartTime).format('YYYY-MM-DD HH:mm:ss');
        if (executionEndTime) {
          text += `\n${moment(executionEndTime).format('YYYY-MM-DD HH:mm:ss')}`;
        }
        return text;
      },
    },
    {
      title: '持续时间',
      dataIndex: 'executionTime',
      align: 'center',
      search: false,
      renderText: (text, record) => {
        const { executionStartTime, executionEndTime } = record;
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
    { title: '任务来源', dataIndex: 'source', search: undefined, align: 'center' },
    {
      title: '任务类型',
      dataIndex: 'sourceTypeText',
      hideInTable: true,
      align: 'center',
      valueEnum: TASK_SOUCE_TYPE_MAP,
      search: {
        transform: (value: any) => ({
          sourceType: value,
        }),
      },
    },

    {
      title: '操作',
      dataIndex: 'action',
      align: 'center',
      valueType: 'option',
      width: 120,
      render: (text, record) => {
        const { id, mode, executionStartTime, state } = record;

        // 进行中：显示3个按钮，详情 | 删除 | 停止
        const isGoing = state === ETransmitTaskState.START;
        // 正在停止：任务状态为1，但是有任务开始时间，显示2个按钮，详情 | 删除
        const isStopping = state === ETransmitTaskState.STOPPED && executionStartTime;
        // 已停止：任务状态为1，任务状态也被清空了，显示三个按钮，编辑 | 删除 | 重新执行
        const isStopped = state === ETransmitTaskState.STOPPED && !executionStartTime;
        // 已完成，显示3个按钮，编辑 | 删除 | 下载/重放
        const isFinished = state === ETransmitTaskState.FINISHED;

        return (
          <>
            <Space>
              {/* 任务进行中，停止任务 */}
              {isGoing && (
                <>
                  <Popconfirm
                    title="停止任务会清除现有进度，确定停止吗？"
                    onConfirm={() => handleStop(record)}
                    icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
                  >
                    <Tooltip title="停止">
                      <a href="#">
                        <StopOutlined />
                      </a>
                    </Tooltip>
                  </Popconfirm>
                </>
              )}
              {/* 任务已停止，可以重新开始任务 */}
              {isStopped && (
                <Tooltip title="开始">
                  <a onClick={() => handleRestart(record)}>
                    <RightCircleOutlined />
                  </a>
                </Tooltip>
              )}
              {/* 文件存储模式已完成，可以下载 */}
              {(mode === ETransmitMode.PCAP || mode === ETransmitMode.PCAPNG) && isFinished && (
                <>
                  <Tooltip title="下载">
                    <a onClick={() => handleDownload(record)}>
                      <DownloadOutlined />
                    </a>
                  </Tooltip>
                  {/* 执行完成的任务，都可以进行分析 */}
                  <Tooltip title="分析">
                    <Link to={getLinkUrl(`/analysis/trace/transmit-task/${id}/analysis`)}>
                      <AreaChartOutlined />
                    </Link>
                  </Tooltip>
                </>
              )}
              {/* 流量重放模式已完成，可以再次重放 */}
              {mode === ETransmitMode.REPLAY && isFinished && (
                <Tooltip title="重放">
                  <a onClick={() => handleRestart(record)}>
                    <LoginOutlined />
                  </a>
                </Tooltip>
              )}
              {!isGoing && !isStopping && (
                <Tooltip title="编辑">
                  <Link to={getLinkUrl(`/analysis/trace/transmit-task/${id}/update`)}>
                    <FormOutlined />
                  </Link>
                </Tooltip>
              )}
            </Space>
            <br />
            <Space>
              <Tooltip title="复制">
                <Link to={getLinkUrl(`/analysis/trace/transmit-task/${id}/copy`)}>
                  <CopyOutlined />
                </Link>
              </Tooltip>
              {/* 删除 */}
              <Popconfirm
                title="确定删除吗？"
                onConfirm={() => handleDelete(record)}
                icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
              >
                <Tooltip title="删除">
                  <a href="#">
                    <DeleteOutlined />
                  </a>
                </Tooltip>
              </Popconfirm>
              <Tooltip title="详情">
                <a onClick={() => toggleDrawer(record)}>
                  <InfoCircleOutlined />
                </a>
              </Tooltip>
            </Space>
          </>
        );
      },
    },
  ];

  // 查询缓存大小
  const fsCacheTotalByte = useMemo(() => {
    if (getMetricsLoading) {
      return <Loading3QuartersOutlined style={{ margin: '0 4px' }} spin />;
    }
    const value = metricsMap.fs_cache_total_byte?.metricValue || 0;
    return bytesToSize(+value, 3, ONE_KILO_1024);
  }, [getMetricsLoading, metricsMap]);

  // 任务详情
  const taskDetail = useMemo(() => {
    return (
      currentTaskId ? taskList.find((task) => task.id === currentTaskId) || {} : {}
    ) as ITransmitTask;
  }, [currentTaskId, taskList]);

  if (drawerVisible) {
    if (Object.keys(taskDetail).length === 0) {
      toggleDrawer();
    }
  }

  return (
    <>
      {/* <div style={{ marginBottom: 10 }}>
        <Alert
          message={
            <div>
              流量查询缓存空间共 {fsCacheTotalByte}
              ，系统会自动清理；每次任务查询最大支持{' '}
              {bytesToSize(transmitTaskFileLimitBytes, 3, ONE_KILO_1024)}{' '}
              PCAP文件导出；相同转发接口下，同一时刻只能执行一个重放任务。
            </div>
          }
          type="info"
          showIcon
        />
      </div> */}
      <ProTable<ITransmitTask>
        bordered
        rowKey="id"
        size="small"
        actionRef={actionRef}
        columns={tableColumns}
        polling={3000}
        rowSelection={{
          type: 'checkbox',
          selectedRowKeys: batchDeleteIds,
          onChange: (_, selectedRows: ITransmitTask[]) => {
            setBatchDeleteIds(selectedRows.map((row) => row.id));
          },
        }}
        tableAlertOptionRender={() => {
          return (
            <Space size={16}>
              <Popconfirm
                title="确定删除吗？"
                onConfirm={() => handleBatchDelete()}
                disabled={batchDeleteIds.length === 0}
                icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
              >
                <a style={{ color: 'red' }}>批量删除</a>
              </Popconfirm>

              <a
                onClick={() => {
                  setBatchDeleteIds([]);
                }}
              >
                取消选择
              </a>
            </Space>
          );
        }}
        request={async (params = {}) => {
          const { current, pageSize, ...rest } = params;
          const newParams = {
            pageSize,
            page: current! - 1,
            ...rest,
          } as any;
          const { success, result } = await queryTransmitTasks(newParams);

          setTaskList(success ? result.content : []);

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
          ...proTableSerchConfig,
          labelWidth: 100,
          span: 8,
          optionRender: (searchConfig, formProps, dom) => [
            ...dom.reverse(),
            <Button
              icon={<PlusOutlined />}
              type="primary"
              key="create"
              onClick={() => history.push(getLinkUrl('/analysis/trace/transmit-task/create'))}
            >
              新建
            </Button>,
          ],
        }}
        toolBarRender={false}
        pagination={{
          ...getTablePaginationDefaultSettings(),
          onChange: (page, pageSize) => {
            storage.put(DEFAULT_PAGE_SIZE_KEY, pageSize);
            setBatchDeleteIds([]);
          },
        }}
      />
      <Drawer
        width={900}
        destroyOnClose
        title="任务详情"
        visible={drawerVisible}
        onClose={() => toggleDrawer()}
        afterVisibleChange={handleCloseDrawerAfter}
      >
        <TransmitTaskProfile loading={queryTransmitTasksLoading} detail={taskDetail} />
      </Drawer>
    </>
  );
};

export default connect(
  ({
    loading: { effects },
    moitorModel: { metricsMap },
    storageSpaceModel: { transmitTaskFileLimitBytes },
    networkModel: { allNetworkMap, networkTree },
    logicSubnetModel: { allLogicalSubnetMap },
  }: ConnectState) => ({
    metricsMap,
    transmitTaskFileLimitBytes,
    allNetworkMap,
    allLogicalSubnetMap,
    networkTree,
    getMetricsLoading: effects['moitorModel/queryMetrics'] || false,
    queryTransmitTasksLoading: effects['transmitTaskModel/queryTransmitTasks'] || false,
  }),
)(TransmitTask);
