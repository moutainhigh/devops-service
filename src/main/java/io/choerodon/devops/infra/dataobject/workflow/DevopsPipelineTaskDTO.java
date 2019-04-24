package io.choerodon.devops.infra.dataobject.workflow;

import java.util.List;


/**
 * Created by Sheep on 2019/4/2.
 */
public class DevopsPipelineTaskDTO {
    private Long taskId;
    private String taskName;
    private List<String> usernames;
    private String taskType;
    private Boolean multiAssign;
    private Long sign;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getSign() {
        return sign;
    }

    public void setSign(Long sign) {
        this.sign = sign;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public List<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(List<String> usernames) {
        this.usernames = usernames;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public Boolean getMultiAssign() {
        return multiAssign;
    }

    public void setMultiAssign(Boolean multiAssign) {
        this.multiAssign = multiAssign;
    }
}
