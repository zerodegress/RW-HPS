package com.github.dr.rwserver.plugin;

import com.github.dr.rwserver.util.IsUtil;
import com.github.dr.rwserver.util.alone.Switch;

/**
 * [语义化版本](https://semver.org/lang/zh-CN/) 支持
 *
 * ### 解析示例
 *
 * `1.0.0-M1+c000000a` 将会解析出下面的内容,
 * [major] (主本号), [minor] (次版本号), [patch] (修订号), [identifier] (先行版本号) 和 [metadata] (元数据).
 * ```
 * SemVersion(
 *   major = 1,
 *   minor = 0,
 *   patch = 0,
 *   identifier  = "M1"
 *   metadata    = "c000000a"
 * )
 * ```
 * 其中 identifier 和 metadata 都是可选的.
 *
 * 对于核心版本号, 此实现稍微比语义化版本规范宽松一些, 允许 x.y 的存在.
 * @author Dr
 */
public class GetVersion {
    /**
     * 主版本号
     */
    public final int major;
    /**
     * 次版本号
     */
    public final int minor;
    /**
     * 修订号
     */
    public final int patch;
    /**
     * 先行版本号识别符
     */
    public final String identifier;
    /**
     * 版本号元数据, 不参与版本号对比([compareTo]), 但是参与版本号严格对比([equals])
     */
    public final String metadata;
    /**
     * 解析一个版本号, 将会返回一个 [SemVersion],
     * 如果发生解析错误将会抛出一个 [IllegalArgumentException] 或者 [NumberFormatException]
     *
     * 对于版本号的组成, 有以下规定:
     * - 必须包含主版本号和次版本号
     * - 存在 先行版本号 的时候 先行版本号 不能为空
     * - 存在 元数据 的时候 元数据 不能为空
     * - 核心版本号只允许 `x.y` 和`x.y.z` 的存在
     *     - `1.0-RC` 是合法的
     *     - `1.0.0-RC` 也是合法的, 与 `1.0-RC` 一样
     *     - `1.0.0.0-RC` 是不合法的, 将会抛出一个 [IllegalArgumentException]
     *
     * 注意情况:
     * - 第一个 `+` 之后的所有内容全部识别为元数据
     *     - `1.0+METADATA-M4`, metadata="METADATA-M4"
     */
    public GetVersion(String version) {
        String[] version1Arr = version.split("\\.");

        this.major = Integer.parseInt(version1Arr[0]);

        if (version1Arr.length > 2) {
            this.minor = Integer.parseInt(version1Arr[1]);
            String[] version2Arr = version1Arr[2].split("-");
            if (IsUtil.isNumeric(version2Arr[0])) {
                this.patch = Integer.parseInt(version2Arr[0]);
                if (version2Arr.length > 1) {
                    String[] version3Arr = version2Arr[1].split("\\+");
                    if (version3Arr.length > 1) {
                        this.identifier = version3Arr[0];
                        this.metadata = version3Arr[1];
                    } else {
                        this.identifier = version3Arr[0];
                        this.metadata = "";
                    }
                } else {
                    /*
                      version1Arr[0] . version1Arr[1] . version2Arr[0] - "" + ""
                     */
                    this.identifier = "";
                    this.metadata = "";
                }
            } else {
                this.patch = 0;
                String[] version3Arr = version2Arr[0].split("\\+");
                if (version3Arr.length > 1) {
                    this.identifier = version3Arr[0];
                    this.metadata = version3Arr[1];
                } else {
                    this.identifier = version3Arr[0];
                    this.metadata = "";
                }
            }
        } else {
            String[] version2Arr = version1Arr[1].split("-");
            this.patch = 0;
            if (IsUtil.isNumeric(version2Arr[0])) {
                this.minor = Integer.parseInt(version2Arr[0]);
                if (version2Arr.length > 1) {
                    String[] version3Arr = version2Arr[1].split("\\+");
                    if (version3Arr.length > 1) {
                        this.identifier = version3Arr[0];
                        this.metadata = version3Arr[1];
                    } else {
                        this.identifier = version3Arr[0];
                        this.metadata = "";
                    }
                } else {
                    /*
                      version1Arr[0] . version1Arr[1] . version2Arr[0] - "" + ""
                     */
                    this.identifier = "";
                    this.metadata = "";
                }
            } else {
                this.minor = 0;
                String[] version3Arr = version2Arr[0].split("\\+");
                if (version3Arr.length > 1) {
                    this.identifier = version3Arr[0];
                    this.metadata = version3Arr[1];
                } else {
                    this.identifier = version3Arr[0];
                    this.metadata = "";
                }
            }
        }
    }

    /**
     * 1.0 -> 1000
     * 1.0.0 -> 1000
     * 1.0.0-DEV -> 1000.001
     * 1.0.0-M1 -> 1000.01
     * 1.0.0-RC -> 1001
     * @return
     */
    public int toMainInt() {
        return major*1000 + minor * 100 + patch * 10;
    }

