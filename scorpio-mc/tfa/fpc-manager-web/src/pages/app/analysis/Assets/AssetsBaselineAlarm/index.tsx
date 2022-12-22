import { useEffect, useRef, useState } from 'react';
import { getTablePaginationDefaultSettings, proTableSerchConfig } from '@/common/app';
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import type { IAjaxResponseFactory, IPageFactory, IProTableData } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'querystring';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
// import { ProTable } from '@ant-design/pro-components';
import ProTable from '@ant-design/pro-table';
import type { ISortParams } from '../typing';
import { EbaselineType, EbaselineTypeNameMap, ESortDirection } from '../typing';
import BaselineSettings from '../component/BaselineSettings';
import { Space } from 'antd';
import { useLocation } from 'umi';
import { history } from 'umi';
import { checkNonEssentialIPv4AndIPv6 } from '@/utils/utils';
import { getDeviceTypeLists, getOperateSystemTypeLists } from '../service';
import moment from 'moment';

type BaselineAlarmItem = {
  ipAddress: string;
  //设备类型
  type: EbaselineType;
  //基线状态
  baseline: string;
  //当前状态
  current: string;
  //告警时间
  alarmTime: string;
};

export const getSortDirection = (sortDirection: any) => {
  switch (sortDirection) {
    case 'ascend':
      return ESortDirection.ASC;
    case 'descend':
      return ESortDirection.DESC;
    default:
      return '';
  }
};

