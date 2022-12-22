import Telnet from '@/pages/app/appliance/Metadata/Telnet';
import moment from 'moment';
import React, { memo } from 'react';
import { ANALYSIS_RESULT_ID_PREFIX } from '../index';

const TelnetDetail = memo(({ location }) => {
  const { analysisResultId, analysisStartTime, analysisEndTime } = location.query;
  return (
    <Telnet
      analysisResultId={`${ANALYSIS_RESULT_ID_PREFIX}${analysisResultId}`}
      startTime={moment(decodeURIComponent(analysisStartTime)).format()}
      endTime={moment(decodeURIComponent(analysisEndTime)).format()}
    />
  );
});

export default TelnetDetail;
