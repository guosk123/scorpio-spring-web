import { useEffect, useRef, useState } from 'react';

/** 支持回调函数的useState */
export default function useCallbackState<T>(
  initialvalue: T,
): [data: T, func: (newData: T, callback?: ((data: T) => void) | undefined) => void] {
  /** 使用ref记录callback函数 */
  const funcRef = useRef<(data: T) => void>();
  /** 记录数据的state */
  const [data, setData] = useState<T>(initialvalue);

  /** 检测data变动，调用callback */
  useEffect(() => {
    if (funcRef.current) {
      funcRef.current(data);
    }
  }, [data]);

  return [
    data,
    function (newData: T, callback?: (data: T) => void) {
      if (callback) {
        /** 设置callback */
        funcRef.current = callback;
      }
      /** 改变data */
      setData(newData);
    },
  ];
}
