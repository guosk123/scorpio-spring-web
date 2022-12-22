import type { ECOption } from '@/components/ReactECharts';
import ReactECharts from '@/components/ReactECharts';
import type { ConnectState } from '@/models/connect';
import type { TTheme } from '@/models/frame/setting';
import { ETheme } from '@/models/frame/setting';
import { connect } from 'dva';
import numeral from 'numeral';
import { useMemo } from 'react';

interface Props {
  srcIp: string | undefined;
  data: any;
  theme: TTheme;
  demandShowText: { networkText: string | undefined; serviceText: string | undefined };
}

function OverviewProtocolPie(props: Props) {
  const { srcIp, data, theme, demandShowText } = props;

  const totalCount = useMemo(() => {
    let total = 0;
    Object.values(data).forEach((v) => {
      total += parseInt(String(v));
    });
    return total;
  }, [data]);

  const srcIpText = useMemo(() => {
    const { networkText, serviceText } = demandShowText;
    const resTexts = [];
    if (srcIp) {
      resTexts.push(`源IP: ${srcIp},`);
    }
    if (networkText) {
      resTexts.push(`网络: ${networkText}`);
    }
    if (serviceText) {
      resTexts.push(`业务: ${serviceText}`);
    }
    return resTexts.join(',');
  }, [demandShowText, srcIp]);

  const labelColor = useMemo(() => {
    if (theme === ETheme.light) {
      return '#222222';
    } else {
      return '#c8c8c8';
    }
  }, [theme]);

  const pieData = useMemo(() => {
    let tmpTotalCount = 0;
    const result: any = [];
    Object.keys(data).map((protocol) => {
      if (data[protocol] > 0) {
        tmpTotalCount += data[protocol];
        result.push({
          name: protocol,
          value: data[protocol],
        });
      }
      return true;
    });
    return {
      totalCount: tmpTotalCount,
      result,
    };
  }, [data]);

  const option = useMemo<ECOption>(() => {
    return {
      title: {
        text: '各协议数量占比',
        subtext: `${srcIpText} 事件总数量: ${numeral(pieData.totalCount).format('0,0')}`,
        left: 'center',
      },
      tooltip: {
        trigger: 'item',
        formatter: (params: any) => {
          const tooltipHtml = `${params.name}<br/>
              事件数量：${numeral(params.value).format('0,0')}个<br/>
              占比：${params.percent.toFixed(2)}%`;
          return tooltipHtml;
        },
      },
      toolbox: {
        show: true,
        feature: {
          saveAsImage: {},
          brush: {
            show: false,
          },
        },
      },
      xAxis: {
        show: false,
      },
      series: [
        {
          type: 'pie',
          radius: ['50%', '70%'],
          avoidLabelOverlap: true,
          label: {
            formatter: (params: any) => {
              const tmp = ((params.data.value / totalCount) * 100).toFixed(2);
              return `${params.name}: ${tmp || params.percent.toFixed(2)}%`;
            },
            show: true,
            color: labelColor,
            position: 'outer',
          },
          emphasis: {
            label: {
              show: true,
              color: labelColor,
              fontWeight: 'bold',
            },
          },
          labelLine: {
            show: true,
          },
          data: pieData.result,
        },
      ],
      legend: {
        type: 'scroll',
        orient: 'vertical',
        right: 10,
        top: 40,
        bottom: 20,
      },
    };
  }, [labelColor, pieData.result, pieData.totalCount, srcIpText]);

  return (
    <ReactECharts option={option} style={{ margin: 10, marginBottom: 20 }} opts={{ height: 460 }} />
  );
}

export default connect(({ settings }: ConnectState) => ({
  theme: settings.theme,
}))(OverviewProtocolPie);
