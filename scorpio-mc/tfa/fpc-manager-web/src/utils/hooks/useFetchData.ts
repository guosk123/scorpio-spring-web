import type { IAjaxResponseFactory } from '@/common/typings';
import { useLatest, useSafeState } from 'ahooks';
import { throttle } from 'lodash';
import { useEffect } from 'react';

/**
 * @description 请不要在比较负载的逻辑中使用改hook, 尤其是参数超过两个以上，或者有特别复杂的控制请求发起的逻辑
 * @param fetch 请求方法
 * @param params args： 请求参数，codition: 控制是否进行请求，为undefined时，可以忽略这个配置，默认请求
 * @param deps 请求依赖，依赖变化是发起请求
 * @returns data: 接口数据，loading: 请求加载状态
 */
export default function useFetchData<T = any>(
  fetch: (...args: any[]) => Promise<IAjaxResponseFactory<T>>,
  params?: { args?: any; condition?: boolean },
  deps: any[] = [],
) {
  const request = useLatest(throttle(fetch, 500));
  const args = useLatest(params?.args);
  const [data, setData] = useSafeState<T>();
  const [loading, setLoading] = useSafeState(false);

  useEffect(() => {
    if (params?.condition === undefined || params.condition === true) {
      setLoading(true);
      request.current.apply(null, args.current)?.then((res) => {
        setLoading(false);
        const { success, result } = res;
        if (success) {
          setData(result);
        }
      });
    }
  }, [args, params?.condition, request, setData, setLoading, ...deps]);

  return {
    data,
    loading,
  };
}
