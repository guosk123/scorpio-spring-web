import SegmentCard from '../../../components/Segment/components/SegmentCard';
import server from '../assets/server.svg';
import monitor from '../assets/monitor.svg';
import { SEGMENT_ITEM_LIST } from './typing';
import type { ConnectState } from '@/models/connect';
import type { TTheme } from 'umi';
import { useSelector } from 'umi';
import SegmentConnector from '../../../components/Segment/components/SegmentConnector';
import { useMemo } from 'react';
import { formatSegmentData } from '../../../components/Segment/utils';
import SegmentContainer from '../../../components/Segment/components/SegmentContainer';
import type {
  ISegmentItemFormattedType,
  SegmentOriginData,
} from '../../../components/Segment/typing';
import SegmentAnalysisWithNetwork from './components/SegmentAnalysisWithNetwork';
import SegmentSpin from '../../../components/Segment/components/SegmentSpin';

export default function SegmentHeader({
  segmentData,
  loading = false,
  selectedSegmentId,
  onFooterClick,
  onItemClick,
}: {
  segmentData: Record<string, SegmentOriginData>;
  loading?: boolean;
  selectedSegmentId: string;
  onFooterClick: (id: string) => void;
  onItemClick?: (id: string, item: ISegmentItemFormattedType) => void;
}) {
  const theme = useSelector<ConnectState, TTheme>((state) => state.settings.theme);

  const segmentDataObj = useMemo(() => {
    return formatSegmentData(SEGMENT_ITEM_LIST, segmentData);
  }, [segmentData]);

  return (
    <>
      <SegmentContainer>
        <SegmentCard theme={theme} footer={'设备'}>
          <img style={{ width: 64, height: 64 }} src={monitor} color="white" />
        </SegmentCard>
        {loading ? (
          <SegmentSpin />
        ) : (
          <>
            <SegmentConnector />
            {Object.keys(segmentDataObj).map((key) => {
              return (
                <>
                  <SegmentAnalysisWithNetwork
                    onFooterClick={onFooterClick}
                    networkId={key}
                    theme={theme}
                    selectedId={selectedSegmentId}
                    data={segmentDataObj[key]}
                    onItemClick={onItemClick}
                  />
                  <SegmentConnector />
                </>
              );
            })}
          </>
        )}

        <SegmentCard theme={theme} footer={'平台'}>
          <img style={{ width: 64, height: 64 }} src={server} />
        </SegmentCard>
      </SegmentContainer>
    </>
  );
}
