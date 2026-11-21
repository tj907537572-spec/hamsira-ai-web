package com.example.data.model

object CareGuidesCatalog {

    fun quickPrompts(language: AppLanguage): List<QuickPrompt> = when (language) {
        AppLanguage.TG -> listOf(
            QuickPrompt(
                id = "fever",
                title = "Ҳарорати 38.5°C дар хона",
                prompt = "Дар одами калонсол ҳарорат то 38.5°C баланд шуд, беҳолӣ ва табларза ҳаст. Дар хона кадом корҳои бехатарро анҷом додан мумкин аст ва кай ба духтур муроҷиат кардан лозим аст?",
                categoryBadge = "Аломатҳо"
            ),
            QuickPrompt(
                id = "bp",
                title = "Фишори хунро чӣ тавр дуруст чен кунем",
                prompt = "Қадам ба қадам фаҳмонед, ки бо тонометр фишори хунро чӣ тавр дуруст чен кардан лозим аст ва кадом нишондиҳандаҳо сабаби муроҷиат ба духтур мешаванд?",
                categoryBadge = "Назорат"
            ),
            QuickPrompt(
                id = "dehydration",
                title = "Аломатҳои беобшавии бадан",
                prompt = "Аломатҳои аввалини беобшавӣ дар калонсолон ва кӯдакон кадомҳоянд ва дар хона моеъи баданро чӣ тавр бехатар барқарор кардан мумкин аст?",
                categoryBadge = "Нигоҳубин"
            ),
            QuickPrompt(
                id = "burn",
                title = "Ёрии аввал ҳангоми сӯхтани сабук",
                prompt = "Ҳангоми сӯхтани сабуки маишӣ аз оби ҷӯш ё буғ дарҳол чӣ бояд кард ва кадом корҳоро кардан қатъиян мумкин нест?",
                categoryBadge = "Ёрии аввал"
            ),
            QuickPrompt(
                id = "meds",
                title = "Қабули бехатари доруҳо",
                prompt = "Қоидаҳои асосии қабули бехатари доруҳоро дар хона хотиррасон кунед: чӣ тавр бо воя, мувофиқати доруҳо ва хӯрок хато накунем?",
                categoryBadge = "Дастур"
            ),
            QuickPrompt(
                id = "sleep",
                title = "Бехобӣ ва изтироб",
                prompt = "Рӯзҳои охир хобам ноором шудааст ва аз хастагӣ набзам тез мезанад. Кадом роҳҳои нарми бе дору барои ором шудан пеш аз хоб кӯмак мекунанд?",
                categoryBadge = "Барқароршавӣ"
            )
        )

        AppLanguage.RU -> listOf(
            QuickPrompt(
                id = "fever",
                title = "Температура 38.5°C дома",
                prompt = "У взрослого поднялась температура 38.5°C, есть слабость и озноб. Какие безопасные шаги по уходу дома и когда нужно вызывать врача?",
                categoryBadge = "Симптомы"
            ),
            QuickPrompt(
                id = "bp",
                title = "Как правильно измерить давление",
                prompt = "Подскажите пошагово, как правильно измерить артериальное давление тонометром и какие показатели считаются поводом для обращения к врачу?",
                categoryBadge = "Контроль"
            ),
            QuickPrompt(
                id = "dehydration",
                title = "Признаки обезвоживания",
                prompt = "Какие ранние признаки обезвоживания у взрослого и ребёнка, и как безопасно восполнять жидкость дома?",
                categoryBadge = "Уход"
            ),
            QuickPrompt(
                id = "burn",
                title = "Первая помощь при лёгком ожоге",
                prompt = "Что делать сразу при небольшом бытовом ожоге кипятком или паром, а чего делать категорически нельзя?",
                categoryBadge = "Первая помощь"
            ),
            QuickPrompt(
                id = "meds",
                title = "Безопасный приём лекарств",
                prompt = "Напомните главные правила безопасного приёма лекарств дома: как не ошибиться с дозировкой, совместимостью и приёмом до/после еды?",
                categoryBadge = "Памятка"
            ),
            QuickPrompt(
                id = "sleep",
                title = "Тревожность и плохой сон",
                prompt = "В последние дни плохой сон и учащённый пульс от усталости. Какие мягкие, немедикаментозные способы помогают успокоиться перед сном?",
                categoryBadge = "Восстановление"
            )
        )

        AppLanguage.UZ_LATN -> listOf(
            QuickPrompt(
                id = "fever",
                title = "Harorat 38.5°C bo'lganda",
                prompt = "Katta odamda tana harorati 38.5°C ga ko'tarildi, holsizlik va titroq bor. Uyda qanday xavfsiz parvarish qilish mumkin va qachon shifokorga murojaat qilish kerak?",
                categoryBadge = "Alomatlar"
            ),
            QuickPrompt(
                id = "bp",
                title = "Qon bosimini to'g'ri o'lchash",
                prompt = "Tonometr bilan qon bosimini to'g'ri o'lchash qoidalarini bosqichma-bosqich tushuntiring va qanday ko'rsatkichlarda shifokorga ko'rinish kerak?",
                categoryBadge = "Nazorat"
            ),
            QuickPrompt(
                id = "dehydration",
                title = "Suvsizlanish belgilari",
                prompt = "Kattalar va bolalarda suvsizlanishning dastlabki belgilari qanday va uyda suyuqlikni qanday to'g'ri tiklash mumkin?",
                categoryBadge = "Parvarish"
            ),
            QuickPrompt(
                id = "burn",
                title = "Yengil kuyishda birinchi yordam",
                prompt = "Kichik maishiy kuyishda darhol nima qilish kerak va nimalarni qilish qat'iyan mumkin emas?",
                categoryBadge = "Birinchi yordam"
            ),
            QuickPrompt(
                id = "meds",
                title = "Dorilarni xavfsiz qabul qilish",
                prompt = "Uyda dorilarni xavfsiz qabul qilishning asosiy qoidalarini eslating: doza va ovqatdan oldin/keyin ichish tartibi haqida.",
                categoryBadge = "Eslatma"
            ),
            QuickPrompt(
                id = "sleep",
                title = "Uyqusizlik va charchoq",
                prompt = "Oxirgi kunlarda uyqu yomonlashdi va charchoqdan yurak tez urmoqda. Uyqudan oldin tinchlanish uchun qanday xavfsiz usullar yordam beradi?",
                categoryBadge = "Tiklanish"
            )
        )
    }

