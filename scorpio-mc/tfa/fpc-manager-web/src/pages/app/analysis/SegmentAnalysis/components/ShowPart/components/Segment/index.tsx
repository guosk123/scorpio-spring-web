import type { INetworkSegmentDetails } from '../../../../typings';
import styles from '../../../../index.less';
import { Descriptions } from 'antd';
import { useContext } from 'react';
import { SearchDataContext } from '../../../..';
import SegTip from '../SegTip';
import DesItem from '../DesItem';
interface Props {
  networkIndex: number;
  networkDetail: INetworkSegmentDetails;
  networkId: string;
  networkBaseSet: any;
}

function Segment(props: Props) {
  const { networkIndex, networkDetail, networkId, networkBaseSet } = props;

  const { currentNetworkMap } = useContext<any>(SearchDataContext);
  // console.log(currentNetworkMap[networkId], 'currentNetworkMap[networkId]???');

  return (
    <div className={styles.wholeDisplay__displayLine__seg}>
      <Descriptions
        bordered
        size="small"
        column={2}
        labelStyle={{ width: '150px' }}
        contentStyle={{ textAlign: 'center', verticalAlign: 'middle' }}
      >
        {Object.keys(networkDetail)
          .filter((item) => item !== 'networkId')
          .map((item: string) => {
            return (
              <Descriptions.Item
                span={2}
                label={
                  // <span
                  //   style={{
                  //     display: 'inline',
                  //     padding: '.2em .6em .3em',
                  //     fontWeight: 'bold',
                  //     // fontSize: '75%',
                  //     lineHeight: 1,
                  //     textAlign: 'center',
                  //     whiteSpace: 'nowrap',
                  //     verticalAlign: 'baseline',
                  //     backgroundColor: getColor(networkDetail[item], labelColorValueRangeMap[item]),
                  //     color: '#fff',
                  //     borderRadius: '.25em',
                  //   }}
                  // >
                  //   {NetworkDetailNameMap[item]}
                  // </span>
                  <DesItem
                    networkDetailItemId={item}
                    networkDetail={networkDetail}
                    networkIndex={networkIndex}
                  />
                }
              >
                <SegTip
                  networkDetailItemId={item}
                  networkDetail={networkDetail}
                  networkIndex={networkIndex}
                  networkBaseSet={networkBaseSet}
                />
              </Descriptions.Item>
            );
          })}
      </Descriptions>
      <div className={styles.wholeDisplay__displayLine__seg__title}>
        {currentNetworkMap[networkId] ?? networkId}
      </div>
    </div>
  );
}

export default Segment;
