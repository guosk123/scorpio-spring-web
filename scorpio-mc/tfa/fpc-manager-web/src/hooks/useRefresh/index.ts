import { useCallback, useState } from 'react';

export default function useRefresh() {
  const [count, setCount] = useState(0);

  const refresh = useCallback(() => {
    setCount((prev) => prev + 1);
  }, []);

  return {
    count,
    refresh,
  };
}
