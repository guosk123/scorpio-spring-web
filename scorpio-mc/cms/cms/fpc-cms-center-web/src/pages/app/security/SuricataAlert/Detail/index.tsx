import { Divider } from 'antd';
import AlertRelation from '../../components/AlertRelation';
import RuleProfile from '../../components/RuleProfile';
import type { ISuricataAlertMessage } from '../../typings';

interface Props {
  alert: ISuricataAlertMessage;
}

const Detail = ({ alert }: Props) => {
  const { sid } = alert;

  return (
    <>
      <AlertRelation alert={alert} />
      <Divider />
      <RuleProfile id={sid} />
    </>
  );
};

export default Detail;