    fun careGuides(language: AppLanguage): List<CareGuide> = when (language) {
        AppLanguage.TG -> listOf(
            CareGuide(
                id = "guide_fever",
                category = GuideCategory.SYMPTOMS,
                title = "Табларза ва ҳарорати баланд",
                summary = "Чӣ тавр ҳангоми шамолкашӣ ба бадан кӯмак расонем ва нишонаҳои хатарнокро сари вақт бинем.",
                safeSteps = listOf(
                    "Оби гарм, чойи сабук ё нӯшокиҳои шифобахшро кам-кам, вале тез-тез нӯшед.",
                    "Ҳавои хонаро тоза ва салқин (19–21°C) нигоҳ доред ва либоси пахтагини сабук пӯшед.",
                    "Ҳар 3–4 соат ҳароратро чен карда, вақти истеъмоли доруҳоро қайд кунед."
                ),
                watchOut = listOf(
                    "Ҳангоми ҳарорати баланд беморро бо кӯрпаи ғафс напечонед — ин ба берун рафтани гармӣ халал мерасонад.",
                    "Бо спирт ё сирко молидани бадан, махсусан дар кӯдакон, манъ аст."
                ),
                redFlags = listOf(
                    "Ҳарорат аз 39.5°C баланд буда, паст нашавад ё зиёда аз 3 рӯз давом кунад.",
                    "Нафастангӣ, гиҷшавӣ, дарди сахти сар, доначаҳо дар пӯст ё ташаннуҷ."
                ),
                consultPrompt = "Дар ман ё наздикам ҳарорат баланд шудааст. Лутфан бигӯед, ки дар хона чӣ тавр бехатар нигоҳубин кунем ва ба чӣ диққат диҳем?"
            ),
            CareGuide(
                id = "guide_bp",
                category = GuideCategory.DAILY_CARE,
                title = "Назорати фишори хун",
                summary = "Қоидаҳои дақиқи чен кардани фишор бо тонометр ва пешбурди дафтарчаи саломатӣ.",
                safeSteps = listOf(
                    "Пеш аз ченкунӣ 5 дақиқа ором нишинед; пушт ба курсии такядор, пойҳо ба ҳам печонда нашаванд.",
                    "Манжетро дар сатҳи дил, 2 см болотар аз буғуми оринҷ бандед.",
                    "30 дақиқа пеш аз ченкунӣ аз қаҳва, чойи талх ва роҳгардии тез худдорӣ кунед."
                ),
                watchOut = listOf(
                    "Бо фосилаи 1–2 дақиқа 2 маротиба чен кунед ва ҳар ду натиҷаро бо набз нависед.",
                    "Ҳатто агар фишор муътадил шуда бошад, доруҳои таъинкардаи духтурро худсарона қатъ накунед."
                ),
                redFlags = listOf(
                    "Фишори 180/120 мм сут.симоб ва болотар ё бадшавии якбораи вазъият.",
                    "Дард ё фишурдашавӣ дар қафаси сина, карахтии рӯй ё даст, вайроншавии нутқ ё нафастангӣ — фавран 103."
                ),
                consultPrompt = "Фаҳмонед, ки дафтарчаи фишори хунро чӣ тавр нигоҳ дорем ва кадом аломатҳо ҳангоми фишори баланд ё паст ёрии таъҷилиро талаб мекунанд?"
            ),
            CareGuide(
                id = "guide_burn",
                category = GuideCategory.FIRST_AID,
                title = "Ёрии аввал ҳангоми сӯхтани маишӣ",
                summary = "Амалҳои дуруст дар 15 дақиқаи аввал ҳангоми сӯхтани сабуки гармӣ.",
                safeSteps = listOf(
                    "Ҷои сӯхтаро зери оби равони салқин (на яхбаста) ба муддати 15–20 дақиқа нигоҳ доред.",
                    "Пеш аз пайдо шудани варам ангуштарин, соат ё либоси тангро эҳтиёткорона бароред.",
                    "Дар ҳолати зарурӣ бо бандинаи тозаи стерилии начаспанда пӯшонед."
                ),
                watchOut = listOf(
                    "Ба ҷои сӯхтаи нав равған, сметана, хамираи дандон ё малҳамҳои худсохт намолед.",
                    "Обилаҳои пайдошударо накафонед — онҳо пӯстро аз сироят муҳофизат мекунанд."
                ),
                redFlags = listOf(
                    "Сӯхтане, ки андозааш аз кафи даст калон аст, сӯхтани амиқ ё осеби рӯй, дастҳо ва пойҳо.",
                    "Сӯхтан дар кӯдаки хурдсол, сӯхтани кимиёвӣ ё барқӣ."
                ),
                consultPrompt = "Сӯхтани сабуки маишӣ рӯй дод. Пас аз хунук кардан бо об пӯстро чӣ тавр бехатар нигоҳубин кунем ва кай ҳатман ба духтур муроҷиат кунем?"
            ),
            CareGuide(
                id = "guide_allergy",
                category = GuideCategory.FIRST_AID,
                title = "Реаксияи шадиди аллергия",
                summary = "Чӣ тавр аллергияи сабуки пӯстро аз ҳолати таъҷилӣ фарқ кунем.",
                safeSteps = listOf(
                    "Тамос бо аллергентро (ғизо, дору, газидани ҳашарот, косметика) фавран қатъ кунед.",
                    "Ҳангоми хориши маҳаллӣ пӯстро бо оби салқин шӯед ва ҳавои тозаро таъмин намоед.",
                    "Вақти дақиқи пайдо шудани доначаҳо ва сабаби эҳтимолиро нависед."
                ),
                watchOut = listOf(
                    "Дар давоми 1–2 соати аввал овоз, нафаскашӣ ва лабу пилкҳоро бодиққат назорат кунед.",
                    "Бе донистани зиддинишондодҳо доруҳои тасодуфӣ истеъмол накунед."
                ),
                redFlags = listOf(
                    "Варами лабҳо, забон, пилкҳо ё гулӯ, гирифтани овоз, нафасгирии душвор ё садои ҳуштак.",
                    "Сарчархзании сахт, арақи хунук, пастшавии фишор ё беҳушшавӣ — фавран ба 103 занг занед."
                ),
                consultPrompt = "Доначаҳо ва хориши ба аллергия монанд пайдо шуд. Ҳоло бояд ба кадом аломатҳои хатарнок диққат диҳам?"
            ),
            CareGuide(
                id = "guide_gi",
                category = GuideCategory.SYMPTOMS,
                title = "Вайроншавии меъда ва барқароркунии об",
                summary = "Пур кардани моеъ ва низоми нарм ҳангоми дилбеҳузурӣ ё дарунравӣ.",
                safeSteps = listOf(
                    "Кам-кам нӯшед: ҳар 5–10 дақиқа 1–2 қошуқ об ё маҳлули регидрататсия.",
                    "Дар соатҳои аввал ба меъда оромӣ диҳед, сипас ғизои сабук (оби биринҷ, нони хушк, банан) хӯред.",
                    "Пас аз ҳар дафъаи ҳоҷатхона ва пеш аз хӯрок дастҳоро бо собун шӯед."
                ),
                watchOut = listOf(
                    "То барқароршавӣ аз нӯшокиҳои газдори ширин, шир ва хӯрокҳои равғанин парҳез кунед.",
                    "Ҳангоми ҳарорати баланд бе маслиҳати духтур доруҳои зидди дарунравӣ нахӯред."
                ),
                redFlags = listOf(
                    "Пайдо шудани хун дар наҷосат ё қай, наҷосати сиёҳранг.",
                    "Қайкунии беист, дарди сахти шикам, хушкшавии забон ва набудани пешоб зиёда аз 6–8 соат."
                ),
                consultPrompt = "Дилбеҳузурӣ ё вайроншавии меъда ҳаст. Чӣ тавр дар хона дуруст моеъ нӯшем, то беобшавӣ рух надиҳад?"
            ),
            CareGuide(
                id = "guide_stress",
                category = GuideCategory.DAILY_CARE,
                title = "Гигиенаи хоб ва коҳиши стресс",
                summary = "Таҷрибаи барқароршавии ором ҳангоми хастагии зиёд ва изтироб.",
                safeSteps = listOf(
                    "Пеш аз хоб хонаро шамол диҳед ва 45–60 дақиқа пеш аз хоб телефон ва экранҳоро канор гузоред.",
                    "Нафаскашии «4–6»-ро санҷед: 4 сония ором нафас кашед ва 6 сония оҳиста бароред (3–5 дақиқа).",
                    "Кӯшиш кунед ҳатто рӯзҳои истироҳат дар як вақт хобида, бедор шавед."
                ),
                watchOut = listOf(
                    "Дар нимаи дуюми рӯз кофеин ва пеш аз хоб хӯроки вазнинро кам кунед.",
                    "Агар бехобӣ зиёда аз 2–3 ҳафта давом кунад, бо духтури оилавӣ машварат кунед."
                ),
                redFlags = listOf(
                    "Нафастангии сахти ногаҳонӣ, дард дар қафаси сина ё тапиши тези дил дар ҳолати оромӣ.",
                    "Хастагии шадид ё ҳамлаҳои изтиробие, ки ба ҳаёти муқаррарӣ халал мерасонанд."
                ),
                consultPrompt = "Ман хастагӣ ва душворӣ дар хоб рафтанро эҳсос мекунам. Як нақшаи нарми шомгоҳӣ барои оромшавӣ пешниҳод кунед."
            )
        )

        AppLanguage.RU -> listOf(
            CareGuide(
                id = "guide_fever",
                category = GuideCategory.SYMPTOMS,
                title = "Лихорадка и высокая температура",
                summary = "Как поддержать организм при ОРВИ, не перегреться и вовремя заметить опасные признаки.",
                safeSteps = listOf(
                    "Обеспечьте тёплое обильное питьё небольшими глотками (вода, морс, слабый чай).",
                    "Поддерживайте прохладный свежий воздух в комнате (19–21°C) и лёгкую одежду из хлопка.",
                    "Измеряйте температуру каждые 3–4 часа и записывайте время приёма лекарств."
                ),
                watchOut = listOf(
                    "Не укутывайте человека плотными одеялами при жаре — это мешает теплоотдаче.",
                    "Не используйте растирания спиртом или уксусом, особенно у детей."
                ),
                redFlags = listOf(
                    "Температура выше 39.5°C и плохо снижается, либо держится дольше 3 суток.",
                    "Одышка, спутанность сознания, сильная головная боль, сыпь или судороги."
                ),
                consultPrompt = "У меня или близкого человека температура при ОРВИ. Подскажите, на что обратить внимание и как безопасно организовать уход дома?"
            ),
            CareGuide(
                id = "guide_bp",
                category = GuideCategory.DAILY_CARE,
                title = "Контроль артериального давления",
                summary = "Правила точного измерения давления тонометром и ведения дневника показателей.",
                safeSteps = listOf(
                    "Отдохните сидя 5 минут перед измерением; спина опирается на спинку стула, ноги не скрещены.",
                    "Манжета надевается на плечо на уровне сердца, на 2 см выше локтевого сгиба.",
                    "За 30 минут до замера избегайте кофе, крепкого чая, курения и активной ходьбы."
                ),
                watchOut = listOf(
                    "Выполните 2 замера с интервалом 1–2 минуты и запишите оба значения с пульсом.",
                    "Не прекращайте назначенные врачом препараты самостоятельно, даже если давление нормализовалось."
                ),
                redFlags = listOf(
                    "Давление 180/120 мм рт. ст. и выше или резкий скачок с ухудшением самочувствия.",
                    "Боль или сдавление в груди, онемение лица/руки, нарушение речи, резкая слабость или одышка."
                ),
                consultPrompt = "Расскажите, как вести дневник артериального давления и какие симптомы при высоком или низком давлении требуют срочной помощи?"
            ),
            CareGuide(
                id = "guide_burn",
                category = GuideCategory.FIRST_AID,
                title = "Первая помощь при бытовом ожоге",
                summary = "Правильные действия в первые 15 минут при лёгком термическом ожоге.",
                safeSteps = listOf(
                    "Охлаждайте место ожога прохладной (не ледяной) проточной водой 15–20 минут.",
                    "Аккуратно снимите кольца, часы или свободную одежду рядом с участком до появления отёка.",
                    "При необходимости накройте чистой неприлипающей стерильной повязкой."
                ),
                watchOut = listOf(
                    "Не наносите на свежий ожог масло, сметану, зубную пасту, кефир или мази.",
                    "Не вскрывайте образовавшиеся пузыри — они защищают кожу от инфекции."
                ),
                redFlags = listOf(
                    "Ожог размером больше ладони пострадавшего, глубокий ожог или поражение лица, кистей, стоп.",
                    "Ожог у маленького ребёнка, химический или электрический ожог."
                ),
                consultPrompt = "Произошёл небольшой бытовой ожог. Как безопасно ухаживать за кожей после охлаждения водой и когда обязательно показаться врачу?"
            ),
            CareGuide(
                id = "guide_allergy",
                category = GuideCategory.FIRST_AID,
                title = "Острая аллергическая реакция",
                summary = "Как отличить лёгкую кожную реакцию от состояния, требующего экстренной помощи.",
                safeSteps = listOf(
                    "Немедленно прекратите контакт с предполагаемым аллергеном (пища, препарат, укус, косметика).",
                    "При локальном кожном зуде промойте кожу прохладной водой и обеспечьте приток свежего воздуха.",
                    "Запишите точное время появления сыпи и что ей предшествовало."
                ),
                watchOut = listOf(
                    "Наблюдайте за голосом, дыханием и губами/веками в течение первых 1–2 часов.",
                    "Не принимайте случайные лекарства без понимания противопоказаний."
                ),
                redFlags = listOf(
                    "Отёк губ, языка, век или горла, осиплость голоса, затруднённое дыхание или свист при вдохе.",
                    "Резкое головокружение, падение давления, холодный пот или потеря сознания — звоните 103 немедленно."
                ),
                consultPrompt = "Появилась кожная сыпь или зуд, похожие на аллергию. На какие опасные признаки нужно следить прямо сейчас?"
            ),
            CareGuide(
                id = "guide_gi",
                category = GuideCategory.SYMPTOMS,
                title = "Расстройство желудка и регидратация",
                summary = "Восполнение жидкости и щадящий режим при тошноте или диарее.",
                safeSteps = listOf(
                    "Пейте дробно: по 1–2 столовые ложки воды или солевого раствора для регидратации каждые 5–10 минут.",
                    "В первые часы дайте желудку покой, затем выбирайте лёгкую пищу (рисовый отвар, сухари, банан).",
                    "Мойте руки после каждого посещения туалета и перед едой."
                ),
                watchOut = listOf(
                    "Избегайте сладких газировок, цельного молока, жирной и острой пищи до восстановления.",
                    "Не принимайте противодиарейные средства при высокой температуре без консультации врача."
                ),
                redFlags = listOf(
                    "Примесь крови в стуле или рвоте, чёрный дегтеобразный стул.",
                    "Неукротимая рвота, сильная локальная боль в животе, сухой язык и отсутствие мочи более 6–8 часов."
                ),
                consultPrompt = "Есть тошнота или расстройство желудка. Как правильно отпаиваться дома, чтобы не допустить обезвоживания?"
            ),
            CareGuide(
                id = "guide_stress",
                category = GuideCategory.DAILY_CARE,
                title = "Гигиена сна и снижение стресса",
                summary = "Практика спокойного восстановления при переутомлении и тревожности.",
                safeSteps = listOf(
                    "Проветрите спальню перед сном и отложите экраны за 45–60 минут до засыпания.",
                    "Попробуйте дыхание «4–6»: плавный вдох на 4 счёта и медленный спокойный выдох на 6 счётов 3–5 минут.",
                    "Старайтесь ложиться и просыпаться в одно и то же время даже в выходные."
                ),
                watchOut = listOf(
                    "Ограничьте кофеин во второй половине дня и плотные ужины прямо перед сном.",
                    "Если бессонница длится больше 2–3 недель, обсудите это с терапевтом."
                ),
                redFlags = listOf(
                    "Внезапная сильная одышка, боли в груди или длительное выраженное сердцебиение в покое.",
                    "Выраженное истощение, панические приступы, мешающие обычной жизни."
                ),
                consultPrompt = "Я испытываю усталость и трудности с засыпанием. Подскажите мягкий вечерний план восстановления без перегрузки."
            )
        )

        AppLanguage.UZ_LATN -> listOf(
            CareGuide(
                id = "guide_fever",
                category = GuideCategory.SYMPTOMS,
                title = "Isitma va yuqori tana harorati",
                summary = "ORVI paytida organizmni qo'llab-quvvatlash va xavfli belgilarni o'z vaqtida payqash.",
                safeSteps = listOf(
                    "Iliq suyuqlikni (suv, choy, mors) tez-tez, kichik qultumlarda iching.",
                    "Xonani shamollating (19–21°C) va yengil paxtali kiyim kiydiring.",
                    "Haroratni har 3–4 soatda o'lchab, vaqti va ko'rsatkichini yozib boring."
                ),
                watchOut = listOf(
                    "Isitma baland bo'lganda qalin ko'rpa bilan o'ramang — bu issiqlik chiqishiga xalaqit beradi.",
                    "Spirt yoki sirka bilan artishdan saqlaning, ayniqsa bolalarda."
                ),
                redFlags = listOf(
                    "Harorat 39.5°C dan oshsa, tushmasa yoki 3 kundan ortiq davom etsa.",
                    "Nafas qisilishi, kuchli bosh og'rig'i, toshma, hushning xiralashishi yoki talvasa."
                ),
                consultPrompt = "O'zimda yoki yaqinimda isitma bor. Uyda xavfsiz parvarish qilish bo'yicha maslahat bering."
            ),
            CareGuide(
                id = "guide_bp",
                category = GuideCategory.DAILY_CARE,
                title = "Qon bosimini nazorat qilish",
                summary = "Tonometrda aniq o'lchash va ko'rsatkichlar kundaligini yuritish qoidalari.",
                safeSteps = listOf(
                    "O'lchashdan oldin 5 daqiqa tinch o'tiring, orqa suyangan va oyoqlar chalkashtirilmagan bo'lsin.",
                    "Manjetani yurak sathida, tirsak bukilishidan 2 sm yuqoriga taqing.",
                    "30 daqiqa oldin kofe, achchiq choy va jismoniy zo'riqishdan saqlaning."
                ),
                watchOut = listOf(
                    "1–2 daqiqa tanaffus bilan 2 marta o'lchab, puls bilan birga qayd eting.",
                    "Shifokor buyurgan dorilarni o'zboshimchalik bilan to'xtatmang."
                ),
                redFlags = listOf(
                    "Bosim 180/120 mm sim.ust. dan yuqori bo'lishi yoki ahvol keskin yomonlashishi.",
                    "Ko'krakda og'riq, yuz yoki qo'l uvishishi, nutq buzilishi, kuchli holsizlik — zudlik bilan 103."
                ),
                consultPrompt = "Qon bosimi kundaligini qanday yuritish va qanday holatlarda tez yordam chaqirish kerakligini tushuntiring."
            ),
            CareGuide(
                id = "guide_burn",
                category = GuideCategory.FIRST_AID,
                title = "Yengil kuyishda birinchi yordam",
                summary = "Kuyishdan keyingi dastlabki 15 daqiqada to'g'ri harakatlar.",
                safeSteps = listOf(
                    "Kuygan joyni salqin (muzdek emas) oqayotgan suv ostida 15–20 daqiqa tuting.",
                    "Shish paydo bo'lishidan oldin uzuk, soat yoki siqib turgan kiyimni ehtiyotkorlik bilan yeching.",
                    "Zarurat bo'lsa, toza yopishmaydigan steril bog'lam bilan yoping."
                ),
                watchOut = listOf(
                    "Yangi kuygan joyga yog', smetana, tish pastasi yoki xalqona vositalar surtmang.",
                    "Paydo bo'lgan pufakchalarni yormang."
                ),
                redFlags = listOf(
                    "Kuyish maydoni kaftdan katta bo'lsa, chuqur bo'lsa yoki yuz, qo'l kafti, oyoqda bo'lsa.",
                    "Yosh boladagi kuyish, kimyoviy yoki elektr kuyishi."
                ),
                consultPrompt = "Yengil maishiy kuyish yuz berdi. Suv bilan sovutgandan keyin terini qanday parvarish qilish kerak?"
            ),
            CareGuide(
                id = "guide_allergy",
                category = GuideCategory.FIRST_AID,
                title = "Allergik reaksiya belgilari",
                summary = "Yengil teri reaksiyasini shoshilinch holatdan ajratish.",
                safeSteps = listOf(
                    "Taxmin qilingan allergen (taom, dori, hasharot chaqishi) bilan aloqani darhol to'xtating.",
                    "Teri qichishganda salqin suv bilan yuving va toza havo kirishini ta'minlang.",
                    "Toshma qachon boshlanganini va undan oldin nima iste'mol qilinganini yozib qo'ying."
                ),
                watchOut = listOf(
                    "Dastlabki 1–2 soat ichida ovoz, nafas olish va lab/qovoqlarni kuzating.",
                    "Qarshi ko'rsatmalarni bilmay turib tasodifiy dorilar ichmang."
                ),
                redFlags = listOf(
                    "Lab, til, qovoq yoki tomoq shishi, ovoz bo'g'ilishi, nafas qisilishi.",
                    "Keskin bosh aylanishi, sovuq ter yoki hushdan ketish — zudlik bilan 103."
                ),
                consultPrompt = "Allergiyaga o'xshash toshma va qichishish paydo bo'ldi. Hozir qaysi xavfli belgilarga e'tibor berishim kerak?"
            ),
            CareGuide(
                id = "guide_gi",
                category = GuideCategory.SYMPTOMS,
                title = "Oshqozon buzilishi va suvsizlanish",
                summary = "Ko'ngil aynishi yoki ich ketishida suyuqlikni xavfsiz tiklash.",
                safeSteps = listOf(
                    "Har 5–10 daqiqada 1–2 osh qoshiqdan toza suv yoki regidratatsiya eritmasi iching.",
                    "Birinchi soatlarda oshqozonga dam bering, keyin yengil taom (guruch qaynatmasi, suxari, banan) tanlang.",
                    "Qo'l gigiyenasiga qat'iy rioya qiling."
                ),
                watchOut = listOf(
                    "Tiklanish davrida shirin gazli ichimliklar, sut va yog'li taomlardan saqlaning.",
                    "Yuqori isitma bo'lsa, shifokor maslahatisiz ichni to'xtatuvchi dorilar ichmang."
                ),
                redFlags = listOf(
                    "Najas yoki qusuqda qon bo'lishi, qora rangli najas.",
                    "To'xtovsiz qusish, kuchli qorin og'rig'i, quruq til va 6–8 soatdan ortiq peshob bo'lmasligi."
                ),
                consultPrompt = "Ko'ngil aynishi va oshqozon buzilishi bor. Suvsizlanishning oldini olish uchun uyda qanday to'g'ri suyuqlik ichish kerak?"
            ),
            CareGuide(
                id = "guide_stress",
                category = GuideCategory.DAILY_CARE,
                title = "Uyqu gigiyenasi va stressni kamaytirish",
                summary = "Charchoq va xavotir paytida tinch tiklanish amaliyoti.",
                safeSteps = listOf(
                    "Uxlashdan oldin xonani shamollating va 45–60 daqiqa oldin telefon/ekranlardan uzoqlashing.",
                    "«4–6» nafas mashqini sinab ko'ring: 4 sanab nafas oling, 6 sanab sekin chiqaring (3–5 daqiqa).",
                    "Dam olish kunlari ham bir xil vaqtda uxlab uyg'onishga harakat qiling."
                ),
                watchOut = listOf(
                    "Kunning ikkinchi yarmida kofe va kechki og'ir ovqatdan saqlaning.",
                    "Uyqusizlik 2–3 haftadan ortiq davom etsa, shifokor bilan maslahatlashing."
                ),
                redFlags = listOf(
                    "Tinch holatda kuchli nafas qisilishi, ko'krak og'rig'i yoki uzoq davom etuvchi yurak urishi.",
                    "Kundalik hayotga xalaqit beradigan kuchli holsizlik."
                ),
                consultPrompt = "Charchoq va uyquga ketishda qiynalish bor. Kechqurun tinchlanish uchun yumshoq reja tuzib bering."
            )
        )
    }
}
