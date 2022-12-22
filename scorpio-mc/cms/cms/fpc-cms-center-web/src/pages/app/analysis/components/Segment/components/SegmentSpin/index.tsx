import { Spin } from 'antd';
import SegmentCard from '../SegmentCard';

/** 分段分析连接器 */
export default function SegmentSpin({
  width,
  style = {},
}: {
  width?: string;
  style?: Record<string, any>;
}) {
  return (
    <>
      <SegmentCard width={width || '90px'} showBackground={false} bordered={false}>
        <Spin style={{ ...style }} />
      </SegmentCard>
    </>
  );
}
