import { getTablePaginationDefaultSettings } from '@/common/app';
import type { ITableColumnProps } from '@/common/typings';
import CustomPagination from '@/components/CustomPagination';
import EnhancedTable from '@/components/EnhancedTable';
import type { IFilter } from '@/components/FieldFilter/typings';
import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType, globalTimeFormatText } from '@/components/GlobalTimeSelector';
import useRefresh from '@/hooks/useRefresh';
import type { ConnectState } from '@/models/connect';
import useFetchData from '@/utils/hooks/useFetchData';
import { jumpNewPage } from '@/utils/utils';
import { useLatest, useSafeState } from 'ahooks';
import { Button, Drawer, Space } from 'antd';
import moment from 'moment';
import { stringify } from 'qs';
import { useEffect, useMemo, useState } from 'react';
import type { GeolocationModelState } from 'umi';
import { useSelector } from 'umi';
import { getMetadataFilter } from '../../appliance/Metadata/components/Template/getMetadataFilter';
import type { EMetadataProtocol } from '../../appliance/Metadata/typings';
import { queryMailLoginRuleDetail } from '../../configuration/MailLoginRule/service';
import type { IMailLoginAlert } from '../typings';
import MailLoginRuleDetail from './MailLoginRuleDetail';
import { queryMailAlerts, queryMailAlertsTotal } from './service';

