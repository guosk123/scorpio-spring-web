import type { ConnectState } from '@/models/connect';
import { useMemo, useState } from 'react';
import type { TTheme } from 'umi';
import { useSelector } from 'umi';
import SegmentAnalysis from './components/SegmentAnalysis';
import SegmentCard from './components/SegmentCard';
import SegmentConnector from './components/SegmentConnector';
import SegmentContainer from './components/SegmentContainer';
import useNetworks from './hooks/useNetworks';
import type { ISegmentItemBaseType, ISegmentItemFormattedType, SegmentOriginData } from './typing';
import { ESegmentItemUnit } from './typing';
import { formatSegmentData } from './utils';

/** 这是一个DEMO　*/
export const SEGMENT_ITEM_LIST: ISegmentItemBaseType[] = [
  {
    label: '设备RTP丢包率',
    index: 'rtp_loss_rate',
    unit: ESegmentItemUnit.RATE,
  },
  {
    label: '设备RTP丢包数',
    index: 'rtp_loss_packets',
    unit: ESegmentItemUnit.COUNT,
  },
  {
    label: '最大抖动',
    index: 'jitter_max',
    unit: ESegmentItemUnit.MICRO,
  },
  {
    label: '平均抖动',
    index: 'jitter_mean',
    unit: ESegmentItemUnit.MICRO,
  },
  
];

/** 模拟数据 */
const segmentMockData: Record<string, SegmentOriginData> = {
  network1: {
    rtp_loss_packets: 122223123,
    rtp_loss_rate: 0.15,
    jitter_max: 32322,
    jitter_mean: 23123,
  },
};

export default function Demo() {
  const theme = useSelector<ConnectState, TTheme>((state) => state.settings.theme);
  const [selectedSegmentId, setSelectedSegmentId] = useState<string>('');

  const segmentDataObj = useMemo(() => {
    return formatSegmentData(SEGMENT_ITEM_LIST, segmentMockData);
  }, []);

  console.log(useNetworks('SWmbZ4IBV7whp27KtKK7'));

  return (
    <>
      {/* 使用　<SegmentContainer/> 包裹 该组件会自动判断屏幕是否占满，是否展示左右滚动箭头 */}
      <SegmentContainer>
        <SegmentCard theme={theme} footer={'设备'}>
          里面可以放任何东西
        </SegmentCard>
        <SegmentConnector />
        {Object.keys(segmentDataObj).map((key) => {
          return (
            <>
              <SegmentCard
                onFooterClick={(id) => {
                  setSelectedSegmentId(id);
                }}
                id={key}
                theme={theme}
                bordered={false}
                align={'top'}
                footer={key}
                selectedId={selectedSegmentId}
              >
                <SegmentAnalysis
                  id={key}
                  theme={theme}
                  dataSource={segmentDataObj[key]}
                  onClick={(id: string, item: ISegmentItemFormattedType) => {
                    console.log(id, item);
                    // TODO 跳转下钻
                  }}
                />
              </SegmentCard>
              {/* 分段连接器 */}
              <SegmentConnector />
            </>
          );
        })}
        <SegmentCard theme={theme} footer={'平台'}>
          里面可以放任何东西
        </SegmentCard>
      </SegmentContainer>
    </>
  );
}
