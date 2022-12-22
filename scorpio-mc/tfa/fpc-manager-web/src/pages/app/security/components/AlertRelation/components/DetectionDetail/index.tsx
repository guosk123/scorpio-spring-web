import Ellipsis from '@/components/Ellipsis';
import type { IFilter } from '@/components/FieldFilter/typings';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { ETimeType } from '@/components/GlobalTimeSelector';
import { getLinkUrl, isIpv4, jumpNewPage } from '@/utils/utils';
import classNames from 'classnames';
import moment from 'moment';
import { useMemo } from 'react';
import styles from '../../index.less';
import type { IDetection } from '../Detection';

interface Props {
  current: boolean;
  detection: IDetection;
}

const DetectionDetail = ({ current, detection }: Props) => {
  const { sid, timestamp } = detection;

  const url = useMemo(() => {
    const result: IFilter[] = [{ field: 'sid', operand: sid, operator: EFilterOperatorTypes.EQ }];

    if (detection.srcRole) {
      result.push({
        field: isIpv4(detection.srcIp) ? 'src_ipv4' : 'src_ipv6',
        operator: EFilterOperatorTypes.EQ,
        operand: detection.srcIp,
      });
    }

    if (detection.destRole) {
      result.push({
        field: isIpv4(detection.destIp) ? 'dest_ipv4' : 'dest_ipv6',
        operator: EFilterOperatorTypes.EQ,
        operand: detection.destIp,
      });
    }

    const startTimestamp = moment(timestamp).add(-30, 'minute').valueOf();
    const endTimestamp = moment(timestamp).add(30, 'minute').valueOf();

    return getLinkUrl(
      `/analysis/security/alert?from=${startTimestamp}&to=${endTimestamp}&timeType=${
        ETimeType.CUSTOM
      }&filter=${encodeURIComponent(JSON.stringify(result))}`,
    );
  }, [detection.destIp, detection.destRole, detection.srcIp, detection.srcRole, timestamp, sid]);

  return (
    <div
      className={classNames({
        [styles.eventDetection]: true,
        [styles.eventDetectionOrigin]: current,
      })}
    >
      <a
        onClick={() => {
          jumpNewPage(url);
        }}
        className={styles.detectionTitle}
      >
        <Ellipsis tooltip={true} lines={2}>
          {detection?.msg}
        </Ellipsis>
      </a>
      <div className={styles.textCenter}>{moment(timestamp).format('YYYY-MM-DD HH:mm:ss')}</div>
    </div>
  );
};

export default DetectionDetail;
