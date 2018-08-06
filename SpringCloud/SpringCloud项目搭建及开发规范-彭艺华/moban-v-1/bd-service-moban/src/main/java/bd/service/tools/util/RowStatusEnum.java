package bd.service.tools.util;

/**
 * 某一行的启用状态枚举
 *
 * @author cipher
 */
public enum RowStatusEnum {

    ENABLE("启用", "启用"),

    DISABLE("停用", "停用"),
	
	DELETE("已删除", "已删除");

    private String key;

    private String value;

    RowStatusEnum(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getkey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public static RowStatusEnum fromValue(String value) {
        if (value != null && value.length() > 0) {
            for (RowStatusEnum temp : RowStatusEnum.values()) {
                if (temp.getValue().equals(value)) {
                    return temp;
                }
            }
        }
        throw new RuntimeException("type enum value error");
    }

}
