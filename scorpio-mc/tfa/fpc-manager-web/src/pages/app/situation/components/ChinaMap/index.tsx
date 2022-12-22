import * as echarts from 'echarts/core';
import ReactEchartsCore from 'echarts-for-react/lib/core';
import chinaMapJson from '@/components/ReactECharts/assets/china.json';
// @ts-ignore
echarts.registerMap('china', chinaMapJson);

const option = {
  title: {
    left: 'center',
    textStyle: {
      color: '#fff',
    },
  },
  tooltip: {
    trigger: 'item',
    formatter: (params: any) => {
      if (params.componentSubType === 'effectScatter') {
        return `
        <div>资源状态：正常</div>
        <div>资源名称：${appConfig?.node_info?.name || '10.0.0.180'}</div>
        <div>资源IP：${appConfig?.node_info?.ip || '10.0.0.180'}</div>
        <div>资源类型：${appConfig?.node_info?.type || '探针'}</div>
        <div>型号：${appConfig?.node_info?.series || 'TC-32P'}</div>
        <div>所在区域：${appConfig?.node_info?.locationDetail || '北京市-北京市-海淀区'}</div>
        <div>告警数：0</div>
        `;
      }

      return '';
    },
  },
  geo: [
    {
      map: 'china',
      zoom: 1.2, // 当前视角的缩放比例
      scaleLimit: {
        // 控制滚轮缩放大小
        max: 1.3,
        min: 1,
      },
      label: {
        emphasis: {
          show: false,
        },
      },
      roam: false, // 是否开启平游或缩放
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
    },
  ],
  series: [
    {
      name: '设备位置',
      type: 'effectScatter',
      coordinateSystem: 'geo',
      data: [
        {
          name: appConfig?.node_info.locationName || '北京',
          value: appConfig?.node_info.locationCoordinate || [117.190182, 39.125596],
          itemStyle: {
            // 绿色太丑了！#1DE9B6
            color: '#00acea',
          },
        },
      ],
      showEffectOn: 'render',
      rippleEffect: {
        brushType: 'stroke',
      },
      hoverAnimation: true,
      label: {
        normal: {
          formatter: '{b}',
          position: 'right',
          // 是否显示地名
          show: true,
        },
      },
      itemStyle: {
        normal: {
          color: '#00acea',
          borderWidth: 1,
        },
      },
    },
  ],
};

const GeoMap = () => {
  return <ReactEchartsCore echarts={echarts} option={option} opts={{ height: 600 }} />;
};

export default GeoMap;
