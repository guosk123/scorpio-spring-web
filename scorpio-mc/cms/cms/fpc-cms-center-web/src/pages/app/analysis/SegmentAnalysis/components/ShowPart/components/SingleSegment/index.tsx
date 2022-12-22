import { Statistic } from 'antd';
import { useContext, useMemo } from 'react';
import { NetworkDetailNameMap } from '../../../../typings';
import { snakeCase } from '@/utils/utils';
import { TotalLinesContext } from '../..';
interface Props {
  titleId: string;
  value: number;
}

function SingleSegment(props: Props) {
  const { titleId, value } = props;
  const valueSuffix = useMemo(() => {
    const titles = snakeCase(titleId).split('_');
    const suffixId = titles.pop();
    if (suffixId === 'latency') {
      return 'ms';
    }
    if (suffixId === 'rate') {
      return '%';
    }
    return '';
  }, [titleId]);
  const boxStyleSetting = useMemo(() => {
    return { background: 'green' };
  }, []);
  // const boxClassName = useMemo(() => {}, []);
  const totalLines = useContext<any>(TotalLinesContext);

  const boxValueStyle = useMemo(() => {
    const fontsize = 35 - 5*(totalLines-1);
    return { fontSize: fontsize };
  }, [totalLines]);

  return (
    <div style={boxStyleSetting}>
      <Statistic
        title={NetworkDetailNameMap[titleId]}
        value={value}
        valueStyle={boxValueStyle}
        suffix={valueSuffix}
      />
    </div>
  );
}

export default SingleSegment;
