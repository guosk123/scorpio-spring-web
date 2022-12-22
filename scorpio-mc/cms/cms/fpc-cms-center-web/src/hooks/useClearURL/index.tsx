import { useEffect } from 'react';
import { history } from 'umi';

export default function useClearURL() {
  const tmpURLQuery = history.location.query || {};

  useEffect(() => {
    const query = {};
    const URLQueryParams = ['from', 'to', 'relative', 'timeType'];
    URLQueryParams.forEach((item) => {
      if (tmpURLQuery[item]) {
        query[item] = tmpURLQuery[item];
      }
    });
    history.replace({
      query,
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
}
