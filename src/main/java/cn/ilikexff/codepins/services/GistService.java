package cn.ilikexff.codepins.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * GitHub Gist服务
 * 用于创建和管理GitHub Gist
 */
@Service
@State(
        name = "GistService",
        storages = {@Storage("codepins-gist-settings.xml")}
)
public final class GistService implements PersistentStateComponent<GistService.State> {

    private State myState = new State();

    /**
     * 获取实例
     *
     * @return GistService实例
     */
    public static GistService getInstance() {
        return ApplicationManager.getApplication().getService(GistService.class);
    }

    /**
     * 创建Gist
     *
     * @param content     内容
     * @param description 描述
     * @param filename    文件名
     * @return Gist URL
     */
    public String createGist(String content, String description, String filename) {
        try {
            // 检查是否有GitHub令牌
            if (myState.githubToken == null || myState.githubToken.isEmpty()) {
                return null;
            }

            // 创建GitHub客户端
            GitHub github = new GitHubBuilder().withOAuthToken(myState.githubToken).build();

            // 创建Gist
            GHGist gist = github.createGist()
                    .description(description)
                    .file(filename, content)
                    .public_(true)
                    .create();

            // 返回Gist URL
            return gist.getHtmlUrl().toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 设置GitHub令牌
     *
     * @param token GitHub令牌
     */
    public void setGithubToken(String token) {
        myState.githubToken = token;
    }

    /**
     * 获取GitHub令牌
     *
     * @return GitHub令牌
     */
    public String getGithubToken() {
        return myState.githubToken;
    }

    /**
     * 检查是否已配置GitHub令牌
     *
     * @return 是否已配置
     */
    public boolean isConfigured() {
        return myState.githubToken != null && !myState.githubToken.isEmpty();
    }

    @Override
    public @Nullable State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        XmlSerializerUtil.copyBean(state, myState);
    }

    /**
     * 持久化状态
     */
    public static class State {
        public String githubToken;
    }
}
