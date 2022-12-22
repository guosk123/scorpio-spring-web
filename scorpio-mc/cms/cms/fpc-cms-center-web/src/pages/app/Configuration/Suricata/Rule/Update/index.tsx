import RuleForm from '@/pages/app/Configuration/Suricata/components/RuleForm';
import { Spin } from 'antd';
import { useEffect, useState } from 'react';
import { useParams } from 'umi';
import { querySuricataRuleDetail } from '../../service';
import type { ISuricataRule } from '../../typings';

const RuleUpdate = () => {
  const { sid } = useParams() as { sid: string };

  const [detail, setDetail] = useState<ISuricataRule>();

  useEffect(() => {
    querySuricataRuleDetail(parseInt(sid)).then((res) => {
      setDetail(res.result);
    });
  }, [sid]);

  if (detail === undefined) {
    return <Spin />;
  }

  return <RuleForm detail={detail} />;
};

export default RuleUpdate;
