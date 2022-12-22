import { getTablePaginationDefaultSettings, proTableSerchConfig } from '@/common/app';
import { PlusOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Button, Divider, Popconfirm } from 'antd';
import { connect } from 'dva';
import { useRef } from 'react';
import type { Dispatch } from 'umi';
import { history, Link } from 'umi';
import { querySsoPlatforms } from '../service';
import type { ISsoPlatform } from '../typings';

interface Props {
  dispatch: Dispatch;
}

function SsoPlatform(props: Props) {
  const { dispatch } = props;
  const actionRef = useRef<ActionType>();
  // 删除
  const handleDelete = ({ id }: ISsoPlatform) => {
    dispatch({
      type: 'ssoModel/deleteSsoPlatform',
      payload: { id },
    }).then(() => actionRef.current?.reload());
  };
  const columns: ProColumns<ISsoPlatform>[] = [
    {
      title: '外部系统名称',
      dataIndex: 'name',
      align: 'center',
      ellipsis: true,
    },
    {
      title: '外部系统ID',
      dataIndex: 'platformId',
      align: 'center',
      search: false,
      ellipsis: true,
    },
    {
      title: 'Token',
      dataIndex: 'appToken',
      align: 'center',
      search: false,
      render: () => '********',
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
          <Link to={`/system/sso/platform/update?id=${record.id}`}>编辑</Link>
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
  return (
    <>
      <ProTable
        rowKey="id"
        bordered
        size="small"
        columns={columns}
        actionRef={actionRef}
        request={async (params) => {
          const { current = 0, pageSize, ...rest } = params;
          const newParams = {
            pageSize,
            page: current && current - 1,
            ...rest,
          };
          const { success, result } = await querySsoPlatforms(newParams);
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
          labelWidth: 'auto',
          optionRender: (searchConfig, formProps, dom) => [
            ...dom.reverse(),
            <Button
              key="create"
              icon={<PlusOutlined />}
              type="primary"
              onClick={() => history.push('/system/sso/platform/create')}
            >
              新建
            </Button>,
          ],
        }}
        toolBarRender={false}
        pagination={getTablePaginationDefaultSettings()}
      />
    </>
  );
}
export default connect()(SsoPlatform);
