import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { camelCase, snakeCase } from '@/utils/utils';
import { v1 as uuidv1 } from 'uuid';
import { Card, message } from 'antd';
import { connect } from 'dva';
import moment from 'moment';
import { useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { queryDimensionsChart } from '../../../../service';
import DimensionsChart from './components/DimensionsChart';
import type { ISearchBoxInfo } from '../../../SearchBox';
import { EFormatterType, fieldFormatterFuncMap, fieldsMapping } from './constant';
import type { EDRILLDOWN } from '../../../../typing';
import { EDimensionsSearchType } from '../../../../typing';
import { DimensionsTypeToFlowFilterMap } from '../../../../typing';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { DimensionsSearchContext } from '../../../../SeartchTabs';
import { getTabDetail } from '@/pages/app/Network/components/EditTabs';
import { queryNetWorkTree } from '@/pages/app/Network/service';
import { getColumns } from '@/pages/app/Network/List/constant';
import { pingQueryTask } from '@/pages/app/appliance/FlowRecords/service';

/**
 * sourceType: network
 * networkId: a,b,c
  startTime: 2021-11-03T10:59:00+08:00
  endTime: 2021-11-03T11:29:00+08:00
  interval: 60
  sortProperty: downstream_bytes
  sortDirection: desc
  dsl:  " gentimes timestamp start="2021-11-03T10:59:00+08:00" end="2021-11-03T11:29:00+08:00"
  drilldown: 0
 */

const makeUpTimeInfo = (data: any, goTime: string, toTime: string, interval: number = 60) => {
  const startTime = moment(goTime).add(interval, 's').valueOf();
  const endTime = moment(toTime).add(interval, 's').valueOf();
  const array = data.map((item: any) => {
    return [moment(item[0]).valueOf(), item[1]];
  });
  let arrIndex = 0;
  let tmpTime: any = startTime;

  const res = [];

  while (moment(tmpTime).isBefore(endTime)) {
    if (tmpTime === array[arrIndex >= array.length ? 0 : arrIndex][0]) {
      res.push(array[arrIndex]);
      arrIndex += 1;
    } else {
      res.push([tmpTime, 0]);
    }
    tmpTime = moment(tmpTime).add(interval, 's').valueOf();
  }

  return res;
};

interface Props {
  selectedRow?: any;
  searchBoxInfo?: ISearchBoxInfo;
  sortProperty?: string;
  sortDirection?: any;
  cancelRow?: any;
  globalSelectedTime: IGlobalTime;
  drilldown?: EDRILLDOWN;
  chartPayload?: any;
}

function DimensionsLineChart(props: Props) {
  const { globalSelectedTime, chartPayload } = props;

  const [chartData, setChartData] = useState<any>([]);

  const [networkDataSet, setNetworkDataSet] = useState<any>([]);

  useEffect(() => {
    queryNetWorkTree()
      .then((result) => {
        setNetworkDataSet(result);
      })
      .catch((err) => {
        message.error(err);
      });
  }, []);
  const isByteToBandwidth = useMemo(() => {
    return (
      fieldsMapping[camelCase(chartPayload.sortProperty || '')]?.formatterType ===
      EFormatterType.BYTE
    );
  }, [chartPayload.sortProperty]);

  const dimensionsChartData = useMemo(() => {
    const tmp = {};
    chartData.forEach((item: any) => {
      const networkName = networkDataSet.find(
        (ele: any) => ele.value === (item?.networkGroupId || item?.networkId),
      )?.title;
      const sortDirectionName = getColumns(
        globalSelectedTime.startTime || '',
        globalSelectedTime.endTime || '',
      ).find((sub) => sub.dataIndex === camelCase(chartPayload.sortProperty || ''))?.title;
      const tooltipTitle = `${networkName} (${sortDirectionName})`;
      if (!tmp[tooltipTitle]) {
        tmp[tooltipTitle] = [];
      }
      tmp[tooltipTitle].push([item.timestamp, item[camelCase(chartPayload.sortProperty || '')]]);
    });

    const res = {};
    Object.keys(tmp).forEach((key) => {
      res[key] = makeUpTimeInfo(
        tmp[key],
        globalSelectedTime.startTime || '',
        globalSelectedTime.endTime || '',
        chartPayload.interval,
      );
      if (isByteToBandwidth) {
        res[key] = res[key].map((item: any) => {
          return [item[0], item[1] / (chartPayload.interval || 1)];
        });
      }
    });
    return res;
  }, [chartData, isByteToBandwidth, globalSelectedTime]);

  const [queryLoding, setQueryLoding] = useState(true);
  const [flowState, flowDispatch] = useContext(DimensionsSearchContext);
  const flowAnalysisDetail = getTabDetail(flowState);
  const [queryId, setQueryId] = useState(uuidv1());

  // ======维持查询心跳 S=====
  // ======维持查询心跳 S=====
  const pingQueryTaskFn = useCallback(() => {
    // 没有 ID 时不 ping
    if (queryId.length === 0) {
      return;
    }
    pingQueryTask({
      queryId,
    }).then((success: boolean) => {
      if (!success) {
        message.destroy();
      }
    });
  }, [queryId]);

  // ======维持查询心跳 E=====
  useEffect(() => {
    let timer: any;
    if (queryId.length > 0) {
      timer = window.setInterval(() => {
        pingQueryTaskFn();
      }, 3000);

      return () => window.clearTimeout(timer);
    }
    window.clearTimeout(timer);
    return undefined;
  }, [pingQueryTaskFn, queryId]);

  useEffect(() => {
    setQueryLoding(true);
    if (!chartPayload || JSON.stringify(chartPayload) === '{}') {
      return;
    }
    let dsl = undefined;

    let filterSpl = `${
      DimensionsTypeToFlowFilterMap[flowAnalysisDetail?.searchBoxInfo?.dimensionsSearchType]
    } ${EFilterOperatorTypes.EQ} ${flowAnalysisDetail?.searchBoxInfo?.content}`;
    if (
      flowAnalysisDetail?.searchBoxInfo?.dimensionsSearchType ===
      EDimensionsSearchType.IPCONVERSATION
    ) {
      const ips = flowAnalysisDetail?.searchBoxInfo?.content.split('-');
      filterSpl = `${
        DimensionsTypeToFlowFilterMap[flowAnalysisDetail?.searchBoxInfo?.dimensionsSearchType]
      } ${EFilterOperatorTypes.EQ} ${ips[0]} and ${
        DimensionsTypeToFlowFilterMap[flowAnalysisDetail?.searchBoxInfo?.dimensionsSearchType]
      } ${EFilterOperatorTypes.EQ} ${ips[1]}`;
    } else if (
      flowAnalysisDetail?.searchBoxInfo?.dimensionsSearchType === EDimensionsSearchType.LOCATION
    ) {
      const locationField = ['country_id', 'province_id', 'city_id'];
      const locationOperandArr = flowAnalysisDetail?.searchBoxInfo?.content.split('_');
      filterSpl = `${locationField[locationOperandArr.length - 1]} ${
        EFilterOperatorTypes.EQ
      } ${locationOperandArr.pop()}`;
    }

    dsl = `${filterSpl} | gentimes timestamp start="${globalSelectedTime.startTime}" end="${globalSelectedTime.endTime}"`;
    // 多维检索未同步dsl
    queryDimensionsChart({ ...chartPayload, dsl, drilldown: 1 }).then((res) => {
      const { success, result } = res;
      if (success) {
        setQueryLoding(false);
        setQueryId('');
        setChartData(result || []);
      }
    });
  }, [chartPayload, globalSelectedTime]);

  // 图表中的数据格式化方法
  const currentFormatter = useMemo(() => {
    // const valueType = fieldsMapping[sortProperty]?.formatterType;
    const valueType = fieldsMapping[camelCase(chartPayload.sortProperty || '')]?.formatterType;
    // 图中的字节数一律转换为bps
    const tmpValueType = valueType === EFormatterType.BYTE ? EFormatterType.BYTE_PS : valueType;
    return fieldFormatterFuncMap[tmpValueType];
  }, [chartPayload.sortProperty]);

  return (
    <div>
      <Card
        size="small"
        bodyStyle={{ padding: 0, paddingTop: '8px' }}
        style={{ padding: 0 }}
        // title={
        //   !selectRowIndex.value
        //     ? fieldsMapping[sortProperty]?.name
        //     : `${selectRowIndex.value}:${fieldsMapping[sortProperty]?.name}`
        // }
        // extra={
        //   selectRowIndex.value ? (
        //     <span onClick={cancelSelectedRow} style={{ cursor: 'pointer' }}>
        //       <CloseSquareOutlined />
        //     </span>
        //   ) : (
        //     <div />
        //   )
        // }
      >
        <DimensionsChart
          loading={queryLoding}
          data={dimensionsChartData || {}}
          unitConverter={currentFormatter}
          selectedTimeInfo={globalSelectedTime}
        />
      </Card>
    </div>
  );
}

export default connect((state: any) => {
  const {
    appModel: { globalSelectedTime },
  } = state;
  return { globalSelectedTime };
})(DimensionsLineChart);
