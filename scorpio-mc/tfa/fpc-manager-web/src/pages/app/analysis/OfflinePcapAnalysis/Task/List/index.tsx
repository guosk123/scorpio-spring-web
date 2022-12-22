import EnhancedTable from '@/components/EnhancedTable';
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { Button, Input, message, Space } from 'antd';
import { useContext, useEffect, useState } from 'react';
// import { history } from 'umi';
import { DEFAULT_PAGE_SIZE_KEY, PAGE_DEFAULT_SIZE } from '@/common/app';
import storage from '@/utils/frame/storage';
import { QuestionCircleOutlined } from '@ant-design/icons';
import { Popconfirm } from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import moment from 'moment';
import { OfflineTabsContext } from '../../OfflineTaskTab';
import { jumpToOfflineTab } from '../../OfflineTaskTab/constant';
import { deletePcapTask, queryPcapTaskList } from '../../service';
import { EOfflineTabType } from '../../typing';

export interface IOfflinePcapTask {
  id: string;
  name: string;
  mode: string;
  filePath: string;
  analyzedFile: string;
  status: string;
  deleted: string;
  createTime: string;
  operator: string;
  creationMethod: string;
}
const { Search } = Input;

export default function TaskList() {
  const [queryLoading, setQueryLoading] = useState(false);
  const [state, offlineDispatch] = useContext(OfflineTabsContext);
  const [taskListDetail, setTaskListDetail] = useState({
    list: [],
    totalPages: 0,
    totalElements: 0,
    number: 0,
    pageSize: parseInt(storage.get(DEFAULT_PAGE_SIZE_KEY) || '20', 10) || PAGE_DEFAULT_SIZE,
  });
  const [fileName, setFileName] = useState('');

  const queryList = (param: {
    name?: string;
    page: number;
    pageSize: number;
    taskId?: string;
    source?: string;
  }) => {
    setQueryLoading(true);
    queryPcapTaskList(param).then((res) => {
      const { success, result } = res;
      setQueryLoading(false);
      if (success) {
        setTaskListDetail({
          list: result.content,
          totalElements: result.totalElements,
          totalPages: result.totalPages,
          number: result.number,
          pageSize: result.size,
        });
      }
      setQueryLoading(false);
    });
  };

  const handleRefresh = () => {
    const { number, pageSize } = taskListDetail;
    queryList({ page: number, pageSize });
  };

  useEffect(() => {
    queryList({
      page: 0,
      pageSize: parseInt(storage.get(DEFAULT_PAGE_SIZE_KEY) || '20', 10) || PAGE_DEFAULT_SIZE,
    });
  }, []);

  const columns: ColumnProps<IOfflinePcapTask>[] = [
    {
      title: '任务名称',
      dataIndex: 'name',
      key: 'name',
      align: 'center',
      width: 200,
      fixed: 'left',
      ellipsis: true,
      render: (text, record) => {
        return (
          <Button
            onClick={() => {
              jumpToOfflineTab(
                state,
                offlineDispatch,
                EOfflineTabType.OFFLINE_TASK_DETAIL,
                { record },
                record.name,
              );
            }}
            type="link"
            size="small"
          >
            {text}
          </Button>
        );
      },
    },
    {
      title: '模式',
      dataIndex: 'modeText',
      key: 'modeText',
      width: 200,
      align: 'center',
    },
    {
      title: '任务创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 200,
      align: 'center',
      render: (text = 0) => (text ? moment(text).format('YYYY-MM-DD HH:mm:ss') : ''),
    },
    // {
    //   title: '状态',
    //   dataIndex: 'status',
    //   key: 'status',
    //   align: 'center',
    //   width: 150,
    // },
    {
      title: '操作',
      dataIndex: 'operation',
      key: 'operation',
      fixed: 'right',
      width: 100,
      align: 'center',
      render: (_, record) => {
        return (
          <div>
            <Popconfirm
              title="确定删除吗？"
              onConfirm={() => {
                deletePcapTask({ id: record.id }).then((res) => {
                  const { success } = res;
                  if (success) {
                    message.info('删除成功');
                    handleRefresh();
                  }
                });
              }}
              icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
            >
              <Button type="link" size="small">
                删除
              </Button>
            </Popconfirm>
            <Button
              type="link"
              size="small"
              onClick={() => {
                jumpToOfflineTab(
                  state,
                  offlineDispatch,
                  EOfflineTabType.OFFLINE_TASK_LOG,
                  { record },
                  record.name,
                  'log',
                );
              }}
            >
              日志
            </Button>
          </div>
        );
      },
    },
  ];

  const handleSearch = (name: string) => {
    setFileName(name);
    queryList({ name, page: 0, pageSize: taskListDetail.pageSize });
  };

  const onPaginationChange = (page: number, pageSize: number | undefined) => {
    queryList({ name: fileName, page: page - 1, pageSize: pageSize || 10 });
    storage.put(DEFAULT_PAGE_SIZE_KEY, pageSize);
  };

  return (
    <EnhancedTable<IOfflinePcapTask>
      tableKey="offline-task-list"
      columns={columns}
      rowKey={'id'}
      loading={queryLoading}
      dataSource={taskListDetail.list}
      size="small"
      bordered
      scroll={{ x: 1500 }}
      extraTool={
        <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
          <Space>
            <Search
              placeholder="请输入"
              onSearch={handleSearch}
              enterButton
              style={{ width: 300 }}
            />
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => {
                // history.push('/analysis/offline/create');
                jumpToOfflineTab(state, offlineDispatch, EOfflineTabType.OFFLINE_TASK_CREATE);
              }}
            >
              新建离线分析任务
            </Button>
            <Button type="primary" icon={<ReloadOutlined />} onClick={handleRefresh}>
              刷新
            </Button>
          </Space>
        </div>
      }
      pagination={{
        onChange: onPaginationChange,
        total: taskListDetail.totalElements,
      }}
    />
  );
}
