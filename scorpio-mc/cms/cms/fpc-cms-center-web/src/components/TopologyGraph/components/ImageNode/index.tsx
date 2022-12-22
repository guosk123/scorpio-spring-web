import tmpIcon from '@/assets/icons/tmp.svg';
import type { ReactShape } from '@antv/x6-react-shape';
import React, { useEffect, useState } from 'react';
import styles from './index.less';

interface Props {
  node?: ReactShape;
}

const ImageNode: React.FC<Props> = (props) => {
  const { node } = props;
  const [icon, setIcon] = useState<string>();

  useEffect(() => {
    import(`@/assets/icons/${node?.data.iconName}.svg`)
      .then((ic) => {
        setIcon(ic.default);
      })
      .catch(() => {
        setIcon(tmpIcon);
      });
  }, [node?.data.iconName]);

  return (
    <div className={styles.nodeContainer}>
      <svg width={50} height={50}>
        {/*
         * fix: 火狐(Firefox)浏览器下拖动带有image的node表现异常的情况
         * @see https://github.com/antvis/X6/issues/831
         */}
        <image style={{ pointerEvents: 'none' }} xlinkHref={icon} width={50} height={50} />
      </svg>
      <span className={styles.title}>{node?.data?.title}</span>
    </div>
  );
};

export default ImageNode;
