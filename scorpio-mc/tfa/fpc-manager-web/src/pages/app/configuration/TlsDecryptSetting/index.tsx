import { proTableSerchConfig, getTablePaginationDefaultSettings } from '@/common/app';
import { getLinkUrl } from '@/utils/utils';
import { PlusOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Button, Divider, Popconfirm, Tooltip } from 'antd';
import { connect } from 'dva';
import { Fragment, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import { history, Link } from 'umi';
import { queryAllTlsDecryptSettings } from './service';

/**
 * 最大数量限制
 */
export const MAX_LIMIT = 100;

export interface TlsDecryptSettingItem {
  id: string;
  ipAddress: string;
  port: string;
  protocol: string;
}

function TlsDecryptSetting(props: { dispatch: Dispatch }) {
  const { dispatch } = props;
  // 是否达到最大数量限制
  const [isMaxTotalTslSetting, setIsMaxTotalTslSetting] = useState<boolean>(false);
  const actionRef = useRef<ActionType>();
  // 删除
  const handleDelete = (id: string) => {
    dispatch({
      type: 'tlsDecryptSettingModel/deleteTlsDecryptSetting',
      payload: { id },
    }).then(() => {
      actionRef.current?.reload();
    });
  };

  const columns: ProColumns<TlsDecryptSettingItem>[] = [
    {
      title: '#',
      dataIndex: 'index',
      align: 'center',
      valueType: 'index',
      width: 60,
    },
    {
      title: 'IP地址',
      dataIndex: 'ipAddress',
      align: 'center',
    },
    {
      title: '端口',
      dataIndex: 'port',
      align: 'center',
    },
    {
      title: '协议',
      dataIndex: 'protocol',
      align: 'center',
      render: (text) => text && String(text).toLocaleUpperCase(),
    },
    {
      title: '操作',
      width: 150,
      align: 'center',
      search: false,
      render: (text, record) => {
        return (
          <Fragment>
            <Link
              to={getLinkUrl(`/configuration/knowledge/tls-decrypt-setting/${record.id}/update`)}
            >
              编辑
            </Link>
            <Divider type="vertical" />
            <Popconfirm
              title="确定删除吗？"
              onConfirm={() => handleDelete(record.id)}
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
    <ProTable
      rowKey="id"
      bordered
      size="small"
      columns={columns}
      actionRef={actionRef}
      request={async (params = {}) => {
        const { success, result } = await queryAllTlsDecryptSettings();
        setIsMaxTotalTslSetting(result.length >= MAX_LIMIT);
        // 过滤
        let res = result;
        if (params?.ipAddress) {
          res = res.filter((item: any) => item.ipAddress === params?.ipAddress);
        }
        if (params?.port) {
          res = res.filter((item: any) => item.port === params?.port);
        }
        if (params?.protocol) {
          res = res.filter((item: any) => item.protocol === params?.protocol);
        }
        return {
          data: res,
          total: res.length,
          success,
        };
      }}
      search={{
        ...proTableSerchConfig,
        span: 6,
        optionRender: (searchConfig, formProps, dom) => [
          ...dom.reverse(),
          <Tooltip
            key="created"
            mouseEnterDelay={isMaxTotalTslSetting ? 0.1 : 99999}
            title={`最多支持新建${MAX_LIMIT}个TLS协议私钥`}
          >
            <Button
              icon={<PlusOutlined />}
              type="primary"
              disabled={isMaxTotalTslSetting}
              onClick={() =>
                history.push(getLinkUrl('/configuration/knowledge/tls-decrypt-setting/create'))
              }
            >
              新建
            </Button>
          </Tooltip>,
        ],
      }}
      toolBarRender={false}
      pagination={getTablePaginationDefaultSettings()}
    />
  );
}
export default connect()(TlsDecryptSetting);
