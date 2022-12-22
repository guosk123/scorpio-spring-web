import classNames from 'classnames';
import styles from '../../../index.less';
import OffenderIcon from './OffenderIcon';
import VictimIcon from './VictimIcon';

const RoleIcon = ({ role }: { role: 'offender' | 'victim' }) => {
  return (
    <div
      className={classNames({
        [styles.roleBadge]: true,
        [styles.roleBadgeVictim]: role === 'victim',
        [styles.roleBadgeOffender]: role === 'offender',
      })}
    >
      <div className={styles.roleBadgeIcon}>
        {role === 'victim' && <VictimIcon />}
        {role === 'offender' && <OffenderIcon />}
      </div>
    </div>
  );
};

export default RoleIcon;
