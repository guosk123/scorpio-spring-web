import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { useEffect, useMemo, useState } from 'react';
import { connect, useLocation, useSelector } from 'umi';
import { stringify } from 'qs';
import TimeRangeSlider from '@/components/TimeRangeSlider';
import useBiListener from '../hooks/useBiListener';
import type { Dispatch, TTheme } from 'umi';
import { changeURLStatic } from '../hooks/changeStaticUrl';
import qs from 'qs';

const isDev = process.env.NODE_ENV === 'development';
const biUri = isDev
  ? `http://localhost:8000/bi/embed/dashboard/tab?`
  : `${window.location.origin}/bi/embed/dashboard/tab?`;

const Index = ({ dispatch }: { dispatch: Dispatch }) => {
  const globalSelectedTime = useSelector<ConnectState, Required<IGlobalTime>>(
    (state) => state.appModel.globalSelectedTime,
  );
  const theme = useSelector<ConnectState, TTheme>((state) => state.settings.theme)
  const [params, setParams] = useState<Record<string, any>>({});
  const [dashboardTabId, setDashboardTabId] = useState<string>(() => {
    return (qs.parse(window.location?.hash?.split('?')[1]) || {})?.dashboardTabId as string || ''
  })

  useEffect(() => {
    setParams((prev) => ({
      ...prev,
      from: globalSelectedTime?.startTimestamp,
      to: globalSelectedTime?.endTimestamp,
    }));
  }, [globalSelectedTime?.endTimestamp, globalSelectedTime?.startTimestamp]);



  const embedUrl = useMemo(() => {
    return biUri + stringify(params) + (() => {
      if (dashboardTabId) {
        changeURLStatic('dashboardTabId', null)
        return `&dashboardTabId=${dashboardTabId}`
      }
      return ''
    })() + `&theme=${theme}`;
  }, [params, theme]);

  useBiListener({
    dispatch, callback: (p: any) => {
      if (p?.dashboardTabId) {
        setDashboardTabId(p?.dashboardTabId)
      }
    }
  });

  return (
    <>
      <div style={{ marginBottom: '10px' }}>
        <TimeRangeSlider />
      </div>
      <iframe
        id="bi-iframe"
        height="100%"
        width="100%"
        frameBorder="0"
        allow="accelerometer; ambient-light-sensor; camera; encrypted-media; geolocation; gyroscope; hid; microphone; midi; payment; usb; vr; xr-spatial-tracking"
        sandbox="allow-forms allow-modals allow-popups allow-presentation allow-same-origin allow-scripts allow-downloads"
        src={embedUrl || biUri}
      />
    </>
  );
};

export default connect()(Index);
