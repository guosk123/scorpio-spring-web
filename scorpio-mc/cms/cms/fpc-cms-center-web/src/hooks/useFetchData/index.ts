import type { IAjaxResponseFactory } from '@/common/typings';
import { useSafeState } from 'ahooks';
import { useEffect } from 'react';

export default function useFetchData<T = any>(
  fetch: (...args: any[]) => Promise<IAjaxResponseFactory<T>>,
  params?: { args?: any },
) {
  const [data, setData] = useSafeState<T>();
  const [loading, setLoading] = useSafeState(false);

  useEffect(() => {
    setLoading(true);
    fetch(params?.args).then((res) => {
      setLoading(false);
      const { success, result } = res;
      if (success) {
        setData(result);
      }
    });
  }, [fetch, params?.args, setData, setLoading]);

  return {
    data,
    loading,
  };
}
