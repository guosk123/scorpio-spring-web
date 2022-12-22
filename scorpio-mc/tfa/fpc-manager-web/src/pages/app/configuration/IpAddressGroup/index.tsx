import { getTablePaginationDefaultSettings, proTableSerchConfig } from '@/common/app';
import { API_BASE_URL, API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import Ellipsis from '@/components/Ellipsis';
import Import from '@/components/Import';
import LinkButton from '@/components/LinkButton';
import { ConnectState } from '@/models/connect';
import { queryIpAddressGroups } from '@/services/app/ipAddressGroup';
import { getLinkUrl } from '@/utils/utils';
import { ExportOutlined, PlusOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Button, Divider, Popconfirm, Tooltip } from 'antd';
import { connect } from 'dva';
import { useMemo, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import { history } from 'umi';
import ConnectCmsState from '../components/ConnectCmsState';
import type { IpAddressGroup } from './typings';

interface Props {
  dispatch: Dispatch;
  importLoading?: boolean;
}

export const MAX_IP_ADDRESS_GROUP_LIMIT = 100;

function IpAddressGroupList(props: Props) {
  const { dispatch, importLoading } = props;
  const [total, setTotal] = useState(0);
  const [cmsConnectFlag, setCmsConnectFlag] = useState(false);
  const searchName = useRef<string>();
  // 删除
  const actionRef = useRef<ActionType>();
  const handleDelete = ({ id }: IpAddressGroup) => {
    dispatch({
      type: 'ipAddressGroupModel/deleteIpAddressGroup',
      payload: { id },
    }).then(() => actionRef.current?.reload());
  };
  const columns: ProColumns<IpAddressGroup>[] = [
    {
      title: '名称',
      dataIndex: 'name',
      align: 'center',
    },
    {
      title: 'IP地址',
      dataIndex: 'ipAddress',
      align: 'center',
      search: false,
      render: (ipAddress) => <Ellipsis lines={6}>{ipAddress}</Ellipsis>,
    },
    {
      title: '描述',
      dataIndex: 'description',
      align: 'center',
      search: false,
      ellipsis: true,
    },
    {
      title: '操作',
      width: 150,
      align: 'center',
      search: false,
      render: (text, record) => (
        <>
          <LinkButton
            onClick={() => {
              history.push(getLinkUrl(`/configuration/objects/hostgroup/${record.id}/update`));
            }}
            disabled={cmsConnectFlag}
          >
            编辑
          </LinkButton>
          <Divider type="vertical" />
          <Popconfirm
            title="确定删除吗？"
            onConfirm={() => handleDelete(record)}
            icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
          >
            <LinkButton disabled={cmsConnectFlag}>删除</LinkButton>
          </Popconfirm>
        </>
      ),
    },
  ];
  /** 导出 */
  const handleExport = () => {
    const url = `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/appliance/host-groups/as-export${
      searchName.current ? `?name=${searchName.current}` : ''
    }`;
    window.open(url);
  };

  const renderCreateButton = useMemo(() => {
    // 超出最大限制
    if (total >= MAX_IP_ADDRESS_GROUP_LIMIT) {
      return (
        <Tooltip
          title={`最多支持新建${MAX_IP_ADDRESS_GROUP_LIMIT}个IP地址组`}
          key="create-disabled"
        >
          <Button icon={<PlusOutlined />} disabled>
            新建
          </Button>
        </Tooltip>
      );
    }

    return (
      <>
        <Import
          key="inport"
          modalTitle="地址组导入"
          loading={importLoading}
          importFunc="ipAddressGroupModel/importIpAddressGroup"
          tempDownloadUrl="/appliance/host-groups/as-template"
          importSuccessCallback={() => actionRef.current?.reload()}
          disabled={cmsConnectFlag}
        />
        <Button key="export" icon={<ExportOutlined />} onClick={handleExport}>
          导出
        </Button>
        <Button
          key="create"
          icon={<PlusOutlined />}
          type="primary"
          onClick={() => history.push(getLinkUrl('/configuration/objects/hostgroup/create'))}
          disabled={cmsConnectFlag}
        >
          新建
        </Button>
      </>
    );
  }, [cmsConnectFlag, total]);

  return (
    <>
      <ConnectCmsState onConnectFlag={setCmsConnectFlag} />
      <ProTable
        rowKey="id"
        bordered
        size="small"
        columns={columns}
        actionRef={actionRef}
        request={async (params = {}) => {
          const { current = 0, pageSize, startTime, endTime, ...rest } = params;
          searchName.current = rest?.name || '';
          const newParams = { pageSize, page: current && current - 1, ...rest };
          const { success, result } = await queryIpAddressGroups(newParams);

          setTotal(success ? result.totalElements : 0);

          return {
            data: result.content,
            page: result.number,
            total: result.totalElements,
            success,
          };
        }}
        search={{
          ...proTableSerchConfig,
          optionRender: (searchConfig, formProps, dom) => [...dom.reverse(), renderCreateButton],
        }}
        toolBarRender={false}
        pagination={getTablePaginationDefaultSettings()}
      />
    </>
  );
}

export default connect(({ loading: { effects } }: ConnectState) => ({
  importLoading: effects['ipAddressGroupModel/importIpAddressGroup'],
}))(IpAddressGroupList);
