import ReactECharts from '@/components/ReactECharts';
import numeral from 'numeral';
import React, { PureComponent } from 'react';

class ProtocolPie extends PureComponent {
  render() {
    const { srcIp, data } = this.props;

    let totalCount = 0;
    const result = [];
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

    const option = {
      title: {
        text: '各协议数量占比',
        subtext: `${srcIp ? `源IP: ${srcIp},` : ''} 事件总数量: ${numeral(totalCount).format(
          '0,0',
        )}`,
        left: 'center',
      },
      tooltip: {
        trigger: 'item',
        formatter: (params) => {
          const tooltipHtml = `
          <div>
            <b>${params.name}</b>
            <br />
            <span>
              事件数量：<b>${numeral(params.value).format('0,0')}个</b>
              <br />
            </span>
            <span>
              占比：<b>${params.percent.toFixed(2)}%</b>
            </span>
          </div>`;
          return tooltipHtml;
        },
      },
      toolbox: {
        show: true,
        feature: {
          saveAsImage: {
            // type: 'svg'
          },
          brush: {
            show: false,
          },
        },
      },
      series: [
        {
          type: 'pie',
          radius: ['50%', '70%'],
          avoidLabelOverlap: false,
          label: {
            show: false,
            position: 'center',
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '30',
              fontWeight: 'bold',
            },
          },
          labelLine: {
            show: false,
          },
          data: result,
        },
      ],
    };

    return <ReactECharts option={option} style={{ marginTop: 10 }} opts={{ height: 350 }} />;
  }
}

export default ProtocolPie;
