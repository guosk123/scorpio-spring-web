import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import type { IAjaxResponseFactory, IPageFactory, IProTableData } from '@/common/typings';
import { getTablePaginationDefaultSettings, proTableSerchConfig } from '@/common/app';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'querystring';
// import { ProTable } from '@ant-design/pro-components';
import ProTable from '@ant-design/pro-table';
import type { ISortParams } from '../typing';
import { EbaselineTypeNameMap, EbaselineStatusType, EbaselineStatusTypeNameMap } from '../typing';
import { useEffect, useRef, useState } from 'react';
import { Button, message, Popconfirm } from 'antd';
import BaselineSettings from '../component/BaselineSettings';
import { deleteBaseline, getDeviceTypeLists } from '../service';
import { QuestionCircleOutlined } from '@ant-design/icons';
import { checkNonEssentialIPv4AndIPv6 } from '@/utils/utils';
import { getSortDirection } from '../AssetsBaselineAlarm';
import EllipsisDiv from '@/components/EllipsisDiv';

export interface BaselineItem {
  id: number;
  ipAddress: string;
  type: string[];
  baseline: string[];
  updateTime: string;
  description: string;
}

const BaselineList = () => {
  const [deviceTypeMap, setDeviceTypeMap] = useState({});
  // const [deviceTypeEnum, setDeviceTypeEnum] = useState({});
  useEffect(() => {
    getDeviceTypeLists().then((res) => {
      const { success, result } = res;
      if (success) {
        const deviceMap = {};
        // const deviceEnum = {};
        result.forEach((item: any) => {
          deviceMap[item.id] = item.device_name;
          // deviceEnum[item.id] = { text: item.device_name };
        });
        setDeviceTypeMap(deviceMap);
        // setDeviceTypeEnum(deviceEnum);
      }
    });
  }, []);

  const actionRef = useRef<ActionType>();

  const handleDeleteBaseline = async (ipAddress: string) => {
    const { success } = await deleteBaseline({ ipAddress: ipAddress });
    if (!success) {
      message.error('删除失败!');
      return;
    }
    message.success('删除成功!');
    actionRef?.current?.reload();
  };
  const tableColumus: ProColumns<BaselineItem>[] = [
    {
      title: 'IP地址',
      dataIndex: 'ipAddress',
      width: '10%',
      formItemProps: { rules: [{ validator: checkNonEssentialIPv4AndIPv6 }] },
    },
    {
      title: '类型',
      dataIndex: 'type',
      hideInSearch: true,
      renderFormItem: (_, { defaultRender }) => {
        return defaultRender(_);
      },
      render: (_, record) => (
        <EllipsisDiv>{record.type.map((item) => EbaselineTypeNameMap[item])}</EllipsisDiv>
      ),
    },
    {
      title: '基线状态',
      dataIndex: 'baseline',
      hideInSearch: true,
      renderFormItem: (_, { defaultRender }) => {
        return defaultRender(_);
      },
      width: 600,
      render: (_, record) => {
        if (record.baseline) {
          return (
            <EllipsisDiv style={{ maxWidth: 600 }}>
              {record.baseline.map((item) => {
                const itemKeys = Object.keys(item);
                return itemKeys.map((i: string) => {
                  if (i === EbaselineStatusType.deviceType) {
                    return `${EbaselineStatusTypeNameMap[EbaselineStatusType.deviceType]}: ${item[i]
                      .split(',')
                      .map((ele: string) => deviceTypeMap[ele])}`;
                  }
                  return `${EbaselineStatusTypeNameMap[i]}: ${item[i]}`;
                });
              })}
            </EllipsisDiv>
          );
        }
        return;
      },
    },
    {
      title: '配置时间',
      dataIndex: 'updateTime',
      valueType: 'dateTime',
      sorter: true,
      defaultSortOrder: 'descend',
      hideInSearch: true,
      render: (text) => <EllipsisDiv>{text}</EllipsisDiv>,
    },
    // {
    //   title: '描述',
    //   dataIndex: 'description',
    //   align: 'center',
    //   hideInSearch: true,
    // },
    {
      title: '操作',
      key: 'option',
      // width: 120,
      valueType: 'option',
      render: (_, record) => [
        <BaselineSettings
          buttonType="link"
          buttonName="编辑"
          operationType="update"
          ipAddress={record.ipAddress}
          type={record.type}
          reloadList={() => actionRef?.current?.reload()}
        />,
        <Popconfirm
          title="确定删除吗？"
          onConfirm={() => handleDeleteBaseline(record.ipAddress)}
          icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
        >
          <Button type="link" size="small">
            删除
          </Button>
        </Popconfirm>,
      ],
    },
  ];
  return (
    <>
      <ProTable<BaselineItem>
        bordered
        size="small"
        columns={tableColumus}
        // rowSelection={{}}
        scroll={{ x: 'max-content' }}
        request={async (params = {}, sort) => {
          console.log(sort, 'sort');
          const sortProperty = Object.keys(sort)[0];
          const sortDirection = getSortDirection(sort[sortProperty]);
          const sortParams: ISortParams = {
            sortProperty: sortProperty,
            sortDirection: sortDirection,
          };
          const { current, pageSize, ...rest } = params;
          const newParams = {
            pageSize,
            page: current! - 1,
            ...sortParams,
            ...rest,
          };
          const { success, result } = (await ajax(
            `${API_VERSION_PRODUCT_V1}/metric/asset-baseline?${stringify(newParams)}`,
          )) as IAjaxResponseFactory<IPageFactory<BaselineItem>>;
          if (!success) {
            return {
              data: [],
              success,
            };
          }

          return {
            data: result.content,
            success,
            page: result.number,
            total: result.totalElements,
          } as IProTableData<BaselineItem[]>;
        }}
        search={{
          ...proTableSerchConfig,
          span: 6,
          optionRender: (searchConfig, formProps, dom) => [...dom.reverse()],
        }}
        pagination={getTablePaginationDefaultSettings()}
        dateFormatter="string"
        toolBarRender={false}
        actionRef={actionRef}
        onReset={actionRef.current?.reload}
      />
    </>
  );
};

export default BaselineList;
