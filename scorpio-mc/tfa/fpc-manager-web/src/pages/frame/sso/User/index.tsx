import { getTablePaginationDefaultSettings, proTableSerchConfig } from '@/common/app';
import type { ConnectState } from '@/models/connect';
import { PlusOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Button, Divider, Popconfirm, Select } from 'antd';
import { connect } from 'dva';
import { useEffect, useRef } from 'react';
import { history, Link } from 'umi';
import type { ISsoModelStateType } from '../model';
import { querySsoUsers } from '../service';
import type { ISsoUser } from '../typings';

interface Props {
  dispatch: any;
  ssoModel: ISsoModelStateType;
  queryAllSsoPlatformsLoading?: boolean;
}

function SsoUser(props: Props) {
  const { dispatch, ssoModel, queryAllSsoPlatformsLoading } = props;
  const actionRef = useRef<ActionType>();
  // 删除
  const handleDelete = ({ id }: ISsoUser) => {
    dispatch({
      type: 'ssoModel/deleteSsoUser',
      payload: { id },
    }).then(() => actionRef.current?.reload());
  };

  const columns: ProColumns<ISsoUser>[] = [
    {
      title: '外部系统',
      dataIndex: 'platformId',
      hideInTable: true,
      renderFormItem: () => {
        return (
          <Select placeholder="选择外部系统" loading={queryAllSsoPlatformsLoading}>
            <Select.Option value="">全部</Select.Option>
            {ssoModel.allSsoPlatforms.map((plat: any) => (
              <Select.Option value={plat.id as any}>{plat.name}</Select.Option>
            ))}
          </Select>
        );
      },
    },
    {
      title: '外部系统',
      dataIndex: 'ssoPlatformName',
      align: 'center',
      search: false,
      ellipsis: true,
    },
    {
      title: '外部用户ID',
      dataIndex: 'platformUserId',
      align: 'center',
      search: false,
      ellipsis: true,
    },
    {
      title: '本系统用户',
      dataIndex: 'systemUserName',
      align: 'center',
      search: false,
    },
    {
      title: '备注',
      dataIndex: 'description',
      align: 'center',
      search: false,
      ellipsis: true,
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      align: 'center',
      search: false,
      valueType: 'dateTime',
    },
    {
      title: '操作',
      width: 100,
      dataIndex: 'action',
      align: 'center',
      search: false,
      render: (text, record) => (
        <>
          <Link to={`/system/sso/user/update?id=${record.id}`}>编辑</Link>
          <Divider type="vertical" />
          <Popconfirm
            title="确定删除吗？"
            onConfirm={() => handleDelete(record)}
            icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
          >
            <a>删除</a>
          </Popconfirm>
        </>
      ),
    },
  ];

  useEffect(() => {
    dispatch({ type: 'ssoModel/queryAllSsoPlatforms' });
  }, [dispatch]);

  return (
    <ProTable
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
        const { success, result } = await querySsoUsers(newParams);
        return {
          data: result.content,
          page: result.number,
          total: result.totalElements,
          success,
        };
      }}
      search={{
        ...proTableSerchConfig,
        span: 6,
        optionRender: (searchConfig, formProps, dom) => [
          ...dom.reverse(),
          <Button
            key="create"
            icon={<PlusOutlined />}
            type="primary"
            onClick={() => history.push('/system/sso/user/create')}
          >
            新建
          </Button>,
        ],
      }}
      toolBarRender={false}
      pagination={getTablePaginationDefaultSettings()}
    />
  );
}
export default connect(
  ({ ssoModel, loading }: ConnectState & { ssoModel: ISsoModelStateType }) => ({
    ssoModel,
    queryAllSsoPlatformsLoading: loading.effects['ssoModel/queryAllSsoPlatforms'],
  }),
)(SsoUser);
