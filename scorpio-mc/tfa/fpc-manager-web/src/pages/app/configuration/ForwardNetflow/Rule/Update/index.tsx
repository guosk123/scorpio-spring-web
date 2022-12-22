import { Spin } from 'antd';
import { useEffect, useState } from 'react';
import { useParams } from 'umi';
import RuleForm from '../../components/RuleForm';
import { queryForwardRuleDetail } from '../../service';
import type { IForwardRule } from '../../typings';

const RuleUpdate = () => {
  const { ruleId } = useParams() as unknown as { ruleId: string };

  const [detail, setDetail] = useState<IForwardRule>();
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    queryForwardRuleDetail(ruleId).then(({ success, result }) => {
      if (success) {
        setDetail(result);
      }
      setLoading(false);
    });
  }, [ruleId]);

  if (loading) {
    return <Spin />;
  }

  return <RuleForm detail={detail} />;
};

export default RuleUpdate;
