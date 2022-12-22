import SegmentCard from '@/pages/app/analysis/components/Segment/components/SegmentCard';
import type {
  ISegmentItemFormattedType,
  SegmentFormattedData,
} from '@/pages/app/analysis/components/Segment/typing';
import SegmentAnalysis from '@/pages/app/analysis/components/Segment/components/SegmentAnalysis';
import type { TTheme } from 'umi';
import useNetwork from '@/pages/app/analysis/components/Segment/hooks/useNetworks';

export default function SegmentAnalysisWithNetwork({
  onFooterClick,
  networkId,
  theme,
  selectedId,
  data,
  onItemClick,
}: {
  onFooterClick?: (id: string) => void;
  onItemClick?: (id: string, item: ISegmentItemFormattedType) => void;
  networkId: string;
  theme: TTheme;
  selectedId: string;
  data: SegmentFormattedData;
}) {
  const network = useNetwork(networkId);

  return (
    <>
      <SegmentCard
        onFooterClick={onFooterClick}
        id={networkId}
        theme={theme}
        bordered={false}
        align={'top'}
        footer={network}
        selectedId={selectedId}
      >
        <SegmentAnalysis id={networkId} theme={theme} dataSource={data} onClick={onItemClick} />
      </SegmentCard>
    </>
  );
}
