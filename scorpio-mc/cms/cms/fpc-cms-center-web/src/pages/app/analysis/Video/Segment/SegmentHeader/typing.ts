import type { ISegmentItemBaseType } from '../../../components/Segment/typing';
import { ESegmentItemUnit } from '../../../components/Segment/typing';

export const SEGMENT_ITEM_LIST: ISegmentItemBaseType[] = [
  {
    label: '设备RTP丢包率',
    index: 'rtpLossPacketsRate',
    unit: ESegmentItemUnit.STRING,
  },
  {
    label: '设备RTP丢包数',
    index: 'rtpLossPackets',
    unit: ESegmentItemUnit.COUNT,
  },
  {
    label: '最大抖动',
    index: 'jitterMax',
    unit: ESegmentItemUnit.MICRO,
  },
  {
    label: '平均抖动',
    index: 'jitterMean',
    unit: ESegmentItemUnit.MICRO,
  },
];
