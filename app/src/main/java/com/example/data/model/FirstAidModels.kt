package com.example.data.model

import androidx.annotation.DrawableRes
import com.example.R

enum class FirstAidMediaFilter {
    ALL, PHOTOS, VIDEOS
}

enum class FirstAidAnimationType {
    BURNS_COOL_WATER,
    BLEEDING_PRESSURE,
    CHOKING_HEIMLICH,
    FAINTING_RECOVERY,
    FEVER_CARE,
    WOUNDS_CLEANSE
}

data class FirstAidVideoScene(
    val stepNumber: Int,
    val title: String,
    val caption: String,
    val durationSeconds: Int = 4,
    val isWarningScene: Boolean = false
)

data class FirstAidTopic(
    val id: String,
    val title: String,
    val shortBadge: String,
    val summary: String,
    @DrawableRes val imageRes: Int,
    val videoDurationLabel: String,
    val animationType: FirstAidAnimationType,
    val videoScenes: List<FirstAidVideoScene>,
    val doSteps: List<String>,
    val doNotSteps: List<String>,
    val redFlags: List<String>,
    val officialVideoUrl: String,
    val consultPrompt: String
)

object FirstAidCatalog {

    fun topics(language: AppLanguage): List<FirstAidTopic> = when (language) {
        AppLanguage.TG -> tajikTopics()
        AppLanguage.RU -> russianTopics()
        AppLanguage.UZ_LATN -> uzbekTopics()
    }

