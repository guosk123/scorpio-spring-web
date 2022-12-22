import { getTablePaginationDefaultSettings, proTableSerchConfig } from '@/common/app';
import Ellipsis from '@/components/Ellipsis';
import { jumpSamePage } from '@/utils/utils';
import type { ActionType, ProColumnType } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { useSafeState } from 'ahooks';
import { Button, Modal, Popconfirm } from 'antd';
import { useRef, useState } from 'react';
import { history } from 'umi';
import CategoryPie from './components/CategoryPie';
import { deleteIpLabel, queryIpLabel } from './service';
import type { IIpLabel } from './typings';
import { IpLabelCategoryText } from './typings';

const IpLabel = () => {
  const actionRef = useRef<ActionType>();

  const [modalVisible, setModalVisible] = useState(false);
  const [data, setData] = useSafeState<IIpLabel[]>([]);

  const handleDelete = (name: string) => {
    deleteIpLabel(name).then(() => {
      actionRef.current?.reload();
    });
  };

  const columns: ProColumnType<IIpLabel>[] = [
    {
      dataIndex: 'name',
      align: 'center',
      title: '名称',
      width: 150,
    },
    {
      dataIndex: 'category',
      align: 'center',
      title: '分类',
      valueType: 'select',
      valueEnum: IpLabelCategoryText,
      width: 150,
    },
    {
      dataIndex: 'ipAddress',
      align: 'center',
      title: 'IP地址',
      search: false,
    },
    {
      dataIndex: 'description',
      align: 'center',
      title: '描述',
      search: false,
      render: (dom) => {
        return <Ellipsis>{dom}</Ellipsis>;
      },
    },
    {
      title: '操作',
      align: 'center',
      dataIndex: 'operate',
      search: false,
      width: 150,
      render: (_, record) => {
        return (
          <>
            <span
              className="link"
              onClick={() => {
                jumpSamePage(`/configuration/objects/ip-label/${record.id}/update`);
              }}
            >
              编辑
            </span>
            <Popconfirm
              title={'是否确认删除'}
              onConfirm={() => {
                handleDelete(record.id);
              }}
            >
              <span style={{ color: 'red', marginLeft: '1em' }}>删除</span>
            </Popconfirm>
          </>
        );
      },
    },
  ];

  return (
    <>
      <ProTable
        bordered
        size="small"
        columns={columns}
        actionRef={actionRef}
        toolBarRender={false}
        pagination={{ ...getTablePaginationDefaultSettings() }}
        request={async (params) => {
          const search: any = {};
          if (params.name) {
            search.name = params.name;
          }
          if (params.category) {
            search.category = params.category;
          }
          search.page = params.current! - 1;
          search.pageSize = params.pageSize;
          const { success, result } = await queryIpLabel(search);

          if (success) {
            setData(result.content);
          }
          return {
            data: result.content,
            total: result.totalElements,
            success,
          };
        }}
        search={{
          ...proTableSerchConfig,
          labelWidth: 'auto',
          span: 8,
          optionRender: (searchConfig, formProps, dom) => {
            return [
              <Button
                key="stat"
                type="primary"
                disabled={data.length < 1}
                onClick={() => {
                  setModalVisible(true);
                }}
              >
                分类统计
              </Button>,
              ...dom.reverse(),
              <Button
                key="created"
                type="primary"
                onClick={() => {
                  jumpSamePage('/configuration/objects/ip-label/create');
                }}
              >
                新建
              </Button>,
            ];
          },
        }}
      />
      <Modal
        width={600}
        visible={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
      >
        <CategoryPie />
      </Modal>
    </>
  );
};

export default IpLabel;
