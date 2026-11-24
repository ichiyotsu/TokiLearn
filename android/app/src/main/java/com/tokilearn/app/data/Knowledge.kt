package com.tokilearn.app.data

data class KnowledgeItem(val title: String, val detail: String)

data class KnowledgeChapter(
    val id: String,
    val title: String,
    val subtitle: String,
    val items: List<KnowledgeItem>,
    /** 未写完时显示，如“第 3 节起未完待续”。 */
    val pendingNote: String? = null,
)

/**
 * 从 TaskPro 笔记风格的文本里抽出章节标题行（纯函数，可单元测试）。
 * 标题行以“绪论”或“Part ”开头。
 */
fun parseChapterHeads(text: String): List<String> =
    text.lineSequence()
        .map { it.trim() }
        .filter { it == "绪论" || it.startsWith("Part ") }
        .toList()

/**
 * 机械设计基础考点（subjectId = "mech-design"）。
 * 内容只用 TaskPro 笔记与教材原文的定义、公式、条件、结论，
 * 不加解说、评价、学习提示；笔记没写完的章节标“未完待续”，绝不编造。
 */
object MechDesignKnowledge {
    val chapters: List<KnowledgeChapter> = listOf(
        KnowledgeChapter(
            id = "intro", title = "绪论", subtitle = "机器 · 机构 · 构件 · 零件",
            items = listOf(
                KnowledgeItem("机器的主体", "机器的主体部分由机构组成，机构由构件组成。"),
                KnowledgeItem("零件与构件", "零件是最小的制造单元，构件是最小的运动单元。"),
                KnowledgeItem("包含关系", "零件和构件之间没有包含关系。"),
            ),
        ),
        KnowledgeChapter(
            id = "part1", title = "Part 1 平面机构运动分析", subtitle = "运动副 · 运动简图 · 自由度",
            items = listOf(
                KnowledgeItem("1-1 运动副", "一个作平面运动的构件在平面内有 3 个自由度；机构是由许多构件组成的；两构件直接接触并能产生一定相对运动的称为运动副；按接触特性分为低副 / 高副。"),
                KnowledgeItem("1-2 平面机构运动简图", "机架 / 焊接 / 杆的表示；刚性三角形在构件数上属于一根杆。"),
                KnowledgeItem("自由度公式", "F＝3n−2PL−PH；F 为自由度，n 为活动构件数，PL 为低副数目，PH 为高副数目。"),
                KnowledgeItem("1-3 自由度特殊情况", "复合铰链 / 局部自由度 / 虚约束。"),
            ),
        ),
        KnowledgeChapter(
            id = "part2", title = "Part 2 平面四连杆机构及其设计", subtitle = "曲柄条件 · 急回 · 压力角 · 死点",
            items = listOf(
                KnowledgeItem("2-1 杆件", "连架杆（分为曲柄和摆杆）/ 连杆。"),
                KnowledgeItem("2-2 周转副存在的条件", "① 两构件必有最短杆；② 杆长条件。"),
                KnowledgeItem("杆长条件公式", "l_min＋l_max ≤ l_3＋l_4。"),
                KnowledgeItem("2-3 曲柄存在的条件", "① 满足杆长条件；② 最短杆为连架杆或机架。"),
                KnowledgeItem("2-4 三种四杆机构类型存在的条件", "铰链四杆机构三种类型存在的条件。"),
                KnowledgeItem("2-5 相位夹角", "相位夹角。"),
                KnowledgeItem("2-6 行程速比系数", "行程速比系数。"),
                KnowledgeItem("2-7 急回运动", "行程速比系数 k＝v2/v1。"),
                KnowledgeItem("2-8 压力角 α", "F 和 V 的夹角；压力角与传动角和为 90°。"),
                KnowledgeItem("2-9 最小传动角", "最小传动角即最大压力角，出现在曲柄与机架共线时两个位置之一处。"),
                KnowledgeItem("2-10 死点", "当曲柄为主动件、摇杆为从动件，一般情况下没有死点；F 与 V 同向时运动最流畅，F 向下时压住了无法移动即为死点。"),
            ),
        ),
        KnowledgeChapter(
            id = "part3", title = "Part 3 凸轮机构", subtitle = "常考盘形凸轮",
            items = listOf(
                KnowledgeItem("推杆类型", "尖顶推杆，滚子推杆，平底推杆（平底不常考，压力角固定）。"),
                KnowledgeItem("对心 / 偏心", "对心与偏心的区分。"),
                KnowledgeItem("基本参数", "基圆，偏距，偏距圆，推程 / 回程等。"),
                KnowledgeItem("三种组合", "对心直动尖顶推杆 / 偏心直动滚子推杆 / 偏心直动平底推杆。"),
                KnowledgeItem("冲击", "刚性冲击 / 柔性冲击；a＝Δv/Δt＝(v2−v1)/(t2−t1)。"),
                KnowledgeItem("反转法原理", "反转法原理。"),
                KnowledgeItem("压力角和基圆半径", "压力角和基圆半径。"),
            ),
        ),
        KnowledgeChapter(
            id = "part4", title = "Part 4 齿轮机构及其设计", subtitle = "渐开线标准齿轮 · 核心公式",
            items = listOf(
                KnowledgeItem("啮合点 / 节点", "啮合点 / 节点。"),
                KnowledgeItem("相交 / 交错", "相交 / 交错；提到了蜗轮蜗杆。"),
                KnowledgeItem("传动比", "i12＝n1/n2＝r2/r1（了解即可）；inv（了解即可）。"),
                KnowledgeItem("渐开线标准齿轮", "渐开线标准齿轮的基本参数和几何尺寸。"),
                KnowledgeItem("分度圆直径", "d＝mz。"),
                KnowledgeItem("基圆直径", "db＝d·cosα。"),
                KnowledgeItem("齿顶高 / 齿根高", "齿顶高 ha＝ha*·m；齿根高 hf＝(ha*＋c*)m。"),
                KnowledgeItem("标准中心距", "a＝m(z1＋z2)/2。"),
                KnowledgeItem("正确啮合条件", "m1＝m2＝m 且 α1＝α2＝α。"),
                KnowledgeItem("渐开线变位齿轮", "变位加工 17 个齿以下的齿轮不发生根切；计算不考。"),
                KnowledgeItem("左右手定则", "左右手定则。"),
            ),
        ),
        KnowledgeChapter(
            id = "part5", title = "Part 5 齿轮系及其设计", subtitle = "定轴 · 周转",
            items = listOf(
                KnowledgeItem("分类", "周转轮系（F＝2 差动轮系，F＝1 行星轮系），定轴轮系。"),
                KnowledgeItem("传动比", "传动比 i1n 的计算。"),
            ),
            pendingNote = "第 3 节起未完待续（TaskPro 笔记写到此处中断，后续章节轴、轴承等尚未写完）。",
        ),
    )
}