    private fun tajikTopics(): List<FirstAidTopic> = listOf(
        FirstAidTopic(
            id = "burns",
            title = "Сӯхтан (гармӣ ва оби ҷӯш)",
            shortBadge = "Сӯхтан",
            summary = "Хунук кардани фаврӣ бо оби равон дар 15–20 дақиқаи аввал ва муҳофизати пӯст.",
            imageRes = R.drawable.img_fa_burns,
            videoDurationLabel = "0:16",
            animationType = FirstAidAnimationType.BURNS_COOL_WATER,
            videoScenes = listOf(
                FirstAidVideoScene(
                    stepNumber = 1,
                    title = "Боздоштани таъсири гармӣ",
                    caption = "Беморро аз манбаи гармӣ дур кунед ва либоси танг ё ангуштаринро пеш аз варам эҳтиёткорона бароред."
                ),
                FirstAidVideoScene(
                    stepNumber = 2,
                    title = "Оби равони салқин (15–20 дақиқа)",
                    caption = "Ҷои сӯхтаро зери оби равони салқин (на яхбаста!) ба муддати 15–20 дақиқа нигоҳ доред."
                ),
                FirstAidVideoScene(
                    stepNumber = 3,
                    title = "Пӯшонидани тоза",
                    caption = "Пас аз хунук кардан бо бинти стерилии начаспанда ё плёнкаи тоза сабук пӯшонед."
                ),
                FirstAidVideoScene(
                    stepNumber = 4,
                    title = "Огоҳӣ: чӣ набояд кард",
                    caption = "Ҳеҷ гоҳ равған, сметана, хамираи дандон ё ях нагузоред ва обилаҳоро накафонед!",
                    isWarningScene = true
                )
            ),
            doSteps = listOf(
                "Ҷои сӯхтаро дарҳол зери оби равони салқин 15–20 дақиқа хунук кунед.",
                "Ангуштарин, соат ва либоси начаспидаро эҳтиёткорона бароред.",
                "Бо бандинаи тозаи стерилии начаспанда сабук пӯшонед."
            ),
            doNotSteps = listOf(
                "Ба ҷои сӯхта равған, сметана, хамираи дандон, тухм ё орд намолед.",
                "Яхро бевосита ба пӯст нагузоред ва обилаҳоро накафонед.",
                "Либоси ба пӯст часпидаро зӯран наканед."
            ),
            redFlags = listOf(
                "Сӯхтани калонтар аз кафи даст, сӯхтани амиқ ё сӯхтани рӯй, дастҳо, пойҳо ё узвҳои таносул.",
                "Сӯхтан дар кӯдаки хурдсол, сӯхтани кимиёвӣ ё барқӣ — фавран ба 103 занг занед."
            ),
            officialVideoUrl = "https://www.youtube.com/results?search_query=Red+Cross+first+aid+burns+treatment",
            consultPrompt = "Дар бораи сӯхтани пӯст ва нигоҳубини бехатар пас аз хунук кардан бо об ба ман маслиҳат диҳед."
        ),
        FirstAidTopic(
            id = "bleeding",
            title = "Хунравӣ (боздоштани хун)",
            shortBadge = "Хунравӣ",
            summary = "Фишори мустақим ба захм бо матои тоза ва нигоҳ доштани узв дар ҳолати ором.",
            imageRes = R.drawable.img_fa_bleeding,
            videoDurationLabel = "0:16",
            animationType = FirstAidAnimationType.BLEEDING_PRESSURE,
            videoScenes = listOf(
                FirstAidVideoScene(
                    stepNumber = 1,
                    title = "Омодагӣ ва ҳифзи даст",
                    caption = "Агар имкон бошад дастпӯши тоза пӯшед ва беморро ором шинонед ё хобонед."
                ),
                FirstAidVideoScene(
                    stepNumber = 2,
                    title = "Фишори мустақим ба захм",
                    caption = "Бо бинти стерилӣ ё матои тоза ба болои захм 10–15 дақиқа пайваста фишор оваред."
                ),
                FirstAidVideoScene(
                    stepNumber = 3,
                    title = "Илова кардани қабати нав",
                    caption = "Агар хун гузарад, қабати аввалро нагиред — аз болояш бандинаи дигар гузошта сахттар бандед."
                ),
                FirstAidVideoScene(
                    stepNumber = 4,
                    title = "Нишонаҳои хатарнок (103)",
                    caption = "Агар хун фаввора занад ё пас аз 10–15 дақиқаи фишор бознаистад, фавран ба 103 занг занед!",
                    isWarningScene = true
                )
            ),
            doSteps = listOf(
                "Беморро хобонед ё шинонед ва ба захм бо бинти стерилӣ ё матои тоза фишори мустақим оваред.",
                "Узви осебдидаро, агар шикастагӣ набошад, каме болотар аз сатҳи дил бардоред.",
                "Агар хун ба бинт гузарад, онро нагиред — аз болояш қабати нав гузоред."
            ),
            doNotSteps = listOf(
                "Бинти хунолуди аввалро нагиред, то лахтаи хун канда нашавад.",
                "Ҷисми бегонаи даруни захммондаро (шиша, оҳан) худсарона накашед.",
                "Ба захм маводи нотоза ё хока нарасонед."
            ),
            redFlags = listOf(
                "Хунравии шадид (хусусан хуни сурхи равшан, ки бо набз меҷаҳад).",
                "Пас аз 10–15 дақиқаи фишори мустақим хун бознаистад, ё сарчархзанӣ, рангпаридагӣ ва беҳушӣ пайдо шавад — фавран 103."
            ),
            officialVideoUrl = "https://www.youtube.com/results?search_query=Red+Cross+first+aid+severe+bleeding",
            consultPrompt = "Чӣ тавр хунравиро бехатар назорат кунем ва кадом аломатҳо муроҷиати фаврӣ ба духтурро талаб мекунанд?"
        ),
        FirstAidTopic(
            id = "choking",
            title = "Нафасгиршавӣ (гулӯгир шудан)",
            shortBadge = "Нафасгирӣ",
            summary = "Ёрии фаврӣ ҳангоми банд шудани роҳи нафас: 5 зарба ба байни китф ва манёври Геймлих.",
            imageRes = R.drawable.img_fa_choking,
            videoDurationLabel = "0:16",
            animationType = FirstAidAnimationType.CHOKING_HEIMLICH,
            videoScenes = listOf(
                FirstAidVideoScene(
                    stepNumber = 1,
                    title = "Санҷиши сулфа ва овоз",
                    caption = "Агар шахс сулфа карда тавонад, ба сулфа кардан ташвиқ кунед. Агар овоз ва нафас набошад — фавран амал кунед."
                ),
                FirstAidVideoScene(
                    stepNumber = 2,
                    title = "5 зарба ба байни шонаҳо",
                    caption = "Беморро ба пеш хам кунед ва бо қисми поёнии кафи даст ба байни ду китф 5 зарбаи қатъӣ занед."
                ),
                FirstAidVideoScene(
                    stepNumber = 3,
                    title = "5 фишори шикам (Геймлих)",
                    caption = "Аз пас бағал кунед, муштро болои ноф гузошта бо дасти дигар ба дарун ва боло 5 маротиба тез фишор диҳед."
                ),
                FirstAidVideoScene(
                    stepNumber = 4,
                    title = "Агар беҳуш шавад — 103!",
                    caption = "Дарҳол ба 103 занг занед. Дар занони ҳомила ва фарбеҳ фишорро ба қисми миёнаи сина мегузаронанд.",
                    isWarningScene = true
                )
            ),
            doSteps = listOf(
                "Агар сулфа кунад — назанед, бигзор сулфа кунад.",
                "Агар нафас нагирад ва гап зада натавонад: 5 зарба ба байни шонаҳо бо хам кардан ба пеш.",
                "Сипас 5 фишори шикам (манёври Геймлих) дар калонсолон ва кӯдакони аз 1-сола боло, то баромадани ҷисм."
            ),
            doNotSteps = listOf(
                "Бо ангушт кӯр-кӯрона ба даруни гулӯ даст наандозед — луқма метавонад чуқуртар равад.",
                "Ба зани ҳомила ё кӯдаки ширхора фишори сахти шикам надиҳед."
            ),
            redFlags = listOf(
                "Кабудшавии лабҳо, натавонистани нафаскашӣ ё сухан гуфтан.",
                "Беҳушшавии ногаҳонӣ — фавран ба 103 занг занед!"
            ),
            officialVideoUrl = "https://www.youtube.com/results?search_query=Red+Cross+first+aid+choking+Heimlich",
            consultPrompt = "Қоидаҳои ёрии аввалро ҳангоми гулӯгир шудан дар калонсолон ва кӯдакон фаҳмонед."
        ),
        FirstAidTopic(
            id = "fainting",
            title = "Беҳушшавӣ (гум кардани ҳуш)",
            shortBadge = "Беҳушӣ",
            summary = "Таъмини ҳавои тоза, санҷиши нафаскашӣ ва гузоштан дар ҳолати бехатари паҳлӯӣ.",
            imageRes = R.drawable.img_fa_fainting,
            videoDurationLabel = "0:16",
            animationType = FirstAidAnimationType.FAINTING_RECOVERY,
            videoScenes = listOf(
                FirstAidVideoScene(
                    stepNumber = 1,
                    title = "Санҷиши ҳуш ва нафаскашӣ",
                    caption = "Ба китфаш оҳиста даст расонида пурсед ва дар давоми 10 сония ҳаракати қафаси синаро бинед."
                ),
                FirstAidVideoScene(
                    stepNumber = 2,
                    title = "Агар нафас кашад — бардоштани пойҳо",
                    caption = "Ба пушт хобонед, гиребонро кушоед, ҳавои тоза диҳед ва пойҳоро тақрибан 20–30 см боло бардоред."
                ),
                FirstAidVideoScene(
                    stepNumber = 3,
                    title = "Ҳолати бехатари паҳлӯӣ",
                    caption = "Агар ҳуш зуд барнагардад ё қайкунӣ бошад, одамро эҳтиёткорона ба паҳлӯ гардонед, то роҳи нафас боз монад."
                ),
                FirstAidVideoScene(
                    stepNumber = 4,
                    title = "Огоҳӣ: об напошед!",
                    caption = "Ба одами беҳуш об ё дору нахӯронед. Агар зиёда аз 1–2 дақиқа ба ҳуш наояд, фавран ба 103 занг занед!",
                    isWarningScene = true
                )
            ),
            doSteps = listOf(
                "Шахсро дар ҷои бехатар хобонед, гиребон ва тасмаро суст кунед, тирезаро кушоед.",
                "Агар муқаррарӣ нафас кашад ва осеби сутунмӯҳра набошад, пойҳоро каме боло бардоред ё ба паҳлӯ гардонед.",
                "Пас аз ба ҳуш омадан якбора нахезонед — бигзор 10–15 дақиқа ором бимонад."
            ),
            doNotSteps = listOf(
                "Ба даҳони одами беҳуш об, чой ё дору нарезед.",
                "Ба рӯяш торсакӣ назанед ва сарашро бо болишти баланд набардоред.",
                "Агар шубҳаи афтидан ё осеби гардан бошад, сару баданро наҷунбонед."
            ),
            redFlags = listOf(
                "Беҳушӣ зиёда аз 1–2 дақиқа давом кунад ё нафаскашӣ қатъ гардад — ФАВРАН ба 103 занг занед.",
                "Дард дар қафаси сина, ташаннуҷ (судороги), лабҳои кабуд ё беҳушӣ ҳангоми ҳомиладорӣ."
            ),
            officialVideoUrl = "https://www.youtube.com/results?search_query=Red+Cross+recovery+position+fainting+first+aid",
            consultPrompt = "Ҳангоми сарчархзанӣ ва пеш аз беҳушшавӣ чӣ кор бояд кард ва кай бояд ба духтур муроҷиат кард?"
        ),
        FirstAidTopic(
            id = "fever",
            title = "Табларза ва ҳарорати баланд",
            shortBadge = "Табларза",
            summary = "Нигоҳубини бехатар дар хона, нӯшокиҳои гарм, компресси салқин ва назорати вазъият.",
            imageRes = R.drawable.img_fa_fever,
            videoDurationLabel = "0:16",
            animationType = FirstAidAnimationType.FEVER_CARE,
            videoScenes = listOf(
                FirstAidVideoScene(
                    stepNumber = 1,
                    title = "Ченкунии дақиқ ва ҳавои тоза",
                    caption = "Ҳароратро чен кунед, хонаро то 19–21°C шамол диҳед ва либоси пахтагини сабук пӯшонед."
                ),
                FirstAidVideoScene(
                    stepNumber = 2,
                    title = "Нӯшидани моеъи кофӣ",
                    caption = "Ҳар 15–20 дақиқа оби гарм, чойи сабук ё маҳлули регидрататсияро кам-кам диҳед."
                ),
                FirstAidVideoScene(
                    stepNumber = 3,
                    title = "Компресси салқин",
                    caption = "Матои нами ширгарм ё салқинро (на ях!) ба пешонӣ гузоред, то бемор худро роҳаттар ҳис кунад."
                ),
                FirstAidVideoScene(
                    stepNumber = 4,
                    title = "Огоҳӣ: спирт ва сирко манъ аст!",
                    caption = "Молидани спирт, арақ ё сирко заҳролудшавӣ ва рагбандиро меорад. Ҳангоми ташаннуҷ фавран 103 занг занед!",
                    isWarningScene = true
                )
            ),
            doSteps = listOf(
                "Моеъи гарми кофӣ диҳед ва либоси сабуки нафасгир пӯшонед.",
                "Ҳарорати хонаро салқин ва тоза нигоҳ доред, истироҳатро таъмин намоед.",
                "Тағйирёбии ҳарорат ва аломатҳои ҳамроҳро қайд кунед."
            ),
            doNotSteps = listOf(
                "Баданро бо спирт, арақ ё сирко намолед — хусусан дар кӯдакон хатарнок аст.",
                "Ҳангоми таби баланд беморро бо кӯрпаи ғафс напечонед.",
                "Бо оби ях оббозӣ накунед."
            ),
            redFlags = listOf(
                "Ҳарорат аз 39.5°C боло буда паст нашавад ё дар кӯдаки то 3-моҳа ҳарорат аз 38°C баланд шавад.",
                "Ташаннуҷ (судороги), доначаҳои бунафш дар пӯст, гардани шахшуда, нафастангӣ ё хоболудии сахт — фавран 103."
            ),
            officialVideoUrl = "https://www.youtube.com/results?search_query=WHO+fever+home+care+warning+signs",
            consultPrompt = "Ҳангоми табларза ва ҳарорати баланд дар хона кадом қадамҳои бехатарро анҷом диҳем?"
        ),
        FirstAidTopic(
            id = "wounds",
            title = "Захмҳо ва буридагиҳои пӯст",
            shortBadge = "Захмҳо",
            summary = "Шӯстани тоза бо оби равон, муҳофизат аз сироят ва бастани дурусти бандина.",
            imageRes = R.drawable.img_fa_wounds,
            videoDurationLabel = "0:16",
            animationType = FirstAidAnimationType.WOUNDS_CLEANSE,
            videoScenes = listOf(
                FirstAidVideoScene(
                    stepNumber = 1,
                    title = "Шӯстани дастҳо ва боздоштани хун",
                    caption = "Пеш аз расидан ба захм дастҳоро бо собун шӯед ва хунравии сабукро бо докаи тоза боздоред."
                ),
                FirstAidVideoScene(
                    stepNumber = 2,
                    title = "Шӯстани захм бо оби тоза",
                    caption = "Захмро зери оби тозаи равон 3–5 дақиқа шӯед, то чангу хок шуста шавад. Атрофро бо собун тоза кунед."
                ),
                FirstAidVideoScene(
                    stepNumber = 3,
                    title = "Бандинаи стерилии тоза",
                    caption = "Бо сачоқи тоза хушк намуда, бо лейкопластир ё бинти стерилӣ пӯшонед ва ҳар рӯз иваз кунед."
                ),
                FirstAidVideoScene(
                    stepNumber = 4,
                    title = "Кай ба духтур муроҷиат кунем?",
                    caption = "Захми чуқур, газидани ҳайвон, чирки зангдор ё сурхию фасодшавӣ муоинаи духтур ва пешгирии кузозро талаб мекунад!",
                    isWarningScene = true
                )
            ),
            doSteps = listOf(
                "Дастҳоро бо собун шӯста, захмро бо оби тозаи равон ё маҳлули физиологӣ бишӯед.",
                "Бо бандинаи тозаи стерилӣ пӯшонед ва то хушк шудан тоза нигоҳ доред.",
                "Агар захм аз ашёи ифлос бошад, санаи эмкунии охирини кузозро (столбняк) бисанҷед."
            ),
            doNotSteps = listOf(
                "Ба даруни захми кушода йод ё зелёнка нарезед — танҳо атрофи пӯстро коркард кунед.",
                "Ба захм пахта нагузоред — нахҳои пахта ба захм мечаспанд.",
                "Ҷисми дар захм фурӯрафтаро зӯран накашед."
            ),
            redFlags = listOf(
                "Захми чуқур ё калон, ки канорҳояш ба ҳам намеоянд ва ба дӯхтан ниёз дорад.",
                "Газидани сагу гурба, сурхии паҳншаванда, гарм шудани атрофи захм, фасод ё баланд шудани ҳарорат."
            ),
            officialVideoUrl = "https://www.youtube.com/results?search_query=Red+Cross+first+aid+cuts+and+wounds",
            consultPrompt = "Буридагии сабуки пӯст дорам. Чӣ тавр захмро тоза нигоҳ дорам ва аломатҳои сироятро сари вақт фаҳмам?"
        )
    )

