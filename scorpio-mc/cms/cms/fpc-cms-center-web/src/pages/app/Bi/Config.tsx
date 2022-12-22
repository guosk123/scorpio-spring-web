const isDev = process.env.NODE_ENV === 'development';
const biUri = isDev
  ? `http://localhost:8000/embed/datasource`
  : `${window.location.origin}/bi/embed/datasource`;

export default () => {
  return (
    <>
      <iframe
        id="bi-iframe"
        height="100%"
        width="100%"
        frameBorder="0"
        allow="accelerometer; ambient-light-sensor; camera; encrypted-media; geolocation; gyroscope; hid; microphone; midi; payment; usb; vr; xr-spatial-tracking"
        sandbox="allow-forms allow-modals allow-popups allow-presentation allow-same-origin allow-scripts allow-downloads"
        src={biUri}
      />
    </>
  );
};
