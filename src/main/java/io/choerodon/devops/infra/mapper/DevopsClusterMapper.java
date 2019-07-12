package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;


import io.choerodon.devops.api.vo.DevopsEnvironmentPodVO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsClusterDTO;

public interface DevopsClusterMapper extends Mapper<DevopsClusterDTO> {

    List<DevopsClusterDTO> listByProjectId(@Param("projectId") Long projectId, @Param("organizationId") Long organizationId);

    void updateSkipCheckPro(@Param("clusterId") Long clusterId, @Param("skipCheckPro") Boolean skipCheckPro);

    List<DevopsClusterDTO> listClusters(@Param("organizationId") Long organizationId,
                                        @Param("searchParam") Map<String, Object> searchParam,
                                        @Param("param") String param);

    /**
     * 查询节点下的Pod
     * @param clusterId 集群id
     * @param nodeName 节点名称
     * @param searchParam 查询参数
     * @return pods
     */
    List<DevopsEnvironmentPodVO> pageQueryPodsByNodeName(@Param("clusterId") Long clusterId,
                                                         @Param("nodeName") String nodeName,
                                                         @Param("searchParam") String searchParam);

    void updateProjectId(@Param("orgId") Long orgId,
                         @Param("proId") Long proId);
}
