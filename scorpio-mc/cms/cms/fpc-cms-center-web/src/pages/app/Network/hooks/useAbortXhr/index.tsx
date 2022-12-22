import { abortAjax } from '@/utils/utils';
import { useEffect } from 'react';

interface Props {
  cancelUrls: string[];
}

export default function useAbortXhr(props: Props) {
  const { cancelUrls = [] } = props;
  useEffect(() => {
    return () => {
      abortAjax(cancelUrls);
    };
  }, []);
}
