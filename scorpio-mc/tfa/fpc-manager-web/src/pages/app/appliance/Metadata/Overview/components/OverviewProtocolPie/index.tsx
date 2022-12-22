import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType } from '@/components/GlobalTimeSelector';
import type { ECOption } from '@/components/ReactECharts';
import ReactECharts from '@/components/ReactECharts';
import type { ConnectState } from '@/models/connect';
import usePieChartLabelColor from '@/utils/hooks/usePieChartLabelColor';
import { getLinkUrl, jumpNewPage } from '@/utils/utils';
import type { ECharts } from 'echarts';
import jsPDF from 'jspdf';
import numeral from 'numeral';
import { useContext, useEffect, useMemo, useRef } from 'react';
import { useParams, useSelector } from 'umi';
import { jumpToMetadataTab } from '../../../Analysis/constant';
import { EMetadataTabType } from '../../../Analysis/typings';
import { MetaDataContext } from '../../../Analysis';

interface Props {
  srcIp: string | undefined;
  data: any;
  demandShowText: { networkText: string | undefined; serviceText: string | undefined };
}

interface IPieData {
  result: { name: string; value: number }[];
  totalCount: number;
}

function OverviewProtocolPie(props: Props) {
  const { srcIp, data, demandShowText } = props;

  const totalCount = useMemo(() => {
    let total = 0;
    Object.values(data).forEach((v) => {
      total += parseInt(String(v));
    });
    return total;
  }, [data]);

  const chartInstanceRef = useRef<ECharts>();
  const pieDataRef = useRef<IPieData>();

  const [state, dispatch] = useContext(MetaDataContext);

  const { startTimestamp, endTimestamp } = useSelector<ConnectState, Required<IGlobalTime>>(
    (globalState) => globalState.appModel.globalSelectedTime,
  );

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

  const { pcapFileId } = useParams() as { pcapFileId: string };

  const labelColor = usePieChartLabelColor();

  const afterChartCreated = (chart: ECharts) => {
    chartInstanceRef.current = chart;
  };

  const pieData = useMemo(() => {
    let totalCount = 0;
    let result: IPieData['result'] = [];
    Object.keys(data).map((protocol) => {
      if (data[protocol] > 0) {
        totalCount += data[protocol];
        result.push({
          name: protocol,
          value: data[protocol],
        });
      }
      return true;
    });
    result = result.sort(
      (v1: { name: string; value: number }, v2: { name: string; value: number }) =>
        v1.value - v2.value,
    );
    const legendData = result.map((item) => item.name).reverse();
    return {
      totalCount,
      result,
      legendData,
    };
  }, [data]);

  useEffect(() => {
    pieDataRef.current = pieData;
  }, [pieData]);

  const exportPdf = () => {
    const doc = new jsPDF();

    // 获取ECharts导出的图片
    const imgData = chartInstanceRef.current?.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#fff',
    });
    // 添加图片
    if (imgData) {
      // 由于下面dom的宽高是：400，600
      // 为了保持比例，所以这里是w 120，h 180
      doc.addImage(imgData!, 'PNG', 10, 10, 180, 120);
    }
    // 添加表格
    doc.table(
      10,
      140,
      (pieDataRef.current?.result || [])
        .sort((a, b) => b.value - a.value)
        .map((row) => ({
          protocol: row.name,
          count: numeral(row.value).format('0,0'),
          percent: `${((row.value / (pieDataRef.current?.totalCount || 1)) * 100).toFixed(2)}%`,
        })),
      [
        { prompt: 'protocol', name: 'protocol', align: 'center', padding: 0, width: 70 },
        { prompt: 'count', name: 'count', align: 'center', padding: 0, width: 70 },
        { prompt: 'percent', name: 'percent', align: 'center', padding: 0, width: 70 },
      ],
      { fontSize: 12 },
    );
    // 导出
    // 直接 doc.save 在火狐浏览器上会覆盖当前页面直接打开
    // 为了兼容此情况，选择所有的浏览器都新开一个页面直接显示
    // 先满足测试需要吧，经过和测试人员沟通，先这样吧
    // 传递的 filename 不生效的，只会显示 blob 内容
    // @ts-ignore
    window.open(doc.output('bloburi', { filename: '应用层协议分析统计.pdf' }));
  };

  const TurnToOtherPage = (currentName: string) => {
    const filter = srcIp
      ? [
          {
            field: 'src_ipv4',
            operator: EFilterOperatorTypes.EQ,
            operand: srcIp,
          },
        ]
      : [];
    if (pcapFileId) {
      jumpToMetadataTab(state, dispatch, EMetadataTabType[currentName.toLocaleUpperCase()], filter);
    } else {
      jumpNewPage(
        getLinkUrl(
          `/analysis/trace/metadata/record?filter=${encodeURIComponent(
            JSON.stringify(filter),
          )}&jumpTabs=${currentName}&from=${startTimestamp}&to=${endTimestamp}&timeType=${
            ETimeType.CUSTOM
          }`,
        ),
      );
    }
  };

  const onClick = {
    click: (e: any) => {
      console.log(e);
      TurnToOtherPage(e.name);
    },
  };

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
          saveAsImage: {
            title: '保存为图片',
          },
          // https://echarts.apache.org/zh/option.html#toolbox.feature
          // 自定义的工具名字，只能以 my 开头
          mySaveAsPdf: {
            show: true,
            title: '保存为PDF',
            icon: 'M17.5,17.3H33 M17.5,17.3H33 M45.4,29.5h-28 M11.5,2v56H51V14.8L38.4,2H11.5z M38.4,2.2v12.7H51 M45.4,41.7h-28',
            onclick: () => {
              exportPdf();
            },
          },
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
              color: '#1890ff',
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
        data: pieData.legendData,
        type: 'scroll',
        orient: 'vertical',
        right: 10,
        top: 40,
        bottom: 20,
      },
    };
  }, [labelColor, pieData.legendData, pieData.result, pieData.totalCount, srcIpText]);

  return (
    <>
      <ReactECharts
        option={option}
        style={{ margin: 10, marginBottom: 20 }}
        opts={{ height: 460 }}
        onEvents={onClick}
      />
      <div style={{ display: 'none' }}>
        <ReactECharts
          option={{ ...option, toolbox: { show: false }, legend: { show: false } }}
          style={{ margin: 10, marginBottom: 20 }}
          opts={{ height: 460, width: 600 }}
          onChartReadyCallback={afterChartCreated}
        />
      </div>
    </>
  );
}

export default OverviewProtocolPie;
