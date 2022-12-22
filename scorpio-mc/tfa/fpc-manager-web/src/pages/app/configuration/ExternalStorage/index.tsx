import { getLinkUrl } from '@/utils/utils';
import { PlusOutlined, QuestionCircleOutlined, ReloadOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Button, Divider, message, Popconfirm, Space } from 'antd';
import { useRef } from 'react';
import { history, Link } from 'umi';
import { deleteExternalStorage, queryExternalStorageList } from './service';
import type { IExternalStorage } from './typings';
import {
  EExternalStorageState,
  EXTERNAL_STORAGE_TYPE_LIST,
  EXTERNAL_STORAGE_USAGE_MAP,
} from './typings';

const AbnormalEventRuleList = () => {
  const actionRef = useRef<ActionType>();
  const handleDelete = (id: string) => {
    deleteExternalStorage(id).then(({ success }) => {
      if (!success) {
        message.error('删除失败');
      }
      actionRef.current?.reload();
    });
  };

  const columns: ProColumns<IExternalStorage>[] = [
    {
      title: '名称',
      dataIndex: 'name',
      align: 'center',
      width: '15%',
    },
    {
      title: '用途',
      dataIndex: 'usage',
      align: 'center',
      width: '14%',
      valueEnum: Object.keys(EXTERNAL_STORAGE_USAGE_MAP).reduce(
        (obj, usageKey) => ({
          ...obj,
          [usageKey]: {
            text: EXTERNAL_STORAGE_USAGE_MAP[usageKey],
          },
        }),
        {},
      ),
    },
    {
      title: '服务器类型',
      dataIndex: 'type',
      align: 'center',
      width: '14%',
      valueEnum: EXTERNAL_STORAGE_TYPE_LIST.reduce(
        (obj, type) => ({
          ...obj,
          [type]: {
            text: type,
          },
        }),
        {},
      ),
    },
    {
      title: 'IP地址',
      dataIndex: 'ipAddress',
      align: 'center',
      width: '14%',
    },
    {
      title: '启用状态',
      dataIndex: 'state',
      align: 'center',
      width: '10%',
      valueEnum: {
        [EExternalStorageState.Open]: {
          text: '启用',
          status: 'Success',
        },
        [EExternalStorageState.Closed]: {
          text: '禁用',
          status: 'Default',
        },
      },
    },
    {
      title: '连接状态',
      dataIndex: 'description',
      align: 'center',
      width: '16%',
    },
    {
      title: '操作',
      key: 'option',
      align: 'center',
      valueType: 'option',
      render: (text, record) => (
        <>
          <Link to={getLinkUrl(`/configuration/third-party/external-storage/${record.id}/update`)}>
            编辑
          </Link>
          <Divider type="vertical" />
          <Popconfirm
            title="确定删除吗？"
            onConfirm={() => handleDelete(record.id!)}
            icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
          >
            <a href="#">删除</a>
          </Popconfirm>
        </>
      ),
    },
  ];

  return (
    <>
      <div style={{ marginBottom: 10, textAlign: 'right' }}>
        <Space>
          <Button
            key="create"
            type="primary"
            icon={<ReloadOutlined />}
            onClick={() => {
              actionRef.current?.reload();
            }}
          >
            刷新
          </Button>

          <Button
            key="create"
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              history.push(getLinkUrl(`/configuration/third-party/external-storage/create`));
            }}
          >
            新建
          </Button>
        </Space>
      </div>
      <ProTable<IExternalStorage>
        bordered
        size="small"
        columns={columns}
        request={async () => {
          const { success, result } = await queryExternalStorageList();
          if (!success) {
            return {
              data: [],
              success,
            };
          }

          return {
            data: Array.isArray(result) ? result : [],
            success,
          };
        }}
        search={false}
        rowKey="id"
        pagination={false}
        actionRef={actionRef}
        dateFormatter="string"
        toolBarRender={false}
        options={false}
      />
    </>
  );
};

export default AbnormalEventRuleList;
