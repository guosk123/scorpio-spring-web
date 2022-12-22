import numeral from 'numeral';
import * as saveSvgAsPng from 'save-svg-as-png';
import { getDvaApp, SAKnowledgeModelState } from 'umi';
import { EForceGraphIndex } from '../typings';

/**
 * @see https://api.highcharts.com.cn/highcharts#lang.numericSymbols
 * @see https://zh.wikipedia.org/wiki/%E5%9B%BD%E9%99%85%E5%8D%95%E4%BD%8D%E5%88%B6%E8%AF%8D%E5%A4%B4
 * @param value 格式化的数组
 * @returns number 返回格式化后的字符串
 */
export const formatNumber: (value: number) => string = (value: number) => {
  if (value === 0) return '0';
  const prefixs = ['', 'k', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y'];
  const i = Math.floor(Math.log(value) / Math.log(1000));
  return `${numeral((value / Math.pow(1000, i)).toFixed(2)).value()}${
    i >= 0 ? prefixs[i] : prefixs[0]
  }`;
};

/**
 * 格式化字节数
 * @param bytes Bytes
 * @param decimal 保留小数位数
 * @param unit 进制
 * @returns
 */
export function formatBytes(bytes: number, decimal = 3, unit: 1000 | 1024 = 1000) {
  if (bytes === 0) return '0KB';
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const i = Math.floor(Math.log(bytes) / Math.log(unit));
  return `${numeral((bytes / Math.pow(unit, i)).toFixed(decimal)).value()}${
    i >= 0 ? sizes[i] : sizes[0]
  }`;
}

export function exportPng(svgName: string) {
  saveSvgAsPng.saveSvgAsPng(document.getElementsByClassName(svgName)[0], 'connections.png');
}

/** 判断点是不是在区域内 */
function pointInArea(
  point: [number, number],
  minX: number,
  minY: number,
  maxX: number,
  maxY: number,
) {
  if (point[0] > minX && point[0] < maxX && point[1] > minY && point[1] < maxY) {
    return true;
  }
  return false;
}

/**
 * @param area 二维数组，区域位置
 * @param links 一维数组，所有的边
 * @returns 一维数组，在此区域内的边
 */
export function getLinksInArea(area: [[number, number], [number, number]], links: any[]) {
  // 结果集
  let result = [];

  // 获取区域 [minX,minY] -> [maxX,maxY]
  const minX = Math.min(...[area[0][0], area[1][0]]);
  const minY = Math.min(...[area[0][1], area[1][1]]);
  const maxX = Math.max(...[area[0][0], area[1][0]]);
  const maxY = Math.max(...[area[0][1], area[1][1]]);

  // 遍历，找在区域内的links
  result = links.filter((link: any) => {
    const { x, y } = link;
    //check source
    if (pointInArea([x, y], minX, minY, maxX, maxY)) {
      return true;
    }
    return false;
  });
  return result;
}

const originData = getDvaApp()._store.getState().SAKnowledgeModel;
const mapData = (originData as SAKnowledgeModelState).allApplicationMap;
export const formatApplications = (indexs: EForceGraphIndex[]) => {
  if (!indexs) {
    return [];
  }
  return indexs?.map((index) => mapData[index]?.name);
};

