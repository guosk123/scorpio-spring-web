import TimeRangeSlider from '@/components/TimeRangeSlider';
// import { useEffect } from 'react';

function IpImageLayout(props: any){
  const { children } = props;
  return (
    <div style={{ height: '100%' }}>
      <TimeRangeSlider/>
      {children}
    </div>
  );
}

export default IpImageLayout;