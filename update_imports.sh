#!/bin/bash

# 更新所有 Java 文件中的导入语句
find /Users/xuyi/Documents/CodePins/CodePins/src/main/java -name "*.java" -type f -exec sed -i '' 's/import cn.ilikexff.codepins.PinEntry;/import cn.ilikexff.codepins.core.PinEntry;/g' {} \;
find /Users/xuyi/Documents/CodePins/CodePins/src/main/java -name "*.java" -type f -exec sed -i '' 's/import cn.ilikexff.codepins.PinStorage;/import cn.ilikexff.codepins.core.PinStorage;/g' {} \;
find /Users/xuyi/Documents/CodePins/CodePins/src/main/java -name "*.java" -type f -exec sed -i '' 's/import cn.ilikexff.codepins.PinState;/import cn.ilikexff.codepins.core.PinState;/g' {} \;
find /Users/xuyi/Documents/CodePins/CodePins/src/main/java -name "*.java" -type f -exec sed -i '' 's/import cn.ilikexff.codepins.PinStateService;/import cn.ilikexff.codepins.core.PinStateService;/g' {} \;

echo "导入语句更新完成"
