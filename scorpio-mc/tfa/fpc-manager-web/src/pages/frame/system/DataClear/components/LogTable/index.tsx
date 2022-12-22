import { getTablePaginationDefaultSettings, pageSizeOptions } from '@/common/app';
import { SYSTEM_LOG_DATA_CLEAR } from '@/common/dict';
import { queryLogs } from '@/services/frame/log';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Button } from 'antd';
import moment from 'moment';
import type { ForwardRefRenderFunction } from 'react';
import { forwardRef, useImperativeHandle, useRef } from 'react';
import type { ISystemLog } from '../../../Log/typing';

const logColumns: ProColumns<ISystemLog>[] = [
  {
    title: '#',
    dataIndex: 'index',
    align: 'center',
    width: 60,
    ellipsis: true,
    search: false,
    render: (dom, record, index) => {
      return index + 1;
    },
  },
  {
    title: '时间',
    dataIndex: 'ariseTime',
    align: 'center',
    search: false,
    render: (dom, { ariseTime }) => moment(ariseTime).format('YYYY-MM-DD HH:mm:ss'),
  },
  {
    title: '描述',
    dataIndex: 'content',
    ellipsis: true,
    search: false,
  },
];

const LogTable: ForwardRefRenderFunction<{ fresh: () => void }, any> = ({}, ref) => {
  const actionRef = useRef<ActionType>();

  useImperativeHandle(ref, () => {
    return {
      fresh: () => {
        actionRef.current?.reload();
      },
    };
  });

  return (
    <ProTable<ISystemLog>
      rowKey="id"
      bordered
      size="small"
      columns={logColumns}
      title={() => (
        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
          <span>日志</span>
          <Button
            type="primary"
            size="small"
            onClick={() => {
              actionRef.current?.reload();
            }}
          >
            刷新
          </Button>
        </div>
      )}
      actionRef={actionRef}
      request={async (params = {}) => {
        const { current = 0, pageSize, ...rest } = params;
        const newParams = {
          pageSize,
          page: current && current - 1,
          category: String(SYSTEM_LOG_DATA_CLEAR),
          ...rest,
        };
        const { success, result } = await queryLogs(newParams);
        return {
          data: result.content,
          page: result.number,
          total: result.totalElements,
          success,
        };
      }}
      search={false}
      toolBarRender={false}
      pagination={{
        hideOnSinglePage: false,
        showQuickJumper: true,
        showSizeChanger: true,
        pageSizeOptions,
        showTotal: (total) => `共 ${total} 条`,
        ...getTablePaginationDefaultSettings(),
      }}
    />
  );
};

export default forwardRef(LogTable);
