package com.tokilearn.app.data

import kotlin.math.abs

/** 题目类型：选择题 / 判断题 / 计算题 / 画图题。 */
enum class QuestionType { SINGLE_CHOICE, TRUE_FALSE, CALCULATION, DRAWING }

data class Question(
    val id: String,
    /** 所属学科，见 [Subjects]。 */
    val subjectId: String,
    val chapter: String,
    val type: QuestionType,
    val stem: String,
    /** 选择题/判断题的选项；判断题固定为 ["对", "错"]。 */
    val options: List<String> = emptyList(),
    /** 选择题/判断题的正确选项下标。 */
    val answerIndex: Int = -1,
    /** 计算题的数值答案（最终结果）。 */
    val numericAnswer: Double? = null,
    /** 计算题的容差：|输入 − 答案| ≤ 容差即判对。 */
    val tolerance: Double = 0.01,
    val unit: String = "",
    /** 画图题的参考答案与答案位置（文字描述）。 */
    val referenceAnswer: String = "",
    val explanation: String = "",
    /** 题目出处，如“浙江大学2012研”。 */
    val source: String = "",
)

/** 计算题按容差判分（纯函数，可单元测试）。 */
fun isNumericCorrect(input: Double, expected: Double, tolerance: Double): Boolean =
    input.isFinite() && abs(input - expected) <= tolerance

/** 选择题/判断题判分（纯函数）。 */
fun isChoiceCorrect(selectedIndex: Int, answerIndex: Int): Boolean =
    selectedIndex == answerIndex

/** 错题本纯操作（可单元测试；持久化见 [LearnStore]）。 */
fun addWrong(ids: Set<String>, id: String): Set<String> = ids + id
fun removeWrong(ids: Set<String>, id: String): Set<String> = ids - id

/**
 * 机械设计基础题库（subjectId = "mech-design"）。
 *
 * 全部 13 道出自杨可桢《机械设计基础》（第 7 版）笔记和课后习题详解
 * （名校考研真题详解，第 1、2 章）与 TaskPro 笔记中的公式
 * （F=3n−2PL−PH、d=mz、db=d·cosα）。其余章节与《专升本必刷 2000 题》
 * 均为“待导入”，见 [pendingTopics]——绝不编造题目。
 */
