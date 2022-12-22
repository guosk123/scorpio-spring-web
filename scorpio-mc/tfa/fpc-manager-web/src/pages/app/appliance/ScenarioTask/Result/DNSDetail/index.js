import React, { memo } from 'react';
import moment from 'moment';
import DNS from '@/pages/app/appliance/Metadata/DNS';
import { ANALYSIS_RESULT_ID_PREFIX } from '../index';

const DNSDetail = memo((props) => {
  const { location } = props;
  const { analysisResultId, analysisStartTime, analysisEndTime } = location.query;
  return (
    <DNS
      analysisResultId={`${ANALYSIS_RESULT_ID_PREFIX}${analysisResultId}`}
      startTime={moment(decodeURIComponent(analysisStartTime)).format()}
      endTime={moment(decodeURIComponent(analysisEndTime)).format()}
      {...props}
    />
  );
});

export default DNSDetail;
