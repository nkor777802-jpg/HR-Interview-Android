package com.hrinterview.app.data.seed

import com.hrinterview.app.data.local.db.CompetenceEntity
import com.hrinterview.app.data.local.db.QuestionEntity
import com.hrinterview.app.domain.PositionType
import com.hrinterview.app.domain.QuestionType

object BuiltInIds {
    const val EXPERIENCE = "experience"
    const val MOTIVATION = "motivation"
    const val RESPONSIBILITY = "responsibility"
    const val COMMUNICATION = "communication"
    const val PROBLEM_SOLVING = "problem_solving"
    const val TEAMWORK = "teamwork"
    const val PEOPLE_MANAGEMENT = "people_management"
    const val INDEPENDENCE = "independence"
    const val CUSTOMER = "customer_orientation"
    const val QUALITY = "quality"
    const val COMPLIANCE = "compliance"
    const val DEVIATIONS = "deviations"
    const val DECISION_MAKING = "decision_making"
    const val INFORMATION = "information"
}

object QuestionBankSeed {
    val competences = listOf(
        c(BuiltInIds.EXPERIENCE, "Опыт"),
        c(BuiltInIds.MOTIVATION, "Мотивация"),
        c(BuiltInIds.RESPONSIBILITY, "Ответственность"),
        c(BuiltInIds.COMMUNICATION, "Коммуникация"),
        c(BuiltInIds.PROBLEM_SOLVING, "Решение проблем"),
        c(BuiltInIds.TEAMWORK, "Работа в команде"),
        c(BuiltInIds.PEOPLE_MANAGEMENT, "Управление людьми"),
        c(BuiltInIds.INDEPENDENCE, "Самостоятельность"),
        c(BuiltInIds.CUSTOMER, "Клиентоориентированность"),
        c(BuiltInIds.QUALITY, "Качество работы"),
        c(BuiltInIds.COMPLIANCE, "Соблюдение требований"),
        c(BuiltInIds.DEVIATIONS, "Действия при отклонениях"),
        c(BuiltInIds.DECISION_MAKING, "Принятие решений"),
        c(BuiltInIds.INFORMATION, "Работа с информацией")
    )

    fun suggestedCompetences(type: PositionType): List<String> = when (type) {
        PositionType.MANAGER -> listOf(
            BuiltInIds.PEOPLE_MANAGEMENT,
            BuiltInIds.DECISION_MAKING,
            BuiltInIds.COMMUNICATION,
            BuiltInIds.PROBLEM_SOLVING,
            BuiltInIds.RESPONSIBILITY,
            BuiltInIds.TEAMWORK
        )
        PositionType.SPECIALIST -> listOf(
            BuiltInIds.INDEPENDENCE,
            BuiltInIds.PROBLEM_SOLVING,
            BuiltInIds.COMMUNICATION,
            BuiltInIds.INFORMATION,
            BuiltInIds.MOTIVATION,
            BuiltInIds.EXPERIENCE
        )
        PositionType.WORKER -> listOf(
            BuiltInIds.EXPERIENCE,
            BuiltInIds.RESPONSIBILITY,
            BuiltInIds.QUALITY,
            BuiltInIds.COMPLIANCE,
            BuiltInIds.DEVIATIONS
        )
    }

