import { Button } from 'antd';

const websshUri = `https://${window.location.hostname}:41115`;

export default () => {
  return (
    <>
      <div style={{ textAlign: 'right', marginBottom: 10 }}>
        <Button type="primary" onClick={() => window.open(websshUri)}>
          新窗口打开
        </Button>
      </div>
      <iframe
        id="iframe"
        height="500px"
        width="100%"
        frameBorder="0"
        allow="accelerometer; ambient-light-sensor; camera; encrypted-media; geolocation; gyroscope; hid; microphone; midi; payment; usb; vr; xr-spatial-tracking"
        sandbox="allow-forms allow-modals allow-popups allow-presentation allow-same-origin allow-scripts allow-downloads"
        src={websshUri}
      />
    </>
  );
};
