import FieldFilter, { filterCondition2Spl } from '@/components/FieldFilter';
import type { IField, IFilter, IFilterCondition } from '@/components/FieldFilter/typings';
import { EFieldOperandType } from '@/components/FieldFilter/typings';
import TimeAxisChart from '@/components/TimeAxisChart';
import type { TimeAxisChartData } from '@/components/TimeAxisChart/typing';
import type { ConnectState } from '@/models/connect';
import { AlertFilled } from '@ant-design/icons';
import { useSafeState } from 'ahooks';
import { Card, Col, Divider, Row, Statistic } from 'antd';
import { snakeCase } from 'lodash';
import { useCallback, useEffect, useMemo, useState } from 'react';
import type { Dispatch, IGlobalSelectedTime } from 'umi';
import { useDispatch, useSelector } from 'umi';
import { querySuricataRules, querySuricataSource } from '../../Configuration/Suricata/service';
import { ERuleSignatureSeverity } from '../../Configuration/Suricata/typings';
import BarChart from '../components/BarChart';
import PieChart from '../components/PieChart';
import { queryAlertMessageHistogram, queryAlertMessageStatistics } from '../service';
import type {
  IMitreAttack,
  IRuleClasstype,
  SuricataHistogramType,
  SuricataStatisticsResult,
  SuricataStatisticsType,
} from '../typings';

export const CHART_HEIGHT = 300;

type StatType<T> = Partial<Record<SuricataStatisticsType | SuricataHistogramType, T>>;

