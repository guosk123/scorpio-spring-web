import {
  getTablePaginationDefaultSettings,
  proTableSerchConfig,
  PRO_TABLE_RESET_SPAN_SIZE,
} from '@/common/app';
import { ExportOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import { API_BASE_URL, API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import type { IAjaxResponseFactory, IPageFactory, IProTableData } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'querystring';
import ProTable from '@ant-design/pro-table';
import { Button, Dropdown, Menu, Space } from 'antd';
import BaselineSettings from '../component/BaselineSettings';
import DataExpireSettings from '../component/DataExpireSettings';
import type { ISortParams } from '../typing';
import { ESortDirection, EIsbaselineTypeMap } from '../typing';
import { useEffect, useMemo, useRef, useState } from 'react';
import { getDeviceTypeLists, getOperateSystemTypeLists, getTotalAssetsNumber } from '../service';
import { checkNonEssentialIPv4AndIPv6, getLinkUrl, jumpNewPage } from '@/utils/utils';
import { EFileType } from '@/pages/app/appliance/components/ExportFile';
import { v4 as uuidv4 } from 'uuid';
import moment from 'moment';
import type { ConnectState } from '@/models/connect';
import { useSelector } from 'umi';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType } from '@/components/GlobalTimeSelector';
import EllipsisDiv from '@/components/EllipsisDiv';
import { withSize } from 'react-sizeme';
import EllipsisCom from '@/components/EllipsisCom';

export interface AssetItem {
  ipKey?: string;
  ipAddress: string;
  deviceType: string;
  // 操作系统
  os: string;
  // 操作系统的类型
  osValue2: string;
  // 端口集合
  port: string;
  label: string;
  // 上报时间
  firstTime: string;
  //更新时间
  timestamp: string;
  // 基线个数
  alarm: number;
}

const searchItems = ['ipAddress', 'deviceType', 'os', 'port', 'alarm'];

const AssetsList = ({ size: { width } }: any) => {
  const actionRef = useRef<ActionType>();
  const globalSelectedTime = useSelector<ConnectState, Required<IGlobalTime>>(
    (state) => state.appModel.globalSelectedTime,
  );

  // console.log(globalSelectedTime, 'globalSelectedTime');
  const [deviceTypeMap, setDeviceTypeMap] = useState({});
  const [deviceTypeEnum, setDeviceTypeEnum] = useState({});
  useEffect(() => {
    getDeviceTypeLists().then((res) => {
      const { success, result } = res;
      if (success) {
        const deviceMap = {};
        const deviceEnum = {};
        result.forEach((item: any) => {
          deviceMap[item.id] = item.device_name;
          deviceEnum[item.id] = { text: item.device_name };
        });
        setDeviceTypeMap(deviceMap);
        setDeviceTypeEnum(deviceEnum);
      }
    });
  }, []);
  const [osTypeMap, setOsTypeMap] = useState({});
  const [osTypeEnum, setOsTypeEnum] = useState({});
  useEffect(() => {
    getOperateSystemTypeLists().then((res) => {
      const { success, result } = res;
      if (success) {
        const osMap = {},
          osEnum = {};
        result.forEach((item: any) => {
          osMap[item.id] = item.os;
          osEnum[item.id] = { text: item.os };
        });
        setOsTypeMap(osMap);
        setOsTypeEnum(osEnum);
      }
    });
  }, []);

  const validatePort = (rule: any, value: number, callback: (msg?: string) => void) => {
    if (value) {
      if (isNaN(value)) {
        callback('请输入正确的端口值');
      }
      if (value < 0 || value > 65535) {
        callback('请输入正确的范围内的端口值!');
      }
    }
    callback();
  };

  const tableColumus: ProColumns<AssetItem>[] = useMemo(() => {
    return [
      {
        title: 'IP地址',
        dataIndex: 'ipKey',
        search: false,
        hideInTable: true,
      },
      {
        title: 'IP地址',
        dataIndex: 'ipAddress',
        sorter: true,
        defaultSortOrder: 'descend',
        width: width * 0.1,
        formItemProps: { rules: [{ validator: checkNonEssentialIPv4AndIPv6 }] },
        // ignoreRules: false,
        renderFormItem: (_, { defaultRender }) => {
          return defaultRender(_);
        },
        render: (_, record) => {
          if (record.ipAddress) {
            return <EllipsisCom style={{ width: width * 0.1 }}>{record.ipAddress}</EllipsisCom>;
          }
          return null;
        },
      },
      {
        title: '设备类型',
        dataIndex: 'deviceType',
        // filters: true,
        // onFilter: true,
        valueType: 'select',
        valueEnum: deviceTypeEnum,
        renderFormItem: (_, { defaultRender }) => {
          return defaultRender(_);
        },
        render: (_, record) => {
          if (record.deviceType) {
            return (
              <EllipsisDiv style={{ minWidth: 60 }}>
                {record.deviceType
                  .split(',')
                  .map((item) => deviceTypeMap[item])
                  .join(',')}
              </EllipsisDiv>
            );
          }
          return null;
        },
      },
      {
        title: '操作系统',
        dataIndex: 'os',
        width: width * 0.2,
        valueType: 'select',
        valueEnum: osTypeEnum,
        renderFormItem: (_, { defaultRender }) => {
          return defaultRender(_);
        },
        render: (_, record) => {
          if (record.os) {
            // return <Space>{record.os.split(',').map((item) => osTypeMap[item])}{record.osValue2}</Space>;
            return <EllipsisDiv style={{ width: width * 0.2 }}>{record.os}</EllipsisDiv>;
            // return <EllipsisCom style={{ width: width * 0.15 }}>{record.os}</EllipsisCom>;
          }
          return null;
        },
      },
      {
        title: '开放端口',
        dataIndex: 'port',
        // valueType: 'digit',
        width: width * 0.15,
        ignoreRules: false,
        formItemProps: { rules: [{ validator: validatePort }] },
        // render: (_, record) => {
        //   return <EllipsisDiv style={{ maxWidth: 420 }}>{record.port}</EllipsisDiv>;
        // },
        render: (_, record) => {
          if (record.port) {
            return <EllipsisCom style={{ width: width * 0.15 }}>{record.port}</EllipsisCom>;
          }

          return null;
        },
        // renderFormItem: (_, { defaultRender }) => {
        //   return defaultRender(_);
        // },
      },
      {
        title: '业务标签',
        dataIndex: 'label',
        search: false,
        render: (text) => {
          return <EllipsisDiv style={{ width: 80 }}>{text}</EllipsisDiv>;
        },
        // renderFormItem: (_, { defaultRender }) => {
        //   return defaultRender(_);
        // },
      },
      {
        title: '上报时间',
        dataIndex: 'firstTime',
        key: 'selectedFirstTimeRange',
        valueType: 'dateTimeRange',
        sorter: true,
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
          if (record.firstTime) {
            return (
              <EllipsisDiv style={{ minWidth: 140 }}>
                {moment(record.firstTime).format('YYYY-MM-DD HH:mm:ss')}
              </EllipsisDiv>
            );
          }
          return null;
        },
      },
      {
        title: '最后更新时间',
        dataIndex: 'timestamp',
        valueType: 'dateTime',
        sorter: true,
        search: false,
        render: (text) => {
          return <EllipsisDiv style={{ minWidth: 100 }}>{text}</EllipsisDiv>;
        },
      },
      {
        title: '基线告警',
        dataIndex: 'alarm',
        valueType: 'select',
        valueEnum: EIsbaselineTypeMap,
        render: (_, record) => {
          if (record.alarm > 0) {
            return (
              <span
                className="link"
                onClick={() => {
                  // const nowTime = moment().format();
                  // console.log(nowTime, 'nowTime');
                  const url = getLinkUrl(
                    `/analysis/trace/assets/baselineAlarm?from=${moment(
                      record.firstTime,
                    ).valueOf()}&to=${moment(record.timestamp).valueOf()}&ipAddress=${
                      record.ipAddress
                    }`,
                  );
                  console.log(url, 'url');
                  jumpNewPage(url);
                }}
              >
                <EllipsisDiv style={{ minWidth: 100 }}>{record.alarm}</EllipsisDiv>
              </span>
            );
          }
          return record.alarm;
        },
      },
      {
        title: '操作',
        key: 'option',
        search: false,
        valueType: 'option',
        render: (_, record) => {
          return (
            <div>
              <a
                onClick={() => {
                  const lastUpdataTime = moment(record.timestamp);
                  // console.log(lastUpdataTime);
                  const minsLastUpdateTime = moment(record.timestamp).subtract(30, 'minute');
                  // console.log(minsLastUpdateTime);
                  const IpImageUrl = getLinkUrl(
                    `/analysis/trace/ip-image?from=${minsLastUpdateTime.valueOf()}&to=${lastUpdataTime.valueOf()}&timeType=${
                      ETimeType.CUSTOM
                    }&ipAddress=${record.ipAddress}`,
                  );
                  jumpNewPage(IpImageUrl);
                }}
              >
                画像
              </a>
              <a>
                <BaselineSettings
                  buttonType="link"
                  buttonName="设定基线"
                  ipAddress={record.ipAddress}
                  operationType="create"
                  reloadList={() => actionRef?.current?.reload()}
                />
              </a>
            </div>
          );
        },
      },
    ];
  }, [deviceTypeEnum, deviceTypeMap, osTypeEnum, width]);

  const [tSpan, setTSpan] = useState(window.innerWidth > PRO_TABLE_RESET_SPAN_SIZE ? 6 : 8);

  useEffect(() => {
    window.addEventListener('resize', () => {
      setTSpan(window.innerWidth > PRO_TABLE_RESET_SPAN_SIZE ? 6 : 8);
    });

    return () => {
      window.removeEventListener('resize', () => {});
    };
  }, []);

  const [exportParams, setExportParams] = useState({});

  const handleExport = (fileType: EFileType) => {
    const params = {
      fileType,
      ...exportParams,
      queryId: uuidv4(),
    };
    console.log(params);
    const url = `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/metric/asset-information/as-export?${stringify(
      params,
    )}`;
    window.location.href = url;
  };

  const beforeSubmitData = (submitdata: any) => {
    // console.log('submit', submitdata);
    const params = {};
    // console.log(searchItems, 'searchItems');
    searchItems.forEach((item: any) => {
      if (submitdata[item]) {
        params[item] = submitdata[item];
      }
    });
    // console.log(params, 'params');
    setExportParams(params);
    return submitdata;
  };

  return (
    <>
      <ProTable<AssetItem>
        bordered
        size="small"
        columns={tableColumus}
        rowKey={(record) => `${record.ipKey}`}
        rowSelection={{}}
        tableAlertRender={({ selectedRowKeys }) => (
          <Space size={24}>
            <span>已选 {selectedRowKeys.length} 项</span>
          </Space>
        )}
        scroll={{ x: 'max-content' }}
        beforeSearchSubmit={beforeSubmitData}
        tableAlertOptionRender={({ selectedRowKeys, selectedRows, onCleanSelected }) => {
          // console.log(selectedRowKeys, 'selectedRowKeys');
          // console.log(selectedRows, 'selectedRows');
          return (
            <Space size={16}>
              <BaselineSettings
                buttonType="link"
                buttonName="设定基线"
                ipAddress={
                  selectedRowKeys && selectedRowKeys.length > 0
                    ? selectedRowKeys
                        .map((item) => {
                          return (item + '').split('^')[0];
                        })
                        .join(',')
                    : ''
                }
                operationType="create"
                reloadList={() => {
                  actionRef?.current?.reload();
                  if (actionRef?.current?.clearSelected) {
                    actionRef.current.clearSelected();
                  }
                }}
              />
              <a onClick={onCleanSelected}>取消选择</a>
            </Space>
          );
        }}
        request={async (params = {}, sort) => {
          const { current, pageSize, ...rest } = params;
          // console.log(current, 'current', pageSize, 'pageSize');
          // const [start, end] = rest?.selectedFirstTimeRange ?? [];
          // const firstTimeParams = {
          //   startTime: start ? moment(start).format() : undefined,
          //   endTime: end ? moment(end).format() : undefined,
          // };
          // console.log(firstTimeParams, 'firstTimeParams');
          // delete rest?.selectedFirstTimeRange;
          const { ipAddress, timestamp, firstTime } = sort;
          let sortParams: ISortParams = {
            sortProperty: 'ipAddress',
            sortDirection: ESortDirection.DESC,
          };
          if (ipAddress) {
            sortParams = {
              sortProperty: 'ipAddress',
              sortDirection: ipAddress === 'ascend' ? ESortDirection.ASC : ESortDirection.DESC,
            };
          }
          if (timestamp) {
            sortParams = {
              sortProperty: 'timestamp',
              sortDirection: timestamp === 'ascend' ? ESortDirection.ASC : ESortDirection.DESC,
            };
          }
          if (firstTime) {
            sortParams = {
              sortProperty: 'firstTime',
              sortDirection: firstTime === 'ascend' ? ESortDirection.ASC : ESortDirection.DESC,
            };
          }
          const newParams = {
            pageSize,
            page: current! - 1,
            ...sortParams,
            // ...firstTimeParams,
            ...rest,
          };
          const getTotalNumber = await getTotalAssetsNumber(newParams);
          const { success, result } = (await ajax(
            `${API_VERSION_PRODUCT_V1}/metric/asset-information?${stringify(newParams)}`,
          )) as IAjaxResponseFactory<IPageFactory<AssetItem>>;

          // let totalNumber = 0;
          // let totalData: AssetItem[];

          // Promise.all([getTotalNumber, getAlldata]).then((results) => {
          //   totalNumber = results[0].success ? results[0].result.total : 0;
          //   totalData = results[1].success ? results[1].result.content : [];
          // });
          if (!success) {
            return {
              data: [],
              success,
            };
          }
          return {
            data: result.content.map((item) => ({
              ipKey: `${item.ipAddress}^${uuidv4()}`,
              ...item,
            })),
            success,
            page: result.number,
            total: getTotalNumber.result.total || 0,
          } as IProTableData<AssetItem[]>;
        }}
        search={{
          ...proTableSerchConfig,
          labelWidth: 'auto',
          span: tSpan,
          optionRender: (searchConfig, formProps, dom) => {
            return [
              ...dom.reverse(),
              <Dropdown
                overlay={
                  <Menu
                    onClick={(e) => {
                      handleExport(e.key as EFileType);
                    }}
                  >
                    <Menu.Item key={EFileType.CSV}>导出 CSV 文件</Menu.Item>
                    <Menu.Item key={EFileType.EXCEL}>导出 Excel 文件</Menu.Item>
                  </Menu>
                }
              >
                <Button key="export" icon={<ExportOutlined />}>
                  导出
                </Button>
              </Dropdown>,
              // <BaselineSettings buttonType="primary" buttonName="设定基线" />,
              <DataExpireSettings />,
            ];
          },
        }}
        pagination={getTablePaginationDefaultSettings()}
        dateFormatter="string"
        toolBarRender={false}
        sortDirections={['ascend', 'descend', 'ascend']}
        actionRef={actionRef}
        onReset={actionRef.current?.reload}
        // form={{
        //   ignoreRules: true,
        // }}
      />
    </>
  );
};

export default withSize({ refreshMode: 'debounce', refreshRate: 60 })(AssetsList);
