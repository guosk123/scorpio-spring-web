/** 卡片默认大小 */
export const CARD_WIDTH = 300;
export const CARD_HEIGHT = 300;

/** ---------------------------------------  配色相关 --------------------------------------- */
/** 颜色阀值 */
export const SEGMENT_THRESHOLD_SAME = 0;

export const SEGMENT_THRESHOLD_NORMAL = 5;

export const SEGMENT_THRESHOLD_WARNING = 30;

export enum ESegmentItemColor {
  'RED' = 'red',
  'GREEN' = 'green',
  'YELLOW' = 'yellow',
  'NULL' = 'null',
}

/** 分段红色值 */
export const SEGMENT_RED_COLOR = '#fff2f0';

/** 分段黄色值 */
export const SEGMENT_YELLOW_COLOR = '#fffbe6';

/** 分段绿色值 */
export const SEGMENT_GREEN_COLOR = '#f6ffed';

/** 分段暗黑红值 */
export const SEGMENT_DARK_RED_COLOR = '#610b00';

/** 分段暗黑黄值 */
export const SEGMENT_DARK_YELLOW_COLOR = '#614700';

/** 分段暗黑绿值 */
export const SEGMENT_DARK_GREEN_COLOR = '#092b00';

/** 分段绿色边框 */
export const SEGMENT_GREEN_BORDER_COLOR = '1px solid #b7eb8f';

/** 分段黄色边框 */
export const SEGMENT_YELLOW_BORDER_COLOR = '1px solid #ffe58f';

/** 分段红色边框 */
export const SEGMENT_RED_BORDER_COLOR = '1px solid #ffccc7';

/** 分段暗黑绿色边框 */
export const SEGMENT_DARK_GREEN_BORDER_COLOR = '';

/** 分段暗黑黄色边框 */
export const SEGMENT_DARK_YELLOW_BORDER_COLOR = '';

/** 分段暗黑红色边框 */
export const SEGMENT_DARK_RED_BORDER_COLOR = '';

/** 分段文字颜色 */
export const SEGMENT_FONT_COLOR = 'black';

/** 分段暗黑文字颜色 */
export const SEGMENT_DARK_FONT_COLOR = 'rgb(119, 119, 119)';

/** 分段卡片背景色 */
export const SEGMENT_CARD_COLOR = '#fafafa';

/** 分段卡片暗黑背景色 */
export const SEGMENT_DARK_CARD_COLOR = 'rgba(0,0,0,0)';

/** ---------------------------------------  单位相关 --------------------------------------- */
/** 分段分析单位
 *   增加单位之后，需要在 ./utils/index.ts/segmentUnitFormatter 方法中加入处理
 */
export enum ESegmentItemUnit {
  'RATE' = 'rate',
  'COUNT' = 'count',
  // 微秒
  'MICRO' = 'microsecond',
  // 毫秒
  'MILLI' = 'millisecond',
  // 字符串
  'STRING' = 'string',
}

// 阀值
export const SEGMENT_THRESHOLD_MAP = {
  [ESegmentItemUnit.RATE]: 0.5,
  [ESegmentItemUnit.COUNT]: 10,
  [ESegmentItemUnit.MICRO]: 10,
};

/**
 * 分段项目类型
 */
export interface ISegmentItemBaseType {
  unit: ESegmentItemUnit;
  label: string;
  index: string;
}

/** 处理好的分段项目类型 */
export interface ISegmentItemFormattedType extends ISegmentItemBaseType {
  color: ESegmentItemColor;
  value: string;
}

/** 分段数据
 * 处理好的一个分段卡片的数据
 */
export type SegmentFormattedData = Record<string, ISegmentItemFormattedType>;

/**
 * 分段源数据
 */
export type SegmentOriginData = Record<string, number>;

// 分段数量最大值
export const SEGMENT_ITEM_CNT = 6;
