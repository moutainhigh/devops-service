package io.choerodon.devops.infra.template;

import io.choerodon.core.notify.PmTemplate;

/**
 * @author zmf
 * @since 12/5/19
 */
// TODO by zmf
public class ServiceFailurePmTemplate implements PmTemplate {
    @Override
    public String code() {
        return "ServiceFailurePm";
    }

    @Override
    public String name() {
        return "网络创建失败站内信模板";
    }

    @Override
    public String businessTypeCode() {
        // TODO by zmf
        return null;
    }

    @Override
    public String title() {
        return "网络创建失败";
    }

    @Override
    public String content() {
        return "<p>您在项目“${projectName}”下“${envName}”环境中创建的网络“${resourceName}”失败</p>";
    }
}
