import {
  getTablePaginationDefaultSettings,
  proTableSerchConfig,
  PRO_TABLE_RESET_SPAN_SIZE,
} from '@/common/app';
import Ellipsis from '@/components/Ellipsis';
import Import from '@/components/Import';
import { queryIpAddressGroups } from '@/services/app/ipAddressGroup';
import { getLinkUrl } from '@/utils/utils';
import { ExportOutlined, PlusOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Button, Divider, Popconfirm, Tooltip } from 'antd';
import { connect } from 'dva';
import { useEffect, useMemo, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import application from '@/common/applicationConfig';
import { history } from 'umi';
import ConnectCmsState from '../components/ConnectCmsState';
import type { IpAddressGroup } from './typings';
import type { ConnectState } from '@/models/connect';

const { API_BASE_URL, API_VERSION_PRODUCT_V1 } = application;

interface Props {
  dispatch: Dispatch;
  importLoading?: boolean;
}

export const MAX_IP_ADDRESS_GROUP_LIMIT = 100;

function IpAddressGroupList(props: Props) {
  const { dispatch, importLoading } = props;
  const [total, setTotal] = useState(0);
  const [cmsConnectFlag, setCmsConnectFlag] = useState(false);
  // 删除
  const actionRef = useRef<ActionType>();
  const handleDelete = ({ id }: IpAddressGroup) => {
    dispatch({
      type: 'ipAddressGroupModel/deleteIpAddressGroup',
      payload: { id },
    }).then(() => actionRef.current?.reload());
  };
  const [tSpan, setTSpan] = useState(window.innerWidth > PRO_TABLE_RESET_SPAN_SIZE ? 8 : 12);

  useEffect(() => {
    window.addEventListener('resize', () => {
      setTSpan(window.innerWidth > PRO_TABLE_RESET_SPAN_SIZE ? 8 : 12);
    });

    return () => {
      window.removeEventListener('resize', () => {});
    };
  }, []);
  const columns: ProColumns<IpAddressGroup>[] = [
    {
      title: '#',
      dataIndex: 'index',
      key: 'index',
      align: 'center',
      width: 60,
      search: false,
      render: (text, record, index) => `${index + 1}`,
    },
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
          <Button
            type="link"
            size="small"
            onClick={() => {
              history.push(getLinkUrl(`/configuration/objects/hostgroup/${record.id}/update`));
            }}
            disabled={cmsConnectFlag}
          >
            编辑
          </Button>
          <Divider type="vertical" />
          <Popconfirm
            title="确定删除吗？"
            onConfirm={() => handleDelete(record)}
            icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
          >
            <Button type="link" size="small" disabled={cmsConnectFlag}>
              删除
            </Button>
          </Popconfirm>
        </>
      ),
    },
  ];
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

    /** 导出 */
    const handleExport = () => {
      const url = `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/appliance/host-groups/as-export`;
      window.location.href = url;
    };

    return (
      <>
        <Import
          key="inport"
          modalTitle="业务导入"
          loading={importLoading}
          importFunc="ipAddressGroupModel/importIpAddressGroups"
          tempDownloadUrl="/appliance/host-groups/as-template"
          importSuccessCallback={() => actionRef.current?.reload()}
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
          span: tSpan,
          optionRender: (searchConfig, formProps, dom) => [...dom.reverse(), renderCreateButton],
        }}
        toolBarRender={false}
        pagination={getTablePaginationDefaultSettings()}
      />
    </>
  );
}

export default connect(({ loading: { effects } }: ConnectState) => ({
  importLoading: effects['ipAddressGroupModel/importIpAddressGroups'],
}))(IpAddressGroupList);
