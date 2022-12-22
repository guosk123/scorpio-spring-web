import type { INetworkDetails } from '../../../../typings';
import SingleSegment from '../SingleSegment';
import styles from '../../../../index.less';
interface Props {
  networkDetail: INetworkDetails;
}

function Segment(props: Props) {
  const { networkDetail } = props;
  const showItem = JSON.stringify(networkDetail) == '{}';
  return (
    <div
      className={
        showItem
          ? styles.wholeDisplay__displayLine__emptySeg
          : styles.wholeDisplay__displayLine__seg
      }
    >
      {Object.keys(networkDetail).map((item: string) => {
        return <SingleSegment titleId={item} value={networkDetail[item]} />;
      })}
    </div>
  );
}

export default Segment;
