import { getTablePaginationDefaultSettings, proTableSerchConfig } from '@/common/app';
import {
  SYSTEM_COMPONENT,
  SYSTEM_LOG_CATEGORY,
  SYSTEM_LOG_CATEGORY_AUDIT,
  SYSTEM_LOG_LEVEL,
} from '@/common/dict';
import { queryLogs } from '@/services/frame/log';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Select, TreeSelect } from 'antd';
import moment from 'moment';
import { useEffect, useRef, useState } from 'react';
import { useAccess } from 'umi';
import { queryDeviceList } from '../../SSO/service';
import { serialListToTree } from '../../SSO/SingleSignSetting';
import { getSensorNameOnSensorTree } from '../Alerm';
import SelectComBar from '../Components/SelectComBar';
import type { ISystemLog } from './typings';

const SystemLog = () => {
  const access = useAccess();
  const actionRef = useRef<ActionType>();
  // let initComVal = '';
  // 根据权限计算用户可以看到的日志类型
  const systemLogCategory: typeof SYSTEM_LOG_CATEGORY = [];
  // 审计管理员只能查看审计日志
  if (access.hasAuditPerm) {
    systemLogCategory.push(
      ...SYSTEM_LOG_CATEGORY.filter((item) => item.key === SYSTEM_LOG_CATEGORY_AUDIT),
    );
  }
  // 系统管理员可以查看除审计日志外的其他类型
  if (access.hasAdminPerm) {
    systemLogCategory.push(
      ...SYSTEM_LOG_CATEGORY.filter((item) => item.key !== SYSTEM_LOG_CATEGORY_AUDIT),
    );
  }
  //配置管理员都可以看
  if (access.hasUserPerm) {
    systemLogCategory.push(
      ...SYSTEM_LOG_CATEGORY.filter((item) => item.key !== SYSTEM_LOG_CATEGORY_AUDIT),
    );
    // initComVal = '001001';
  }

  const [cmsSensorTree, setCmsSensorTree] = useState<any>();
  useEffect(() => {
    queryDeviceList().then((res) => {
      const { success, result } = res;
      if (success) {
        setCmsSensorTree(result);
      }
    });
  }, []);

  const columns: ProColumns<ISystemLog>[] = [
    {
      title: '日志级别',
      dataIndex: 'level',
      align: 'center',
      width: 80,
      valueType: 'select',
      renderFormItem: () => {
        return (
          <Select
            showSearch
            filterOption={(input, option) =>
              // @ts-ignore
              option?.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
            }
            placeholder="请选择"
            defaultValue={''}
          >
            <Select.Option key="" value="">
              全部
            </Select.Option>
            {SYSTEM_LOG_LEVEL.map((item) => (
              <Select.Option key={item.key} id={item.key} value={item.key}>
                {item.label}
              </Select.Option>
            ))}
          </Select>
        );
      },
    },
    {
      title: '日志类型',
      dataIndex: 'category',
      align: 'center',
      width: 80,
      valueType: 'select',
      renderFormItem: () => {
        return (
          <Select
            mode={!access.onlyAuditPerm ? 'multiple' : undefined}
            allowClear={!access.onlyAuditPerm}
            showSearch
            maxTagCount={2}
            filterOption={(input, option) =>
              // @ts-ignore
              option?.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
            }
            placeholder="请选择"
          >
            {systemLogCategory.map((item) => (
              <Select.Option key={item.key} id={item.key} value={item.key}>
                {item.label}
              </Select.Option>
            ))}
          </Select>
        );
      },
      search: {
        transform: (value) => ({
          category: Array.isArray(value) ? value.join(',') : value,
        }),
      },
    },
    {
      title: '组件',
      dataIndex: 'component',
      align: 'center',
      width: 120,
      valueType: 'select',
      // initialValue: initComVal,
      renderFormItem: (item, { type, defaultRender, ...rest }, form) => {
        if (type === 'form') {
          return null;
        }
        // if (access.hasUserPerm) {
        //   return <SelectComBar {...rest} state={{ type: 1 }} needOptions={SYSTEM_COMPONENT} />;
        // }
        return <SelectComBar {...rest} state={{ type: 2 }} needOptions={SYSTEM_COMPONENT} />;
      },
    },
    {
      title: '设备',
      dataIndex: 'nodeId',
      align: 'center',
      width: 120,
      ellipsis: true,
      valueType: 'select',
      renderFormItem: () => {
        const tmpTree = serialListToTree(cmsSensorTree);
        tmpTree.value = '';
        return <TreeSelect treeData={[tmpTree]} allowClear treeDefaultExpandAll showSearch />;
      },
      render: (text, record) => {
        const res: any = [];
        getSensorNameOnSensorTree(cmsSensorTree || {}, record.nodeId, res);
        return record.nodeId === '' ? '本机' : res[0];
      },
    },
    {
      title: '时间',
      dataIndex: 'ariseTime',
      align: 'center',
      width: 170,
      valueType: 'dateTimeRange',
      search: {
        transform: (value) => ({
          timeBegin: moment(value[0]).format(),
          timeEnd: moment(value[1]).format(),
        }),
      },
      render: (_, { ariseTime }) => ariseTime && moment(ariseTime).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '描述',
      dataIndex: 'content',
      ellipsis: true,
    },
    {
      title: '日志来源',
      dataIndex: 'source',
      ellipsis: true,
      width: 360,
    },
  ];

  return (
    <>
      <ProTable<ISystemLog>
        rowKey="id"
        bordered
        size="small"
        columns={columns}
        actionRef={actionRef}
        request={async (params = {}) => {
          const { current = 0, pageSize, ...rest } = params;
          const newParams = {
            pageSize,
            page: current && current - 1,
            ...rest,
          };
          const { success, result } = await queryLogs(newParams);
          return {
            data: result.content,
            page: result.number,
            total: result.totalElements,
            success,
          };
        }}
        search={{
          ...proTableSerchConfig,
        }}
        toolBarRender={false}
        pagination={getTablePaginationDefaultSettings()}
      />
    </>
  );
};

export default SystemLog;
