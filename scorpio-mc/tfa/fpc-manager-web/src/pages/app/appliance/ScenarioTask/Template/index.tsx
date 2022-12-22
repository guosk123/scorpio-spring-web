import { getTablePaginationDefaultSettings } from '@/common/app';
import EllipsisCom from '@/components/EllipsisCom';
import { getLinkUrl } from '@/utils/utils';
import '@ant-design/compatible/assets/index.css';
import { PlusOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Button, Divider, Drawer, Popconfirm } from 'antd';
import React, { useRef, useState } from 'react';
import { history, useDispatch } from 'umi';
import Profile, {
  getDataSourceInfo,
  getEvalFunctionDom,
  getGroupByInfo,
} from '../components/ScenarioTemplateProfile';
import { queryAllScenarioCustomTemplates } from '../service';
import type { IScenarioCustomTemplate } from '../typings';

const ScenarioTaskTemplate: React.FC = () => {
  const [templateId, setTemplateId] = useState('');
  const actionRef = useRef<ActionType>();

  const dispatch = useDispatch();

  const handleDelete = async ({ id }: IScenarioCustomTemplate) => {
    await dispatch({
      type: 'scenarioTaskModel/deleteScenarioCustomTemplate',
      payload: { id },
    });
  };

  const handleOpenDetail = (id: string) => {
    setTemplateId(id);
  };

  const handleCloseDetail = () => {
    setTemplateId('');
  };

  const columns: ProColumns<IScenarioCustomTemplate>[] = [
    {
      title: '模板名称',
      dataIndex: 'name',
      align: 'center',
      search: false,
      render: (text) => {
        return <EllipsisCom>{text}</EllipsisCom>;
      },
    },
    {
      title: '数据源',
      width: 120,
      dataIndex: 'dataSource',
      align: 'center',
      search: false,
      render: (text) => getDataSourceInfo(text as string).label,
    },
    {
      title: '计算方法',
      dataIndex: 'function',
      align: 'center',
      width: 160,
      search: false,
      render: (functionJson, record) => {
        return getEvalFunctionDom(record);
      },
    },
    {
      title: '按时间平均',
      dataIndex: 'avgTimeInterval',
      align: 'center',
      search: false,
      render: (avgTimeInterval) => (avgTimeInterval === 0 ? '不平均' : `${avgTimeInterval}s`),
    },
    {
      title: '按时间分片',
      dataIndex: 'sliceTimeInterval',
      align: 'center',
      search: false,
      hideInForm: true,
      render: (sliceTimeInterval) => (sliceTimeInterval === 0 ? '不分片' : `${sliceTimeInterval}s`),
    },
    {
      title: '分组',
      dataIndex: 'groupBy',
      align: 'center',
      search: false,
      hideInForm: true,
      render: (groupBy) => {
        if (!groupBy) {
          return '不分组';
        }
        return getGroupByInfo(groupBy as string).label;
      },
    },
    {
      title: '操作',
      width: 100,
      align: 'center',
      search: false,
      hideInForm: true,
      render: (text, record) => (
        <>
          <a onClick={() => handleOpenDetail(record.id!)}>详情</a>
          <Divider type="vertical" />
          <Popconfirm
            title="确定删除吗？"
            onConfirm={() => {
              actionRef.current?.reload();
              handleDelete(record);
            }}
            icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
          >
            <a>删除</a>
          </Popconfirm>
        </>
      ),
    },
  ];

  const handleAddClick = () => {
    history.push(getLinkUrl('/configuration/safety-analysis/scenario-task-template/create'));
  };

  return (
    <>
      <ProTable<IScenarioCustomTemplate>
        bordered
        rowKey="id"
        actionRef={actionRef}
        size="small"
        columns={columns}
        polling={3000}
        request={async (params = {}) => {
          const { current, pageSize, ...rest } = params;
          const newParams = {
            pageSize,
            page: current! - 1,
            ...rest,
          } as any;
          const { success, result } = await queryAllScenarioCustomTemplates({
            isDetail: true,
            ...newParams,
          });

          if (!success) {
            return {
              data: [],
              success,
            };
          }

          return {
            data: result,
            success,
            total: result.length,
          };
        }}
        search={{
          collapseRender: false,
          optionRender: () => [
            <Button key="create" icon={<PlusOutlined />} type="primary" onClick={handleAddClick}>
              新建
            </Button>,
          ],
        }}
        toolBarRender={false}
        pagination={getTablePaginationDefaultSettings()}
      />
      <Drawer
        width={600}
        destroyOnClose
        title="自定义模板详情"
        onClose={() => handleCloseDetail()}
        visible={!!templateId}
      >
        {templateId && <Profile id={templateId} />}
      </Drawer>
    </>
  );
};

export default ScenarioTaskTemplate;
