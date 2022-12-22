import { Button } from 'antd';

export default function LinkButton(props: any) {
  return (
    <Button size="small" type="link" {...props}>
      {props.children}
    </Button>
  );
}
