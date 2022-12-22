import TimeRangeSlider from '@/components/TimeRangeSlider';
import PageLayout from '../PageLayout';
import type { BasicLayoutProps } from '../BasicLayout';

export interface ITimeLayout {
  location: {
    pathname: string;
  };
  route: BasicLayoutProps['route'] & {
    authority: string[];
  };
  match: {
    path: string;
    url: string;
  };
  children?: React.ReactNode;
}

const Index: React.FC<ITimeLayout> = ({
  route,
  match,
  location = {
    pathname: '/',
  },
  children,
}) => {
  return (
    <>
      <TimeRangeSlider />
      <PageLayout location={location} route={route} match={match} children={children} />
    </>
  );
};

export default Index;
