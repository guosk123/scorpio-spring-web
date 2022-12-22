import { useParams } from 'umi';
import TransmitForm from '../components/TransmitForm';

export default function Update() {
  const { id } = useParams<{ id: string }>();
  return <TransmitForm syslogId={id} />;
}
