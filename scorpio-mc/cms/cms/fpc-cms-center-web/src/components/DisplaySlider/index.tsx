import { Slider } from 'antd';
import type { SliderRangeProps, SliderSingleProps } from 'antd/lib/slider';
import styles from './index.less'

export default function DisplaySlider(props: SliderSingleProps | SliderRangeProps) {
  return (
    <div className={styles.display}>
      <Slider {...props} />
    </div>
  );
}
