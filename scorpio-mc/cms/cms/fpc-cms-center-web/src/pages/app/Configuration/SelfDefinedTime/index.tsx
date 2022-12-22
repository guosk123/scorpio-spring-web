import { proTableSerchConfig } from '@/common/app';
import { getLinkUrl } from '@/utils/utils';
import { PlusOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import type { ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Button, message, Popconfirm, Space } from 'antd';
import moment from 'moment';
import { Fragment, useRef } from 'react';
import { history } from 'umi';
import { deleteSelfDefinedTime, deleteAllSelfdefinedTimes, queryAllSelfdefinedTime } from './services';
// import type { TimeConfigItem } from './typings';
import { time_Enum, ECustomTimeType } from './typings';

export default function SelfDefinedTime() {
  const actionRef = useRef<any>();
  const handleCreate = () => {
    history.push(getLinkUrl(`/configuration/objects/selfDefinedTime/create`));
  };

  const handleUpdate = (id: any) => {
    history.push(getLinkUrl(`/configuration/objects/selfDefinedTime/${id}/update`));
  };

  const handleDelete = async (deletedTimes: any[]) => {
    const sendedData: string = deletedTimes.length === 1 ? deletedTimes[0] : deletedTimes.join(',');
    const { success } =
      deletedTimes.length === 1
        ? await deleteSelfDefinedTime(sendedData)
        : await deleteAllSelfdefinedTimes(sendedData);
    if (!success) {
      message.error('删除失败!');
      return;
    }
    message.success('删除成功!');
    actionRef.current.reload();
    // location.reload();
  };
  const columns: ProColumns<any>[] = [
    {
      title: '名称',
      dataIndex: 'name',
      width: 300,
      align: 'center',
    },
    {
      title: '类型',
      dataIndex: 'type',
      align: 'center',
      width: 300,
      search: false,
      render: (dom, record) => {
        const { type } = record;
        return time_Enum[type];
      },
    },
    {
      title: '开始结束时间',
      dataIndex: 'customTimeSetting',
      valueType: 'dateTime',
      align: 'center',
      search: false,
      render: (text, record) => {
        //TODO： 2种时间，考虑好时间格式化
        const times = JSON.parse(record.customTimeSetting || '[]');
        if (record.type === ECustomTimeType.PeriodicTime) {
          return times
            .map((item: any) => {
              return `(${Object.values(item)})`;
            })
            .join('-');
        }
        if (record.type === ECustomTimeType.DisposableTime) {
          return times
            .map((item: any) => {
              const content = Object.values(item).map((ele: any) =>
                moment(ele).format('YYYY-MM-DDTHH:mm:ss'),
              );
              return content;
            })
            .join('-');
        }
        return '时间有误';
      },
    },
    {
      title: '操作',
      width: 160,
      align: 'center',
      search: false,
      dataIndex: 'action',
      render: (text, record) => {
        return (
          <Fragment>
            <Button type="link" size="small" onClick={() => handleUpdate(record.id)}>
              编辑
            </Button>
            <Popconfirm
              title="确定删除吗？"
              onConfirm={() => handleDelete([record.id])}
              icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
            >
              <Button type="link" size="small">
                删除
              </Button>
            </Popconfirm>
          </Fragment>
        );
      },
    },
  ];
  return (
    <>
      <ProTable
        rowKey="id"
        bordered
        size="small"
        columns={columns}
        rowSelection={
          {
            // 自定义选择项参考: https://ant.design/components/table-cn/#components-table-demo-row-selection-custom
            // 注释该行则默认不显示下拉选项
            // selections: [Table.SELECTION_ALL, Table.SELECTION_INVERT],
            // defaultSelectedRowKeys: [],
            // selectedRowKeys: [],
          }
        }
        tableAlertRender={({ selectedRowKeys }) => (
          <Space size={24}>
            <span>已选 {selectedRowKeys.length} 项</span>
          </Space>
        )}
        tableAlertOptionRender={({ selectedRowKeys, onCleanSelected }) => {
          return (
            <Space size={16}>
              <Popconfirm
                title="确定删除吗？"
                onConfirm={() => {
                  handleDelete(selectedRowKeys);
                  // TODO:
                  // selectedRowKeys = selectedRowKeys.splice(0, selectedRowKeys.length);
                }}
                icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
              >
                <a style={{ color: 'red' }}>批量删除</a>
              </Popconfirm>
              <a onClick={onCleanSelected}>取消选择</a>
            </Space>
          );
        }}
        actionRef={actionRef}
        search={{
          ...proTableSerchConfig,
          labelWidth: 'auto',
          span: 6,
          optionRender: (searchConfig, formProps, dom) => [
            ...dom.reverse(),
            <Button key="created" icon={<PlusOutlined />} type="primary" onClick={handleCreate}>
              新建
            </Button>,
          ],
        }}
        request={async () => {
          const { success, result } = await queryAllSelfdefinedTime();
          if (!success) {
            return [];
          }
          return {
            data: result,
            success: true,
          };
        }}
        toolBarRender={false}
      />
    </>
  );
}
