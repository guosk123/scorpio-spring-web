import { useEffect,useMemo } from 'react';
import { useLocation } from 'umi';
import { handleMsgFromparents } from './utils/handleMsgFromParent';

const isDev = process.env.NODE_ENV === 'development';
const biUri = isDev
  ? `http://localhost:8001/bi/embed/dashboard/tab`
  : `${window.location.origin}/bi/embed/dashboard/tab`;

const Index = () => {
  useEffect(() => {
    window.addEventListener('message', handleMsgFromparents, false);
  }, []);

  const location = useLocation()
  const embedUrl = useMemo(() => {
    if ((location as any)?.query?.id) {
      return biUri+`?id=${(location as any)?.query?.id}`
    }
    return undefined;
  }, [location]);

  return (
    <iframe
      id="bi-iframe"
      height="100%"
      width="100%"
      frameBorder="0"
      allow="accelerometer; ambient-light-sensor; camera; encrypted-media; geolocation; gyroscope; hid; microphone; midi; payment; usb; vr; xr-spatial-tracking"
      sandbox="allow-forms allow-modals allow-popups allow-presentation allow-same-origin allow-scripts allow-downloads"
      src={embedUrl||biUri}
    />
  );
};

export default Index;
