import { useSafeState } from 'ahooks';
import { useCallback, useRef } from 'react';

export default function useQueryLoading(query: () => Promise<any>) {
  const queryRef = useRef(query);
  queryRef.current = query;
  const [loading, setLoading] = useSafeState(false);

  const resFn = useCallback(() => {
    setLoading(true);
    queryRef?.current().then(() => {
      setLoading(false);
    });
  }, [setLoading]);

  return [loading, resFn] as const;
}
