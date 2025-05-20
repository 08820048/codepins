package cn.ilikexff.codepins.utils;

import cn.ilikexff.codepins.PinEntry;
import cn.ilikexff.codepins.PinState;
import cn.ilikexff.codepins.PinStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.FileDocumentManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 导入导出工具类
 * 用于导出和导入图钉数据
 */
public class ImportExportUtil {

    // 导出文件版本号
    private static final String EXPORT_VERSION = "1.0";

    /**
     * 导出图钉数据到文件
     *
     * @param project 当前项目
     * @param file 导出文件
     * @param pins 要导出的图钉列表
     * @return 是否导出成功
     */
    public static boolean exportPins(Project project, File file, List<PinEntry> pins) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // 创建导出数据对象
            JsonObject exportData = new JsonObject();
            exportData.addProperty("version", EXPORT_VERSION);
            exportData.addProperty("exportTime", System.currentTimeMillis());
            exportData.addProperty("projectName", project.getName());

            // 添加图钉数据
            JsonArray pinsArray = new JsonArray();
            for (PinEntry pin : pins) {
                JsonObject pinObject = new JsonObject();

                // 基本信息
                pinObject.addProperty("filePath", pin.filePath);
                pinObject.addProperty("line", pin.getCurrentLine(pin.marker.getDocument()));
                pinObject.addProperty("note", pin.note != null ? pin.note : "");
                pinObject.addProperty("timestamp", pin.timestamp);
                pinObject.addProperty("author", pin.author);
                pinObject.addProperty("isBlock", pin.isBlock);

                // 如果是代码块，添加偏移量
                if (pin.isBlock) {
                    pinObject.addProperty("startOffset", pin.marker.getStartOffset());
                    pinObject.addProperty("endOffset", pin.marker.getEndOffset());
                }

                // 添加标签
                JsonArray tagsArray = new JsonArray();
                for (String tag : pin.getTags()) {
                    tagsArray.add(tag);
                }
                pinObject.add("tags", tagsArray);

                pinsArray.add(pinObject);
            }
            exportData.add("pins", pinsArray);

            // 使用 Gson 格式化 JSON
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonOutput = gson.toJson(exportData);

            // 写入文件
            writer.write(jsonOutput);
            return true;
        } catch (IOException e) {
            Messages.showErrorDialog(
                    project,
                    "导出图钉数据失败: " + e.getMessage(),
                    "导出错误"
            );
            return false;
        }
    }

    /**
     * 从文件导入图钉数据
     *
     * @param project 当前项目
     * @param file 导入文件
     * @param mode 导入模式（合并或替换）
     * @return 导入的图钉数量，-1 表示导入失败
     */
    public static int importPins(Project project, File file, ImportMode mode) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // 解析 JSON 数据
            JsonObject importData = JsonParser.parseReader(reader).getAsJsonObject();

            // 检查版本兼容性
            String version = importData.get("version").getAsString();
            if (!isVersionCompatible(version)) {
                Messages.showErrorDialog(
                        project,
                        "导入文件版本 (" + version + ") 与当前版本不兼容",
                        "版本不兼容"
                );
                return -1;
            }

            // 如果是替换模式，先清空现有图钉
            if (mode == ImportMode.REPLACE) {
                PinStorage.clearAll();
            }

            // 导入图钉数据
            JsonArray pinsArray = importData.getAsJsonArray("pins");
            int importCount = 0;

            for (int i = 0; i < pinsArray.size(); i++) {
                JsonObject pinObject = pinsArray.get(i).getAsJsonObject();

                // 解析基本信息
                String filePath = pinObject.get("filePath").getAsString();
                int line = pinObject.get("line").getAsInt();
                String note = pinObject.has("note") ? pinObject.get("note").getAsString() : "";
                long timestamp = pinObject.get("timestamp").getAsLong();
                String author = pinObject.get("author").getAsString();
                boolean isBlock = pinObject.get("isBlock").getAsBoolean();

                // 解析标签
                List<String> tags = new ArrayList<>();
                if (pinObject.has("tags")) {
                    JsonArray tagsArray = pinObject.getAsJsonArray("tags");
                    for (int j = 0; j < tagsArray.size(); j++) {
                        tags.add(tagsArray.get(j).getAsString());
                    }
                }

                // 创建 PinState 对象
                PinState pinState;
                if (isBlock && pinObject.has("startOffset") && pinObject.has("endOffset")) {
                    int startOffset = pinObject.get("startOffset").getAsInt();
                    int endOffset = pinObject.get("endOffset").getAsInt();
                    pinState = new PinState(filePath, line, note, timestamp, author, isBlock, startOffset, endOffset, tags);
                } else {
                    pinState = new PinState(filePath, line, note, timestamp, author, isBlock, tags);
                }

                // 添加到存储
                PinStorage.addPinState(pinState);
                importCount++;
            }

            return importCount;
        } catch (Exception e) {
            Messages.showErrorDialog(
                    project,
                    "导入图钉数据失败: " + e.getMessage(),
                    "导入错误"
            );
            return -1;
        }
    }

    /**
     * 检查版本兼容性
     *
     * @param version 导入文件的版本号
     * @return 是否兼容
     */
    private static boolean isVersionCompatible(String version) {
        // 目前只支持 1.0 版本
        return version.equals(EXPORT_VERSION);
    }

    /**
     * 导入模式枚举
     */
    public enum ImportMode {
        /**
         * 合并模式：保留现有图钉，添加导入的图钉
         */
        MERGE,

        /**
         * 替换模式：清空现有图钉，只保留导入的图钉
         */
        REPLACE
    }
}
