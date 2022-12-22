import type { AppModelState } from '@/models/app/index';
import type { ConnectState } from '@/models/connect';
import type { IField, IFilter, IFilterGroup } from '@/components/FieldFilter/typings';
import type { IFlowHistParams } from '../../service';
import type { ProColumns } from '@ant-design/pro-table';
import type { IFlowIp, IFlowSession, IFlowPort, IUrlParams } from '../../typing';
import type { TrendChartData } from '@/pages/app/analysis/components/AnalysisChart';
import { getTablePaginationDefaultSettings } from '@/common/app';
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import { Card } from 'antd';
import { stringify } from 'qs';
import { useCallback, useEffect, useState, useMemo } from 'react';
import { connect, useParams } from 'umi';
import { EDrilldown, ESortDirection, tableRowInfos, ETableDataType } from '../../typing';
import { lineChartConverter, sessionFomatter } from '../../utils/converter';
import { bytesToSize, camelCase } from '@/utils/utils';
import { snakeCase } from '@/utils/utils';
import { getTimeInterval } from '../../utils/timeTool';
import { filterCondition2Spl } from '@/components/FieldFilter';
import { v1 as uuidv1 } from 'uuid';
import { completeTimePoint } from '@/pages/app/analysis/components/FlowAnalysis';
import { processingMinutes } from '../../utils/timeTool';
import { isIpv4 } from '../../utils/filterTools';
import { getFilterFields } from '../../Flow';
import AutoHeightContainer from '@/components/AutoHeightContainer';
import ajax from '@/utils/frame/ajax';
import ProTable from '@ant-design/pro-table';
import numeral from 'numeral';
import moment from 'moment';
import AnalysisChart from '@/pages/app/analysis/components/AnalysisChart';

interface IFlowTemplate extends AppModelState {
  tableColumns: ProColumns<any>[];
  pageName?: string;
  filterCondition: (IFilter | IFilterGroup)[];
  queryFunction: (params: IFlowHistParams) => any;
  initialRank: string;
  addConditionToFilter: any;
}

