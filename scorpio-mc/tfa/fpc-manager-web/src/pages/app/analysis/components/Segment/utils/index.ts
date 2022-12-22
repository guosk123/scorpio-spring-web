import type { TTheme } from 'umi';
import type { ISegmentItemBaseType, SegmentFormattedData, SegmentOriginData } from '../typing';
import { SEGMENT_DARK_YELLOW_BORDER_COLOR, SEGMENT_YELLOW_BORDER_COLOR } from '../typing';
import { SEGMENT_DARK_YELLOW_COLOR, SEGMENT_YELLOW_COLOR } from '../typing';
import {
  SEGMENT_THRESHOLD_NORMAL,
  SEGMENT_THRESHOLD_SAME,
  SEGMENT_THRESHOLD_WARNING,
} from '../typing';
import {
  ESegmentItemColor,
  ESegmentItemUnit,
  SEGMENT_DARK_FONT_COLOR,
  SEGMENT_DARK_GREEN_BORDER_COLOR,
  SEGMENT_DARK_GREEN_COLOR,
  SEGMENT_DARK_RED_BORDER_COLOR,
  SEGMENT_DARK_RED_COLOR,
  SEGMENT_FONT_COLOR,
  SEGMENT_GREEN_BORDER_COLOR,
  SEGMENT_GREEN_COLOR,
  SEGMENT_ITEM_CNT,
  SEGMENT_RED_BORDER_COLOR,
  SEGMENT_RED_COLOR,
} from '../typing';

/** 获取颜色
 * @param value 需要判断的值
 * @param baseLine 基准值
 * @return ESegmentItemColor
 */
export const getColor = (value: number, baseLine: number): ESegmentItemColor => {
  const multi = value / (baseLine || 1) - 1;
  if (multi <= SEGMENT_THRESHOLD_SAME) {
    return ESegmentItemColor.NULL;
  } else if (multi <= SEGMENT_THRESHOLD_NORMAL) {
    return ESegmentItemColor.GREEN;
  } else if (multi <= SEGMENT_THRESHOLD_WARNING) {
    return ESegmentItemColor.YELLOW;
  } else {
    return ESegmentItemColor.RED;
  }
};

/** 单位formatter */
export const segmentUnitFormatter = (value: number, unit: ESegmentItemUnit) => {
  switch (unit) {
    case ESegmentItemUnit.COUNT:
      return `${value}`;
    case ESegmentItemUnit.RATE:
      return `${Math.round(value * 100)}%`;
    case ESegmentItemUnit.MICRO:
      return `${value / 1000}ms`;
    case ESegmentItemUnit.MILLI:
      return `${value}ms`;
    case ESegmentItemUnit.STRING:
      return `${value}`;
    default:
      return '';
  }
};

/** 获取分段背景颜色 */
export function getSegmentBackgroundColor(color: ESegmentItemColor, theme: TTheme) {
  switch (color) {
    case ESegmentItemColor.GREEN:
      if (theme === 'dark') {
        return SEGMENT_DARK_GREEN_COLOR;
      } else {
        return SEGMENT_GREEN_COLOR;
      }
    case ESegmentItemColor.RED:
      if (theme === 'dark') {
        return SEGMENT_DARK_RED_COLOR;
      } else {
        return SEGMENT_RED_COLOR;
      }
    case ESegmentItemColor.YELLOW:
      if (theme === 'dark') {
        return SEGMENT_DARK_YELLOW_COLOR;
      } else {
        return SEGMENT_YELLOW_COLOR;
      }
    default:
      return '';
  }
}

/** 获取分段边框颜色 */
export function getSegmentBorderColor(color: ESegmentItemColor, theme: TTheme) {
  switch (color) {
    case ESegmentItemColor.GREEN:
      if (theme === 'dark') {
        return SEGMENT_DARK_GREEN_BORDER_COLOR;
      } else {
        return SEGMENT_GREEN_BORDER_COLOR;
      }
    case ESegmentItemColor.RED:
      if (theme === 'dark') {
        return SEGMENT_DARK_RED_BORDER_COLOR;
      } else {
        return SEGMENT_RED_BORDER_COLOR;
      }
    case ESegmentItemColor.YELLOW:
      if (theme === 'dark') {
        return SEGMENT_DARK_YELLOW_BORDER_COLOR;
      } else {
        return SEGMENT_YELLOW_BORDER_COLOR;
      }
    default:
      return '';
  }
}

/** 获取分段文字颜色 */
export function getSegmentFontColor(theme: TTheme) {
  return theme === 'dark' ? SEGMENT_DARK_FONT_COLOR : SEGMENT_FONT_COLOR;
}

/** 格式化分段数据
 * @param segmentItemList 分段分析Item
 * @param segmentData 分段数据
 * @returns Record<string, SegmentFormattedData>
 */
export function formatSegmentData(
  segmentItemList: ISegmentItemBaseType[],
  segmentData: Record<string, SegmentOriginData>,
): Record<string, SegmentFormattedData> {
  const result = {};
  segmentItemList
    .slice(0, SEGMENT_ITEM_CNT > 0 ? SEGMENT_ITEM_CNT : 1)
    .forEach(({ index, label, unit }) => {
      const valueList = Object.keys(segmentData).map((key) => segmentData[key]?.[index] || 0);
      const baseLine = Math.min(...valueList);
      const networks = Object.keys(segmentData);
      if (networks.length == 1) {
        networks.forEach((key) => {
          const data: Record<string, number> = segmentData[key];
          result[key] = {
            ...(result[key] || {}),
            [index]: {
              label,
              unit,
              value: segmentUnitFormatter(data[index], unit),
            },
          };
        });
      } else {
        networks.forEach((key) => {
          const data: Record<string, number> = segmentData[key];
          result[key] = {
            ...(result[key] || {}),
            [index]: {
              label,
              unit,
              value: segmentUnitFormatter(data[index], unit),
              color: getColor(data[index], baseLine),
            },
          };
        });
      }
    });
  return result;
}
