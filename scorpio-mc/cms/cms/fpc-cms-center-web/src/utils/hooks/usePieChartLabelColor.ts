import type { ConnectState } from '@/models/connect';
import { useMemo } from 'react';
import type { TTheme } from 'umi';
import { ETheme, useSelector } from 'umi';

export default function usePieChartLabelColor() {
  const theme = useSelector<ConnectState, TTheme>((state) => state.settings.theme);

  const labelColor = useMemo(() => {
    if (theme === ETheme.light) {
      return '#222222';
    } else {
      return '#c8c8c8';
    }
  }, [theme]);

  return labelColor;
}
