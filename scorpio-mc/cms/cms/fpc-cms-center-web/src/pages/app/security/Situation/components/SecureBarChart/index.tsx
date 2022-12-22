import type { ECOption } from '@/components/ReactECharts';
import ReactECharts, { CHART_COLORS } from '@/components/ReactECharts';

export default ({ datas, xAxisTitles }: { datas: number[]; xAxisTitles: string[] }) => {
  const option: ECOption = {
    xAxis: {
      type: 'category',
      data: xAxisTitles,
      axisLabel: {
        rotate: 70,
        interval: 0, // 标签设置为全部显示
        formatter(params: any) {
          let newParamsName = ''; // 最终拼接成的字符串
          const paramsNameNumber = params.length; // 实际标签的个数
          const provideNumber = 4; // 每行能显示的字的个数
          const rowNumber = Math.ceil(paramsNameNumber / provideNumber); // 换行的话，需要显示几行，向上取整

          // 条件等同于rowNumber>1
          if (paramsNameNumber > provideNumber) {
            for (let p = 0; p < rowNumber; p += 1) {
              let tempStr = ''; // 表示每一次截取的字符串
              const start = p * provideNumber; // 开始截取的位置
              const end = start + provideNumber; // 结束截取的位置
              // 此处特殊处理最后一行的索引值
              if (p === rowNumber - 1) {
                // 最后一次不换行
                tempStr = params.substring(start, paramsNameNumber);
              } else {
                // 每一次拼接字符串并换行
                tempStr = `${params.substring(start, end)}\n`;
              }
              newParamsName += tempStr; // 最终拼成的字符串
            }
          } else {
            // 将旧标签的值赋给新标签
            newParamsName = params;
          }
          // 将最终的字符串返回
          return newParamsName;
        },
      },
    },

    yAxis: {
      type: 'value',
    },
    series: [
      {
        data: datas.map((d, index) => {
          return {
            value: d,
            itemStyle: {
              color: CHART_COLORS[index % CHART_COLORS.length],
            },
          };
        }),
        name: '攻击次数',
        type: 'bar',
      },
    ],
  };
  return <ReactECharts option={option} style={{ height: '100%' }} />;
};
