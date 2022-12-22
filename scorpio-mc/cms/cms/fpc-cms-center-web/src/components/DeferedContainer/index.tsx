import { debounce } from 'lodash';
import type { FC, PropsWithChildren } from 'react';
import { useEffect, useRef, useState } from 'react';

function checkVisibleInDocument(el: HTMLDivElement) {
  if (!(el.offsetHeight || el.offsetWidth || el.getClientRects().length)) return false;

  const { height, top } = el.getBoundingClientRect();
  const windowHeight = window.innerHeight || document.documentElement.clientHeight;
  return top < windowHeight && top + height > 0;
}

interface Props {
  visibleChange?: (visible: boolean) => void;
}

const DeferedContainer: FC<PropsWithChildren<Props>> = (props) => {
  const { children, visibleChange } = props;
  const [visible, setVisible] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const func = () => {
      if (containerRef.current) {
        const inView = checkVisibleInDocument(containerRef.current);
        setVisible(inView);
      }
    };
    const handleScroll = debounce(func, 100);
    handleScroll();
    window.addEventListener('scroll', handleScroll, false);
    return () => {
      window.removeEventListener('scroll', handleScroll);
    };
  }, []);

  useEffect(() => {
    if (visibleChange) {
      visibleChange(visible);
    }
  }, [visible, visibleChange]);

  return <div ref={containerRef}>{children}</div>;
};

export default DeferedContainer;
