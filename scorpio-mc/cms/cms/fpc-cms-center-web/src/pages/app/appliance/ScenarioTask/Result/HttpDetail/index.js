import React, { memo } from 'react';
import moment from 'moment';
import HTTP from '@/pages/app/appliance/Metadata/HTTP';
import { ANALYSIS_RESULT_ID_PREFIX } from '../index';

const HttpDetail = memo(({ location }) => {
  const { analysisResultId, analysisStartTime, analysisEndTime } = location.query;
  return (
    <HTTP
      analysisResultId={`${ANALYSIS_RESULT_ID_PREFIX}${analysisResultId}`}
      startTime={moment(decodeURIComponent(analysisStartTime)).format()}
      endTime={moment(decodeURIComponent(analysisEndTime)).format()}
    />
  );
});

export default HttpDetail;
