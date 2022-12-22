import { CaretDownFilled } from '@ant-design/icons';
import classNames from 'classnames';
import { useMemo } from 'react';
import styles from '../../index.less';
import DetectionDetail from '../DetectionDetail';
import RoleIcon from './components/RoleIcon';

export function simpleSecond(second: number) {
  const abs = Math.round(Math.abs(second));

  if (abs < 60) {
    return `${Math.floor(abs)}s`;
  }

  if (abs < 60 * 60) {
    return `${Math.floor(abs / 60)}M`;
  }

  return `${Math.floor(abs / 60 / 60)}H`;
}

export interface IDetection {
  sid: number;
  offset: number;
  participantMsg?: string;
  srcIp: string;
  destIp: string;
  timestamp: string;
  srcRole: 'offender' | 'victim' | null;
  destRole: 'offender' | 'victim' | null;
  msg: string;
}

interface Props {
  detection: IDetection;
}

const Detecton = ({ detection }: Props) => {
  const current = detection.offset === 0;

  const sourceDom = useMemo(() => {
    if (detection.offset === 0) {
      return (
        <div className={styles.originParticipant}>
          <div className={styles.originParticipantLabel}>
            <RoleIcon role={detection.srcRole!} />
          </div>

          <div className={styles.originParticipantTitle}>{detection.srcIp}</div>
        </div>
      );
    }
    if (!detection.srcRole) {
      return null;
    }

    return <RoleIcon role={detection.srcRole} />;
  }, [detection.offset, detection.srcIp, detection.srcRole]);

  const targetDom = useMemo(() => {
    if (detection.offset === 0) {
      return (
        <div className={styles.originParticipant}>
          <div className={styles.originParticipantLabel}>
            <RoleIcon role={detection.destRole!} />
          </div>
          <div className={styles.originParticipantTitle}>{detection.destIp}</div>
        </div>
      );
    }
    if (!detection.destRole) {
      return null;
    }

    return <RoleIcon role={detection.destRole} />;
  }, [detection.destIp, detection.destRole, detection.offset]);

  return (
    <>
      <div
        className={classNames({
          [styles.relativeTime]: true,
          [styles.textCell]: true,
          [styles.relativeTimeOrigin]: current,
        })}
      >
        <span>
          {current
            ? 'T0'
            : `T${detection.offset > 0 ? '+' : '-'}${simpleSecond(detection.offset / 1000)}`}
        </span>
      </div>
      <div className={styles.timeline}>
        <div className={styles.timelineBackground}>
          <div className={styles.timelineNode}>
            {current ? (
              <div className={styles.timelineNodeOriginPointer}>
                <CaretDownFilled />
              </div>
            ) : (
              <div className={styles.timelineNodeDot} />
            )}
          </div>
        </div>
      </div>
      <div className={styles.detection}>
        <DetectionDetail current={current} detection={detection} />
      </div>
      <div
        className={classNames({
          [styles.detectionBottom]: true,
          [styles.detectionBottomOrigin]: current,
        })}
      />
      <div className={classNames([styles.participantsLabel, styles.textCell])} />
      <div
        className={classNames({
          [styles.offenderRole]: true,
          [styles.textCell]: true,
          [styles.offenderRoleOrigin]: current,
        })}
      >
        {sourceDom}
      </div>
      <div
        className={classNames({
          [styles.victimRole]: true,
          [styles.textCell]: true,
          [styles.victimRoleOrigin]: current,
        })}
      >
        {targetDom}
      </div>
      <div className={classNames([styles.participantDescription, styles.textCell])}>
        {detection.participantMsg}
      </div>
    </>
  );
};

export default Detecton;
