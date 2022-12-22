// import { scaleLinear } from 'd3';
import type { INetworkSegmentDetails } from '../../../../typings';
import { NetworkDetailNameMap } from '../../../../typings';

interface Props {
  networkDetailItemId: string;
  networkDetail: INetworkSegmentDetails;
  networkIndex: number;
}

// const getColor = (value: number, valueRange: number[]) => {
//   const [minValue, maxValue] = valueRange;
//   const typicalColor: any = scaleLinear([-1, 0, 1], ['green', 'yellowGreen', 'red']);
//   const midValue = +(minValue + (maxValue - minValue) / 2).toFixed(2);
//   let ratio = 0;
//   if (value < midValue) {
//     ratio = -((midValue - value) / (midValue - minValue)).toFixed(2);
//   }
//   if (value > midValue) {
//     ratio = +((value - midValue) / (maxValue - midValue)).toFixed(2);
//   }
//   if (value == midValue) {
//     ratio = 0;
//   }
//   console.log(ratio, 'ratio');
//   const newColor = typicalColor(ratio);
//   return newColor;
// };

export default function DesItem(props: Props) {
  const { networkDetailItemId } = props;

  return (
    <>
      {/* {networkIndex === 0 ? (
        <span
          style={{
            display: 'inline',
            fontWeight: 'bold',
            lineHeight: 1,
            textAlign: 'center',
            whiteSpace: 'nowrap',
            verticalAlign: 'baseline',
            color: 'black',
          }}
        >
          {NetworkDetailNameMap[networkDetailItemId]}
        </span>
      ) : (
        <span
          style={{
            display: 'inline',
            padding: '.2em .6em .3em',
            fontWeight: 'bold',
            // fontSize: '75%',
            lineHeight: 1,
            textAlign: 'center',
            whiteSpace: 'nowrap',
            verticalAlign: 'baseline',
            backgroundColor: getColor(
              networkDetail[networkDetailItemId],
              labelColorValueRangeMap[networkDetailItemId],
            ),
            color: '#fff',
            borderRadius: '.25em',
          }}
        >
          {NetworkDetailNameMap[networkDetailItemId]}
        </span>
      )} */}
      <span
        style={{
          display: 'inline',
          // fontWeight: 500,
          lineHeight: 1,
          textAlign: 'center',
          whiteSpace: 'nowrap',
          verticalAlign: 'baseline',
          color: 'black',
        }}
      >
        {NetworkDetailNameMap[networkDetailItemId]}
      </span>
    </>
  );
}
