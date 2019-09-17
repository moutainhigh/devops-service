package io.choerodon.devops.api.vo;

import io.choerodon.devops.infra.enums.AppServiceStatus;

import java.util.List;

/**
 * @author zhaotianxin
 * @since 2019/9/12
 */
public class AppServiceVO {
    private Long id;
    private String name;
    private String code;
    private String type;
    List<AppServiceVersionVO> appServiceVersions;
    private String status;

    public List<AppServiceVersionVO> getAppServiceVersions() {
        return appServiceVersions;
    }

    public void setAppServiceVersions(List<AppServiceVersionVO> appServiceVersions) {
        this.appServiceVersions = appServiceVersions;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