const AssetsBaselineAlarm = () => {
  const location = useLocation() as any as {
    query: { ipAddress: string; from: string; to: string };
  };
  const [initIpAddress] = useState(() => {
    if (location.query.ipAddress) {
      const tempIp = location.query.ipAddress;
      const queryObj = history.location.query || {};
      delete queryObj.ipAddress;
      history.replace({ query: queryObj });
      return tempIp;
    } else {
      return undefined;
    }
  });

  const [initTimeRange] = useState(() => {
    if (location.query.from && location.query.to) {
      const tempTimeRange = [
        moment(+location.query.from).format('YYYY-MM-DD HH:mm:ss'),
        moment(+location.query.to).format('YYYY-MM-DD HH:mm:ss'),
      ];
      const queryObj = history.location.query || {};
      delete queryObj.from;
      delete queryObj.to;
      history.replace({ query: queryObj });
      return tempTimeRange;
    } else {
      return [];
    }
  });

  const actionRef = useRef<ActionType>();

  const [deviceTypeMap, setDeviceTypeMap] = useState({});
  useEffect(() => {
    getDeviceTypeLists().then((res) => {
      const { success, result } = res;
      if (success) {
        const deviceMap = {};
        result.forEach((item: any) => {
          deviceMap[item.id] = item.device_name;
        });
        setDeviceTypeMap(deviceMap);
      }
    });
  }, []);
  const [osTypeMap, setOsTypeMap] = useState({});
  useEffect(() => {
    getOperateSystemTypeLists().then((res) => {
      const { success, result } = res;
      if (success) {
        const osMap = {};
        result.forEach((item: any) => {
          osMap[item.id] = item.os;
        });
        setOsTypeMap(osMap);
      }
    });
  }, []);

  const getShowedStatus = (type: EbaselineType, status: string) => {
    switch (type) {
      case EbaselineType.deviceTypeChange:
        return status.split(',').map((item) => deviceTypeMap[item]);
      case EbaselineType.operatingSystemChange:
        return status.split(',').map((item) => osTypeMap[item]);
      case EbaselineType.openPortChange:
        return status;
      case EbaselineType.assetsOfflineSenor:
        return status;
      case EbaselineType.bussinessLabelChange:
        return status;
      default:
        return '';
    }
  };

  const tableColumus: ProColumns<BaselineAlarmItem>[] = [
    {
      title: 'IP地址',
      dataIndex: 'ipAddress',
      // copyable: true,
      width: '10%',
      initialValue: initIpAddress,
      // search: {}
      formItemProps: { rules: [{ validator: checkNonEssentialIPv4AndIPv6 }] },
    },
    {
      title: '类型',
      dataIndex: 'type',

      // filters: true,
      // onFilter: true,
      valueType: 'select',
      valueEnum: EbaselineTypeNameMap,
      renderFormItem: (_, { defaultRender }) => {
        return defaultRender(_);
      },
      render: (_, record) => {
        if (record.type) {
          return <Space>{record.type.split(',').map((item) => EbaselineTypeNameMap[item])}</Space>;
        }
        return null;
      },
    },
    {
      title: '基线状态',
      dataIndex: 'baseline',
      hideInSearch: true,
      renderFormItem: (_, { defaultRender }) => {
        return defaultRender(_);
      },
      render: (_, record) => {
        if (record.type === EbaselineType.deviceTypeChange) {
          return <Space>{record.baseline.split(',').map((item) => deviceTypeMap[item])}</Space>;
        }
        return <Space>{record.baseline}</Space>;
      },
    },
    {
      title: '当前状态',
      dataIndex: 'current',
      hideInSearch: true,
      renderFormItem: (_, { defaultRender }) => {
        return defaultRender(_);
      },
      render: (_, record) => {
        const currentStatus = getShowedStatus(record.type, record.current);
        return <Space>{currentStatus}</Space>;
      },
    },
    {
      title: '告警时间',
      dataIndex: 'alarmTime',
      key: 'selectedAlarmTimeRange',
      valueType: 'dateTimeRange',
      sorter: true,
      defaultSortOrder: 'descend',
      // hideInSearch: true,
      initialValue: initTimeRange,
      search: {
        transform: (value) => {
          if (value.length === 0) {
            return {};
          }
          return {
            startTime: moment(value[0]).format(),
            endTime: moment(value[1]).format(),
          };
        },
      },
      render: (_, record) => {
        if (record.alarmTime) {
          return moment(record.alarmTime).format('YYYY-MM-DD HH:mm:ss');
        }
        return null;
      },
    },
    {
      title: '操作',
      key: 'option',
      // width: 120,
      valueType: 'option',
      render: (_, record) => {
        return (
          <BaselineSettings
            buttonType="link"
            buttonName="基线编辑"
            ipAddress={record.ipAddress}
            operationType="update"
            reloadList={() => actionRef?.current?.reload()}
          />
        );
      },
    },
  ];

  return (
    <>
      <ProTable<BaselineAlarmItem>
        bordered
        size="small"
        columns={tableColumus}
        // rowSelection={{}}
        scroll={{ x: 'max-content' }}
        request={async (params = {}, sort) => {
          const { current, pageSize, ...rest } = params;
          const [start, end] = rest?.selectedAlarmTimeRange ?? [];
          // console.log(start, 'start');
          const alarmTimeParams = {
            startTime: start ? moment(start).format() : undefined,
            endTime: end ? moment(end).format() : undefined,
          };
          console.log(alarmTimeParams, 'alarmTimeParams');
          delete rest?.selectedAlarmTimeRange;
          console.log(sort, 'sort');
          const sortProperty = Object.keys(sort)[0];
          const sortDirection = getSortDirection(sort[sortProperty]);
          const sortParams: ISortParams = {
            sortProperty: sortProperty,
            sortDirection: sortDirection,
          };

          const newParams = {
            pageSize,
            page: current! - 1,
            ...sortParams,
            ...alarmTimeParams,
            ...rest,
          };
          const { success, result } = (await ajax(
            `${API_VERSION_PRODUCT_V1}/metric/asset-alarm?${stringify(newParams)}`,
          )) as IAjaxResponseFactory<IPageFactory<BaselineAlarmItem>>;
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
          } as IProTableData<BaselineAlarmItem[]>;
        }}
        search={{
          ...proTableSerchConfig,
          span: 6,
          optionRender: (searchConfig, formProps, dom) => [
            ...dom.reverse(),
            // <Button key="primary" type="primary">
            //   设定基线
            // </Button>,
          ],
        }}
        actionRef={actionRef}
        sortDirections={['ascend', 'descend', 'ascend']}
        onReset={actionRef.current?.reload}
        pagination={getTablePaginationDefaultSettings()}
        dateFormatter="string"
        toolBarRender={false}
      />
    </>
  );
};

export default AssetsBaselineAlarm;
