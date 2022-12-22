import type { IAccessRouteItem } from '@/pages/frame/system/MenuEdit/typing';
import routes from '../../../../config/routes';
//hasUserPerm
export const editIdentificationCode = (code: string) => {
  return code === 'all' ? code : `all${code}`;
};
// 配置管理员路由
const admAndAdminRoutes = routes
  .filter((item) => item.path === '/')[0]
  .routes?.filter((item) => item.path === '/')[0] as IAccessRouteItem;
const admRoutes = admAndAdminRoutes.routes?.filter((item) => item.access === 'hasUserPerm');
export const routesPathArr = admRoutes || [];

export const getAllRoutesTree = () => {
  // 配置管理员路由
  const tmpArr = [...routesPathArr];
  for (let index = 0; index < tmpArr.length; index++) {
    const item = tmpArr[index];
    if (item?.accessMenuIgnore) {
      continue;
    }
    if (item.routes?.length) {
      tmpArr.push(...item.routes);
    }
    item.key = item.path || '';
    const tmpChildren: any = [];
    item.children = tmpChildren
      .concat(
        item.routes?.filter(
          (route) =>
            !route?.accessMenuIgnore && !((window as any)?.disablePath || []).includes(route.path),
        ) || [],
      )
      .concat(item.accessFunction?.map((accItem) => ({ ...accItem, key: accItem.path })) || []);
  }

  return [
    {
      title: '所有菜单',
      key: 'all',
      path: 'all',
      children: routesPathArr,
    },
  ];
};

export const getRoutesMenu = () => {
  return getAllRoutesTree()[0].children;
};

export const getRouteAccessMap = (defaultAdm?: boolean) => {
  const resMap = {};
  const tmpArr = [...getAllRoutesTree()];
  for (let index = 0; index < tmpArr.length; index++) {
    const element = tmpArr[index];
    if (element?.children?.length) {
      tmpArr.push(...(element.children as any));
    }
    resMap[editIdentificationCode(element.path)] = defaultAdm ? 1 : 0;
  }
  return resMap;
};

// 根据路由生成的map，从后端存储map中过滤出权限值
export const routeAccessMap = getRouteAccessMap();

/**
 *
 * @param paramRoutes 配置管理员路由
 * @param pathname 路由
 * @returns 传入路由子节点
 */
const getRouteChildNodes = (paramRoutes: IAccessRouteItem[], pathname: string) => {
  let resNodes: string[] = [];
  const tmpNodes = paramRoutes;
  for (let index = 0; index < tmpNodes.length; index++) {
    const element = tmpNodes[index];
    if (editIdentificationCode(element.path) === pathname) {
      // console.log('paramRoutes', paramRoutes, element.children);
      resNodes = element.children?.map((item) => item.key || 'nokey') || [];
      return resNodes;
    }
    if (element.children) {
      tmpNodes.push(...element.children);
    }
  }
  return resNodes;
};

export const whileListForAccess = {
  'all/welcome': 1,
  'all/': 1,
  'all/redirect': 1,
  'all/analysis/network/packet': 1,
};

export const getAuthenticationFn = (authenticationCode: string, authenticationMap: any) => {
  // 生成父节点时使用的临时变量
  const tmpArr: string[] = [];
  // 父节点key
  const fatherNodeKeys: string[] = [];
  // 节点是否打开
  const accessMap = {
    ...(authenticationMap || {}),
    ...whileListForAccess,
  };
  const resflag = (() => {
    let res = 0;
    const tmpRes = String(accessMap[authenticationCode]);
    if (tmpRes === 'undefined') {
      res = 0;
    } else {
      res = accessMap[authenticationCode];
    }
    if (authenticationCode.includes('/embed/')) {
      // 暂时忽略内嵌
      return true;
    }
    return res;
  })();
  // 子节点是否全部打开
  let childFlag = false;
  // 根据pathname生成可能的父节点key，
  authenticationCode.split('/').forEach((item) => {
    tmpArr.push(item);
    fatherNodeKeys.push(tmpArr.join('/'));
  });

  // 打开节点，父节点打开+本节点打开+子节点打开
  // 关闭节点，向上逐渐关闭父节点中子节点打开数量为1的节点，遇到子节点打开数量大于1的停止关闭父节点
  const fatherNodes: string[] = [];
  const childNodes: string[] = [];
  /**
   * key: 节点key
   * access: 节点当前权限
   * childAccSum: 子节点打开个数
   * fatherNodeChilds: 子节点
   */
  const fatherNodeAcc: {
    key: string;
    access: boolean;
    childAccSum: number;
    fatherNodeChilds: string[];
  }[] = [];
  const childNodeAcc: { key: string; access: boolean }[] = [];

  const authenticationMapKeys = Object.keys(authenticationMap);

  authenticationMapKeys.forEach((key) => {
    if (key.includes(`${authenticationCode}/`) && key !== authenticationCode) {
      childNodeAcc.push({ key: key, access: authenticationMap[key] });
      childNodes.push(key);
    }
  });

  fatherNodeKeys.forEach((item) => {
    // 从总的map中过滤出可用父节点
    if (authenticationMapKeys.includes(`${item}`) && item !== authenticationCode) {
      fatherNodes.push(item);
      const fatherNodeChilds = getRouteChildNodes(getAllRoutesTree(), item) || [];
      const childAccStates: number[] = fatherNodeChilds.map((accNode) =>
        authenticationMap[editIdentificationCode(accNode)] ? 1 : 0,
      );
      fatherNodeAcc.push({
        key: item,
        access: authenticationMap[item],
        childAccSum: childAccStates.length ? childAccStates.reduce((r, l) => r + l) : 0,
        fatherNodeChilds: fatherNodeChilds || [],
      });
    }

    childNodes.forEach((tNode) => {
      if (tNode.includes('$accessFunction')) {
        return;
      }
      if (authenticationMap[tNode] && !childFlag) {
        childFlag = true;
      }
    });
    // resflag = childNodes.length ? childFlag : resflag;
  });
  return {
    flag: resflag,
    path: authenticationCode,
    childFlag,
    fatherNodes,
    childNodes,
    fatherNodeAcc: fatherNodeAcc.sort((r, l) => l.key.length - r.key.length),
    childNodeAcc,
  };
};
