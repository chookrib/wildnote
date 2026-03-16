package cool.done.wildnote.server.application;

/**
 * 扩展日志类型
 */
public enum ExtraLogType {
    NOTE("note.log", "note"),
    REMIND("remind.log", "remind"),
    SMS("sms.log", "sms");

    private String filename;
    private String name;

    public String getFilename() { return filename; }

    public String getName() { return name; }

    ExtraLogType(String filename, String name) {
        this.filename = filename;
        this.name = name;
    }

    /**
     * 解析日志类型
     */
    public static ExtraLogType getByName(String name) {
        for (ExtraLogType type : values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        //throw new IllegalArgumentException("未知的日志类型: " + name);
        throw new ApplicationException(String.format("未知的日志类型: %s", name));
    }
}
