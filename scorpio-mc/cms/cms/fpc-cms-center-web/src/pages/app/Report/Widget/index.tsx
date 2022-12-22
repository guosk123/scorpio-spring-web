import { useLocation } from 'umi';
import { useMemo } from 'react';

const isDev = process.env.NODE_ENV === 'development';
const biUri = isDev
  ? `http://localhost:8001/bi/embed/widget`
  : `${window.location.origin}/bi/embed/widget`;

const Index = () => {
  const location = useLocation();
  const embedUrl = useMemo(() => {
    if ((location as any)?.query?.embedUrl) {
      return isDev
        ? `http://localhost:8001/bi${(location as any)?.query?.embedUrl}`
        : `${window.location.origin}/bi${(location as any)?.query?.embedUrl}`;
    }
    return undefined;
  }, [location]);

  console.log(embedUrl)

  return (
    <iframe
      id="bi-iframe"
      height="100%"
      width="100%"
      frameBorder="0"
      allow="accelerometer; ambient-light-sensor; camera; encrypted-media; geolocation; gyroscope; hid; microphone; midi; payment; usb; vr; xr-spatial-tracking"
      sandbox="allow-forms allow-modals allow-popups allow-presentation allow-same-origin allow-scripts allow-downloads"
      src={embedUrl || biUri}
    />
  );
};

export default Index;
