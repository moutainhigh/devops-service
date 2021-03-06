package io.choerodon.devops.infra.feign.operator;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.base.Functions;
import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.CiVariableVO;
import io.choerodon.devops.api.vo.FileCreationVO;
import io.choerodon.devops.app.service.PermissionHelper;
import io.choerodon.devops.infra.dto.RepositoryFileDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.gitlab.*;
import io.choerodon.devops.infra.dto.gitlab.ci.Pipeline;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.GitlabServiceClient;
import io.choerodon.devops.infra.util.FeignResponseStatusCodeParse;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.PageInfoUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;


/**
 * Created by Sheep on 2019/7/11.
 */

@Component
public class GitlabServiceClientOperator {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabServiceClientOperator.class);
    private static final String ERROR_CREATE_PIPELINE_FILED = "error.create.pipeline.failed";
    private static final String ERROR_RETRY_PIPELINE_FILED = "error.retry.pipeline.filed";
    private static final String ERROR_CANCEL_PIPELINE_FILED = "error.cancel.pipeline.filed";
    @Autowired
    @Lazy
    private GitlabServiceClient gitlabServiceClient;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private GitUtil gitUtil;
    @Autowired
    private PermissionHelper permissionHelper;


    public GitLabUserDTO createUser(String password, Integer projectsLimit, GitlabUserReqDTO userReqDTO) {
        ResponseEntity<GitLabUserDTO> userDOResponseEntity;
        try {
            GitlabTransferDTO gitlabTransferDTO = new GitlabTransferDTO();
            gitlabTransferDTO.setGitlabUserReqDTO(userReqDTO);
            gitlabTransferDTO.setPassword(password);
            userDOResponseEntity = gitlabServiceClient.createUser(projectsLimit, gitlabTransferDTO);
        } catch (Exception e) {
            LOGGER.info("error.gitlab.user.create");
            throw new CommonException(e.getMessage(), e);
        }
        return userDOResponseEntity.getBody();
    }

    public GitLabUserDTO queryUserByUserName(String userName) {
        ResponseEntity<GitLabUserDTO> userDTOResponseEntity;
        try {
            userDTOResponseEntity = gitlabServiceClient.queryUserByUserName(userName);
        } catch (Exception e) {
            return null;
        }
        return userDTOResponseEntity.getBody();
    }

    public GitLabUserDTO queryAdminUser() {
        ResponseEntity<GitLabUserDTO> userDTOResponseEntity;
        try {
            userDTOResponseEntity = gitlabServiceClient.queryAdminUser();
        } catch (Exception e) {
            throw new CommonException(e);
        }
        return userDTOResponseEntity.getBody();
    }

    public GitLabUserDTO updateUser(Integer userId, Integer projectsLimit, GitlabUserReqDTO userReqDTO) {
        ResponseEntity<GitLabUserDTO> userDTOResponseEntity;
        try {
            userDTOResponseEntity = gitlabServiceClient.updateGitLabUser(
                    userId, projectsLimit, userReqDTO);
        } catch (Exception e) {
            throw new CommonException(e);
        }
        return userDTOResponseEntity.getBody();
    }

    /**
     * 更新用户密码(用gitlab的admin更新用户的密码)
     *
     * @param userId   gitlab用户id
     * @param password 要被更新的密码
     */
    public void updateUserPassword(Integer userId, String password) {
        GitlabUserWithPasswordDTO user = new GitlabUserWithPasswordDTO();
        user.setPassword(Objects.requireNonNull(password));
        try {
            gitlabServiceClient.updateUserPasswordByUserId(Objects.requireNonNull(userId), user);
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }

    public void enableUser(Integer userId) {

        try {
            gitlabServiceClient.enableUser(userId);
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }

    public void disableUser(Integer userId) {
        try {
            gitlabServiceClient.disableUser(userId);
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }

    public GitLabUserDTO queryUserById(Integer userId) {
        ResponseEntity<GitLabUserDTO> userDTOResponseEntity;
        try {
            userDTOResponseEntity = gitlabServiceClient.queryUserById(userId);
        } catch (Exception e) {
            throw new CommonException(e);
        }
        return userDTOResponseEntity.getBody();
    }

    public Boolean checkEmail(String email) {
        return gitlabServiceClient.checkEmail(email).getBody();
    }


    public MemberDTO queryGroupMember(Integer groupId, Integer userId) {
        MemberDTO memberDTO = gitlabServiceClient.queryGroupMember(
                groupId, userId).getBody();
        if (memberDTO.getId() == null) {
            return null;
        }
        return memberDTO;
    }

    public void deleteGroupMember(Integer groupId, Integer userId) {
        gitlabServiceClient.deleteMember(groupId, userId);
    }

    public int createGroupMember(Integer groupId, MemberDTO memberDTO) {
        return gitlabServiceClient.createGroupMember(groupId, memberDTO).getStatusCodeValue();
    }


    public void updateGroupMember(Integer groupId, MemberDTO memberDTO) {
        gitlabServiceClient.updateGroupMember(groupId, memberDTO);
    }


    public void createProjectVariable(Integer gitlabProjectId, String key, String value, Boolean protecteds, Integer userId) {
        GitlabTransferDTO gitlabTransferDTO = new GitlabTransferDTO();
        gitlabTransferDTO.setKey(key);
        gitlabTransferDTO.setValue(value);
        gitlabServiceClient.addProjectVariable(gitlabProjectId, gitlabTransferDTO, protecteds, userId);
    }

    public List<CiVariableVO> batchSaveGroupVariable(Integer gitlabGroupId, Integer userId, List<CiVariableVO> variableVOS) {
        try {
            return gitlabServiceClient.batchSaveGroupVariable(gitlabGroupId, userId, variableVOS).getBody();
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }

    public List<CiVariableVO> batchSaveProjectVariable(Integer gitlabProjectId, Integer userId, List<CiVariableVO> ciVariableVOList) {
        try {
            return gitlabServiceClient.batchSaveProjectVariable(gitlabProjectId, userId, ciVariableVOList).getBody();
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }

    public void batchDeleteGroupVariable(Integer gitlabGroupId, Integer userId, List<String> keys) {
        try {
            gitlabServiceClient.batchGroupDeleteVariable(gitlabGroupId, userId, keys);
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }

    public void batchDeleteProjectVariable(Integer gitlabProjectId, Integer userId, List<String> keys) {
        try {
            gitlabServiceClient.batchProjectDeleteVariable(gitlabProjectId, userId, keys);
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }


    public List<String> listProjectToken(Integer gitlabProjectId, String name, Integer userId) {
        ResponseEntity<List<ImpersonationTokenDTO>> impersonationTokens;
        try {
            impersonationTokens = gitlabServiceClient
                    .listProjectToken(userId);
        } catch (Exception e) {
            gitUtil.deleteWorkingDirectory(name);
            gitlabServiceClient.deleteProjectById(gitlabProjectId, userId);
            throw new CommonException(e);
        }
        List<String> tokens = new ArrayList<>();
        impersonationTokens.getBody().stream().forEach(impersonationToken ->
                tokens.add(impersonationToken.getToken())
        );
        return tokens;
    }

    public String createProjectToken(Integer gitlabProjectId, String name, Integer userId) {
        ResponseEntity<ImpersonationTokenDTO> impersonationToken;
        try {
            impersonationToken = gitlabServiceClient.createProjectToken(userId);
        } catch (Exception e) {
            gitUtil.deleteWorkingDirectory(name);
            gitlabServiceClient.deleteProjectById(gitlabProjectId, userId);
            throw new CommonException(e);
        }
        return impersonationToken.getBody().getToken();
    }

    /**
     * 从gitlab项目创建access token
     *
     * @param userId 用户id
     * @return access token
     */
    @Nullable
    public String createProjectToken(Integer userId) {
        ResponseEntity<ImpersonationTokenDTO> impersonationToken;
        try {
            impersonationToken = gitlabServiceClient.createProjectToken(userId);
        } catch (Exception e) {
            return null;
        }
        return impersonationToken.getBody().getToken();
    }

    public GroupDTO queryGroupByName(String groupName, Integer userId) {
        ResponseEntity<GroupDTO> groupDTOResponseEntity;
        try {
            groupDTOResponseEntity = gitlabServiceClient.queryGroupByName(groupName, userId);
        } catch (Exception e) {
            throw new CommonException(e);
        }
        if (groupDTOResponseEntity != null) {
            return groupDTOResponseEntity.getBody();
        } else {
            return null;
        }
    }

    public GroupDTO createGroup(GroupDTO groupDTO, Integer userId) {
        ResponseEntity<GroupDTO> groupDTOResponseEntity;
        try {
            groupDTOResponseEntity = gitlabServiceClient.createGroup(groupDTO, userId);
        } catch (Exception e) {
            throw new CommonException(e);
        }
        return groupDTOResponseEntity.getBody();
    }

    public void createFile(Integer projectId, String path, String content, String commitMessage, Integer userId) {
        try {
            FileCreationVO fileCreationVO = new FileCreationVO();
            fileCreationVO.setPath(path);
            fileCreationVO.setContent(content);
            fileCreationVO.setCommitMessage(commitMessage);
            fileCreationVO.setUserId(userId);
            ResponseEntity<RepositoryFileDTO> result = gitlabServiceClient
                    .createFile(projectId, fileCreationVO);
            if (result.getBody().getFilePath() == null) {
                throw new CommonException("error.file.create");
            }
        } catch (RetryableException e) {
            LOGGER.info(e.getMessage(), e);
        }
    }


    public void createFile(Integer projectId, String path, String content, String commitMessage, Integer userId, String branch) {
        try {
            FileCreationVO fileCreationVO = new FileCreationVO();
            fileCreationVO.setPath(path);
            fileCreationVO.setContent(content);
            fileCreationVO.setCommitMessage(commitMessage);
            fileCreationVO.setUserId(userId);
            fileCreationVO.setBranchName(branch);
            ResponseEntity<RepositoryFileDTO> result = gitlabServiceClient
                    .createFile(projectId, fileCreationVO);
            if (result.getBody().getFilePath() == null) {
                throw new CommonException("error.file.create");
            }
        } catch (RetryableException e) {
            LOGGER.info(e.getMessage(), e);
        }
    }

    /**
     * 这里是更新master分支上的文件内容
     *
     * @param projectId     项目id
     * @param path          文件路径
     * @param content       文件内容
     * @param commitMessage 提交信息
     * @param userId        gitlab用户id
     */
    public void updateFile(Integer projectId, String path, String content, String commitMessage, Integer userId) {
        try {
            FileCreationVO fileCreationVO = new FileCreationVO();
            fileCreationVO.setUserId(userId);
            fileCreationVO.setPath(path);
            fileCreationVO.setContent(content);
            fileCreationVO.setCommitMessage(commitMessage);
            ResponseEntity<RepositoryFileDTO> result = gitlabServiceClient
                    .updateFile(projectId, fileCreationVO);
            if (result.getBody().getFilePath() == null) {
                throw new CommonException("error.file.update");
            }
        } catch (RetryableException e) {
            LOGGER.info(e.getMessage(), e);
        }
    }

    public void deleteFile(Integer projectId, String path, String commitMessage, Integer userId) {
        try {
            FileCreationVO fileCreationVO = new FileCreationVO();
            fileCreationVO.setPath(path);
            fileCreationVO.setCommitMessage(commitMessage);
            fileCreationVO.setUserId(userId);
            gitlabServiceClient.deleteFile(projectId, fileCreationVO);
        } catch (Exception e) {
            throw new CommonException("error.file.delete", e, path);
        }
    }

    public void deleteProjectByName(String groupName, String projectName, Integer userId) {
        try {
            gitlabServiceClient.deleteProjectByName(groupName, projectName, userId);
        } catch (Exception e) {
            throw new CommonException("error.app.delete", e);
        }
    }

    public Boolean getFile(Integer projectId, String branch, String filePath) {
        try {
            gitlabServiceClient.getFile(projectId, branch, filePath);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void createProtectBranch(Integer projectId, String name, String mergeAccessLevel, String pushAccessLevel,
                                    Integer userId) {
        try {
            GitlabTransferDTO gitlabTransferDTO = new GitlabTransferDTO();
            gitlabTransferDTO.setBranchName(name);
            gitlabTransferDTO.setMergeAccessLevel(mergeAccessLevel);
            gitlabTransferDTO.setPushAccessLevel(pushAccessLevel);
            gitlabServiceClient.createProtectedBranch(
                    projectId, gitlabTransferDTO, userId);
        } catch (Exception e) {
            throw new CommonException("error.branch.create", e);
        }
    }

    public void deleteProjectById(Integer projectId, Integer userId) {
        try {
            gitlabServiceClient.deleteProjectById(projectId, userId);
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }

    public void updateGroup(Integer projectId, Integer userId, GroupDTO groupDTO) {
        gitlabServiceClient.updateGroup(projectId, userId, groupDTO);
    }


    public ProjectHookDTO createWebHook(Integer projectId, Integer userId, ProjectHookDTO projectHookDTO) {
        try {
            return gitlabServiceClient.createProjectHook(projectId, userId, projectHookDTO).getBody();
        } catch (Exception e) {
            throw new CommonException("error.projecthook.create", e);

        }
    }

    public ProjectHookDTO updateProjectHook(Integer projectId, Integer hookId, Integer userId) {
        ResponseEntity<ProjectHookDTO> projectHookResponseEntity;
        try {
            projectHookResponseEntity = gitlabServiceClient
                    .updateProjectHook(projectId, hookId, userId);
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
        return projectHookResponseEntity.getBody();
    }

    public GitlabProjectDTO createProject(Integer groupId, String projectName, Integer userId, boolean visibility) {
        try {
            return gitlabServiceClient
                    .createProject(groupId, projectName, userId, visibility).getBody();
        } catch (Exception e) {
            throw new CommonException("error.gitlab.project.create", e);

        }
    }

    public GitlabProjectDTO queryProjectById(Integer projectId) {
        try {
            return gitlabServiceClient.queryProjectById(projectId).getBody();
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }

    public GitlabProjectDTO queryProjectByName(String groupName, String projectName, Integer userId) {
        try {
            return gitlabServiceClient.queryProjectByName(userId, groupName, projectName).getBody();
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }

    public List<ProjectHookDTO> listProjectHook(Integer projectId, Integer userId) {
        try {
            return gitlabServiceClient.listProjectHook(projectId, userId).getBody();
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }

    public List<CiVariableVO> listAppServiceVariable(Integer projectId, Integer userId) {
        try {
            return gitlabServiceClient.listAppServiceVariable(projectId, userId).getBody();
        } catch (Exception e) {
            throw new CommonException("error.devops.ci.variable.key.list");
        }
    }

    public List<CiVariableVO> listProjectVariable(Integer projectId, Integer userId) {
        try {
            return gitlabServiceClient.listProjectVariable(projectId, userId).getBody();
        } catch (Exception e) {
            throw new CommonException("error.devops.ci.variable.key.list");
        }
    }


    public List<DeployKeyDTO> listDeployKey(Integer projectId, Integer userId) {
        try {
            return gitlabServiceClient.listDeploykey(projectId, userId).getBody();
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }

    public void createDeployKey(Integer projectId, String title, String key, boolean canPush, Integer userId) {
        try {
            GitlabTransferDTO gitlabTransferDTO = new GitlabTransferDTO();
            gitlabTransferDTO.setTitle(title);
            gitlabTransferDTO.setSshKey(key);
            gitlabServiceClient.createDeploykey(projectId, gitlabTransferDTO, canPush, userId);
        } catch (Exception e) {
            throw new CommonException("error.deploykey.create", e);
        }
    }

    public void createProjectMember(Integer projectId, MemberDTO memberDTO) {
        try {
            gitlabServiceClient.createProjectMember(projectId, memberDTO);
        } catch (Exception e) {
            throw new CommonException("error.member.add", e);
        }
    }

    public void updateProjectMember(Integer projectId, List<MemberDTO> memberDTOS) {
        try {
            gitlabServiceClient.updateProjectMember(projectId, memberDTOS);
        } catch (Exception e) {
            throw new CommonException("error.member.update", e);
        }
    }

    public void deleteProjectMember(Integer groupId, Integer userId) {
        try {
            gitlabServiceClient.deleteProjectMember(groupId, userId);
        } catch (Exception e) {
            throw new CommonException("error.member.remove", e);
        }
    }


    public List<GitlabProjectDTO> listProjectByUser(Integer userId) {
        try {
            return gitlabServiceClient.listProjectByUser(userId).getBody();
        } catch (Exception e) {
            throw new CommonException("error.project.get.by.userId", e);
        }
    }


    public MergeRequestDTO createMergeRequest(Integer projectId, String sourceBranch, String targetBranch, String title, String description, Integer userId) {
        try {
            GitlabTransferDTO gitlabTransferDTO = new GitlabTransferDTO();
            gitlabTransferDTO.setSourceBranch(sourceBranch);
            gitlabTransferDTO.setTargetBranch(targetBranch);
            gitlabTransferDTO.setTitle(title);
            gitlabTransferDTO.setDescription(description);
            return gitlabServiceClient.createMergeRequest(projectId, gitlabTransferDTO, userId).getBody();
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }


    public void acceptMergeRequest(Integer projectId, Integer mergeRequestId, String mergeCommitMessage, Boolean shouldRemoveSourceBranch, Boolean mergeWhenPipelineSucceeds, Integer userId) {
        gitlabServiceClient.acceptMergeRequest(projectId, mergeRequestId, mergeCommitMessage, shouldRemoveSourceBranch, mergeWhenPipelineSucceeds, userId);
    }

    public void createTag(Integer gitLabProjectId, String tag, String ref, String msg, String releaseNotes, Integer userId) {
        try {
            if (msg == null) {
                msg = "No ReleaseNote";
            }
            if (releaseNotes == null) {
                releaseNotes = "No ReleaseNote";
            }
            GitlabTransferDTO gitlabTransferDTO = new GitlabTransferDTO();
            gitlabTransferDTO.setTagName(tag);
            gitlabTransferDTO.setRef(ref);
            gitlabTransferDTO.setMsg(msg);
            gitlabTransferDTO.setReleaseNotes(releaseNotes);
            gitlabServiceClient.createTag(gitLabProjectId, gitlabTransferDTO, userId);
        } catch (Exception e) {
            throw new CommonException("create gitlab tag failed: " + e.getMessage(), e);
        }
    }

    public TagDTO updateTag(Integer gitLabProjectId, String tag, String releaseNotes, Integer userId) {
        try {
            if (releaseNotes == null) {
                releaseNotes = "";
            }
            GitlabTransferDTO gitlabTransferDTO = new GitlabTransferDTO();
            gitlabTransferDTO.setTagName(tag);
            gitlabTransferDTO.setReleaseNotes(releaseNotes);
            return gitlabServiceClient.updateTag(gitLabProjectId, gitlabTransferDTO, userId).getBody();
        } catch (Exception e) {
            throw new CommonException("update gitlab tag failed: " + e.getMessage(), e);
        }
    }

    public void deleteTag(Integer gitLabProjectId, String tag, Integer userId) {
        try {
            gitlabServiceClient.deleteTag(gitLabProjectId, tag, userId);
        } catch (Exception e) {
            throw new CommonException("delete gitlab tag failed: " + e.getMessage(), e);
        }
    }


    public BranchDTO createBranch(Integer projectId, String branchName, String baseBranch, Integer userId) {
        ResponseEntity<BranchDTO> responseEntity;
        try {
            GitlabTransferDTO gitlabTransferDTO = new GitlabTransferDTO();
            gitlabTransferDTO.setBranchName(branchName);
            gitlabTransferDTO.setSourceBranch(baseBranch);
            responseEntity =
                    gitlabServiceClient.createBranch(projectId, gitlabTransferDTO, userId);
        } catch (Exception e) {
            throw new CommonException("error.branch.create", e);
        }
        return responseEntity.getBody();
    }

    public List<BranchDTO> listBranch(Integer projectId, String path, Integer userId) {
        ResponseEntity<List<BranchDTO>> responseEntity;
        try {
            responseEntity = gitlabServiceClient.listBranch(projectId, userId);
        } catch (Exception e) {
            throw new CommonException("error.branch.get", e);
        }
        List<BranchDTO> branches = responseEntity.getBody();
        branches.forEach(t -> t.getCommit().setUrl(
                String.format("%s/commit/%s?view=parallel", path, t.getCommit().getId())));
        return branches;
    }


    public Page<TagDTO> pageTag(ProjectDTO projectDTO, Integer gitlabProjectId, String path, Integer page, String params, Integer size, Integer userId) {
        if (!permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectDTO.getId())) {
            MemberDTO memberDTO = getProjectMember(
                    gitlabProjectId,
                    userId);
            if (memberDTO == null) {
                throw new CommonException("error.user.not.in.gitlab.project");
            }
        }

        List<TagDTO> tagTotalList = listTags(gitlabProjectId, userId);
        List<TagDTO> tagList = tagTotalList.stream()
                .filter(t -> filterTag(t, params))
                .peek(t -> {
                    if (t.getRelease() == null) {
                        ReleaseDO releaseDO = new ReleaseDO();
                        releaseDO.setTagName(t.getName());
                        t.setRelease(releaseDO);
                    }
                })
                .collect(Collectors.toCollection(ArrayList::new));

        Page<TagDTO> resp = PageInfoUtil.createPageFromList(tagList, new PageRequest(page, size));

        resp.getContent().stream()
                .sorted(this::sortTag)
                .forEach(t -> {
                    IamUserDTO userDTO = baseServiceClientOperator.queryByEmail(TypeUtil.objToLong(gitlabProjectId), t.getCommit().getAuthorEmail());
                    if (userDTO != null) {
                        t.setCommitUserImage(userDTO.getImageUrl());
                    }
                    t.getCommit().setUrl(String.format("%s/commit/%s?view=parallel", path, t.getCommit().getId()));
                });
        return resp;
    }

    private Boolean filterTag(TagDTO tagDTO, String params) {
        Integer index = 0;
        if (!StringUtils.isEmpty(params)) {
            Map<String, Object> maps = TypeUtil.castMapParams(params);
            List<String> paramList = TypeUtil.cast(maps.get(TypeUtil.PARAMS));
            if (!CollectionUtils.isEmpty(paramList)) {
                for (String param : paramList) {
                    if (tagDTO.getName().contains(param) || tagDTO.getCommit().getShortId().contains(param)
                            || tagDTO.getCommit().getCommitterName().contains(param)
                            || tagDTO.getCommit().getMessage().contains(param)) {
                        index = 1;
                        break;
                    }
                }
            }
            Map<String, Object> searchParam = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
            if (searchParam != null) {
                index = getTagName(index, tagDTO, searchParam);
                index = getShortId(index, tagDTO, searchParam);
                index = getCommitterName(index, tagDTO, searchParam);
                index = getMessage(index, tagDTO, searchParam);
            }
        } else {
            return true;
        }
        return index >= 0;
    }

    private Integer getTagName(Integer index, TagDTO tagDTO, Map<String, Object> mapSearch) {
        String tagName = "tagName";
        if (index >= 0 && mapSearch.containsKey(tagName)
                && !StringUtils.isEmpty(mapSearch.get(tagName))) {
            index = tagDTO.getName().contains(String.valueOf(mapSearch.get(tagName))) ? 1 : -1;
        }
        return index;
    }

    private Integer getShortId(Integer index, TagDTO tagDTO, Map<String, Object> mapSearch) {
        String shortId = "shortId";
        if (index >= 0 && mapSearch.containsKey(shortId)
                && !StringUtils.isEmpty(mapSearch.get(shortId))) {
            index = tagDTO.getCommit().getId()
                    .contains(String.valueOf(mapSearch.get(shortId))) ? 1 : -1;
        }
        return index;
    }

    private Integer getCommitterName(Integer index, TagDTO tagDTO, Map<String, Object> mapSearch) {
        String committerName = "committerName";
        if (index >= 0 && mapSearch.containsKey(committerName)
                && !StringUtils.isEmpty(mapSearch.get(committerName))) {
            index = tagDTO.getCommit().getCommitterName()
                    .contains(String.valueOf(mapSearch.get(committerName))) ? 1 : -1;
        }
        return index;
    }

    private Integer getMessage(Integer index, TagDTO tagDTO, Map<String, Object> mapSearch) {
        String msg = "message";
        if (index >= 0 && mapSearch.containsKey(msg)
                && !StringUtils.isEmpty(mapSearch.get(msg))) {
            index = tagDTO.getCommit().getMessage().contains(String.valueOf(mapSearch.get(msg))) ? 1 : -1;
        }
        return index;
    }

    public List<TagDTO> listTags(Integer projectId, Integer userId) {
        ResponseEntity<List<TagDTO>> tagResponseEntity;
        try {
            tagResponseEntity = gitlabServiceClient.getTags(projectId, userId);
        } catch (Exception e) {
            throw new CommonException("error.tags.get", e);
        }
        return tagResponseEntity.getBody();
    }


    public BranchDTO queryBranch(Integer gitlabProjectId, String branch) {
        try {
            return gitlabServiceClient.queryBranch(gitlabProjectId, branch).getBody();
        } catch (Exception e) {
            throw new CommonException("error.branch.get", e);

        }
    }


    public CompareResultDTO queryCompareResult(Integer gitlabProjectId, String from, String to) {
        try {
            GitlabTransferDTO gitlabTransferDTO = new GitlabTransferDTO();
            gitlabTransferDTO.setFrom(from);
            gitlabTransferDTO.setTo(to);
            return gitlabServiceClient.queryCompareResult(gitlabProjectId, gitlabTransferDTO).getBody();
        } catch (Exception e) {
            throw new CommonException("error.diffs.get", e);
        }
    }


    private Integer sortTag(TagDTO a, TagDTO b) {
        TagNodeDO tagA = TagNodeDO.tagNameToTagNode(a.getName());
        TagNodeDO tagB = TagNodeDO.tagNameToTagNode(b.getName());
        if (tagA != null && tagB != null) {
            return tagA.compareTo(tagB) * -1;
        } else if (tagA == null && tagB != null) {
            return 1;
        } else if (tagA != null) {
            return -1;
        } else {
            return a.getName().compareToIgnoreCase(b.getName());
        }
    }

    public List<CommitDTO> getCommits(Integer gitLabProjectId, String branchName, String date) {
        try {
            GitlabTransferDTO gitlabTransferDTO = new GitlabTransferDTO();
            gitlabTransferDTO.setBranchName(branchName);
            gitlabTransferDTO.setSince(date);
            return gitlabServiceClient.getCommits(gitLabProjectId, gitlabTransferDTO).getBody();
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }


    public List<BranchDTO> listBranch(Integer gitlabProjectId, Integer userId) {
        try {
            return gitlabServiceClient.listBranch(gitlabProjectId, userId).getBody();
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }


    public List<GitlabPipelineDTO> listPipeline(Integer projectId, Integer userId) {
        ResponseEntity<List<GitlabPipelineDTO>> responseEntity;
        try {
            responseEntity = gitlabServiceClient.listPipeline(projectId, userId);
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return responseEntity.getBody();
    }


    public List<GitlabPipelineDTO> pagePipeline(Integer projectId, Integer page, Integer size, Integer userId) {
        ResponseEntity<List<GitlabPipelineDTO>> responseEntity;
        try {
            responseEntity =
                    gitlabServiceClient.pagePipeline(projectId, page, size, userId);
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return responseEntity.getBody();
    }


    public GitlabPipelineDTO queryPipeline(Integer projectId, Integer pipelineId, Integer userId) {
        ResponseEntity<GitlabPipelineDTO> responseEntity;
        try {
            responseEntity = gitlabServiceClient.queryPipeline(projectId, pipelineId, userId);
        } catch (Exception e) {
            throw new CommonException(e);
        }
        return responseEntity.getBody();
    }


    public CommitDTO queryCommit(Integer projectId, String sha, Integer userId) {
        ResponseEntity<CommitDTO> responseEntity;
        try {
            responseEntity = gitlabServiceClient.queryCommit(projectId, sha, userId);
        } catch (Exception e) {
            return null;
        }
        return responseEntity.getBody();
    }


    public List<JobDTO> listJobs(Integer projectId, Integer pipelineId, Integer userId) {
        ResponseEntity<List<JobDTO>> responseEntity;
        try {
            responseEntity = gitlabServiceClient.listJobs(Objects.requireNonNull(projectId),
                    Objects.requireNonNull(pipelineId), userId);
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return responseEntity.getBody();
    }


    public Pipeline retryPipeline(Integer projectId, Integer pipelineId, Integer userId) {
        ResponseEntity<Pipeline> pipeline;
        try {
            pipeline = gitlabServiceClient.retryPipeline(projectId, pipelineId, userId);
        } catch (Exception e) {
            throw new CommonException(ERROR_RETRY_PIPELINE_FILED);
        }
        return pipeline.getBody();
    }


    public Pipeline cancelPipeline(Integer projectId, Integer pipelineId, Integer userId) {
        ResponseEntity<Pipeline> pipeline;
        try {
            pipeline = gitlabServiceClient.cancelPipeline(projectId, pipelineId, userId);
        } catch (Exception e) {
            throw new CommonException(ERROR_CANCEL_PIPELINE_FILED);
        }
        return pipeline.getBody();
    }


    public List<CommitStatusDTO> listCommitStatus(Integer projectId, String sha, Integer useId) {
        ResponseEntity<List<CommitStatusDTO>> commitStatuse;
        try {
            commitStatuse = gitlabServiceClient.listCommitStatus(projectId, sha, useId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
        return commitStatuse.getBody();
    }


    public List<CommitDTO> listCommits(Integer projectId, Integer userId, Integer page, Integer size) {
        try {
            List<CommitDTO> commitDTOS = new LinkedList<>();
            commitDTOS.addAll(gitlabServiceClient.listCommits(projectId, page, size, userId).getBody());
            return commitDTOS;
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    public List<CommitDTO> listCommits(Integer projectId, Integer mergeRequestId, Integer userId) {
        try {
            List<CommitDTO> commitDTOS = new LinkedList<>();
            ResponseEntity<List<CommitDTO>> responseEntity = gitlabServiceClient.listCommits(projectId, mergeRequestId, userId);
            if (responseEntity.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return null;
            } else {
                commitDTOS.addAll(responseEntity.getBody());
                return commitDTOS;
            }
        } catch (Exception e) {
            if (FeignResponseStatusCodeParse.parseStatusCode(e.getMessage()) == 404) {
                return null;
            }
            throw new CommonException(e.getMessage(), e);
        }
    }


    public MemberDTO getProjectMember(Integer projectId, Integer userId) {

        MemberDTO memberDTO = gitlabServiceClient.getProjectMember(
                projectId, userId).getBody();
        if (memberDTO.getId() == null) {
            return null;
        }
        return memberDTO;
    }


    public void deleteBranch(Integer projectId, String branchName, Integer userId) {
        try {
            gitlabServiceClient.deleteBranch(projectId, branchName, userId);
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }

    public List<MemberDTO> listMemberByProject(Integer projectId) {
        try {
            return gitlabServiceClient.listMemberByProject(projectId).getBody();
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }

    public String getAdminToken() {
        try {
            return gitlabServiceClient.getAdminToken().getBody();
        } catch (Exception e) {
            throw new CommonException(e);
        }

    }

    /**
     * 为一个已经是admin的gitlab用户再设置admin也不会报错且返回的是正常的false，
     * 所以没有在对用户赋予admin权限前判断他是不是admin
     *
     * @param iamUserId    iamUserId
     * @param gitlabUserId gitlabUserId
     */
    public void assignAdmin(Long iamUserId, Integer gitlabUserId) {
        Boolean result;
        try {
            ResponseEntity<Boolean> responseEntity = gitlabServiceClient.assignAdmin(Objects.requireNonNull(gitlabUserId));
            result = responseEntity == null ? Boolean.FALSE : responseEntity.getBody();
        } catch (Exception e) {
            throw new CommonException(e);
        }

        if (!Boolean.TRUE.equals(result)) {
            throw new CommonException("failed.to.set.user.gitlab.admin", Objects.requireNonNull(iamUserId));
        }
    }

    public void deleteAdmin(Long iamUserId, Integer gitlabUserId) {
        Boolean result;

        try {
            ResponseEntity<Boolean> responseEntity = gitlabServiceClient.deleteAdmin(Objects.requireNonNull(gitlabUserId));
            result = responseEntity == null ? Boolean.FALSE : responseEntity.getBody();
        } catch (Exception e) {
            throw new CommonException(e);
        }

        if (!Boolean.TRUE.equals(result)) {
            throw new CommonException("failed.to.delete.user.gitlab.admin", Objects.requireNonNull(iamUserId));
        }
    }

    /**
     * 用户是否是gitlab的admin
     *
     * @param gitlabUserId gitlab用户id
     * @return true表明是
     */
    public boolean isGitlabAdmin(Integer gitlabUserId) {
        try {
            ResponseEntity<Boolean> responseEntity = gitlabServiceClient.checkIsAdmin(Objects.requireNonNull(gitlabUserId));
            if (responseEntity == null) {
                return false;
            }
            if (responseEntity.getBody() == null) {
                return false;
            }
            return responseEntity.getBody();
        } catch (Exception e) {
            LOGGER.info("Error occurred when check whether the user with gitlab id {} is root...", gitlabUserId);
            LOGGER.info("The exception is: ", e);
            return false;
        }
    }

    public List<AccessRequestDTO> listAccessRequestsOfGroup(Integer groupId) {
        try {
            ResponseEntity<List<AccessRequestDTO>> resp = gitlabServiceClient.listAccessRequestsOfGroup(Objects.requireNonNull(groupId));
            if (resp == null || resp.getBody() == null) {
                return Collections.emptyList();
            }
            return resp.getBody();
        } catch (Exception ex) {
            throw new CommonException(ex);
        }
    }

    /**
     * 拒绝AccessRequest
     *
     * @param groupId          组id
     * @param userIdToBeDenied 被拒绝的用户的id
     */
    public void denyAccessRequest(Integer groupId, Integer userIdToBeDenied) {
        try {
            gitlabServiceClient.denyAccessRequest(Objects.requireNonNull(groupId), Objects.requireNonNull(userIdToBeDenied));
        } catch (Exception e) {
            LOGGER.info("Swallow exception when denying access request of group id {}, userIdToBeDenied {}", groupId, userIdToBeDenied);
            LOGGER.info("The exception is: ", e);
        }
    }

    /**
     * 拒绝这些用户在组里的AccessRequest
     * 用户对组AccessRequest存在会导致通过API给这个用户分配项目的Member失败 (Gitlab版本11.6.5 (237bddc6))
     *
     * @param groupId 组id
     * @param users   用户
     */
    public void denyAllAccessRequestInvolved(Integer groupId, List<UserAttrDTO> users) {
        // 查出组里的AccessRequest
        Map<Integer, AccessRequestDTO> allRequests = listAccessRequestsOfGroup(groupId).stream().collect(Collectors.toMap(AccessRequestDTO::getId, Functions.identity()));

        Objects.requireNonNull(users).forEach(user -> {
            if (user == null) {
                return;
            }
            Integer gitlabUserId = TypeUtil.objToInteger(user.getGitlabUserId());
            if (allRequests.containsKey(gitlabUserId)) {
                denyAccessRequest(groupId, gitlabUserId);
            }
        });
    }

    /**
     * 拒绝这些用户在组里的AccessRequest
     * 用户对组AccessRequest存在会导致通过API给这个用户分配项目的Member失败 (Gitlab版本11.6.5 (237bddc6))
     *
     * @param groupId       组id
     * @param gitlabUserIds 用户gitlab id
     */
    public void denyAllAccessRequestInvolved(List<Integer> gitlabUserIds, Integer groupId) {
        // 查出组里的AccessRequest
        Map<Integer, AccessRequestDTO> allRequests = listAccessRequestsOfGroup(groupId).stream().collect(Collectors.toMap(AccessRequestDTO::getId, Functions.identity()));

        Objects.requireNonNull(gitlabUserIds).forEach(gitlabUserId -> {
            if (gitlabUserId == null) {
                return;
            }
            if (allRequests.containsKey(gitlabUserId)) {
                denyAccessRequest(groupId, gitlabUserId);
            }
        });
    }

    /**
     * 创建gitlab文件并创建提交
     *
     * @param gitlabProjectId gitlab项目id
     * @param gitlabUserId    用户id
     * @param branch          分支名
     * @param pathContent     文件路径和内容的映射，不能为空
     * @param commitMessage   提交信息
     */
    public void createGitlabFiles(Integer gitlabProjectId, Integer gitlabUserId, String branch, Map<String, String> pathContent, String commitMessage) {
        try {
            List<CommitActionDTO> actions = new ArrayList<>();
            pathContent.forEach((filePath, fileContent) -> actions.add(new CommitActionDTO(CommitActionDTO.Action.CREATE, filePath, fileContent)));
            CommitPayloadDTO commitPayloadDTO = new CommitPayloadDTO(Objects.requireNonNull(branch), Objects.requireNonNull(commitMessage), actions);
            gitlabServiceClient.createCommit(Objects.requireNonNull(gitlabProjectId), Objects.requireNonNull(gitlabUserId), commitPayloadDTO);
        } catch (Exception ex) {
            throw new CommonException("error.manipulate.gitlab.files");
        }
    }

    public RepositoryFileDTO getWholeFile(Integer projectId, String branch, String filePath) {
        try {
            return gitlabServiceClient.getFile(projectId, branch, filePath).getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public Pipeline createPipeline(int projectId, int gitlabUserid, String ref) {
        ResponseEntity<Pipeline> pipeline;
        try {
            pipeline = gitlabServiceClient.createPipeline(projectId, gitlabUserid, ref);
        } catch (Exception e) {
            throw new CommonException(ERROR_CREATE_PIPELINE_FILED);
        }
        return pipeline.getBody();
    }

    public String queryTrace(int gitlabProjectId, int jobId, int gitlabUserid) {
        return gitlabServiceClient.queryTrace(gitlabProjectId, jobId, gitlabUserid).getBody();
    }

    public JobDTO retryJob(int gitlabProjectId, int jobId, int gitlabUserId) {
        return gitlabServiceClient.retryJob(gitlabProjectId, jobId, gitlabUserId).getBody();
    }

    public BranchDTO getBranch(int gitlabProjectId, String ref) {
        return gitlabServiceClient.queryBranchByName(gitlabProjectId, ref).getBody();
    }

    public MemberDTO getMember(Long gitlabProjectId, Long gitlabUserId) {
        return gitlabServiceClient.getProjectMember(gitlabProjectId.intValue(), gitlabUserId.intValue()).getBody();
    }

    public List<Long> listMergeRequestIds(Integer gitlabProjectId) {
        return gitlabServiceClient.listIds(gitlabProjectId).getBody();
    }
}
