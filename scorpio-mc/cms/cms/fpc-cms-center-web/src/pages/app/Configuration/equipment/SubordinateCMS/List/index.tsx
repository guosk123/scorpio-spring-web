import type { TableColumnProps } from 'antd';
import { Badge } from 'antd';
import { message } from 'antd';
import { Popconfirm } from 'antd';
import { Button, Space } from 'antd';
import ReloadOutlined from '@ant-design/icons/lib/icons/ReloadOutlined';
import EnhancedTable from '@/components/EnhancedTable';
import moment from 'moment';
import { connect } from 'dva';
import type { Dispatch } from 'umi';
import { history } from 'umi';
import { Fragment, useCallback, useEffect, useState } from 'react';
import { PlusOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import StatusBox from '../../components/StatusBox';
import DetailButton from '../../components/DetailButton';
import { queryLoginCmsUrl } from '../../service';

interface LowerCMSItem {
  version: string;
  connectStatus: string;
  cpuMetric: number;
  memoryMetric: number;
  lastInteractiveTime: number;
  lastInteractiveLatency: string;
  upTime: number;
  serialNumber: number;
  IP: string;
  lastLoginTime: string;
}

interface ITableState {
  content: LowerCMSItem[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

interface Props {
  queryLowerCMSList: any;
  deleteLowerCMSItem: any;
  queryLowerCMSListLoading: boolean;
}

const LoginToCms = (record: LowerCMSItem) => {
  queryLoginCmsUrl(record.serialNumber).then((res) => {
    const { success, result } = res;
    if (success) {
      window.open(result);
    }
  });
};

function LowerCMSList(props: Props) {
  const { queryLowerCMSList, deleteLowerCMSItem, queryLowerCMSListLoading } = props;
  const [tableState, setTableState] = useState<ITableState | any>();

  const queryList = useCallback(
    (payload) => {
      queryLowerCMSList(payload).then((result: any) => {
        setTableState(result);
      });
    },
    [queryLowerCMSList],
  );

  useEffect(() => {
    queryList({});
  }, [queryList]);

  const deleteLowerCMS = useCallback(
    (record: any) => {
      deleteLowerCMSItem(record.id).then((success: boolean) => {
        if (success) {
          message.info('删除成功');
          const newPage =
            tableState?.content.length === 1 && tableState?.number > 0
              ? tableState?.number - 1
              : tableState?.number;
          queryList({ page: newPage, pageSize: tableState?.size });
        }
      });
    },
    [deleteLowerCMSItem, queryList, tableState],
  );

  const columns: TableColumnProps<LowerCMSItem>[] = [
    {
      title: 'CMS名称',
      dataIndex: 'name',
      key: 'name',
      align: 'center',
      width: 200,
    },
    {
      title: 'CMS版本',
      dataIndex: 'version',
      key: 'version',
      width: 200,
      align: 'center',
      render: (text) => <Badge status={text === '连接正常' ? 'success' : 'error'} text={text} />,
    },
    {
      title: '在线状态',
      dataIndex: 'connectStatusText',
      key: 'connectStatusText',
      width: 200,
      align: 'center',
    },
    {
      title: 'CPU',
      dataIndex: 'cpuMetric',
      key: 'cpuMetric',
      width: 200,
      align: 'center',
      render: (text) => <StatusBox progress={text} />,
    },
    {
      title: '内存',
      dataIndex: 'memoryMetric',
      key: 'memoryMetric',
      width: 200,
      align: 'center',
      render: (text) => <StatusBox progress={text} />,
    },
    // {
    //   title: '硬盘',
    //   dataIndex: 'diskUsed',
    //   key: 'diskUsed',
    //   width: 200,
    //   align: 'center',
    //   render: (text) => {
    //     return (
    //       <Fragment>
    //         {text.split(',').map((item: string) => {
    //           return (
    //             <Fragment key={item}>
    //               {item} <br />
    //             </Fragment>
    //           );
    //         })}
    //       </Fragment>
    //     );
    //   },
    // },
    {
      title: '最近通讯时间',
      dataIndex: 'lastInteractiveTime',
      key: 'lastInteractiveTime',
      width: 200,
      align: 'center',
      render: (text = 0) => (text ? moment(text).format('YYYY-MM-DD HH:mm:ss') : ''),
    },
    {
      title: '最近交互时延(ms)',
      dataIndex: 'lastInteractiveLatency',
      key: 'lastInteractiveLatency',
      width: 200,
      align: 'center',
    },
    {
      title: '系统运行时间',
      dataIndex: 'upTime',
      key: 'upTime',
      width: 200,
      align: 'center',
      render: (text = 0) =>
        text ? `${(text / 1000 / 60 / 60).toFixed(0)}:${moment(text).format('mm:ss')}` : '-',
    },
    {
      title: '设备序列号',
      dataIndex: 'serialNumber',
      key: 'serialNumber',
      width: 200,
      align: 'center',
    },
    {
      title: 'IP',
      dataIndex: 'ip',
      key: 'ip',
      width: 200,
      align: 'center',
    },
    {
      title: '登录时间',
      dataIndex: 'lastLoginTime',
      key: 'lastLoginTime',
      width: 200,
      align: 'center',
      render: (text = 0) => (text ? moment(text).format('YYYY-MM-DD HH:mm:ss') : ''),
    },
    {
      title: '操作',
      width: 160,
      align: 'center',
      dataIndex: 'action',
      fixed: 'right',
      render: (text, record) => {
        return (
          <Fragment>
            {record?.connectStatus === '1' ? (
              <Popconfirm
                title="确定删除吗？"
                onConfirm={() => {
                  deleteLowerCMS(record);
                }}
                icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
              >
                <Button type="link" size="small">
                  删除
                </Button>
              </Popconfirm>
            ) : (
              <Button
                onClick={() => {
                  LoginToCms(record);
                }}
                type="link"
                size="small"
              >
                登录
              </Button>
            )}
            <DetailButton detail={record} col={columns} />
          </Fragment>
        );
      },
    },
  ];

  const onPaginationChange = (page: number, pageSize: number | undefined) => {
    queryLowerCMSList({ page: page - 1, pageSize: pageSize || 10 });
  };

  return (
    <EnhancedTable<LowerCMSItem>
      tableKey="subordinateCMSList"
      columns={columns}
      rowKey={'id'}
      loading={queryLowerCMSListLoading}
      dataSource={tableState || []}
      size="small"
      bordered
      scroll={{ x: 1500 }}
      extraTool={
        <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
          <Space>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              style={{ display: 'none' }}
              onClick={() => {
                history.push('/configuration/equipment/lower-cms/create');
              }}
            >
              新建
            </Button>
            <Button
              type="primary"
              icon={<ReloadOutlined />}
              onClick={() => {
                queryList({});
              }}
            >
              刷新
            </Button>
          </Space>
        </div>
      }
      pagination={{
        onChange: onPaginationChange,
        total: tableState?.totalElements,
      }}
    />
  );
}
export default connect(
  ({ loading: { effects } }: any) => {
    return {
      queryLowerCMSListLoading: effects['ConfigurationModel/queryLowerCMSList'],
    };
  },
  (dispatch: Dispatch) => {
    return {
      queryLowerCMSList: (payload: any) => {
        return dispatch({
          type: 'ConfigurationModel/queryLowerCMSList',
          payload,
        });
      },
      deleteLowerCMSItem: (delItem: any) => {
        return dispatch({
          type: 'ConfigurationModel/deleteLowerCMS',
          payload: { id: delItem },
        });
      },
    };
  },
)(LowerCMSList);
