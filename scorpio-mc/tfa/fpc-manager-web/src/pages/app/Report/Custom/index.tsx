import type { ConnectState } from '@/models/connect';
import type { TTheme} from 'umi';
import { useSelector } from 'umi';
import useBiListener from '../hooks/useBiListener';


const isDev = process.env.NODE_ENV === 'development';
const biUri = isDev
  ? `http://localhost:8000/bi/embed/report`
  : `${window.location.origin}/bi/embed/report`;

const Index = () => {
  useBiListener({});
  const theme = useSelector<ConnectState, TTheme>((state) => state.settings.theme)
  return (
    <>
      <iframe
        id="bi-iframe"
        height="100%"
        width="100%"
        frameBorder="0"
        allow="accelerometer; ambient-light-sensor; camera; encrypted-media; geolocation; gyroscope; hid; microphone; midi; payment; usb; vr; xr-spatial-tracking"
        sandbox="allow-forms allow-modals allow-popups allow-presentation allow-same-origin allow-scripts allow-downloads"
        src={biUri + `?theme=${theme}`}
      />
    </>
  );
};

export default Index;