    private fun russianTopics(): List<FirstAidTopic> = listOf(
        FirstAidTopic(
            id = "burns",
            title = "Ожоги (кипяток, пар, пламя)",
            shortBadge = "Ожоги",
            summary = "Немедленное охлаждение прохладной проточной водой 15–20 минут и защита кожи.",
            imageRes = R.drawable.img_fa_burns,
            videoDurationLabel = "0:16",
            animationType = FirstAidAnimationType.BURNS_COOL_WATER,
            videoScenes = listOf(
                FirstAidVideoScene(1, "Убрать источник тепла", "Остановите контакт с теплом и снимите кольца, часы или неприлипшую одежду до отёка."),
                FirstAidVideoScene(2, "Прохладная вода 15–20 мин", "Держите область под прохладной (не ледяной) проточной водой 15–20 минут."),
                FirstAidVideoScene(3, "Стерильное покрытие", "Накройте чистой неприлипающей стерильной повязкой без тугого бинтования."),
                FirstAidVideoScene(4, "Чего делать нельзя!", "Не наносите масло, сметану, зубную пасту или лёд и не прокалывайте пузыри!", isWarningScene = true)
            ),
            doSteps = listOf(
                "Охлаждайте ожог прохладной проточной водой 15–20 минут.",
                "Аккуратно снимите украшения и свободную одежду рядом до появления отёка.",
                "Закройте чистой неприлипающей повязкой."
            ),
            doNotSteps = listOf(
                "Не мажьте ожог маслом, сметаной, кефиром или зубной пастой.",
                "Не прикладывайте лёд и не вскрывайте пузыри."
            ),
            redFlags = listOf(
                "Ожог больше ладони, глубокий ожог или поражение лица, кистей, стоп.",
                "Ожог у ребёнка, химический или электрический ожог — звоните 103."
            ),
            officialVideoUrl = "https://www.youtube.com/results?search_query=Red+Cross+first+aid+burns+treatment",
            consultPrompt = "Подскажите, как безопасно ухаживать за лёгким бытовым ожогом после охлаждения водой?"
        ),
        FirstAidTopic(
            id = "bleeding",
            title = "Кровотечение",
            shortBadge = "Кровотечение",
            summary = "Прямое давление на рану чистой повязкой 10–15 минут и покой пострадавшего.",
            imageRes = R.drawable.img_fa_bleeding,
            videoDurationLabel = "0:16",
            animationType = FirstAidAnimationType.BLEEDING_PRESSURE,
            videoScenes = listOf(
                FirstAidVideoScene(1, "Безопасность и покой", "Усадите или уложите пострадавшего и по возможности наденьте чистые перчатки."),
                FirstAidVideoScene(2, "Прямое давление на рану", "Плотно прижмите чистую стерильную салфетку или ткань прямо к ране на 10–15 минут."),
                FirstAidVideoScene(3, "Дополнительный слой", "Если повязка пропиталась кровью, не снимайте её — положите сверху новый слой."),
                FirstAidVideoScene(4, "Когда звонить 103", "Если кровь бьёт пульсирующей струёй или не останавливается через 10–15 минут — звоните 103!", isWarningScene = true)
            ),
            doSteps = listOf(
                "Прижмите чистую повязку прямо к ране с устойчивым давлением.",
                "Если нет подозрения на перелом, приподнимите конечность.",
                "При пропитывании кровью накладывайте новые слои поверх первого."
            ),
            doNotSteps = listOf(
                "Не снимайте первую промокшую повязку.",
                "Не вытаскивайте инородные предметы, застрявшие в ране."
            ),
            redFlags = listOf(
                "Сильное пульсирующее кровотечение или кровь не останавливается 10–15 минут.",
                "Бледность, холодный пот, головокружение — срочно 103."
            ),
            officialVideoUrl = "https://www.youtube.com/results?search_query=Red+Cross+first+aid+severe+bleeding",
            consultPrompt = "Напомните безопасные шаги контроля кровотечения и опасные признаки."
        ),
        FirstAidTopic(
            id = "choking",
            title = "Удушье (попадание в дыхательные пути)",
            shortBadge = "Удушье",
            summary = "Помощь при перекрытии дыхательных путей: 5 ударов между лопатками и приём Геймлиха.",
            imageRes = R.drawable.img_fa_choking,
            videoDurationLabel = "0:16",
            animationType = FirstAidAnimationType.CHOKING_HEIMLICH,
            videoScenes = listOf(
                FirstAidVideoScene(1, "Оценка дыхания и кашля", "Если человек кашляет — поощряйте кашель. Если не может дышать или говорить — действуйте сразу."),
                FirstAidVideoScene(2, "5 ударов между лопатками", "Наклоните человека вперёд и нанесите 5 чётких ударов основанием ладони между лопаток."),
                FirstAidVideoScene(3, "5 толчков в живот (Геймлих)", "Обхватите сзади, поставьте кулак выше пупка и сделайте 5 быстрых толчков внутрь и вверх."),
                FirstAidVideoScene(4, "Потеря сознания — 103!", "При потере сознания немедленно звоните 103. Беременным толчки выполняются в область грудины.", isWarningScene = true)
            ),
            doSteps = listOf(
                "При сильном кашле дайте человеку откашляться самостоятельно.",
                "При отсутствии дыхания: чередуйте 5 ударов между лопатками и 5 абдоминальных толчков."
            ),
            doNotSteps = listOf(
                "Не пытайтесь достать предмет пальцами вслепую — можно протолкнуть глубже.",
                "Не делайте резких толчков в живот беременным и младенцам."
            ),
            redFlags = listOf(
                "Посинение губ, отсутствие вдоха и голоса.",
                "Потеря сознания — немедленно звоните 103."
            ),
            officialVideoUrl = "https://www.youtube.com/results?search_query=Red+Cross+first+aid+choking+Heimlich",
            consultPrompt = "Объясните алгоритм первой помощи при удушье у взрослых и детей."
        ),
        FirstAidTopic(
            id = "fainting",
            title = "Обморок (потеря сознания)",
            shortBadge = "Обморок",
            summary = "Свежий воздух, проверка дыхания и устойчивое боковое положение.",
            imageRes = R.drawable.img_fa_fainting,
            videoDurationLabel = "0:16",
            animationType = FirstAidAnimationType.FAINTING_RECOVERY,
            videoScenes = listOf(
                FirstAidVideoScene(1, "Проверка реакции и дыхания", "Осторожно потрогайте за плечо и оцените дыхание в течение 10 секунд."),
                FirstAidVideoScene(2, "Свежий воздух и приподнятые ноги", "Расстегните воротник, обеспечьте приток воздуха и при наличии дыхания приподнимите ноги на 20–30 см."),
                FirstAidVideoScene(3, "Боковое положение", "Если сознание не возвращается быстро или есть тошнота, аккуратно поверните на бок."),
                FirstAidVideoScene(4, "Не поите без сознания!", "Не вливайте воду человеку без сознания. Если обморок длится дольше 1–2 минут — звоните 103!", isWarningScene = true)
            ),
            doSteps = listOf(
                "Уложите пострадавшего, освободите шею от тесной одежды и откройте окно.",
                "При сохранённом дыхании приподнимите ноги или уложите в устойчивое боковое положение."
            ),
            doNotSteps = listOf(
                "Не вливайте воду или лекарства человеку без сознания.",
                "Не бейте по щекам и не поднимайте резко на ноги сразу после пробуждения."
            ),
            redFlags = listOf(
                "Сознание не возвращается дольше 1–2 минут или нет дыхания — срочно 103.",
                "Боль в груди, судороги, травма головы при падении."
            ),
            officialVideoUrl = "https://www.youtube.com/results?search_query=Red+Cross+recovery+position+fainting+first+aid",
            consultPrompt = "Что делать при предобморочном состоянии и когда обморок требует вызова врача?"
        ),
        FirstAidTopic(
            id = "fever",
            title = "Лихорадка и высокий жар",
            shortBadge = "Жар",
            summary = "Безопасное охлаждение, восполнение жидкости и контроль опасных симптомов.",
            imageRes = R.drawable.img_fa_fever,
            videoDurationLabel = "0:16",
            animationType = FirstAidAnimationType.FEVER_CARE,
            videoScenes = listOf(
                FirstAidVideoScene(1, "Прохладный воздух и лёгкая одежда", "Проветрите комнату (19–21°C) и оставьте лёгкую хлопковую одежду."),
                FirstAidVideoScene(2, "Тёплое питьё дробно", "Давайте тёплую воду, морс или раствор для регидратации небольшими глотками."),
                FirstAidVideoScene(3, "Прохладный компресс", "Приложите ко лбу ткань, смоченную слегка прохладной (не ледяной) водой."),
                FirstAidVideoScene(4, "Запрещены спирт и уксус!", "Растирания спиртом или уксусом опасны отравлением. При судорогах немедленно звоните 103!", isWarningScene = true)
            ),
            doSteps = listOf(
                "Поддерживайте достаточное питьё и комфортную прохладу в комнате.",
                "Записывайте время измерения температуры."
            ),
            doNotSteps = listOf(
                "Не растирайте тело спиртом, водкой или уксусом.",
                "Не укутывайте человека с жаром тёплыми одеялами."
            ),
            redFlags = listOf(
                "Температура выше 39.5°C или выше 38°C у младенца до 3 месяцев.",
                "Судороги, сыпь, ригидность шеи, одышка — звоните 103."
            ),
            officialVideoUrl = "https://www.youtube.com/results?search_query=WHO+fever+home+care+warning+signs",
            consultPrompt = "Какие безопасные шаги ухода помогают при высокой температуре дома?"
        ),
        FirstAidTopic(
            id = "wounds",
            title = "Раны и порезы",
            shortBadge = "Раны",
            summary = "Промывание чистой проточной водой и защита стерильной повязкой.",
            imageRes = R.drawable.img_fa_wounds,
            videoDurationLabel = "0:16",
            animationType = FirstAidAnimationType.WOUNDS_CLEANSE,
            videoScenes = listOf(
                FirstAidVideoScene(1, "Чистые руки", "Перед обработкой раны вымойте руки с мылом и остановите небольшое кровотечение салфеткой."),
                FirstAidVideoScene(2, "Промывание чистой водой", "Промойте рану чистой проточной водой 3–5 минут, удаляя мелкую грязь."),
                FirstAidVideoScene(3, "Стерильная повязка", "Аккуратно промокните и закройте стерильным пластырем или бинтом."),
                FirstAidVideoScene(4, "Осмотр у врача", "При глубоком порезе, укусе животного или покраснении с гноем обратитесь к врачу!", isWarningScene = true)
            ),
            doSteps = listOf(
                "Промойте рану чистой водой и закройте стерильной повязкой.",
                "Ежедневно проверяйте края раны на чистоту и сухость."
            ),
            doNotSteps = listOf(
                "Не заливайте йод или зелёнку прямо внутрь открытой раны.",
                "Не прикладывайте вату прямо на раневую поверхность."
            ),
            redFlags = listOf(
                "Глубокая рана с расходящимися краями, укус животного.",
                "Нарастающее покраснение, жар, отёк, гной или отсутствие прививки от столбняка."
            ),
            officialVideoUrl = "https://www.youtube.com/results?search_query=Red+Cross+first+aid+cuts+and+wounds",
            consultPrompt = "Как правильно очистить и защитить небольшой бытовой порез дома?"
        )
    )

