import { Card } from 'antd';
import { connect } from 'umi';
import AnalysisChart from '../../../components/AnalysisChart';
import type { ConnectState } from '@/models/connect';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { SEGMENT_ITEM_LIST } from '../SegmentHeader/typing';
import { segmentUnitFormatter } from '../../../components/Segment/utils';
import { ESegmentItemUnit } from '../../../components/Segment/typing';

type Time = string | number;
type Value = number;
type SerieName = string;
export type TrendChartData = [Time, Value][];

type Data = Record<SerieName, TrendChartData>;
export type SegmentTableData = Record<string, Data>;

interface ISegmenttableProps {
  globalSelectedTime: Required<IGlobalTime>;
  dataSources: SegmentTableData;
  loading?: boolean;
}

function SegmentTables({
  globalSelectedTime,
  dataSources = {},
  loading = false,
}: ISegmenttableProps) {
  return (
    <div style={{ width: '100%', height: '100%' }}>
      {SEGMENT_ITEM_LIST.map(({ index, label, unit }) => {
        const data = dataSources[index];
        if (!data) {
          return '';
        }
        return (
          <Card
            title={label}
            size="small"
            style={{ float: 'left', width: '49%', marginRight: '1%', marginTop: '10px' }}
            bodyStyle={{ padding: '5px' }}
          >
            <AnalysisChart
              data={data}
              loading={loading}
              unitConverter={(value: number) => {
                if (index === 'rtpLossPacketsRate') {
                  return segmentUnitFormatter(value, ESegmentItemUnit.RATE);
                }
                return segmentUnitFormatter(value, unit);
              }}
              selectedTimeInfo={globalSelectedTime}
            />
          </Card>
        );
      })}
    </div>
  );
}

const mapStateToProps = ({ appModel: { globalSelectedTime } }: ConnectState) => ({
  globalSelectedTime,
});

export default connect(mapStateToProps)(SegmentTables);
