import { getTablePaginationDefaultSettings, proTableSerchConfig } from '@/common/app';
import type { ConnectState } from '@/models/connect';
import { getLinkUrl } from '@/utils/utils';
import { PlusOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Button, Divider, Popconfirm, Select } from 'antd';
import { connect } from 'dva';
import { Fragment, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import { history } from 'umi';
import type { IL7Protocol, IL7ProtocolMap } from '../../appliance/Metadata/typings';
import ConnectCmsState from '../components/ConnectCmsState';
import { STANDARD_PROTOCOL_SOURCE_DEFAULT, STANDARD_PROTOCOL_SOURCE_LIST } from './components/Form';
import { queryStandardProtocols } from './service';

export interface StandardProtocalItem {
  id: string;
  ipProtocol: string;
  l7ProtocolId: string;
  port: string;
  source: string;
  sourceText: string;
}

interface Props {
  dispatch: Dispatch;
  metadataProtocolMap: IL7ProtocolMap;
  metadataProtocolsList: IL7Protocol[];
  allL7ProtocolsList: IL7Protocol[];
}

function StandardProtocol(props: Props) {
  const { allL7ProtocolsList, dispatch } = props;
  const actionRef = useRef<ActionType>();
  const [cmsConnectFlag, setCmsConnectFlag] = useState(false);
  const handleDelete = ({ id }: { id: string }) => {
    dispatch({
      type: 'standardProtocolModel/deleteStandardProtocol',
      payload: { id },
    }).then(() => {
      actionRef.current?.reload();
    });
  };
  const handleAddClick = () => {
    history.push(getLinkUrl('/configuration/objects/standard-protocol/create'));
  };
  const columns: ProColumns<StandardProtocalItem>[] = [
    {
      title: '协议',
      dataIndex: 'l7ProtocolId',
      align: 'center',
      render: (text, record) => {
        return allL7ProtocolsList?.find((item) => item?.protocolId === record.l7ProtocolId)
          ?.nameText;
      },
      renderFormItem: () => {
        return (
          <Select showSearch optionFilterProp="title" placeholder="请选择协议">
            <Select.Option value="">全部</Select.Option>
            {allL7ProtocolsList?.map((item) => (
              <Select.Option value={item?.protocolId} title={item?.nameText} key={item?.protocolId}>
                {item?.nameText}
              </Select.Option>
            ))}
          </Select>
        );
      },
    },
    {
      title: '传输层协议',
      dataIndex: 'ipProtocol',
      align: 'center',
      search: false,
      render: (text, record) => record.ipProtocol && record.ipProtocol.toLocaleUpperCase(),
    },
    {
      title: '端口',
      dataIndex: 'port',
      align: 'center',
    },
    {
      title: '属性',
      dataIndex: 'sourceText',
      align: 'center',
      renderFormItem: () => {
        return (
          <Select placeholder="请选择属性">
            <Select.Option value="">全部</Select.Option>
            {STANDARD_PROTOCOL_SOURCE_LIST.map((item) => (
              <Select.Option value={item.value} key={item.value}>
                {item.label}
              </Select.Option>
            ))}
          </Select>
        );
      },
    },
    {
      title: '操作',
      width: 150,
      align: 'center',
      search: false,
      render: (text, record) => {
        if (record.source === STANDARD_PROTOCOL_SOURCE_DEFAULT) {
          return null;
        }
        return (
          <Fragment>
            <Button
              onClick={() => {
                history.push(
                  getLinkUrl(`/configuration/objects/standard-protocol/${record.id}/update`),
                );
              }}
              disabled={cmsConnectFlag}
            >
              编辑
            </Button>
            <Divider type="vertical" />
            <Popconfirm
              title="确定删除吗？"
              onConfirm={() => handleDelete({ id: record.id })}
              icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
            >
              <a>删除</a>
            </Popconfirm>
          </Fragment>
        );
      },
    },
  ];
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
          const newParams = {
            pageSize,
            page: current && current - 1,
            ...rest,
            source: params?.sourceText,
            protocol: params?.l7ProtocolId,
          };
          const { success, result } = await queryStandardProtocols(newParams);
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
              onClick={() => handleAddClick()}
              disabled={cmsConnectFlag}
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
export default connect((state: ConnectState) => {
  const {
    metadataModel: { metadataProtocolMap, metadataProtocolsList, allL7ProtocolsList },
  } = state;
  return {
    metadataProtocolMap,
    metadataProtocolsList,
    allL7ProtocolsList,
  };
})(StandardProtocol);
