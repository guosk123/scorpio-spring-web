import { Progress } from 'antd';
import { useMemo } from 'react';

interface Props {
  progress: number;
  width?: number;
}

export default function StatusBox(props: Props) {
  const { progress, width = 150 } = props;
  const percent = useMemo(() => {
    return progress.toFixed(0);
  }, [progress]);
  const color = useMemo(() => {
    const perc = parseInt(percent);
    if (perc <= 80) {
      return '#52c41a';
    } else if (perc > 80 && perc <= 90) {
      return 'yellow';
    } else if (perc > 90) {
      return '#ff4d4f';
    }
  }, [percent]);
  return (
    <Progress percent={parseInt(percent)} size="small" style={{ width }} strokeColor={color} status="active"/>
  );
}
