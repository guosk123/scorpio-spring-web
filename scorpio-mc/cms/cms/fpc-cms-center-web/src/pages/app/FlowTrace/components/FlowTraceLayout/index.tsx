import type { ReactNode } from 'react';
import TimeRangeSlider from '@/components/TimeRangeSlider';
import { history } from 'umi';

interface Props {
  children: ReactNode;
}

export default function FlowTraceLayout(props: Props) {
  const { children } = props;

  const disableShowTime =
    history.location.pathname.includes('/flow-trace/packet-retrieval/task-list') ||
    history.location.pathname.includes('packet/analysis');

  return (
    <div style={{ height: '100%' }}>
      <TimeRangeSlider
        // showRelativeSelect={false}
        style={disableShowTime ? { display: 'none' } : {}}
      />
      {children}
    </div>
  );
}
