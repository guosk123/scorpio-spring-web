import useFetchData from '@/utils/hooks/useFetchData';
import { Spin } from 'antd';
import { useParams } from 'umi';
import MailRuleForm from '../Form';
import { queryMailLoginRuleDetail } from '../service';
import type { IMailLoginRule } from '../typings';

const MailRuleUpdate = () => {
  const { ruleId } = useParams() as { ruleId: string };

  const { data, loading } = useFetchData<IMailLoginRule>(queryMailLoginRuleDetail, {
    args: [ruleId],
  });

  if (loading) {
    return (
      <div className="center">
        <Spin />
      </div>
    );
  }

  return <MailRuleForm detail={data} />;
};

export default MailRuleUpdate;
