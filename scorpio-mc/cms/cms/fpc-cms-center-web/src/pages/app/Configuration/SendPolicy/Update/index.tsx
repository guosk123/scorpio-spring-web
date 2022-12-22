import { useParams } from 'umi';
import SendPolicyForm from '../component/SendPolicyForm';

export default function Update() {
  const { id } = useParams<{ id: string }>();
  return <SendPolicyForm id={id} />;
}
