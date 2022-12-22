import { editIdentificationCode, getAuthenticationFn } from '@/utils/frame/menuAccess';
import { Checkbox } from 'antd';
import { Fragment, useContext, useEffect, useState } from 'react';
import { MenuEditContext } from '../..';
import useAuthentication from '../../hooks/useAuthentication';
import type { IMenuAccessItem } from '../../typing';

interface Props {
  identificationCode: string;
  authenticationMap: any;
  label?: string;
  accessFunctionBox?: boolean;
}

export default function AuthenticationCheckBox(props: Props) {
  const { identificationCode, authenticationMap, label, accessFunctionBox } = props;
  const tmpCode = editIdentificationCode(identificationCode);
  const {
    flag: defAuthenticationFlag,
    fatherNodes,
    childNodes,
    fatherNodeAcc,
  } = useAuthentication(tmpCode, authenticationMap);
  const [checkFlag, setCheckFlag] = useState(defAuthenticationFlag);
  const [queryAccessMenus, setAuthenticationMap] = useContext(MenuEditContext);

  useEffect(() => {
    setCheckFlag(defAuthenticationFlag);
  }, [defAuthenticationFlag]);

  const updateMenu = (flag: boolean) => {
    setCheckFlag(flag ? 1 : 0);
    const tmpParamsArr: IMenuAccessItem[] = [];
    tmpParamsArr.push({ resource: tmpCode, perm: flag ? 1 : 0 });
    if (flag) {
      fatherNodes.forEach((faKey) => tmpParamsArr.push({ resource: faKey, perm: 1 }));
      childNodes.forEach((subKey) => tmpParamsArr.push({ resource: subKey, perm: 1 }));
      if (accessFunctionBox) {
        const urlArrs = tmpCode.split('/');
        urlArrs[urlArrs.length - 1] = 'DEFdetail';
        tmpParamsArr.push({ resource: urlArrs.join('/'), perm: 1 });
      }
    } else {
      const jumpFlag = { flag: false };
      fatherNodeAcc.forEach((faNode) => {
        if (faNode.childAccSum < 2 && !jumpFlag.flag) {
          tmpParamsArr.push({ resource: faNode.key, perm: 0 });
        } else {
          jumpFlag.flag = true;
        }
      });
      childNodes.forEach((subKey) => tmpParamsArr.push({ resource: subKey, perm: 0 }));
    }
    console.log('tmpParamsArr', tmpParamsArr);
    setAuthenticationMap((prev: any) => {
      const tmpMap = { ...prev };
      tmpParamsArr.forEach((subTreeNode) => {
        tmpMap[subTreeNode.resource] = subTreeNode.perm;
      });
      return tmpMap;
    });
  };

  return (
    <Fragment>
      <Checkbox
        onClick={(e) => {
          e.stopPropagation();
        }}
        onChange={(e) => {
          updateMenu(e.target.checked);
        }}
        checked={checkFlag ? true : false}
        defaultChecked={defAuthenticationFlag ? true : false}
      >
        {label && <span style={{ paddingLeft: 8 }}>{label}</span>}
      </Checkbox>
      <span
        style={{ display: 'none' }}
        onClick={() => {
          console.log(
            'click access',
            tmpCode,
            getAuthenticationFn(tmpCode, { ...authenticationMap }),
          );
        }}
      >
        acc
      </span>
    </Fragment>
  );
}
