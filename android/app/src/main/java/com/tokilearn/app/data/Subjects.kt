package com.tokilearn.app.data

/**
 * 学科注册表：TokiLearn 是多学科学习软件，后续加学科只需在这里加一份
 * [Subject] 数据（考点 + 题目 + 考试信息 + Gemini 上下文），不用改导航和页面。
 * 所有数据层一律按 [Subject.id] 分键存储，见 [LearnStore]。
 */
data class Subject(
    val id: String,
    val name: String,
    val subtitle: String,
    val description: String,
    val chapters: List<KnowledgeChapter>,
    val questions: List<Question>,
    val pendingTopics: List<String>,
    /** 考试信息：只写卷面/资料中明确写明的，不编分值。 */
    val examInfo: List<Pair<String, String>>,
    /** 发给 Gemini 的学科上下文（不含 Key）。 */
    val geminiContext: String,
)

object Subjects {
    val mechDesign = Subject(
        id = "mech-design",
        name = "机械设计基础",
        subtitle = "上海专升本 · 考尚",
        description = "杨可桢《机械设计基础》（第 7 版）：绪论、平面机构、四连杆、凸轮、齿轮、齿轮系。",
        chapters = MechDesignKnowledge.chapters,
        questions = MechDesignQuestions.questions,
        pendingTopics = MechDesignQuestions.pendingTopics,
        examInfo = listOf(
            "全卷满分" to "100 分",
            "选择题" to "15 题",
            "判断题" to "10 题",
            "计算题" to "7 题（模拟卷 6 题）",
            "每题分值" to "卷面未写明，以卷面为准",
        ),
        geminiContext = "你是机械设计基础（上海专升本）学习助手。范围：绪论、平面机构运动分析、平面四连杆机构、凸轮机构、齿轮机构、齿轮系。只依据教材与笔记内容回答，不确定的地方明说不知道，不要编造。",
    )

    /** 0.1.0 先只有一门学科。 */
    val all: List<Subject> = listOf(mechDesign)

    fun byId(id: String): Subject? = all.find { it.id == id }
}
