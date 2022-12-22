import * as echarts from 'echarts/core';
import ReactEcharts from 'echarts-for-react';
import chinaMapJson from '@/components/ReactECharts/assets/china.json';
// @ts-ignore
echarts.registerMap('china', chinaMapJson);

const option = {
  title: {
    x: 'center',
    textStyle: {
      color: '#9c0505',
    },
  },
  tooltip: {
    trigger: 'item',
    formatter: (params: any) => {
      console.log('params', params);
      return `
        <div>${params.name}</div>
        <div>${params.value}</div>
        `;
    },
  },  
  series: [
    {
      type: 'map',
      map: 'china',
      data: [{name: '北京市', value: 5000000}],
      label: {
        show: false,
        color: 'black',
        fontStyle: 10,
        align: 'center',
      },
      zoom: 1, // 当前缩放比例
      roam: true, // 是否支持拖拽
      itemStyle: {
        normal: {
          borderColor: 'rgba(0,63,140,0.2)',
          shadowColor: 'rgba(0,63,140,0.2)',
          // 地图板块的背景色
          areaColor: 'rgb(238 238 238)',
        },
        emphasis: {
          // 鼠标悬浮时的背景
          areaColor: 'rgb(238 238 238)',
        },
      },
      emphasis: {
        // 高亮显示
        label: {
          color: 'black',
          fontSize: 10,
        },
        itemStyle: {
          areaColor: 'rgba(0,63,140,0.8)', // 区域高亮颜色
        },
      },
    },
  ],
  visualMap: {
    left: 'right',
    min: 500000,
    max: 38000000,
    inRange: {
      color: [
        '#313695',
        '#4575b4',
        '#74add1',
        '#abd9e9',
        '#e0f3f8',
        '#ffffbf',
        '#fee090',
        '#fdae61',
        '#f46d43',
        '#d73027',
        '#a50026',
      ],
    },
    text: ['High', 'Low'],
    calculable: true,
  },
  toolbox: {
    show: false,
    //orient: 'vertical',
    left: 'left',
    top: 'top',
    feature: {
      dataView: { readOnly: false },
      restore: {},
      saveAsImage: {},
    },
  },
};

const StatisticsMapCN = () => {
  return <ReactEcharts echarts={echarts} option={option} opts={{ width: 800, height: 600 }} />;
};

export default StatisticsMapCN;
