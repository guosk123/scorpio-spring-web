import { abortAllQuery } from '@/utils/utils';
import { useEffect } from 'react';

export default function useCancelAllQuery() {
  useEffect(() => {
    return () => {
      abortAllQuery();
    };
  }, []);
}