    val questions: List<QuestionEntity> = listOf(
        q(1, "Расскажите о задаче, где ваш опыт напрямую повлиял на результат. Что именно вы сделали иначе, чем менее опытный коллега?", BuiltInIds.EXPERIENCE, QuestionType.EXPERIENCE, all()),
        q(2, "Какой самый сложный рабочий случай за последние 1–2 года вы готовы разобрать подробно: контекст, ваши действия, итог?", BuiltInIds.EXPERIENCE, QuestionType.EXPERIENCE, all()),
        q(3, "Чего вы ждёте от этой роли через полгода? По каким признакам поймёте, что выбор оказался верным?", BuiltInIds.MOTIVATION, QuestionType.MOTIVATION, all()),
        q(4, "Бывало ли, что работа стала менее интересной? Что вы тогда меняли в подходе, а что оставляли без изменений?", BuiltInIds.MOTIVATION, QuestionType.MOTIVATION, all()),
        q(5, "Опишите ситуацию, когда вы взяли на себя ответственность за ошибку, хотя формально могли переложить её на других.", BuiltInIds.RESPONSIBILITY, QuestionType.BEHAVIORAL, all()),
        q(6, "Как вы поступаете, если видите риск срыва срока, но ещё есть шанс успеть при сверхусилиях команды?", BuiltInIds.RESPONSIBILITY, QuestionType.SITUATIONAL, all()),
        q(7, "Приведите пример, когда вам нужно было донести неприятную, но важную информацию. Как вы готовились и чем закончилось?", BuiltInIds.COMMUNICATION, QuestionType.BEHAVIORAL, all()),
        q(8, "Собеседник на встрече говорит общими фразами и уходит от сути. Как вы вернёте разговор к конкретным фактам?", BuiltInIds.COMMUNICATION, QuestionType.SITUATIONAL, all()),
        q(9, "Опишите проблему, которую вы решали без готовой инструкции. С чего начали, какие варианты рассматривали, почему выбрали итоговый?", BuiltInIds.PROBLEM_SOLVING, QuestionType.BEHAVIORAL, all()),
        q(10, "Два решения выглядят равноценными, но последствия разные для качества и для сроков. Как выберете?", BuiltInIds.PROBLEM_SOLVING, QuestionType.SITUATIONAL, all()),
        q(11, "Вспомните случай, когда команда расходилась во мнениях. Какую роль вы заняли и что сделали, чтобы сдвинуть работу?", BuiltInIds.TEAMWORK, QuestionType.BEHAVIORAL, all()),
        q(12, "Коллега систематически не передаёт вам нужные данные вовремя. Как будете действовать на первой и на третьей неделе?", BuiltInIds.TEAMWORK, QuestionType.SITUATIONAL, all()),
        q(13, "Расскажите, как вы входили в работу на новом месте: что выясняли в первую неделю и чего сознательно не делали.", BuiltInIds.INDEPENDENCE, QuestionType.EXPERIENCE, all()),
        q(14, "Руководитель недоступен, а решение нужно сегодня. В каких границах вы действуете сами и когда всё же эскалируете?", BuiltInIds.INDEPENDENCE, QuestionType.SITUATIONAL, setOf(PositionType.SPECIALIST, PositionType.MANAGER)),
        q(15, "Клиент или внутренний заказчик настаивает на решении, которое ухудшит качество. Как вы строите разговор?", BuiltInIds.CUSTOMER, QuestionType.SITUATIONAL, setOf(PositionType.SPECIALIST, PositionType.MANAGER)),
        q(16, "Опишите случай, когда вы сохранили отношения с заказчиком после сбоя. Что конкретно сделали?", BuiltInIds.CUSTOMER, QuestionType.BEHAVIORAL, setOf(PositionType.SPECIALIST, PositionType.MANAGER)),

        q(17, "Два сильных сотрудника вашей команды вступили в конфликт, который уже влияет на работу остальных. Что вы будете делать?", BuiltInIds.PEOPLE_MANAGEMENT, QuestionType.SITUATIONAL, mgr()),
        q(18, "Как вы принимаете решение, кого развивать в первую очередь, если оба сотрудника сильны, но по-разному?", BuiltInIds.PEOPLE_MANAGEMENT, QuestionType.COMPETENCY, mgr()),
        q(19, "Ключевой сотрудник объявил об уходе в период высокой нагрузки. Ваши ближайшие шаги на этой и следующей неделе?", BuiltInIds.PEOPLE_MANAGEMENT, QuestionType.SITUATIONAL, mgr()),
        q(20, "Расскажите, как вы давали сложную обратную связь человеку, который считал свою работу безупречной.", BuiltInIds.PEOPLE_MANAGEMENT, QuestionType.BEHAVIORAL, mgr()),
        q(21, "Подчинённый регулярно срывает сроки, объясняя это «загрузкой от других». Как проверите факты и что сделаете?", BuiltInIds.PEOPLE_MANAGEMENT, QuestionType.SITUATIONAL, mgr()),
        q(22, "Вам нужно провести непопулярное, но необходимое изменение. Как вы подготовите команду и снизите сопротивление?", BuiltInIds.DECISION_MAKING, QuestionType.COMPETENCY, mgr()),
        q(23, "Данных недостаточно, а ждать нельзя: ошибка дорого обойдётся. Как примете решение и как зафиксируете риски?", BuiltInIds.DECISION_MAKING, QuestionType.SITUATIONAL, mgr()),
        q(24, "Приведите пример решения, которое вы позже пересмотрели. Что стало сигналом, что курс нужно менять?", BuiltInIds.DECISION_MAKING, QuestionType.BEHAVIORAL, mgr()),
        q(25, "Как вы контролируете результат без ежедневного микроменеджмента? Приведите рабочую схему.", BuiltInIds.PEOPLE_MANAGEMENT, QuestionType.COMPETENCY, mgr()),
        q(26, "Команде одновременно пришли два приоритета «от первых лиц». Как вы выстроите работу и коммуникацию наверх?", BuiltInIds.DECISION_MAKING, QuestionType.SITUATIONAL, mgr()),

        q(27, "Руководитель одновременно поставил две срочные задачи, но обе невозможно выполнить в установленный срок. Как вы будете действовать?", BuiltInIds.INDEPENDENCE, QuestionType.SITUATIONAL, spec()),
        q(28, "Вы не согласны с техническим или методическим решением, которое уже озвучено как финальное. Что сделаете?", BuiltInIds.COMMUNICATION, QuestionType.SITUATIONAL, spec()),
        q(29, "В середине работы изменились требования. Как вы оцените влияние, с кем согласуете и что зафиксируете?", BuiltInIds.INFORMATION, QuestionType.SITUATIONAL, spec()),
        q(30, "Вы обнаружили ошибку коллеги, которая ещё не стала видимой для заказчика. Ваши действия?", BuiltInIds.RESPONSIBILITY, QuestionType.SITUATIONAL, spec()),
        q(31, "Опишите сложную задачу, которую вы закрыли почти без сопровождения. Где брали недостающие данные?", BuiltInIds.INDEPENDENCE, QuestionType.BEHAVIORAL, spec()),
        q(32, "Заказчик просит сделать работу вне согласованного объёма «быстро и неформально». Как ответите?", BuiltInIds.CUSTOMER, QuestionType.SITUATIONAL, spec()),
        q(33, "Чтобы начать, вам не хватает вводных, а источник информации отвечает с задержкой. Как сдвинете работу?", BuiltInIds.INFORMATION, QuestionType.SITUATIONAL, spec()),
        q(34, "Объясните сложную профессиональную тему человеку без вашей экспертизы. Как проверите, что вас поняли?", BuiltInIds.COMMUNICATION, QuestionType.COMPETENCY, spec()),
        q(35, "Срок выглядит нереалистичным уже на старте. Что скажете руководителю и какие варианты предложите?", BuiltInIds.PROBLEM_SOLVING, QuestionType.SITUATIONAL, spec()),
        q(36, "Как вы проверяете качество своей работы до передачи дальше? Приведите конкретный рабочий ритуал.", BuiltInIds.QUALITY, QuestionType.COMPETENCY, spec()),

        q(37, "Во время работы вы заметили отклонение в качестве продукции, но оборудование продолжает работать. Ваши действия?", BuiltInIds.DEVIATIONS, QuestionType.SITUATIONAL, wrk()),
        q(38, "Вас просят пропустить шаг по технике безопасности или инструкции, чтобы успеть к сроку. Что ответите и что сделаете?", BuiltInIds.COMPLIANCE, QuestionType.SITUATIONAL, wrk()),
        q(39, "Коллега выполняет операцию не по инструкции, ссылаясь на «так быстрее и все так делают». Ваши действия?", BuiltInIds.COMPLIANCE, QuestionType.SITUATIONAL, wrk()),
        q(40, "Оборудование или инструмент ведёт себя необычно, хотя явного отказа ещё нет. Как поступите?", BuiltInIds.DEVIATIONS, QuestionType.SITUATIONAL, wrk()),
        q(41, "Вы не до конца поняли часть задания, а смена уже началась. Как уточните задачу, не останавливая общий поток работы?", BuiltInIds.COMMUNICATION, QuestionType.SITUATIONAL, wrk()),
        q(42, "При приёмке смены информация передана неполно. Что обязательно выясните до начала самостоятельной работы?", BuiltInIds.RESPONSIBILITY, QuestionType.SITUATIONAL, wrk()),
        q(43, "После вашей операции обнаружен дефект. Как будете разбирать причину и что предложите, чтобы это не повторилось?", BuiltInIds.QUALITY, QuestionType.BEHAVIORAL, wrk()),
        q(44, "Как вы лично контролируете качество своей работы в течение смены, а не только в конце?", BuiltInIds.QUALITY, QuestionType.COMPETENCY, wrk()),
        q(45, "Ночная смена, руководитель недоступен, появилась нестандартная ситуация. В каких границах действуете сами?", BuiltInIds.INDEPENDENCE, QuestionType.SITUATIONAL, wrk()),
        q(46, "Материал или заготовка не соответствует привычным требованиям. Что проверите и кому сообщите до продолжения работы?", BuiltInIds.DEVIATIONS, QuestionType.SITUATIONAL, wrk()),
        q(47, "Расскажите, как вы осваивали новую операцию или участок. Что помогло не снизить качество в первые дни?", BuiltInIds.EXPERIENCE, QuestionType.EXPERIENCE, wrk()),
        q(48, "Почему вам важна именно эта рабочая роль, а не любая другая с похожей оплатой?", BuiltInIds.MOTIVATION, QuestionType.MOTIVATION, wrk())
    )

    private fun c(id: String, name: String) = CompetenceEntity(id, name, isBuiltIn = true, isEnabled = true)

    private fun all() = PositionType.entries.toSet()
    private fun mgr() = setOf(PositionType.MANAGER)
    private fun spec() = setOf(PositionType.SPECIALIST)
    private fun wrk() = setOf(PositionType.WORKER)

    private fun q(
        n: Int,
        text: String,
        competenceId: String,
        type: QuestionType,
        positions: Set<PositionType>
    ) = QuestionEntity(
        id = "builtin_%02d".format(n),
        text = text,
        competenceId = competenceId,
        questionType = type.name,
        positions = positions.joinToString(",") { it.name },
        isBuiltIn = true,
        isEnabled = true,
        sortKey = n
    )
}