object MechDesignQuestions {
    val questions: List<Question> = listOf(
        Question(
            id = "md-1-1", subjectId = "mech-design", chapter = "绪论 / 平面机构运动分析",
            type = QuestionType.SINGLE_CHOICE,
            stem = "构件是机械中独立的（ ）单元。",
            options = listOf("运动", "设计", "制造", "分析"),
            answerIndex = 0,
            explanation = "构件是运动的基本单元，零件是制造的基本单元。",
            source = "浙江大学2012研",
        ),
        Question(
            id = "md-1-2", subjectId = "mech-design", chapter = "绪论 / 平面机构运动分析",
            type = QuestionType.SINGLE_CHOICE,
            stem = "两构件通过（ ）接触组成的运动副称为低副。",
            options = listOf("面", "线", "点", "面或线"),
            answerIndex = 0,
            explanation = "按接触特性：低副为面接触，高副为点或线接触。",
            source = "浙江大学2012研",
        ),
        Question(
            id = "md-2-1", subjectId = "mech-design", chapter = "平面四连杆机构",
            type = QuestionType.SINGLE_CHOICE,
            stem = "在设计铰链四杆机构时，应使最小传动角（ ）。",
            options = listOf("尽可能小", "尽可能大", "为 0°", "为 90°"),
            answerIndex = 1,
            explanation = "传动角用来判断连杆机构的传力性能，传动角越大，传力性能越好。",
            source = "浙江大学2013研",
        ),
        Question(
            id = "md-2-2", subjectId = "mech-design", chapter = "平面四连杆机构",
            type = QuestionType.SINGLE_CHOICE,
            stem = "四杆机构中是否存在死点取决于主动件作往复运动时，（ ）是否与连杆共线。",
            options = listOf("主动件", "从动连架杆", "摇杆", "机架"),
            answerIndex = 1,
            explanation = "连杆与从动连架杆共线时传动角 γ＝0°，主动件的力恰好通过从动件回转中心，出现“顶死”现象，即死点。",
            source = "湘潭大学2016研",
        ),
        Question(
            id = "md-1-t1", subjectId = "mech-design", chapter = "绪论 / 平面机构运动分析",
            type = QuestionType.TRUE_FALSE,
            stem = "两构件通过面或线接触组成的运动副称为低副。",
            options = listOf("对", "错"),
            answerIndex = 1,
            explanation = "面接触是低副，线接触是高副。",
            source = "浙江大学2012研",
        ),
        Question(
            id = "md-1-t2", subjectId = "mech-design", chapter = "绪论 / 平面机构运动分析",
            type = QuestionType.TRUE_FALSE,
            stem = "将构件用运动副联接成具有确定运动的机构的条件是自由度数为 1。",
            options = listOf("对", "错"),
            answerIndex = 1,
            explanation = "机构具有确定运动的条件是自由度大于 0，且自由度数等于原动件数。",
            source = "浙江大学2013研",
        ),
        Question(
            id = "md-2-t1", subjectId = "mech-design", chapter = "平面四连杆机构",
            type = QuestionType.TRUE_FALSE,
            stem = "在铰链四杆机构中，若以最短杆为机架，则该机构为双曲柄机构。",
            options = listOf("对", "错"),
            answerIndex = 1,
            explanation = "还须满足杆长条件（最短杆＋最长杆 ≤ 其余两杆长度之和）；只取最短杆为机架还不够。",
            source = "西安理工大学2017研",
        ),
        Question(
            id = "md-1-c1", subjectId = "mech-design", chapter = "绪论 / 平面机构运动分析",
            type = QuestionType.CALCULATION,
            stem = "某机构活动构件数 n＝9，低副数 PL＝12，高副数 PH＝2，按 F＝3n−2PL−PH 求自由度 F。",
            numericAnswer = 1.0, tolerance = 0.01,
            explanation = "F＝3×9−2×12−2＝1。",
            source = "厦门大学2013研（图 1-3-1）",
        ),
        Question(
            id = "md-1-c2", subjectId = "mech-design", chapter = "绪论 / 平面机构运动分析",
            type = QuestionType.CALCULATION,
            stem = "某机构共有 6 个活动杆件，8 个低副（6 个转动副，2 个移动副），两齿轮啮合处有 1 个高副，按 F＝3n−2PL−PH 求自由度 F。",
            numericAnswer = 1.0, tolerance = 0.01,
            explanation = "F＝3×6−2×8−1＝1。",
            source = "厦门大学2011研（图 1-3-2）",
        ),
        Question(
            id = "md-1-c3", subjectId = "mech-design", chapter = "绪论 / 平面机构运动分析",
            type = QuestionType.CALCULATION,
            stem = "飞剪机构共有 5 个活动杆件，7 个低副（5 个转动副，2 个移动副），无高副，按 F＝3n−2PL−PH 求自由度 F。",
            numericAnswer = 1.0, tolerance = 0.01,
            explanation = "F＝3×5−2×7−0＝1。",
            source = "浙江大学2010研（图 1-3-3）",
        ),
        Question(
            id = "md-4-c1", subjectId = "mech-design", chapter = "齿轮机构",
            type = QuestionType.CALCULATION,
            stem = "标准直齿圆柱齿轮，模数 m＝2 mm，齿数 z＝20，按 d＝mz 求分度圆直径 d。",
            numericAnswer = 40.0, tolerance = 0.01, unit = "mm",
            explanation = "d＝mz＝2×20＝40 mm。",
            source = "TaskPro 笔记 Part 4 核心公式",
        ),
        Question(
            id = "md-4-c2", subjectId = "mech-design", chapter = "齿轮机构",
            type = QuestionType.CALCULATION,
            stem = "某齿轮分度圆直径 d＝40 mm，压力角 α＝20°，按 db＝d·cosα 求基圆直径 db（保留两位小数）。",
            numericAnswer = 37.59, tolerance = 0.05, unit = "mm",
            explanation = "db＝40×cos20°≈37.59 mm。",
            source = "TaskPro 笔记 Part 4 核心公式",
        ),
        Question(
            id = "md-2-d1", subjectId = "mech-design", chapter = "平面四连杆机构",
            type = QuestionType.DRAWING,
            stem = "铰链四杆机构中，lAB＝30 mm，lBC＝110 mm，lCD＝80 mm，lAD＝120 mm，构件 1 为原动件。（1）判断 1 能否成为曲柄；（2）作图求构件 3 的最大摆角 ψmax；（3）作图求最小传动角 γmin；（4）分别固定构件 1、2、3、4 时，各获得何种机构？",
            referenceAnswer = "杆长条件：最短杆 30＋最长杆 120＝150 ≤ 110＋80＝190，满足；最短杆 AB 为连架杆，1 能成为曲柄，机构为曲柄摇杆机构。固定构件 1→曲柄摇杆机构；固定构件 2（连杆）→双摇杆机构；固定构件 3→曲柄摇杆机构；固定构件 4（最短杆为机架）→双曲柄机构。",
            explanation = "对照参考答案自判：四问全对算答对，任一问不对算答错。",
            source = "厦门大学2013研",
        ),
    )

    /** 待导入的题目位置（2000 题 OCR 进行中，绝不以假题目填充）。 */
    val pendingTopics: List<String> = listOf(
        "凸轮机构：选择题 / 判断题 / 计算题（待导入）",
        "齿轮机构：选择题 / 判断题（待导入）",
        "齿轮系：选择题 / 判断题 / 计算题（待导入）",
        "《专升本必刷 2000 题 机械设计基础》：OCR 进行中（待导入）",
        "11 月强化月测卷 / 模拟测试卷（一）～（四）：图片卷，待录入",
    )
}