const MailLogin = () => {
  const [pagination, setPagination] = useState({
    currentPage: 0,
    pageSize: getTablePaginationDefaultSettings().pageSize || 20,
    total: 0,
  });

  const { allCityMap, allCountryMap, allProvinceMap } = useSelector<
    ConnectState,
    GeolocationModelState
  >((state) => state.geolocationModel);

  const [data, setData] = useSafeState<IMailLoginAlert[]>([]);
  const [dataLoading, setDataLoading] = useSafeState(false);
  const [totalLoading, setTotalLoading] = useSafeState(false);

  const [currnetRuleId, setCurrentRuleId] = useState<string>();

  const { data: ruleDetail, loading: ruleLoading } = useFetchData(
    queryMailLoginRuleDetail,
    { args: [currnetRuleId], condition: currnetRuleId !== undefined },
    [currnetRuleId],
  );

  const { count, refresh } = useRefresh();

  const globalSelectedTime = useSelector<ConnectState, Required<IGlobalTime>>(
    (state) => state.appModel.globalSelectedTime,
  );

  const timeParams = useMemo(() => {
    return {
      startTime: globalSelectedTime.originStartTime,
      endTime: globalSelectedTime.originEndTime,
    };
  }, [globalSelectedTime.originEndTime, globalSelectedTime.originStartTime]);

  const timeParamsRef = useLatest(timeParams);

  useEffect(() => {
    setDataLoading(true);
    queryMailAlerts({
      ...timeParams,
      pageNumber: pagination.currentPage,
      pageSize: pagination.pageSize,
    }).then((res) => {
      setDataLoading(false);
      const { success, result } = res;
      if (success) {
        const { content } = result;
        setData(
          content.map((item, index) => {
            return {
              ...item,
              index: pagination.currentPage * pagination.pageSize + index + 1,
            };
          }),
        );
      }
    });
  }, [pagination.currentPage, pagination.pageSize, setData, setDataLoading, timeParams, count]);

  useEffect(() => {
    setTotalLoading(true);
    queryMailAlertsTotal(timeParams).then((res) => {
      setTotalLoading(false);
      const { success, result } = res;
      if (success) {
        setPagination((prev) => {
          return {
            ...prev,
            total: result.total,
          };
        });
      }
    });
  }, [setTotalLoading, timeParams, count]);

  const columns: ITableColumnProps<IMailLoginAlert>[] = [
    {
      dataIndex: 'index',
      title: '#',
    },
    {
      dataIndex: 'timestamp',
      title: '时间',
      render: (dom, record) => {
        const { timestamp } = record;
        return moment(timestamp).format(globalTimeFormatText);
      },
    },
    {
      dataIndex: 'srcIp',
      title: '源IP',
      searchable: true,
      fieldType: EFieldType.IP,
      operandType: EFieldOperandType.IP,
    },
    {
      dataIndex: 'srcPort',
      title: '源端口',
      searchable: true,
      operandType: EFieldOperandType.PORT,
    },
    {
      dataIndex: 'destIp',
      title: '目的IP',
      searchable: true,
      fieldType: EFieldType.IP,
      operandType: EFieldOperandType.IP,
    },
    {
      dataIndex: 'destPort',
      title: '目的端口',
      searchable: true,
      operandType: EFieldOperandType.PORT,
    },
    {
      dataIndex: 'protocol',
      title: '协议',
      searchable: true,
      operandType: EFieldOperandType.ENUM,
      enumValue: [
        {
          text: 'POP3',
          value: 'pop3',
        },
        {
          text: 'IMAP',
          value: 'imap',
        },
        {
          text: 'SMTP',
          value: 'smtp',
        },
      ],
    },
    {
      dataIndex: 'mailAddress',
      title: '邮箱',
      searchable: true,
    },
    {
      dataIndex: 'countryId',
      title: '登录国家',
      render: (dom, record) => {
        const { countryId } = record;
        if (countryId) {
          return allCountryMap[countryId]?.nameText;
        }
        return '';
      },
    },
    {
      dataIndex: 'provinceId',
      title: '登录省份',
      render: (dom, record) => {
        const { provinceId } = record;
        if (provinceId) {
          return allProvinceMap[provinceId]?.nameText;
        }
        return '';
      },
    },
    {
      dataIndex: 'cityId',
      title: '登录城市',
      render: (dom, record) => {
        const { cityId } = record;
        if (cityId) {
          return allCityMap[cityId]?.nameText;
        }
        return '';
      },
    },
    {
      dataIndex: 'loginTimestamp',
      title: '登录时间',
      render: (dom, record) => {
        const { loginTimestamp } = record;
        return moment(loginTimestamp).format(globalTimeFormatText);
      },
    },
    {
      dataIndex: 'description',
      title: '描述',
    },
    {
      dataIndex: 'operation',
      title: '操作',
      fixed: 'right',
      width: 150,
      render(dom, record) {
        const { protocol, srcIp, srcPort, destIp, destPort, ruleId } = record;

        const filter: IFilter[] = [];
        if (protocol) {
          filter.push(
            ...getMetadataFilter({
              srcIp,
              destIp,
              srcPort,
              destPort,
              protocol: protocol.toLocaleLowerCase() as EMetadataProtocol,
            }),
          );
        }

        return (
          <Space>
            <span
              className={protocol ? 'link' : 'disabled'}
              onClick={() => {
                jumpNewPage(
                  `/analysis/trace/metadata/record?jumpTabs=${protocol?.toLocaleLowerCase()}&${stringify(
                    {
                      from: new Date(timeParamsRef.current.startTime).getTime(),
                      to: new Date(timeParamsRef.current.endTime).getTime(),
                      timeType: ETimeType.CUSTOM,
                    },
                  )}&filter=${encodeURIComponent(JSON.stringify(filter))}`,
                );
              }}
            >
              应用层协议详单
            </span>
            <span
              className={ruleId ? 'link' : 'disabled'}
              onClick={() => {
                setCurrentRuleId(ruleId);
              }}
            >
              详情
            </span>
          </Space>
        );
      },
    },
  ];

  const handlePageChange: Parameters<typeof CustomPagination>[0]['onChange'] = (current, size) => {
    setPagination((prev) => {
      return {
        ...prev,
        currentPage: current - 1,
        pageSize: size,
      };
    });
  };

  // const filterFields: IField[] = columns
  //   .filter((item) => item.searchable === true)
  //   .map((item) => {
  //     return {
  //       type: item.fieldType,
  //       operandType: item.operandType,
  //       dataIndex: item.dataIndex,
  //       title: item.title,
  //     };
  //   });

  return (
    <>
      <EnhancedTable
        tableKey={'security-mail-login'}
        columns={columns}
        autoHeight={true}
        loading={dataLoading}
        dataSource={data}
        pagination={false}
        extraTool={
          <Button onClick={refresh} style={{ float: 'right' }} type="primary">
            刷新
          </Button>
        }
        extraFooter={
          <CustomPagination
            loading={totalLoading}
            onChange={handlePageChange}
            currentPage={pagination.currentPage + 1}
            pageSize={pagination.pageSize}
            total={pagination.total}
          />
        }
        // extraTool={
        //   <FieldFilter
        //     condition={[]}
        //     fields={filterFields}
        //     historyStorageKey={'security-mail-login-filter'}
        //   />
        // }
      />
      <Drawer
        visible={currnetRuleId !== undefined}
        onClose={() => {
          setCurrentRuleId(undefined);
        }}
        title="规则详情"
        width={500}
      >
        <MailLoginRuleDetail detail={ruleDetail} loading={ruleLoading} />
      </Drawer>
    </>
  );
};

export default MailLogin;
