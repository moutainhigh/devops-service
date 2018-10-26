package io.choerodon.devops.api.dto;

import java.util.Map;

/**
 * Created by younger on 2018/4/9.
 */
public class DevopsEnviromentDTO {

    private Long projectId;
    private String name;
    private String code;
    private String description;
    private Long devopsEnvGroupId;
    private Map<String, Boolean> updateMap;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getDevopsEnvGroupId() {
        return devopsEnvGroupId;
    }

    public void setDevopsEnvGroupId(Long devopsEnvGroupId) {
        this.devopsEnvGroupId = devopsEnvGroupId;
    }

    public Map<String, Boolean> getUpdateMap() {
        return updateMap;
    }

    public void setUpdateMap(Map<String, Boolean> updateMap) {
        this.updateMap = updateMap;
    }
}
