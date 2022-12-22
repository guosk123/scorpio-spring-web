import TimeRangeSlider from '@/components/TimeRangeSlider';
import { connect } from 'dva';
import { useEffect } from 'react';

function SegmentAnalysisLayout(props: any) {
  const { dispatch } = props;
  useEffect(() => {
    dispatch({
      type: 'networkModel/queryAllNetworkSensor',
      payload: {},
    });
  }, [dispatch]);
  const { children } = props;
  return (
    <div style={{ height: '100%' }}>
      <TimeRangeSlider showRelativeSelect={false} />
      {children}
    </div>
  );
}
export default connect()(SegmentAnalysisLayout);
