import React, { memo } from 'react';
import moment from 'moment';
import FTP from '@/pages/app/appliance/Metadata/FTP';
import { ANALYSIS_RESULT_ID_PREFIX } from '../index';

const FTPDetail = memo((props) => {
  const { location } = props;
  const { analysisResultId, analysisStartTime, analysisEndTime } = location.query;
  return (
    <FTP
      analysisResultId={`${ANALYSIS_RESULT_ID_PREFIX}${analysisResultId}`}
      startTime={moment(decodeURIComponent(analysisStartTime)).format()}
      endTime={moment(decodeURIComponent(analysisEndTime)).format()}
      {...props}
    />
  );
});

export default FTPDetail;
