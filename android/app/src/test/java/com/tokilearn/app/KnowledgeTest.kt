package com.tokilearn.app

import com.tokilearn.app.data.MechDesignKnowledge
import com.tokilearn.app.data.Subjects
import com.tokilearn.app.data.parseChapterHeads
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KnowledgeTest {
    @Test fun parseChapterHeadsExtractsHeads() {
        val text = """
            绪论

            1 机器的主体部分由机构组成。

            Part 1 平面机构运动分析

            1-1 运动副及其分类

            Part 2 平面四连杆机构及其设计
        """.trimIndent()
        assertEquals(
            listOf("绪论", "Part 1 平面机构运动分析", "Part 2 平面四连杆机构及其设计"),
            parseChapterHeads(text),
        )
    }

    @Test fun parseChapterHeadsIgnoresBodyLines() {
        assertEquals(emptyList<String>(), parseChapterHeads("F=3n-2PL-PH\n复合铰链 / 局部自由度"))
    }

    @Test fun mechDesignChaptersComplete() {
        val chapters = MechDesignKnowledge.chapters
        assertEquals(listOf("绪论", "Part 1 平面机构运动分析", "Part 2 平面四连杆机构及其设计", "Part 3 凸轮机构", "Part 4 齿轮机构及其设计", "Part 5 齿轮系及其设计"), chapters.map { it.title })
        assertTrue(chapters.all { it.items.isNotEmpty() })
    }

    @Test fun unfinishedChapterIsMarked() {
        val part5 = MechDesignKnowledge.chapters.find { it.id == "part5" }
        assertNotNull(part5)
        assertTrue(part5!!.pendingNote!!.contains("未完待续"))
    }
}

class SubjectsTest {
    @Test fun registryHasMechDesignOnlyForNow() {
        assertEquals(listOf("mech-design"), Subjects.all.map { it.id })
        assertNotNull(Subjects.byId("mech-design"))
    }

    @Test fun everyQuestionResolvesToItsSubject() {
        Subjects.all.forEach { subject ->
            assertTrue(subject.questions.isNotEmpty())
            assertTrue(subject.questions.all { it.subjectId == subject.id })
            assertTrue(subject.pendingTopics.isNotEmpty())
        }
    }

    @Test fun examInfoStatesFullScoreWithoutInventedPerQuestionScores() {
        val info = Subjects.mechDesign.examInfo.toMap()
        assertEquals("100 分", info["全卷满分"])
        assertTrue(info.getValue("每题分值").contains("卷面未写明"))
    }
}
