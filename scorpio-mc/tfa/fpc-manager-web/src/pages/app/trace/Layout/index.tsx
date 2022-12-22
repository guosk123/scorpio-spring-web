import TimeRangeSlider from '@/components/TimeRangeSlider';
import pathToRegexp from 'path-to-regexp';
import { history, useLocation } from 'umi';

const TraceLayout = (props: any) => {
  const { pathname } = useLocation();

  const hideTime =
    pathToRegexp('(/embed)?/analysis/trace/transmit-task(.*?)').test(pathname) ||
    pathToRegexp(`(/embed)?/analysis/trace/packet/analysis`).test(pathname) || 
    history.location.pathname.includes('/analysis/trace/assets/assetsList') ||
    history.location.pathname.includes('/analysis/trace/assets/baselineLis') ||
    history.location.pathname.includes('/analysis/trace/assets/baselineAlarm');

  return (
    <div>
      {!hideTime && <TimeRangeSlider />}
      {props.children}
    </div>
  );
};

export default TraceLayout;