const Template: React.FC<IFlowTemplate> = ({
  // 过滤条件
  filterCondition,
  // 表格列
  tableColumns,
  // 页面名称
  pageName,
  globalSelectedTime,
  // 查询折线图函数
  queryFunction,
  // 初始化排序字段
  initialRank,
}) => {
  // 排序字段
  const [sortProperty, setSortProperty] = useState<string>(initialRank);
  const [sortDirection, setSortDirection] = useState<string>(ESortDirection.DESC);
  // 表格数据和图数据
  const [stackedChartData, setStackedChartData] = useState<Record<string, TrendChartData>>();
  const [tableData, setTableData] = useState<IFlowIp[]>([]);
  // 表格高度
  const [tableHeight, setTableHeight] = useState<number>(400);
  // 等待标志
  const [loading, setLoading] = useState<boolean>(false);
  const [histLoading, setHistLoading] = useState<boolean>(false);
  // 动态路由参数
  const urlParams = useParams<IUrlParams>();
  // 截断时间
  const cutStartTime = useMemo(() => {
    return processingMinutes(globalSelectedTime.originStartTime);
  }, [globalSelectedTime.originStartTime]);

  const cutEndTime = useMemo(() => {
    return processingMinutes(globalSelectedTime.originEndTime);
  }, [globalSelectedTime.originEndTime]);

  // 下钻标志
  const drilldown = useMemo(() => {
    return filterCondition.length > 0 ? '1' : '0';
  }, [filterCondition]);
  // dsl查询语句
  const dsl = useMemo(() => {
    if (filterCondition) {
      if (filterCondition.length !== 0) {
        return `${filterCondition2Spl(filterCondition, getFilterFields(), (f: IFilter) => {
          if (f.field === 'ip_address') {
            return `(ip_address<${isIpv4(`${f.operand}`) ? 'IPv4' : 'IPv6'}> ${f.operator} ${
              f.operand
            })`;
          }
          if (f.field === 'src_ip') {
            return `(src_ip<${isIpv4(`${f.operand}`) ? 'IPv4' : 'IPv6'}> ${f.operator} ${
              f.operand
            })`;
          }
          if (f.field === 'dest_ip') {
            return `(dest_ip<${isIpv4(`${f.operand}`) ? 'IPv4' : 'IPv6'}> ${f.operator} ${
              f.operand
            })`;
          }
          return `${f.field} ${f.operator} ${f.operand}`;
        })}| gentimes report_time start="${cutStartTime}" end="${cutEndTime}"`;
      }
    }
    return undefined;
  }, [filterCondition, cutStartTime, cutEndTime]);
  // 时间间隔
  const interval = useMemo(() => {
    return getTimeInterval(cutStartTime, cutEndTime);
  }, [cutEndTime, cutStartTime]);
  // 获取折线图数据
  const updateHist = useCallback(() => {
    setHistLoading(true);
    queryFunction({
      deviceName: urlParams.deviceName,
      netifNo: urlParams.netifNo,
      startTime: cutStartTime,
      endTime: cutEndTime,
      sortProperty: sortProperty ? snakeCase(sortProperty) : '',
      sortDirection,
      drilldown: filterCondition?.length !== 0 ? EDrilldown.drilldown : EDrilldown.undrilldown,
      dsl,
      interval,
    }).then((res: any) => {
      if (res.success) {
        const { result } = res;
        setStackedChartData(
          lineChartConverter(
            result,
            (() => {
              if (pageName === 'ip' || pageName === 'transmit-ip' || pageName === 'ingest-ip') {
                return 'ipAddress';
              }
              if (pageName === 'protocol-port') {
                return 'port';
              }
              if (pageName === 'session') {
                return 'sessionId';
              }
              return '';
            })(),
            sortProperty ? camelCase(sortProperty) : '',
            pageName === 'session' ? sessionFomatter : undefined,
          ),
        );
      }
      setHistLoading(false);
    });
  }, [
    queryFunction,
    urlParams.deviceName,
    urlParams.netifNo,
    cutStartTime,
    cutEndTime,
    sortProperty,
    sortDirection,
    filterCondition?.length,
    dsl,
    interval,
    pageName,
  ]);

  // 获取表格数据
  const fetchData = useCallback(() => {
    setLoading(true);
    ajax(
      `${API_VERSION_PRODUCT_V1}/metric/netflows/${pageName}?${stringify({
        startTime: cutStartTime,
        endTime: cutEndTime,
        deviceName: urlParams.deviceName,
        netifNo: urlParams.netifNo,
        sortProperty: sortProperty ? snakeCase(sortProperty) : '',
        sortDirection,
        dsl,
        drilldown,
      })}`,
    ).then((res) => {
      setLoading(false);
      if (!res.success) {
        return;
      }
      if (res.success) {
        setTableData(res.result);
      }
    });
  }, [
    pageName,
    cutStartTime,
    cutEndTime,
    urlParams.deviceName,
    urlParams.netifNo,
    sortProperty,
    sortDirection,
    dsl,
    drilldown,
  ]);

  // 单位格式化
  const unitConverter = (value: number) => {
    // 如果是字节类型
    if (tableRowInfos[sortProperty] && tableRowInfos[sortProperty].type === ETableDataType.BYTE) {
      if (value === undefined) {
        return bytesToSize(0);
      }
      return bytesToSize(parseInt(`${value}`, 10));
    }
    if (tableRowInfos[sortProperty] && tableRowInfos[sortProperty].type === ETableDataType.PACKET) {
      if (value === undefined) {
        return '0';
      }
      return `${numeral(parseInt(`${value}`, 10)).format('0,0')}`;
    }
    return '';
  };
  // 随着时间变动加载数据
  useEffect(() => {
    updateHist();
    fetchData();
  }, [fetchData, globalSelectedTime, updateHist, urlParams]);
  // 折线图数据补点
  const completeStackData = useMemo(() => {
    if (stackedChartData) {
      Object.keys(stackedChartData).forEach((key) => {
        stackedChartData[key] = completeTimePoint(
          stackedChartData[key],
          moment(moment(cutStartTime).add(5, 'minutes')).format(),
          cutEndTime,
          interval,
        );
      });
    }
    return stackedChartData;
  }, [cutEndTime, cutStartTime, interval, stackedChartData]);

  return (
    <>
      <Card
        key={sortProperty}
        title={tableRowInfos[sortProperty || ''] ? tableRowInfos[sortProperty || ''].title : ''}
        size="small"
        bodyStyle={{ padding: '5px' }}
      >
        <AnalysisChart
          data={completeStackData || {}}
          loading={histLoading}
          unitConverter={unitConverter}
          selectedTimeInfo={globalSelectedTime}
        />
      </Card>
      <AutoHeightContainer onHeightChange={(h) => setTableHeight(h - 100)}>
        <ProTable<IFlowIp | IFlowPort | IFlowSession>
          style={{ marginTop: 10 }}
          scroll={{ y: tableHeight }}
          rowKey={uuidv1()}
          bordered
          loading={loading}
          size="small"
          onChange={(_, filter, sorter) => {
            const { order, field } = sorter as { order: string; field: string };
            if (order === `${ESortDirection.ASC}end`) {
              setSortDirection(ESortDirection.ASC);
            } else if (order === `${ESortDirection.DESC}end`) {
              setSortDirection(ESortDirection.DESC);
            }
            if (field) {
              setSortProperty(field);
            }
          }}
          columns={tableColumns}
          dataSource={tableData}
          search={false}
          toolBarRender={false}
          form={{
            ignoreRules: false,
          }}
          dateFormatter="string"
          pagination={getTablePaginationDefaultSettings()}
        />
      </AutoHeightContainer>
    </>
  );
};

export default connect(({ appModel: { globalSelectedTime } }: ConnectState) => ({
  globalSelectedTime,
}))(Template);
