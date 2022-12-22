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
  style?: React.CSSProperties;
  className?: string;
}

const DeferedContainer: FC<PropsWithChildren<Props>> = (props) => {
  const { children, visibleChange, style, className } = props;
  const [visible, setVisible] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  // const isVisible = useIsInViewPort(containerRef);

  useEffect(() => {
    const windowScrll = () => {
      // console.log('window scroll')
      if (containerRef.current) {
        const inView = checkVisibleInDocument(containerRef.current);
        setVisible(inView);
      }
    };
    const handleScroll = debounce(windowScrll, 500);
    handleScroll();
    window.addEventListener('scroll', windowScrll);
    return () => {
      // console.log('scroll listener be removed')
      window.removeEventListener('scroll', windowScrll);
    };
  }, []);

  useEffect(() => {
    if (visibleChange) {
      visibleChange(visible);
    }
  }, [visible, visibleChange]);

  return (
    <div ref={containerRef} style={style} className={className}>
      {children}
    </div>
  );
};

export default DeferedContainer;
