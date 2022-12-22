import EnhancedTable from '@/components/EnhancedTable';
import { useContext, useEffect, useState } from 'react';
// import { history } from 'umi';
import { queryPcapTaskLogList } from '../../service';
import type { ColumnProps } from 'antd/lib/table';
import moment from 'moment';
import { OfflineTabsContext } from '../../OfflineTaskTab';
import storage from '@/utils/frame/storage';
import { DEFAULT_PAGE_SIZE_KEY, PAGE_DEFAULT_SIZE } from '@/common/app';

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

export default function TaskLogList() {
  const [queryLoading, setQueryLoading] = useState(false);
  const [state, offlineDispatch] = useContext<any>(OfflineTabsContext);
  const [taskLogListDetail, setTaskLogListDetail] = useState({
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
    queryPcapTaskLogList(param).then((res) => {
      const { success, result } = res;
      setQueryLoading(false);
      if (success) {
        setTaskLogListDetail({
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
    const { number, pageSize } = taskLogListDetail;
    queryList({ page: number, pageSize });
  };
  const [logTaskId, setLogTaskId] = useState();
  console.log('log state', state);
  useEffect(() => {
    const taskDetail = state?.panes?.find((item: any) => item.key === state?.activeKey).paneDetail;
    console.log('taskDetail', taskDetail);
    setLogTaskId(taskDetail?.record.id);
    queryList({
      taskId: taskDetail?.record.id,
      page: 0,
      pageSize: parseInt(storage.get(DEFAULT_PAGE_SIZE_KEY) || '20', 10) || PAGE_DEFAULT_SIZE,
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const columns: ColumnProps<IOfflinePcapTask>[] = [
    {
      title: '日志时间',
      dataIndex: 'ariseTime',
      key: 'ariseTime',
      width: 200,
      align: 'center',
      render: (text = 0) => (text ? moment(text).format('YYYY-MM-DD HH:mm:ss') : ''),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      align: 'center',
      width: 150,
    },
    {
      title: '日志内容',
      dataIndex: 'content',
      key: 'content',
      align: 'center',
      width: 150,
    },
  ];

  const handleSearch = (name: string) => {
    setFileName(name);
    queryList({ name, page: 0, pageSize: taskLogListDetail.pageSize });
  };

  const onPaginationChange = (page: number, pageSize: number | undefined) => {
    queryList({ taskId: logTaskId, name: fileName, page: page - 1, pageSize: pageSize || 10 });
    storage.put(DEFAULT_PAGE_SIZE_KEY, pageSize);
  };

  return (
    <EnhancedTable<IOfflinePcapTask>
      tableKey="task-log-list"
      columns={columns}
      rowKey={'id'}
      loading={queryLoading}
      dataSource={taskLogListDetail.list}
      size="small"
      bordered
      pagination={{
        onChange: onPaginationChange,
        total: taskLogListDetail.totalElements,
      }}
    />
  );
}