const Dashboard = () => {
  // const {
  //   query: { filter },
  // } = useLocation() as unknown as { query: { filter?: string } };
  const [filterCondition, setFilterCondition] = useState<IFilter[]>([]);

  const [data, setData] = useState<StatType<SuricataStatisticsResult[]>>({});

  const [loadings, setLoadings] = useState<StatType<boolean>>({});

  const [topAlarmRule, setTopAlarmRule] = useState<{ key: string; count: number; label: string }[]>(
    [],
  );

  const [alarmTrendData, setAlarmTrendData] = useState<TimeAxisChartData[]>([]);

  // const { data: basicTags } = useFetchData<string[]>(queryAlertMessageTags);

  const dispatch = useDispatch<Dispatch>();

  const globalTime = useSelector<ConnectState, Required<IGlobalSelectedTime>>(
    (state) => state.appModel.globalSelectedTime,
  );

  const classtypeDict = useSelector<ConnectState, Record<string, IRuleClasstype>>(
    (state) => state.suricataModel.classtypeDict,
  );
  // console.log(classtypeDict, 'classtypeDict');
  const mitreDict = useSelector<ConnectState, Record<string, IMitreAttack>>(
    (state) => state.suricataModel.mitreDict,
  );

  const [sources, setSources] = useSafeState<Record<string, string>>({});

  const classtypes = useSelector<ConnectState, IRuleClasstype[]>(
    (state) => state.suricataModel.classtypes,
  );

  const searchFields: IField[] = useMemo(() => {
    return [
      {
        title: '规则分类',
        dataIndex: snakeCase('classtypeId'),
        operandType: EFieldOperandType.ENUM,
        enumValue: classtypes.map((item) => {
          return {
            value: item.id,
            text: item.name,
          };
        }),
      },
      {
        title: '来源',
        dataIndex: 'source',
        operandType: EFieldOperandType.ENUM,
        enumValue: Object.keys(sources).map((key) => {
          return {
            value: key,
            text: sources[key],
          };
        }),
      },
      {
        title: '严重级别',
        dataIndex: 'signature_severity',
        operandType: EFieldOperandType.ENUM,
        enumValue: Object.keys(ERuleSignatureSeverity).map((key) => {
          return {
            value: ERuleSignatureSeverity[key],
            text: key,
          };
        }),
      },
      // {
      //   title: '基础标签',
      //   dataIndex: 'basic_tag',
      //   operandType: EFieldOperandType.ENUM,
      //   enumValue: basicTags?.map((tag) => ({ value: tag, text: tag })),
      // },
    ];
  }, [classtypes, sources]);

  const handleFilterChange = (newFilter: IFilterCondition) => {
    setFilterCondition(newFilter as IFilter[]);
  };

  useEffect(() => {
    dispatch({
      type: 'suricataModel/querySuricataRuleClasstype',
      // payload: { globalTime.startTime, globalTime.endTime },
    });
    dispatch({
      type: 'suricataModel/querySuricataMitreAttack',
    });
    // querySuricataSource().then(({ success, result }) => {
    //   if (success) {
    //     setSources(result);
    //   }
    // });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const nextDsl = useMemo(() => {
    let dsl = ``;
    if (filterCondition.length > 0) {
      dsl += ` AND ` + filterCondition2Spl(filterCondition, searchFields);
    }
    dsl += ` | gentimes timestamp start="${globalTime.startTime}" end="${globalTime.endTime}"`;
    return dsl;
  }, [filterCondition, globalTime.endTime, globalTime.startTime, searchFields]);

  const querySevenCharts = useCallback(() => {
    const { interval } = globalTime;
    // 统计类型
    const statTypes: SuricataStatisticsType[] = [
      'top_target_host',
      'top_origin_ip',
      'classification_proportion',
      'top_alarm_id',
      'mitre_tactic_proportion',
      'source_alarm_trend',
      'basic_tag',
      'signature_severity',
    ];

    setLoadings({
      top_target_host: true,
      top_origin_ip: true,
      classification_proportion: true,
      top_alarm_id: true,
      mitre_tactic_proportion: true,
      source_alarm_trend: true,
      alarm_trend: true,
      signature_severity: true,
      basic_tag: true,
    });

    statTypes.map((type) => {
      queryAlertMessageStatistics({
        dsl: `type = ${type as SuricataStatisticsType}` + nextDsl,
      }).then((res) => {
        if (res.success) {
          setData((prev) => {
            return {
              ...prev,
              [type]: res.result,
            };
          });
          setLoadings((prev) => {
            return {
              ...prev,
              [type]: false,
            };
          });
        }
      });
    });

    queryAlertMessageHistogram({
      dsl: `type = ${'alarm_trend'}` + nextDsl,
      interval,
    }).then((res) => {
      if (res.success) {
        setAlarmTrendData(res?.result || []);
        setLoadings((prev) => {
          return {
            ...prev,
            alarm_trend: false,
          };
        });
      }
    });
  }, [globalTime, nextDsl]);

  useEffect(() => {
    querySevenCharts();
  }, [globalTime, querySevenCharts]);

  useEffect(() => {
    // 告警id转换规则名称
    if (data.top_alarm_id) {
      const sids = data.top_alarm_id.map((item) => item.key);
      querySuricataRules({ sid: sids.join(',') }).then((res) => {
        const { success, result } = res;
        if (success) {
          const prevAlarmId = data.top_alarm_id!;
          const nextAlarmId: { key: string; count: number; label: string }[] = prevAlarmId.map(
            (item) => {
              return {
                ...item,
                label:
                  result.content.find((rule) => rule.sid.toString() === item.key)?.msg || item.key,
              };
            },
          );
          setTopAlarmRule(nextAlarmId);
        }
      });
    }
    if (data.source_alarm_trend) {
      // console.log(data.source_alarm_trend, 'source_alarm_trend??');
      querySuricataSource().then(({ success, result }) => {
        if (success) {
          setSources(result);
        }
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [data.top_alarm_id, data.source_alarm_trend]);

  // 计算data.alarm_trend之和
  const totalAlarmCount: number = useMemo(() => {
    return (
      alarmTrendData?.reduce((total: number, curr: any) => {
        return total + curr.count;
      }, 0) || 0
    );
  }, [alarmTrendData]);

  return (
    <Card
      bordered={false}
      title={
        <Statistic
          value={totalAlarmCount}
          prefix={
            <>
              <AlertFilled />
              <span style={{ fontSize: 14 }}>告警总数:</span>
            </>
          }
        />
      }
    >
      <FieldFilter
        fields={searchFields}
        condition={filterCondition}
        onChange={handleFilterChange}
        historyStorageKey="suricata-alarm-filter"
      />
      <Divider dashed style={{ marginBottom: 10, marginTop: 5 }} />
      <Row gutter={[10, 10]} align="middle">
        <Col span={24}>
          <Card title="告警趋势" size="small">
            <TimeAxisChart
              startTime={globalTime.startTimestamp}
              endTime={globalTime.endTimestamp}
              interval={globalTime.interval}
              data={alarmTrendData}
              brush={true}
              chartHeight={CHART_HEIGHT}
              nameMap={{ count: '告警总数' }}
            />
          </Card>
        </Col>
        <Col span={12}>
          <BarChart
            menus={['alarm', 'surRule', 'stopRule', 'flow']}
            title="TOP10告警"
            chartType="top_alarm_id"
            loading={loadings.top_alarm_id}
            data={topAlarmRule || []}
          />
        </Col>
        <Col span={12}>
          <PieChart
            title="来源占比"
            data={data.source_alarm_trend || []}
            categoryMap={sources}
            loading={loadings.source_alarm_trend}
          />
        </Col>
        <Col span={12}>
          <PieChart
            title="分类占比"
            data={data.classification_proportion || []}
            categoryMap={Object.keys(classtypeDict).reduce((total, curr) => {
              return {
                ...total,
                [curr]: classtypeDict[curr].name,
              };
            }, {})}
            loading={loadings.classification_proportion}
          />
        </Col>
        <Col span={12}>
          <PieChart
            title="战术占比"
            data={data.mitre_tactic_proportion || []}
            categoryMap={Object.keys(mitreDict).reduce((total, curr) => {
              return {
                ...total,
                [curr]: mitreDict[curr].name,
              };
            }, {})}
            loading={loadings.mitre_tactic_proportion}
          />
        </Col>
        {/* <Col span={12}>
          <PieChart title="告警标签分布" data={data.basic_tag || []} loading={loadings.basic_tag} />
        </Col> */}
        <Col span={12}>
          <BarChart
            title="TOP10受害者"
            loading={loadings.top_target_host}
            data={data.top_target_host || []}
            chartType="top_target_host"
            menus={['flow', 'alarm']}
          />
        </Col>
        <Col span={12}>
          <BarChart
            title="TOP10攻击者"
            chartType="top_origin_ip"
            menus={['flow', 'alarm']}
            loading={loadings.top_origin_ip}
            data={data.top_origin_ip || []}
          />
        </Col>
        <Col span={12}>
          <PieChart
            title="告警严重级别"
            data={data.signature_severity || []}
            loading={loadings.signature_severity}
            categoryMap={Object.keys(ERuleSignatureSeverity).reduce((total, curr) => {
              return {
                ...total,
                [ERuleSignatureSeverity[curr]]: curr,
              };
            }, {})}
          />
        </Col>
      </Row>
    </Card>
  );
};

export default Dashboard;
