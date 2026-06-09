GP-月薪喵 - Salary Cat
====================

月薪喵是一个在桌面上播放猫猫动画 + 背景音乐的 Java Swing 小应用。


目录结构
--------
  salaryCat/
  ├── src/
  │   ├── SalaryCatApp.java      # 入口
  │   ├── SalaryCatFrame.java    # 主窗口（GIF 播放 + UI）
  │   └── AudioPlayer.java       # 音频播放（WAV）
  ├── cat.GIF                    # 猫猫动画
  ├── music.wav                  # 背景音乐（WAV 格式）
  ├── run.bat                    # Windows 运行脚本
  ├── run.sh                     # Linux/macOS 运行脚本
  └── README.txt


前置条件
--------
- JDK 17+（Java 17 及以上）
- javac 和 java 命令已添加到系统 PATH


运行方式
--------

  Windows:
      run.bat

  Linux / macOS:
      chmod +x run.sh
      ./run.sh

  手动编译运行:
      mkdir -p bin
      javac -d bin -encoding UTF-8 src/*.java
      java -cp bin SalaryCatApp


说明
----
- 打开即自动播放动画与音乐
- 窗口全屏最大化，动画自动居中并等比缩放
- GIF 采用预缩放缓存 + 精确帧调度，播放流畅
- 关闭窗口自动结束音乐与帧线程
