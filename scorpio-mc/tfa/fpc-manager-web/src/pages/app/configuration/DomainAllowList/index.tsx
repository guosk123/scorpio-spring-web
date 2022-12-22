import { getTablePaginationDefaultSettings } from '@/common/app';
import { API_BASE_URL, API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import EllipsisDiv from '@/components/EllipsisDiv';
import Import from '@/components/Import';
import { ExportOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { useSafeState } from 'ahooks';
import { Button, Descriptions, Drawer, Form, Input, message, Modal, Popconfirm, Space } from 'antd';
import { useRef, useState } from 'react';
import { history } from 'umi';
import {
  deleteDomainAllowList,
  deleteDomainAllowListItem,
  importDomainAllowList,
  queryDomainAllowList,
} from './service';
import type { DomainAllowListItem } from './typings';

const jumpDetail = (id: string) => {
  history.push(`/configuration/objects/domain-allow-list/${id}/update`);
};

const DomainAllowList = () => {
  const actionRef = useRef<ActionType>();

  const [deleteModalVisible, setDeleteModalVisible] = useState(false);
  const [deleteLoading, setDeleteLoading] = useSafeState(false);
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [detail, setDetail] = useState<DomainAllowListItem>();

  const toggleDrawer = () => {
    setDrawerVisible((prev) => !prev);
  };

  const columns: ProColumns<DomainAllowListItem>[] = [
    {
      dataIndex: 'name',
      title: '名称',
      width: 200,
    },
    {
      dataIndex: 'domain',
      title: '域名',
      ellipsis: true,
      render: (dom) => {
        return <EllipsisDiv>{dom}</EllipsisDiv>;
      },
    },
    {
      dataIndex: 'description',
      title: '描述',
      search: false,
      width: 400,
    },
    {
      dataIndex: 'operation',
      title: '操作',
      search: false,
      width: 210,
      render: (_, record) => {
        const { id } = record;
        return (
          <Space>
            <span
              className="link"
              onClick={() => {
                jumpDetail(id);
              }}
            >
              编辑
            </span>
            <Popconfirm
              title="是否确认删除"
              onConfirm={() => {
                deleteDomainAllowListItem(id).then((res) => {
                  const { success } = res;
                  if (success) {
                    message.success('删除成功');
                  } else {
                    message.error('删除失败');
                  }
                  actionRef.current?.reload();
                });
              }}
            >
              <span style={{ color: 'red' }}>删除</span>
            </Popconfirm>
            <span
              className="link"
              onClick={() => {
                setDetail(record);
                toggleDrawer();
              }}
            >
              详情
            </span>
          </Space>
        );
      },
    },
  ];

  const handleImport = (formData: any, handleCloseModal: () => void) => {
    importDomainAllowList(formData).then((res) => {
      const { success } = res;
      if (success) {
        message.success('导入成功');
        actionRef.current?.reload();
      } else {
        message.error('导入失败');
      }
      handleCloseModal();
    });
  };

  const handleExport = () => {
    window.open(`${API_BASE_URL}${API_VERSION_PRODUCT_V1}/appliance/domain-white/as-export`);
  };

  const handleDeleteAll = () => {
    setDeleteLoading(true);
    deleteDomainAllowList().then((res) => {
      const { success } = res;
      setDeleteLoading(false);
      if (success) {
        message.success('删除成功');
        actionRef.current?.reload();
      } else {
        message.error('删除失败');
      }
    });
  };

  const toggleDeleteModalVisible = () => {
    setDeleteModalVisible((prev) => !prev);
  };

  const deleteRule = (values: any) => {
    setDeleteLoading(true);
    deleteDomainAllowList(values).then((res) => {
      const { success } = res;
      setDeleteLoading(false);
      if (success) {
        message.success('删除成功');
        actionRef.current?.reload();
        toggleDeleteModalVisible();
      } else {
        message.error('删除失败');
      }
    });
  };

  return (
    <div>
      <ProTable
        bordered
        size="small"
        actionRef={actionRef}
        columns={columns}
        toolBarRender={false}
        pagination={getTablePaginationDefaultSettings()}
        request={async (params) => {
          const { current, ...rest } = params;
          const { success, result } = await queryDomainAllowList({
            ...rest,
            page: current ? current - 1 : 0,
          });
          if (!success) {
            return {
              total: 0,
              success: true,
              data: [],
            };
          }
          return {
            total: result.totalElements,
            success: true,
            data: result.content,
          };
        }}
        search={{
          optionRender(config, _, dom) {
            return [
              ...dom,
              <Import
                key="import"
                loading={undefined}
                modalTitle={'导入白名单'}
                tempDownloadUrl={'/appliance/domain-white/as-template'}
                customImportFunc={handleImport}
              />,
              <Button key="export" icon={<ExportOutlined />} onClick={handleExport}>
                导出
              </Button>,
              <Popconfirm key="deleteAll" title="是否确定删除全部名单" onConfirm={handleDeleteAll}>
                <Button danger loading={deleteLoading}>
                  删除全部
                </Button>
              </Popconfirm>,
              <Button danger key="delete" onClick={toggleDeleteModalVisible}>
                条件删除
              </Button>,
              <Button
                key="create"
                type="primary"
                onClick={() => {
                  history.push('/configuration/objects/domain-allow-list/create');
                }}
              >
                新建
              </Button>,
            ];
          },
        }}
      />
      <Modal
        visible={deleteModalVisible}
        title="根据条件删除白名单"
        footer={null}
        onCancel={toggleDeleteModalVisible}
      >
        <Form onFinish={deleteRule} labelCol={{ span: 3 }}>
          <Form.Item label="名称" name="name">
            <Input placeholder="请输入删除的规则名称" />
          </Form.Item>
          <Form.Item label="域名" name="domain">
            <Input placeholder="请输入需要删除的域名" />
          </Form.Item>
          <Form.Item wrapperCol={{ offset: 3 }}>
            <Space>
              <Button type="primary" htmlType="submit" loading={deleteLoading}>
                删除
              </Button>
              <Button onClick={toggleDeleteModalVisible}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
      <Drawer visible={drawerVisible} onClose={toggleDrawer} width={600}>
        {detail && (
          <Descriptions
            bordered
            column={1}
            size="small"
            title="白名单详情"
            labelStyle={{ width: 100 }}
          >
            <Descriptions.Item label="名称">{detail.name}</Descriptions.Item>
            <Descriptions.Item label="域名">{detail.domain}</Descriptions.Item>
            <Descriptions.Item label="描述">{detail.description}</Descriptions.Item>
          </Descriptions>
        )}
      </Drawer>
    </div>
  );
};

export default DomainAllowList;
