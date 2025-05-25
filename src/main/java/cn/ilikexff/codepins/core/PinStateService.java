package cn.ilikexff.codepins.core;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 插件持久化服务类，实现 PersistentStateComponent 接口。
 * 该类用于保存和加载所有图钉的状态信息。
 */
@State(
        name = "CodePinsStorage",
        storages = @Storage("codepins.xml")
)
public class PinStateService implements PersistentStateComponent<PinStateService.State> {

    /**
     * 内部状态类：用于表示插件需要保存的所有数据结构。
     * 在这里，pins 保存了所有的图钉。
     */
    public static class State {
        public List<PinState> pins = new ArrayList<>();
        public Set<String> globalTags = new HashSet<>(); // 添加全局标签集合
    }

    // 当前插件的持久化状态
    private final State state = new State();

    /**
     * 返回当前的持久化状态（保存数据）
     */
    @Override
    public @Nullable State getState() {
        return state;
    }

    /**
     * 加载已保存的状态（恢复数据）
     */
    @Override
    public void loadState(@NotNull State loadedState) {
        state.pins = loadedState.pins;
        state.globalTags = loadedState.globalTags;
    }

    /**
     * 获取插件服务的全局单例实例
     */
    public static PinStateService getInstance() {
        return ApplicationManager.getApplication().getService(PinStateService.class);
    }

    /**
     * 获取当前所有保存的图钉状态
     */
    public List<PinState> getPins() {
        return state.pins;
    }

    /**
     * 添加图钉（支持 PinEntry 格式，会转换为 PinState）
     */
    public void addPin(PinEntry entry) {
        if (entry.isBlock) {
            state.pins.add(new PinState(
                    entry.filePath,
                    entry.getCurrentLine(entry.marker.getDocument()),
                    entry.note,
                    entry.timestamp,
                    entry.author,
                    entry.isBlock,
                    entry.marker.getStartOffset(),
                    entry.marker.getEndOffset(),
                    entry.getTags()
            ));
        } else {
            state.pins.add(new PinState(
                    entry.filePath,
                    entry.getCurrentLine(entry.marker.getDocument()),
                    entry.note,
                    entry.timestamp,
                    entry.author,
                    entry.isBlock,
                    entry.getTags()
            ));
        }
    }

    /**
     * 添加图钉（直接使用 PinState 格式，适合更灵活调用）
     */
    public void addPin(PinState pin) {
        state.pins.add(pin);
    }

    /**
     * 清除所有图钉
     */
    public void clear() {
        state.pins.clear();
    }

    /**
     * 添加全局标签
     * @param tag 标签
     */
    public void addGlobalTag(String tag) {
        if (tag != null && !tag.trim().isEmpty()) {
            state.globalTags.add(tag.trim());
        }
    }

    /**
     * 获取全局标签
     * @return 全局标签集合的副本
     */
    public Set<String> getGlobalTags() {
        return new HashSet<>(state.globalTags);
    }
}
