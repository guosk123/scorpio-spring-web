import { LOGIN_OUT_KEY } from '@/components/GlobalHeader/components/AvatarDropdown';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { getGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import type { ICurrentUser } from '@/models/frame/global';
import { getMenuData, PageLoading } from '@ant-design/pro-layout';
import { Button, Result } from 'antd';
import moment from 'moment';
import pathToRegexp from 'path-to-regexp';
import React from 'react';
import type { ConnectProps, Dispatch } from 'umi';
import { connect, history, Redirect } from 'umi';
import { menuDataRender } from '../BasicLayout';

interface SecurityLayoutProps extends ConnectProps {
  loading?: boolean;
  dispatch: Dispatch;
  currentUser?: ICurrentUser;
  globalSelectedTime: IGlobalTime;
}

interface SecurityLayoutState {
  isReady: boolean;
}

/** 定时查询服务器时间 */
let pollingQueryTimer: number | undefined;
/** 每秒自动累加存在客户端中的服务端时间 */
let pollingUpdateTimer: number | undefined;

class SecurityLayout extends React.Component<SecurityLayoutProps, SecurityLayoutState> {
  state: SecurityLayoutState = {
    isReady: false,
  };

  async componentDidMount() {
    const { dispatch } = this.props;

    if (dispatch) {
      // 取产品信息
      await dispatch({
        type: 'globalModel/queryProductInfos',
      });

      await dispatch({
        type: 'globalModel/queryCurrentUser',
      }).then(async (result: { success: boolean; currentUser: ICurrentUser }) => {
        const { success, currentUser } = result;
        if (!success) {
          return;
        }
        // 判断下角色权限
        const { authorities = [] } = currentUser;
        if (
          Array.isArray(authorities) &&
          authorities.find(
            (el) => el.authority === 'PERM_USER' || el.authority === 'PERM_SERVICE_USER',
          )
        ) {
          await dispatch({
            type: 'networkModel/queryNetworkSensorTree',
          });
          await dispatch({
            type: 'networkModel/queryNetworkGroupTree',
          });
          await dispatch({
            type: 'serviceModel/queryAllServices',
          });
          await dispatch({
            type: 'metadataModel/queryAllProtocols',
          });
          await dispatch({
            type: 'SAKnowledgeModel/queryAllApplications',
          });
          await dispatch({
            type: 'geolocationModel/queryGeolocations',
          });
          await dispatch({
            type: 'ipAddressGroupModel/queryAllIpAddressGroup',
          });
        }
      });

      // 先设置服务器时间
      await this.queryRuntimeEnvironments();
      // 更新一次时间
      const fixGlobalTime = getGlobalTime(this.props.globalSelectedTime);
      await dispatch({
        type: 'appModel/updateGlobalSelectedTime',
        payload: {
          globalSelectedTime: fixGlobalTime,
        },
      });

      pollingQueryTimer = window.setInterval(() => {
        this.queryRuntimeEnvironments();
        // 每10分钟定时同步一次时间
      }, 10 * 60 * 1000);

      this.setState({
        isReady: true,
      });
    }
  }

  componentWillUnmount() {
    if (pollingQueryTimer) {
      window.clearInterval(pollingQueryTimer);
    }
    if (pollingUpdateTimer) {
      window.clearInterval(pollingUpdateTimer);
    }
  }

  queryRuntimeEnvironments = async () => {
    const { dispatch } = this.props;
    await dispatch({
      type: 'globalModel/queryRuntimeEnvironments',
    });

    const { systemTime, clientTimeDiffSystemTimeSeconds: diffSeconds } = window;
    // 先清除定时
    if (pollingUpdateTimer) {
      window.clearInterval(pollingUpdateTimer);
    }

    // 如果不存在时间差，表示2个时间相等或者是获取是服务器时间异常
    // 获取服务器异常时直接使用客户端时间即可，也没必要启动定时器了
    if (!systemTime) {
      return;
    }

    pollingUpdateTimer = window.setInterval(() => {
      if (systemTime) {
        // 新的时间 = 客户端时间 - 时间差
        const newTime = moment(new Date()).subtract(diffSeconds, 'seconds').format();
        window.systemTime = newTime;
        dispatch({
          type: 'globalModel/updateState',
          payload: {
            systemTime: newTime,
          },
        });
      }
    }, 1000);
  };

  render() {
    const { isReady } = this.state;
    const { children, loading, currentUser, route, location } = this.props;
    // You can replace it to your authentication rule (such as check token exists)
    // 你可以把它替换成你自己的登录认证规则（比如判断 token 是否存在）
    const isLogin = currentUser && currentUser.id;
    // const queryString = stringify({
    //   redirect: window.location.href,
    // });

    if ((!isLogin && loading) || !isReady) {
      return <PageLoading />;
    }

    // 先判断登录
    if (!isLogin && window.location.pathname !== '/login') {
      /**
       * @note: 内嵌页面不再跳转到登录页面，给出提示即可
       */
      if (window.location.href.includes('/embed/')) {
        return <Redirect to={`/embed/not-login`} />;
      }
      // 从系统页面退出登录时，不需要在后边添加要打开的页面
      if (
        window.location.href.includes('openUrl') ||
        history.location.query?.[LOGIN_OUT_KEY] === LOGIN_OUT_KEY ||
        history.location.pathname === '/'
      ) {
        return <Redirect to={`/login`} />;
      }
      const urlQuery = history.location.query || {};
      const queryParams = Object.keys(urlQuery)
        .map((key) => `${key}=${urlQuery[key]}`)
        .join('&');
      const pathAndQuery = encodeURIComponent(
        `${history.location.pathname}${queryParams.length ? `?${queryParams}` : ''}`,
      );
      return <Redirect to={`/login?openUrl=${pathAndQuery}`} />;
    }

    // 再判断权限
    const { breadcrumbMap } = getMenuData(
      route?.routes || [],
      { locale: false },
      undefined,
      menuDataRender,
    );

    // 所有命中的菜单
    const pathList = Array.from(breadcrumbMap.keys());
    // 正则查找匹配上的路由
    const targetRoute = pathList.find((path) => pathToRegexp(path).test(location.pathname));
    // 排除掉 BI 的页面
    if (location.pathname.indexOf('/dashboard/custom/') > -1) {
      return children;
    }

    if (!targetRoute || breadcrumbMap.get(targetRoute)?.unaccessible) {
      return <Redirect to={`/`} />;
      return (
        <Result
          status={!targetRoute ? '404' : '403'}
          title={!targetRoute ? '页面不存在' : '没有权限访问'}
          subTitle={!targetRoute ? '抱歉，您访问的页面不存在' : '抱歉，您无权访问该页面'}
          extra={[
            <Button key="go-back" type="primary" onClick={() => history.goBack()}>
              返回上一级
            </Button>,
            <Button key="go-home" type="primary" onClick={() => history.replace('/')}>
              返回首页
            </Button>,
          ]}
        />
      );
    }

    return children;
  }
}

export default connect(
  ({ globalModel, appModel: { globalSelectedTime }, loading }: ConnectState) => ({
    currentUser: globalModel.currentUser,
    globalSelectedTime,
    loading: loading.models.globalModel,
  }),
)(SecurityLayout);
