import { getTablePaginationDefaultSettings } from '@/common/app';
import { getLinkUrl } from '@/utils/utils';
import { PlusOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import type { ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Button, Divider, Popconfirm, Tooltip } from 'antd';
import { useState } from 'react';
import type { Dispatch } from 'umi';
import { history, useDispatch } from 'umi';
import { DEFAULT_POLICY_ID } from './components/IngestPolicyForm';
import { queryIngestPolicies } from './service';
import type { IIngestPolicy } from './typings';
import { v1 as uuidv1 } from 'uuid';
import ConnectCmsState from '../components/ConnectCmsState';
import LinkButton from '@/components/LinkButton';

export default () => {
  const dispatch = useDispatch<Dispatch>();

  // 强制表格刷新
  const [tableKey, setTableKey] = useState(uuidv1());

  const handleDelete = ({ id }: IIngestPolicy) => {
    dispatch({
      type: 'ingestPolicyModel/deleteIngestPolicy',
      payload: id,
    }).then(() => {
      setTableKey(uuidv1());
    });
  };

  const [cmsConnectFlag, setCmsConnectFlag] = useState(false);

  const renderDeleteDom = (record: IIngestPolicy) => {
    const { id, referenceCount } = record;
    if (cmsConnectFlag) {
      return (
        <Button size="small" type="link" disabled={cmsConnectFlag}>
          删除
        </Button>
      );
    }
    if (id === DEFAULT_POLICY_ID) {
      return (
        <Tooltip title="默认策略无法删除">
          <Button size="small" type="link" disabled={true}>
            删除
          </Button>
        </Tooltip>
      );
    }
    if (referenceCount > 0) {
      return (
        <Tooltip title="策略已被使用，无法删除">
          <Button size="small" type="link" disabled={true}>
            删除
          </Button>
        </Tooltip>
      );
    }
    return (
      <Popconfirm
        title="确定删除吗？"
        onConfirm={() => handleDelete(record)}
        icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
      >
        <Button type={'link'} size={'small'} disabled={cmsConnectFlag}>
          删除
        </Button>
      </Popconfirm>
    );
  };

  const tableColumns: ProColumns<IIngestPolicy>[] = [
    {
      title: '名称',
      dataIndex: 'name',
      search: false,
      ellipsis: true,
    },
    {
      title: '策略引用次数',
      key: 'referenceCount',
      search: false,
      dataIndex: 'referenceCount',
    },
    {
      title: '描述',
      dataIndex: 'description',
      search: false,
      ellipsis: true,
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      dataIndex: 'action',
      search: false,
      align: 'center',
      render: (text, record) => (
        <>
          <LinkButton
            onClick={() => {
              history.push(getLinkUrl(`/configuration/netflow/ingest/policy/${record.id}/examine`));
            }}
            style={{ display: cmsConnectFlag ? '' : 'none' }}
          >
            查看
          </LinkButton>
          <Divider type="vertical" style={{ display: cmsConnectFlag ? '' : 'none' }} />
          <LinkButton
            onClick={() => {
              history.push(getLinkUrl(`/configuration/netflow/ingest/policy/${record.id}/update`));
            }}
            disabled={cmsConnectFlag}
          >
            编辑
          </LinkButton>
          <Divider type="vertical" />

          {renderDeleteDom(record)}
        </>
      ),
    },
  ];

  return (
    <>
      <ConnectCmsState onConnectFlag={setCmsConnectFlag} />
      <ProTable<IIngestPolicy>
        bordered
        rowKey="id"
        key={tableKey}
        size="small"
        columns={tableColumns}
        request={async (params = {}) => {
          const { current, pageSize, ...rest } = params;
          const newParams = {
            pageSize,
            page: current! - 1,
            ...rest,
          } as any;
          const { success, result } = await queryIngestPolicies(newParams);

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
          optionRender: () => [
            <Button
              key="button"
              icon={<PlusOutlined />}
              type="primary"
              disabled={cmsConnectFlag}
              onClick={() =>
                history.push(getLinkUrl('/configuration/netflow/ingest/policy/create'))
              }
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
};
