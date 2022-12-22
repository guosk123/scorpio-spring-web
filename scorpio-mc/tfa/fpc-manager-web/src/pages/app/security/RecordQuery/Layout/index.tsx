import type { IPageLayout } from '@/layouts/PageLayout';
import PageLayout from '@/layouts/PageLayout';

const Layout = (props: IPageLayout) => {
  return <PageLayout {...props} />;
};

export default Layout;
