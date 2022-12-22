import { Progress } from 'antd';
import SegmentCard from '../SegmentCard';

/** 分段分析连接器 */
export default function SegmentConnector({
  width,
  style = {},
}: {
  width?: string;
  style?: Record<string, any>;
}) {
  return (
    <>
      <SegmentCard width={width || '90px'} showBackground={false} bordered={false}>
        <Progress
          style={{ display: 'inline-block', width: '80px', ...style }}
          percent={100}
          showInfo={false}
        />
      </SegmentCard>
    </>
  );
}
