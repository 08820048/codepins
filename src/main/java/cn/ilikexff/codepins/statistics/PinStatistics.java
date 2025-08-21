package cn.ilikexff.codepins.statistics;

import cn.ilikexff.codepins.core.PinEntry;
import cn.ilikexff.codepins.core.PinStorage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 图钉统计数据计算类
 * 提供各种维度的图钉统计信息
 */
public class PinStatistics {
    
    /**
     * 获取总体统计信息
     */
    public static OverallStats getOverallStats() {
        List<PinEntry> pins = PinStorage.getPins();
        
        int totalPins = pins.size();
        int singleLinePins = (int) pins.stream().filter(pin -> !pin.isBlock).count();
        int blockPins = (int) pins.stream().filter(pin -> pin.isBlock).count();
        
        Set<String> uniqueFiles = pins.stream()
                .map(pin -> pin.filePath)
                .collect(Collectors.toSet());
        
        Set<String> uniqueAuthors = pins.stream()
                .map(pin -> pin.author)
                .collect(Collectors.toSet());
        
        Set<String> allTags = pins.stream()
                .flatMap(pin -> pin.getTags().stream())
                .collect(Collectors.toSet());
        
        return new OverallStats(totalPins, singleLinePins, blockPins, 
                               uniqueFiles.size(), uniqueAuthors.size(), allTags.size());
    }
    
    /**
     * 获取标签使用统计
     */
    public static Map<String, Integer> getTagStats() {
        List<PinEntry> pins = PinStorage.getPins();
        Map<String, Integer> tagCounts = new HashMap<>();
        
        for (PinEntry pin : pins) {
            for (String tag : pin.getTags()) {
                tagCounts.put(tag, tagCounts.getOrDefault(tag, 0) + 1);
            }
        }
        
        // 按使用频率排序
        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();
        tagCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> sortedMap.put(entry.getKey(), entry.getValue()));
        return sortedMap;
    }
    
    /**
     * 获取文件分布统计
     */
    public static Map<String, Integer> getFileStats() {
        List<PinEntry> pins = PinStorage.getPins();
        Map<String, Integer> fileCounts = new HashMap<>();
        
        for (PinEntry pin : pins) {
            String fileName = new File(pin.filePath).getName();
            fileCounts.put(fileName, fileCounts.getOrDefault(fileName, 0) + 1);
        }
        
        // 按图钉数量排序
        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();
        fileCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> sortedMap.put(entry.getKey(), entry.getValue()));
        return sortedMap;
    }
    
    /**
     * 获取作者统计
     */
    public static Map<String, Integer> getAuthorStats() {
        List<PinEntry> pins = PinStorage.getPins();
        Map<String, Integer> authorCounts = new HashMap<>();
        
        for (PinEntry pin : pins) {
            String author = pin.author != null ? pin.author : "Unknown";
            authorCounts.put(author, authorCounts.getOrDefault(author, 0) + 1);
        }
        
        // 按图钉数量排序
        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();
        authorCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> sortedMap.put(entry.getKey(), entry.getValue()));
        return sortedMap;
    }
    
    /**
     * 获取时间分布统计（按天）
     */
    public static Map<String, Integer> getTimeStats() {
        List<PinEntry> pins = PinStorage.getPins();
        Map<String, Integer> timeCounts = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        for (PinEntry pin : pins) {
            String date = dateFormat.format(new Date(pin.timestamp));
            timeCounts.put(date, timeCounts.getOrDefault(date, 0) + 1);
        }
        
        // 按日期排序
        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();
        timeCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> sortedMap.put(entry.getKey(), entry.getValue()));
        return sortedMap;
    }
    
    /**
     * 总体统计数据类
     */
    public static class OverallStats {
        public final int totalPins;
        public final int singleLinePins;
        public final int blockPins;
        public final int uniqueFiles;
        public final int uniqueAuthors;
        public final int uniqueTags;
        
        public OverallStats(int totalPins, int singleLinePins, int blockPins, 
                           int uniqueFiles, int uniqueAuthors, int uniqueTags) {
            this.totalPins = totalPins;
            this.singleLinePins = singleLinePins;
            this.blockPins = blockPins;
            this.uniqueFiles = uniqueFiles;
            this.uniqueAuthors = uniqueAuthors;
            this.uniqueTags = uniqueTags;
        }
    }
}
