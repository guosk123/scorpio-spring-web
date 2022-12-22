import { useParams } from 'umi';
import RuleForm from '../component/RuleForm';

export default function Update() {
  const { id } = useParams<{ id: string }>();
  return <RuleForm id={id} />;
}