    private fun uzbekTopics(): List<FirstAidTopic> = listOf(
        FirstAidTopic(
            id = "burns",
            title = "Kuyish (qaynoq suv, bug', olov)",
            shortBadge = "Kuyish",
            summary = "Dastlabki 15–20 daqiqada salqin oqayotgan suv bilan sovutish va terini himoyalash.",
            imageRes = R.drawable.img_fa_burns,
            videoDurationLabel = "0:16",
            animationType = FirstAidAnimationType.BURNS_COOL_WATER,
            videoScenes = listOf(
                FirstAidVideoScene(1, "Issiqlik ta'sirini to'xtatish", "Issiqlik manbaidan uzoqlashing va shishdan oldin uzuk yoki soatni yeching."),
                FirstAidVideoScene(2, "Salqin suv 15–20 daqiqa", "Kuygan joyni salqin (muzdek emas) oqayotgan suv ostida 15–20 daqiqa tuting."),
                FirstAidVideoScene(3, "Toza steril bog'lam", "Yopishmaydigan toza steril doka bilan yengil yoping."),
                FirstAidVideoScene(4, "Nimalar mumkin emas!", "Yog', smetana, tish pastasi yoki muz surtmang va pufakchalarni yormang!", isWarningScene = true)
            ),
            doSteps = listOf(
                "Kuygan joyni 15–20 daqiqa salqin oqayotgan suvda sovuting.",
                "Uzuk, soat va siqib turgan kiyimni ehtiyotkorlik bilan yeching."
            ),
            doNotSteps = listOf(
                "Yog', smetana yoki tish pastasi surtmang.",
                "Pufakchalarni teshmang."
            ),
            redFlags = listOf(
                "Kaftdan katta yoki chuqur kuyish, yuz/qo'l/oyoq kuyishi.",
                "Yosh boladagi kuyish, kimyoviy yoki elektr kuyishi — 103."
            ),
            officialVideoUrl = "https://www.youtube.com/results?search_query=Red+Cross+first+aid+burns+treatment",
            consultPrompt = "Yengil kuyishdan keyin terini uyda qanday xavfsiz parvarish qilish mumkin?"
        ),
        FirstAidTopic(
            id = "bleeding",
            title = "Qon ketishi",
            shortBadge = "Qon ketish",
            summary = "Toza mato yoki steril doka bilan to'g'ridan-to'g'ri bosib turish.",
            imageRes = R.drawable.img_fa_bleeding,
            videoDurationLabel = "0:16",
            animationType = FirstAidAnimationType.BLEEDING_PRESSURE,
            videoScenes = listOf(
                FirstAidVideoScene(1, "Tinch holat", "Jabrlanuvchini o'tqazing yoki yotqizing."),
                FirstAidVideoScene(2, "To'g'ridan-to'g'ri bosim", "Toza doka bilan yaraga 10–15 daqiqa mahkam bosing."),
                FirstAidVideoScene(3, "Yangi qavat qo'shish", "Qon o'tsa, birinchi dokani olmang — ustidan yangisini qo'ying."),
                FirstAidVideoScene(4, "103 ga qo'ng'iroq", "Qon 10–15 daqiqada to'xtamasa yoki kuchli otilib chiqsa — darhol 103!", isWarningScene = true)
            ),
            doSteps = listOf("Yaraga toza mato bilan to'g'ridan-to'g'ri bosim o'tkazing."),
            doNotSteps = listOf("Birinchi qonli bog'lamni yulib olmang."),
            redFlags = listOf("Kuchli qon ketishi yoki bosh aylanishi — zudlik bilan 103."),
            officialVideoUrl = "https://www.youtube.com/results?search_query=Red+Cross+first+aid+severe+bleeding",
            consultPrompt = "Qon ketishida birinchi yordam va xavfli belgilar haqida ma'lumot bering."
        ),
        FirstAidTopic(
            id = "choking",
            title = "Nafas yo'li tiqilishi",
            shortBadge = "Tiqilish",
            summary = "Kuraklar orasiga 5 zarba va Geymlix usuli bilan nafas yo'lini ochish.",
            imageRes = R.drawable.img_fa_choking,
            videoDurationLabel = "0:16",
            animationType = FirstAidAnimationType.CHOKING_HEIMLICH,
            videoScenes = listOf(
                FirstAidVideoScene(1, "Yo'talni tekshirish", "Agar yo'talsa — yo'talishga undang. Nafas ololmasa darhol yordam bering."),
                FirstAidVideoScene(2, "Kuraklar orasiga 5 zarba", "Oldinga egib kuraklar orasiga 5 marta kaft asosi bilan uring."),
                FirstAidVideoScene(3, "5 qorin bosimi (Geymlix)", "Orqadan quchoqlab kindikdan yuqoriga ichkariga va yuqoriga 5 marta bosing."),
                FirstAidVideoScene(4, "Hushdan ketsa — 103!", "Darhol 103 ga qo'ng'iroq qiling.", isWarningScene = true)
            ),
            doSteps = listOf("5 kurak zarbasi va 5 Geymlix bosimini navbatma-navbat bajaring."),
            doNotSteps = listOf("Barmoq bilan ko'r-ko'rona tomoqqa kirmang."),
            redFlags = listOf("Lab ko'karishi, nafas ololmaslik yoki hushdan ketish — 103."),
            officialVideoUrl = "https://www.youtube.com/results?search_query=Red+Cross+first+aid+choking+Heimlich",
            consultPrompt = "Nafas yo'li tiqilganda birinchi yordam tartibini tushuntiring."
        ),
        FirstAidTopic(
            id = "fainting",
            title = "Hushdan ketish",
            shortBadge = "Hushdan ketish",
            summary = "Toza havo, nafasni tekshirish va xavfsiz yonbosh holat.",
            imageRes = R.drawable.img_fa_fainting,
            videoDurationLabel = "0:16",
            animationType = FirstAidAnimationType.FAINTING_RECOVERY,
            videoScenes = listOf(
                FirstAidVideoScene(1, "Nafasni tekshirish", "Yelkadan ohista tegib ko'ring va 10 soniya nafasini kuzating."),
                FirstAidVideoScene(2, "Oyoqlarni ko'tarish", "Yoqani bo'shating, toza havo kiriting va oyoqlarni 20–30 sm ko'taring."),
                FirstAidVideoScene(3, "Xavfsiz yonbosh holat", "Agar tez o'ziga kelmasa, ehtiyotkorlik bilan yonboshga yotqizing."),
                FirstAidVideoScene(4, "Suv ichirmang!", "Hushsiz odamga suv quyish mumkin emas. 1–2 daqiqadan oshsa — 103!", isWarningScene = true)
            ),
            doSteps = listOf("Xavfsiz yotqizing, yoqani bo'shating va toza havo bering."),
            doNotSteps = listOf("Hushsiz holatda suv yoki dori ichirmang."),
            redFlags = listOf("1–2 daqiqadan ortiq hushsizlik yoki nafas yo'qligi — 103."),
            officialVideoUrl = "https://www.youtube.com/results?search_query=Red+Cross+recovery+position+fainting+first+aid",
            consultPrompt = "Hushdan ketishdan oldingi belgilar va xavfsiz yordam haqida ayting."
        ),
        FirstAidTopic(
            id = "fever",
            title = "Isitma va yuqori harorat",
            shortBadge = "Isitma",
            summary = "Suyuqlik ichirish, xonani shamollatish va xavfsiz salqin kompress.",
            imageRes = R.drawable.img_fa_fever,
            videoDurationLabel = "0:16",
            animationType = FirstAidAnimationType.FEVER_CARE,
            videoScenes = listOf(
                FirstAidVideoScene(1, "Xonani shamollatish", "Xonani 19–21°C da tuting va yengil paxtali kiyim kiydiring."),
                FirstAidVideoScene(2, "Iliq suyuqlik", "Tez-tez kichik qultumlarda iliq suv yoki choy ichiring."),
                FirstAidVideoScene(3, "Salqin kompress", "Peshonaga iliq-salqin nam mato qo'ying."),
                FirstAidVideoScene(4, "Spirt va sirka taqiqlanadi!", "Spirt yoki sirka bilan artmang. Talvasa bo'lsa — 103!", isWarningScene = true)
            ),
            doSteps = listOf("Ko'proq iliq suyuqlik bering va haroratni nazorat qiling."),
            doNotSteps = listOf("Spirt yoki sirka bilan artmang, qalin ko'rpa o'ramang."),
            redFlags = listOf("39.5°C dan yuqori harorat, talvasa, toshma — 103."),
            officialVideoUrl = "https://www.youtube.com/results?search_query=WHO+fever+home+care+warning+signs",
            consultPrompt = "Yuqori haroratda uyda qanday xavfsiz yordam berish kerak?"
        ),
        FirstAidTopic(
            id = "wounds",
            title = "Yaralar va kesilishlar",
            shortBadge = "Yaralar",
            summary = "Toza oqayotgan suv bilan yuvish va steril bog'lam bilan himoyalash.",
            imageRes = R.drawable.img_fa_wounds,
            videoDurationLabel = "0:16",
            animationType = FirstAidAnimationType.WOUNDS_CLEANSE,
            videoScenes = listOf(
                FirstAidVideoScene(1, "Qo'lni yuvish", "Yaraga tegishdan oldin qo'lingizni sovunlab yuving."),
                FirstAidVideoScene(2, "Suv bilan yuvish", "Yarani 3–5 daqiqa toza oqayotgan suvda yuving."),
                FirstAidVideoScene(3, "Steril bog'lam", "Toza steril plastir yoki bint bilan yoping."),
                FirstAidVideoScene(4, "Shifokorga murojaat", "Chuqur yara, hayvon tishlashi yoki yiringlashda shifokorga murojaat qiling!", isWarningScene = true)
            ),
            doSteps = listOf("Toza suvda yuving va steril bog'lam bilan yoping."),
            doNotSteps = listOf("Ochiq yara ichiga yod quymang va paxta bosmang."),
            redFlags = listOf("Chuqur kesilish, hayvon tishlashi, qizarish va yiringlash."),
            officialVideoUrl = "https://www.youtube.com/results?search_query=Red+Cross+first+aid+cuts+and+wounds",
            consultPrompt = "Kichik kesilgan yarani uyda qanday to'g'ri parvarish qilish kerak?"
        )
    )
}
