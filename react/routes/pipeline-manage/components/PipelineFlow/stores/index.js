import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import { usePipelineManageStore } from '../../../stores';

const Store = createContext();

export function usePipelineFlowStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props) => {
  const {
    children,
  } = props;

  const {
    mainStore: {
      getSelectedMenu,
    },
  } = usePipelineManageStore();

  const value = {
    ...props,
    getSelectedMenu,
    // : {
    //   parentId, Id, type, status, name,
    // },
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
