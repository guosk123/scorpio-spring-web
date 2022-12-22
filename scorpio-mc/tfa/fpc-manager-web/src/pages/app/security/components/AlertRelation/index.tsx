import { ipIsEqual } from '@/utils/frame/ip';
import { abortAjax } from '@/utils/utils';
import { useSafeState } from 'ahooks';
import { Spin } from 'antd';
import moment from 'moment';
import { useEffect, useMemo } from 'react';
import { queryAlertDetailRelation } from '../../service';
import type { ISuricataAlertMessage, SuricataAlertEvent } from '../../typings';
import type { IDetection } from './components/Detection';
import Detection from './components/Detection';
import OffenderIcon from './components/Detection/components/OffenderIcon';
import VictimIcon from './components/Detection/components/VictimIcon';
import styles from './index.less';

interface Props {
  alert: ISuricataAlertMessage;
}

const roleLabel = {
  offender: '攻击者',
  victim: '受害者',
};

const AlertRelation = (props: Props) => {
  const { alert } = props;
  const [events, setEvents] = useSafeState<SuricataAlertEvent[]>();
  const [loading, setLoading] = useSafeState(false);

  useEffect(() => {
    const endTime = moment(alert.timestamp).add(1, 'day').format();
    const startTime = moment(alert.timestamp).add(-1, 'day').format();

    setLoading(true);
    queryAlertDetailRelation({
      srcIp: alert.srcIp,
      destIp: alert.destIp,
      sid: alert.sid,
      startTime,
      endTime,
    }).then((res) => {
      setLoading(false);
      const { success, result } = res;
      if (success) {
        setEvents(result);
      }
    });
    return () => {
      abortAjax(['/suricata/alert-messages/relation']);
    };
  }, [alert, setEvents, setLoading]);

  // 将相同sid与时间的event聚合起来
  const detections: IDetection[] = useMemo(() => {
    if (events) {
      // 构建检测
      const detectionMap = events?.reduce((map, currentEvent) => {
        const { sid, timestamp, srcRole, msg, destRole } = currentEvent;

        map[`${sid}-${timestamp}`] = {
          sid,
          timestamp,
          srcIp: alert.srcIp,
          destIp: alert.destIp,
          offset: 0,
          msg,
          srcRole,
          destRole,
        };

        return map;
      }, {} as Record<string, IDetection>);

      const tmpDetections: IDetection[] = Object.values(detectionMap);

      // offset计算
      tmpDetections.forEach((detection) => {
        detection.offset =
          new Date(detection.timestamp).getTime() - new Date(alert.timestamp).getTime();
      });

      const currentDetection = {
        sid: alert.sid,
        offset: 0,
        srcIp: alert.srcIp,
        destIp: alert.destIp,
        timestamp: alert.timestamp,
        srcRole: ipIsEqual(alert.srcIp, alert.target) ? 'victim' : 'offender',
        destRole: ipIsEqual(alert.destIp, alert.target) ? 'victim' : 'offender',
        msg: alert.msg,
      };

      // 根据当前告警构建detection
      tmpDetections.push(currentDetection as IDetection);

      // 检测按照偏移排序
      tmpDetections.sort((detectionA, detectionB) => {
        return detectionA.offset - detectionB.offset;
      });

      tmpDetections.forEach((item) => {
        if (item.offset === 0) return;
        let detectionChangeMsgs: string[] = [];
        if (item.srcRole === currentDetection.srcRole) {
          detectionChangeMsgs.push(`Same ${roleLabel[currentDetection.srcRole]}`);
        }
        if (item.destRole === currentDetection.destRole) {
          detectionChangeMsgs.push(`Same ${roleLabel[currentDetection.destRole]}`);
        }
        if (item.srcRole && item.srcRole === currentDetection.destRole) {
          detectionChangeMsgs.push(
            `${roleLabel[currentDetection.srcRole]} -> ${roleLabel[item.srcRole]}`,
          );
        }
        if (item.destRole && item.destRole === currentDetection.srcRole) {
          detectionChangeMsgs.push(
            `${roleLabel[currentDetection.destRole!]} -> ${roleLabel[item.destRole]}`,
          );
        }

        if (detectionChangeMsgs.length === 2) {
          if (detectionChangeMsgs.filter((msg) => msg.indexOf('Same') !== -1).length === 2) {
            detectionChangeMsgs = ['相同身份'];
          }
          if (detectionChangeMsgs.filter((msg) => msg.indexOf('->') !== -1).length === 2) {
            detectionChangeMsgs = ['身份逆转'];
          }
        }

        detectionChangeMsgs = detectionChangeMsgs.map((msg) => {
          return msg.replace('Same ', '相同');
        });
        item.participantMsg = detectionChangeMsgs.join(',');
      });

      return tmpDetections.slice(0, 15);
    }
    return [];
  }, [alert, events]);

  if (loading) {
    return (
      <div className={styles.textCell}>
        <Spin />
      </div>
    );
  }

  return (
    <>
      <div className={styles.container}>
        <div
          className={styles.relationTimelineGrid}
          style={{
            gridTemplateColumns: `repeat(${detections.length}, ${
              Math.floor(1500 / detections.length) - 5
            }px)`,
          }}
        >
          {detections?.map((detection) => {
            return (
              <Detection key={`${detection.sid}-${detection.timestamp}`} detection={detection} />
            );
          })}
        </div>
        <div className={styles.legend}>
          <div className={styles.legendItem}>
            <div className={styles.icon}>
              <OffenderIcon />
            </div>
            <span>攻击者</span>
          </div>
          <div className={styles.legendItem}>
            <div className={styles.icon}>
              <VictimIcon />
            </div>
            <span>受害者</span>
          </div>
        </div>
      </div>
    </>
  );
};

export default AlertRelation;
