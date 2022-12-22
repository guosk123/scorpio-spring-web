import SSL from '@/pages/app/appliance/Metadata/SSL';
import moment from 'moment';
import React, { memo } from 'react';
import { ANALYSIS_RESULT_ID_PREFIX } from '../index';

const SSLDetail = memo(({ location }) => {
  const { analysisResultId, analysisStartTime, analysisEndTime } = location.query;
  return (
    <SSL
      analysisResultId={`${ANALYSIS_RESULT_ID_PREFIX}${analysisResultId}`}
      startTime={moment(decodeURIComponent(analysisStartTime)).format()}
      endTime={moment(decodeURIComponent(analysisEndTime)).format()}
    />
  );
});

export default SSLDetail;
