import type { ConnectState } from '@/models/connect';
import ClasstypeForm from '@/pages/app/Configuration/Suricata/components/ClasstypeForm';
import type { IRuleClasstype } from '@/pages/app/security/typings';
import { useParams, useSelector } from 'umi';

const RuleClasstypeUpdate = () => {
  const { id } = useParams() as { id: string };

  const detail = useSelector<ConnectState, IRuleClasstype | undefined>((state) =>
    state.suricataModel.classtypes.find((classtype) => classtype.id === id),
  );

  return <ClasstypeForm detail={detail} />;
};

export default RuleClasstypeUpdate;
