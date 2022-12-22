import { getTablePaginationDefaultSettings, proTableSerchConfig } from '@/common/app';
import { API_BASE_URL, API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import type { IProTableData } from '@/common/typings';
import AutoHeightContainer from '@/components/AutoHeightContainer';
import Import from '@/components/Import';
import type { ConnectState } from '@/models/connect';
import { getLinkUrl } from '@/utils/utils';
import { ExportOutlined, PlusOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Button, Divider, Popconfirm, Tooltip } from 'antd';
import { Fragment, useMemo, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, history, Link } from 'umi';
import ConnectCmsState from '../components/ConnectCmsState';
import { queryServices } from './service';
import type { IService } from './typings';
import { MAX_CUSTOM_SERVICE_LIMIT } from './typings';

interface IProps {
  dispatch: Dispatch;
  importLoading?: boolean;
}
const ServiceList = ({ dispatch, importLoading }: IProps) => {
  const actionRef = useRef<ActionType>();
  const [tableHeight, setTableHeight] = useState(200);
  // 业务总数
  const [total, setTotal] = useState(0);
  const [cmsConnectFlag, setCmsConnectFlag] = useState(false);

  /** 导出 */
  const handleExport = () => {
    const url = `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/appliance/services/as-export`;
    window.open(url);
  };

  const handleDelete = ({ id }: IService) => {
    dispatch({
      type: 'serviceModel/deleteService',
      payload: id,
    }).then((success: boolean) => {
      if (success) {
        actionRef.current?.reload();
      }
    });
  };

  const isMaxService = useMemo(() => {
    return total >= MAX_CUSTOM_SERVICE_LIMIT;
  }, [total]);

  const columns: ProColumns<IService>[] = [
    {
      title: '名称',
      dataIndex: 'name',
      ellipsis: true,
    },
    {
      title: '网络',
      dataIndex: 'networkNames',
      search: false,
    },
    {
      title: '描述',
      dataIndex: 'description',
      ellipsis: true,
      search: false,
    },
    {
      title: '操作',
      dataIndex: 'option',
      width: 200,
      align: 'center',
      search: false,
      render: (text, record) => (
        <Fragment>
          <Link
            style={{ ...(cmsConnectFlag ? { pointerEvents: 'none', color: '#B8B8B8' } : {}) }}
            to={getLinkUrl(`/configuration/service-settings/service/${record.id}/update`)}
          >
            编辑
          </Link>
          <Divider type="vertical" />
          <Link to={getLinkUrl(`/configuration/service-settings/service/${record.id}/link`)}>
            业务路径
          </Link>
          <Divider type="vertical" />
          <Popconfirm
            title="确定删除吗？"
            onConfirm={() => handleDelete(record)}
            icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
            disabled={cmsConnectFlag}
          >
            <Button type="link" size="small" disabled={cmsConnectFlag}>
              删除
            </Button>
          </Popconfirm>
        </Fragment>
      ),
    },
  ];

  return (
    // 高度 - 搜索表单高度- 表头高度 - 分页高度
    <div>
      <ConnectCmsState onConnectFlag={setCmsConnectFlag} />
      <AutoHeightContainer onHeightChange={(h) => setTableHeight(h - 62 - 40 - 40)}>
        <ProTable<IService>
          bordered
          size="small"
          actionRef={actionRef}
          columns={columns}
          rowKey={(record) => `${record.id}`}
          request={async (params = {}) => {
            const { current, pageSize, ...rest } = params;
            const newParams = { pageSize, page: current! - 1, ...rest } as any;
            const { success, result } = await queryServices(newParams);
            if (!success) {
              setTotal(0);
              return {
                data: [],
                success,
              };
            }
            setTotal(result.totalElements);
            return {
              data: result.content,
              success,
              page: result.number,
              total: result.totalElements,
            } as IProTableData<IService[]>;
          }}
          search={{
            ...proTableSerchConfig,
            labelWidth: 'auto',
            span: 8,
            optionRender: (searchConfig, formProps, dom) => [
              ...dom.reverse(),
              <Import
                key="inport"
                modalTitle="业务导入"
                loading={importLoading}
                importFunc="serviceModel/importService"
                tempDownloadUrl="/appliance/services/as-template"
                importSuccessCallback={() => actionRef.current?.reload()}
                disabled={cmsConnectFlag}
              />,
              <Button key="export" icon={<ExportOutlined />} onClick={handleExport}>
                导出
              </Button>,
              <Tooltip
                title={`最多支持业务${MAX_CUSTOM_SERVICE_LIMIT}个`}
                mouseEnterDelay={isMaxService ? 0.1 : 9999}
                key="create"
              >
                <Button
                  key="button"
                  icon={<PlusOutlined />}
                  type="primary"
                  disabled={isMaxService || cmsConnectFlag}
                  onClick={() =>
                    history.push(getLinkUrl('/configuration/service-settings/service/create'))
                  }
                >
                  新建
                </Button>
              </Tooltip>,
            ],
          }}
          toolBarRender={false}
          scroll={{ y: tableHeight }}
          pagination={getTablePaginationDefaultSettings()}
        />
      </AutoHeightContainer>
    </div>
  );
};

export default connect(({ loading: { effects } }: ConnectState) => ({
  importLoading: effects['serviceModel/importService'],
}))(ServiceList);
