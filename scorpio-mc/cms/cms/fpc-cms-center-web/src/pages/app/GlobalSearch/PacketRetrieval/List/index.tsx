import {
  DEFAULT_PAGE_SIZE_KEY,
  getTablePaginationDefaultSettings,
  proTableSerchConfig,
  PRO_TABLE_RESET_SPAN_SIZE,
} from '@/common/app';
import Ellipsis from '@/components/Ellipsis';
import { querySensorList } from '@/pages/app/Configuration/equipment/service';
import storage from '@/utils/frame/storage';
import { getLinkUrl } from '@/utils/utils';
import { PlusOutlined, QuestionCircleOutlined, ReloadOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Button, message, Popconfirm, Space } from 'antd';
import { connect } from 'dva';
import moment from 'moment';
import React, { useEffect, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import { history, Link } from 'umi';
import SensorTaskList from '../components/SensorTaskList';
import { deleteTransmitTask, batchDelTransmitTasks, queryTransmitTasks } from '../service';
import type { ITransmitTask } from '../typings';
import { FILTER_CONDITION_TYPE_MAP, TASK_MODE_MAP, TASK_SOUCE_TYPE_MAP } from '../typings';

interface Props {
  dispatch: Dispatch;
}

const List: React.FC<Props> = () => {
  const actionRef = useRef<ActionType>();
  const [tSpan, setTSpan] = useState(window.innerWidth > PRO_TABLE_RESET_SPAN_SIZE ? 8 : 12);

  const [sensorList, setSensorList] = useState<any>([]);

  useEffect(() => {
    querySensorList().then((res) => {
      const { success, result } = res;
      if (success) {
        setSensorList(result);
      }
    });
  }, []);

  useEffect(() => {
    window.addEventListener('resize', () => {
      setTSpan(window.innerWidth > PRO_TABLE_RESET_SPAN_SIZE ? 8 : 12);
    });

    return () => {
      window.removeEventListener('resize', () => {});
    };
  }, []);

  // 批量删除id
  const [batchDeleteIds, setBatchDeleteIds] = useState<string[]>([]);

  /** 删除任务 */
  const handleDelete = ({ id }: ITransmitTask) => {
    deleteTransmitTask({ id }).then((res) => {
      const { success } = res;
      if (success) {
        message.info('删除成功');
      } else {
        message.error('删除失败');
      }
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
    actionRef.current?.reload();
  };

  const tableColumns: ProColumns<ITransmitTask>[] = [
    {
      title: '',
      dataIndex: '#',
      align: 'center',
      width: 40,
      search: false,
      render: (text, record) => <SensorTaskList taskDetail={record} />,
    },
    {
      title: '任务名称',
      dataIndex: 'name',
      align: 'center',
      width: 180,
    },
    {
      title: '过滤时间范围',
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
      title: '过滤条件类型',
      dataIndex: 'filterConditionType',
      align: 'center',
      width: 180,
      valueEnum: FILTER_CONDITION_TYPE_MAP,
      search: {
        transform: (value: any) => ({
          filterConditionType: value,
        }),
      },
    },
    {
      title: '任务开始时间',
      dataIndex: 'executionStartTime',
      align: 'center',
      width: 180,
      search: false,
      renderText: (text) => {
        return !text.length ? '-' : moment(text).format('YYYY-MM-DD HH:mm:ss');
      },
    },
    {
      title: '导出模式',
      dataIndex: 'mode',
      align: 'center',
      width: 180,
      valueEnum: TASK_MODE_MAP,
      search: {
        transform: (value: any) => ({
          mode: value,
        }),
      },
    },
    {
      title: '任务执行时间',
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
      title: '查询探针',
      dataIndex: 'fpcSerialNumber',
      search: false,
      align: 'center',
      width: 180,
      render: (text, record) => {
        const fpcSerialNumbers = record.fpcSerialNumber.split(',');
        const res = sensorList
          .filter((item: any) => fpcSerialNumbers.includes(item.serialNumber))
          .map((sub: any) => sub.name)
          .join(',');
        return (
          <Ellipsis tooltip lines={1}>
            {res}
          </Ellipsis>
        );
      },
    },
    {
      title: '任务来源',
      width: 180,
      dataIndex: 'source',
      search: true,
      align: 'center',
    },
    {
      title: '任务来源方式',
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
      width: 160,
      fixed: 'right',
      render: (text, record) => {
        return (
          <Space>
            <Link to={getLinkUrl(`/flow-trace/packet-retrieval/${record.id}/copy`)}>复制</Link>
            {/* 删除 */}
            <Popconfirm
              title="确定删除吗？"
              onConfirm={() => handleDelete(record)}
              icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
            >
              <a href="#">删除</a>
            </Popconfirm>
          </Space>
        );
      },
    },
  ];

  return (
    <ProTable<ITransmitTask>
      bordered
      rowKey="id"
      size="small"
      scroll={{ x: 1200 }}
      actionRef={actionRef}
      columns={tableColumns}
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
        span: tSpan,
        optionRender: (searchConfig, formProps, dom) => [
          ...dom.reverse(),
          <Button
            icon={<ReloadOutlined />}
            type="primary"
            onClick={() => actionRef.current?.reload()}
          >
            刷新
          </Button>,
          <Button
            icon={<PlusOutlined />}
            type="primary"
            onClick={() => history.push(getLinkUrl('/flow-trace/packet-retrieval/create-task'))}
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
  );
};

export default connect()(List);