    public double getVersion() {
        double version = toMainInt();
        final String identifierUpCase = identifier.toUpperCase();
        if (identifierUpCase.contains("M")) {
            version += Integer.parseInt(identifierUpCase.replace("M","")) * 0.01;
        } else if (identifierUpCase.contains("RC")){
            version += 1;
        } else if (identifierUpCase.contains("DEV")){
            version += Integer.parseInt(identifierUpCase.replace("DEV","")) * 0.001;
        }
        return version;
    }

    public boolean getIfVersion(String version) {
        if (IsUtil.isBlank(version)) {
            return false;
        }
        return new DemandChainDescription(version).getIfVersion(this);
    }

    @Override
    public String toString() {
        return "GetVersion{" +
                "major=" + major +
                ", minor=" + minor +
                ", patch=" + patch +
                ", identifier='" + identifier + '\'' +
                ", metadata='" + metadata + '\'' +
                '}';
    }

    private static final class DemandChainDescription {
        private final EqualVersion equalVersion;

        public DemandChainDescription(String version) {
            this.equalVersion = register(version);
        }

        public boolean getIfVersion(GetVersion getVersion) {
            return this.equalVersion.get(getVersion);
        }

        private EqualVersion register(String version) {
            if (version.contains("(") || version.contains(")") || version.contains("[") || version.contains("]")) {
                version = version.replace(" ","");
                final String[] VersionArray = version.substring(1, version.length()-1).split(",");
                final double StartVersion = new GetVersion(VersionArray[0]).getVersion();
                final double EndVersion = new GetVersion(VersionArray[1]).getVersion();
                return Switch.in(version)
                        .out(EqualVersion.class)
                        .isCustom(e -> e.startsWith("(") && e.endsWith(")")).thenGet(e -> IsUtil.inTwoNumbersNoSE(StartVersion,e.getVersion(),EndVersion))
                        .isCustom(e -> e.startsWith("(") && e.endsWith("]")).thenGet(e -> IsUtil.inTwoNumbersNoSrE(StartVersion,e.getVersion(),EndVersion,false))
                        .isCustom(e -> e.startsWith("[") && e.endsWith(")")).thenGet(e -> IsUtil.inTwoNumbersNoSrE(StartVersion,e.getVersion(),EndVersion,true))
                        .isCustom(e -> e.startsWith("[") && e.endsWith("]")).thenGet(e -> IsUtil.inTwoNumbers(StartVersion,e.getVersion(),EndVersion))
                        .elseGet((e) -> false);
            } else {
                final String[] VersionArray = version.trim().split(" ");
                final double SourceVersion = new GetVersion(VersionArray[1]).getVersion();
                return Switch.in(VersionArray[0])
                        .out(EqualVersion.class)
                        .is(">").thenGet(e -> e.getVersion() > SourceVersion)
                        .is(">=").thenGet(e -> e.getVersion() >= SourceVersion)
                        .is("<").thenGet(e -> e.getVersion() < SourceVersion)
                        .is("<=").thenGet(e -> e.getVersion() <= SourceVersion)
                        .is("!=").thenGet(e -> e.getVersion() != SourceVersion)
                        .elseGet((e) -> e.getVersion() == SourceVersion);
            }

        }
    }

    private interface EqualVersion {
        boolean get(GetVersion getVersion);
    }
}



/**
 * 解析一条依赖需求描述, 在无法解析的时候抛出 [IllegalArgumentException]
 *
 * 对于一条规则, 有以下方式可选
 *
 * - `1.0.0-M4`       要求 1.0.0-M4 版本, 且只能是 1.0.0-M4 版本
 * - `> 1.0.0-RC`     要求 1.0.0-RC 之后的版本, 不能是 1.0.0-RC
 * - `>= 1.0.0-RC`    要求 1.0.0-RC 或之后的版本, 可以是 1.0.0-RC
 * - `< 1.0.0-RC`     要求 1.0.0-RC 之前的版本, 不能是 1.0.0-RC
 * - `<= 1.0.0-RC`    要求 1.0.0-RC 或之前的版本, 可以是 1.0.0-RC
 * - `!= 1.0.0-RC`    要求 除了1.0.0-RC 的任何版本
 *     - `[1.0.0, 1.2.0]`
 *     - `(1.0.0, 1.2.0]`
 *     - `[1.0.0, 1.2.0)`
 *     - `(1.0.0, 1.2.0)` [数学区间](https://baike.baidu.com/item/%E5%8C%BA%E9%97%B4/1273117)
 *
 * 对于多个规则, 允许使用逻辑符号 `{}`, `||`, `&&`
 * 例如:
 * - `1.x || 2.x || 3.0.0`
 * - `<= 0.5.3 || >= 1.0.0`
 * - `{> 1.0 && < 1.5} || {> 1.8}`
 * - `{> 1.0 && < 1.5} || {> 1.8}`
 * - `> 1.0.0 && != 1.2.0`
 *
 * 特别注意:
 * - 依赖规则版本号不需要携带版本号元数据, 元数据不参与依赖需求的检查
 * - 如果目标版本号携带有先行版本号, 请不要忘记先行版本号
 * - 因为 `()` 已经用于数学区间, 使用 `{}` 替代 `()`
 */