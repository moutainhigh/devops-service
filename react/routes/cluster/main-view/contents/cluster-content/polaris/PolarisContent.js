import React, { Fragment, Suspense, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Spin } from 'choerodon-ui';
import { useClusterMainStore } from '../../../stores';
import { useClusterContentStore } from '../stores';
import EmptyPage from '../../../../../../components/empty-page';
import NumberDetail from './number-detail';
import CollapseDetail from './collapse-detail';

import './index.less';

const polaris = observer((props) => {
  const {
    intlPrefix,
    prefixCls,
  } = useClusterMainStore();
  const {
    contentStore: {
      setTabKey,
    },
    formatMessage,
    tabs: {
      POLARIS_TAB,
    },
    ClusterDetailDs,
  } = useClusterContentStore();

  function refresh() {

  }

  return (
    <div className={`${prefixCls}-polaris-wrap`}>
      <NumberDetail />
      <CollapseDetail />
    </div>
  );
});

export default polaris;
