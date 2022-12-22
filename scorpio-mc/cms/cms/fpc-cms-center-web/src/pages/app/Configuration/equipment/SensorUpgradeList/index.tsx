import type { TableColumnProps } from 'antd';
import { message } from 'antd';
import { Button, Popconfirm, Space } from 'antd';
import ReloadOutlined from '@ant-design/icons/lib/icons/ReloadOutlined';
import EnhancedTable from '@/components/EnhancedTable';
import { Fragment, useCallback, useEffect, useState } from 'react';
import { QuestionCircleOutlined } from '@ant-design/icons';
import DetailButton from '../components/DetailButton';
import { connect } from 'react-redux';
import type { Dispatch } from 'umi';
import moment from 'moment';
import UploadButton from '../components/UploadButton';
import { bytesToSize } from '@/utils/utils';
import { ONE_KILO_1024 } from '@/common/dict';

interface SensorUpgradeItem {
  packetType: string;
  packetVersion: string;
  packetName: string;
  fileSize: number;
  uploadTime: number;
  upgradedTime: string;
}

interface ITableState {
  content: SensorUpgradeItem[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

interface Props {
  querySensorUpgradeList: any;
  deleteSensorUpgradeItem: any;
  querySensorUpgradeListLoading: boolean;
}

function SensorUpgradeList(props: Props) {
  const { querySensorUpgradeList, deleteSensorUpgradeItem, querySensorUpgradeListLoading } = props;
  const [tableState, setTableState] = useState<ITableState>();

  const queryList = useCallback(
    (payload) => {
      querySensorUpgradeList(payload).then((result: any) => {
        setTableState(result);
      });
    },
    [querySensorUpgradeList],
  );

  useEffect(() => {
    queryList({});
  }, [queryList]);

  const deleteUpgradePacket = useCallback(
    (record: any) => {
      deleteSensorUpgradeItem(record.id).then((success: boolean) => {
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
    [deleteSensorUpgradeItem, queryList, tableState],
  );

  const columns: TableColumnProps<SensorUpgradeItem>[] = [
    {
      title: '升级包类型',
      dataIndex: 'packetType',
      key: 'packetType',
      align: 'center',
      width: 200,
    },
    {
      title: '版本号',
      dataIndex: 'packetVersion',
      key: 'packetVersion',
      width: 200,
      align: 'center',
    },
    {
      title: '升级包名称',
      dataIndex: 'packetName',
      key: 'packetName',
      width: 200,
      align: 'center',
    },
    {
      title: '文件大小',
      dataIndex: 'fileSize',
      key: 'fileSize',
      width: 200,
      align: 'center',
      render: (text = 0) => bytesToSize(text, 3, ONE_KILO_1024),
    },
    {
      title: '上传时间',
      dataIndex: 'uploadTime',
      key: 'uploadTime',
      width: 200,
      align: 'center',
      render: (text = 0) => (text ? moment(text).format('YYYY-MM-DD HH:mm:ss') : ''),
    },
    {
      title: '升级完成时间',
      dataIndex: 'upgradedTime',
      key: 'upgradedTime',
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
            <Popconfirm
              title="确定删除吗？"
              onConfirm={() => {
                deleteUpgradePacket(record);
              }}
              icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
            >
              <Button type="link" size="small">
                删除
              </Button>
            </Popconfirm>
            <Button type="link" size="small" onClick={() => {}}>
              升级
            </Button>
            <DetailButton detail={record} col={columns} />
          </Fragment>
        );
      },
    },
  ];

  const onPaginationChange = (page: number, pageSize: number | undefined) => {
    querySensorUpgradeList({ page: page - 1, pageSize: pageSize || 10 });
  };

  return (
    <EnhancedTable
      tableKey="sensorUpgradeList"
      columns={columns}
      rowKey={'id'}
      loading={querySensorUpgradeListLoading}
      dataSource={tableState?.content}
      size="small"
      bordered
      scroll={{ x: 1500 }}
      extraTool={
        <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
          <Space>
            <UploadButton />
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
      querySensorUpgradeListLoading: effects['ConfigurationModel/querySensorUpgradeList'],
    };
  },
  (dispatch: Dispatch) => {
    return {
      querySensorUpgradeList: (payload: any) => {
        return dispatch({
          type: 'ConfigurationModel/querySensorUpgradeList',
          payload,
        });
      },
      deleteSensorUpgradeItem: (delItem: any) => {
        return dispatch({
          type: 'ConfigurationModel/deleteSensorUpgradeItem',
          payload: { id: delItem },
        });
      },
    };
  },
)(SensorUpgradeList);
